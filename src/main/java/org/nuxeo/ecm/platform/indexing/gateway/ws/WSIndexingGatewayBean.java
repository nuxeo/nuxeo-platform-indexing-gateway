package org.nuxeo.ecm.platform.indexing.gateway.ws;

import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.naming.NamingException;

import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
import org.nuxeo.ecm.platform.api.ws.DocumentSnapshot;
import org.nuxeo.ecm.platform.api.ws.NuxeoRemoting;
import org.nuxeo.ecm.platform.api.ws.session.WSRemotingSession;
import org.nuxeo.ecm.platform.audit.api.AuditException;
import org.nuxeo.ecm.platform.audit.ws.EventDescriptorPage;
import org.nuxeo.ecm.platform.audit.ws.ModifiedDocumentDescriptor;
import org.nuxeo.ecm.platform.audit.ws.ModifiedDocumentDescriptorPage;
import org.nuxeo.ecm.platform.audit.ws.api.WSAudit;
import org.nuxeo.ecm.platform.audit.ws.delegate.WSAuditBeanBusinessDelegate;
import org.nuxeo.ecm.platform.indexing.gateway.adapter.IndexingAdapter;
import org.nuxeo.ecm.platform.indexing.gateway.ws.api.WSIndexingGateway;
import org.nuxeo.ecm.platform.ws.AbstractNuxeoWebService;
import org.nuxeo.ecm.platform.ws.delegate.NuxeoRemotingBeanBusinessDelegate;
import org.nuxeo.runtime.api.Framework;

@Stateless
@SerializedConcurrentAccess
@WebService(name = "WSIndexingGatewayInterface", serviceName = "WSIndexingGatewayService")
@SOAPBinding(style = Style.DOCUMENT)
public class WSIndexingGatewayBean extends AbstractNuxeoWebService implements
        WSIndexingGateway {

    private static final long serialVersionUID = 4696352633818100451L;

    protected final WSAuditBeanBusinessDelegate auditBeanDelegate = new WSAuditBeanBusinessDelegate();

    protected transient WSAudit auditBean;

    protected final NuxeoRemotingBeanBusinessDelegate platformBeanDelegate = new NuxeoRemotingBeanBusinessDelegate();

    protected transient NuxeoRemoting platformRemoting;

    protected IndexingAdapter adapter;

    protected WSAudit getWSAudit() throws AuditException {
        if (auditBean == null) {
            try {
                auditBean = auditBeanDelegate.getWSAuditRemote();
            } catch (NamingException ne) {
                throw new AuditException("Cannot find audit bean...");
            }
        }
        if (auditBean == null) {
            throw new AuditException("Cannot find audit bean...");
        }
        return auditBean;
    }

    protected NuxeoRemoting getWSNuxeoRemoting() throws ClientException {
        if (platformRemoting == null) {
            try {
                platformRemoting = platformBeanDelegate.getWSNuxeoRemotingRemote();
            } catch (NamingException ne) {
                throw new ClientException("Cannot find nuxeo remoting bean...");
            }
        }

        if (platformRemoting == null) {
            throw new ClientException("Cannot find nuxeo remoting bean...");
        }
        return platformRemoting;
    }

    protected IndexingAdapter getAdapter() throws ClientException {
        if (adapter == null) {
            adapter = Framework.getLocalService(IndexingAdapter.class);
            if (adapter == null) {
                throw new ClientException(
                        "could not find IntuitionAdapterService");
            }
        }
        return adapter;
    }

    @WebMethod
    public DocumentDescriptor[] getChildren(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        return getWSNuxeoRemoting().getChildren(sessionId, uuid);
    }

    @WebMethod
    public DocumentDescriptor getCurrentVersion(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uid) throws ClientException {
        return getWSNuxeoRemoting().getCurrentVersion(sid, uid);
    }

    @WebMethod
    public DocumentDescriptor getDocument(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        CoreSession session = initSession(sessionId).getDocumentManager();
        DocumentDescriptor dd = getWSNuxeoRemoting().getDocument(sessionId,
                uuid);
        return getAdapter().adaptDocumentDescriptor(session, uuid, dd);
    }

    @WebMethod
    public ACE[] getDocumentACL(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        CoreSession session = initSession(sid).getDocumentManager();
        ACE[] aces = getWSNuxeoRemoting().getDocumentACL(sid, uuid);
        return getAdapter().adaptDocumentACL(session, uuid, aces);
    }

    @WebMethod
    public ACE[] getDocumentLocalACL(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        CoreSession session = initSession(sid).getDocumentManager();
        ACE[] aces = getWSNuxeoRemoting().getDocumentLocalACL(sid, uuid);
        return getAdapter().adaptDocumentLocalACL(session, uuid, aces);
    }

    @WebMethod
    public DocumentBlob[] getDocumentBlobs(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        CoreSession session = initSession(sid).getDocumentManager();
        DocumentBlob[] blobs = getWSNuxeoRemoting().getDocumentBlobs(sid, uuid);
        return getAdapter().adaptDocumentBlobs(session, uuid, blobs);
    }

    @WebMethod
    public DocumentProperty[] getDocumentNoBlobProperties(
            @WebParam(name = "sessionId")
            String sid, @WebParam(name = "uuid")
            String uuid) throws ClientException {
        CoreSession session = initSession(sid).getDocumentManager();
        DocumentProperty[] properties = getWSNuxeoRemoting().getDocumentNoBlobProperties(
                sid, uuid);
        return getAdapter().adaptDocumentNoBlobProperties(session, uuid,
                properties);
    }

    @WebMethod
    public DocumentProperty[] getDocumentProperties(
            @WebParam(name = "sessionId")
            String sid, @WebParam(name = "uuid")
            String uuid) throws ClientException {
        CoreSession session = initSession(sid).getDocumentManager();
        DocumentProperty[] properties = getWSNuxeoRemoting().getDocumentProperties(
                sid, uuid);
        return getAdapter().adaptDocumentProperties(session, uuid, properties);
    }

    @WebMethod
    public String[] getGroups(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "parentGroup")
    String parentGroup) throws ClientException {
        return getWSNuxeoRemoting().getGroups(sid, parentGroup);
    }

    @WebMethod
    public String getRepositoryName(@WebParam(name = "sessionId")
    String sid) throws ClientException {
        return getWSNuxeoRemoting().getRepositoryName(sid);
    }

    @WebMethod
    public DocumentDescriptor getRootDocument(@WebParam(name = "sessionId")
    String sessionId) throws ClientException {
        return getWSNuxeoRemoting().getRootDocument(sessionId);
    }

    @WebMethod
    public String resolvePathToUUID(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "path")
    String path) throws ClientException {
        CoreSession session = initSession(sessionId).getDocumentManager();
        if (session != null) {
            DocumentModel doc = session.getDocument(new PathRef(path));
            return doc.getId();
        } else
            return null;
    }

    @WebMethod
    public DocumentDescriptor getDocumentFromPath(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "path")
    String path) throws ClientException {
        String uuid = resolvePathToUUID(sessionId, path);
        return getWSNuxeoRemoting().getDocument(sessionId, uuid);
    }

    @WebMethod
    public DocumentDescriptor getSourceDocument(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uid) throws ClientException {
        return getWSNuxeoRemoting().getSourceDocument(sid, uid);
    }

    @WebMethod
    public String[] getUsers(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "parentGroup")
    String parentGroup) throws ClientException {
        return getWSNuxeoRemoting().getUsers(sid, parentGroup);
    }

    @WebMethod
    public DocumentDescriptor[] getVersions(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uid) throws ClientException {
        return getWSNuxeoRemoting().getVersions(sid, uid);
    }

    @WebMethod
    public String[] listGroups(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "from")
    int from, @WebParam(name = "to")
    int to) throws ClientException {
        return getWSNuxeoRemoting().listGroups(sid, from, to);
    }

    @WebMethod
    public String[] listUsers(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "from")
    int from, @WebParam(name = "to")
    int to) throws ClientException {
        return getWSNuxeoRemoting().listUsers(sid, from, to);
    }

    @WebMethod
    public ModifiedDocumentDescriptor[] listModifiedDocuments(
            @WebParam(name = "sessionId")
            String sessionId, @WebParam(name = "dateRangeQuery")
            String dateRangeQuery) throws AuditException {
        return getWSAudit().listModifiedDocuments(sessionId, dateRangeQuery);
    }

    @WebMethod
    public ModifiedDocumentDescriptorPage listModifiedDocumentsByPage(
            @WebParam(name = "sessionId")
            String sessionId, @WebParam(name = "dateRangeQuery")
            String dateRangeQuery, @WebParam(name = "path")
            String path, @WebParam(name = "page")
            int page, @WebParam(name = "pageSize")
            int pageSize) throws AuditException {
        return getWSAudit().listModifiedDocumentsByPage(sessionId,
                dateRangeQuery, path, page, pageSize);
    }

    @WebMethod
    public EventDescriptorPage listEventsByPage(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "dateRangeQuery")
    String dateRangeQuery, @WebParam(name = "page")
    int page, @WebParam(name = "pageSize")
    int pageSize) throws AuditException {
        return getWSAudit().listEventsByPage(sessionId, dateRangeQuery, page,
                pageSize);
    }

    @WebMethod
    public EventDescriptorPage listDocumentEventsByPage(
            @WebParam(name = "sessionId")
            String sessionId, @WebParam(name = "dateRangeQuery")
            String dateRangeQuery, @WebParam(name = "startDate")
            String startDate, @WebParam(name = "path")
            String path, @WebParam(name = "page")
            int page, @WebParam(name = "pageSize")
            int pageSize) throws AuditException {
        return getWSAudit().listDocumentEventsByPage(sessionId, dateRangeQuery,
                startDate, path, page, pageSize);
    }

    @WebMethod
    public String getRelativePathAsString(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        return getWSNuxeoRemoting().getRelativePathAsString(sessionId, uuid);
    }

    @WebMethod
    public boolean hasPermission(@WebParam(name = "sessionId")
    String sid, @WebParam(name = "uuid")
    String uuid, @WebParam(name = "permission")
    String permission) throws ClientException {
        return getWSNuxeoRemoting().hasPermission(sid, uuid, permission);
    }

    @WebMethod
    public String uploadDocument(@WebParam(name = "sessionId")
    String sid, String path, String type, String[] properties)
            throws ClientException {
        return getWSNuxeoRemoting().uploadDocument(sid, path, type, properties);
    }

    @WebMethod
    public String connect(@WebParam(name = "userName")
    String username, @WebParam(name = "password")
    String password) throws ClientException {
        return getWSNuxeoRemoting().connect(username, password);
    }

    @WebMethod
    public void disconnect(@WebParam(name = "sessionId")
    String sid) throws ClientException {
        getWSNuxeoRemoting().disconnect(sid);
    }

    @WebMethod
    public EventDescriptorPage queryEventsByPage(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "whereClause")
    String whereClause, @WebParam(name = "pageIndex")
    int page, @WebParam(name = "pageSize")
    int pageSize) throws AuditException {
        return getWSAudit().queryEventsByPage(sessionId, whereClause, page,
                pageSize);
    }

    @WebMethod
    public boolean validateUserPassword(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "username")
    String username, @WebParam(name = "password")
    String password) throws ClientException {
        WSRemotingSession rs = initSession(sessionId);
        return rs.getUserManager().checkUsernamePassword(username, password);
    }

    @WebMethod
    public String[] getUserGroups(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "username")
    String username) throws ClientException {
        WSRemotingSession rs = initSession(sessionId);
        List<String> groups = rs.getUserManager().getPrincipal(username).getAllGroups();
        String[] groupArray = new String[groups.size()];
        groups.toArray(groupArray);
        return groupArray;
    }

    @WebMethod
    public DocumentSnapshot getDocumentSnapshot(@WebParam(name = "sessionId")
    String sessionId, @WebParam(name = "uuid")
    String uuid) throws ClientException {
        return getWSNuxeoRemoting().getDocumentSnapshot(sessionId, uuid);
    }

}
