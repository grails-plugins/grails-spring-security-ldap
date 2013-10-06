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

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;

/**
 * Based on the anonymous inner class in DefaultSpringSecurityContextSource.
 *
 * @author Luke Taylor
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsSimpleDirContextAuthenticationStrategy extends SimpleDirContextAuthenticationStrategy {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected String userDn;

	@Override
	public void setupEnvironment(@SuppressWarnings("rawtypes") Hashtable env, String dn, String password) {
		super.setupEnvironment(env, dn, password);
		// Remove the pooling flag unless we are authenticating as the 'manager' user.
		if (!userDn.equals(dn) && env.containsKey(AbstractContextSource.SUN_LDAP_POOLING_FLAG)) {
			log.debug("Removing pooling flag for user {0}", dn);
			env.remove(AbstractContextSource.SUN_LDAP_POOLING_FLAG);
		}
	}

	/**
	 * Dependency injection for the userDn.
	 * @param dn the userDn
	 */
	public void setUserDn(String dn) {
		userDn = dn;
	}
}
