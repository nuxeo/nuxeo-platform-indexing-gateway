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
 * $Id: $
 */
package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.api.ws.DocumentProperty;

/**
 * Adapter to build a new property ecm:pathTitle holding the physical path in the repository with human readable titles
 * instead of technical local path ids found in the default ecm:path property.
 *
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 */
public class RepositoryPathTitleAdapter extends BaseIndexingAdapter {

    public static final String PATH_TITLE_PROPERTY = "ecm:pathTitle";

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(RepositoryPathTitleAdapter.class);

    public static final String PATH_SEPARATOR = "/";

    @Override
    public DocumentProperty[] adaptDocumentNoBlobProperties(CoreSession session, String uuid,
            DocumentProperty[] properties) {
        return addPathTitleProperty(session, uuid, properties);
    }

    @Override
    public DocumentProperty[] adaptDocumentProperties(CoreSession session, String uuid, DocumentProperty[] properties)
            {
        return addPathTitleProperty(session, uuid, properties);
    }

    protected DocumentProperty[] addPathTitleProperty(CoreSession session, String uuid, DocumentProperty[] properties)
            {

        IdRef docRef = new IdRef(uuid);
        List<DocumentModel> parentDocuments = session.getParentDocuments(docRef);
        // remove the current document from the list of ancestors
        parentDocuments = parentDocuments.subList(0, parentDocuments.size() - 1);

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
        enhancedProperties.add(new DocumentProperty(PATH_TITLE_PROPERTY, pathTitle));

        return enhancedProperties.toArray(new DocumentProperty[enhancedProperties.size()]);
    }
}
