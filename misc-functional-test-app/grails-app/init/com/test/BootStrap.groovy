package com.test

class BootStrap {
	def init = {

		def riemann = new User('riemann', 'n/a')
		riemann.save(failOnError: true)

		def gauss = new User('gauss', 'n/a')
		gauss.save(failOnError: true)

        def euler = new User('euler', 'n/a')
		euler.save(failOnError: true)

		def euclid = new User('euclid', 'n/a')
		euclid.save(failOnError: true)

		def einstein = new User('einstein', 'n/a')
		einstein.save(failOnError: true)

		def newton = new User('newton', 'n/a')
		newton.save(failOnError: true)

		def galieleo = new User('galieleo', 'n/a')
		galieleo.save(failOnError: true)

		def tesla = new User('tesla', 'n/a')
		tesla.save(failOnError: true)

		def roleSuperUser = new Role('ROLE_SUPERUSER').save(failOnError: true)
		UserRole.create einstein, roleSuperUser, true
	}
}
