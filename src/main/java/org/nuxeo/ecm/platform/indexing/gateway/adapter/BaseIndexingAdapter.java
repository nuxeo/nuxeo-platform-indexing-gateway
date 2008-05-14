/*
 * (C) Copyright 2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id: BaseIndexingAdapter.java 31331 2008-04-08 17:04:02Z ogrisel $
 */
package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;

/**
 * Base class to derive to help implement contributions to the
 * IndexingAdapterService.
 *
 * All methods of this class return the raw parameter without modifying it.
 *
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 *
 */
public class BaseIndexingAdapter implements IndexingAdapter {

    public DocumentDescriptor adaptDocumentDescriptor(CoreSession session,
            String uuid, DocumentDescriptor dd) throws ClientException {
        return dd;
    }

    public ACE[] adaptDocumentLocalACL(CoreSession session, String uuid,
            ACE[] aces) throws ClientException {
        return aces;
    }

    public ACE[] adaptDocumentACL(CoreSession session, String uuid, ACE[] aces)
            throws ClientException {
        return aces;
    }

    public DocumentBlob[] adaptDocumentBlobs(CoreSession session, String uuid,
            DocumentBlob[] blobs) throws ClientException {
        return blobs;
    }

    public DocumentProperty[] adaptDocumentNoBlobProperties(
            CoreSession session, String uuid, DocumentProperty[] properties)
            throws ClientException {
        return properties;
    }

    public DocumentProperty[] adaptDocumentProperties(CoreSession session,
            String uuid, DocumentProperty[] properties) throws ClientException {
        return properties;
    }

}
