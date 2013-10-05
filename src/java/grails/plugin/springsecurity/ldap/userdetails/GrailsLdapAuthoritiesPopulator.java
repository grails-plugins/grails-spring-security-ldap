/* Copyright 2006-2013 SpringSource.
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
package grails.plugin.springsecurity.ldap.userdetails;

import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.util.Assert;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator implements InitializingBean {

	private GrailsUserDetailsService userDetailsService;
	private Boolean retrieveDatabaseRoles;

	private String rolePrefix = "ROLE_";
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

	/**
	 * This cleans a role based on configuration flags set.
	 * @param role the role to clean
	 * @return the cleaned role
	 */
	public GrantedAuthority cleanRole(GrantedAuthority role) {
		if (!(role instanceof SimpleGrantedAuthority)) {
			return role;
		}

		SimpleGrantedAuthority newRole = (SimpleGrantedAuthority) role;

		if (roleConvertDashes && newRole.getAuthority().indexOf('-') > -1) {
			// replace dashes
			newRole = new SimpleGrantedAuthority(newRole.getAuthority().replaceAll("-", "_"));
		}

		if (roleToUpperCase && !newRole.getAuthority().toUpperCase().equals(newRole.getAuthority())) {
			// convert to upper case
			newRole = new SimpleGrantedAuthority(newRole.getAuthority().toUpperCase());
		}

		if (roleStripPrefix != null) {
			// strip prefix if found
			String tempPrefix = rolePrefix + roleStripPrefix;
			if (tempPrefix.length() > 0
					&& newRole.getAuthority().indexOf(tempPrefix) == 0
					&& newRole.getAuthority().length() > tempPrefix.length()) {
				// replace dashes
				newRole = new SimpleGrantedAuthority(newRole.getAuthority().replace(tempPrefix, rolePrefix).trim());
			}
		}

		if (roleStripSuffix != null) {
			// strip suffix if found
			if (roleStripSuffix != null && roleStripSuffix.length() > 0
					&& newRole.getAuthority().length() > roleStripSuffix.length()
					&& newRole.getAuthority().endsWith(roleStripSuffix)) {
				int roleLength = newRole.getAuthority().length();
				int suffixLength = roleStripSuffix.length();
				newRole = new SimpleGrantedAuthority(
						newRole.getAuthority().substring(0, roleLength - suffixLength).trim());
			}
		}

		if (newRole.getAuthority().indexOf(' ') > -1) {
			// replace spaces
			newRole = new SimpleGrantedAuthority(newRole.getAuthority().replaceAll(" ", "_"));
		}

		while (newRole.getAuthority().indexOf("__") > -1) {
			// replace __
			newRole = new SimpleGrantedAuthority(newRole.getAuthority().replaceAll("__", "_"));
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
		if (retrieveDatabaseRoles) {
			UserDetails dbDetails = null;
			try {
				dbDetails = userDetailsService.loadUserByUsername(username, true);
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
		userDetailsService = service;
	}

	/**
	 * Dependency injection for whether to retrieve roles from the database in addition to LDAP.
	 * @param retrieve  if <code>true</code> then load roles from database also
	 */
	public void setRetrieveDatabaseRoles(final boolean retrieve) {
		retrieveDatabaseRoles = retrieve;
	}

	/**
	 * Hooks to adjust prefix string if other cleaning flags are set
	 */
	private void updateRoleStripPrefix() {
		// convert dashes
		if (roleConvertDashes) {
			if (roleStripPrefix != null && roleStripPrefix.indexOf('-') > -1) {
				roleStripPrefix = roleStripPrefix.replaceAll("-", "_");
			}
		}
		// To upper case
		if (roleToUpperCase) {
			if (roleStripPrefix != null && !roleStripPrefix.toUpperCase().equals(roleStripPrefix)) {
				roleStripPrefix = roleStripPrefix.toUpperCase();
			}
		}
	}

	/**
	 * Hooks to adjust suffix string if other cleaning flags are set
	 */
	private void updateRoleStripSuffix() {
		// convert dashes
		if (roleConvertDashes) {
			if (roleStripSuffix != null && roleStripSuffix.indexOf('-') > -1) {
				roleStripSuffix = roleStripSuffix.replaceAll("-", "_");
			}
		}
		// To upper case
		if (roleToUpperCase) {
			if (roleStripSuffix != null && !roleStripSuffix.toUpperCase().equals(roleStripSuffix)) {
				roleStripSuffix = roleStripSuffix.toUpperCase();
			}
		}
	}

	/**
	 * Dependency injection for the name of the rolePrefix to use when creating new roles.
	 * @param prefix defaults to 'ROLE_'.  Changing this is not recommended.
	 */
	@Override
	public void setRolePrefix(final String prefix) {
		rolePrefix = prefix;
	}

	/**
	 * Dependency injection for whether or not to remove a prefix string from a LDAP
	 * group name if it matches the beginning of the group name, but not the full
	 * name of the group.
	 * @param prefix if not null, this is stripped from the group name before it is made into a role
	 */
	public void setRoleStripPrefix(final String prefix) {
		roleStripPrefix = prefix;
		updateRoleStripPrefix();
	}

	/**
	 * Dependency injection for whether or not to remove a suffix string from a LDAP
	 * group name if it matches the end of the group name, but not the full
	 * name of the group.
	 * @param suffix if not null, this is stripped from the group name before it is made into a role
	 */
	public void setRoleStripSuffix(final String suffix) {
		roleStripSuffix = suffix;
		updateRoleStripSuffix();
	}

	/**
	 * Dependency injection for whether or not to convert all dashes to underscores if found in a
	 * group name before it is made into a role.
	 * @param convertDashes if <code>true</code>, all dashes are converted to underscores
	 */
	public void setRoleConvertDashes(final boolean convertDashes) {
		roleConvertDashes = convertDashes;
		updateRoleStripPrefix();
		updateRoleStripSuffix();
	}

	/**
	 * Dependency injection for whether or not to convert group names to uppercase before they
	 * are made into roles.
	 * @param toUpperCase if <code>true</code>, roles are converted to uppercase
	 */
	public void setRoleToUpperCase(final boolean toUpperCase) {
		roleToUpperCase = toUpperCase;
		updateRoleStripPrefix();
		updateRoleStripSuffix();
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.notNull(userDetailsService, "userDetailsService must be specified");
		Assert.notNull(retrieveDatabaseRoles, "retrieveDatabaseRoles must be specified");
	}
}
