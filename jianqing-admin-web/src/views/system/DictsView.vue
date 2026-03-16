<template>
  <div class="dict-page">
    <el-card class="jq-glass-card jq-list-page" shadow="never">
      <template #header>
        <ListPageHeader :refresh-loading="typeLoading" @search="handleTypeSearch" @reset="handleTypeReset" @refresh="loadDictTypes">
          <template #filters>
            <el-input v-model="typeKeywordInput" clearable placeholder="搜索字典名称/类型" class="jq-toolbar-field" @keyup.enter="handleTypeSearch" />
            <el-select v-model="typeStatusInput" class="jq-toolbar-select--sm">
              <el-option label="全部状态" value="all" />
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
            </el-select>
          </template>
          <template #actions>
            <el-button v-if="canAddType" type="primary" :icon="Plus" @click="openTypeCreate">新增字典</el-button>
          </template>
        </ListPageHeader>
      </template>
      <div class="dict-card-head">
        <div>
          <div class="dict-card-title">字典类型</div>
          <div class="dict-card-subtitle">维护字典类型编码，切换后可继续管理该类型下的字典数据。</div>
        </div>
        <el-tag v-if="selectedDictType" effect="plain">当前类型：{{ selectedDictType }}</el-tag>
      </div>
      <div class="jq-table-panel">
        <el-table :data="pagedTypeRows" stripe :empty-text="typeEmptyText" height="100%" @row-click="handleSelectTypeRow">
          <el-table-column prop="dictName" label="字典名称" min-width="180" />
          <el-table-column prop="dictType" label="字典类型" min-width="200" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <StatusTag
                :status="scope.row.status"
                :enabled-value="STATUS_ENABLED"
                :enabled-text="statusText(STATUS_ENABLED)"
                :disabled-text="statusText(STATUS_DISABLED)"
                :enabled-type="statusTagType(STATUS_ENABLED)"
                :disabled-type="statusTagType(STATUS_DISABLED)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <div class="dict-action-row">
                <el-button type="primary" link @click="openTypeData(scope.row)">查看数据</el-button>
                <el-button v-if="canEditType" type="primary" link @click.stop="openTypeEdit(scope.row)">编辑</el-button>
                <el-button v-if="canDeleteType" type="danger" link @click.stop="handleDeleteType(scope.row)">删除</el-button>
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
          :total="filteredTypeRows.length"
          :page-sizes="PAGE_SIZE_OPTIONS"
          v-model:current-page="typePageNo"
          v-model:page-size="typePageSize"
        />
      </div>
    </el-card>

    <el-card class="jq-glass-card jq-list-page dict-data-card" shadow="never">
      <template #header>
        <ListPageHeader :refresh-loading="dataLoading" @search="handleDataSearch" @reset="handleDataReset" @refresh="loadDictData">
          <template #filters>
            <el-select v-model="selectedDictType" class="jq-toolbar-field dict-type-select" filterable placeholder="请选择字典类型" @change="handleDictTypeChange">
              <el-option v-for="item in typeRows" :key="item.id" :label="`${item.dictName}（${item.dictType}）`" :value="item.dictType" />
            </el-select>
            <el-input v-model="dataKeywordInput" clearable placeholder="搜索标签/键值" class="jq-toolbar-field" @keyup.enter="handleDataSearch" />
            <el-select v-model="dataStatusInput" class="jq-toolbar-select--sm">
              <el-option label="全部状态" value="all" />
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
            </el-select>
          </template>
          <template #actions>
            <el-button v-if="canAddData" type="primary" :icon="Plus" :disabled="!selectedDictType" @click="openDataCreate">新增字典数据</el-button>
          </template>
        </ListPageHeader>
      </template>
      <div class="dict-card-head">
        <div>
          <div class="dict-card-title">字典数据</div>
          <div class="dict-card-subtitle">{{ selectedTypeDescription }}</div>
        </div>
      </div>
      <div class="jq-table-panel">
        <el-table :data="pagedDataRows" stripe :empty-text="dataEmptyText" height="100%">
          <el-table-column prop="label" label="标签" min-width="140" />
          <el-table-column prop="value" label="键值" min-width="140" />
          <el-table-column label="颜色" width="120">
            <template #default="scope">
              <el-tag :type="resolveColorTypeTag(scope.row.colorType)">{{ colorTypeText(scope.row.colorType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cssClass" label="样式类" min-width="140" />
          <el-table-column prop="sortNo" label="排序" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <StatusTag
                :status="scope.row.status"
                :enabled-value="STATUS_ENABLED"
                :enabled-text="statusText(STATUS_ENABLED)"
                :disabled-text="statusText(STATUS_DISABLED)"
                :enabled-type="statusTagType(STATUS_ENABLED)"
                :disabled-type="statusTagType(STATUS_DISABLED)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <div class="dict-action-row">
                <el-button v-if="canEditData" type="primary" link @click="openDataEdit(scope.row)">编辑</el-button>
                <el-button v-if="canDeleteData" type="danger" link @click="handleDeleteData(scope.row)">删除</el-button>
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
          :total="filteredDataRows.length"
          :page-sizes="PAGE_SIZE_OPTIONS"
          v-model:current-page="dataPageNo"
          v-model:page-size="dataPageSize"
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="typeDialogVisible" :title="typeEditingId ? '编辑字典类型' : '新增字典类型'" width="520px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="字典名称">
        <el-input v-model="typeForm.dictName" maxlength="64" />
      </el-form-item>
      <el-form-item label="字典类型">
        <el-input v-model="typeForm.dictType" maxlength="64" :disabled="Boolean(typeEditingId)" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="typeForm.status" style="width: 100%;">
          <el-option v-for="item in statusOptions" :key="item.value" :value="Number(item.value)" :label="item.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="typeForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="typeSubmitting" @click="typeDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="typeSubmitting" @click="handleSubmitType">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dataDialogVisible" :title="dataEditingId ? '编辑字典数据' : '新增字典数据'" width="560px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="字典类型">
        <el-select v-model="dataForm.dictType" style="width: 100%;" filterable>
          <el-option v-for="item in typeRows" :key="item.id" :label="`${item.dictName}（${item.dictType}）`" :value="item.dictType" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="dataForm.label" maxlength="64" />
      </el-form-item>
      <el-form-item label="键值">
        <el-input v-model="dataForm.value" maxlength="64" />
      </el-form-item>
      <el-form-item label="颜色">
        <el-select v-model="dataForm.colorType" style="width: 100%;" clearable>
          <el-option v-for="item in colorTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="样式类">
        <el-input v-model="dataForm.cssClass" maxlength="64" placeholder="可选，前端业务样式类" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="dataForm.sortNo" :min="0" :max="9999" style="width: 100%;" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="dataForm.status" style="width: 100%;">
          <el-option v-for="item in statusOptions" :key="item.value" :value="Number(item.value)" :label="item.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="dataForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="dataSubmitting" @click="dataDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="dataSubmitting" @click="handleSubmitData">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
import StatusTag from '../../components/StatusTag.vue';
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  fetchDictData,
  fetchDictTypes,
  updateDictData,
  updateDictType
} from '../../api/system';
import {
  DEFAULT_LIST_PAGE_SIZE,
  PAGE_SIZE_OPTIONS,
  STATUS_DISABLED,
  STATUS_ENABLED,
  STATUS_FILTER_ALL
} from '../../constants/app';
import { useDictOptions } from '../../composables/useDictOptions';
import { usePermissionGroup } from '../../composables/usePermissions';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { showSuccessMessage } from '../../utils/feedback';
import { ignoreHandledError } from '../../utils/errors';
import { isValidDictType } from '../../utils/validators';

const fallbackColorTypeOptions = [
  { label: '默认', value: '' },
  { label: '主色', value: 'primary' },
  { label: '成功', value: 'success' },
  { label: '信息', value: 'info' },
  { label: '警告', value: 'warning' },
  { label: '危险', value: 'danger' }
];

const fallbackStatusOptions = [
  { label: '启用', value: '1', colorType: 'success' },
  { label: '禁用', value: '0', colorType: 'danger' }
];

const DICT_COLOR_TYPE = 'sys_dict_color_type';
const DICT_COMMON_STATUS = 'sys_common_status';

const typeRows = ref([]);
const dataRows = ref([]);
const typeLoading = ref(false);
const dataLoading = ref(false);
const typeSubmitting = ref(false);
const dataSubmitting = ref(false);

const typeKeywordInput = ref('');
const typeStatusInput = ref(STATUS_FILTER_ALL);
const typeKeyword = ref('');
const typeStatus = ref(STATUS_FILTER_ALL);
const typePageNo = ref(1);
const typePageSize = ref(DEFAULT_LIST_PAGE_SIZE);

const dataKeywordInput = ref('');
const dataStatusInput = ref(STATUS_FILTER_ALL);
const dataKeyword = ref('');
const dataStatus = ref(STATUS_FILTER_ALL);
const dataPageNo = ref(1);
const dataPageSize = ref(DEFAULT_LIST_PAGE_SIZE);

const selectedDictType = ref('');

const typeDialogVisible = ref(false);
const typeEditingId = ref(null);
const typeForm = ref(createTypeForm());

const dataDialogVisible = ref(false);
const dataEditingId = ref(null);
const dataForm = ref(createDataForm());

const {
  canAddType,
  canEditType,
  canDeleteType,
  canAddData,
  canEditData,
  canDeleteData
} = usePermissionGroup({
  canAddType: 'system:dict:add',
  canEditType: 'system:dict:edit',
  canDeleteType: 'system:dict:remove',
  canAddData: 'system:dict-data:add',
  canEditData: 'system:dict-data:edit',
  canDeleteData: 'system:dict-data:remove'
});
const { loadDictOptions, getOptions, getLabel, getTagType } = useDictOptions();

const colorTypeOptions = computed(() => {
  const options = getOptions(DICT_COLOR_TYPE);
  return options.length ? options : fallbackColorTypeOptions;
});

const statusOptions = computed(() => {
  const options = getOptions(DICT_COMMON_STATUS);
  return options.length ? options : fallbackStatusOptions;
});

const filteredTypeRows = computed(() => {
  const keyword = typeKeyword.value.trim().toLowerCase();
  return typeRows.value.filter((item) => {
    const keywordMatched = !keyword
      || item.dictName.toLowerCase().includes(keyword)
      || item.dictType.toLowerCase().includes(keyword);
    const statusMatched = typeStatus.value === STATUS_FILTER_ALL || item.status === typeStatus.value;
    return keywordMatched && statusMatched;
  });
});

const pagedTypeRows = computed(() => {
  const start = (typePageNo.value - 1) * typePageSize.value;
  return filteredTypeRows.value.slice(start, start + typePageSize.value);
});

const filteredDataRows = computed(() => {
  const keyword = dataKeyword.value.trim().toLowerCase();
  return dataRows.value.filter((item) => {
    const keywordMatched = !keyword
      || item.label.toLowerCase().includes(keyword)
      || item.value.toLowerCase().includes(keyword);
    const statusMatched = dataStatus.value === STATUS_FILTER_ALL || item.status === dataStatus.value;
    return keywordMatched && statusMatched;
  });
});

const pagedDataRows = computed(() => {
  const start = (dataPageNo.value - 1) * dataPageSize.value;
  return filteredDataRows.value.slice(start, start + dataPageSize.value);
});

const selectedTypeDescription = computed(() => {
  if (!selectedDictType.value) {
    return '请先选择字典类型，再维护对应字典数据。';
  }
  const current = typeRows.value.find((item) => item.dictType === selectedDictType.value);
  if (!current) {
    return `当前类型：${selectedDictType.value}`;
  }
  return `当前类型：${current.dictName}（${current.dictType}）`;
});

const typeEmptyText = computed(() => (typeLoading.value ? '加载中...' : '暂无字典类型'));
const dataEmptyText = computed(() => {
  if (dataLoading.value) {
    return '加载中...';
  }
  if (!selectedDictType.value) {
    return '请先选择字典类型';
  }
  return '暂无字典数据';
});

usePageInitializer(async () => {
  await loadDictOptions([DICT_COLOR_TYPE, DICT_COMMON_STATUS]);
  await loadDictTypes();
});

async function loadDictTypes() {
  typeLoading.value = true;
  try {
    const rows = await fetchDictTypes();
    typeRows.value = rows;
    if (!rows.length) {
      selectedDictType.value = '';
      dataRows.value = [];
      return;
    }
    if (!rows.some((item) => item.dictType === selectedDictType.value)) {
      selectedDictType.value = rows[0].dictType;
    }
    await loadDictData();
  } finally {
    typeLoading.value = false;
  }
}

async function loadDictData() {
  if (!selectedDictType.value) {
    dataRows.value = [];
    return;
  }
  dataLoading.value = true;
  try {
    dataRows.value = await fetchDictData(selectedDictType.value);
  } finally {
    dataLoading.value = false;
  }
}

function handleTypeSearch() {
  typeKeyword.value = typeKeywordInput.value;
  typeStatus.value = typeStatusInput.value;
  typePageNo.value = 1;
}

function handleTypeReset() {
  typeKeywordInput.value = '';
  typeStatusInput.value = STATUS_FILTER_ALL;
  handleTypeSearch();
}

function handleDataSearch() {
  dataKeyword.value = dataKeywordInput.value;
  dataStatus.value = dataStatusInput.value;
  dataPageNo.value = 1;
}

function handleDataReset() {
  dataKeywordInput.value = '';
  dataStatusInput.value = STATUS_FILTER_ALL;
  handleDataSearch();
}

function handleSelectTypeRow(row) {
  selectedDictType.value = row.dictType;
  loadDictData().catch(ignoreHandledError);
}

function handleDictTypeChange() {
  dataPageNo.value = 1;
  loadDictData().catch(ignoreHandledError);
}

function openTypeCreate() {
  typeEditingId.value = null;
  typeForm.value = createTypeForm();
  typeDialogVisible.value = true;
}

function openTypeEdit(row) {
  typeEditingId.value = row.id;
  typeForm.value = {
    dictName: row.dictName,
    dictType: row.dictType,
    status: row.status,
    remark: row.remark || ''
  };
  typeDialogVisible.value = true;
}

function openTypeData(row) {
  selectedDictType.value = row.dictType;
  loadDictData().catch(ignoreHandledError);
}

async function handleSubmitType() {
  if (!typeForm.value.dictName.trim()) {
    ElMessage.warning('请填写字典名称');
    return;
  }
  if (!isValidDictType(typeForm.value.dictType.trim())) {
    ElMessage.warning('字典类型仅支持小写字母、数字和下划线，且需以字母开头');
    return;
  }
  typeSubmitting.value = true;
  try {
    const payload = {
      ...typeForm.value,
      dictName: typeForm.value.dictName.trim(),
      dictType: typeForm.value.dictType.trim(),
      remark: (typeForm.value.remark || '').trim()
    };
    if (typeEditingId.value) {
      await updateDictType(typeEditingId.value, payload);
      showSuccessMessage('更新字典类型');
    } else {
      await createDictType(payload);
      showSuccessMessage('新增字典类型');
    }
    typeDialogVisible.value = false;
    selectedDictType.value = payload.dictType;
    await loadDictTypes();
  } catch (error) {
    ignoreHandledError(error);
  } finally {
    typeSubmitting.value = false;
  }
}

async function handleDeleteType(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典类型“${row.dictName}”吗？`, '提示', {
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteDictType(row.id);
    showSuccessMessage('删除字典类型');
    await loadDictTypes();
  } catch (error) {
    ignoreHandledError(error);
  }
}

function openDataCreate() {
  if (!selectedDictType.value) {
    ElMessage.warning('请先选择字典类型');
    return;
  }
  dataEditingId.value = null;
  dataForm.value = createDataForm(selectedDictType.value);
  dataDialogVisible.value = true;
}

function openDataEdit(row) {
  dataEditingId.value = row.id;
  dataForm.value = {
    dictType: row.dictType,
    label: row.label,
    value: row.value,
    colorType: row.colorType || '',
    cssClass: row.cssClass || '',
    sortNo: row.sortNo,
    status: row.status,
    remark: row.remark || ''
  };
  dataDialogVisible.value = true;
}

async function handleSubmitData() {
  if (!dataForm.value.dictType) {
    ElMessage.warning('请选择字典类型');
    return;
  }
  if (!dataForm.value.label.trim() || !dataForm.value.value.trim()) {
    ElMessage.warning('请填写字典标签和键值');
    return;
  }
  dataSubmitting.value = true;
  try {
    const payload = {
      ...dataForm.value,
      dictType: dataForm.value.dictType.trim(),
      label: dataForm.value.label.trim(),
      value: dataForm.value.value.trim(),
      colorType: dataForm.value.colorType || '',
      cssClass: (dataForm.value.cssClass || '').trim(),
      remark: (dataForm.value.remark || '').trim()
    };
    if (dataEditingId.value) {
      await updateDictData(dataEditingId.value, payload);
      showSuccessMessage('更新字典数据');
    } else {
      await createDictData(payload);
      showSuccessMessage('新增字典数据');
    }
    dataDialogVisible.value = false;
    if (selectedDictType.value !== payload.dictType) {
      selectedDictType.value = payload.dictType;
    }
    await loadDictData();
  } catch (error) {
    ignoreHandledError(error);
  } finally {
    dataSubmitting.value = false;
  }
}

async function handleDeleteData(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典数据“${row.label}”吗？`, '提示', {
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteDictData(row.id);
    showSuccessMessage('删除字典数据');
    await loadDictData();
  } catch (error) {
    ignoreHandledError(error);
  }
}

function createTypeForm() {
  return {
    dictName: '',
    dictType: '',
    status: STATUS_ENABLED,
    remark: ''
  };
}

function createDataForm(dictType = selectedDictType.value) {
  return {
    dictType,
    label: '',
    value: '',
    colorType: '',
    cssClass: '',
    sortNo: 0,
    status: STATUS_ENABLED,
    remark: ''
  };
}

function colorTypeText(value) {
  return getLabel(DICT_COLOR_TYPE, value, colorTypeOptions.value.find((item) => item.value === value)?.label || '默认');
}

function resolveColorTypeTag(value) {
  return normalizeTagType(getTagType(DICT_COLOR_TYPE, value, colorTypeOptions.value.find((item) => item.value === value)?.value));
}

function normalizeTagType(value) {
  return value || undefined;
}

function statusText(value) {
  return getLabel(DICT_COMMON_STATUS, value, String(value) === '1' ? '启用' : '禁用');
}

function statusTagType(value) {
  return getTagType(DICT_COMMON_STATUS, value, String(value) === '1' ? 'success' : 'danger');
}
</script>

<style scoped>
.dict-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dict-data-card {
  min-height: 520px;
}

.dict-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.dict-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.dict-card-subtitle {
  margin-top: 6px;
  color: var(--jq-card-subtitle);
  font-size: 13px;
  font-weight: 400;
  line-height: 1.55;
}

:global(html[data-theme='midnight']) .dict-card-title,
:global(html[data-theme='emerald']) .dict-card-title,
:global(html[data-theme='sunset']) .dict-card-title,
:global(html[data-theme='violet']) .dict-card-title {
  color: rgba(15, 23, 42, 0.98);
  font-weight: 700;
}

:global(html[data-theme='midnight']) .dict-card-subtitle,
:global(html[data-theme='emerald']) .dict-card-subtitle,
:global(html[data-theme='sunset']) .dict-card-subtitle,
:global(html[data-theme='violet']) .dict-card-subtitle {
  color: rgba(30, 41, 59, 0.88);
  font-weight: 500;
}

:global(html[data-theme='midnight']) .dict-page .jq-glass-card,
:global(html[data-theme='emerald']) .dict-page .jq-glass-card,
:global(html[data-theme='sunset']) .dict-page .jq-glass-card,
:global(html[data-theme='violet']) .dict-page .jq-glass-card {
  background: rgba(255, 255, 255, 0.82) !important;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.16);
}

.dict-action-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.dict-type-select {
  min-width: 260px;
}

@media (max-width: 900px) {
  .dict-card-head {
    flex-direction: column;
  }
}
</style>
