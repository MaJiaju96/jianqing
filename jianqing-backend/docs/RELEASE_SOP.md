# 简擎后端发布执行 SOP（v0.1）

## 0. 前置说明

- 当前工作目录若未初始化 git（`git status` 报“不是 git 仓库”），请先完成仓库初始化与远端绑定后再执行发布动作。
- 建议在发布前确保本地与远端分支一致，避免发布内容偏差。

## 1. 发布前校验

按以下顺序执行：

```bash
mvn -DskipTests compile
mvn test
mvn checkstyle:check
```

核对文档：

- `docs/RELEASE_CHECKLIST.md`
- `docs/SCOPE_v0.1_v0.2.md`
- `docs/RELEASE_NOTES_v0.1.md`

## 2. 版本标记（tag）

```bash
git tag -a v0.1.0 -m "release: v0.1.0"
git push origin v0.1.0
```

## 3. 发布说明

若使用 GitHub CLI：

```bash
gh release create v0.1.0 \
  --title "简擎后端 v0.1.0" \
  --notes-file docs/RELEASE_NOTES_v0.1.md
```

## 4. 发布后核验

- 检查 CI 是否全绿
- 检查 Release 页面内容是否完整
- 检查 README 中版本相关链接是否可访问

## 5. 回滚策略（最小）

- 若仅 Release 文案有误：直接编辑 Release 描述
- 若 tag 错误且未被依赖：删除错误 tag 并重建

```bash
git tag -d v0.1.0
git push origin :refs/tags/v0.1.0
```

- 若代码内容错误：修复后发布 `v0.1.1`
