#!/bin/bash -e

if [ -z "$2" ]
  then
    echo "Missing parameters. Please enter the [Scala version] and [project version]."
    echo "sbt-build.sh 2.13.16 0.1.0"
    exit 1
else
  SCALA_VERSION=$1
  PROJECT_VERSION=$2

  echo "      SCALA_VERSION=$SCALA_VERSION"
  echo "    PROJECT_VERSION=$PROJECT_VERSION"
  echo "CURRENT_BRANCH_NAME=$CURRENT_BRANCH_NAME"
  echo "         GITHUB_TAG=$GITHUB_TAG"

  if [[ $CURRENT_BRANCH_NAME == "master" || $GITHUB_TAG != "" ]]
  then
    sbt \
      ++${SCALA_VERSION}! \
      'set ThisBuild / version := "'$PROJECT_VERSION'"' \
      clean \
      test \
      packagedArtifacts
  else
    sbt \
      ++${SCALA_VERSION}! \
      'set ThisBuild / version := "'$PROJECT_VERSION'"' \
      clean \
      test \
      doc
  fi

fi
