/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Georges Racinet
 */
package org.nuxeo.ecm.platform.indexing.gateway.adapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nuxeo.ecm.core.api.security.PermissionProvider;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * Shared info about security filtering (copied from removed module)
 *
 * @author <a href="mailto:gracinet@nuxeo.com">Georges Racinet</a>
 */
public class SecurityFiltering {

    public static final String[] BROWSE_PERMISSION_SEEDS = { SecurityConstants.BROWSE };

    /**
     * Return the recursive closure of all permissions that comprises the requested seed permissions. TODO: this logics
     * should be moved upward to the PermissionProvider interface.
     *
     * @param seedPermissions
     * @return the list of permissions, seeds inclusive
     */
    public static List<String> getPermissionList(String[] seedPermissions) {
        PermissionProvider pprovider = Framework.getService(PermissionProvider.class);
        List<String> aggregatedPerms = new LinkedList<String>();
        for (String seedPerm : seedPermissions) {
            aggregatedPerms.add(seedPerm);
            String[] compoundPerms = pprovider.getPermissionGroups(seedPerm);
            if (compoundPerms != null) {
                aggregatedPerms.addAll(Arrays.asList(compoundPerms));
            }
        }
        // EVERYTHING is special and may not be explicitly registered as a
        // compound
        if (!aggregatedPerms.contains(SecurityConstants.EVERYTHING)) {
            aggregatedPerms.add(SecurityConstants.EVERYTHING);
        }
        return aggregatedPerms;
    }

    /**
     * This is the list of all permissions that grant access to some indexed document.
     *
     * @return the list of all permissions that include Browse directly or un-directly
     */
    public static List<String> getBrowsePermissionList() {
        return getPermissionList(BROWSE_PERMISSION_SEEDS);
    }

    // public static final String SEPARATOR = "#";

    // public static final String ESCAPE = "[#]";

    // Constant utility class.
    // private SecurityFiltering() {
    // }

}
