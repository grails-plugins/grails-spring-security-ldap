package com.test

class BootStrap {
	def init = {

		def gauss = new User('gauss', 'n/a')
		gauss.save(failOnError: true)

		def roleSuperUser = new Role('ROLE_SUPERUSER').save(failOnError: true)
		UserRole.create gauss, roleSuperUser, true
	}
}
