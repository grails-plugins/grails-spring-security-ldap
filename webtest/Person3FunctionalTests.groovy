class Person3FunctionalTests extends AbstractSecurityWebTest {

	// person3 has ROLE_USER from LDAP and ROLE_SUPERUSER from database

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
			j_username = 'person3'
			j_password = 'password3'
			_spring_security_remember_me = true
			clickButton 'Login'
		}

		get '/secure/superusers'
		assertContentContains 'ROLE_USER'
		assertContentContains 'ROLE_SUPERUSER'
		assertContentDoesNotContain 'ROLE_ADMIN'

		get '/secure/users'
		assertContentContains 'ROLE_USER'
		assertContentContains 'ROLE_SUPERUSER'
		assertContentDoesNotContain 'ROLE_ADMIN'

		get '/secure/admins'
		assertContentContains "Sorry, you're not authorized to view this page."
	}
}
