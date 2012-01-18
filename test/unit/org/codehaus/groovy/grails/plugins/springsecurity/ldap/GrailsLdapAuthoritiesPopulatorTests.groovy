package org.codehaus.groovy.grails.plugins.springsecurity.ldap

import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.security.core.authority.GrantedAuthorityImpl
import grails.test.*

class GrailsLdapAuthoritiesPopulatorTests extends GrailsUnitTestCase {

	def contextSource

    protected void setUp() {
        super.setUp()

		contextSource = new LdapContextSource()
    }

	/**
	 * This one test should cover everything added in the cleanRole() function
	 */
    void testGetGroupMembershipRoles() {

		def testRole = new GrantedAuthorityImpl("ROLE_Test-Pre Sys-AdminTest-Pre-Test-Post-Group Test-Post")

		def grailsLdapAuthoritiesPopulator = new GrailsLdapAuthoritiesPopulator(contextSource, '')

		grailsLdapAuthoritiesPopulator.setGroupRoleAttribute('member')
		grailsLdapAuthoritiesPopulator.setGroupSearchFilter('fake={0}')
		grailsLdapAuthoritiesPopulator.setSearchSubtree(true)
		grailsLdapAuthoritiesPopulator.setDefaultRole('ROLE_USER')
		grailsLdapAuthoritiesPopulator.setIgnorePartialResultException(false)
		grailsLdapAuthoritiesPopulator.setRetrieveDatabaseRoles(false)
		grailsLdapAuthoritiesPopulator.setRoleStripPrefix('Test-Pre')
		grailsLdapAuthoritiesPopulator.setRoleStripSuffix('Test-Post')
		grailsLdapAuthoritiesPopulator.setRoleConvertDashes(true)
		grailsLdapAuthoritiesPopulator.setRoleToUpperCase(true)

		// the settings should run through all the permutations in one swipe
		def newRole = grailsLdapAuthoritiesPopulator.cleanRole(testRole)

		// make sure our test did what we expected it to
		assert "ROLE_SYS_ADMINTEST_PRE_TEST_POST_GROUP" == newRole.getAuthority()

    }
}
