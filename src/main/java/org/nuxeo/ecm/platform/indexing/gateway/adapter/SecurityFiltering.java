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
