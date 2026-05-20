# Set Up And Run Builds

Use this when checking Java, resources, or final jar output.

## Working Environment

This project expects Java 25. Source `env.sh` before Gradle:

```bash
source ./env.sh
```

That sets:

- `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64`
- `GRADLE_USER_HOME=$PWD/.gradle`
- `PATH=$JAVA_HOME/bin:$PATH`

Without this, Gradle/Groovy may fail against the shell Java with:

```text
Unsupported class file major version 70
```

## Useful Commands

```bash
source ./env.sh && ./gradlew compileJava
```

Checks Java compile only.

```bash
source ./env.sh && ./gradlew processResources
```

Checks resource processing and generated mod metadata, including `src/main/templates/META-INF/neoforge.mods.toml` expansion.

```bash
source ./env.sh && ./gradlew build
```

Runs Java compile, resource processing, checks, and jar assembly. Use this as final verification.

## Local Sandbox Symptom

Inside this coding environment, Gradle may need elevated/local networking permission. Symptom:

```text
Could not determine a usable wildcard IP for this machine.
```

Same command works when run with required escalation. This is an environment issue, not mod code failure.

## New Template OP Setup

New template worlds start with an empty `run/ops.json`. If you need commands in the dev server, copy the player entry from `run/usercache.json` into `run/ops.json`:

```json
[
  {
    "uuid": "player-uuid-from-usercache",
    "name": "PlayerName",
    "level": 4,
    "bypassesPlayerLimit": false
  }
]
```

The local dev username has been `Dev` in this workspace. Confirm with `run/usercache.json` or `run/logs/latest.log` before assuming the UUID.

## What Not To Do

- Do not run Gradle without `source ./env.sh` unless you have already verified Java 25 is active.
- Do not treat `compileJava` as resource validation.
- Do not treat `processResources` as gameplay validation.
