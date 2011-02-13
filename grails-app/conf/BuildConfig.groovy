grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir	= 'target/test-reports'
grails.project.docs.output.dir = 'docs' // for the gh-pages branch

grails.project.dependency.resolution = {

	inherits('global')

	log 'warn'

	repositories {        
		grailsPlugins()
		grailsHome()
		ebr() // SpringSource  http://www.springsource.com/repository
	}

	dependencies {
		runtime('org.springframework.security:org.springframework.security.ldap:3.0.4.RELEASE') {
			transitive = false
		}
		runtime('org.springframework.ldap:org.springframework.ldap:1.3.0.RELEASE') {
			transitive = false
		}
	}
}
