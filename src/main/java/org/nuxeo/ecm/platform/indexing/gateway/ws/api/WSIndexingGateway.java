package org.nuxeo.ecm.platform.indexing.gateway.ws.api;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.NuxeoRemoting;
import org.nuxeo.ecm.platform.audit.ws.api.WSAudit;
import org.nuxeo.ecm.platform.indexing.gateway.ws.DocumentTypeDescriptor;
import org.nuxeo.ecm.platform.indexing.gateway.ws.UUIDPage;

public interface WSIndexingGateway extends NuxeoRemoting, WSAudit {

    String resolvePathToUUID(String sessionId, String path);

    DocumentDescriptor getDocumentFromPath(String sessionId, String path);

    boolean validateUserPassword(String sessionId, String username, String password);

    String[] getUserGroups(String sessionId, String username);

    String[] getRecursiveChildrenUUIDs(String sid, String uuid);

    UUIDPage getRecursiveChildrenUUIDsByPage(String sid, String uuid, int page, int pageSize);

    DocumentTypeDescriptor[] getTypeDefinitions();

}
