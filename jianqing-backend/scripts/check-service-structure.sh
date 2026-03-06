#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_DIR="$BASE_DIR/src/main/java/com/jianqing"

errors=0

check_service_package() {
  local package_dir="$1"
  local package_name="$2"

  if [[ ! -d "$package_dir" ]]; then
    return
  fi

  while IFS= read -r -d '' file; do
    local filename
    filename="$(basename "$file")"

    if [[ "$filename" == *Impl.java ]]; then
      echo "[ERROR] $package_name/service 下不允许出现 Impl 文件：$file"
      errors=$((errors + 1))
      continue
    fi

    if ! grep -q "public interface" "$file"; then
      echo "[ERROR] $package_name/service 下必须是接口：$file"
      errors=$((errors + 1))
      continue
    fi

    local interface_name
    interface_name="${filename%.java}"
    local expected_impl="$package_dir/impl/${interface_name}Impl.java"

    if [[ ! -f "$expected_impl" ]]; then
      echo "[ERROR] 缺少实现类：$expected_impl"
      errors=$((errors + 1))
      continue
    fi

    if ! grep -qE "class ${interface_name}Impl( extends [^{]+)? implements ${interface_name}" "$expected_impl"; then
      echo "[ERROR] 实现类未按规范实现接口：$expected_impl"
      errors=$((errors + 1))
    fi
  done < <(find "$package_dir" -maxdepth 1 -type f -name "*Service.java" -print0)

  if [[ -d "$package_dir/impl" ]]; then
    while IFS= read -r -d '' impl_file; do
      local impl_name
      impl_name="$(basename "$impl_file" .java)"
      local interface_name="${impl_name%Impl}"
      local interface_file="$package_dir/${interface_name}.java"
      if [[ ! -f "$interface_file" ]]; then
        echo "[ERROR] 实现类无对应接口：$impl_file"
        errors=$((errors + 1))
      fi
    done < <(find "$package_dir/impl" -maxdepth 1 -type f -name "*ServiceImpl.java" -print0)
  fi
}

check_service_package "$JAVA_DIR/module/auth/service" "module.auth"
check_service_package "$JAVA_DIR/module/audit/service" "module.audit"
check_service_package "$JAVA_DIR/module/system/service" "module.system"
check_service_package "$JAVA_DIR/framework/security" "framework.security"
check_service_package "$JAVA_DIR/framework/cache" "framework.cache"

if [[ $errors -gt 0 ]]; then
  echo "[FAIL] 服务层结构检查未通过，共发现 $errors 个问题。"
  exit 1
fi

echo "[PASS] 服务层结构检查通过。"
