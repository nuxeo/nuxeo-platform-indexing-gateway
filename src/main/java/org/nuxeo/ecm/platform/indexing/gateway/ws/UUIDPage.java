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

public class UUIDPage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String[] uuids;

    private int pageIndex;

    private boolean hasMorePage;

    public UUIDPage(String[] uuids, int pageIndex, boolean hasMorePage) {
        this.uuids = uuids;
        this.pageIndex = pageIndex;
        this.hasMorePage = hasMorePage;
    }

    public UUIDPage() {
    }

    public String[] getUuids() {
        return uuids;
    }

    public void setUuids(String[] uuids) {
        this.uuids = uuids;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public boolean isHasMorePage() {
        return hasMorePage;
    }

    public void setHasMorePage(boolean hasMorePage) {
        this.hasMorePage = hasMorePage;
    }

}
