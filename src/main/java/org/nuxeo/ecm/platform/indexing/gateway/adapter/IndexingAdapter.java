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
 * $Id: IndexingAdapter.java 31331 2008-04-08 17:04:02Z ogrisel $
 */

package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
import org.nuxeo.ecm.platform.api.ws.WsACE;

public interface IndexingAdapter {

    DocumentDescriptor adaptDocumentDescriptor(CoreSession session, String uuid, DocumentDescriptor dd);

    WsACE[] adaptDocumentACL(CoreSession session, String uuid, WsACE[] aces);

    WsACE[] adaptDocumentLocalACL(CoreSession session, String uuid, WsACE[] aces);

    DocumentBlob[] adaptDocumentBlobs(CoreSession session, String uuid, DocumentBlob[] blobs);

    DocumentProperty[] adaptDocumentNoBlobProperties(CoreSession session, String uuid, DocumentProperty[] properties);

    DocumentProperty[] adaptDocumentProperties(CoreSession session, String uuid, DocumentProperty[] properties);

    boolean useDownloadUrlForBlob();

}
