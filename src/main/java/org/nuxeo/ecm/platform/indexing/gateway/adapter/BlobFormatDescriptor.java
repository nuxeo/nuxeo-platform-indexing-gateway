package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Simple descripor to configure if blob must be returned as byte[] or as download URLs
 *
 * @author tiry
 */
@XObject("blobFormat")
public class BlobFormatDescriptor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @XNode("@useDownloadUrl")
    private boolean useDownloadUrl = true;

    public boolean isUseDownloadUrl() {
        return useDownloadUrl;
    }

    public void setUseDownloadUrl(boolean useDownloadUrl) {
        this.useDownloadUrl = useDownloadUrl;
    }

}
