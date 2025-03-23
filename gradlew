#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

DIRNAME=`dirname "$0"`
GRADLE_HOME=`cd "$DIRNAME" && pwd`

exec "$GRADLE_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
