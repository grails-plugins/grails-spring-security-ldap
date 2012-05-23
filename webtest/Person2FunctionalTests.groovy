class Person2FunctionalTests extends AbstractSecurityWebTest {

	// person2 has ROLE_USER and ROLE_ADMIN from LDAP

	void testSecurity() {
		checkSecuredUrlsNotVisibleWithoutAuth()
		loginAndCheckAllowed()
	}

	private void checkSecuredUrlsNotVisibleWithoutAuth() {
		get '/secure/admins'
		assertContentContains 'Please Login'

		get '/secure/users'
		assertContentContains 'Please Login'

		get '/secure/superusers'
		assertContentContains 'Please Login'
	}

	private void loginAndCheckAllowed() {
		get '/login/auth'
		assertContentContains 'Please Login'

		form {
			j_username = 'person2'
			j_password = 'password2'
			_spring_security_remember_me = true
			clickButton 'Login'
		}

		get '/secure/admins'
		assertContentContains 'ROLE_USER'
		assertContentContains 'ROLE_ADMIN'
		assertContentDoesNotContain 'ROLE_SUPERUSER'
		assertContentDoesNotContain 'ROLE_FOO_BAR'

		get '/secure/users'
		assertContentContains 'ROLE_USER'
		assertContentContains 'ROLE_ADMIN'
		assertContentDoesNotContain 'ROLE_SUPERUSER'
		assertContentDoesNotContain 'ROLE_FOO_BAR'

		get '/secure/superusers'
		assertContentContains "Sorry, you're not authorized to view this page."
	}
}
