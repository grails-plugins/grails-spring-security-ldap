/* Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.ldap.DatabaseOnlyLdapAuthoritiesPopulator
import org.codehaus.groovy.grails.plugins.springsecurity.ldap.GrailsLdapAuthoritiesPopulator
import org.codehaus.groovy.grails.plugins.springsecurity.ldap.GrailsLdapRoleMapper
import org.codehaus.groovy.grails.plugins.springsecurity.ldap.GrailsLdapUserDetailsManager
import org.codehaus.groovy.grails.plugins.springsecurity.ldap.SimpleAuthenticationSource

import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.authentication.BindAuthenticator
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator
import org.springframework.security.ldap.authentication.PasswordComparisonAuthenticator
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper
import org.springframework.security.ldap.userdetails.PersonContextMapper

class SpringSecurityLdapGrailsPlugin {

	String version = '1.0.5'
	String grailsVersion = '1.2.3 > *'
	Map dependsOn = [springSecurityCore: '1.1 > *']

	List pluginExcludes = [
		'docs/**',
		'src/docs/**'
	]

	String author = 'Burt Beckwith'
	String authorEmail = 'beckwithb@vmware.com'
	String title = 'LDAP authentication support for the Spring Security plugin.'
	String description = 'LDAP authentication support for the Spring Security plugin.'

	String documentation = 'http://grails.org/plugin/spring-security-ldap'

	def doWithSpring = {

		def conf = SpringSecurityUtils.securityConfig
		if (!conf || !conf.active) {
			return
		}

		SpringSecurityUtils.loadSecondaryConfig 'DefaultLdapSecurityConfig'
		// have to get again after overlaying DefaultLdapSecurityConfig
		conf = SpringSecurityUtils.securityConfig

		if (!conf.ldap.active) {
			return
		}

		println 'Configuring Spring Security LDAP ...'

		SpringSecurityUtils.registerProvider 'ldapAuthProvider'

		Class<?> contextFactoryClass = classForName(conf.ldap.context.contextFactoryClassName) // com.sun.jndi.ldap.LdapCtxFactory
		Class<?> dirObjectFactoryClass = classForName(conf.ldap.context.dirObjectFactoryClassName) // org.springframework.ldap.core.support.DefaultDirObjectFactory

		contextSource(DefaultSpringSecurityContextSource, conf.ldap.context.server) {
			userDn = conf.ldap.context.managerDn
			password = conf.ldap.context.managerPassword
			contextFactory = contextFactoryClass
			dirObjectFactory = dirObjectFactoryClass
			baseEnvironmentProperties = conf.ldap.context.baseEnvironmentProperties // none
			cacheEnvironmentProperties = conf.ldap.context.cacheEnvironmentProperties // true
			anonymousReadOnly = conf.ldap.context.anonymousReadOnly // false
			referral = conf.ldap.context.referral // null
			authenticationSource = ref('ldapAuthenticationSource')
		}

		ldapAuthenticationSource(SimpleAuthenticationSource) {
			userDn = conf.ldap.context.managerDn
			password = conf.ldap.context.managerPassword
		}

		String[] searchAttributesToReturn = toStringArray(conf.ldap.search.attributesToReturn) // null - all
		ldapUserSearch(FilterBasedLdapUserSearch, conf.ldap.search.base, conf.ldap.search.filter, contextSource) {
			searchSubtree = conf.ldap.search.searchSubtree // true
			derefLinkFlag = conf.ldap.search.derefLink // false
			searchTimeLimit = conf.ldap.search.timeLimit // 0 (unlimited)
			returningAttributes = searchAttributesToReturn
		}

		String[] attributesToReturn = toStringArray(conf.ldap.authenticator.attributesToReturn) // null - all
		String[] dnPatterns = toStringArray(conf.ldap.authenticator.dnPatterns) // null
		if (conf.ldap.authenticator.useBind) {
			ldapAuthenticator(BindAuthenticator, contextSource) {
				userSearch = ldapUserSearch
				if (attributesToReturn) {
					userAttributes = attributesToReturn
				}
				if (dnPatterns) {
					userDnPatterns = dnPatterns
				}
			}
		}
		else {
			ldapAuthenticator(PasswordComparisonAuthenticator, contextSource) {
				userSearch = ldapUserSearch
				if (attributesToReturn) {
					userAttributes = attributesToReturn
				}
				if (dnPatterns) {
					userDnPatterns = dnPatterns
				}
				passwordAttributeName = conf.ldap.authenticator.passwordAttributeName
				passwordEncoder = ref('passwordEncoder')
			}
		}

		if (conf.ldap.mapper.userDetailsClass == 'person') {
			ldapUserDetailsMapper(PersonContextMapper)
		}
		else if (conf.ldap.mapper.userDetailsClass == 'inetOrgPerson') {
			ldapUserDetailsMapper(InetOrgPersonContextMapper)
		}
		else {
			ldapUserDetailsMapper(LdapUserDetailsMapper) {
				convertToUpperCase = conf.ldap.mapper.convertToUpperCase // true
				passwordAttributeName = conf.ldap.mapper.passwordAttributeName // 'userPassword'
				if (conf.ldap.mapper.roleAttributes) {
					roleAttributes = conf.ldap.mapper.roleAttributes
				}
			}
		}

		if (conf.ldap.authorities.retrieveGroupRoles) {
			ldapAuthoritiesPopulator(GrailsLdapAuthoritiesPopulator, contextSource, conf.ldap.authorities.groupSearchBase) {
				groupRoleAttribute = conf.ldap.authorities.groupRoleAttribute
				groupSearchFilter = conf.ldap.authorities.groupSearchFilter
				searchSubtree = conf.ldap.authorities.searchSubtree
				if (conf.ldap.authorities.defaultRole) {
					defaultRole = conf.ldap.authorities.defaultRole
				}
				ignorePartialResultException = conf.ldap.authorities.ignorePartialResultException // false
				if (conf.ldap.useRememberMe && conf.ldap.authorities.retrieveDatabaseRoles) {
					userDetailsService = ref('ldapRememberMeUserDetailsService')
				}
				else {
					userDetailsService = ref('userDetailsService')
				}
				retrieveDatabaseRoles = conf.ldap.authorities.retrieveDatabaseRoles // false
			}
		}
		else if (conf.ldap.authorities.retrieveDatabaseRoles) {
			ldapAuthoritiesPopulator(DatabaseOnlyLdapAuthoritiesPopulator) {
				if (conf.ldap.authorities.defaultRole) {
					defaultRole = conf.ldap.authorities.defaultRole
				}
				if (conf.ldap.useRememberMe) {
					userDetailsService = ref('ldapRememberMeUserDetailsService')
				}
				else {
					userDetailsService = ref('userDetailsService')
				}
			}
		}
		else {
			ldapAuthoritiesPopulator(NullLdapAuthoritiesPopulator)
		}
		ldapAuthProvider(LdapAuthenticationProvider, ldapAuthenticator, ldapAuthoritiesPopulator) {
			userDetailsContextMapper = ldapUserDetailsMapper
			hideUserNotFoundExceptions = conf.ldap.auth.hideUserNotFoundExceptions // true
			useAuthenticationRequestCredentials = conf.ldap.auth.useAuthPassword // true
		}

		if (conf.ldap.useRememberMe) {
			if (!conf.rememberMe.persistent) {
				println "\n\nERROR: LDAP remember-me requires persistent remember-me; run the s2-create-persistent-token script to configure this\n\n"
				System.exit 1 
			}

			// needed just for database role lookups
			if (conf.ldap.authorities.retrieveGroupRoles) {
				ldapRememberMeUserDetailsService(GormUserDetailsService) {
					grailsApplication = ref('grailsApplication')
				}
			}

			String[] detailsAttributesToRetrieve = toStringArray(conf.ldap.rememberMe.detailsManager.attributesToRetrieve) // null - all
			userDetailsService(GrailsLdapUserDetailsManager, ref('contextSource')) {
				usernameMapper = ref('ldapUsernameMapper')
				userDetailsMapper = ref('ldapUserDetailsMapper')
				roleMapper = ref('ldapRoleMapper')
				passwordAttributeName = conf.ldap.rememberMe.detailsManager.passwordAttributeName // 'userPassword'
				groupSearchBase = conf.ldap.rememberMe.detailsManager.groupSearchBase // 'cn=groups'
				groupRoleAttributeName = conf.ldap.rememberMe.detailsManager.groupRoleAttributeName // 'cn'
				groupMemberAttributeName = conf.ldap.rememberMe.detailsManager.groupMemberAttributeName // 'uniquemember'
				if (detailsAttributesToRetrieve != null) {
					attributesToRetrieve = detailsAttributesToRetrieve
				}
			}

			ldapRoleMapper(GrailsLdapRoleMapper) {
				groupRoleAttributeName = conf.ldap.rememberMe.detailsManager.groupRoleAttributeName // 'cn'
			}

			ldapUsernameMapper(DefaultLdapUsernameToDnMapper,
				conf.ldap.rememberMe.usernameMapper.userDnBase,
				conf.ldap.rememberMe.usernameMapper.usernameAttribute)
		}
	}

	private String[] toStringArray(value) {
		if (value == null) {
			return null
		}
		if (value instanceof String) {
			value = [value]
		}
		value as String[]
	}

	private Class<?> classForName(String name) {
		Class.forName name, true, Thread.currentThread().contextClassLoader
	}
}
