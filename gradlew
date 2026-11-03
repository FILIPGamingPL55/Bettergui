#!/bin/sh
APP_HOME=$(dirname "$(realpath "$0")")
exec java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
