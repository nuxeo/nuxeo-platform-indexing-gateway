/*
 * (C) Copyright 2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id: SimpleACLIndexingAdapter.java 31426 2008-04-09 17:00:34Z ogrisel $
 */

package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.search.api.backend.security.SecurityFiltering;

/**
 * Simple IndexingAdapter that filters blocked local ACEs with the default
 * blocking strategy in Nuxeo: "Deny Everything to Everyone" and only provide
 * intuition with permissions that are related to read access.
 *
 * @author Olivier Grisel <ogrisel@nuxeo.com>
 *
 */
public class SimpleACLIndexingAdapter extends BaseIndexingAdapter {

    protected final static ACE BLOCKING_ACE = new ACE(
            SecurityConstants.EVERYONE, SecurityConstants.EVERYTHING, false);

    protected List<String> CACHED_PERMISSIONS_TO_INDEX;

    protected List<String> getPermissionsToIndex() throws ClientException {
        if (CACHED_PERMISSIONS_TO_INDEX == null) {
            try {
                CACHED_PERMISSIONS_TO_INDEX = SecurityFiltering.getBrowsePermissionList();
            } catch (Exception e) {
                throw new ClientException(e);
            }
        }
        return CACHED_PERMISSIONS_TO_INDEX;
    }

    @Override
    public ACE[] adaptDocumentLocalACL(CoreSession session, String uuid,
            ACE[] aces) throws ClientException {
        return adaptDocumentACL(session, uuid, aces);
    }

    @Override
    public ACE[] adaptDocumentACL(CoreSession session, String uuid, ACE[] aces)
            throws ClientException {
        List<ACE> aceList = Arrays.asList(aces);
        List<ACE> filteredAceList = new LinkedList<ACE>();

        int index = aceList.indexOf(BLOCKING_ACE);
        if (index != -1) {
            aceList = aceList.subList(0, index);
        }
        for (ACE ace : aceList) {
            if (getPermissionsToIndex().contains(ace.getPermission())) {
                filteredAceList.add(ace);
            }
        }
        return filteredAceList.toArray(new ACE[filteredAceList.size()]);
    }
}
