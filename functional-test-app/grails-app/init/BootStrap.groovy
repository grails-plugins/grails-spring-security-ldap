import com.test.Role
import com.test.User
import com.test.UserRole

class BootStrap {
	def init = {
		new User('person1', 'n/a').save(failOnError: true)
		new User('person2', 'n/a').save(failOnError: true)
		def person3 = new User('person3', 'n/a').save(failOnError: true)
		def roleSuperUser = new Role('ROLE_SUPERUSER').save(failOnError: true)
		UserRole.create person3, roleSuperUser, true
	}
}
