#!/bin/bash -e

: ${CURRENT_BRANCH_NAME:?"CURRENT_BRANCH_NAME is missing."}

if [ -z "$2" ]
  then
    echo "Missing parameters. Please enter the [Scala version] and [project version]."
    echo "sbt-build.sh 2.13.0 0.1.0"
    exit 1
else
  SCALA_VERSION=$1
  PROJECT_VERSION=$2

  if [[ $CURRENT_BRANCH_NAME == "master" || $GITHUB_TAG != "" ]]
  then
    sbt -J-Xmx2048m \
      ++${SCALA_VERSION}! \
      'set ThisBuild / version := "'$PROJECT_VERSION'"' \
      clean \
      test \
      packagedArtifacts
  else
    sbt -J-Xmx2048m \
      ++${SCALA_VERSION}! \
      'set ThisBuild / version := "'$PROJECT_VERSION'"' \
      clean \
      test
  fi

fi
