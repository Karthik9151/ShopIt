#!/usr/bin/env bash
set -euo pipefail

# Local-only by default. Set BASE_URL only to an explicitly authorized
# isolated test API; this script does not create, modify, or delete data.
BASE_URL="${BASE_URL:-http://localhost:8080}"
BASE_URL="${BASE_URL%/}"

check_get() {
  local path="$1"
  local expected="$2"
  local actual
  actual="$(curl --silent --show-error --output /dev/null --write-out '%{http_code}' \
    --max-time 15 "$BASE_URL$path")"
  if [[ "$actual" != "$expected" ]]; then
    printf 'FAIL %s expected=%s actual=%s\n' "$path" "$expected" "$actual" >&2
    return 1
  fi
  printf 'PASS %s HTTP %s\n' "$path" "$actual"
}

check_get "/actuator/health" "200"
check_get "/api/categories" "200"
check_get "/api/products?page=0&size=1" "200"

printf 'Smoke checks passed for %s (read-only endpoints only).\n' "$BASE_URL"
