#!/usr/bin/env bash

set -e

EXIT_STATUS=0

echo "TRAVIS_TAG          : $TRAVIS_TAG"
echo "TRAVIS_BRANCH       : $TRAVIS_BRANCH"
echo "TRAVIS_PULL_REQUEST : $TRAVIS_PULL_REQUEST"



echo "*******************************"
echo "grails-spring-security-ldap"
echo "*******************************"

./gradlew clean check install --stacktrace --console=plain || EXIT_STATUS=$?

if [ $EXIT_STATUS -ne 0 ]; then
  echo "grails-spring-security-ldap:check failed => exit $EXIT_STATUS"
  exit $EXIT_STATUS
fi

PROJECTS="retrieve-group-rules retrieve-db-roles custom_user_details_context_mapper"

cd functional-test-app

./gradlew clean check --stacktrace --console=plain || EXIT_STATUS=$?

if [ $EXIT_STATUS -ne 0 ]; then
  echo "functional-test-app:check failed => exit $EXIT_STATUS"
  exit $EXIT_STATUS
fi

cd ..

cd misc-functional-test-app

for project in $PROJECTS; do

    cd $project 

    ./gradlew clean check --stacktrace --console=plain || EXIT_STATUS=$?

    if [ $EXIT_STATUS -ne 0 ]; then
        echo "$project check failed => exit $EXIT_STATUS"
        exit $EXIT_STATUS
    fi

    cd ..

done

cd ..

if [[ -n $TRAVIS_TAG ]] || [[ $TRAVIS_BRANCH == 'master' && $TRAVIS_PULL_REQUEST == 'false' ]]; then

  echo "Publishing archives for branch $TRAVIS_BRANCH for tag $TRAVIS_TAG"

  if [[ -n $TRAVIS_TAG ]]; then

    echo "Pushing build to Bintray for tag $TRAVIS_TAG"

    ./gradlew bintrayUpload || EXIT_STATUS=$?
    
    ./publish-docs.sh

  fi
fi

exit $EXIT_STATUS
