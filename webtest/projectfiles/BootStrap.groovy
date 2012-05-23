import com.testldap.Role
import com.testldap.User
import com.testldap.UserRole

class BootStrap {

	def sessionFactory

	def init = { servletContext ->

		new User(username: 'person1', password: 'n/a', enabled: true).save()

		new User(username: 'person2', password: 'n/a', enabled: true).save()

		def person3 = new User(username: 'person3', password: 'n/a', enabled: true).save()
		def roleSuperUser = new Role(authority: 'ROLE_SUPERUSER').save()
		UserRole.create person3, roleSuperUser

		sessionFactory.currentSession.flush()
	}
}
