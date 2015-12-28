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
 *     Nuxeo - initial API and implementation
 *
 * $Id: BaseIndexingAdapter.java 31331 2008-04-08 17:04:02Z ogrisel $
 */
package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
import org.nuxeo.ecm.platform.api.ws.WsACE;

/**
 * Base class to derive to help implement contributions to the IndexingAdapterService. All methods of this class return
 * the raw parameter without modifying it.
 * 
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 */
public class BaseIndexingAdapter implements IndexingAdapter {

    public DocumentDescriptor adaptDocumentDescriptor(CoreSession session, String uuid, DocumentDescriptor dd)
            {
        return dd;
    }

    public WsACE[] adaptDocumentLocalACL(CoreSession session, String uuid, WsACE[] aces) {
        return aces;
    }

    public WsACE[] adaptDocumentACL(CoreSession session, String uuid, WsACE[] aces) {
        return aces;
    }

    public DocumentBlob[] adaptDocumentBlobs(CoreSession session, String uuid, DocumentBlob[] blobs)
            {
        return blobs;
    }

    public DocumentProperty[] adaptDocumentNoBlobProperties(CoreSession session, String uuid,
            DocumentProperty[] properties) {
        return properties;
    }

    public DocumentProperty[] adaptDocumentProperties(CoreSession session, String uuid, DocumentProperty[] properties)
            {
        return properties;
    }

    public boolean useDownloadUrlForBlob() {
        throw new UnsupportedOperationException();
    }

}
