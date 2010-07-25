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
		runtime('org.springframework.security:org.springframework.security.ldap:3.0.3.RELEASE') {
			excludes 'com.springsource.org.apache.commons.logging',
			         'org.springframework.beans',
			         'org.springframework.core',
			         'org.springframework.context',
			         'com.springsource.org.apache.commons.lang',
			         'com.springsource.org.aopalliance'
		}
	}
}
