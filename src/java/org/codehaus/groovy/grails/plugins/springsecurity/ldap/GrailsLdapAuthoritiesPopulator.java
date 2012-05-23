/* Copyright 2006-2012 SpringSource.
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

	private String _rolePrefix = "ROLE_";
	private String _roleStripPrefix;
	private String _roleStripSuffix;
	private boolean _roleConvertDashes = false;
	private boolean _roleToUpperCase = false;

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

	/**
	 * This cleans a role based on configuration flags set.
	 * @param role the role to clean
	 * @return the cleaned role
	 */
	public GrantedAuthority cleanRole(GrantedAuthority role) {
		if (!(role instanceof GrantedAuthorityImpl)) {
			return role;
		}

		GrantedAuthorityImpl newRole = (GrantedAuthorityImpl) role;

		if (_roleConvertDashes && newRole.getAuthority().indexOf('-') > -1) {
			// replace dashes
			newRole = new GrantedAuthorityImpl(newRole.getAuthority().replaceAll("-", "_"));
		}

		if (_roleToUpperCase && !newRole.getAuthority().toUpperCase().equals(newRole.getAuthority())) {
			// convert to upper case
			newRole = new GrantedAuthorityImpl(newRole.getAuthority().toUpperCase());
		}

		if (_roleStripPrefix != null) {
			// strip prefix if found
			String tempPrefix = _rolePrefix + _roleStripPrefix;
			if (tempPrefix != null && tempPrefix.length() > 0
					&& newRole.getAuthority().indexOf(tempPrefix) == 0
					&& newRole.getAuthority().length() > tempPrefix.length()) {
				// replace dashes
				newRole = new GrantedAuthorityImpl(newRole.getAuthority().replace(tempPrefix, _rolePrefix).trim());
			}
		}

		if (_roleStripSuffix != null) {
			// strip suffix if found
			if (_roleStripSuffix != null && _roleStripSuffix.length() > 0
					&& newRole.getAuthority().length() > _roleStripSuffix.length()
					&& newRole.getAuthority().endsWith(_roleStripSuffix)) {
				int roleLength = newRole.getAuthority().length();
				int suffixLength = _roleStripSuffix.length();
				newRole = new GrantedAuthorityImpl(
						newRole.getAuthority().substring(0, roleLength - suffixLength).trim());
			}
		}

		if (newRole.getAuthority().indexOf(' ') > -1) {
			// replace spaces
			newRole = new GrantedAuthorityImpl(newRole.getAuthority().replaceAll(" ", "_"));
		}

		while (newRole.getAuthority().indexOf("__") > -1) {
			// replace __
			newRole = new GrantedAuthorityImpl(newRole.getAuthority().replaceAll("__", "_"));
		}
		return newRole;
	}

	@Override
	public Set<GrantedAuthority> getGroupMembershipRoles(final String userDn, final String username) {
		Set<GrantedAuthority> roles = super.getGroupMembershipRoles(userDn, username);
		Set<GrantedAuthority> fixed = new HashSet<GrantedAuthority>();
		for (GrantedAuthority role : roles) {
			fixed.add(cleanRole(role));
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
	 * Hooks to adjust prefix string if other cleaning flags are set
	 */
	private void updateRoleStripPrefix() {
		// convert dashes
		if (_roleConvertDashes) {
			if (_roleStripPrefix != null && _roleStripPrefix.indexOf('-') > -1) {
				_roleStripPrefix = _roleStripPrefix.replaceAll("-", "_");
			}
		}
		// To upper case
		if (_roleToUpperCase) {
			if (_roleStripPrefix != null && !_roleStripPrefix.toUpperCase().equals(_roleStripPrefix)) {
				_roleStripPrefix = _roleStripPrefix.toUpperCase();
			}
		}
	}

	/**
	 * Hooks to adjust suffix string if other cleaning flags are set
	 */
	private void updateRoleStripSuffix() {
		// convert dashes
		if (_roleConvertDashes) {
			if (_roleStripSuffix != null && _roleStripSuffix.indexOf('-') > -1) {
				_roleStripSuffix = _roleStripSuffix.replaceAll("-", "_");
			}
		}
		// To upper case
		if (_roleToUpperCase) {
			if (_roleStripSuffix != null && !_roleStripSuffix.toUpperCase().equals(_roleStripSuffix)) {
				_roleStripSuffix = _roleStripSuffix.toUpperCase();
			}
		}
	}

	/**
	 * Dependency injection for the name of the rolePrefix to use when creating new roles.
	 * @param rolePrefix defaults to 'ROLE_'.  Changing this is not recommended.
	 */
	@Override
	public void setRolePrefix(final String rolePrefix) {
		_rolePrefix = rolePrefix;
	}

	/**
	 * Dependency injection for whether or not to remove a prefix string from a LDAP
	 * group name if it matches the beginning of the group name, but not the full
	 * name of the group.
	 * @param roleStripPrefix if not null, this is stripped from the group name before it is made into a role
	 */
	public void setRoleStripPrefix(final String roleStripPrefix) {
		_roleStripPrefix = roleStripPrefix;
		updateRoleStripPrefix();
	}

	/**
	 * Dependency injection for whether or not to remove a suffix string from a LDAP
	 * group name if it matches the end of the group name, but not the full
	 * name of the group.
	 * @param roleStripSuffix if not null, this is stripped from the group name before it is made into a role
	 */
	public void setRoleStripSuffix(final String roleStripSuffix) {
		_roleStripSuffix = roleStripSuffix;
		updateRoleStripSuffix();
	}

	/**
	 * Dependency injection for whether or not to convert all dashes to underscores if found in a
	 * group name before it is made into a role.
	 * @param roleConvertDashes if <code>true</code>, all dashes are converted to underscores
	 */
	public void setRoleConvertDashes(final boolean roleConvertDashes) {
		_roleConvertDashes = roleConvertDashes;
		updateRoleStripPrefix();
		updateRoleStripSuffix();
	}

	/**
	 * Dependency injection for whether or not to convert group names to uppercase before they
	 * are made into roles.
	 * @param roleToUpperCase if <code>true</code>, roles are converted to uppercase
	 */
	public void setRoleToUpperCase(final boolean roleToUpperCase) {
		_roleToUpperCase = roleToUpperCase;
		updateRoleStripPrefix();
		updateRoleStripSuffix();
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
