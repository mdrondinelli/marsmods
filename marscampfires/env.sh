#!/usr/bin/env bash

# Source this file before using Gradle:
#   source ./env.sh

export JAVA_HOME="/usr/lib/jvm/java-25-openjdk-amd64"
export GRADLE_USER_HOME="$PWD/.gradle"
export PATH="$JAVA_HOME/bin:$PATH"

echo "JAVA_HOME=$JAVA_HOME"
java -version
