class Person1FunctionalTests extends AbstractSecurityWebTest {

	// person1 has ROLE_USER from LDAP

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
			j_username = 'person1'
			j_password = 'password1'
			_spring_security_remember_me = true
			clickButton 'Login'
		}

		get '/secure/users'
		assertContentContains 'ROLE_USER'
		assertContentDoesNotContain 'ROLE_ADMIN'
		assertContentDoesNotContain 'ROLE_SUPERUSER'

		get '/secure/admins'
		assertContentContains "Sorry, you're not authorized to view this page."

		get '/secure/superusers'
		assertContentContains "Sorry, you're not authorized to view this page."
	}
}
