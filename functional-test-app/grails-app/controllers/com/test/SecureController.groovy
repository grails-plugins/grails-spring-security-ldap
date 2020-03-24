package com.test

import grails.plugin.springsecurity.annotation.Secured

class SecureController {

	def springSecurityService

	@Secured('ROLE_ADMIN')
	def admins() {
		renderRoles()
	}

	@Secured('ROLE_USER')
	def users() {
		renderRoles()
	}

	@Secured('ROLE_SUPERUSER')
	def superusers() {
		renderRoles()
	}

	@Secured('ROLE_FOO_BAR')
	def fooBar() {
		renderRoles()
	}

	private void renderRoles() {
		render 'You have these roles: ' + springSecurityService.principal.authorities.join(' ')
	}
}
