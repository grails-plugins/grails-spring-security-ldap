/* Copyright 2011-2013 SpringSource.
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

import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class DatabaseOnlyLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator, InitializingBean {

	protected GrantedAuthority defaultRole;
	protected GrailsUserDetailsService userDetailsService;

	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator#getGrantedAuthorities(
	 * 	org.springframework.ldap.core.DirContextOperations, java.lang.String)
	 */
	public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {

		UserDetails dbDetails = null;
		try {
			dbDetails = userDetailsService.loadUserByUsername(username, true);
		}
		catch (UsernameNotFoundException ignored) {
			// just looking for roles, so ignore the UsernameNotFoundException
			return AuthorityUtils.NO_AUTHORITIES;
		}

		if (dbDetails.getAuthorities() == null) {
			return AuthorityUtils.NO_AUTHORITIES;
		}

		Collection<GrantedAuthority> roles = new HashSet<GrantedAuthority>(dbDetails.getAuthorities());
		if (defaultRole != null) {
			roles.add(defaultRole);
		}

		return roles;
	}

	/**
	 * Dependency injection for the user details service.
	 * @param service the service
	 */
	public void setUserDetailsService(final GrailsUserDetailsService service) {
		userDetailsService = service;
	}

	/**
	 * The default role which will be assigned to all users.
	 *
	 * @param defaultRoleName the role name, including any desired prefix.
	 */
	public void setDefaultRole(String defaultRoleName) {
		Assert.notNull(defaultRoleName, "The defaultRole property cannot be set to null");
		defaultRole = new SimpleGrantedAuthority(defaultRoleName);
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.notNull(userDetailsService, "userDetailsService must be specified");
	}
}
