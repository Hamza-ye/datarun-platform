#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/compose.yaml"
CONFIG_ONLY=false

usage() {
  cat <<'EOF'
Usage: preflight.sh [--config-only]

Validates the provider-neutral reference deployment inputs without printing
configuration or secret values. The default mode also verifies the local
image identity and container readability of mounted files.

--config-only  Render and validate Compose without requiring the image locally.
EOF
}

fail() {
  printf 'preflight: ERROR: %s\n' "$*" >&2
  exit 1
}

notice() {
  printf 'preflight: %s\n' "$*"
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "required command is unavailable: $1"
}

require_env() {
  local name="$1"
  [[ -n "${!name:-}" ]] || fail "required environment variable is unset: $name"
}

require_absolute_directory() {
  local name="$1"
  local path="${!name}"
  [[ "$path" == /* ]] || fail "$name must be an absolute path"
  [[ "$path" != *:* && "$path" != *,* && "$path" != *$'\n'* ]] \
    || fail "$name contains a character unsupported by the image-readability check"
  [[ -d "$path" ]] || fail "$name does not name an existing directory"
}

require_regular_file() {
  local path="$1"
  local label="$2"
  [[ -f "$path" ]] || fail "$label must resolve to a regular file"
  [[ -s "$path" ]] || fail "$label must not be empty"
}

require_scalar_file() {
  local path="$1"
  local label="$2"
  require_regular_file "$path" "$label"
  local lines
  lines="$(awk 'END { print NR }' "$path")"
  [[ "$lines" -le 1 ]] || fail "$label must contain one scalar value"
}

read_scalar() {
  local path="$1"
  local label="$2"
  require_scalar_file "$path" "$label"
  REPLY="$(<"$path")"
  [[ -n "$REPLY" ]] || fail "$label must not be blank"
}

require_port() {
  local name="$1"
  local value="${!name}"
  [[ "$value" =~ ^[0-9]+$ ]] || fail "$name must be an integer TCP port"
  (( value >= 1024 && value <= 65535 )) \
    || fail "$name must be between 1024 and 65535"
}

require_positive_integer() {
  local name="$1"
  local value="${!name}"
  [[ "$value" =~ ^[1-9][0-9]*$ ]] || fail "$name must be a positive integer"
}

while (($#)); do
  case "$1" in
    --config-only)
      CONFIG_ONLY=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      usage >&2
      fail "unknown argument: $1"
      ;;
  esac
  shift
done

require_command docker
require_command awk

require_env DATARUN_IMAGE
require_env DATARUN_IMAGE_SOURCE
require_env DATARUN_IMAGE_REVISION
require_env DATARUN_RUNTIME_CONFIG_DIR
require_env DATARUN_PROVISIONING_DIR
require_env DATARUN_TRUST_DIR

[[ "$DATARUN_IMAGE" =~ ^[^[:space:]@]+@sha256:[0-9a-f]{64}$ ]] \
  || fail "DATARUN_IMAGE must be a repository reference pinned by sha256 digest"
[[ "$DATARUN_IMAGE_SOURCE" =~ ^https://[^/?#[:space:]]+[^[:space:]]*$ ]] \
  || fail "DATARUN_IMAGE_SOURCE must be an absolute HTTPS source URL"
[[ "$DATARUN_IMAGE_REVISION" =~ ^[0-9a-fA-F]{40}$ ]] \
  || fail "DATARUN_IMAGE_REVISION must be a full 40-character source commit"

export DATARUN_APP_HOST_PORT="${DATARUN_APP_HOST_PORT:-18080}"
export DATARUN_MANAGEMENT_HOST_PORT="${DATARUN_MANAGEMENT_HOST_PORT:-18081}"
export DATARUN_CPU_LIMIT="${DATARUN_CPU_LIMIT:-1.0}"
export DATARUN_MEMORY_LIMIT="${DATARUN_MEMORY_LIMIT:-1024m}"
export DATARUN_PIDS_LIMIT="${DATARUN_PIDS_LIMIT:-256}"
export DATARUN_STOP_GRACE_PERIOD="${DATARUN_STOP_GRACE_PERIOD:-45s}"

require_port DATARUN_APP_HOST_PORT
require_port DATARUN_MANAGEMENT_HOST_PORT
[[ "$DATARUN_APP_HOST_PORT" != "$DATARUN_MANAGEMENT_HOST_PORT" ]] \
  || fail "application and management host ports must differ"
[[ "$DATARUN_CPU_LIMIT" =~ ^([1-9][0-9]*)(\.[0-9]+)?$ \
  || "$DATARUN_CPU_LIMIT" =~ ^0\.[0-9]*[1-9][0-9]*$ ]] \
  || fail "DATARUN_CPU_LIMIT must be a positive CPU count"
[[ "$DATARUN_MEMORY_LIMIT" =~ ^[1-9][0-9]*(b|k|kb|m|mb|g|gb)$ ]] \
  || fail "DATARUN_MEMORY_LIMIT must be a positive Compose memory limit"
require_positive_integer DATARUN_PIDS_LIMIT
[[ "$DATARUN_STOP_GRACE_PERIOD" =~ ^[1-9][0-9]*(s|m)$ ]] \
  || fail "DATARUN_STOP_GRACE_PERIOD must be a positive duration in seconds or minutes"
if [[ "$DATARUN_STOP_GRACE_PERIOD" == *m ]]; then
  stop_grace_seconds="$(( ${DATARUN_STOP_GRACE_PERIOD%m} * 60 ))"
else
  stop_grace_seconds="${DATARUN_STOP_GRACE_PERIOD%s}"
fi
(( stop_grace_seconds > 30 )) \
  || fail "DATARUN_STOP_GRACE_PERIOD must exceed the 30-second application shutdown phase"

require_absolute_directory DATARUN_RUNTIME_CONFIG_DIR
require_absolute_directory DATARUN_PROVISIONING_DIR
require_absolute_directory DATARUN_TRUST_DIR

runtime_keys=(
  datarun.auth.mode
  datarun.auth.oidc.issuer
  datarun.auth.oidc.audience
  datarun.auth.oidc.jwks-uri
  datarun.auth.principal-bindings.applied-by
  datarun.auth.principal-bindings.manifest
  spring.datasource.url
  spring.datasource.username
  spring.datasource.password
)

for key in "${runtime_keys[@]}"; do
  require_scalar_file "$DATARUN_RUNTIME_CONFIG_DIR/$key" "runtime config file $key"
done

read_scalar "$DATARUN_RUNTIME_CONFIG_DIR/datarun.auth.mode" "datarun.auth.mode"
[[ "$REPLY" == "oidc-jwks" ]] || fail "datarun.auth.mode must be oidc-jwks"

for key in datarun.auth.oidc.issuer datarun.auth.oidc.jwks-uri; do
  read_scalar "$DATARUN_RUNTIME_CONFIG_DIR/$key" "$key"
  [[ "$REPLY" =~ ^https://[^/?#[:space:]]+[^[:space:]]*$ ]] \
    || fail "$key must be an absolute HTTPS URI"
done

read_scalar \
  "$DATARUN_RUNTIME_CONFIG_DIR/datarun.auth.principal-bindings.applied-by" \
  "datarun.auth.principal-bindings.applied-by"
[[ "$REPLY" != "system:auth-principal-binding-provisioner" ]] \
  || fail "principal-binding applied-by must identify the actual operator"

read_scalar \
  "$DATARUN_RUNTIME_CONFIG_DIR/datarun.auth.principal-bindings.manifest" \
  "datarun.auth.principal-bindings.manifest"
[[ "$REPLY" == "/run/datarun/provisioning/principal-bindings.json" ]] \
  || fail "principal-binding manifest must use the fixed read-only container path"

read_scalar "$DATARUN_RUNTIME_CONFIG_DIR/spring.datasource.url" "spring.datasource.url"
database_url="$REPLY"
[[ "$database_url" == jdbc:postgresql://* ]] \
  || fail "spring.datasource.url must use PostgreSQL JDBC"
[[ "$database_url" =~ [\?\&]sslmode=verify-full([\&\#]|$) ]] \
  || fail "spring.datasource.url must require sslmode=verify-full"
[[ "$database_url" =~ [\?\&]sslrootcert=/run/datarun/trust/postgresql-root\.crt([\&\#]|$) ]] \
  || fail "spring.datasource.url must use the mounted PostgreSQL root certificate"
[[ ! "$database_url" =~ [\?\&](password|user)= ]] \
  || fail "database credentials must use separate config-tree files"

database_authority="${database_url#jdbc:postgresql://}"
database_authority="${database_authority%%/*}"
[[ "$database_authority" != *@* ]] \
  || fail "spring.datasource.url must not contain user information"
if [[ "$database_authority" == \[* ]]; then
  database_host="${database_authority#\[}"
  database_host="${database_host%%\]*}"
else
  database_host="${database_authority%%:*}"
fi
case "${database_host,,}" in
  localhost|localhost.|127.*|0.0.0.0|::1|db|database|postgres|postgresql)
    fail "spring.datasource.url must name an external PostgreSQL host"
    ;;
esac

read_scalar "$DATARUN_RUNTIME_CONFIG_DIR/spring.datasource.username" \
  "spring.datasource.username"
[[ "$REPLY" != "datarun" ]] || fail "database username must not use the development default"
read_scalar "$DATARUN_RUNTIME_CONFIG_DIR/spring.datasource.password" \
  "spring.datasource.password"
[[ "$REPLY" != "datarun" ]] || fail "database password must not use the development default"

require_regular_file \
  "$DATARUN_PROVISIONING_DIR/principal-bindings.json" \
  "principal-bindings.json"
require_regular_file \
  "$DATARUN_TRUST_DIR/postgresql-root.crt" \
  "postgresql-root.crt"

docker compose -f "$COMPOSE_FILE" config --quiet
notice "Compose configuration is valid and publishes application and management ports on host loopback only"

if "$CONFIG_ONLY"; then
  notice "config-only validation passed; image labels and container file readability were not checked"
  exit 0
fi

docker image inspect "$DATARUN_IMAGE" >/dev/null 2>&1 \
  || fail "pinned image is not present locally; obtain that digest and rerun preflight"

image_source="$(
  docker image inspect \
    --format '{{ index .Config.Labels "org.opencontainers.image.source" }}' \
    "$DATARUN_IMAGE"
)"
image_revision="$(
  docker image inspect \
    --format '{{ index .Config.Labels "org.opencontainers.image.revision" }}' \
    "$DATARUN_IMAGE"
)"
image_user="$(docker image inspect --format '{{ .Config.User }}' "$DATARUN_IMAGE")"

[[ "$image_source" == "$DATARUN_IMAGE_SOURCE" ]] \
  || fail "image OCI source label does not match DATARUN_IMAGE_SOURCE"
[[ "$image_revision" == "$DATARUN_IMAGE_REVISION" ]] \
  || fail "image OCI revision label does not match DATARUN_IMAGE_REVISION"
[[ "$image_user" == "10001:10001" ]] \
  || fail "image runtime user must be 10001:10001"

docker run --rm \
  --network none \
  --read-only \
  --user 10001:10001 \
  --cap-drop ALL \
  --security-opt no-new-privileges \
  --mount "type=bind,src=$DATARUN_RUNTIME_CONFIG_DIR,dst=/run/datarun/config,readonly" \
  --mount "type=bind,src=$DATARUN_PROVISIONING_DIR,dst=/run/datarun/provisioning,readonly" \
  --mount "type=bind,src=$DATARUN_TRUST_DIR,dst=/run/datarun/trust,readonly" \
  --entrypoint /bin/sh \
  "$DATARUN_IMAGE" \
  -ec '
    command -v wget >/dev/null
    for file in \
      /run/datarun/config/datarun.auth.mode \
      /run/datarun/config/datarun.auth.oidc.issuer \
      /run/datarun/config/datarun.auth.oidc.audience \
      /run/datarun/config/datarun.auth.oidc.jwks-uri \
      /run/datarun/config/datarun.auth.principal-bindings.applied-by \
      /run/datarun/config/datarun.auth.principal-bindings.manifest \
      /run/datarun/config/spring.datasource.url \
      /run/datarun/config/spring.datasource.username \
      /run/datarun/config/spring.datasource.password \
      /run/datarun/provisioning/principal-bindings.json \
      /run/datarun/trust/postgresql-root.crt
    do
      test -r "$file"
    done
  '

notice "full preflight passed for the pinned image and mounted inputs"
