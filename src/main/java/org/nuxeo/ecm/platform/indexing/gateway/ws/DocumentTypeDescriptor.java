/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.ecm.platform.indexing.gateway.ws;

import java.io.Serializable;

import org.nuxeo.ecm.core.schema.DocumentType;

/**
 * JAXB object for {@link DocumentType} export via WS
 *
 * @author tiry
 */
public class DocumentTypeDescriptor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected boolean isFile = false;

    protected boolean isFolder = false;

    protected String name;

    protected String[] facets;

    protected String[] schemas;

    public DocumentTypeDescriptor() {

    }

    public DocumentTypeDescriptor(DocumentType docType) {
        isFile = docType.isFile();
        isFolder = docType.isFolder();
        name = docType.getName();
        facets = docType.getFacets().toArray(new String[docType.getFacets().size()]);
        schemas = docType.getSchemaNames();
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getFacets() {
        return facets;
    }

    public void setFacets(String[] facets) {
        this.facets = facets;
    }

    public String[] getSchemas() {
        return schemas;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }
}
