#!/usr/bin/env bash
set -e

# 1. Build debug JVM args for bootRun only
JVM_ARGS=""
if [ "$DEBUG_ENABLED" = "true" ]; then
  JVM_ARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=${DEBUG_SUSPEND},address=*:${DEBUG_PORT}"
  echo "🐞 JDWP debug enabled on port ${DEBUG_PORT} (suspend=${DEBUG_SUSPEND})"
fi

# 2. Start continuous compile in background
#    `-t classes` = re-run compileJava + processResources whenever source files change.
#    DevTools watches build/classes/** and triggers context restart (~1-2s).
./gradlew -t classes -x test --quiet > /tmp/continuous.log 2>&1 &
CONTINUOUS_PID=$!
echo "♻️  Continuous compile running (pid=$CONTINUOUS_PID)"

# 3. Graceful shutdown: kill background compile on SIGINT/SIGTERM
trap 'echo "Shutting down…"; kill $CONTINUOUS_PID 2>/dev/null; exit 0' INT TERM

# 4. Start app via bootRun — Spring DevTools is on the classpath (developmentOnly).
#    Pass debug agent via JAVA_OPTS, not JAVA_TOOL_OPTIONS (to avoid infecting Gradle JVM).
exec ./gradlew bootRun \
  --console=plain \
  -PjvmArgs="$JVM_ARGS"
