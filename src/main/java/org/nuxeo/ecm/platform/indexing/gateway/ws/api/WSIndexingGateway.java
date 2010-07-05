package org.nuxeo.ecm.platform.indexing.gateway.ws.api;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.NuxeoRemoting;
import org.nuxeo.ecm.platform.audit.ws.api.WSAudit;
import org.nuxeo.ecm.platform.indexing.gateway.ws.DocumentTypeDescriptor;
import org.nuxeo.ecm.platform.indexing.gateway.ws.UUIDPage;

public interface WSIndexingGateway extends NuxeoRemoting, WSAudit {

    String resolvePathToUUID(String sessionId, String path) throws ClientException;

    DocumentDescriptor getDocumentFromPath(String sessionId, String path) throws ClientException;

    boolean validateUserPassword(String sessionId, String username, String password) throws ClientException;

    String[] getUserGroups(String sessionId, String username) throws ClientException;

    String[] getRecursiveChildrenUUIDs(String sid, String uuid ) throws ClientException;

    UUIDPage getRecursiveChildrenUUIDsByPage(String sid,String uuid ,int page, int pageSize) throws ClientException;

    DocumentTypeDescriptor[] getTypeDefinitions() throws ClientException;

}
