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
package grails.plugin.springsecurity.ldap.core;

import org.springframework.ldap.core.AuthenticationSource;

/**
 * Copied from the package-default inner class in <code>AbstractContextSource</code>.
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class SimpleAuthenticationSource implements AuthenticationSource {

	protected String password;
	protected String userDn;

	/**
	 * {@inheritDoc}
	 * @see org.springframework.ldap.core.AuthenticationSource#getCredentials()
	 */
	public String getCredentials() {
		return password;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.ldap.core.AuthenticationSource#getPrincipal()
	 */
	public String getPrincipal() {
		return userDn;
	}

	/**
	 * Dependency injection for the password.
	 * @param pwd the password
	 */
	public void setPassword(final String pwd) {
		password = pwd;
	}

	/**
	 * Dependency injection for the userDn.
	 * @param dn the userDn
	 */
	public void setUserDn(final String dn) {
		userDn = dn;
	}
}
