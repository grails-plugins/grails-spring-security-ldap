/* Copyright 2006-2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Creates test applications for functional tests.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

includeTargets << grailsScript('_GrailsBootstrap')

functionalTestPluginVersion = '1.2.7'
appName = null
grailsHome = null
dotGrails = null
grailsVersion = null
projectDir = null
pluginVersion = null
pluginZip = null
testprojectRoot = null
deleteAll = false

target(createLdapTestApps: 'Creates LDAP test apps') {

	def configFile = new File(basedir, 'testapps.config.groovy')
	if (!configFile.exists()) {
		error "$configFile.path not found"
	}

	new ConfigSlurper().parse(configFile.text).each { name, config ->
		printMessage "\nCreating app based on configuration $name: ${config.flatten()}\n"
		init name, config
		createApp()
		installPlugins()
		runQuickstart()
		createProjectFiles()
	}
}

private void init(String name, config) {

	pluginVersion = config.pluginVersion
	if (!pluginVersion) {
		error "pluginVersion wasn't specified for config '$name'"
	}

	pluginZip = new File(basedir, "grails-spring-security-ldap-${pluginVersion}.zip")
	if (!pluginZip.exists()) {
		error "plugin $pluginZip.absolutePath not found"
	}

	grailsHome = config.grailsHome
	if (!new File(grailsHome).exists()) {
		error "Grails home $grailsHome not found"
	}

	projectDir = config.projectDir
	appName = 'spring-security-ldap-test-' + name
	testprojectRoot = "$projectDir/$appName"
	grailsVersion = config.grailsVersion
	dotGrails = config.dotGrails + '/' + grailsVersion
}

private void createApp() {

	ant.mkdir dir: projectDir

	deleteDir testprojectRoot
	deleteDir "$dotGrails/projects/$appName"

	callGrails(grailsHome, projectDir, 'dev', 'create-app') {
		ant.arg value: appName
	}
}

private void installPlugins() {

	File buildConfig = new File(testprojectRoot, 'grails-app/conf/BuildConfig.groovy')
	String contents = buildConfig.text

	contents = contents.replace('grails.project.class.dir = "target/classes"', "grails.project.work.dir = 'target'")
	contents = contents.replace('grails.project.test.class.dir = "target/test-classes"', '')
	contents = contents.replace('grails.project.test.reports.dir = "target/test-reports"', '')

	if (grailsVersion.startsWith('1')) {
		contents = contents.replace('grailsCentral()', 'mavenRepo "http://repo.grails.org/grails/plugins"')
	}

	buildConfig.withWriter {
		it.writeLine contents
		// configure the functional tests to run in order
		it.writeLine 'grails.testing.patterns = ["Person1Functional", "Person2Functional", "Person3Functional"]'
	}

	callGrails(grailsHome, testprojectRoot, 'dev', 'install-plugin') {
		ant.arg value: "functional-test $functionalTestPluginVersion"
	}

	callGrails(grailsHome, testprojectRoot, 'dev', 'install-plugin') {
		ant.arg value: 'ldap-server'
	}

	callGrails(grailsHome, testprojectRoot, 'dev', 'install-plugin') {
		ant.arg value: 'spring-security-core'
	}

//	callGrails(grailsHome, testprojectRoot, 'dev', 'install-plugin') {
//		ant.arg value: pluginZip.absolutePath
//	}
	callGrails(grailsHome, testprojectRoot, 'dev', 'install-plugin') {
		ant.arg value: "spring-security-ldap $pluginVersion"
	}
}

private void runQuickstart() {
	callGrails(grailsHome, testprojectRoot, 'dev', 's2-quickstart') {
		ant.arg value: 'com.testldap'
		ant.arg value: 'User'
		ant.arg value: 'Role'
	}
	callGrails(grailsHome, testprojectRoot, 'dev', 's2-create-persistent-token') {
		ant.arg value: 'com.testldap.PersistentLogin'
	}
}

private void createProjectFiles() {
	String source = "$basedir/webtest/projectfiles"

	ant.copy file: "$source/classpath", tofile: "$testprojectRoot/.classpath", overwrite: true

	ant.copy file: "$source/SecureController.groovy",
	         todir: "$testprojectRoot/grails-app/controllers", overwrite: true

	ant.copy file: "$source/BootStrap.groovy",
	         todir: "$testprojectRoot/grails-app/conf", overwrite: true

	ant.copy(todir: "$testprojectRoot/test/functional", overwrite: true) {
		fileset dir: "$basedir/webtest", includes: "*Test*.groovy"
	}

	ant.mkdir dir: "$testprojectRoot/grails-app/ldap-servers/d1/data"
	ant.copy file: "$source/users.ldif", todir: "$testprojectRoot/grails-app/ldap-servers/d1/data", overwrite: true

	new File("$testprojectRoot/grails-app/conf/Config.groovy").withWriterAppend {
		it.writeLine """
grails.plugins.springsecurity.ldap.context.managerDn = 'uid=admin,ou=system'
grails.plugins.springsecurity.ldap.context.managerPassword = 'secret'
grails.plugins.springsecurity.ldap.context.server = 'ldap://localhost:10389'
grails.plugins.springsecurity.ldap.authorities.groupSearchFilter = 'uniquemember={0}'
grails.plugins.springsecurity.ldap.authorities.groupSearchBase = 'ou=groups,dc=d1,dc=example,dc=com'
grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
grails.plugins.springsecurity.ldap.search.base = 'dc=d1,dc=example,dc=com'
grails.plugins.springsecurity.ldap.search.filter = '(uid={0})'
grails.plugins.springsecurity.password.algorithm = 'SHA-256'
ldapServers {
   d1 {
      base = 'dc=d1,dc=example,dc=com'
      port = 10389
      indexed = ['objectClass', 'uid', 'mail', 'userPassword', 'description']
   }
}
grails.plugins.springsecurity.ldap.useRememberMe = true
grails.plugins.springsecurity.ldap.rememberMe.detailsManager.groupSearchBase = 'ou=groups,dc=d1,dc=example,dc=com'
grails.plugins.springsecurity.ldap.rememberMe.detailsManager.groupRoleAttributeName = 'cn'
grails.plugins.springsecurity.ldap.rememberMe.usernameMapper.userDnBase = 'dc=d1,dc=example,dc=com'
grails.plugins.springsecurity.ldap.rememberMe.usernameMapper.usernameAttribute = 'cn'
"""
	}
}

private void deleteDir(String path) {
	if (new File(path).exists() && !deleteAll) {
		String code = "confirm.delete.$path"
		ant.input message: "$path exists, ok to delete?", addproperty: code, validargs: 'y,n,a'
		def result = ant.antProject.properties[code]
		if ('a'.equalsIgnoreCase(result)) {
			deleteAll = true
		}
		else if (!'y'.equalsIgnoreCase(result)) {
			printMessage "\nNot deleting $path"
			exit 1
		}
	}

	ant.delete dir: path
}

private void error(String message) {
	errorMessage "\nERROR: $message"
	exit 1
}

private void callGrails(String grailsHome, String dir, String env, String action, extraArgs = null) {

	println "running: grails $env $action in dir $dir"

	File output = new File('call_grails_outputproperty')
	output.deleteOnExit()

	try {
		ant.exec(executable: "${grailsHome}/bin/grails", dir: dir, failonerror: 'true',
		         output: output.absolutePath) {
			ant.env key: 'GRAILS_HOME', value: grailsHome
			ant.arg value: env
			ant.arg value: action
			extraArgs?.call()
		}
	}
	catch (e) {
		errorMessage output.text
		throw e
	}
}

printMessage = { String message -> event('StatusUpdate', [message]) }
errorMessage = { String message -> event('StatusError', [message]) }

setDefaultTarget 'createLdapTestApps'
