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
 * $Id: SimpleACLIndexingAdapter.java 31426 2008-04-09 17:00:34Z ogrisel $
 */

package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.api.ws.WsACE;

/**
 * Simple IndexingAdapter that filters blocked local ACEs with the default blocking strategy in Nuxeo:
 * "Deny Everything to Everyone" and only provide intuition with permissions that are related to read access.
 *
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 */
public class SimpleACLIndexingAdapter extends BaseIndexingAdapter {

    protected final static WsACE BLOCKING_ACE = new WsACE(SecurityConstants.EVERYONE, SecurityConstants.EVERYTHING, false);

    protected List<String> CACHED_PERMISSIONS_TO_INDEX;

    protected List<String> getPermissionsToIndex() {
        if (CACHED_PERMISSIONS_TO_INDEX == null) {
            CACHED_PERMISSIONS_TO_INDEX = SecurityFiltering.getBrowsePermissionList();
        }
        return CACHED_PERMISSIONS_TO_INDEX;
    }

    @Override
    public WsACE[] adaptDocumentLocalACL(CoreSession session, String uuid, WsACE[] aces) {
        return adaptDocumentACL(session, uuid, aces);
    }

    @Override
    public WsACE[] adaptDocumentACL(CoreSession session, String uuid, WsACE[] aces) {
        List<WsACE> aceList = Arrays.asList(aces);
        List<WsACE> filteredAceList = new LinkedList<WsACE>();

        int index = aceList.indexOf(BLOCKING_ACE);
        if (index != -1) {
            aceList = aceList.subList(0, index);
        }
        for (WsACE ace : aceList) {
            if (getPermissionsToIndex().contains(ace.getPermission())) {
                filteredAceList.add(ace);
            }
        }
        return filteredAceList.toArray(new WsACE[filteredAceList.size()]);
    }
}
