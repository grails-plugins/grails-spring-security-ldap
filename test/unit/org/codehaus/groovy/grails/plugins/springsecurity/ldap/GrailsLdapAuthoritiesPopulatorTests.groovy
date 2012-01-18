package org.codehaus.groovy.grails.plugins.springsecurity.ldap

import grails.test.*

class GrailsLdapAuthoritiesPopulatorTests extends GrailsUnitTestCase {

	def populatorSettings
	def testAuthorities

    protected void setUp() {
        super.setUp()

		populatorSettings = [
				groupRoleAttribute: 'member',
				groupSearchFilter: 'fake={0}',
				searchSubtree: true,
				defaultRole: 'ROLE_USER',
				ignorePartialResultException: false;
				retrieveDatabaseRoles: false,
				roleStripPrefix: 'Test-Pre',
				roleStripSuffix: 'Test-Post'
				roleConvertDashes: true,
				roleToUpperCase: true ]
		def testRoles = [
			"ROLE_Test-Pre Sys-AdminTest-Pre-Test-Post-Group Test-Post",
			"ROLE_Test-Pre Sys-AdminTest Pre-Test Post-Group Test-Post"
			]

		testAuthorities = testRoles.collect{ new GrantedAuthorityImpl(it) } 
    }

    void testGetGroupMembershipRoles() {

		def popMocker = mockFor(GrailsLdapAuthoritiesPopulator)
		popMock.demand.super.getGroupMembershipRoles(0..1) = { dn, username -> testRoles }
		def grailsLdapAuthoritiesPopulator = new GrailsLdapAuthoritiesPopulator( populatorSettings)

		def authorities = grailsLdapAuthoritiesPopulator.getGroupMembershipRoles('cn=test,dn=example,dn=org', 'test')

    }
}
