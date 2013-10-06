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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Copied from the private implementation in <code>LdapUserDetailsManager</code> to allow overriding.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsLdapRoleMapper implements AttributesMapper {

	protected String groupRoleAttributeName;
	protected String rolePrefix = "ROLE_";

	/**
	 * {@inheritDoc}
	 * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
	 */
	public Object mapFromAttributes(final Attributes attributes) throws NamingException {
		Attribute roleAttr = attributes.get(groupRoleAttributeName);

		NamingEnumeration<?> ne = roleAttr.getAll();
		// assert ne.hasMore();
		Object group = ne.next();
		String role = group.toString();

		return new SimpleGrantedAuthority(rolePrefix + role.toUpperCase());
	}

	/**
	 * Dependency injection for <code>groupRoleAttributeName</code>.
	 * @param name the name
	 */
	public void setGroupRoleAttributeName(final String name) {
		groupRoleAttributeName = name;
	}

	/**
	 * Dependency injection for <code>rolePrefix</code>.
	 * @param prefix defaults to 'ROLE_'. Changing this is not recommended.
	 */
	public void setRolePrefix(final String prefix) {
		rolePrefix = prefix;
	}
}
