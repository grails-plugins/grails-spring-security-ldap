grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for the gh-pages branch
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile('org.springframework.security:spring-security-ldap:3.0.7.RELEASE') {
			excludes 'spring-security-core', 'spring-ldap-core', 'ldapsdk', 'apacheds-core',
			         'apacheds-server-jndi', 'slf4j-log4j12', 'log4j', 'junit',
			         'mockito-core', 'jmock-junit4'
		}
		runtime('org.springframework.ldap:spring-ldap-core:1.3.1.RELEASE') {
			excludes 'commons-logging', 'commons-lang', 'commons-pool', 'ldapbp',
			         'spring-core', 'spring-beans', 'spring-tx', 'spring-context',
			         'spring-jdbc', 'spring-orm', 'junit', 'easymock', 'gsbase',
			         'commons-logging', 'spring-core', 'spring-beans', 'junit', 'log4j'
		}
	}

	plugins {
		compile ':spring-security-core:1.2.7.3'

		compile(":hibernate:$grailsVersion") {
			export = false
		}

		build(':release:2.0.2', ':rest-client-builder:1.0.2') {
			export = false
		}
	}
}
