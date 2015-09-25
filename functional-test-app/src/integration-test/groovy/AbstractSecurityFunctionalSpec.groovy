import geb.spock.GebReportingSpec
import grails.test.mixin.integration.Integration
import pages.LoginPage
import pages.LogoutPage
import spock.lang.Stepwise

@Integration
//@Rollback
@Stepwise
abstract class AbstractSecurityFunctionalSpec extends GebReportingSpec {

	def setup() {
		logout()
	}

	protected void login(String user, String pwd) {
		to LoginPage
		username = user
		password = pwd
		loginButton.click()
	}

	protected void logout() {
		to LogoutPage
		logoutButton.click()
		browser.clearCookies()
	}

	protected void assertContentContains(String expected) {
		assert $().text().contains(expected)
	}

	protected void assertContentDoesNotContain(String unexpected) {
		assert !$().text().contains(unexpected)
	}
}
