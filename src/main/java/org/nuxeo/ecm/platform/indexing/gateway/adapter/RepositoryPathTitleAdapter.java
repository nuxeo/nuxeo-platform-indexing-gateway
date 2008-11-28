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
 * $Id: $
 */
package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;

/**
 * Adapter to build a new property ecm:pathTitle holding the physical path in
 * the repository with human readable titles instead of technical local path ids
 * found in the default ecm:path property.
 *
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 */
public class RepositoryPathTitleAdapter extends BaseIndexingAdapter {

    public static final String PATH_TITLE_PROPERTY = "ecm:pathTitle";

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(RepositoryPathTitleAdapter.class);

    public static final String PATH_SEPARATOR = "/";

    @Override
    public DocumentProperty[] adaptDocumentNoBlobProperties(
            CoreSession session, String uuid, DocumentProperty[] properties)
            throws ClientException {
        return addPathTitleProperty(session, uuid, properties);
    }

    @Override
    public DocumentProperty[] adaptDocumentProperties(CoreSession session,
            String uuid, DocumentProperty[] properties) throws ClientException {
        return addPathTitleProperty(session, uuid, properties);
    }

    protected DocumentProperty[] addPathTitleProperty(CoreSession session,
            String uuid, DocumentProperty[] properties) throws ClientException {

        IdRef docRef = new IdRef(uuid);
        List<DocumentModel> parentDocuments;
        try {
            parentDocuments = session.getParentDocuments(docRef);
        } catch (ClientException e) {
            log.warn("could not get path title property for missing document with ref "
                    + uuid);
            return properties;
        }
        // build a list with all the existing properties for document with ref
        // uuid
        List<DocumentProperty> enhancedProperties = new ArrayList<DocumentProperty>();
        enhancedProperties.addAll(Arrays.asList(properties));

        // fetch the list of ancestor titles and build the new property
        List<String> titles = new ArrayList<String>(parentDocuments.size());
        for (DocumentModel ancestor : parentDocuments) {
            titles.add(ancestor.getTitle());
        }
        String pathTitle = StringUtils.join(titles, PATH_SEPARATOR);
        enhancedProperties.add(new DocumentProperty(PATH_TITLE_PROPERTY,
                pathTitle));

        return enhancedProperties.toArray(new DocumentProperty[enhancedProperties.size()]);
    }
}
