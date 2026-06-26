#!/usr/bin/env bash
set -euo pipefail

KEYCLOAK_CONTAINER="${KEYCLOAK_CONTAINER:-datarun-local-keycloak}"
KEYCLOAK_ADMIN_USERNAME="${KEYCLOAK_ADMIN_USERNAME:-kc-admin}"
KEYCLOAK_ADMIN_PASSWORD_FILE="${KEYCLOAK_ADMIN_PASSWORD_FILE:-}"
REALM="${REALM:-datarun-local}"
ISSUER="${ISSUER:-https://keycloak.lab:28443/realms/${REALM}}"
WEB_CLIENT_ID="${WEB_CLIENT_ID:-datarun-web-admin}"
WEB_REDIRECT_URI="${WEB_REDIRECT_URI:-https://datarun-app.lab:28443/web-admin/oidc/callback}"
MOBILE_CLIENT_ID="${MOBILE_CLIENT_ID:-datarun-mobile}"
MOBILE_REDIRECT_URI="${MOBILE_REDIRECT_URI:-dev.datarun.mobile://oauth2redirect}"
DATARUN_AUDIENCE="${DATARUN_AUDIENCE:-datarun-server}"
PILOT_USERNAME="${PILOT_USERNAME:-hamza-pilot}"
PILOT_EMAIL="${PILOT_EMAIL:-hamza@example.invalid}"
PILOT_FIRST_NAME="${PILOT_FIRST_NAME:-Hamza}"
PILOT_LAST_NAME="${PILOT_LAST_NAME:-Pilot}"
PILOT_PASSWORD_FILE="${PILOT_PASSWORD_FILE:-}"
WEB_CLIENT_SECRET_FILE="${WEB_CLIENT_SECRET_FILE:-}"
PRINCIPAL_BINDING_OUT="${PRINCIPAL_BINDING_OUT:-./principal-bindings.generated.json}"
DATARUN_BINDING_ACTOR_ID="${DATARUN_BINDING_ACTOR_ID:-}"
OPERATION_ID="${OPERATION_ID:-local-keycloak-bind-${PILOT_USERNAME}}"
BINDING_SOURCE="${BINDING_SOURCE:-local-keycloak-reference}"

KCADM=(docker exec "${KEYCLOAK_CONTAINER}" /opt/keycloak/bin/kcadm.sh)

require_file() {
  local name="$1"
  local path="$2"
  if [[ -z "${path}" || ! -f "${path}" ]]; then
    printf '%s must point to an existing file\n' "${name}" >&2
    exit 1
  fi
}

require_value() {
  local name="$1"
  local value="$2"
  if [[ -z "${value}" ]]; then
    printf '%s must be set\n' "${name}" >&2
    exit 1
  fi
}

secret_from_file() {
  local path="$1"
  tr -d '\r\n' < "${path}"
}

csv_second_line() {
  sed -n '2p' | tr -d '\r'
}

client_uuid() {
  "${KCADM[@]}" get clients -r "${REALM}" -q "clientId=$1" \
    --fields id --format csv --noquotes 2>/dev/null | csv_second_line
}

user_uuid() {
  "${KCADM[@]}" get users -r "${REALM}" -q "username=${PILOT_USERNAME}" \
    --fields id --format csv --noquotes 2>/dev/null | csv_second_line
}

mapper_uuid() {
  local client_id="$1"
  "${KCADM[@]}" get "clients/${client_id}/protocol-mappers/models" \
    -r "${REALM}" --fields id,name --format csv --noquotes 2>/dev/null \
    | awk -F, '$2 == "datarun-server-audience" { print $1; exit }'
}

require_file KEYCLOAK_ADMIN_PASSWORD_FILE "${KEYCLOAK_ADMIN_PASSWORD_FILE}"
require_file PILOT_PASSWORD_FILE "${PILOT_PASSWORD_FILE}"
require_value WEB_CLIENT_SECRET_FILE "${WEB_CLIENT_SECRET_FILE}"
require_value DATARUN_BINDING_ACTOR_ID "${DATARUN_BINDING_ACTOR_ID}"

admin_password="$(secret_from_file "${KEYCLOAK_ADMIN_PASSWORD_FILE}")"
pilot_password="$(secret_from_file "${PILOT_PASSWORD_FILE}")"
web_origin="${WEB_REDIRECT_URI%/web-admin/oidc/callback}"

"${KCADM[@]}" config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user "${KEYCLOAK_ADMIN_USERNAME}" \
  --password "${admin_password}" >/dev/null

if ! "${KCADM[@]}" get "realms/${REALM}" >/dev/null 2>&1; then
  "${KCADM[@]}" create realms \
    -s "realm=${REALM}" \
    -s enabled=true \
    -s registrationAllowed=false \
    -s resetPasswordAllowed=false
fi

web_id="$(client_uuid "${WEB_CLIENT_ID}")"
if [[ -z "${web_id}" ]]; then
  "${KCADM[@]}" create clients -r "${REALM}" \
    -s "clientId=${WEB_CLIENT_ID}" \
    -s enabled=true \
    -s protocol=openid-connect \
    -s publicClient=false \
    -s standardFlowEnabled=true \
    -s implicitFlowEnabled=false \
    -s directAccessGrantsEnabled=false \
    -s serviceAccountsEnabled=false \
    -s "redirectUris=[\"${WEB_REDIRECT_URI}\"]" \
    -s "webOrigins=[\"${web_origin}\"]"
  web_id="$(client_uuid "${WEB_CLIENT_ID}")"
else
  "${KCADM[@]}" update "clients/${web_id}" -r "${REALM}" \
    -s enabled=true \
    -s protocol=openid-connect \
    -s publicClient=false \
    -s standardFlowEnabled=true \
    -s implicitFlowEnabled=false \
    -s directAccessGrantsEnabled=false \
    -s serviceAccountsEnabled=false \
    -s "redirectUris=[\"${WEB_REDIRECT_URI}\"]" \
    -s "webOrigins=[\"${web_origin}\"]"
fi

mobile_id="$(client_uuid "${MOBILE_CLIENT_ID}")"
if [[ -z "${mobile_id}" ]]; then
  "${KCADM[@]}" create clients -r "${REALM}" \
    -s "clientId=${MOBILE_CLIENT_ID}" \
    -s enabled=true \
    -s protocol=openid-connect \
    -s publicClient=true \
    -s standardFlowEnabled=true \
    -s implicitFlowEnabled=false \
    -s directAccessGrantsEnabled=false \
    -s serviceAccountsEnabled=false \
    -s "redirectUris=[\"${MOBILE_REDIRECT_URI}\"]" \
    -s 'attributes."pkce.code.challenge.method"=S256'
  mobile_id="$(client_uuid "${MOBILE_CLIENT_ID}")"
else
  "${KCADM[@]}" update "clients/${mobile_id}" -r "${REALM}" \
    -s enabled=true \
    -s protocol=openid-connect \
    -s publicClient=true \
    -s standardFlowEnabled=true \
    -s implicitFlowEnabled=false \
    -s directAccessGrantsEnabled=false \
    -s serviceAccountsEnabled=false \
    -s "redirectUris=[\"${MOBILE_REDIRECT_URI}\"]" \
    -s 'attributes."pkce.code.challenge.method"=S256'
fi

for client_id in "${web_id}" "${mobile_id}"; do
  mapper_id="$(mapper_uuid "${client_id}")"
  if [[ -z "${mapper_id}" ]]; then
    "${KCADM[@]}" create "clients/${client_id}/protocol-mappers/models" \
      -r "${REALM}" \
      -s name=datarun-server-audience \
      -s protocol=openid-connect \
      -s protocolMapper=oidc-audience-mapper \
      -s "config.\"included.custom.audience\"=${DATARUN_AUDIENCE}" \
      -s 'config."id.token.claim"=true' \
      -s 'config."access.token.claim"=true'
  else
    "${KCADM[@]}" update \
      "clients/${client_id}/protocol-mappers/models/${mapper_id}" \
      -r "${REALM}" \
      -s name=datarun-server-audience \
      -s protocol=openid-connect \
      -s protocolMapper=oidc-audience-mapper \
      -s "config.\"included.custom.audience\"=${DATARUN_AUDIENCE}" \
      -s 'config."id.token.claim"=true' \
      -s 'config."access.token.claim"=true'
  fi
done

secret_json="$("${KCADM[@]}" get "clients/${web_id}/client-secret" -r "${REALM}")"
printf '%s\n' "${secret_json}" \
  | sed -n 's/.*"value"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' \
  > "${WEB_CLIENT_SECRET_FILE}"
chmod 0600 "${WEB_CLIENT_SECRET_FILE}"

pilot_id="$(user_uuid)"
if [[ -z "${pilot_id}" ]]; then
  "${KCADM[@]}" create users -r "${REALM}" \
    -s "username=${PILOT_USERNAME}" \
    -s enabled=true \
    -s emailVerified=true \
    -s "email=${PILOT_EMAIL}" \
    -s "firstName=${PILOT_FIRST_NAME}" \
    -s "lastName=${PILOT_LAST_NAME}"
else
  "${KCADM[@]}" update "users/${pilot_id}" -r "${REALM}" \
    -s enabled=true \
    -s emailVerified=true \
    -s "email=${PILOT_EMAIL}" \
    -s "firstName=${PILOT_FIRST_NAME}" \
    -s "lastName=${PILOT_LAST_NAME}"
fi

"${KCADM[@]}" set-password -r "${REALM}" \
  --username "${PILOT_USERNAME}" \
  --new-password "${pilot_password}" \
  --temporary=false

pilot_subject="$(user_uuid)"
require_value pilot_subject "${pilot_subject}"

cat > "${PRINCIPAL_BINDING_OUT}" <<JSON
{
  "manifest_version": "deployment-bindings/v1",
  "source": "${BINDING_SOURCE}",
  "operations": [
    {
      "operation_id": "${OPERATION_ID}",
      "issuer": "${ISSUER}",
      "subject": "${pilot_subject}",
      "actor_id": "${DATARUN_BINDING_ACTOR_ID}",
      "state": "active",
      "reason": "Local Keycloak proof fixture binding generated from the live Keycloak user subject"
    }
  ]
}
JSON
chmod 0600 "${PRINCIPAL_BINDING_OUT}"

printf 'realm=%s\n' "${REALM}"
printf 'issuer=%s\n' "${ISSUER}"
printf 'web_client=%s\n' "${WEB_CLIENT_ID}"
printf 'mobile_client=%s\n' "${MOBILE_CLIENT_ID}"
printf 'pilot_username=%s\n' "${PILOT_USERNAME}"
printf 'pilot_subject=%s\n' "${pilot_subject}"
printf 'web_client_secret_file=%s\n' "${WEB_CLIENT_SECRET_FILE}"
printf 'principal_binding_manifest=%s\n' "${PRINCIPAL_BINDING_OUT}"
