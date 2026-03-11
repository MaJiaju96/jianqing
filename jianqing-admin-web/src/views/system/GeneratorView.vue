<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <div class="jq-toolbar-shell">
      <div class="jq-toolbar-group jq-toolbar-group--filters generator-filters">
        <el-select v-model="form.tableName" placeholder="选择数据表" class="jq-toolbar-field jq-toolbar-select-lg" filterable @change="handleTableChange">
          <el-option v-for="item in tables" :key="item.tableName" :label="tableLabel(item)" :value="item.tableName" />
        </el-select>
        <el-input v-model="form.moduleName" class="jq-toolbar-select--sm" placeholder="模块名" />
        <el-input v-model="form.businessName" class="jq-toolbar-select--sm" placeholder="业务名" />
        <el-input v-model="form.className" class="jq-toolbar-select--sm" placeholder="实体名" />
        <el-input v-model="form.permPrefix" class="jq-toolbar-field" placeholder="权限前缀，如 system:customer" />
        <el-button type="primary" :loading="previewLoading" @click="handlePreview">生成预览</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="jq-toolbar-group jq-toolbar-group--actions">
        <el-button type="primary" :disabled="downloadLoading || !previewFiles.length" :loading="downloadLoading" @click="handleDownload">下载 ZIP</el-button>
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
              <el-dropdown-item v-for="item in historyList" :key="item.id" @click="handleHistoryCommand(item)">
                <el-icon><Clock /></el-icon>{{ item.tableName }} - {{ item.className }}
              </el-dropdown-item>
              <el-dropdown-item v-if="historyList.length" disabled>--- 历史记录 ---</el-dropdown-item>
              <el-dropdown-item v-if="!historyList.length" disabled>暂无历史记录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="generator-layout">
      <div class="generator-panel jq-glass-card">
        <div class="panel-title">字段元数据</div>
        <el-table :data="columns" stripe height="100%" :empty-text="columnEmptyText">
          <el-table-column prop="columnName" label="字段名" min-width="160" />
          <el-table-column prop="columnComment" label="注释" min-width="160" />
          <el-table-column prop="javaType" label="Java 类型" width="120" />
          <el-table-column prop="columnType" label="数据库类型" min-width="140" />
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

      <div class="generator-panel jq-glass-card">
        <div class="panel-title">代码预览</div>
        <el-empty v-if="!previewFiles.length" description="先选择数据表并点击生成预览" />
        <el-tabs v-else v-model="activeFilePath" class="generator-tabs">
          <el-tab-pane v-for="file in previewFiles" :key="file.filePath" :label="fileTabLabel(file.filePath)" :name="file.filePath">
            <div class="file-path">{{ file.filePath }}</div>
            <el-input :model-value="file.content" type="textarea" :rows="22" readonly resize="none" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowDown, FolderChecked, FolderAdd, Clock } from '@element-plus/icons-vue';
import { fetchGeneratorColumns, fetchGeneratorTables, previewGenerator, downloadGeneratorZip, writeGenerator } from '../../api/generator';
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

const HISTORY_KEY = 'jq_generator_history';
const MAX_HISTORY = 10;

function loadHistory() {
  try {
    const stored = localStorage.getItem(HISTORY_KEY);
    historyList.value = stored ? JSON.parse(stored) : [];
  } catch {
    historyList.value = [];
  }
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

function applyHistory(historyItem) {
  form.value = {
    tableName: historyItem.tableName,
    moduleName: historyItem.moduleName,
    businessName: historyItem.businessName,
    className: historyItem.className,
    permPrefix: historyItem.permPrefix
  };
  handleTableChange(historyItem.tableName);
}

const form = ref(createDefaultForm());

const tablesFeedback = useTableFeedback();
const previewAction = useActionLoading();
const downloadAction = useActionLoading();
const writeAction = useActionLoading();

const tablesLoading = tablesFeedback.loading;
const previewLoading = previewAction.loading;
const downloadLoading = downloadAction.loading;
const writeLoading = writeAction.loading;
const columnEmptyText = computed(() => (form.value.tableName ? '当前表暂无字段' : '请先选择数据表'));

async function loadTables() {
  await tablesFeedback.run(async () => {
    tables.value = await fetchGeneratorTables();
    if (!form.value.tableName && tables.value.length) {
      form.value.tableName = tables.value[0].tableName;
      await handleTableChange(form.value.tableName);
    }
  });
}

async function handleTableChange(tableName) {
  if (!tableName) {
    columns.value = [];
    previewFiles.value = [];
    activeFilePath.value = '';
    return;
  }
  selectedTable.value = tables.value.find((item) => item.tableName === tableName) || null;
  fillFormByTable(tableName);
  columns.value = await fetchGeneratorColumns(tableName);
  previewFiles.value = [];
  activeFilePath.value = '';
}

async function handlePreview() {
  if (!form.value.tableName) {
    ElMessage.warning('请先选择数据表');
    return;
  }
  if (!form.value.moduleName || !form.value.businessName || !form.value.className || !form.value.permPrefix) {
    ElMessage.warning('请补齐模块名、业务名、实体名、权限前缀');
    return;
  }
  await previewAction.run(async () => {
    previewFiles.value = await previewGenerator(form.value);
    activeFilePath.value = previewFiles.value[0]?.filePath || '';
  });
}

async function handleDownload() {
  if (!previewFiles.value.length) {
    ElMessage.warning('请先生成代码预览');
    return;
  }
  await downloadAction.run(async () => {
    const { blob, fileName } = await downloadGeneratorZip(form.value);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    showSuccessMessage('下载生成代码');
  });
}

async function handleWriteToProject() {
  if (!previewFiles.value.length) {
    ElMessage.warning('请先生成代码预览');
    return;
  }
  await writeAction.run(async () => {
    await writeGenerator(form.value);
    showSuccessMessage('代码已写入项目目录');
  });
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
  if (!form.value.tableName || !form.value.moduleName || !form.value.businessName || !form.value.className || !form.value.permPrefix) {
    ElMessage.warning('请先完善配置信息');
    return;
  }
  saveHistory(form.value);
  showSuccessMessage('配置已保存到历史记录');
}

function handleHistoryCommand(historyItem) {
  applyHistory(historyItem);
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

function fileTabLabel(filePath) {
  return filePath.split('/').pop();
}

function tableLabel(item) {
  return item.tableComment ? `${item.tableName}（${item.tableComment}）` : item.tableName;
}

function toPascalCase(value) {
  return value.split('_').filter(Boolean).map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1)).join('');
}

usePageInitializer(async () => {
  loadHistory();
  await loadTables();
});
</script>

<style scoped>
.generator-filters {
  flex-wrap: wrap;
  gap: 8px;
}

.generator-filters .el-select,
.generator-filters .el-input {
  margin-right: 4px;
}

.generator-filters .el-button {
  margin-left: 4px;
}

.generator-layout {
  display: grid;
  grid-template-columns: minmax(420px, 44%) minmax(0, 1fr);
  gap: 16px;
  min-height: calc(100vh - 250px);
}

.generator-panel {
  min-height: 0;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.panel-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.generator-tabs {
  min-height: 0;
}

.file-path {
  margin-bottom: 12px;
  color: var(--jq-card-subtitle);
  font-size: 13px;
  word-break: break-all;
}

.jq-toolbar-select-lg {
  min-width: 200px;
}

@media (max-width: 1280px) {
  .generator-layout {
    grid-template-columns: 1fr;
    min-height: auto;
  }
}
</style>
