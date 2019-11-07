#!/bin/bash -e

if [ "${GITHUB_TAG:-}" != "" ]; then

  : ${BINTRAY_USER:?"BINTRAY_USER is missing."}
  : ${BINTRAY_PASS:?"BINTRAY_PASS is missing."}

  PROJECT_VERSION="${GITHUB_TAG#v}"
  BINTRAY_SUBJECT=${BINTRAY_SUBJECT:-hedgehogqa}
  BINTRAY_REPO=${BINTRAY_REPO:-scala-hedgehog-maven}

  BINTRAY_PACKAGES="hedgehog-core hedgehog-runner hedgehog-sbt hedgehog-minitest"

  echo "PROJECT_VERSION: $PROJECT_VERSION"
  echo "BINTRAY_SUBJECT: $BINTRAY_SUBJECT"
  echo "   BINTRAY_REPO: $BINTRAY_REPO"

  for bintray_package in $BINTRAY_PACKAGES
  do
    echo "bintray_package: $bintray_package"
    echo "Sync to Maven Central..."
    curl \
      --user "${BINTRAY_USER}:${BINTRAY_PASS}" \
      -X POST \
      "https://api.bintray.com/maven_central_sync/$BINTRAY_SUBJECT/$BINTRAY_REPO/$bintray_package/versions/$PROJECT_VERSION"
  done
else
  echo "It's not a tag release so 'Sync to Maven Central' has been skipped."
fi
