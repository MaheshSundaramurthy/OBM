/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2013 Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Set;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.AddressBookDaoJdbcImpl;
import org.obm.domain.dao.ObmInfoDao;
import org.obm.domain.dao.ObmInfoDaoJdbcImpl;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserPatternDao;
import org.obm.domain.dao.UserPatternDaoJdbcImpl;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.dao.exceptions.GroupExistsException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupRecursionException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(GroupDaoJdbcImplTest.Env.class)
public class GroupDaoJdbcImplTest {

    public static class Env extends AbstractModule {

        @Override
        protected void configure() {
            bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

            bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
            bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
            bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
            bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
            bind(AddressBookDao.class).to(AddressBookDaoJdbcImpl.class);
            bind(UserPatternDao.class).to(UserPatternDaoJdbcImpl.class);
        }

    }

    private ObmDomain domain1;
    private ObmUser user1, nonexistentUser;
    private Group group4, group6, group7, nonexistentGroup;

    @Before
    public void init() {
        domain1 = ToolBox.getDefaultObmDomain();
        user1 = userDao.findUserById(1, domain1);

        group4 = generateGroup("existing-nousers-subgroups-child1");
        group6 = generateGroup("existing-users-subgroups-child1");
        group7 = generateGroup("existing-users-subgroups-child2");

        nonexistentUser = generateUser(999);
        nonexistentGroup = generateGroup("nonexistent");
    }

    private ObmUser generateUser(int uid) {
        String stringUid = String.valueOf(uid);
        return ObmUser.builder()
                      .uid(uid)
                      .login("user" + stringUid)
                      .commonName("")
                      .lastName("")
                      .firstName("")
                      .entityId(uid)
                      .extId(UserExtId.valueOf("user" + stringUid))
                      .emailAndAliases("user" + stringUid + "@test.tlse.lng")
                      .publicFreeBusy(true)
                      .domain(domain1)
                      .build();
    }

    private Group generateGroup(String prefix) {
        return Group.builder()
                    .extId(GroupExtId.valueOf(prefix))
                    .name(prefix + "-name")
                    .description(prefix + "-description")
                    .build();
    }

    @Inject
    private GroupDao dao;

    @Inject
    private UserDao userDao;

    @Rule
    @Inject
    public H2InMemoryDatabase db;

    @Test(expected = GroupNotFoundException.class)
    public void testGetNonexistantGroup() throws Exception {
        dao.get(domain1, GroupExtId.valueOf("1234"));
    }

    @Test
    public void testExistingGroup() throws Exception {
        Group group = dao.get(domain1, GroupExtId.valueOf("existing-nousers-nosubgroups"));
        testGroupBase("existing-nousers-nosubgroups", group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();
    }

    @Test(expected = GroupNotFoundException.class)
    public void testGetNonexistantRecursiveGroup() throws Exception {
        dao.getRecursive(domain1, GroupExtId.valueOf("nonexistent"), true, -1);
    }

    @Test
    public void testExistingRecursiveNoUsersNoSubgroups() throws Exception {

        Group group = dao.getRecursive(domain1, GroupExtId.valueOf("existing-nousers-nosubgroups"), true, -1);
        testGroupBase("existing-nousers-nosubgroups", group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();
    }

    @Test
    public void testExtistingRecursiveUsersNoSubgroups() throws Exception {
        String prefix = "existing-users-nosubgroups";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, true, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, false, 2);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();
    }

    @Test
    public void testExistingRecursiveNoUsersSubgroups() throws Exception {
        String prefix = "existing-nousers-subgroups";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).containsOnly(group4);

        group = dao.getRecursive(domain1, groupExtId, false, 1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).containsOnly(group4);

        group = dao.getRecursive(domain1, groupExtId, false, 2);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).containsOnly(group4);
    }

    @Test
    public void testExtistingRecursiveUsersSubgroups() throws Exception {
        String prefix = "existing-users-subgroups";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, 0);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, true, 0);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, true, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(group6, group7);

        group = dao.getRecursive(domain1, groupExtId, true, 1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(group6);

        group = dao.getRecursive(domain1, groupExtId, true, 2);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(group6, group7);

    }

    @Test
    public void testCyclicDirectChildren() throws Exception {
        /* The group structure here is:

           recursive-direct-parent
            |-recursive-direct-child1
               |-recursive-direct-parent    <-- Expansion should stop here
                  |-recursive-direct-child1
                     |-...
        */
        String prefix = "recursive-direct-parent";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertExpandedToDepth(group, 2);
    }

    @Test
    public void testCyclicMultipleChildren() throws Exception {
        /* The group structure here is:

            recursive-multichild-parent
             |-recursive-multichild-child1
             |  |-recursive-multichild-childcommon            <-- This must be expanded
             |     |-recursive-multichild-childcommonexpand
             |-recursive-multichild-child2
                |-recursive-multichild-childcommon            <-- This must be expanded
                   |-recursive-multichild-childcommonexpand

         */
        String prefix = "recursive-multichild-parent";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertExpandedToDepth(group, 3);
    }

    @Test
    public void testCreateGroup() throws Exception {
        String prefix = "created-group";
        Group group = generateGroup(prefix);
        Group createdGroup = dao.create(domain1, group);
        testGroupBase(prefix, createdGroup);

        createdGroup = dao.get(domain1, group.getExtId());
        testGroupBase(prefix, createdGroup);
    }

    @Test(expected = GroupExistsException.class)
    public void testDuplicateCreate() throws Exception {
        String prefix = "created-group-duplicate";
        Group group = generateGroup(prefix);

        // This should work
        Group createdGroup = dao.create(domain1, group);
        testGroupBase(prefix, createdGroup);

        // This should fail
        createdGroup = dao.create(domain1, group);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testUpdateNonExistent() throws Exception {
        dao.update(domain1, nonexistentGroup);
    }

    @Test
    public void testUpdateGroup() throws Exception {
         String prefix = "modified-group";
         GroupExtId extId = GroupExtId.valueOf(prefix);

         Group modifiedGroup = Group.builder()
                                     .extId(extId)
                                     .name("modified-group-changed")
                                     .description("modified-group-description-changed")
                                     .build();

         Group modifiedReturnedGroup = dao.update(domain1, modifiedGroup);

         assertThat(modifiedGroup)
             .isLenientEqualsToByAcceptingFields(modifiedReturnedGroup,
                                                 "name", "description", "extId");

         Group retrievedGroup = dao.get(domain1, extId);
         assertThat(modifiedGroup)
             .isLenientEqualsToByAcceptingFields(retrievedGroup,
                                                 "name", "description", "extId");
    }

    @Test(expected = GroupNotFoundException.class)
    public void testDeleteNonexistent() throws Exception {
        dao.delete(domain1, nonexistentGroup.getExtId());
    }

    @Test
    public void testDelete() throws Exception {
         String prefix = "delete-group";
         GroupExtId extId = GroupExtId.valueOf(prefix);
         dao.delete(domain1, extId);

         try {
             dao.get(domain1, extId);
             throw new AssertionFailedError("Group exists after deletion");
         } catch (GroupNotFoundException e) {
             // This is expected, swallow the exception
         }
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddUserNonexistent() throws Exception {
        dao.addUser(domain1, nonexistentGroup.getExtId(), user1);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddSubgroupNonexistentParent() throws Exception {
        GroupExtId subgroupId = GroupExtId.valueOf("addusersubgroup-group-child");
        dao.addSubgroup(domain1, nonexistentGroup.getExtId(), subgroupId);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddSubgroupNonexistentChild() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("addusersubgroup-group-parent");
        dao.addSubgroup(domain1, parentId, nonexistentGroup.getExtId());
    }

    @Test(expected = GroupRecursionException.class)
    public void testAddSubgroupRecursion() throws Exception {
        Group parent = generateGroup("addusersubgroup-group-parent");
        dao.addSubgroup(domain1, parent.getExtId(), parent.getExtId());
    }

    @Test
    public void testAddUserSubgroup() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("addusersubgroup-group-parent");
        Group childGroup = generateGroup("addusersubgroup-group-child");
        dao.addUser(domain1, parentId, user1);
        dao.addSubgroup(domain1, parentId, childGroup.getExtId());

        Group group = dao.getRecursive(domain1, parentId, true, 1);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(childGroup);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveUserNonexistentGroup() throws Exception {
        dao.removeUser(domain1, nonexistentGroup.getExtId(), user1);
    }
    @Test(expected = UserNotFoundException.class)
    public void testRemoveUserNonexistentUser() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("removeusersubgroup-group-parent");
        dao.removeUser(domain1, parentId, nonexistentUser);
    }

    @Test
    public void testRemoveUserSubgroups() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("removeusersubgroup-group-parent");
        GroupExtId subgroup = GroupExtId.valueOf("removeusersubgroup-group-child");
        dao.removeUser(domain1, parentId, user1);
        dao.removeSubgroup(domain1, parentId, subgroup);

        Group parent = dao.getRecursive(domain1, parentId, true, 1);
        assertThat(parent.getSubgroups()).isEmpty();
        assertThat(parent.getUsers()).isEmpty();
    }

    /**
     * Helper function to make sure the base properties of the groups are correct
     *
     * @param prefix        The group prefix, which is the extId and the prefix for the base fields
     * @param group         The group to check.
     */
    private void testGroupBase(String prefix, Group group) {
        assertThat(group.getName()).isEqualTo(prefix + "-name");
        assertThat(group.getDescription()).isEqualTo(prefix + "-description");
        assertThat(group.getExtId().getId()).isEqualTo(prefix);
    }

    /**
     * Make sure the given group is expanded to a certain depth. If the depth
     * is greater than 1, each subgroup must have the same depth.
     *
     * @param group     The group to check
     * @param depth     0  -> Group should have no subgroups
     *                  1  -> Just the group itself should have subgroups
     *                  >1 -> Subgroups will recursively be checked for the depth
     */
    private void assertExpandedToDepth(Group group, int depth) {
        Set<Group> subgroups = group.getSubgroups();
        if (depth > 0) {
            assertThat(subgroups.size()).isGreaterThan(0);
            for (Group sub : group.getSubgroups()) {
                assertExpandedToDepth(sub, depth - 1);
            }
        } else {
            assertThat(subgroups.size()).isEqualTo(0);
        }
    }
}
