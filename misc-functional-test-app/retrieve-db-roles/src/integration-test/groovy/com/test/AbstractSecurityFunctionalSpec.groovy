package com.test

import geb.spock.GebReportingSpec
import grails.testing.mixin.integration.Integration
import pages.LoginPage
import pages.LogoutPage
import spock.lang.Stepwise

@Integration
@Stepwise
abstract class AbstractSecurityFunctionalSpec extends GebReportingSpec {

	void setup() {
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
		assert contentContains(expected)
	}

	boolean contentContains(String expected) {
		browser.driver.pageSource.contains(expected)
	}

	protected void assertContentDoesNotContain(String unexpected) {
		assert !contentContains(unexpected)
	}
}
