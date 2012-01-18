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
package org.codehaus.groovy.grails.plugins.springsecurity.ldap;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.util.Assert;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator implements InitializingBean {

	private GrailsUserDetailsService _userDetailsService;
	private Boolean _retrieveDatabaseRoles;

	private String roleStripPrefix;
	private String roleStripSuffix;
	private boolean roleConvertDashes = false;
	private boolean roleToUpperCase = false;

	/**
	 * Constructor for group search scenarios. <tt>userRoleAttributes</tt> may still be
	 * set as a property.
	 *
	 * @param contextSource supplies the contexts used to search for user roles.
	 * @param groupSearchBase          if this is an empty string the search will be performed from the root DN of the
	 *                                 context factory.
	 */
	public GrailsLdapAuthoritiesPopulator(final ContextSource contextSource, final String groupSearchBase) {
		super(contextSource, groupSearchBase);
	}

	@Override
	public Set<GrantedAuthority> getGroupMembershipRoles(final String userDn, final String username) {
		Set<GrantedAuthority> roles = super.getGroupMembershipRoles(userDn, username);
		Set<GrantedAuthority> fixed = new HashSet<GrantedAuthority>();
		for (GrantedAuthority role : roles) {
			if (role instanceof GrantedAuthorityImpl) {
				GrantedAuthorityImpl newRole = (GrantedAuthorityImpl) role;

				// replace dashes
				if (roleConvertDashes && newRole.getAuthority().indexOf('-') > -1) {
					logger.debug("converting dashes to underscores in authority:" + newRole.getAuthority());
					newRole = new GrantedAuthorityImpl(newRole.getAuthority().replaceAll("-", "_"));
					if (roleStripPrefix.indexOf('-') > -1) {
						roleStripPrefix = roleStripPrefix.replaceAll("-", "_");
					}
					if (roleStripSuffix.indexOf('-') > -1) {
						roleStripSuffix = roleStripSuffix.replaceAll("-", "_");
					}
				}

				// convert to upper case
				if (roleToUpperCase && !newRole.getAuthority().toUpperCase().equals(newRole.getAuthority())) {
					logger.debug("converting role to uppercase:" + newRole.getAuthority());
					newRole = new GrantedAuthorityImpl(newRole.getAuthority().toUpperCase());
					if (!roleStripPrefix.toUpperCase().equals(roleStripPrefix)) {
						roleStripPrefix = roleStripPrefix.toUpperCase();
					}
					if (!roleStripSuffix.toUpperCase().equals(roleStripSuffix)) {
						roleStripSuffix = roleStripSuffix.toUpperCase();
					}
				}

				// strip prefix if found
				String tempPrefix = "ROLE_" + roleStripPrefix;
				if (tempPrefix != null && tempPrefix.length() > 0 
						&& newRole.getAuthority().indexOf(tempPrefix) == 0
						&& newRole.getAuthority().length() > tempPrefix.length()) {
					// replace dashes
					logger.debug("removing prefix '" + roleStripPrefix + "' from authority:" + newRole.getAuthority());
					newRole = new GrantedAuthorityImpl(newRole.getAuthority().replace(tempPrefix, "ROLE_").trim());
				}

				// strip suffix if found
				if (roleStripSuffix != null && roleStripSuffix.length() > 0 
						&& newRole.getAuthority().length() > roleStripSuffix.length()) {
					int roleLength = newRole.getAuthority().length();
					int suffixLength = roleStripSuffix.length();
					int suffixIndex = newRole.getAuthority().indexOf(roleStripSuffix);
					if (suffixIndex == (roleLength - suffixLength)) {
						logger.debug("removing suffix '" + roleStripSuffix + "' from authority:" + newRole.getAuthority());
						newRole = new GrantedAuthorityImpl(newRole.getAuthority().replace(roleStripSuffix, "").trim());
					}
				}

				// replace spaces
				if (newRole.getAuthority().indexOf(' ') > -1) {
					logger.debug("removing spaces from authority:" + newRole.getAuthority());
					newRole = new GrantedAuthorityImpl(newRole.getAuthority().replaceAll(" ", "_"));
				}
			}
			fixed.add(newRole);
		}
		return fixed;
	}

	@Override
	protected Set<GrantedAuthority> getAdditionalRoles(final DirContextOperations user, final String username) {
		if (_retrieveDatabaseRoles) {
			UserDetails dbDetails = null;
			try {
				dbDetails = _userDetailsService.loadUserByUsername(username, true);
			}
			catch (UsernameNotFoundException ignored) {
				// just looking for roles, so ignore the UsernameNotFoundException
				return null;
			}

			if (dbDetails.getAuthorities() != null) {
				return new HashSet<GrantedAuthority>(dbDetails.getAuthorities());
			}
		}
		return null;
	}

	/**
	 * Dependency injection for the user details service.
	 * @param service  the service
	 */
	public void setUserDetailsService(final GrailsUserDetailsService service) {
		_userDetailsService = service;
	}

	/**
	 * Dependency injection for whether to retrieve roles from the database in addition to LDAP.
	 * @param retrieve  if <code>true</code> then load roles from database also
	 */
	public void setRetrieveDatabaseRoles(final boolean retrieve) {
		_retrieveDatabaseRoles = retrieve;
	}

	/**
	 * Dependency injection for whether or not to remove a prefix string from a LDAP
	 * group name if it matches the beginning of the group name, but not the full
	 * name of the group.
	 * @param roleStripPrefix if not null, this is stripped from the group name before it is made into a role
	 */
	public void setRoleStripPrefix(final String roleStripPrefix) {
		this.roleStripSuffix = roleStripSuffix;
	}

	/**
	 * Dependency injection for whether or not to remove a suffix string from a LDAP
	 * group name if it matches the end of the group name, but not the full
	 * name of the group.
	 * @param roleStripSuffix if not null, this is stripped from the group name before it is made into a role
	 */
	public void setRoleStripSuffix(final String roleStripSuffix) {
		this.roleStripSuffix = roleStripSuffix;
	}

	/**
	 * Dependency injection for whether or not to convert all dashes to underscores if found in a
	 * group name before it is made into a role.
	 * @param roleConvertDashes if <code>true</code>, all dashes are converted to underscores
	 */
	public void setRoleConvertDashes(final boolean roleConvertDashes) {
		this.roleConvertDashes = roleConvertDashes;
	}

	/**
	 * Dependency injection for whether or not to convert group names to uppercase before they
	 * are made into roles.
	 * @param roleToUpperCase if <code>true</code>, roles are converted to uppercase
	 */
	public void setRoleToUpperCase(final boolean roleToUpperCase) {
		this.roleToUpperCase = roleToUpperCase;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.notNull(_userDetailsService, "userDetailsService must be specified");
		Assert.notNull(_retrieveDatabaseRoles, "retrieveDatabaseRoles must be specified");
	}
}
