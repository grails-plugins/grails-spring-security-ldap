rm -rf target/release
mkdir target/release
cd target/release
git clone git@github.com:grails-plugins/grails-spring-security-ldap.git
cd grails-spring-security-ldap
grails clean
grails compile

#grails publish-plugin --snapshot --stacktrace
grails publish-plugin --stacktrace
