import pages.IndexPage
import pages.LoginPage
import pages.SecureSuperuserPage
import pages.SecureUserPage

class SecureControllerFunctionalSpec extends AbstractSecurityFunctionalSpec {

	void 'secured urls are not visible without auth'() {
		when:
		go SecureUserPage.url

		then:
		assertContentContains 'Please Login'

		when:
		go SecureSuperuserPage.url

		then:
		assertContentContains 'Please Login'
	}

	void 'secured urls are visible when authenticated'() {
		when:
		login 'euler', 'password1'

		then:
		at LoginPage

		when:
		login 'euler', 'password'

		then:
		at IndexPage

		when:
		to SecureUserPage

		then:
		assertContentContains 'ROLE_USER'
		assertContentDoesNotContain 'ROLE_SUPERUSER'

		when:
		go SecureSuperuserPage.url

		then:
		assertContentContains "Sorry, you're not authorized to view this page."

		when:
		logout()

		then:
		at IndexPage

		when:
		go SecureUserPage.url

		then:
		assertContentContains 'Please Login'

		when:
		login 'einstein', 'password'

		then:
		at SecureUserPage

		and:
		assertContentContains 'ROLE_USER'
		assertContentContains 'ROLE_SUPERUSER'

		when:
		to SecureUserPage

		then:
		assertContentContains 'ROLE_USER'
		assertContentContains 'ROLE_SUPERUSER'
	}
}
