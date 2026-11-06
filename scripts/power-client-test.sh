#!/bin/bash
# Plays the owner's power test (docs/OWNER_POWER_TEST.md) with a real Minecraft client against the dev server.
# Development only: it starts ./gradlew runServerVerify (port 25566, run-verify/), gives the test player "PowerTester" a fresh
# power state (200 Arcane Shards, no school), starts the client game test (src/gametest) and stops the server again.
#
#   scripts/power-client-test.sh                       all phases
#   scripts/power-client-test.sh permission,setup      only these phases (see PowerClientTest for the names)
#   POWERTEST_ROOTS=menu,civ scripts/power-client-test.sh setup,menus   the menu crawl, only from these command roots
#
# It needs a desktop (the client opens a window), Java 25, python and about seven minutes. Screenshots go to
# build/run/clientGameTest/screenshots. The result lines start with CLIENTTEST in the log it prints at the end.
set -u
cd "$(dirname "$0")/.."
PHASES="${1:-permission,setup,technique,grants,domain,combat,death,regression,catalogs}"
WORK="$(mktemp -d)"
CMDS="$WORK/commands.txt"
SERVER_LOG="$WORK/server.log"
CLIENT_LOG="$WORK/client.log"
: > "$CMDS"

if (echo > /dev/tcp/127.0.0.1/25566) 2>/dev/null; then
  echo "Something already listens on port 25566. Stop the dev server first." >&2
  exit 2
fi

PY=""
for candidate in python3 python py; do
  if "$candidate" -c "import sys" > /dev/null 2>&1; then PY="$candidate"; break; fi
done
if [ -z "$PY" ]; then
  echo "python is needed to seed the test player" >&2
  exit 3
fi

# a dev folder that has never run gets the settings the test needs (a fresh server asks for a whitelist and stops a slow first tick)
mkdir -p run-verify
if [ ! -f run-verify/server.properties ]; then
  printf 'white-list=false
enforce-whitelist=false
max-tick-time=-1
online-mode=false
server-port=25566
' > run-verify/server.properties
fi

# a fresh power state for the test player (the offline UUID of the name), written while no server is running
mkdir -p run-verify/world
"$PY" - <<'PYEOF'
import hashlib, json, uuid
h = bytearray(hashlib.md5(b"OfflinePlayer:PowerTester").digest())
h[6] = (h[6] & 0x0f) | 0x30
h[8] = (h[8] & 0x3f) | 0x80
u = str(uuid.UUID(bytes=bytes(h)))
data = {"schema_version": 1, "data": {"players": {u: {"shards": 200, "shards_earned_total": 200}}, "unique_holders": {}, "quarantine": {}}}
open("run-verify/world/power.json", "w", encoding="utf8").write(json.dumps(data))
print("seeded power state for", u)
PYEOF
if [ $? -ne 0 ]; then
  echo "could not seed the test player" >&2
  exit 3
fi

stop_server() {
  # A server that has not finished loading must never be told to save/stop partway through loading data.
  if [ "$ready" = 1 ]; then
    echo stop >> "$CMDS"
    sleep 15
  fi
  kill "$TAIL_PID" 2>/dev/null
}

(tail -n 0 -f "$CMDS" | ./gradlew runServerVerify --console=plain > "$SERVER_LOG" 2>&1) &
TAIL_PID=$!
ready=0
for _ in $(seq 1 150); do
  sleep 2
  if grep -q "PoliticalServer data loaded" "$SERVER_LOG" 2>/dev/null; then ready=1; break; fi
done
if [ "$ready" != 1 ]; then
  echo "The dev server did not start; see $SERVER_LOG" >&2
  stop_server
  exit 1
fi
echo "op PowerTester" >> "$CMDS"
sleep 3

JAVA_CMDS="$CMDS"
if command -v cygpath >/dev/null 2>&1; then JAVA_CMDS="$(cygpath -m "$CMDS")"; fi
echo "Dev server loaded; starting client phases: $PHASES (logs: $WORK)"
export JAVA_TOOL_OPTIONS="-Dpowertest.commands=$JAVA_CMDS -Dpowertest.phases=$PHASES${POWERTEST_ROOTS:+ -Dpowertest.roots=$POWERTEST_ROOTS}${POWERTEST_CLICKS:+ -Dpowertest.crawl.maxClicks=$POWERTEST_CLICKS}"
./gradlew runClientGameTest --console=plain > "$CLIENT_LOG" 2>&1
STATUS=$?
unset JAVA_TOOL_OPTIONS
stop_server

if [[ ",$PHASES," == *",regression,"* || "$PHASES" == "" ]]; then
  if [ -f run-verify/world/ledger.json ]; then echo "CLIENTTEST ok     ledger.json was written when the server stopped"; else echo "CLIENTTEST FAILED ledger.json was not written when the server stopped"; STATUS=1; fi
fi
grep "CLIENTTEST" "$CLIENT_LOG" | sed 's/^.*CLIENTTEST/CLIENTTEST/' | grep -v "CLIENTTEST screen"
echo "--- errors the dev server logged during the run (the Windows system info warnings at startup are not the mod's) ---"
grep -E "/(ERROR|WARN)\]|Exception" "$SERVER_LOG" | grep -vE "oshi|Win32|Hkey|Perflib|system info|Process (Loads|Virtual|Resident)|OFFLINE|offline|authenticate|hackers|online-mode|Can't keep up|Tick service" | head -40
echo "client test exit status: $STATUS (0 means every check passed); logs: $WORK"
exit "$STATUS"
