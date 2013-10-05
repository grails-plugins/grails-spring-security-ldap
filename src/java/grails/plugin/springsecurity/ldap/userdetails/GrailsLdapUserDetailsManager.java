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

import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsLdapUserDetailsManager extends LdapUserDetailsManager implements GrailsUserDetailsService {

	/**
	 * Constructor.
	 * @param contextSource the context source
	 */
	public GrailsLdapUserDetailsManager(final ContextSource contextSource) {
		super(contextSource);
	}

	public UserDetails loadUserByUsername(final String username, final boolean loadRoles)
			throws UsernameNotFoundException, DataAccessException {
		return super.loadUserByUsername(username);
	}
}
