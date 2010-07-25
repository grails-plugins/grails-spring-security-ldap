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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.util.Assert;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator implements InitializingBean {

	private GrailsUserDetailsService _userDetailsService;
	private Boolean _retrieveDatabaseRoles;

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
	protected Set<GrantedAuthority> getAdditionalRoles(final DirContextOperations user, final String username) {
		if (_retrieveDatabaseRoles) {
			UserDetails dbDetails = _userDetailsService.loadUserByUsername(username, true);
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
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(_userDetailsService, "userDetailsService must be specified");
		Assert.notNull(_retrieveDatabaseRoles, "retrieveDatabaseRoles must be specified");
	}
}
