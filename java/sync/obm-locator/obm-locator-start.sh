#!/bin/sh -e

exec $JAVA_HOME $JAVA_OPTS -jar $LOCATOR_JAR >> $LOCATOR_LOG_FILE 2>&1 &
echo $! >$LOCATOR_PID
