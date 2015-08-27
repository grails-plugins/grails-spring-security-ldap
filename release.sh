rm -rf build/release
mkdir -p build/release
cd build/release
git clone git@github.com:grails-plugins/grails-spring-security-ldap.git
cd grails-spring-security-ldap
grails clean
grails compile

gradle bintrayUpload --stacktrace
