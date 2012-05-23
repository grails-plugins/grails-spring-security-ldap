import grails.plugins.springsecurity.Secured

class SecureController {

	def springSecurityService

	@Secured(['ROLE_ADMIN'])
	def admins = {
		renderRoles()
	}

	@Secured(['ROLE_USER'])
	def users = {
		renderRoles()
	}

	@Secured(['ROLE_SUPERUSER'])
	def superusers = {
		renderRoles()
	}

	@Secured(['ROLE_FOO_BAR'])
	def fooBar = {
		renderRoles()
	}

	private void renderRoles() {
		def roles = new StringBuilder('You have these roles: ')
		for (auth in springSecurityService.principal.authorities) {
			roles.append auth.authority
			roles.append ' '
		}
		render roles.toString()
	}
}
