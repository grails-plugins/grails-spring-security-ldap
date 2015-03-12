import com.testldap.Role
import com.testldap.User
import com.testldap.UserRole

class BootStrap {

	def init = {
		new User(username: 'person1', password: 'n/a').save(failOnError: true)
		new User(username: 'person2', password: 'n/a').save(failOnError: true)
		def person3 = new User(username: 'person3', password: 'n/a').save(failOnError: true)
		def roleSuperUser = new Role(authority: 'ROLE_SUPERUSER').save(failOnError: true)
		UserRole.create person3, roleSuperUser, true
	}
}
