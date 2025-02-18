/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.services.resources.admin.permissions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.keycloak.authorization.AdminPermissionsSchema;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.models.AdminRoles;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;

class GroupPermissionsV2 extends GroupPermissions {

    private final KeycloakSession session;

    GroupPermissionsV2(KeycloakSession session, AuthorizationProvider authz, MgmtPermissions root) {
        super(authz, root);
        this.session = session;
    }

    @Override
    public boolean canView() {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS, AdminRoles.VIEW_USERS)) {
            return true;
        }

        return hasPermission(null, AdminPermissionsSchema.VIEW, AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canView(GroupModel group) {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS, AdminRoles.VIEW_USERS)) {
            return true;
        }

        return hasPermission(group.getId(), AdminPermissionsSchema.VIEW, AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canManage() {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS)) {
            return true;
        }

        return hasPermission(null, AdminPermissionsSchema.VIEW, AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canManage(GroupModel group) {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS)) {
            return true;
        }

        return hasPermission(group.getId(), AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canViewMembers(GroupModel group) {
        if (root.users().canView()) return true;

        return hasPermission(group.getId(), AdminPermissionsSchema.VIEW_MEMBERS, AdminPermissionsSchema.MANAGE_MEMBERS);
    }

    @Override
    public boolean canManageMembers(GroupModel group) {
        if (root.users().canManage()) return true;

        return hasPermission(group.getId(), AdminPermissionsSchema.MANAGE_MEMBERS);
    }

    @Override
    public boolean canManageMembership(GroupModel group) {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS)) {
            return true;
        }

        return hasPermission(group.getId(), AdminPermissionsSchema.MANAGE, AdminPermissionsSchema.MANAGE_MEMBERSHIP);
    }

    @Override
    public Set<String> getGroupIdsWithViewPermission() {
        if (root.users().canView()) return Collections.emptySet();

        if (!root.isAdminSameRealm()) {
            return Collections.emptySet();
        }

        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return Collections.emptySet();
        }

        Set<String> granted = new HashSet<>();

        resourceStore.findByType(server, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, groupResource -> {
            if (hasPermission(groupResource.getId(), AdminPermissionsSchema.VIEW_MEMBERS, AdminPermissionsSchema.MANAGE_MEMBERS)) {
                granted.add(groupResource.getId());
            }
        });

        return granted;
    }

    private boolean hasPermission(String groupId, String... scopes) {
        if (!root.isAdminSameRealm()) {
            return false;
        }

        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return false;
        }

        Resource resource = groupId == null ? null : resourceStore.findByName(server, groupId);

        if (resource == null) {
            resource = AdminPermissionsSchema.SCHEMA.getResourceTypeResource(session, server, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE);

            // check if there is a permission for "all-groups". If so, proceed with the evaluation to check scopes
            if (policyStore.findByResource(server, resource).isEmpty()) {
                return false;
            }
        }

        Collection<Permission> permissions = root.evaluatePermission(new ResourcePermission(resource, resource.getScopes(), server), server);

        List<String> expectedScopes = Arrays.asList(scopes);

        for (Permission permission : permissions) {
            for (String scope : permission.getScopes()) {
                if (expectedScopes.contains(scope)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isPermissionsEnabled(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public void setPermissionsEnabled(GroupModel group, boolean enable) {
       throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy viewMembersPermission(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy manageMembersPermission(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy manageMembershipPermission(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy viewPermission(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy managePermission(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Resource resource(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Map<String, String> getPermissions(GroupModel group) {
        throw new UnsupportedOperationException("Not supported in V2");
    }
}
