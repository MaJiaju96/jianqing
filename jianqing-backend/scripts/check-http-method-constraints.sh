#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$BASE_DIR/src/main/java"
FRONTEND_API_DIR="$BASE_DIR/../jianqing-admin-web/src/api"

errors=0

check_backend() {
  local pattern='@PutMapping|@DeleteMapping|RequestMethod\.PUT|RequestMethod\.DELETE'
  local matches
  matches=$(grep -R -n -E "$pattern" "$BACKEND_DIR" --include='*.java' || true)
  if [[ -n "$matches" ]]; then
    echo "[ERROR] 后端检测到不允许的 HTTP 方法（仅允许 GET/POST）："
    echo "$matches"
    errors=$((errors + 1))
  fi
}

check_frontend() {
  if [[ ! -d "$FRONTEND_API_DIR" ]]; then
    return
  fi
  local pattern='\.put\(|\.delete\(|method:\s*"PUT"|method:\s*"DELETE"|method:\s*'"'"'PUT'"'"'|method:\s*'"'"'DELETE'"'"''
  local matches
  matches=$(grep -R -n -E "$pattern" "$FRONTEND_API_DIR" --include='*.js' || true)
  if [[ -n "$matches" ]]; then
    echo "[ERROR] 前端 API 检测到不允许的 HTTP 方法（仅允许 GET/POST）："
    echo "$matches"
    errors=$((errors + 1))
  fi
}

check_backend
check_frontend

if [[ $errors -gt 0 ]]; then
  echo "[FAIL] HTTP 方法约束检查未通过，共发现 $errors 处违规。"
  exit 1
fi

echo "[PASS] HTTP 方法约束检查通过（仅 GET/POST）。"
