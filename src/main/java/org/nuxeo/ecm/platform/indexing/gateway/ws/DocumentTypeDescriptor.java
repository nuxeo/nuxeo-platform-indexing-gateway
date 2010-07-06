package org.nuxeo.ecm.platform.indexing.gateway.ws;

import java.io.Serializable;

import org.nuxeo.ecm.core.schema.DocumentType;

/**
 *
 * JAXB object for {@link DocumentType} export via WS
 *
 * @author tiry
 *
 */
public class DocumentTypeDescriptor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected boolean isFile=false;
    protected boolean isFolder=false;
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
