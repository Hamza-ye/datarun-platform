#!/bin/sh
set -eu

repo_root=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)
image_tag=${IMAGE_TAG:-datarun-server:image-verification}
image_version=${IMAGE_VERSION:-verification}
image_revision=${IMAGE_REVISION:-$(git -C "$repo_root" rev-parse HEAD 2>/dev/null || printf 'unknown')}
image_created=${IMAGE_CREATED:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}
db_host=${IMAGE_SMOKE_DB_HOST:-127.0.0.1}
db_port=${IMAGE_SMOKE_DB_PORT:-15432}
db_name=${IMAGE_SMOKE_DB_NAME:-datarun_test}
db_user=${IMAGE_SMOKE_DB_USER:-datarun}
db_password=${IMAGE_SMOKE_DB_PASSWORD:-datarun}
server_port=${IMAGE_SMOKE_SERVER_PORT:-18080}
smoke_container="datarun-server-image-smoke-$$"
inspect_container=
artifact_dir=$(mktemp -d)

cleanup() {
    docker rm -f "$smoke_container" >/dev/null 2>&1 || true
    if [ -n "$inspect_container" ]; then
        docker rm -f "$inspect_container" >/dev/null 2>&1 || true
    fi
    rm -rf "$artifact_dir"
}
trap cleanup EXIT INT TERM

docker build \
    --file "$repo_root/server/Dockerfile" \
    --tag "$image_tag" \
    --build-arg "IMAGE_VERSION=$image_version" \
    --build-arg "IMAGE_REVISION=$image_revision" \
    --build-arg "IMAGE_CREATED=$image_created" \
    "$repo_root"

runtime_user=$(docker image inspect --format '{{.Config.User}}' "$image_tag")
case "$runtime_user" in
    ""|0|root|0:0|root:root)
        echo "Release image must declare a non-root runtime user" >&2
        exit 1
        ;;
esac

verify_label() {
    label_name=$1
    expected=$2
    actual=$(docker image inspect \
        --format "{{ index .Config.Labels \"$label_name\" }}" "$image_tag")
    if [ "$actual" != "$expected" ]; then
        echo "Unexpected $label_name label: expected '$expected', got '$actual'" >&2
        exit 1
    fi
}

verify_label org.opencontainers.image.version "$image_version"
verify_label org.opencontainers.image.revision "$image_revision"
verify_label org.opencontainers.image.created "$image_created"

inspect_container=$(docker create "$image_tag")
docker cp "$inspect_container:/app/app.jar" "$artifact_dir/app.jar"
unzip -Z1 "$artifact_dir/app.jar" > "$artifact_dir/jar-entries.txt"

verify_jar_entry() {
    entry=$1
    if ! grep -Fqx "$entry" "$artifact_dir/jar-entries.txt"; then
        echo "Missing required image resource: $entry" >&2
        exit 1
    fi
}

verify_jar_entry BOOT-INF/classes/pattern-definition.schema.json
for source in "$repo_root"/contracts/patterns/*.json; do
    verify_jar_entry "BOOT-INF/classes/patterns/$(basename "$source")"
done
for source in "$repo_root"/contracts/shapes/*.schema.json; do
    verify_jar_entry "BOOT-INF/classes/shapes/$(basename "$source")"
done

docker run --detach \
    --name "$smoke_container" \
    --network host \
    --env "DB_HOST=$db_host" \
    --env "DB_PORT=$db_port" \
    --env "DB_NAME=$db_name" \
    --env "DB_USER=$db_user" \
    --env "DB_PASSWORD=$db_password" \
    --env "SERVER_PORT=$server_port" \
    "$image_tag" >/dev/null

attempt=0
while [ "$attempt" -lt 90 ]; do
    if [ "$(docker inspect --format '{{.State.Running}}' "$smoke_container")" != "true" ]; then
        echo "Release image stopped before startup completed" >&2
        docker logs "$smoke_container" >&2
        exit 1
    fi

    if docker logs "$smoke_container" 2>&1 \
        | grep -Fq "Started DatarunServerApplication"; then
        http_status=$(curl --silent --output /dev/null --write-out '%{http_code}' \
            "http://127.0.0.1:$server_port/api/auth/me" || true)
        if [ "$http_status" = "401" ]; then
            echo "Verified image resources, non-root runtime, release labels, and PostgreSQL startup smoke"
            exit 0
        fi
    fi

    attempt=$((attempt + 1))
    sleep 1
done

echo "Release image did not become ready for the startup smoke" >&2
docker logs "$smoke_container" >&2
exit 1
