#!/bin/bash -e

: ${PROJECT_VERSION:?"PROJECT_VERSION is missing."}
: ${SCALA_VERSION:?"SCALA_VERSION is missing."}

sbt ++${SCALA_VERSION}! \
  'set version in ThisBuild := "'$PROJECT_VERSION'"' \
  "set publishMavenStyle in ThisBuild := false" \
  publish
# Keep publishing to the "generic" bintray repository
sbt ++${SCALA_VERSION}! \
  'set version in ThisBuild := "'$PROJECT_VERSION'"' \
  "set publishMavenStyle in ThisBuild := true" \
  publish
# Publish to the "maven" bintray repository as well
export BINTRAY_REPO=${BINTRAY_REPO:-scala-hedgehog-maven}
sbt ++${SCALA_VERSION}! \
  'set version in ThisBuild := "'$PROJECT_VERSION'"' \
  'set publishMavenStyle in ThisBuild := true' \
  publish
