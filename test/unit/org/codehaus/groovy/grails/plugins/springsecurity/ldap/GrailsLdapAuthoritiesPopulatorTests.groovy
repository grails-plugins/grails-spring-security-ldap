package org.codehaus.groovy.grails.plugins.springsecurity.ldap

import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.security.core.authority.GrantedAuthorityImpl
import grails.test.*

class GrailsLdapAuthoritiesPopulatorTests extends GrailsUnitTestCase {

	def contextSource
	def rolesToTest 

    protected void setUp() {
        super.setUp()

		contextSource = new LdapContextSource()

		rolesToTest = ['EnHS-GLNE', 
			'EnHS-NCS-Financial', 
			'EnHS-WBFCR', 
			'EnHS-HS-AssetTrackers', 
			'EnHS-IT', 
			'EnHS-NCS-All', 
			'EnHS-NCS-Remove-List-Member', 
			'EnHS-NCS-List-Viewer', 
			'EnHS-NCS-Stress', 
			'EnHS-HS-Lyris', 
			'EnHS-Staff', 
			'NCS-Protected-Stress', 
			'EnHS-NCS-DocGen-Manage', 
			'EnHS-NunStudy-Investigator', 
			'EnHS-NCS-Segment', 
			'EnHS-NCS', 
			'EnHS-NCS-Protected-Exec-Function', 
			'EnHS-MES', 
			'EnHS-NCS-Edit-List-Member', 
			'EnHS-NCS-Lookup', 
			'EnHS-NCS-Protected-Ulnar-Length', 
			'EnHS-NCS-Receipt', 
			'NCS-HumanResources', 
			'EnHS-NCS-Add-List-Member', 
			'EnHS-HS-DBAs', 
			'EnHS-NunStudy-Administrative', 
			'EnHS-NCS-Calling', 
			'EnHS-NCS-Exec-Function', 
			'EnHS-NunStudy', 
			'EnHS-NCS-Segment-Lookup', 
			'EnHS-NCS-Security', 
			'EnHS-Print-Admins', 
			'EnHS-NCS-Formative', 
			'EnHS-MTC-RemoteDesktopUsers', 
			'NCS-Ulnar-Length', 
			'EnHS-HS-All', 
			'EnHS-AVAD', 
			'EnHS-NCS-DLR', 
			'EnHS-NCS-Protected', 
			'EnHS-NCS-Incentives', 
			'EnHS-NCST', 
			'EnHS-HS-IT', 
			'EnHS-NCS-Nutrition', 
			'EnHS-HPVHSS', 
			'EnHS-NCS-Assign-List-Auth', 
			'EnHS-HS-Staff', 
			'EnHS-NCS-DocGen', 
			'EnHS-NCS-List-Admin', 
			'EnHS-NCS-List-Tester', 
			'EnHS-NCS-Protected-Nutrition', 
			'EnHS-NCS-NORC-Data', 
			'EnHS-NCS-Data', 
			'EnHS-HS-BulkPrinters', 
			'NCS-IT', 
			'EnHS-All', 
			'EnHS-NCS-Reports']
    }

	/**
	 * This one test should cover everything added in the cleanRole() function
	 */
    void testMyRoles() {


		def grailsLdapAuthoritiesPopulator = new GrailsLdapAuthoritiesPopulator(contextSource, '')

		grailsLdapAuthoritiesPopulator.setGroupRoleAttribute('member')
		grailsLdapAuthoritiesPopulator.setGroupSearchFilter('fake={0}')
		grailsLdapAuthoritiesPopulator.setSearchSubtree(true)
		grailsLdapAuthoritiesPopulator.setDefaultRole('ROLE_USER')
		grailsLdapAuthoritiesPopulator.setIgnorePartialResultException(false)
		grailsLdapAuthoritiesPopulator.setRetrieveDatabaseRoles(false)
		grailsLdapAuthoritiesPopulator.setRoleStripPrefix('EnHS-')
		grailsLdapAuthoritiesPopulator.setRoleConvertDashes(true)
		grailsLdapAuthoritiesPopulator.setRoleToUpperCase(true)

		rolesToTest.each { roleName ->
			def testRole = new GrantedAuthorityImpl("ROLE_" + roleName)

			// the settings should run through all the permutations in one swipe
			def newRole = grailsLdapAuthoritiesPopulator.cleanRole(testRole)

			def cleanRoleName = 'ROLE_' + roleName.toUpperCase().replaceAll('-', '_').replaceFirst('ENHS_', '')

			// make sure our test did what we expected it to
			assert cleanRoleName == newRole.getAuthority()
		}

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
