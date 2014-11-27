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
 * $Id: IndexingAdapterService.java 31426 2008-04-09 17:00:34Z ogrisel $
 */

package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
import org.nuxeo.ecm.platform.api.ws.WsACE;
import org.nuxeo.runtime.RuntimeServiceException;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Service to allow client code to register converters for the datastructures
 * served to the Sinequa Intuition to be indexed.
 *
 * This is especially useful to be able to index Access Control Policy after
 * some post-processing since the ACL model of Intuition is not as expressive as
 * the Nuxeo Core security model.
 *
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 */
public class IndexingAdapterService extends DefaultComponent implements
        IndexingAdapter {

    public static final String INTUITION_ADAPTER_XP = "adapters";

    public static final String BLOB_FORMAT_XP = "blobFormat";

    protected final List<IndexingAdapterDescriptor> registeredAdapters = new LinkedList<IndexingAdapterDescriptor>();

    protected final List<IndexingAdapter> mergedAdapters = new LinkedList<IndexingAdapter>();

    protected boolean useDownloadUrl = true;

    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        if (INTUITION_ADAPTER_XP.equals(extensionPoint)) {
            mergedAdapters.clear(); // invalidate merged contributions
            IndexingAdapterDescriptor descriptor = (IndexingAdapterDescriptor) contribution;
            if (descriptor.isEnabled()) {
                // do not try to instantiate classes to be disabled by the
                // contribution
                IndexingAdapter adapterInstance;
                try {
                    adapterInstance = (IndexingAdapter) contributor.getContext().loadClass(
                            descriptor.getClassName()).newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
                descriptor.setAdapterInstance(adapterInstance);
            }
            registeredAdapters.add(descriptor);
        } else if (BLOB_FORMAT_XP.equals(extensionPoint)) {
            BlobFormatDescriptor desc = (BlobFormatDescriptor) contribution;
            useDownloadUrl = desc.isUseDownloadUrl();
        } else {
            throw new RuntimeServiceException("unsupported extension point: "
                    + extensionPoint);
        }
    }

    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        if (INTUITION_ADAPTER_XP.equals(extensionPoint)) {
            mergedAdapters.clear(); // invalidate merged contributions
            IndexingAdapterDescriptor descriptor = (IndexingAdapterDescriptor) contribution;
            registeredAdapters.remove(registeredAdapters.lastIndexOf(descriptor));

        } else {
            throw new RuntimeServiceException("unsupported extension point: "
                    + extensionPoint);
        }
    }

    public DocumentDescriptor adaptDocumentDescriptor(CoreSession session,
            String uuid, DocumentDescriptor dd) throws ClientException {
        for (IndexingAdapter adapter : getMergedAdapters()) {
            dd = adapter.adaptDocumentDescriptor(session, uuid, dd);
        }
        return dd;
    }

    public  WsACE[] adaptDocumentACL(CoreSession session, String uuid,  WsACE[] aces)
            throws ClientException {
        for (IndexingAdapter adapter : getMergedAdapters()) {
            aces = adapter.adaptDocumentACL(session, uuid, aces);
        }
        return aces;
    }

    public  WsACE[] adaptDocumentLocalACL(CoreSession session, String uuid,
             WsACE[] aces) throws ClientException {
        for (IndexingAdapter adapter : getMergedAdapters()) {
            aces = adapter.adaptDocumentLocalACL(session, uuid, aces);
        }
        return aces;
    }

    public DocumentBlob[] adaptDocumentBlobs(CoreSession session, String uuid,
            DocumentBlob[] blobs) throws ClientException {
        for (IndexingAdapter adapter : getMergedAdapters()) {
            blobs = adapter.adaptDocumentBlobs(session, uuid, blobs);
        }
        return blobs;
    }

    public DocumentProperty[] adaptDocumentNoBlobProperties(
            CoreSession session, String uuid, DocumentProperty[] properties)
            throws ClientException {
        for (IndexingAdapter adapter : getMergedAdapters()) {
            properties = adapter.adaptDocumentNoBlobProperties(session, uuid,
                    properties);
        }
        return properties;
    }

    public DocumentProperty[] adaptDocumentProperties(CoreSession session,
            String uuid, DocumentProperty[] properties) throws ClientException {
        for (IndexingAdapter adapter : getMergedAdapters()) {
            properties = adapter.adaptDocumentProperties(session, uuid,
                    properties);
        }
        return properties;
    }

    protected List<IndexingAdapter> getMergedAdapters() throws ClientException {
        if (mergedAdapters.isEmpty()) {
            synchronized (this) {
                Map<String, IndexingAdapterDescriptor> descriptorByClass = new HashMap<String, IndexingAdapterDescriptor>();
                // merge registered contribution by class names
                for (IndexingAdapterDescriptor descriptor : registeredAdapters) {
                    descriptorByClass.put(descriptor.getClassName(), descriptor);
                }

                // sort merge contributions by order
                List<IndexingAdapterDescriptor> mergedDescriptors = new ArrayList<IndexingAdapterDescriptor>(
                        descriptorByClass.values());
                Collections.sort(mergedDescriptors);

                // filter out disabled adapters and collect the instances of the
                // remaining sorted contributions
                for (IndexingAdapterDescriptor descriptor : mergedDescriptors) {
                    if (descriptor.isEnabled()) {
                        mergedAdapters.add(descriptor.getAdapterInstance());
                    }
                }
            }
        }
        return mergedAdapters;
    }

    public boolean useDownloadUrlForBlob() {
        return useDownloadUrl;
    }
}
