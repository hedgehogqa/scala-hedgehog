#!/bin/bash -e

: ${PROJECT_VERSION:?"PROJECT_VERSION is missing."}
: ${SCALA_VERSION:?"SCALA_VERSION is missing."}

echo "  SCALA_VERSION=$SCALA_VERSION"
echo "PROJECT_VERSION=$PROJECT_VERSION"
echo "   BINTRAY_REPO=$BINTRAY_REPO"

echo "sbt publish no maven style to $BINTRAY_REPO"

sbt ++${SCALA_VERSION}! \
  'set version in ThisBuild := "'$PROJECT_VERSION'"' \
  "set publishMavenStyle in ThisBuild := false" \
  publish

echo "sbt publish maven style to $BINTRAY_REPO"
# Keep publishing to the "generic" bintray repository
sbt ++${SCALA_VERSION}! \
  'set version in ThisBuild := "'$PROJECT_VERSION'"' \
  "set publishMavenStyle in ThisBuild := true" \
  publish

export BINTRAY_REPO=${BINTRAY_REPO:-scala-hedgehog-maven}
echo "sbt publish maven style to $BINTRAY_REPO"
# Publish to the "maven" bintray repository as well
sbt ++${SCALA_VERSION}! \
  'set version in ThisBuild := "'$PROJECT_VERSION'"' \
  'set publishMavenStyle in ThisBuild := true' \
  publish
