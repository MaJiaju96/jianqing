#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$BASE_DIR"

echo "[pre-commit] 运行服务层结构检查..."
bash "$BASE_DIR/scripts/check-service-structure.sh"

echo "[pre-commit] 运行 HTTP 方法约束检查..."
bash "$BASE_DIR/scripts/check-http-method-constraints.sh"

echo "[pre-commit] 运行编译检查..."
mvn -B -ntp -DskipTests compile

echo "[pre-commit] 运行 Checkstyle 检查..."
mvn -B -ntp checkstyle:check

echo "[pre-commit] 检查通过。"
