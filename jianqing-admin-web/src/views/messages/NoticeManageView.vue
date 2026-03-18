<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="loading" @search="handleSearch" @reset="handleReset" @refresh="loadData">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索通知标题" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="statusInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="all" />
            <el-option v-for="item in NOTICE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="publishModeInput" class="jq-toolbar-select--sm">
            <el-option label="全部方式" value="all" />
            <el-option v-for="item in NOTICE_PUBLISH_MODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
        <template #actions>
          <el-button v-if="canAdd" type="primary" @click="openCreate">新建通知</el-button>
        </template>
      </ListPageHeader>
    </template>

    <div class="jq-table-panel">
      <el-table :data="pagedRows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column prop="title" label="通知标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="级别" width="100">
          <template #default="scope">
            <el-tag :type="optionTagType(NOTICE_LEVEL_OPTIONS, scope.row.level)">{{ optionLabel(NOTICE_LEVEL_OPTIONS, scope.row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布方式" width="110">
          <template #default="scope">
            {{ optionLabel(NOTICE_PUBLISH_MODE_OPTIONS, scope.row.publishMode) }}
          </template>
        </el-table-column>
        <el-table-column label="目标类型" width="110">
          <template #default="scope">
            {{ optionLabel(NOTICE_TARGET_TYPE_OPTIONS, scope.row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="optionTagType(NOTICE_STATUS_OPTIONS, scope.row.status)">{{ optionLabel(NOTICE_STATUS_OPTIONS, scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduledAt" label="计划时间" min-width="170">
          <template #default="scope">{{ scope.row.scheduledAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" min-width="170">
          <template #default="scope">{{ scope.row.publishedAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.id)">详情</el-button>
            <el-button v-if="canEditRow(scope.row)" link type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
            <el-button v-if="canPublishRow(scope.row)" link type="primary" @click="handlePublish(scope.row)">发布</el-button>
            <el-button v-if="canCancelRow(scope.row)" link type="warning" @click="handleCancel(scope.row)">取消</el-button>
            <el-button v-if="canDeleteRow(scope.row)" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="jq-pagination-panel">
      <el-pagination
        class="table-pagination"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-sizes="pageSizes"
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑通知' : '新建通知'" width="760px" append-to-body>
    <el-form label-width="96px">
      <el-form-item label="通知标题">
        <el-input v-model="form.title" maxlength="128" />
      </el-form-item>
      <el-form-item label="通知内容">
        <el-input v-model="form.content" type="textarea" :rows="6" maxlength="5000" show-word-limit />
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="通知级别">
            <el-select v-model="form.level" style="width: 100%;">
              <el-option v-for="item in NOTICE_LEVEL_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="提醒方式">
            <el-switch v-model="form.popupEnabled" :active-value="1" :inactive-value="0" inline-prompt active-text="弹窗" inactive-text="静默" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="发布方式">
            <el-select v-model="form.publishMode" style="width: 100%;">
              <el-option v-for="item in NOTICE_PUBLISH_MODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划发布时间" v-if="form.publishMode === 'SCHEDULED'">
            <el-date-picker v-model="form.scheduledAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="目标类型">
        <el-select v-model="form.targetType" style="width: 100%;">
          <el-option v-for="item in NOTICE_TARGET_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标范围" v-if="form.targetType !== 'ALL'">
        <el-select v-model="form.targetIds" multiple filterable style="width: 100%;" placeholder="请选择目标">
          <el-option v-for="item in currentTargetOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="有效时间">
        <el-date-picker
          v-model="validRange"
          type="datetimerange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%;"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" maxlength="255" type="textarea" :rows="3" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="通知详情" width="760px" append-to-body>
    <template v-if="detail">
      <div class="notice-manage-detail__head">
        <div class="notice-manage-detail__title">{{ detail.title }}</div>
        <div class="notice-manage-detail__meta">
          <el-tag :type="optionTagType(NOTICE_LEVEL_OPTIONS, detail.level)">{{ optionLabel(NOTICE_LEVEL_OPTIONS, detail.level) }}</el-tag>
          <el-tag :type="optionTagType(NOTICE_STATUS_OPTIONS, detail.status)">{{ optionLabel(NOTICE_STATUS_OPTIONS, detail.status) }}</el-tag>
          <span>目标：{{ optionLabel(NOTICE_TARGET_TYPE_OPTIONS, detail.targetType) }}</span>
        </div>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发布方式">{{ optionLabel(NOTICE_PUBLISH_MODE_OPTIONS, detail.publishMode) }}</el-descriptions-item>
        <el-descriptions-item label="弹窗提醒">{{ detail.popupEnabled === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="计划时间">{{ detail.scheduledAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detail.publishedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效开始">{{ detail.validFrom || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效结束">{{ detail.validTo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="notice-manage-detail__section">
        <div class="notice-manage-detail__section-title">目标明细</div>
        <div class="notice-manage-detail__content">{{ targetSummary(detail) }}</div>
      </div>
      <div class="notice-manage-detail__section">
        <div class="notice-manage-detail__section-title">通知正文</div>
        <div class="notice-manage-detail__content">{{ detail.content }}</div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
import {
  cancelNotice,
  createNotice,
  deleteNotice,
  fetchDepts,
  fetchNoticeDetail,
  fetchNotices,
  fetchRoles,
  fetchUsers,
  publishNotice,
  updateNotice
} from '../../api/system';
import {
  DEFAULT_LIST_PAGE_SIZE,
  NOTICE_LEVEL_OPTIONS,
  NOTICE_PUBLISH_MODE_OPTIONS,
  NOTICE_STATUS_OPTIONS,
  NOTICE_TARGET_TYPE_OPTIONS,
  PAGE_SIZE_OPTIONS
} from '../../constants/app';
import { useActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { usePermissionGroup } from '../../composables/usePermissions';
import { useSystemListPage } from '../../composables/useSystemListPage';
import { ignoreHandledError } from '../../utils/errors';
import { showSuccessMessage } from '../../utils/feedback';
import { flattenDeptOptions } from '../system/deptTreeUtils';

const rows = ref([]);
const detail = ref(null);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const editingId = ref(null);
const statusInput = ref('all');
const publishModeInput = ref('all');
const form = ref(createEmptyForm());
const validRange = ref([]);
const roleOptions = ref([]);
const deptOptions = ref([]);
const userOptions = ref([]);
const tableFeedback = useTableFeedback();
const submitAction = useActionLoading();
const loading = tableFeedback.loading;
const submitting = submitAction.loading;
const tableEmptyText = tableFeedback.emptyText;

const {
  canAdd,
  canEdit,
  canPublish,
  canCancel,
  canDelete
} = usePermissionGroup({
  canAdd: 'system:notice:add',
  canEdit: 'system:notice:edit',
  canPublish: 'system:notice:publish',
  canCancel: 'system:notice:cancel',
  canDelete: 'system:notice:delete'
});

const {
  keywordInput,
  filterInput,
  pageNo,
  pageSize,
  pageSizes,
  pagedRows,
  total,
  handleSearch: runListSearch,
  handleReset: runListReset
} = useSystemListPage({
  defaultPageSize: DEFAULT_LIST_PAGE_SIZE,
  pageSizes: PAGE_SIZE_OPTIONS,
  initialFilterInput: { status: 'all', publishMode: 'all' },
  initialFilterValue: { status: 'all', publishMode: 'all' },
  loadData,
  filterRows: ({ keyword, filterValue }) => {
    const query = keyword.trim().toLowerCase();
    return rows.value.filter((item) => {
      const keywordMatched = !query || (item.title || '').toLowerCase().includes(query);
      const statusMatched = filterValue.status === 'all' || item.status === filterValue.status;
      const publishMatched = filterValue.publishMode === 'all' || item.publishMode === filterValue.publishMode;
      return keywordMatched && statusMatched && publishMatched;
    });
  }
});

const currentTargetOptions = computed(() => {
  if (form.value.targetType === 'ROLE') {
    return roleOptions.value;
  }
  if (form.value.targetType === 'DEPT') {
    return deptOptions.value;
  }
  if (form.value.targetType === 'USER') {
    return userOptions.value;
  }
  return [];
});

function createEmptyForm() {
  return {
    title: '',
    content: '',
    level: 'NORMAL',
    publishMode: 'IMMEDIATE',
    scheduledAt: '',
    popupEnabled: 0,
    targetType: 'ALL',
    targetIds: [],
    remark: ''
  };
}

async function loadData() {
  await tableFeedback.run(async () => {
    const [notices, roles, depts, users] = await Promise.all([fetchNotices(), fetchRoles(), fetchDepts(), fetchUsers()]);
    rows.value = notices;
    roleOptions.value = roles.map((item) => ({ label: item.roleName, value: item.id }));
    deptOptions.value = flattenDeptOptions(depts).map((item) => ({ label: item.label, value: item.id }));
    userOptions.value = users.map((item) => ({ label: `${item.nickname || item.username}（${item.username}）`, value: item.id }));
  });
}

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value || '-';
}

function optionTagType(options, value) {
  return options.find((item) => item.value === value)?.tagType || 'info';
}

function canEditRow(row) {
  return canEdit.value && row.status !== 'PUBLISHED';
}

function canPublishRow(row) {
  return canPublish.value && (row.status === 'DRAFT' || row.status === 'CANCELLED');
}

function canCancelRow(row) {
  return canCancel.value && (row.status === 'PENDING' || row.status === 'DRAFT');
}

function canDeleteRow(row) {
  return canDelete.value && row.status !== 'PUBLISHED';
}

function handleSearch() {
  filterInput.value = {
    status: statusInput.value,
    publishMode: publishModeInput.value
  };
  runListSearch();
}

function handleReset() {
  statusInput.value = 'all';
  publishModeInput.value = 'all';
  filterInput.value = {
    status: 'all',
    publishMode: 'all'
  };
  runListReset();
}

async function openDetail(id) {
  try {
    detail.value = await fetchNoticeDetail(id);
    detailVisible.value = true;
  } catch (error) {
    ignoreHandledError(error);
  }
}

function openCreate() {
  editingId.value = null;
  form.value = createEmptyForm();
  validRange.value = [];
  dialogVisible.value = true;
}

async function openEdit(id) {
  try {
    const data = await fetchNoticeDetail(id);
    editingId.value = id;
    form.value = {
      title: data.title,
      content: data.content,
      level: data.level,
      publishMode: data.publishMode,
      scheduledAt: data.scheduledAt,
      popupEnabled: data.popupEnabled,
      targetType: data.targetType,
      targetIds: data.targetIds || [],
      remark: data.remark || ''
    };
    validRange.value = data.validFrom || data.validTo ? [data.validFrom || '', data.validTo || ''] : [];
    dialogVisible.value = true;
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleSubmit() {
  if (!editingId.value && !canAdd.value) {
    ElMessage.warning('当前账号没有新增通知权限');
    return;
  }
  if (editingId.value && !canEdit.value) {
    ElMessage.warning('当前账号没有编辑通知权限');
    return;
  }
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写通知标题和内容');
    return;
  }
  if (form.value.publishMode === 'SCHEDULED' && !form.value.scheduledAt) {
    ElMessage.warning('定时发布必须填写计划时间');
    return;
  }
  if (validRange.value?.length === 2 && validRange.value[0] && validRange.value[1] && validRange.value[0] > validRange.value[1]) {
    ElMessage.warning('有效结束时间不能早于开始时间');
    return;
  }
  if (form.value.targetType !== 'ALL' && !form.value.targetIds.length) {
    ElMessage.warning('请选择通知目标');
    return;
  }
  const payload = {
    ...form.value,
    validFrom: validRange.value?.[0] || null,
    validTo: validRange.value?.[1] || null,
    scheduledAt: form.value.publishMode === 'SCHEDULED' ? form.value.scheduledAt : null,
    targetIds: form.value.targetType === 'ALL' ? [] : form.value.targetIds
  };
  await submitAction.run(async () => {
    if (editingId.value) {
      await updateNotice(editingId.value, payload);
      showSuccessMessage('更新通知');
    } else {
      await createNotice(payload);
      showSuccessMessage('新建通知');
    }
    dialogVisible.value = false;
    await loadData();
  });
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确认发布通知「${row.title}」吗？`, '提示', { type: 'warning' });
    await publishNotice(row.id);
    showSuccessMessage('发布通知');
    await loadData();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(`确认取消通知「${row.title}」吗？`, '提示', { type: 'warning' });
    await cancelNotice(row.id);
    showSuccessMessage('取消通知');
    await loadData();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除通知「${row.title}」吗？`, '提示', { type: 'warning' });
    await deleteNotice(row.id);
    showSuccessMessage('删除通知');
    await loadData();
  } catch (error) {
    ignoreHandledError(error);
  }
}

function targetSummary(row) {
  if (row.targetType === 'ALL') {
    return '全员';
  }
  const options = row.targetType === 'ROLE' ? roleOptions.value : row.targetType === 'DEPT' ? deptOptions.value : userOptions.value;
  return (row.targetIds || []).map((id) => options.find((item) => item.value === id)?.label || `#${id}`).join('、') || '-';
}

watch(() => form.value.publishMode, (value) => {
  if (value !== 'SCHEDULED') {
    form.value.scheduledAt = '';
  }
});

watch(() => form.value.targetType, (value) => {
  if (value === 'ALL') {
    form.value.targetIds = [];
  }
});

usePageInitializer(loadData);
</script>

<style scoped>
.notice-manage-detail__head {
  margin-bottom: 16px;
}

.notice-manage-detail__title {
  font-size: 24px;
  font-weight: 700;
  color: var(--jq-card-title);
}

.notice-manage-detail__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.notice-manage-detail__section {
  margin-top: 18px;
}

.notice-manage-detail__section-title {
  margin-bottom: 8px;
  font-weight: 600;
}

.notice-manage-detail__content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--jq-card-text, #31444d);
}
</style>
