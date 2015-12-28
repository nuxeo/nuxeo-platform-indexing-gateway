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
 * $Id: IndexingAdapterDescriptor.java 31426 2008-04-09 17:00:34Z ogrisel $
 */

package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("adapter")
public class IndexingAdapterDescriptor implements Comparable<IndexingAdapterDescriptor> {

    @XNode("@class")
    private String className = "";

    @XNode("@order")
    private int order = 0;

    @XNode("@enabled")
    private boolean enabled = true;

    private IndexingAdapter adapterInstance;

    public String getClassName() {
        return className;
    }

    public IndexingAdapter getAdapterInstance() {
        return adapterInstance;
    }

    public void setAdapterInstance(IndexingAdapter adapterInstance) {
        this.adapterInstance = adapterInstance;
    }

    public int compareTo(IndexingAdapterDescriptor o) {
        return order - o.order;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // needed to make the following un-registration logics work:
    // registeredAdapters.remove(registeredAdapters.lastIndexOf(descriptor));
    @Override
    public boolean equals(Object o) {
        if (o instanceof IndexingAdapterDescriptor) {
            IndexingAdapterDescriptor otherDescriptor = (IndexingAdapterDescriptor) o;
            return className.equals(otherDescriptor.className);
        }
        return false;
    }

}
