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
