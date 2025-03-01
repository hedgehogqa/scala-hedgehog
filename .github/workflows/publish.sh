#!/bin/bash -e

export SOURCE_DATE_EPOCH=$(date +%s)
echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"

if [[ -z "${GITHUB_TAG}" ]]
then
  echo "It is not a tag build."
  echo "sbt publish to Sonatype snapshots"
  sbt \
    clean \
    +test \
    +packagedArtifacts \
    ci-release

else
  echo "It is a tag build. GITHUB_TAG=${GITHUB_TAG}"
  echo "sbt publish $GITHUB_TAG to Maven Central"
  sbt \
    clean \
    +test \
    +packagedArtifacts \
    ci-release
fi

