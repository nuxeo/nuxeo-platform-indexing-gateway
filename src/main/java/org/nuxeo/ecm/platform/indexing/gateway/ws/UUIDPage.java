package org.nuxeo.ecm.platform.indexing.gateway.ws;

import java.io.Serializable;

public class UUIDPage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String[] uuids;

    private int pageIndex;

    private boolean hasMorePage;

    public UUIDPage(String[] uuids, int pageIndex,boolean hasMorePage) {
        this.uuids=uuids;
        this.pageIndex=pageIndex;
        this.hasMorePage=hasMorePage;
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
