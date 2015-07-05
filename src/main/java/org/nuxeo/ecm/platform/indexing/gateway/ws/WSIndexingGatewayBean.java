package org.nuxeo.ecm.platform.indexing.gateway.ws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
import org.nuxeo.ecm.platform.api.ws.DocumentSnapshot;
import org.nuxeo.ecm.platform.api.ws.NuxeoRemoting;
import org.nuxeo.ecm.platform.api.ws.WsACE;
import org.nuxeo.ecm.platform.api.ws.session.WSRemotingSession;
import org.nuxeo.ecm.platform.audit.api.AuditException;
import org.nuxeo.ecm.platform.audit.ws.EventDescriptorPage;
import org.nuxeo.ecm.platform.audit.ws.ModifiedDocumentDescriptor;
import org.nuxeo.ecm.platform.audit.ws.ModifiedDocumentDescriptorPage;
import org.nuxeo.ecm.platform.audit.ws.WSAuditBean;
import org.nuxeo.ecm.platform.audit.ws.api.WSAudit;
import org.nuxeo.ecm.platform.indexing.gateway.adapter.IndexingAdapter;
import org.nuxeo.ecm.platform.indexing.gateway.ws.api.WSIndexingGateway;
import org.nuxeo.ecm.platform.ws.AbstractNuxeoWebService;
import org.nuxeo.ecm.platform.ws.NuxeoRemotingBean;
import org.nuxeo.runtime.api.Framework;

/**
 * Base class for WS beans used for external indexers. Implements most of NuxeoRemotingBean trying as hard as possible
 * no to throw ClientException when a requested document is missing but returning empty descriptions instead so as to
 * make external indexers not view recently deleted documents as applicative errors.
 * 
 * @author tiry
 */
@WebService(name = "WSIndexingGatewayInterface", serviceName = "WSIndexingGatewayService")
@SOAPBinding(style = Style.DOCUMENT)
public class WSIndexingGatewayBean extends AbstractNuxeoWebService implements WSIndexingGateway {

    protected static final String ENFORCE_SYNC_PROP_NAME = "nuxeo.indexing.gateway.forceSync";

    protected static Log log = LogFactory.getLog(WSIndexingGatewayBean.class);

    private static final long serialVersionUID = 4696352633818100451L;

    protected transient WSAudit auditBean;

    protected transient NuxeoRemoting platformRemoting;

    protected IndexingAdapter adapter;

    protected ConcurrentHashMap<String, ReentrantLock> sessionIdLocks = new ConcurrentHashMap<String, ReentrantLock>();

    protected Boolean enforceSync = null;

    protected boolean forceSync() {
        if (enforceSync == null) {
            String value = Framework.getProperty(ENFORCE_SYNC_PROP_NAME, null);
            enforceSync = false;
            if (value != null) {
                enforceSync = Boolean.parseBoolean(value);
            } else {
                enforceSync = false;
            }
        }
        return enforceSync;
    }

    protected void lockSession(String sid) {
        if (forceSync()) {
            ReentrantLock lock = sessionIdLocks.putIfAbsent(sid, new ReentrantLock());
            boolean aquired = false;
            if (lock == null) {
                lock = sessionIdLocks.get(sid);
            }
            try {
                aquired = lock.tryLock(10, TimeUnit.SECONDS);
            } catch (Throwable e) {
                log.error("Failed to acquire lock for sid " + sid, e);
            }
            if (!aquired) {
                log.error("Failed to acquire lock (timeout) for sid " + sid);
            }
        }
    }

    protected void releaseSession(String sid) {
        if (forceSync()) {
            ReentrantLock lock = sessionIdLocks.get(sid);
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    protected WSAudit getWSAudit() throws AuditException {
        if (auditBean == null) {
            auditBean = new WSAuditBean();
        }
        return auditBean;
    }

    protected NuxeoRemoting getWSNuxeoRemoting() {
        if (platformRemoting == null) {
            platformRemoting = new NuxeoRemotingBean();
        }
        return platformRemoting;
    }

    protected IndexingAdapter getAdapter() {
        if (adapter == null) {
            adapter = Framework.getLocalService(IndexingAdapter.class);
            if (adapter == null) {
                throw new ClientException("could not find IntuitionAdapterService");
            }
        }
        return adapter;
    }

    @WebMethod
    public DocumentDescriptor[] getChildren(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "uuid") String uuid) {
        try {
            lockSession(sessionId);
            CoreSession session = initSession(sessionId).getDocumentManager();
            if (session.exists(new IdRef(uuid))) {
                return getWSNuxeoRemoting().getChildren(sessionId, uuid);
            } else {
                return new DocumentDescriptor[0];
            }
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public DocumentDescriptor getCurrentVersion(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uid) {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            if (session.exists(new IdRef(uid))) {
                return getWSNuxeoRemoting().getCurrentVersion(sid, uid);
            } else {
                return missingDocumentDescriptor(uid);
            }
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public DocumentDescriptor getDocument(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "uuid") String uuid) {
        try {
            lockSession(sessionId);
            CoreSession session = initSession(sessionId).getDocumentManager();
            DocumentDescriptor dd;
            if (session.exists(new IdRef(uuid))) {
                dd = getWSNuxeoRemoting().getDocument(sessionId, uuid);
            } else {
                dd = missingDocumentDescriptor(uuid);
            }
            return getAdapter().adaptDocumentDescriptor(session, uuid, dd);
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public WsACE[] getDocumentACL(@WebParam(name = "sessionId") String sid, @WebParam(name = "uuid") String uuid)
            {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            WsACE[] aces;
            if (session.exists(new IdRef(uuid))) {
                aces = getWSNuxeoRemoting().getDocumentACL(sid, uuid);
            } else {
                aces = new WsACE[0];
            }
            return getAdapter().adaptDocumentACL(session, uuid, aces);
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public WsACE[] getDocumentLocalACL(@WebParam(name = "sessionId") String sid, @WebParam(name = "uuid") String uuid)
            {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            WsACE[] aces;
            if (session.exists(new IdRef(uuid))) {
                aces = getWSNuxeoRemoting().getDocumentLocalACL(sid, uuid);
            } else {
                aces = new WsACE[0];
            }
            return getAdapter().adaptDocumentLocalACL(session, uuid, aces);
        } finally {
            releaseSession(sid);
        }
    }

    public DocumentBlob[] getDocumentBlobsExt(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uuid, @WebParam(name = "useDownloadUrl") boolean useDownloadUrl)
            {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            DocumentBlob[] blobs;
            if (session.exists(new IdRef(uuid))) {
                blobs = getWSNuxeoRemoting().getDocumentBlobsExt(sid, uuid, useDownloadUrl);
            } else {
                blobs = new DocumentBlob[0];
            }
            return getAdapter().adaptDocumentBlobs(session, uuid, blobs);

        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public DocumentBlob[] getDocumentBlobs(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uuid) {
        return getDocumentBlobsExt(sid, uuid, getAdapter().useDownloadUrlForBlob());
    }

    @WebMethod
    public DocumentProperty[] getDocumentNoBlobProperties(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uuid) {

        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            DocumentProperty[] properties;
            if (session.exists(new IdRef(uuid))) {
                properties = getWSNuxeoRemoting().getDocumentNoBlobProperties(sid, uuid);
            } else {
                properties = new DocumentProperty[0];
            }
            return getAdapter().adaptDocumentNoBlobProperties(session, uuid, properties);
        } finally {
            releaseSession(sid);
        }

    }

    @WebMethod
    public DocumentProperty[] getDocumentProperties(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uuid) {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            DocumentProperty[] properties;
            if (session.exists(new IdRef(uuid))) {
                properties = getWSNuxeoRemoting().getDocumentProperties(sid, uuid);
            } else {
                properties = new DocumentProperty[0];
            }
            return getAdapter().adaptDocumentProperties(session, uuid, properties);
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public String[] getGroups(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "parentGroup") String parentGroup) {
        return getWSNuxeoRemoting().getGroups(sid, parentGroup);
    }

    @WebMethod
    public String getRepositoryName(@WebParam(name = "sessionId") String sid) {
        return getWSNuxeoRemoting().getRepositoryName(sid);
    }

    @WebMethod
    public DocumentDescriptor getRootDocument(@WebParam(name = "sessionId") String sessionId) {
        try {
            lockSession(sessionId);
            return getWSNuxeoRemoting().getRootDocument(sessionId);
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public String resolvePathToUUID(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "path") String path)
            {
        try {
            lockSession(sessionId);
            CoreSession session = initSession(sessionId).getDocumentManager();
            if (session != null) {
                PathRef pathRef = new PathRef(path);
                if (session.exists(pathRef)) {
                    return session.getDocument(pathRef).getId();
                }
            }
            return null;
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public UUIDPage getRecursiveChildrenUUIDsByPage(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uuid, @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize) {

        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();

            List<String> uuids = new ArrayList<String>();
            IdRef parentRef = new IdRef(uuid);
            DocumentModel parent = session.getDocument(parentRef);
            String path = parent.getPathAsString();

            String query = "select ecm:uuid from Document where ecm:path startswith '" + path + "' order by ecm:uuid";

            IterableQueryResult result = session.queryAndFetch(query, "NXQL");
            boolean hasMore = false;
            try {
                if (page > 1) {
                    int skip = (page - 1) * pageSize;
                    result.skipTo(skip);
                }

                for (Map<String, Serializable> record : result) {
                    uuids.add((String) record.get(NXQL.ECM_UUID));
                    if (uuids.size() == pageSize) {
                        hasMore = true;
                        break;
                    }
                }
            } finally {
                result.close();
            }
            return new UUIDPage(uuids.toArray(new String[uuids.size()]), page, hasMore);
        } finally {
            releaseSession(sid);
        }

    }

    @WebMethod
    public String[] getRecursiveChildrenUUIDs(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uuid) {

        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();

            List<String> uuids = new ArrayList<String>();
            IdRef parentRef = new IdRef(uuid);
            DocumentModel parent = session.getDocument(parentRef);
            String path = parent.getPathAsString();

            String query = "select ecm:uuid from Document where ecm:path startswith '" + path + "' order by ecm:uuid";

            IterableQueryResult result = session.queryAndFetch(query, "NXQL");

            try {
                for (Map<String, Serializable> record : result) {
                    uuids.add((String) record.get(NXQL.ECM_UUID));
                }
            } finally {
                result.close();
            }

            return uuids.toArray(new String[uuids.size()]);
        } finally {
            releaseSession(sid);
        }

    }

    @WebMethod
    public DocumentTypeDescriptor[] getTypeDefinitions() {

        List<DocumentTypeDescriptor> result = new ArrayList<DocumentTypeDescriptor>();
        SchemaManager sm = null;
        try {
            sm = Framework.getService(SchemaManager.class);
        } catch (Exception e) {
            throw new ClientException("Unable to access SchemaManager", e);
        }

        for (DocumentType dt : sm.getDocumentTypes()) {
            result.add(new DocumentTypeDescriptor(dt));
        }

        return result.toArray(new DocumentTypeDescriptor[result.size()]);
    }

    @WebMethod
    public DocumentDescriptor getDocumentFromPath(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "path") String path) {
        try {
            lockSession(sessionId);
            String uuid = resolvePathToUUID(sessionId, path);
            if (uuid != null) {
                return getWSNuxeoRemoting().getDocument(sessionId, uuid);
            } else {
                // should we return a missing document with an null uuid
                // instead?
                return null;
            }
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public DocumentDescriptor getSourceDocument(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uid) {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            if (session.exists(new IdRef(uid))) {
                return getWSNuxeoRemoting().getSourceDocument(sid, uid);
            } else {
                return missingDocumentDescriptor(uid);
            }
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public String[] getUsers(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "parentGroup") String parentGroup) {
        return getWSNuxeoRemoting().getUsers(sid, parentGroup);
    }

    @WebMethod
    public DocumentDescriptor[] getVersions(@WebParam(name = "sessionId") String sid,
            @WebParam(name = "uuid") String uid) {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            if (session.exists(new IdRef(uid))) {
                return getWSNuxeoRemoting().getVersions(sid, uid);
            } else {
                return new DocumentDescriptor[0];
            }
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public String[] listGroups(@WebParam(name = "sessionId") String sid, @WebParam(name = "from") int from,
            @WebParam(name = "to") int to) {
        return getWSNuxeoRemoting().listGroups(sid, from, to);
    }

    @WebMethod
    public String[] listUsers(@WebParam(name = "sessionId") String sid, @WebParam(name = "from") int from,
            @WebParam(name = "to") int to) {
        return getWSNuxeoRemoting().listUsers(sid, from, to);
    }

    @WebMethod
    public ModifiedDocumentDescriptor[] listModifiedDocuments(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dateRangeQuery") String dateRangeQuery) throws AuditException {
        return getWSAudit().listModifiedDocuments(sessionId, dateRangeQuery);
    }

    @WebMethod
    public ModifiedDocumentDescriptorPage listModifiedDocumentsByPage(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dateRangeQuery") String dateRangeQuery, @WebParam(name = "path") String path,
            @WebParam(name = "page") int page, @WebParam(name = "pageSize") int pageSize) throws AuditException {
        return getWSAudit().listModifiedDocumentsByPage(sessionId, dateRangeQuery, path, page, pageSize);
    }

    @WebMethod
    public EventDescriptorPage listEventsByPage(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dateRangeQuery") String dateRangeQuery, @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize) throws AuditException {
        return getWSAudit().listEventsByPage(sessionId, dateRangeQuery, page, pageSize);
    }

    @WebMethod
    public EventDescriptorPage listDocumentEventsByPage(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dateRangeQuery") String dateRangeQuery, @WebParam(name = "startDate") String startDate,
            @WebParam(name = "path") String path, @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize) throws AuditException {
        return getWSAudit().listDocumentEventsByPage(sessionId, dateRangeQuery, startDate, path, page, pageSize);
    }

    @WebMethod
    public String getRelativePathAsString(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "uuid") String uuid) {
        try {
            lockSession(sessionId);
            CoreSession session = initSession(sessionId).getDocumentManager();
            if (session.exists(new IdRef(uuid))) {
                return getWSNuxeoRemoting().getRelativePathAsString(sessionId, uuid);
            } else {
                return null;
            }
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public boolean hasPermission(@WebParam(name = "sessionId") String sid, @WebParam(name = "uuid") String uuid,
            @WebParam(name = "permission") String permission) {
        try {
            lockSession(sid);
            CoreSession session = initSession(sid).getDocumentManager();
            if (session.exists(new IdRef(uuid))) {
                return getWSNuxeoRemoting().hasPermission(sid, uuid, permission);
            } else {
                return false;
            }
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public String uploadDocument(@WebParam(name = "sessionId") String sid, String path, String type, String[] properties)
            {
        try {
            lockSession(sid);
            return getWSNuxeoRemoting().uploadDocument(sid, path, type, properties);
        } finally {
            releaseSession(sid);
        }
    }

    @WebMethod
    public String connect(@WebParam(name = "userName") String username, @WebParam(name = "password") String password)
            {
        return getWSNuxeoRemoting().connect(username, password);
    }

    @WebMethod
    public void disconnect(@WebParam(name = "sessionId") String sid) {
        getWSNuxeoRemoting().disconnect(sid);
        if (forceSync()) {
            ReentrantLock lock = sessionIdLocks.get(sid);
            if (lock != null) {
                if (lock.isLocked()) {
                    lock.unlock();
                }
                sessionIdLocks.remove(sid);
            }
        }
    }

    @WebMethod
    public EventDescriptorPage queryEventsByPage(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "whereClause") String whereClause, @WebParam(name = "pageIndex") int page,
            @WebParam(name = "pageSize") int pageSize) throws AuditException {
        return getWSAudit().queryEventsByPage(sessionId, whereClause, page, pageSize);
    }

    @WebMethod
    public boolean validateUserPassword(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "username") String username, @WebParam(name = "password") String password)
            {
        WSRemotingSession rs = initSession(sessionId);
        return rs.getUserManager().checkUsernamePassword(username, password);
    }

    @WebMethod
    public String[] getUserGroups(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "username") String username) {
        WSRemotingSession rs = initSession(sessionId);
        List<String> groups = rs.getUserManager().getPrincipal(username).getAllGroups();
        String[] groupArray = new String[groups.size()];
        groups.toArray(groupArray);
        return groupArray;
    }

    public DocumentSnapshot getDocumentSnapshotExt(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "uuid") String uuid, @WebParam(name = "useDownloadUrl") boolean useDownloadUrl)
            {

        try {
            lockSession(sessionId);
            WSRemotingSession rs = initSession(sessionId);
            DocumentModel doc = rs.getDocumentManager().getDocument(new IdRef(uuid));

            DocumentProperty[] props = getDocumentNoBlobProperties(sessionId, uuid);
            DocumentBlob[] blobs = getDocumentBlobs(sessionId, uuid);

            WsACE[] resACP = null;

            ACP acp = doc.getACP();
            if (acp != null && acp.getACLs().length > 0) {
                ACL acl = acp.getMergedACLs("MergedACL");
                resACP = WsACE.wrap(acl.getACEs());
            }
            DocumentSnapshot ds = new DocumentSnapshot(props, blobs, doc.getPathAsString(), resACP);
            return ds;
        } finally {
            releaseSession(sessionId);
        }
    }

    @WebMethod
    public DocumentSnapshot getDocumentSnapshot(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "uuid") String uuid) {
        return getDocumentSnapshotExt(sessionId, uuid, getAdapter().useDownloadUrlForBlob());
    }

    public ModifiedDocumentDescriptorPage listDeletedDocumentsByPage(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataRangeQuery") String dateRangeQuery, @WebParam(name = "docPath") String path,
            @WebParam(name = "pageIndex") int page, @WebParam(name = "pageSize") int pageSize) throws AuditException {

        return getWSAudit().listDeletedDocumentsByPage(sessionId, dateRangeQuery, path, page, pageSize);
    }

    /**
     * Utility method to build descriptor for a document that is non longer to be found in the repository.
     * 
     * @param uuid
     * @return
     */
    protected DocumentDescriptor missingDocumentDescriptor(String uuid) {
        // TODO: if we have to make the API / WSDL evolve it would be nice to
        // include an explicit attribute in DocumentDescriptor to mark missing
        // documents
        DocumentDescriptor dd = new DocumentDescriptor();
        dd.setUUID(uuid);
        return dd;
    }

}
