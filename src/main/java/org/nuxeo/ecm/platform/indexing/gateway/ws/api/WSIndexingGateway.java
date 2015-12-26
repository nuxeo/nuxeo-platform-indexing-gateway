/*
 * (C) Copyright 2008 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thierry Delprat
 */
package org.nuxeo.ecm.platform.indexing.gateway.ws.api;

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
