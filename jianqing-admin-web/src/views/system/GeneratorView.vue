<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <div class="jq-toolbar-shell generator-toolbar-shell">
      <div class="generator-toolbar-topline">
        <div class="generator-toolbar-context">
          <div class="generator-toolbar-context__title">代码生成配置</div>
          <div class="generator-toolbar-context__desc">先确认命名与权限前缀，再生成预览与下载模板。</div>
        </div>
        <div class="generator-toolbar-side">
          <div class="generator-actions-panel">
            <div class="generator-actions-panel__primary">
              <el-button type="primary" :loading="previewLoading" @click="handlePreview">生成预览</el-button>
              <el-button type="primary" plain :disabled="downloadLoading || !previewFiles.length" :loading="downloadLoading" @click="handleDownload">下载 ZIP</el-button>
            </div>
            <div class="generator-actions-panel__secondary">
              <el-button @click="handleReset">重置</el-button>
              <el-dropdown trigger="click">
                <el-button>
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleSaveHistory" :disabled="!previewFiles.length">
                      <el-icon><FolderChecked /></el-icon>保存配置
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleWriteToProject" :disabled="!previewFiles.length">
                      <el-icon><FolderAdd /></el-icon>写入项目
                    </el-dropdown-item>
                    <el-dropdown-item @click="openQuickDeleteDialog" :disabled="!markerHistoryList.length">
                      <el-icon><FolderChecked /></el-icon>快速删除
                    </el-dropdown-item>
                    <el-dropdown-item @click="openWriteRecordDialog">
                      <el-icon><Clock /></el-icon>写入记录
                    </el-dropdown-item>
                    <el-dropdown-item v-for="item in historyList" :key="item.id" @click="handleHistoryCommand(item)">
                      <div class="generator-history-item">
                        <div class="generator-history-item__title">
                          <el-icon><Clock /></el-icon>
                          <span>{{ item.tableName }} → {{ item.className }}</span>
                        </div>
                        <div class="generator-history-item__meta">
                          <span>{{ item.moduleName }}/{{ item.businessName }}</span>
                          <span class="generator-history-item__dot">·</span>
                          <span>{{ item.permPrefix }}</span>
                        </div>
                        <div class="generator-history-item__time">{{ formatHistoryTime(item.createdAt) }}</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="historyList.length" disabled>--- 点击可恢复整套配置 ---</el-dropdown-item>
                    <el-dropdown-item v-if="!historyList.length" disabled>暂无历史记录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
      <div class="generator-config-grid">
        <div class="generator-field">
          <span class="generator-field__label">数据表</span>
          <el-select v-model="form.tableName" placeholder="选择数据表" class="generator-config-grid__table" filterable @change="handleTableChange">
            <el-option v-for="item in tables" :key="item.tableName" :label="tableLabel(item)" :value="item.tableName" />
          </el-select>
        </div>
        <div class="generator-field">
          <span class="generator-field__label">模块名</span>
          <el-input v-model="form.moduleName" placeholder="如 sys" />
        </div>
        <div class="generator-field">
          <span class="generator-field__label">业务名</span>
          <el-input v-model="form.businessName" placeholder="如 sys_configs" />
        </div>
        <div class="generator-field">
          <span class="generator-field__label">实体名</span>
          <el-input v-model="form.className" placeholder="如 SysConfig" />
        </div>
        <div class="generator-field generator-field--wide">
          <span class="generator-field__label">权限前缀</span>
          <el-input v-model="form.permPrefix" class="generator-config-grid__perm" placeholder="如 system:customer" />
        </div>
      </div>
    </div>

    <div class="generator-layout">
      <div class="generator-panel jq-glass-card">
        <div class="panel-header">
          <div class="panel-header__topline">
            <div class="panel-title">字段元数据</div>
            <el-tooltip content="查看注意事项" placement="top">
              <el-button class="generator-alert-btn generator-alert-btn--icon" circle aria-label="查看注意事项" @click="noticeDrawerVisible = true">
                <span class="generator-alert-btn__icon-mark">!</span>
              </el-button>
            </el-tooltip>
          </div>
          <div class="panel-subtitle">字段结构决定生成结果质量，生成前先快速确认主键、状态字段和时间字段是否齐全。</div>
        </div>
        <div class="generator-panel-meta">
          <div class="generator-panel-meta__stat">
            <span class="generator-panel-meta__label">当前表</span>
            <span class="generator-panel-meta__value">{{ selectedTableLabel }}</span>
          </div>
          <div class="generator-panel-meta__stat">
            <span class="generator-panel-meta__label">字段数</span>
            <span class="generator-panel-meta__value">{{ columns.length ? `${columns.length} 个字段` : '待加载' }}</span>
          </div>
        </div>
        <div class="jq-table-panel generator-table-panel">
          <el-table :data="columns" stripe height="100%" :empty-text="columnEmptyText">
            <el-table-column prop="columnName" label="字段名" min-width="160" />
            <el-table-column prop="columnComment" label="注释" min-width="180" />
            <el-table-column prop="javaType" label="Java 类型" width="126" />
            <el-table-column prop="columnType" label="数据库类型" min-width="160" />
            <el-table-column label="主键" width="80">
              <template #default="scope">
                {{ scope.row.primaryKey ? '是' : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="可空" width="80">
              <template #default="scope">
                {{ scope.row.nullable ? '是' : '否' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="generator-panel jq-glass-card">
        <div class="panel-header">
          <div class="panel-title">代码预览</div>
          <div class="panel-subtitle">先核对产物结构，再执行下载或写入，避免把错误模板带进项目。</div>
        </div>
        <div class="generator-guide generator-guide--compact">
          <div class="generator-guide__summary">
            <div class="generator-guide__item">
              <span class="generator-guide__label">当前表</span>
              <span class="generator-guide__value">{{ selectedTableLabel }}</span>
            </div>
            <div class="generator-guide__item">
              <span class="generator-guide__label">预览文件</span>
              <span class="generator-guide__value">{{ previewSummary.totalText }}</span>
            </div>
            <div class="generator-guide__item generator-guide__item--wide">
              <span class="generator-guide__label">产物分布</span>
              <span class="generator-guide__value">{{ previewSummary.categoryText }}</span>
            </div>
          </div>
          <div class="generator-guide__inline-tip">先看命名与路由，再滚动检查 controller / service / 列表页模板；代码区支持横向和纵向滚动查看。</div>
        </div>
        <el-empty v-if="!previewFiles.length" description="先选择数据表并点击生成预览" />
        <el-tabs v-else v-model="activeFilePath" class="generator-tabs">
          <el-tab-pane v-for="file in previewFiles" :key="file.filePath" :label="fileTabLabel(file.filePath)" :name="file.filePath">
            <div class="generator-file-row">
              <div class="file-path">{{ file.filePath }}</div>
              <div class="generator-file-row__actions">
                <el-button size="small" text @click="handleCopyFilePath(file.filePath)">
                  <el-icon><DocumentCopy /></el-icon>
                  复制路径
                </el-button>
                <el-button size="small" text @click="handleCopyFileContent(file)">
                  <el-icon><DocumentCopy /></el-icon>
                  复制内容
                </el-button>
                <el-switch
                  v-model="wrapPreviewLines"
                  inline-prompt
                  :active-text="'自动换行'"
                  :inactive-text="'保留长行'"
                  class="generator-wrap-switch"
                />
              </div>
            </div>
            <div class="generator-file-guide">
              <div class="generator-file-guide__badge">{{ activePreviewFileMeta.category }}</div>
              <div class="generator-file-guide__content">
                <div class="generator-file-guide__title">{{ activePreviewFileMeta.purpose }}</div>
                <div class="generator-file-guide__text">{{ activePreviewFileMeta.reviewTip }}</div>
              </div>
            </div>
            <div class="generator-code-viewer" :class="{ 'generator-code-viewer--wrapped': wrapPreviewLines }">
              <pre class="generator-code-viewer__content" :class="{ 'generator-code-viewer__content--wrapped': wrapPreviewLines }"><code>{{ file.content }}</code></pre>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-drawer v-model="noticeDrawerVisible" title="字段 / 模板注意事项" size="420px" append-to-body>
      <div class="generator-drawer-note">
        <div class="generator-drawer-note__lead">
          这里集中展示生成前自检建议，避免占用字段表格的主观察空间。
        </div>
        <div class="generator-notice-board__list">
          <div v-for="notice in generatorNotices" :key="notice.title" class="generator-notice-board__item" :class="`generator-notice-board__item--${notice.level}`">
            <div class="generator-notice-board__item-title">{{ notice.title }}</div>
            <div class="generator-notice-board__item-text">{{ notice.text }}</div>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog
      v-model="conflictDialogVisible"
      title="覆盖确认"
      width="640px"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @close="handleCancelConflictOverwrite"
    >
      <div class="generator-conflict-dialog">
        <div class="generator-conflict-dialog__lead">检测到 {{ conflictFiles.length }} 个目标文件已存在，确认后将覆盖以下文件：</div>
        <div class="generator-conflict-dialog__toolbar">
          <el-input
            v-model="conflictKeyword"
            placeholder="搜索冲突路径（支持文件名/目录关键字）"
            clearable
          />
          <el-radio-group v-model="conflictQuickFilter" size="small" class="generator-conflict-dialog__filters">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="java-main">高风险 Java</el-radio-button>
            <el-radio-button label="frontend">前端</el-radio-button>
            <el-radio-button label="sql">SQL</el-radio-button>
          </el-radio-group>
          <el-switch
            v-model="conflictDisplayMode"
            active-value="name"
            inactive-value="path"
            inline-prompt
            active-text="仅文件名"
            inactive-text="完整路径"
          />
          <div v-if="conflictDisplayMode === 'path' && groupedConflictFiles.length" class="generator-conflict-dialog__group-actions">
            <el-button size="small" text @click="expandAllConflictGroups">全部展开</el-button>
            <el-button size="small" text @click="collapseAllConflictGroups">全部折叠</el-button>
          </div>
          <span class="generator-conflict-dialog__count">{{ filteredConflictCountText }}</span>
        </div>
        <div class="generator-conflict-summary">
          <span class="generator-conflict-summary__label">目录统计：</span>
          <span v-for="item in conflictDirectorySummary" :key="item.directory" class="generator-conflict-summary__chip">
            {{ item.directory }} {{ item.count }}
          </span>
          <span v-if="!conflictDirectorySummary.length" class="generator-conflict-summary__empty">暂无统计</span>
        </div>
        <div class="generator-conflict-dialog__list-wrap">
          <el-empty v-if="!filteredConflictFiles.length" description="未匹配到冲突文件" />
          <ul v-else-if="conflictDisplayMode === 'name'" class="generator-conflict-dialog__list">
            <li v-for="fileName in uniqueConflictBaseNames" :key="fileName">{{ fileName }}</li>
          </ul>
          <div v-else class="generator-conflict-group-list">
            <div v-for="group in groupedConflictFiles" :key="group.directory" class="generator-conflict-group">
              <button class="generator-conflict-group__title" type="button" @click="toggleConflictGroup(group.directory)">
                <span>{{ isConflictGroupCollapsed(group.directory) ? '▶' : '▼' }}</span>
                <span>{{ group.directory }}（{{ group.files.length }}）</span>
              </button>
              <ul v-if="!isConflictGroupCollapsed(group.directory)" class="generator-conflict-dialog__list">
                <li v-for="filePath in group.files" :key="filePath">{{ filePath }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleCopyConflictFiles">复制清单</el-button>
        <el-button @click="handleExportConflictFiles">导出 TXT</el-button>
        <el-button @click="handlePreviewConflictMarkdown">预览 MD</el-button>
        <el-button @click="handleExportConflictMarkdown">导出 MD</el-button>
        <el-button @click="handleCancelConflictOverwrite">取消</el-button>
        <el-button type="primary" @click="handleConfirmConflictOverwrite">覆盖写入</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="conflictMarkdownPreviewVisible"
      title="冲突清单 Markdown 预览"
      width="760px"
      append-to-body
    >
      <div class="generator-markdown-preview">
        <div class="generator-markdown-preview__toolbar">
          <span>当前内容可直接复制或导出。</span>
          <el-button size="small" @click="handleCopyMarkdownPreview">复制 Markdown</el-button>
        </div>
        <div class="generator-markdown-preview__content-wrap">
          <pre class="generator-markdown-preview__content"><code>{{ conflictMarkdownPreviewContent }}</code></pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="conflictMarkdownPreviewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExportConflictMarkdown">导出 MD</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="quickDeleteDialogVisible"
      title="快速删除（按标记）"
      width="520px"
      append-to-body
    >
      <div class="generator-quick-delete">
        <div class="generator-quick-delete__hint">选择历史标记或输入标记，删除该次生成写入的文件。</div>
        <el-select v-model="quickDeleteMarkerId" placeholder="选择历史标记" clearable filterable>
          <el-option v-for="item in markerHistoryList" :key="item.markerId" :label="markerOptionLabel(item)" :value="item.markerId" />
        </el-select>
        <el-input v-model="quickDeleteMarkerId" placeholder="也可直接输入标记，如 gen-20260312123000-aaaaaa" />
      </div>
      <template #footer>
        <el-button @click="quickDeleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleteLoading" @click="handleQuickDeleteByMarker">删除标记文件</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="writeRecordDialogVisible"
      title="写入记录"
      width="860px"
      append-to-body
    >
      <div class="generator-write-record">
        <div class="generator-write-record__filters">
          <el-input v-model="writeRecordQuery.tableName" placeholder="按表名筛选（如 jq_sys_user）" clearable />
          <el-date-picker
            v-model="writeRecordQuery.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            clearable
          />
          <el-button :loading="writeRecordLoading" @click="loadWriteRecords">查询</el-button>
        </div>
        <el-table :data="writeRecordList" height="420" stripe>
          <el-table-column prop="markerId" label="标记" min-width="240" />
          <el-table-column prop="tableName" label="表名" min-width="160" />
          <el-table-column prop="moduleName" label="模块" width="100" />
          <el-table-column prop="businessName" label="业务" min-width="120" />
          <el-table-column prop="username" label="操作人" width="120" />
          <el-table-column label="写入时间" width="170">
            <template #default="scope">{{ formatHistoryTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button type="danger" link @click="handleDeleteRecord(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowDown, FolderChecked, FolderAdd, Clock, DocumentCopy } from '@element-plus/icons-vue';
import {
  fetchGeneratorColumns,
  fetchGeneratorTables,
  previewGenerator,
  downloadGeneratorZip,
  writeGenerator,
  fetchGeneratorWriteConflicts,
  deleteGeneratorByMarker,
  fetchGeneratorWriteRecords
} from '../../api/generator';
import { useActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { ignoreHandledError } from '../../utils/errors';
import { showSuccessMessage } from '../../utils/feedback';

const tables = ref([]);
const columns = ref([]);
const previewFiles = ref([]);
const activeFilePath = ref('');
const selectedTable = ref(null);
const historyList = ref([]);
const noticeDrawerVisible = ref(false);
const wrapPreviewLines = ref(false);
const conflictDialogVisible = ref(false);
const conflictFiles = ref([]);
const conflictKeyword = ref('');
const conflictQuickFilter = ref('all');
const conflictDisplayMode = ref('path');
const conflictCollapsedGroups = ref({});
const conflictMarkdownPreviewVisible = ref(false);
const conflictMarkdownPreviewContent = ref('');
const quickDeleteDialogVisible = ref(false);
const quickDeleteMarkerId = ref('');
const markerHistoryList = ref([]);
const writeRecordDialogVisible = ref(false);
const writeRecordList = ref([]);
const writeRecordQuery = ref({
  tableName: '',
  timeRange: []
});
let conflictResolver = null;

const HISTORY_KEY = 'jq_generator_history';
const MARKER_HISTORY_KEY = 'jq_generator_markers';
const MAX_HISTORY = 10;
const TABLE_NAME_PATTERN = /^[a-zA-Z0-9_]+$/;
const MODULE_NAME_PATTERN = /^[a-z][a-z0-9_]*$/;
const BUSINESS_NAME_PATTERN = /^[a-z][a-z0-9_-]*$/;
const CLASS_NAME_PATTERN = /^[A-Z][A-Za-z0-9]*$/;
const PERM_PREFIX_PATTERN = /^[a-z][a-z0-9:-]*$/;

function loadHistory() {
  try {
    const stored = localStorage.getItem(HISTORY_KEY);
    historyList.value = stored ? JSON.parse(stored) : [];
  } catch {
    historyList.value = [];
  }
}

function loadMarkerHistory() {
  return fetchGeneratorWriteRecords({ limit: MAX_HISTORY })
    .then((records) => {
      markerHistoryList.value = records;
    })
    .catch(() => {
      try {
        const stored = localStorage.getItem(MARKER_HISTORY_KEY);
        markerHistoryList.value = stored ? JSON.parse(stored) : [];
      } catch {
        markerHistoryList.value = [];
      }
    });
}

function saveMarkerHistory(markerId) {
  if (!markerId) {
    return;
  }
  loadMarkerHistory().catch(ignoreHandledError);
}

function saveHistory(formData) {
  const history = {
    id: Date.now(),
    tableName: formData.tableName,
    moduleName: formData.moduleName,
    businessName: formData.businessName,
    className: formData.className,
    permPrefix: formData.permPrefix,
    createdAt: new Date().toISOString()
  };
  historyList.value = [history, ...historyList.value.filter((h) => h.tableName !== history.tableName || h.className !== history.className)].slice(0, MAX_HISTORY);
  localStorage.setItem(HISTORY_KEY, JSON.stringify(historyList.value));
}

function saveHistoryIfReady(formData) {
  const payload = sanitizeGeneratorForm(formData);
  if (getGeneratorFormError(payload)) {
    return;
  }
  saveHistory(payload);
}

function applyHistory(historyItem) {
  form.value = {
    tableName: historyItem.tableName,
    moduleName: historyItem.moduleName,
    businessName: historyItem.businessName,
    className: historyItem.className,
    permPrefix: historyItem.permPrefix
  };
  handleTableChange(historyItem.tableName, { skipAutoFill: true });
}

const form = ref(createDefaultForm());

const tablesFeedback = useTableFeedback();
const previewAction = useActionLoading();
const downloadAction = useActionLoading();
const writeAction = useActionLoading();
const deleteAction = useActionLoading();
const writeRecordAction = useActionLoading();

const tablesLoading = tablesFeedback.loading;
const previewLoading = previewAction.loading;
const downloadLoading = downloadAction.loading;
const writeLoading = writeAction.loading;
const deleteLoading = deleteAction.loading;
const writeRecordLoading = writeRecordAction.loading;
const columnEmptyText = computed(() => (form.value.tableName ? '当前表暂无字段' : '请先选择数据表'));
const selectedTableLabel = computed(() => {
  if (!selectedTable.value) {
    return '未选择';
  }
  return tableLabel(selectedTable.value);
});
const previewSummary = computed(() => {
  if (!previewFiles.value.length) {
    return {
      totalText: '尚未生成',
      categoryText: '生成后会按后端 / 前端 / SQL 统计'
    };
  }
  const counters = previewFiles.value.reduce((result, file) => {
    if (isBackendPreviewFile(file.filePath)) {
      result.backend += 1;
      return result;
    }
    if (isFrontendPreviewFile(file.filePath)) {
      result.frontend += 1;
      return result;
    }
    if (isSqlPreviewFile(file.filePath)) {
      result.sql += 1;
      return result;
    }
    result.other += 1;
    return result;
  }, {
    backend: 0,
    frontend: 0,
    sql: 0,
    other: 0
  });
  const parts = [
    counters.backend ? `后端 ${counters.backend}` : '',
    counters.frontend ? `前端 ${counters.frontend}` : '',
    counters.sql ? `SQL ${counters.sql}` : '',
    counters.other ? `其他 ${counters.other}` : ''
  ].filter(Boolean);

  return {
    totalText: `共 ${previewFiles.value.length} 份`,
    categoryText: parts.join(' · ')
  };
});
const activePreviewFile = computed(() => previewFiles.value.find((item) => item.filePath === activeFilePath.value) || null);
const activePreviewFileMeta = computed(() => buildPreviewFileMeta(activePreviewFile.value?.filePath || ''));
const quickFilteredConflictFiles = computed(() => {
  if (conflictQuickFilter.value === 'java-main') {
    return conflictFiles.value.filter((filePath) => filePath.includes('src/main/java/'));
  }
  if (conflictQuickFilter.value === 'frontend') {
    return conflictFiles.value.filter((filePath) => filePath.includes('jianqing-admin-web/src/'));
  }
  if (conflictQuickFilter.value === 'sql') {
    return conflictFiles.value.filter((filePath) => filePath.startsWith('sql/') || filePath.includes('/sql/'));
  }
  return conflictFiles.value;
});
const filteredConflictFiles = computed(() => {
  const keyword = conflictKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return quickFilteredConflictFiles.value;
  }
  return quickFilteredConflictFiles.value.filter((filePath) => filePath.toLowerCase().includes(keyword));
});
const uniqueConflictBaseNames = computed(() => {
  const seen = new Set();
  const result = [];
  for (const filePath of filteredConflictFiles.value) {
    const baseName = baseNameOf(filePath);
    if (!seen.has(baseName)) {
      seen.add(baseName);
      result.push(baseName);
    }
  }
  return result;
});
const filteredConflictCountText = computed(() => {
  if (conflictDisplayMode.value === 'name') {
    return `文件名 ${uniqueConflictBaseNames.value.length} / 路径 ${filteredConflictFiles.value.length} / 总数 ${conflictFiles.value.length}`;
  }
  return `筛选后 ${filteredConflictFiles.value.length} / ${conflictFiles.value.length}`;
});
const conflictDirectorySummary = computed(() => {
  const counters = new Map();
  for (const filePath of filteredConflictFiles.value) {
    const directory = topDirectoryOf(filePath);
    counters.set(directory, (counters.get(directory) || 0) + 1);
  }
  return Array.from(counters.entries())
    .map(([directory, count]) => ({ directory, count }))
    .sort((left, right) => right.count - left.count)
    .slice(0, 6);
});
const groupedConflictFiles = computed(() => {
  const groups = new Map();
  for (const filePath of filteredConflictFiles.value) {
    const directory = parentDirectoryOf(filePath);
    if (!groups.has(directory)) {
      groups.set(directory, []);
    }
    groups.get(directory).push(filePath);
  }
  return Array.from(groups.entries())
    .map(([directory, files]) => {
      const sortedFiles = [...files].sort((left, right) => compareConflictPath(left, right));
      const maxRisk = sortedFiles.reduce((risk, filePath) => Math.max(risk, conflictRiskLevel(filePath)), 0);
      return {
        directory,
        files: sortedFiles,
        maxRisk
      };
    })
    .sort((left, right) => {
      if (left.maxRisk !== right.maxRisk) {
        return right.maxRisk - left.maxRisk;
      }
      if (left.files.length !== right.files.length) {
        return right.files.length - left.files.length;
      }
      return left.directory.localeCompare(right.directory);
    });
});
const generatorNotices = computed(() => {
  if (!form.value.tableName) {
      return [
        {
          title: '先选择数据表',
          text: '选表后会自动带出字段分析结果，方便先确认结构，再决定是否生成。',
          level: 'info'
        },
        {
          title: '首轮重点看命名',
          text: '模块名、业务名、实体名、权限前缀决定生成目录与接口语义，首轮不要跳过。',
          level: 'info'
        },
        {
          title: '生成器更适合骨架场景',
          text: '复杂业务校验、字典映射和联表查询仍建议在生成后按实际模块补齐。',
          level: 'info'
        }
      ];
  }

  const hasPrimaryKey = columns.value.some((item) => item.primaryKey);
  const primaryKeys = columns.value.filter((item) => item.primaryKey);
  const statusField = columns.value.find((item) => /status|state/i.test(item.columnName));
  const logicalDeleteField = columns.value.find((item) => /is_deleted|deleted/i.test(item.columnName));
  const timeFields = columns.value.filter((item) => /create_time|update_time|created_at|updated_at/i.test(item.columnName));
  const longTextFields = columns.value.filter((item) => /remark|description|content|note/i.test(item.columnName));
  const uncommentedFields = columns.value.filter((item) => !item.columnComment || !item.columnComment.trim());
  const nullableRequiredFields = columns.value.filter((item) => !item.nullable && !isAuditLikeColumn(item.columnName));
  const autoIncrementPrimaryKey = primaryKeys.find((item) => item.autoIncrement);
  const notices = [];

  notices.push({
    title: hasPrimaryKey ? '主键结构可生成' : '缺少主键会阻断生成',
    text: hasPrimaryKey
      ? `已识别主键 ${primaryKeys.map((item) => item.columnName).join('、')}${autoIncrementPrimaryKey ? '，其中包含自增主键' : ''}，可继续生成 CRUD 骨架。`
      : '当前生成器要求单表必须存在主键，否则 preview/download 无法输出可执行 CRUD 模板。',
    level: hasPrimaryKey ? 'info' : 'danger'
  });

  notices.push({
    title: uncommentedFields.length ? '存在未注释字段' : '字段注释较完整',
    text: uncommentedFields.length
      ? `未写注释的字段有 ${uncommentedFields.slice(0, 4).map((item) => item.columnName).join('、')}${uncommentedFields.length > 4 ? ' 等' : ''}，生成列表标题、表单标签和 DTO 语义会偏弱，建议先补齐。`
      : '字段注释完整度较好，生成后的列表列名、表单标签和 DTO 文案会更自然。',
    level: uncommentedFields.length ? 'warning' : 'info'
  });

  notices.push({
    title: statusField ? '检测到状态字段' : '未检测到状态字段',
    text: statusField ? `已识别 ${statusField.columnName}，生成列表页会优先按状态类字段输出更贴近后台场景的控件。` : '如果业务需要启停/发布状态，建议补标准状态字段，后续列表筛选与表单控件会更顺手。',
    level: statusField ? 'info' : 'advice'
  });

  notices.push({
    title: logicalDeleteField ? '已识别逻辑删除字段' : '暂未识别逻辑删除字段',
    text: logicalDeleteField ? `已识别 ${logicalDeleteField.columnName}，模板会更符合当前项目软删约束。` : '若该表计划接入系统级 CRUD，建议优先沿用 is_deleted 这类软删字段命名，减少后续手工调整。',
    level: logicalDeleteField ? 'info' : 'advice'
  });

  notices.push({
    title: timeFields.length ? '时间字段较完整' : '时间字段偏少',
    text: timeFields.length ? `已识别 ${timeFields.map((item) => item.columnName).join('、')}，生成页会更容易保留常见时间展示习惯。` : '建议补 create_time / update_time 这类审计字段，后续列表展示与排序会更通用。',
    level: timeFields.length ? 'info' : 'advice'
  });

  notices.push({
    title: nullableRequiredFields.length ? '存在非空业务字段' : '当前字段大多可空',
    text: nullableRequiredFields.length
      ? `检测到 ${nullableRequiredFields.slice(0, 4).map((item) => item.columnName).join('、')}${nullableRequiredFields.length > 4 ? ' 等' : ''} 为非空字段，生成后建议优先核对表单必填校验与默认值。`
      : '如果该表有核心业务字段，建议明确哪些字段应设为非空，避免生成后的表单约束过弱。',
    level: nullableRequiredFields.length ? 'warning' : 'advice'
  });

  notices.push({
    title: longTextFields.length ? '长文本字段需人工复核' : '暂无明显长文本字段',
    text: longTextFields.length ? `已识别 ${longTextFields.map((item) => item.columnName).join('、')}，生成器会优先映射为 textarea，但仍建议人工复核列表展示宽度和表单校验。` : '若后续有备注、描述、富文本等需求，建议生成后再按业务补字段组件与校验。',
    level: longTextFields.length ? 'warning' : 'advice'
  });

  if (previewFiles.value.length) {
    notices.push({
      title: '已生成预览，可直接做模板复核',
      text: `当前已生成 ${previewFiles.value.length} 份文件，建议优先抽查 ${previewSummary.value.categoryText} 中最核心的 controller、service 和前端列表页。`,
      level: 'info'
    });
  }

  return notices;
});

async function loadTables() {
  await tablesFeedback.run(async () => {
    tables.value = await fetchGeneratorTables();
    if (!form.value.tableName && tables.value.length) {
      form.value.tableName = tables.value[0].tableName;
      await handleTableChange(form.value.tableName);
    }
  });
}

async function handleTableChange(tableName, options = {}) {
  if (!tableName) {
    columns.value = [];
    previewFiles.value = [];
    activeFilePath.value = '';
    return;
  }
  selectedTable.value = tables.value.find((item) => item.tableName === tableName) || null;
  if (!options.skipAutoFill) {
    fillFormByTable(tableName);
  }
  columns.value = await fetchGeneratorColumns(tableName);
  previewFiles.value = [];
  activeFilePath.value = '';
}

async function handlePreview() {
  const payload = validateAndNormalizeForm();
  if (!payload) {
    return;
  }
  await previewAction.run(async () => {
    previewFiles.value = await previewGenerator(payload);
    activeFilePath.value = previewFiles.value[0]?.filePath || '';
    saveHistoryIfReady(payload);
  });
}

async function handleDownload() {
  if (!previewFiles.value.length) {
    ElMessage.warning('请先生成代码预览');
    return;
  }
  const payload = validateAndNormalizeForm();
  if (!payload) {
    return;
  }
  await downloadAction.run(async () => {
    const { blob, fileName } = await downloadGeneratorZip(payload);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    saveHistoryIfReady(payload);
    showSuccessMessage('下载生成代码');
  });
}

async function handleWriteToProject() {
  if (!previewFiles.value.length) {
    ElMessage.warning('请先生成代码预览');
    return;
  }
  const payload = validateAndNormalizeForm();
  if (!payload) {
    return;
  }
  await writeAction.run(async () => {
    let conflictFiles = [];
    try {
      conflictFiles = await fetchGeneratorWriteConflicts(payload);
    } catch (error) {
      if (!isConflictEndpointMissing(error)) {
        throw error;
      }
      await writeWithLegacyConflictFlow(payload);
      return;
    }
    if (!conflictFiles.length) {
      const writeResult = await writeGenerator(payload);
      saveMarkerHistory(writeResult?.markerId);
      saveHistoryIfReady(payload);
      showSuccessMessage(`代码已写入项目目录（标记: ${writeResult?.markerId || '-'}）`);
      return;
    }

    const confirmed = await openConflictConfirmDialog(conflictFiles);
    if (!confirmed) {
      return;
    }
    const writeResult = await writeGenerator(payload, { overwrite: true });
    saveMarkerHistory(writeResult?.markerId);
    saveHistoryIfReady(payload);
    showSuccessMessage(`已覆盖并写入项目目录（标记: ${writeResult?.markerId || '-'}）`);
  });
}

async function writeWithLegacyConflictFlow(payload) {
  try {
    const writeResult = await writeGenerator(payload);
    saveMarkerHistory(writeResult?.markerId);
    saveHistoryIfReady(payload);
    showSuccessMessage(`代码已写入项目目录（标记: ${writeResult?.markerId || '-'}）`);
    return;
  } catch (error) {
    if (!isWriteConflictError(error)) {
      throw error;
    }
  }
  const confirmed = await openConflictConfirmDialog(['检测到文件冲突（当前后端版本不支持冲突清单详情）']);
  if (!confirmed) {
    return;
  }
  const writeResult = await writeGenerator(payload, { overwrite: true });
  saveMarkerHistory(writeResult?.markerId);
  saveHistoryIfReady(payload);
  showSuccessMessage(`已覆盖并写入项目目录（标记: ${writeResult?.markerId || '-'}）`);
}

function handleReset() {
  form.value = createDefaultForm();
  columns.value = [];
  previewFiles.value = [];
  activeFilePath.value = '';
  if (tables.value.length) {
    form.value.tableName = tables.value[0].tableName;
    handleTableChange(form.value.tableName).catch(ignoreHandledError);
  }
}

function handleSaveHistory() {
  const payload = validateAndNormalizeForm();
  if (!payload) {
    return;
  }
  saveHistory(payload);
  showSuccessMessage('配置已保存到历史记录');
}

function handleHistoryCommand(historyItem) {
  applyHistory(historyItem);
  showSuccessMessage(`已恢复 ${historyItem.tableName} 的生成配置`);
}

function openQuickDeleteDialog() {
  quickDeleteMarkerId.value = markerHistoryList.value[0]?.markerId || '';
  quickDeleteDialogVisible.value = true;
}

function openWriteRecordDialog() {
  writeRecordDialogVisible.value = true;
  loadWriteRecords().catch(ignoreHandledError);
}

async function loadWriteRecords() {
  await writeRecordAction.run(async () => {
    const [startAt, endAt] = writeRecordQuery.value.timeRange || [];
    writeRecordList.value = await fetchGeneratorWriteRecords({
      limit: 50,
      tableName: (writeRecordQuery.value.tableName || '').trim(),
      startAt: startAt || '',
      endAt: endAt || ''
    });
  });
}

async function handleDeleteRecord(record) {
  if (!record?.markerId) {
    return;
  }
  await deleteAction.run(async () => {
    const result = await deleteGeneratorByMarker(record.markerId);
    await loadMarkerHistory();
    await loadWriteRecords();
    showSuccessMessage(`已删除 ${result.deletedCount} 个文件（缺失 ${result.missingCount} 个）`);
  });
}

async function handleQuickDeleteByMarker() {
  const markerId = (quickDeleteMarkerId.value || '').trim();
  if (!markerId) {
    ElMessage.warning('请先输入或选择标记');
    return;
  }
  await deleteAction.run(async () => {
    const result = await deleteGeneratorByMarker(markerId);
    quickDeleteDialogVisible.value = false;
    await loadMarkerHistory();
    showSuccessMessage(`已删除 ${result.deletedCount} 个文件（缺失 ${result.missingCount} 个）`);
  });
}

function fillFormByTable(tableName) {
  const normalizedName = tableName.replace(/^jq_/, '');
  const businessName = normalizedName.endsWith('s') ? normalizedName : `${normalizedName}s`;
  form.value.moduleName = normalizedName.split('_')[0] || 'demo';
  form.value.businessName = businessName;
  form.value.className = toPascalCase(normalizedName);
  form.value.permPrefix = `${form.value.moduleName}:${normalizedName.replace(/_/g, ':')}`;
}

function createDefaultForm() {
  return {
    tableName: '',
    moduleName: '',
    businessName: '',
    className: '',
    permPrefix: ''
  };
}

function sanitizeGeneratorForm(rawForm) {
  return {
    tableName: (rawForm.tableName || '').trim(),
    moduleName: (rawForm.moduleName || '').trim(),
    businessName: (rawForm.businessName || '').trim(),
    className: (rawForm.className || '').trim(),
    permPrefix: (rawForm.permPrefix || '').trim()
  };
}

function getGeneratorFormError(payload) {
  if (!payload.tableName) {
    return '请先选择数据表';
  }
  if (!payload.moduleName || !payload.businessName || !payload.className || !payload.permPrefix) {
    return '请补齐模块名、业务名、实体名、权限前缀';
  }
  if (!TABLE_NAME_PATTERN.test(payload.tableName)) {
    return '表名仅允许字母、数字和下划线';
  }
  if (!MODULE_NAME_PATTERN.test(payload.moduleName)) {
    return '模块名需以小写字母开头，仅允许小写字母、数字和下划线';
  }
  if (!BUSINESS_NAME_PATTERN.test(payload.businessName)) {
    return '业务名需以小写字母开头，仅允许小写字母、数字、下划线和短横线';
  }
  if (!CLASS_NAME_PATTERN.test(payload.className)) {
    return '实体名需为大驼峰，仅允许字母和数字';
  }
  if (!PERM_PREFIX_PATTERN.test(payload.permPrefix)) {
    return '权限前缀需以小写字母开头，仅允许小写字母、数字、冒号和短横线';
  }
  return '';
}

function validateAndNormalizeForm() {
  const payload = sanitizeGeneratorForm(form.value);
  form.value = {
    ...form.value,
    ...payload
  };
  const errorMessage = getGeneratorFormError(payload);
  if (errorMessage) {
    ElMessage.warning(errorMessage);
    return null;
  }
  return payload;
}

function fileTabLabel(filePath) {
  return filePath.split('/').pop();
}

function buildPreviewFileMeta(filePath) {
  if (!filePath) {
    return {
      category: '预览文件',
      purpose: '切换标签后查看当前模板用途。',
      reviewTip: '优先确认命名、字段映射和引用路径是否符合当前模块约定。'
    };
  }
  if (filePath.endsWith('Controller.java')) {
    return {
      category: '后端控制器',
      purpose: '负责列表、详情、保存、删除等接口入口定义。',
      reviewTip: '重点检查请求路径、权限前缀、分页查询与返回 DTO 是否匹配预期。'
    };
  }
  if (filePath.endsWith('Service.java') || filePath.endsWith('ServiceImpl.java')) {
    return {
      category: '后端服务',
      purpose: '承接业务编排、保存更新逻辑和列表查询实现。',
      reviewTip: '重点确认保存填充、软删逻辑、分页字段和排序约定。'
    };
  }
  if (filePath.endsWith('Mapper.java') || filePath.endsWith('Mapper.xml')) {
    return {
      category: '数据访问层',
      purpose: '负责数据库映射与查询 SQL 条件组装。',
      reviewTip: '重点检查字段映射、查询条件、排序字段和软删过滤是否完整。'
    };
  }
  if (filePath.endsWith('SaveRequest.java')) {
    return {
      category: '保存请求 DTO',
      purpose: '定义新增/编辑表单提交时的字段结构。',
      reviewTip: '重点确认必填字段、类型映射以及不应暴露给前端的字段是否已排除。'
    };
  }
  if (filePath.endsWith('Summary.java')) {
    return {
      category: '列表摘要 DTO',
      purpose: '定义列表页和详情页的主要展示字段。',
      reviewTip: '重点确认表格列顺序、状态字段和时间字段是否适合直接展示。'
    };
  }
  if (filePath.endsWith('.vue')) {
    return {
      category: '前端页面',
      purpose: '生成业务列表页骨架，承接查询、表格、弹窗和分页交互。',
      reviewTip: '重点检查查询区、表格列、表单控件和按钮权限是否贴合实际业务。'
    };
  }
  if (filePath.endsWith('.js')) {
    return {
      category: '前端脚本',
      purpose: '生成 API 调用或路由片段，帮助快速接入业务模块。',
      reviewTip: '重点确认接口路径、导出函数名和路由元信息是否与现有模块保持一致。'
    };
  }
  if (filePath.endsWith('.sql')) {
    return {
      category: 'SQL 补丁',
      purpose: '生成菜单、权限或初始化数据补丁，便于后台模块接入。',
      reviewTip: '重点确认菜单层级、权限标识、排序值和幂等执行风险。'
    };
  }
  return {
    category: '模板文件',
    purpose: '当前文件属于生成结果的一部分，请结合路径判断所在层级。',
    reviewTip: '优先确认命名、字段映射和引用路径是否符合当前模块规则。'
  };
}

function isBackendPreviewFile(filePath) {
  return filePath.includes('jianqing-backend/')
    || filePath.startsWith('backend/')
    || filePath.endsWith('.java')
    || filePath.endsWith('Mapper.xml');
}

function isFrontendPreviewFile(filePath) {
  return filePath.includes('jianqing-admin-web/')
    || filePath.startsWith('frontend/')
    || filePath.endsWith('.vue')
    || (filePath.endsWith('.js') && !isSqlPreviewFile(filePath));
}

function isSqlPreviewFile(filePath) {
  return filePath.startsWith('sql/') || filePath.includes('/sql/');
}

function tableLabel(item) {
  return item.tableComment ? `${item.tableName}（${item.tableComment}）` : item.tableName;
}

function formatHistoryTime(value) {
  if (!value) {
    return '刚刚保存';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '刚刚保存';
  }
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

function markerOptionLabel(item) {
  return `${item.markerId}${item.tableName ? ` / ${item.tableName}` : ''}（${formatHistoryTime(item.createdAt)}）`;
}

async function copyText(text) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }
  const textArea = document.createElement('textarea');
  textArea.value = text;
  textArea.setAttribute('readonly', 'readonly');
  textArea.style.position = 'fixed';
  textArea.style.opacity = '0';
  document.body.appendChild(textArea);
  textArea.select();
  document.execCommand('copy');
  document.body.removeChild(textArea);
}

async function handleCopyFilePath(filePath) {
  try {
    await copyText(filePath);
    showSuccessMessage('复制文件路径');
  } catch {
    ElMessage.warning('复制文件路径失败，请手动复制');
  }
}

async function handleCopyFileContent(file) {
  try {
    await copyText(file.content);
    showSuccessMessage('复制文件内容');
  } catch {
    ElMessage.warning('复制文件内容失败，请手动复制');
  }
}

function isAuditLikeColumn(columnName) {
  return /created?_by|updated?_by|create_time|update_time|created_at|updated_at|deleted|is_deleted/i.test(columnName);
}

function openConflictConfirmDialog(files) {
  conflictFiles.value = files;
  conflictKeyword.value = '';
  conflictQuickFilter.value = 'all';
  conflictDisplayMode.value = 'path';
  conflictCollapsedGroups.value = {};
  conflictDialogVisible.value = true;
  return new Promise((resolve) => {
    conflictResolver = resolve;
  });
}

function finishConflictConfirm(confirmed) {
  conflictDialogVisible.value = false;
  const resolver = conflictResolver;
  conflictResolver = null;
  if (resolver) {
    resolver(confirmed);
  }
}

function handleConfirmConflictOverwrite() {
  finishConflictConfirm(true);
}

function handleCancelConflictOverwrite() {
  finishConflictConfirm(false);
}

function handleCopyConflictFiles() {
  copyText(buildConflictCopyText())
    .then(() => {
      showSuccessMessage('复制冲突清单');
    })
    .catch(() => {
      ElMessage.warning('复制冲突清单失败，请手动复制');
    });
}

function handleExportConflictFiles() {
  const content = buildConflictCopyText();
  if (!content) {
    ElMessage.warning('当前筛选结果为空，无法导出');
    return;
  }
  const mode = conflictDisplayMode.value === 'name' ? 'names' : 'paths';
  const timestamp = buildExportTimestamp();
  const fileName = `generator-conflicts-${mode}-${timestamp}.txt`;
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  showSuccessMessage('导出冲突清单');
}

function handleExportConflictMarkdown() {
  const content = buildConflictMarkdownContent();
  if (!content) {
    ElMessage.warning('当前筛选结果为空，无法导出');
    return;
  }
  const timestamp = buildExportTimestamp();
  const fileName = `generator-conflicts-${timestamp}.md`;
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  showSuccessMessage('导出冲突 Markdown');
}

function handlePreviewConflictMarkdown() {
  const content = buildConflictMarkdownContent();
  if (!content) {
    ElMessage.warning('当前筛选结果为空，无法预览');
    return;
  }
  conflictMarkdownPreviewContent.value = content;
  conflictMarkdownPreviewVisible.value = true;
}

function handleCopyMarkdownPreview() {
  copyText(conflictMarkdownPreviewContent.value)
    .then(() => {
      showSuccessMessage('复制 Markdown');
    })
    .catch(() => {
      ElMessage.warning('复制 Markdown 失败，请手动复制');
    });
}

function buildConflictCopyText() {
  if (conflictDisplayMode.value === 'name') {
    return uniqueConflictBaseNames.value.join('\n');
  }
  return filteredConflictFiles.value.join('\n');
}

function buildConflictMarkdownContent() {
  if (!filteredConflictFiles.value.length) {
    return '';
  }
  const lines = [];
  lines.push('# 代码生成冲突清单');
  lines.push('');
  lines.push(`- 导出时间：${new Date().toLocaleString()}`);
  lines.push(`- 快捷过滤：${conflictQuickFilterLabel()}`);
  lines.push(`- 关键字：${conflictKeyword.value.trim() || '无'}`);
  lines.push(`- 显示模式：${conflictDisplayMode.value === 'name' ? '仅文件名' : '完整路径'}`);
  lines.push(`- 总冲突：${conflictFiles.value.length}`);
  lines.push(`- 当前结果：${filteredConflictFiles.value.length}`);
  lines.push(`- 风险统计：${buildConflictRiskSummaryText()}`);
  lines.push('');
  lines.push('## 目录统计');
  lines.push('');
  if (!conflictDirectorySummary.value.length) {
    lines.push('- 无');
  } else {
    conflictDirectorySummary.value.forEach((item) => {
      lines.push(`- ${item.directory}: ${item.count}`);
    });
  }
  lines.push('');
  lines.push('## 覆盖建议顺序');
  lines.push('');
  const suggestionLines = buildConflictSuggestionLines();
  if (!suggestionLines.length) {
    lines.push('- 无');
  } else {
    suggestionLines.forEach((line) => lines.push(line));
  }
  lines.push('');
  lines.push('## 冲突明细');
  lines.push('');
  if (conflictDisplayMode.value === 'name') {
    uniqueConflictBaseNames.value.forEach((fileName) => {
      lines.push(`- ${fileName}`);
    });
    return lines.join('\n');
  }
  groupedConflictFiles.value.forEach((group) => {
    lines.push(`### ${group.directory}（${group.files.length}，${conflictRiskTag(group.maxRisk)}）`);
    lines.push('');
    group.files.forEach((filePath) => {
      lines.push(`- [${conflictRiskTag(conflictRiskLevel(filePath))}] ${filePath}`);
    });
    lines.push('');
  });
  return lines.join('\n');
}

function conflictQuickFilterLabel() {
  if (conflictQuickFilter.value === 'java-main') {
    return '高风险 Java';
  }
  if (conflictQuickFilter.value === 'frontend') {
    return '前端';
  }
  if (conflictQuickFilter.value === 'sql') {
    return 'SQL';
  }
  return '全部';
}

function buildConflictSuggestionLines() {
  if (conflictDisplayMode.value === 'name') {
    const high = filteredConflictFiles.value.filter((filePath) => conflictRiskTag(conflictRiskLevel(filePath)) === 'HIGH').length;
    const medium = filteredConflictFiles.value.filter((filePath) => conflictRiskTag(conflictRiskLevel(filePath)) === 'MEDIUM').length;
    const low = filteredConflictFiles.value.filter((filePath) => conflictRiskTag(conflictRiskLevel(filePath)) === 'LOW').length;
    return [
      `1. HIGH（${high}）：优先复核后端主源码路径对应文件名。`,
      `2. MEDIUM（${medium}）：其次复核 SQL 变更相关文件名。`,
      `3. LOW（${low}）：最后复核前端与其它文件名。`
    ];
  }
  return groupedConflictFiles.value.map((group, index) => {
    const topFile = group.files[0] || '';
    const topFileName = topFile ? baseNameOf(topFile) : '无';
    return `${index + 1}. [${conflictRiskTag(group.maxRisk)}] ${group.directory}（${group.files.length}）→ 先看 ${topFileName}`;
  });
}

function isConflictEndpointMissing(error) {
  const status = error?.response?.status;
  const message = String(error?.response?.data?.message || error?.message || '');
  return status === 404 && message.includes('/api/dev/gen/write/conflicts');
}

function isWriteConflictError(error) {
  const message = String(error?.response?.data?.message || error?.message || '');
  return message.includes('已存在文件') || message.includes('overwrite=true');
}

function toggleConflictGroup(directory) {
  conflictCollapsedGroups.value = {
    ...conflictCollapsedGroups.value,
    [directory]: !conflictCollapsedGroups.value[directory]
  };
}

function isConflictGroupCollapsed(directory) {
  return Boolean(conflictCollapsedGroups.value[directory]);
}

function expandAllConflictGroups() {
  const next = {};
  groupedConflictFiles.value.forEach((group) => {
    next[group.directory] = false;
  });
  conflictCollapsedGroups.value = next;
}

function collapseAllConflictGroups() {
  const next = {};
  groupedConflictFiles.value.forEach((group) => {
    next[group.directory] = true;
  });
  conflictCollapsedGroups.value = next;
}

function parentDirectoryOf(filePath) {
  const segments = filePath.split('/');
  if (segments.length <= 1) {
    return '(根目录)';
  }
  return segments.slice(0, -1).join('/');
}

function topDirectoryOf(filePath) {
  const segments = filePath.split('/').filter(Boolean);
  return segments[0] || '(根目录)';
}

function baseNameOf(filePath) {
  const segments = filePath.split('/').filter(Boolean);
  return segments[segments.length - 1] || filePath;
}

function conflictRiskLevel(filePath) {
  if (filePath.includes('src/main/java/')) {
    return 3;
  }
  if (filePath.startsWith('sql/') || filePath.includes('/sql/')) {
    return 2;
  }
  if (filePath.includes('jianqing-admin-web/src/')) {
    return 1;
  }
  return 0;
}

function compareConflictPath(left, right) {
  const leftRisk = conflictRiskLevel(left);
  const rightRisk = conflictRiskLevel(right);
  if (leftRisk !== rightRisk) {
    return rightRisk - leftRisk;
  }
  return left.localeCompare(right);
}

function buildExportTimestamp() {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  const hh = String(now.getHours()).padStart(2, '0');
  const mi = String(now.getMinutes()).padStart(2, '0');
  const ss = String(now.getSeconds()).padStart(2, '0');
  return `${yyyy}${mm}${dd}-${hh}${mi}${ss}`;
}

function buildConflictRiskSummaryText() {
  const counter = {
    HIGH: 0,
    MEDIUM: 0,
    LOW: 0
  };
  filteredConflictFiles.value.forEach((filePath) => {
    const tag = conflictRiskTag(conflictRiskLevel(filePath));
    if (counter[tag] !== undefined) {
      counter[tag] += 1;
    }
  });
  return `HIGH ${counter.HIGH} / MEDIUM ${counter.MEDIUM} / LOW ${counter.LOW}`;
}

function conflictRiskTag(level) {
  if (level >= 3) {
    return 'HIGH';
  }
  if (level >= 2) {
    return 'MEDIUM';
  }
  return 'LOW';
}

function toPascalCase(value) {
  return value.split('_').filter(Boolean).map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1)).join('');
}

usePageInitializer(async () => {
  loadHistory();
  await loadMarkerHistory();
  await loadTables();
});
</script>

<style scoped>
.generator-toolbar-shell {
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
}

.generator-toolbar-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.generator-toolbar-context {
  min-width: 0;
  flex: 1 1 auto;
}

.generator-toolbar-context__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.generator-toolbar-context__desc {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--jq-card-subtitle);
}

.generator-toolbar-side {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
}

.generator-config-grid {
  flex: 1 1 auto;
  min-width: 0;
  display: grid;
  grid-template-columns: minmax(220px, 1.3fr) repeat(3, minmax(120px, 0.8fr)) minmax(220px, 1.2fr);
  gap: 10px;
}

.generator-field {
  min-width: 0;
}

.generator-field--wide {
  min-width: 0;
}

.generator-field__label {
  display: block;
  margin-bottom: 6px;
  padding-left: 2px;
  font-size: 12px;
  font-weight: 600;
  color: rgba(86, 98, 130, 0.92);
}

.generator-config-grid__table,
.generator-config-grid__perm {
  min-width: 0;
}

.generator-actions-panel {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.generator-actions-panel__primary,
.generator-actions-panel__secondary {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 8px;
}

.generator-actions-panel__primary .el-button,
.generator-actions-panel__secondary .el-button {
  margin: 0;
}

.generator-layout {
  display: grid;
  grid-template-columns: minmax(560px, 48%) minmax(0, 1fr);
  flex: 1 1 auto;
  gap: 16px;
  min-height: 0;
  overflow: hidden;
}

.generator-panel {
  min-height: 0;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  margin-bottom: 12px;
}

.panel-header__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--jq-card-subtitle);
}

.generator-alert-btn {
  border-color: rgba(245, 158, 11, 0.32) !important;
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.95), rgba(255, 237, 213, 0.88)) !important;
  color: #b45309 !important;
}

.generator-alert-btn:hover,
.generator-alert-btn:focus-visible {
  border-color: rgba(217, 119, 6, 0.45) !important;
  background: linear-gradient(135deg, rgba(255, 247, 237, 1), rgba(254, 215, 170, 0.92)) !important;
  color: #92400e !important;
}

.generator-alert-btn--inline {
  flex-shrink: 0;
}

.generator-alert-btn--icon {
  width: 34px;
  height: 34px;
  padding: 0 !important;
  border-radius: 999px !important;
}

.generator-alert-btn__icon-mark {
  font-size: 15px;
  font-weight: 800;
  line-height: 1;
}

.generator-history-item {
  display: grid;
  gap: 4px;
  min-width: 240px;
  padding: 2px 0;
}

.generator-history-item__title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.generator-history-item__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--jq-card-subtitle);
}

.generator-history-item__dot {
  color: rgba(120, 132, 158, 0.9);
}

.generator-history-item__time {
  font-size: 11px;
  color: rgba(120, 132, 158, 0.92);
}

.generator-panel-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.generator-panel-meta__stat {
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
}

.generator-panel-meta__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.generator-panel-meta__value {
  display: block;
  font-size: 13px;
  line-height: 1.6;
  color: var(--jq-card-title);
  word-break: break-all;
}

.generator-table-panel {
  flex: 1 1 auto;
}

.generator-notice-board {
  padding: 14px;
  border-radius: 14px;
  background: var(--jq-toolbar-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--jq-border-light, rgba(255, 255, 255, 0.12));
}

.generator-notice-board__title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.generator-notice-board__list {
  display: grid;
  gap: 10px;
}

.generator-notice-board__item {
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid transparent;
}

.generator-notice-board__item--danger {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.2);
}

.generator-notice-board__item--warning {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.24);
}

.generator-notice-board__item--advice {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.2);
}

.generator-notice-board__item--info {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.12);
}

.generator-notice-board__item-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.generator-notice-board__item-text {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--jq-card-subtitle);
}

.generator-guide {
  margin-bottom: 12px;
  padding: 14px;
  border: 1px solid var(--jq-border-light, rgba(255, 255, 255, 0.12));
  border-radius: 14px;
  background: var(--jq-toolbar-bg, rgba(255, 255, 255, 0.04));
}

.generator-guide--compact {
  padding: 12px;
}

.generator-guide__summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 8px;
}

.generator-guide__item {
  min-width: 0;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
}

.generator-guide__item--wide {
  grid-column: span 1;
}

.generator-guide__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.generator-guide__value {
  display: block;
  font-size: 13px;
  line-height: 1.6;
  color: var(--jq-card-title);
  word-break: break-all;
}

.generator-guide__inline-tip {
  font-size: 12px;
  line-height: 1.6;
  color: var(--jq-card-subtitle);
}

.generator-tabs {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

:deep(.generator-tabs .el-tabs__header) {
  margin-bottom: 12px;
}

:deep(.generator-tabs .el-tabs__nav-wrap) {
  padding: 0 42px;
}

:deep(.generator-tabs .el-tabs__nav-prev),
:deep(.generator-tabs .el-tabs__nav-next) {
  width: 32px;
  height: 32px;
  top: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.82);
  color: #f8fafc;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.24);
  transform: translateY(-50%);
  transition: all 0.2s ease;
}

:deep(.generator-tabs .el-tabs__nav-prev) {
  left: 0;
}

:deep(.generator-tabs .el-tabs__nav-next) {
  right: 0;
}

:deep(.generator-tabs .el-tabs__nav-prev:hover),
:deep(.generator-tabs .el-tabs__nav-prev:focus-visible),
:deep(.generator-tabs .el-tabs__nav-next:hover),
:deep(.generator-tabs .el-tabs__nav-next:focus-visible) {
  border-color: rgba(129, 140, 248, 0.5);
  background: rgba(30, 41, 59, 0.94);
  color: #ffffff;
}

:deep(.generator-tabs .el-tabs__nav-prev.is-disabled),
:deep(.generator-tabs .el-tabs__nav-next.is-disabled) {
  opacity: 0.38;
  cursor: not-allowed;
}

:deep(.generator-tabs .el-tabs__item) {
  color: var(--jq-card-subtitle);
}

:deep(.generator-tabs .el-tabs__item.is-active) {
  color: var(--jq-card-title);
}

:deep(.generator-tabs .el-tabs__content) {
  flex: 1 1 auto;
  min-height: 0;
}

:deep(.generator-tabs .el-tab-pane) {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.generator-file-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.generator-file-row__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.generator-wrap-switch {
  margin-left: 4px;
}

.generator-file-row__actions .el-button {
  margin: 0;
}

.file-path {
  color: var(--jq-card-subtitle);
  font-size: 13px;
  word-break: break-all;
  flex: 1 1 auto;
}

.generator-file-guide {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.generator-file-guide__badge {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(109, 125, 255, 0.16);
  color: #c7d2fe;
  font-size: 12px;
  font-weight: 600;
}

.generator-file-guide__content {
  min-width: 0;
}

.generator-file-guide__title {
  font-size: 13px;
  font-weight: 600;
  color: #f8fafc;
}

.generator-file-guide__text {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #cbd5e1;
}

.generator-code-viewer {
  flex: 1 1 auto;
  min-height: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(16, 24, 40, 0.92);
  overflow: auto;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.generator-code-viewer__content {
  min-width: max-content;
  margin: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: #e2e8f0;
  white-space: pre;
}

.generator-code-viewer--wrapped {
  overflow-x: hidden;
}

.generator-code-viewer__content--wrapped {
  min-width: 100%;
  white-space: pre-wrap;
  word-break: break-word;
}

.generator-drawer-note__lead {
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--jq-card-subtitle);
}

.generator-conflict-dialog__lead {
  margin-bottom: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--jq-card-subtitle);
}

.generator-conflict-dialog__toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.generator-conflict-dialog__filters {
  flex-shrink: 0;
}

.generator-conflict-dialog__group-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.generator-conflict-dialog__count {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.generator-conflict-summary {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.generator-conflict-summary__label {
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.generator-conflict-summary__chip {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--jq-card-title);
}

.generator-conflict-summary__empty {
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.generator-markdown-preview__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.generator-markdown-preview__content-wrap {
  max-height: 420px;
  overflow: auto;
  border-radius: 10px;
  border: 1px solid var(--jq-border-light, rgba(255, 255, 255, 0.12));
  background: rgba(16, 24, 40, 0.92);
}

.generator-markdown-preview__content {
  margin: 0;
  padding: 12px;
  white-space: pre;
  font-size: 12px;
  line-height: 1.6;
  color: #e2e8f0;
}

.generator-quick-delete {
  display: grid;
  gap: 10px;
}

.generator-quick-delete__hint {
  font-size: 12px;
  color: var(--jq-card-subtitle);
  line-height: 1.6;
}

.generator-write-record {
  display: grid;
  gap: 10px;
}

.generator-write-record__filters {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(300px, 1fr) auto;
  gap: 10px;
}

.generator-conflict-dialog__list-wrap {
  max-height: 280px;
  overflow: auto;
  border-radius: 10px;
  border: 1px solid var(--jq-border-light, rgba(255, 255, 255, 0.12));
  background: var(--jq-toolbar-bg, rgba(255, 255, 255, 0.04));
  padding: 10px 12px;
}

.generator-conflict-group-list {
  display: grid;
  gap: 10px;
}

.generator-conflict-group {
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
}

.generator-conflict-group__title {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--jq-card-subtitle);
}

.generator-conflict-dialog__list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--jq-card-title);
}

.jq-toolbar-select-lg {
  min-width: 200px;
}

@media (max-width: 1280px) {
  .generator-toolbar-topline {
    flex-direction: column;
    align-items: flex-start;
  }

  .generator-toolbar-side {
    width: 100%;
    align-items: flex-start;
  }

  .generator-config-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .generator-actions-panel {
    align-items: flex-start;
    justify-content: flex-start;
    flex-direction: column;
  }

  .generator-actions-panel__primary,
  .generator-actions-panel__secondary {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .generator-layout {
    grid-template-columns: 1fr;
    min-height: auto;
  }
}

@media (max-width: 960px) {
  .generator-config-grid,
  .generator-guide__summary {
    grid-template-columns: 1fr;
  }

  .generator-file-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .generator-file-row__actions {
    flex-wrap: wrap;
  }

  .generator-wrap-switch {
    margin-left: 0;
  }

  .panel-header__topline {
    flex-direction: column;
    align-items: flex-start;
  }

  .generator-panel-meta {
    grid-template-columns: 1fr;
  }

  .generator-conflict-dialog__toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .generator-conflict-dialog__filters {
    align-self: flex-start;
  }

  .generator-write-record__filters {
    grid-template-columns: 1fr;
  }
}
</style>
