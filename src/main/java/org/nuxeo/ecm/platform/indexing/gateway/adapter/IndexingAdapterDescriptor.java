/*
 * (C) Copyright 2008 Nuxeo SA (http://nuxeo.com/) and others.
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
