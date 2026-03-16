<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="loading" @search="handleSearch" @reset="handleReset" @refresh="loadConfigs">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索参数键/参数名称" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="groupInput" class="jq-toolbar-select--sm" filterable allow-create default-first-option>
            <el-option label="全部分组" value="all" />
            <el-option v-for="item in groupOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="valueTypeInput" class="jq-toolbar-select--sm">
            <el-option label="全部类型" value="all" />
            <el-option v-for="item in valueTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="builtinInput" class="jq-toolbar-select--sm">
            <el-option label="全部来源" value="all" />
            <el-option v-for="item in builtinOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
          </el-select>
        </template>
        <template #actions>
          <el-button v-if="canAdd" @click="openDeletedHistory">恢复已删</el-button>
          <el-button v-if="canAdd" type="primary" :icon="Plus" @click="openCreate">新增参数</el-button>
        </template>
      </ListPageHeader>
    </template>
    <div class="jq-table-panel">
      <el-table :data="pagedRows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column prop="configName" label="参数名称" min-width="160" />
        <el-table-column prop="configKey" label="参数键" min-width="220" />
        <el-table-column prop="configGroup" label="参数分组" width="140" />
        <el-table-column prop="configValue" label="参数值" min-width="220" show-overflow-tooltip />
        <el-table-column label="值类型" width="120">
          <template #default="scope">
            <el-tag>{{ valueTypeText(scope.row.valueType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100">
          <template #default="scope">
            <el-tag :type="builtinTagType(scope.row.isBuiltin)">{{ builtinText(scope.row.isBuiltin) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="config-action-row">
              <el-button v-if="canEdit" type="primary" link @click="openEdit(scope.row)">编辑</el-button>
              <el-button type="primary" link @click="openHistory(scope.row)">历史</el-button>
              <el-button v-if="canDelete" type="danger" link :disabled="scope.row.isBuiltin === 1" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="jq-pagination-panel">
      <el-pagination
        class="table-pagination"
        background
        layout="total, sizes, prev, pager, next"
        :total="filteredRows.length"
        :page-sizes="PAGE_SIZE_OPTIONS"
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑参数' : '新增参数'" width="560px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="参数名称">
        <el-input v-model="form.configName" maxlength="128" />
      </el-form-item>
      <el-form-item label="参数键">
        <el-input v-model="form.configKey" maxlength="128" :disabled="Boolean(editingId) || form.isBuiltin === 1" />
      </el-form-item>
      <el-form-item label="参数值">
        <el-input v-model="form.configValue" type="textarea" :rows="4" maxlength="1024" show-word-limit />
      </el-form-item>
      <el-form-item label="参数分组">
        <el-select v-model="form.configGroup" style="width: 100%;" filterable allow-create default-first-option>
          <el-option v-for="item in groupOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="值类型">
        <el-select v-model="form.valueType" style="width: 100%;">
          <el-option v-for="item in valueTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="内置参数">
        <el-select v-model="form.isBuiltin" style="width: 100%;" :disabled="Boolean(editingId)">
          <el-option v-for="item in builtinFormOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="historyDialogVisible" title="参数变更历史" width="880px" append-to-body>
    <div class="config-history-head" v-if="currentHistoryConfigName">
      当前参数：{{ currentHistoryConfigName }}
    </div>
    <div class="config-history-head" v-if="compareBaseHistory">
      已选基准：#{{ compareBaseHistory.id }} · {{ compareBaseHistory.changeType }} · {{ compareBaseHistory.createdAt }}
      <el-button type="primary" link @click="clearCompareBase">清除基准</el-button>
    </div>
    <el-table :data="historyRows" stripe :empty-text="historyLoading ? '加载中...' : '暂无历史记录'" max-height="420">
      <el-table-column prop="changeType" label="变更类型" width="100" />
      <el-table-column prop="configGroup" label="分组" width="110" />
      <el-table-column prop="configValue" label="参数值" min-width="220" show-overflow-tooltip />
      <el-table-column prop="changeNote" label="说明" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" min-width="180" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="toggleCompareBase(scope.row)">{{ compareBaseHistory?.id === scope.row.id ? '取消基准' : '设为基准' }}</el-button>
          <el-button type="primary" link @click="openDiff(scope.row)">对比</el-button>
          <el-button type="primary" link :disabled="scope.row.changeType === 'DELETE'" @click="handleRollback(scope.row)">回滚</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="diffDialogVisible" title="参数差异对比" width="920px" append-to-body>
    <div class="config-history-head" v-if="diffSummary">
      参数键：{{ diffSummary.configKey }} ·
      <template v-if="diffSummary.compareWithCurrent">
        当前版本 vs 历史 #{{ diffSummary.historyId }}（{{ diffSummary.historyChangeType }} · {{ diffSummary.historyCreatedAt }}）
      </template>
      <template v-else>
        历史 #{{ diffSummary.historyId }}（{{ diffSummary.historyChangeType }} · {{ diffSummary.historyCreatedAt }}）
        vs 历史 #{{ diffSummary.compareHistoryId }}（{{ diffSummary.compareChangeType }} · {{ diffSummary.compareCreatedAt }}）
      </template>
    </div>
    <el-table :data="diffItems" stripe :empty-text="diffLoading ? '加载中...' : '暂无差异数据'" max-height="420">
      <el-table-column prop="fieldLabel" label="字段" width="120" />
      <el-table-column prop="currentValue" :label="leftDiffLabel" min-width="220" show-overflow-tooltip />
      <el-table-column prop="historyValue" :label="rightDiffLabel" min-width="220" show-overflow-tooltip />
      <el-table-column label="是否变化" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.changed ? 'warning' : 'success'">{{ scope.row.changed ? '已变化' : '一致' }}</el-tag>
        </template>
      </el-table-column>
      </el-table>
  </el-dialog>

  <el-dialog v-model="deletedHistoryDialogVisible" title="已删参数恢复" width="920px" append-to-body>
    <el-table :data="deletedHistoryRows" stripe :empty-text="deletedHistoryLoading ? '加载中...' : '暂无可恢复记录'" max-height="420">
      <el-table-column prop="configName" label="参数名称" min-width="160" />
      <el-table-column prop="configKey" label="参数键" min-width="220" />
      <el-table-column prop="configGroup" label="参数分组" width="120" />
      <el-table-column prop="configValue" label="参数值" min-width="220" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="删除时间" min-width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="openRestorePreview(scope.row)">预览</el-button>
          <el-button type="primary" link :loading="restoreHistoryId === scope.row.id" @click="handleRestoreDeleted(scope.row)">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="restorePreviewDialogVisible" title="恢复前预览" width="720px" append-to-body>
    <div v-loading="restorePreviewLoading">
      <template v-if="restorePreview">
        <div class="config-history-head">
          将恢复删除历史 #{{ restorePreview.id }} · 删除时间：{{ restorePreview.createdAt }}
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="参数名称">{{ restorePreview.configName }}</el-descriptions-item>
          <el-descriptions-item label="参数键">{{ restorePreview.configKey }}</el-descriptions-item>
          <el-descriptions-item label="参数分组">{{ restorePreview.configGroup }}</el-descriptions-item>
          <el-descriptions-item label="值类型">{{ valueTypeText(restorePreview.valueType) }}</el-descriptions-item>
          <el-descriptions-item label="参数值">{{ restorePreview.configValue || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ builtinText(restorePreview.isBuiltin) }}</el-descriptions-item>
          <el-descriptions-item label="删除说明">{{ restorePreview.changeNote || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="restore-preview-status" :class="restorePreview.keyOccupied ? 'is-warning' : 'is-safe'">
          {{ restorePreview.keyOccupied
            ? `当前已有同键参数（#${restorePreview.existingConfigId} ${restorePreview.existingConfigName || ''}），直接恢复会失败。`
            : '当前不存在同键参数，可直接恢复。' }}
        </div>
        <el-table
          v-if="restorePreview.keyOccupied"
          :data="restorePreview.items || []"
          stripe
          max-height="280"
          empty-text="暂无差异数据"
        >
          <el-table-column prop="fieldLabel" label="字段" width="120" />
          <el-table-column prop="currentValue" label="当前同键值" min-width="220" show-overflow-tooltip />
          <el-table-column prop="historyValue" label="待恢复值" min-width="220" show-overflow-tooltip />
          <el-table-column label="是否变化" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.changed ? 'warning' : 'success'">{{ scope.row.changed ? '已变化' : '一致' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
    <template #footer>
      <el-button :disabled="restorePreviewLoading || restoreHistoryId !== null" @click="restorePreviewDialogVisible = false">关闭</el-button>
      <el-button type="primary" :loading="restorePreview?.historyId === restoreHistoryId" :disabled="!restorePreview || restorePreviewLoading || restorePreview.keyOccupied" @click="handleRestoreDeleted({ id: restorePreview.historyId, configName: restorePreview.configKey }, true)">确认恢复</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
import {
  createConfig,
  deleteConfig,
  fetchConfigDiff,
  fetchConfigHistory,
  fetchConfigs,
  fetchDeletedConfigHistory,
  fetchDeletedConfigRestorePreview,
  restoreDeletedConfig,
  rollbackConfig,
  updateConfig
} from '../../api/system';
import { DEFAULT_LIST_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../../constants/app';
import { useDictOptions } from '../../composables/useDictOptions';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { usePermissionGroup } from '../../composables/usePermissions';
import { ignoreHandledError } from '../../utils/errors';
import { showSuccessMessage } from '../../utils/feedback';
import { isValidConfigKey } from '../../utils/validators';

const fallbackValueTypeOptions = [
  { label: '字符串', value: 'string' },
  { label: '数字', value: 'number' },
  { label: '布尔值', value: 'boolean' },
  { label: 'JSON', value: 'json' }
];

const CONFIG_VALUE_TYPE_DICT = 'sys_config_value_type';
const CONFIG_SOURCE_DICT = 'sys_config_source';

const rows = ref([]);
const loading = ref(false);
const submitting = ref(false);
const keywordInput = ref('');
const groupInput = ref('all');
const valueTypeInput = ref('all');
const builtinInput = ref('all');
const keyword = ref('');
const group = ref('all');
const valueType = ref('all');
const builtin = ref('all');
const pageNo = ref(1);
const pageSize = ref(DEFAULT_LIST_PAGE_SIZE);
const dialogVisible = ref(false);
const editingId = ref(null);
const form = ref(createForm());
const historyDialogVisible = ref(false);
const historyLoading = ref(false);
const historyRows = ref([]);
const currentHistoryConfigName = ref('');
const diffDialogVisible = ref(false);
const diffLoading = ref(false);
const diffSummary = ref(null);
const compareBaseHistory = ref(null);
const deletedHistoryDialogVisible = ref(false);
const deletedHistoryLoading = ref(false);
const deletedHistoryRows = ref([]);
const restorePreviewDialogVisible = ref(false);
const restorePreviewLoading = ref(false);
const restorePreview = ref(null);
const restoreHistoryId = ref(null);

const { canAdd, canEdit, canDelete } = usePermissionGroup({
  canAdd: 'system:config:add',
  canEdit: 'system:config:edit',
  canDelete: 'system:config:remove'
});
const { loadDictOptions, getOptions, getLabel, getTagType } = useDictOptions();

const valueTypeOptions = computed(() => {
  const options = getOptions(CONFIG_VALUE_TYPE_DICT);
  return options.length ? options : fallbackValueTypeOptions;
});

const builtinOptions = computed(() => {
  const options = getOptions(CONFIG_SOURCE_DICT);
  return options.length ? options : [
    { label: '自定义', value: '0', colorType: 'info' },
    { label: '内置', value: '1', colorType: 'warning' }
  ];
});

const builtinFormOptions = computed(() => builtinOptions.value.map((item) => ({
  label: item.value === '1' ? '是' : '否',
  value: item.value
})));

const groupOptions = computed(() => Array.from(new Set(rows.value.map((item) => item.configGroup).filter(Boolean))).sort());

const filteredRows = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  return rows.value.filter((item) => {
    const keywordMatched = !query
      || item.configKey.toLowerCase().includes(query)
      || item.configName.toLowerCase().includes(query);
    const groupMatched = group.value === 'all' || item.configGroup === group.value;
    const typeMatched = valueType.value === 'all' || item.valueType === valueType.value;
    const builtinMatched = builtin.value === 'all' || item.isBuiltin === builtin.value;
    return keywordMatched && groupMatched && typeMatched && builtinMatched;
  });
});

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value;
  return filteredRows.value.slice(start, start + pageSize.value);
});

const tableEmptyText = computed(() => (loading.value ? '加载中...' : '暂无参数'));
const leftDiffLabel = computed(() => (diffSummary.value?.compareWithCurrent ? '当前值' : '历史值A'));
const rightDiffLabel = computed(() => (diffSummary.value?.compareWithCurrent ? '历史值' : '历史值B'));

usePageInitializer(async () => {
  await loadDictOptions([CONFIG_VALUE_TYPE_DICT, CONFIG_SOURCE_DICT]);
  await loadConfigs();
});

async function loadConfigs() {
  loading.value = true;
  try {
    rows.value = await fetchConfigs();
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  keyword.value = keywordInput.value;
  group.value = groupInput.value;
  valueType.value = valueTypeInput.value;
  builtin.value = builtinInput.value;
  pageNo.value = 1;
}

function handleReset() {
  keywordInput.value = '';
  groupInput.value = 'all';
  valueTypeInput.value = 'all';
  builtinInput.value = 'all';
  handleSearch();
}

function openCreate() {
  editingId.value = null;
  form.value = createForm();
  dialogVisible.value = true;
}

function openEdit(row) {
  editingId.value = row.id;
  form.value = {
    configKey: row.configKey,
    configValue: row.configValue,
    configName: row.configName,
    configGroup: row.configGroup,
    valueType: row.valueType,
    isBuiltin: row.isBuiltin,
    remark: row.remark || ''
  };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!form.value.configName.trim()) {
    ElMessage.warning('请填写参数名称');
    return;
  }
  if (!isValidConfigKey(form.value.configKey.trim())) {
    ElMessage.warning('参数键仅支持小写字母开头，后续可包含小写字母、数字、点、横线与下划线');
    return;
  }
  if (!form.value.configValue.trim()) {
    ElMessage.warning('请填写参数值');
    return;
  }
  if (!form.value.configGroup.trim()) {
    ElMessage.warning('请填写参数分组');
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      configKey: form.value.configKey.trim(),
      configValue: form.value.configValue.trim(),
      configName: form.value.configName.trim(),
      configGroup: form.value.configGroup.trim().toUpperCase(),
      valueType: form.value.valueType,
      isBuiltin: form.value.isBuiltin,
      remark: (form.value.remark || '').trim()
    };
    if (editingId.value) {
      await updateConfig(editingId.value, payload);
      showSuccessMessage('更新参数');
    } else {
      await createConfig(payload);
      showSuccessMessage('新增参数');
    }
    dialogVisible.value = false;
    await loadConfigs();
  } catch (error) {
    ignoreHandledError(error);
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除参数“${row.configName}”吗？`, '提示', { type: 'warning' });
  } catch {
    return;
  }
  try {
    await deleteConfig(row.id);
    showSuccessMessage('删除参数');
    await loadConfigs();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function openHistory(row) {
  currentHistoryConfigName.value = row.configName;
  editingId.value = row.id;
  compareBaseHistory.value = null;
  historyDialogVisible.value = true;
  historyLoading.value = true;
  try {
    historyRows.value = await fetchConfigHistory(row.id);
  } catch (error) {
    historyRows.value = [];
    ignoreHandledError(error);
  } finally {
    historyLoading.value = false;
  }
}

function toggleCompareBase(row) {
  if (compareBaseHistory.value?.id === row.id) {
    compareBaseHistory.value = null;
    return;
  }
  compareBaseHistory.value = row;
}

function clearCompareBase() {
  compareBaseHistory.value = null;
}

async function openDeletedHistory() {
  deletedHistoryDialogVisible.value = true;
  deletedHistoryLoading.value = true;
  try {
    deletedHistoryRows.value = await fetchDeletedConfigHistory();
  } catch (error) {
    deletedHistoryRows.value = [];
    ignoreHandledError(error);
  } finally {
    deletedHistoryLoading.value = false;
  }
}

async function openRestorePreview(row) {
  restorePreviewDialogVisible.value = true;
  restorePreviewLoading.value = true;
  restorePreview.value = null;
  try {
    restorePreview.value = await fetchDeletedConfigRestorePreview(row.id);
  } catch (error) {
    restorePreviewDialogVisible.value = false;
    ignoreHandledError(error);
  } finally {
    restorePreviewLoading.value = false;
  }
}

async function handleRollback(row) {
  try {
    await ElMessageBox.confirm(`确认回滚到历史记录 #${row.id} 吗？`, '提示', { type: 'warning' });
  } catch {
    return;
  }
  try {
    await rollbackConfig(editingId.value, row.id);
    showSuccessMessage('回滚参数');
    await Promise.all([loadConfigs(), openHistory({ id: editingId.value, configName: currentHistoryConfigName.value })]);
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleRestoreDeleted(row, skipConfirm = false) {
  if (!skipConfirm) {
    try {
      await ElMessageBox.confirm(`确认恢复已删除参数“${row.configName}”吗？`, '提示', { type: 'warning' });
    } catch {
      return;
    }
  }
  restoreHistoryId.value = row.id;
  try {
    await restoreDeletedConfig(row.id);
    showSuccessMessage('恢复参数');
    restorePreviewDialogVisible.value = false;
    await Promise.all([loadConfigs(), openDeletedHistory()]);
  } catch (error) {
    ignoreHandledError(error);
  } finally {
    restoreHistoryId.value = null;
  }
}

const diffItems = computed(() => diffSummary.value?.items || []);

async function openDiff(row) {
  if (compareBaseHistory.value?.id === row.id) {
    ElMessage.warning('请为对比选择另一条历史记录');
    return;
  }
  diffDialogVisible.value = true;
  diffLoading.value = true;
  diffSummary.value = null;
  try {
    diffSummary.value = await fetchConfigDiff(editingId.value, row.id, compareBaseHistory.value?.id);
  } catch (error) {
    ignoreHandledError(error);
  } finally {
    diffLoading.value = false;
  }
}

function valueTypeText(value) {
  return getLabel(CONFIG_VALUE_TYPE_DICT, value, valueTypeOptions.value.find((item) => item.value === value)?.label || value);
}

function builtinText(value) {
  return getLabel(CONFIG_SOURCE_DICT, value, String(value) === '1' ? '内置' : '自定义');
}

function builtinTagType(value) {
  return getTagType(CONFIG_SOURCE_DICT, value, String(value) === '1' ? 'warning' : 'info');
}

function createForm() {
  return {
    configKey: '',
    configValue: '',
    configName: '',
    configGroup: 'DEFAULT_GROUP',
    valueType: 'string',
    isBuiltin: 0,
    remark: ''
  };
}
</script>

<style scoped>
.config-action-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.config-history-head {
  margin-bottom: 12px;
  color: var(--jq-card-subtitle);
}

.restore-preview-status {
  margin: 16px 0 12px;
}

.restore-preview-status.is-warning {
  color: var(--el-color-warning);
}

.restore-preview-status.is-safe {
  color: var(--el-color-success);
}
</style>
