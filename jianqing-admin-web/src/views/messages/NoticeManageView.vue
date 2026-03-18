<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="loading" @search="handleSearch" @reset="handleReset" @refresh="loadData">
        <template #filters>
          <div class="notice-bucket-switch" role="tablist" aria-label="通知分组切换">
            <button
              type="button"
              class="notice-bucket-switch__item"
              :class="{ 'is-active': activeBucket === 'active' }"
              @click="activeBucket = 'active'"
            >
              通知列表
            </button>
            <button
              type="button"
              class="notice-bucket-switch__item"
              :class="{ 'is-active': activeBucket === 'trash' }"
              @click="activeBucket = 'trash'"
            >
              垃圾箱
            </button>
          </div>
          <el-input v-model="keywordInput" clearable placeholder="搜索通知标题" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-if="activeBucket === 'active'" v-model="statusInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="all" />
            <el-option v-for="item in NOTICE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-if="activeBucket === 'active'" v-model="publishModeInput" class="jq-toolbar-select--sm">
            <el-option label="全部方式" value="all" />
            <el-option v-for="item in NOTICE_PUBLISH_MODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-if="activeBucket === 'trash'" v-model="trashCategoryInput" class="jq-toolbar-select--sm" @change="handleSearch">
            <el-option label="全部" value="all" />
            <el-option label="已发布" value="published" />
            <el-option label="未发布" value="unpublished" />
          </el-select>
        </template>
        <template #actions>
          <template v-if="activeBucket === 'active'">
            <el-button v-if="canAdd" type="primary" @click="openCreate">新建通知</el-button>
            <el-button v-if="canPublish" :disabled="!selectedPublishableCount" @click="handleBatchPublish">批量发布</el-button>
            <el-button v-if="canDelete" :disabled="!selectedRows.length" @click="handleBatchDelete">批量删除</el-button>
          </template>
          <template v-else>
            <el-button v-if="canEdit" :disabled="!selectedRows.length" @click="handleBatchRestore">批量恢复</el-button>
            <el-button v-if="canDelete" type="danger" plain :disabled="!selectedRows.length" @click="handleBatchPurge">批量彻底删除</el-button>
          </template>
        </template>
      </ListPageHeader>
    </template>

    <div class="jq-table-panel">
      <el-table
        ref="tableRef"
        :data="pagedRows"
        row-key="id"
        stripe
        :empty-text="tableEmptyText"
        height="100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />
        <el-table-column label="通知标题" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" :underline="false" class="notice-title-link" @click="openDetail(scope.row.id)">
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>
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
          <template #default="scope">{{ formatDateTimeText(scope.row.scheduledAt) }}</template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" min-width="170">
          <template #default="scope">{{ formatDateTimeText(scope.row.publishedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="activeBucket === 'trash' ? 170 : 130" fixed="right">
          <template #default="scope">
            <div class="notice-row-actions">
              <template v-if="activeBucket === 'trash'">
                <el-button v-if="canEdit" link type="primary" @click="handleRestore(scope.row)">恢复</el-button>
                <el-button v-if="canDelete" link type="danger" @click="handlePurge(scope.row)">彻底删除</el-button>
              </template>
              <template v-else>
                <el-button v-if="canEditRow(scope.row)" link type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
                <el-button v-if="canPublishRow(scope.row)" link type="primary" @click="handlePublish(scope.row)">发布</el-button>
                <el-button v-if="canDirectDeleteRow(scope.row)" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
                <el-dropdown v-if="buildMoreActions(scope.row).length" @command="(command) => handleMoreCommand(command, scope.row)">
                  <el-button link type="info">更多</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="action in buildMoreActions(scope.row)"
                        :key="action.command"
                        :command="action.command"
                      >
                        {{ action.label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
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
      <el-button type="primary" plain :loading="submitting" @click="handleSaveDraft">保存草稿</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmitAndPublish">发布</el-button>
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
        <el-descriptions-item label="计划时间">{{ formatDateTimeText(detail.scheduledAt) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDateTimeText(detail.publishedAt) }}</el-descriptions-item>
        <el-descriptions-item label="有效开始">{{ formatDateTimeText(detail.validFrom) }}</el-descriptions-item>
        <el-descriptions-item label="有效结束">{{ formatDateTimeText(detail.validTo) }}</el-descriptions-item>
      </el-descriptions>
      <div class="notice-manage-detail__section">
        <div class="notice-manage-detail__section-title">目标明细</div>
        <div class="notice-manage-detail__content">{{ targetSummary(detail) }}</div>
      </div>
      <div class="notice-manage-detail__section">
        <div class="notice-manage-detail__section-title">通知正文</div>
        <div class="notice-manage-detail__content">{{ detail.content }}</div>
      </div>
      <div class="notice-manage-detail__section">
        <div class="notice-manage-detail__section-title">备注</div>
        <div class="notice-manage-detail__content">{{ detail.remark || '-' }}</div>
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
  fetchNoticeTrash,
  fetchRoles,
  fetchUsers,
  purgeNotice,
  publishNotice,
  restoreNotice,
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
import { formatDateTimeText } from '../../utils/datetime';
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
const trashCategoryInput = ref('all');
const activeBucket = ref('active');
const form = ref(createEmptyForm());
const validRange = ref([]);
const roleOptions = ref([]);
const deptOptions = ref([]);
const userOptions = ref([]);
const optionLoaded = ref(false);
const tableRef = ref(null);
const selectedRows = ref([]);
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
      if (activeBucket.value === 'trash') {
        return keywordMatched;
      }
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
const selectedPublishableCount = computed(() => selectedRows.value.filter((item) => canPublishRow(item)).length);

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
    const notices = activeBucket.value === 'trash'
      ? await fetchNoticeTrash(trashCategoryInput.value)
      : await fetchNotices();
    rows.value = notices;
    selectedRows.value = [];
    if (tableRef.value) {
      tableRef.value.clearSelection();
    }
    if (!optionLoaded.value) {
      const [roles, depts, users] = await Promise.all([fetchRoles(), fetchDepts(), fetchUsers()]);
      roleOptions.value = roles.map((item) => ({ label: item.roleName, value: item.id }));
      deptOptions.value = flattenDeptOptions(depts).map((item) => ({ label: item.label, value: item.id }));
      userOptions.value = users.map((item) => ({ label: `${item.nickname || item.username}（${item.username}）`, value: item.id }));
      optionLoaded.value = true;
    }
  });
}

function handleSelectionChange(value) {
  selectedRows.value = Array.isArray(value) ? value : [];
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
  return canDelete.value;
}

function canDirectDeleteRow(row) {
  return canDeleteRow(row) && row.status === 'PUBLISHED';
}

function buildMoreActions(row) {
  const actions = [];
  if (canCancelRow(row)) {
    actions.push({ label: '取消', command: 'cancel' });
  }
  if (canDeleteRow(row) && !canDirectDeleteRow(row)) {
    actions.push({ label: '删除', command: 'delete' });
  }
  return actions;
}

function handleMoreCommand(command, row) {
  if (command === 'cancel') {
    handleCancel(row);
    return;
  }
  if (command === 'delete') {
    handleDelete(row);
  }
}

function handleSearch() {
  filterInput.value = {
    status: statusInput.value,
    publishMode: publishModeInput.value,
    trashCategory: trashCategoryInput.value
  };
  runListSearch();
}

function handleReset() {
  statusInput.value = 'all';
  publishModeInput.value = 'all';
  filterInput.value = {
    status: 'all',
    publishMode: 'all',
    trashCategory: 'all'
  };
  trashCategoryInput.value = 'all';
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

function validateForm() {
  if (!editingId.value && !canAdd.value) {
    ElMessage.warning('当前账号没有新增通知权限');
    return false;
  }
  if (editingId.value && !canEdit.value) {
    ElMessage.warning('当前账号没有编辑通知权限');
    return false;
  }
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写通知标题和内容');
    return false;
  }
  if (form.value.publishMode === 'SCHEDULED' && !form.value.scheduledAt) {
    ElMessage.warning('定时发布必须填写计划时间');
    return false;
  }
  if (validRange.value?.length === 2 && validRange.value[0] && validRange.value[1] && validRange.value[0] > validRange.value[1]) {
    ElMessage.warning('有效结束时间不能早于开始时间');
    return false;
  }
  if (form.value.targetType !== 'ALL' && !form.value.targetIds.length) {
    ElMessage.warning('请选择通知目标');
    return false;
  }
  return true;
}

function buildSubmitPayload() {
  return {
    ...form.value,
    validFrom: validRange.value?.[0] || null,
    validTo: validRange.value?.[1] || null,
    scheduledAt: form.value.publishMode === 'SCHEDULED' ? form.value.scheduledAt : null,
    targetIds: form.value.targetType === 'ALL' ? [] : form.value.targetIds
  };
}

async function handleSaveDraft() {
  await submitNotice(false);
}

async function handleSubmitAndPublish() {
  await submitNotice(true);
}

async function submitNotice(publishAfterSave) {
  if (!validateForm()) {
    return;
  }
  if (publishAfterSave && !canPublish.value) {
    ElMessage.warning('当前账号没有发布通知权限');
    return;
  }
  const payload = buildSubmitPayload();
  await submitAction.run(async () => {
    let noticeId = editingId.value;
    if (editingId.value) {
      await updateNotice(editingId.value, payload);
    } else {
      const created = await createNotice(payload);
      noticeId = created?.id;
    }
    if (publishAfterSave) {
      if (!noticeId) {
        ElMessage.warning('通知创建成功但未获取到通知编号，请刷新后重试发布');
        return;
      }
      await publishNotice(noticeId);
      showSuccessMessage('发布通知');
      notifyNoticeRefresh();
    } else {
      showSuccessMessage(editingId.value ? '保存草稿' : '新建草稿');
    }
    dialogVisible.value = false;
    await loadData();
    handleSearch();
  });
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确认发布通知「${row.title}」吗？`, '提示', { type: 'warning' });
    await publishNotice(row.id);
    showSuccessMessage('发布通知');
    await loadData();
    notifyNoticeRefresh();
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
    notifyNoticeRefresh();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除通知「${row.title}」吗？删除后将移入垃圾箱。`, '提示', { type: 'warning' });
    await deleteNotice(row.id);
    showSuccessMessage('已移入垃圾箱');
    await loadData();
    notifyNoticeRefresh();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleRestore(row) {
  try {
    await ElMessageBox.confirm(`确认恢复通知「${row.title}」吗？`, '提示', { type: 'warning' });
    await restoreNotice(row.id);
    showSuccessMessage('恢复通知');
    await loadData();
    notifyNoticeRefresh();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handlePurge(row) {
  try {
    await ElMessageBox.confirm(`确认彻底删除通知「${row.title}」吗？该操作不可恢复。`, '提示', { type: 'warning' });
    await purgeNotice(row.id);
    showSuccessMessage('彻底删除通知');
    await loadData();
    notifyNoticeRefresh();
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function runBatchAction(items, action, successText, options = {}) {
  if (!items.length) {
    return;
  }
  const { limit = 3, deadlockRetry = 0 } = options;
  const settled = await runWithConcurrencyLimit(items, limit, async (item) => executeActionWithRetry(action, item.id, deadlockRetry));
  const successCount = settled.filter((item) => item.status === 'fulfilled').length;
  const failedCount = settled.length - successCount;
  if (successCount) {
    showSuccessMessage(`${successText}${successCount} 条`);
  }
  if (failedCount) {
    ElMessage.warning(`${failedCount} 条处理失败，请刷新后重试`);
  }
  await loadData();
  handleSearch();
  notifyNoticeRefresh();
}

function notifyNoticeRefresh() {
  window.dispatchEvent(new CustomEvent('jq-notice-refresh'));
}

async function handleBatchPublish() {
  const candidates = selectedRows.value.filter((item) => canPublishRow(item));
  if (!candidates.length) {
    ElMessage.warning('请先勾选可发布的草稿/已取消通知');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认批量发布选中的 ${candidates.length} 条通知吗？`, '提示', { type: 'warning' });
    await runBatchAction(candidates, publishNotice, '已发布 ', { limit: 4, deadlockRetry: 2 });
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function executeActionWithRetry(action, id, deadlockRetry) {
  const maxRetry = Math.max(0, Number(deadlockRetry) || 0);
  let attempt = 0;
  while (true) {
    try {
      await action(id);
      return;
    } catch (error) {
      if (!isDeadlockError(error) || attempt >= maxRetry) {
        throw error;
      }
      attempt += 1;
      await sleep(80 * attempt);
    }
  }
}

function isDeadlockError(error) {
  const message = `${error?.response?.data?.message || ''} ${error?.message || ''}`.toLowerCase();
  return message.includes('deadlock') || message.includes('锁') || message.includes('1213');
}

function sleep(ms) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

async function runWithConcurrencyLimit(items, limit, task) {
  const safeLimit = Math.max(1, Number(limit) || 1);
  const results = new Array(items.length);
  let currentIndex = 0;

  async function worker() {
    while (currentIndex < items.length) {
      const index = currentIndex;
      currentIndex += 1;
      try {
        await task(items[index]);
        results[index] = { status: 'fulfilled' };
      } catch (error) {
        results[index] = { status: 'rejected', reason: error };
        ignoreHandledError(error);
      }
    }
  }

  const workers = [];
  for (let i = 0; i < Math.min(safeLimit, items.length); i += 1) {
    workers.push(worker());
  }
  await Promise.all(workers);
  return results;
}

async function handleBatchDelete() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选要删除的通知');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条通知吗？删除后将移入垃圾箱。`, '提示', { type: 'warning' });
    await runBatchAction(selectedRows.value, deleteNotice, '已删除 ');
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleBatchRestore() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选要恢复的通知');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认恢复选中的 ${selectedRows.value.length} 条通知吗？`, '提示', { type: 'warning' });
    await runBatchAction(selectedRows.value, restoreNotice, '已恢复 ');
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleBatchPurge() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选要彻底删除的通知');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认彻底删除选中的 ${selectedRows.value.length} 条通知吗？该操作不可恢复。`, '提示', { type: 'warning' });
    await runBatchAction(selectedRows.value, purgeNotice, '已彻底删除 ');
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

watch(activeBucket, async (value) => {
  if (value === 'active') {
    statusInput.value = 'all';
    publishModeInput.value = 'all';
  } else {
    trashCategoryInput.value = 'all';
  }
  await loadData();
  handleSearch();
});

usePageInitializer(loadData);
</script>

<style scoped>
.notice-bucket-switch {
  display: inline-flex;
  align-items: center;
  padding: 4px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(20, 67, 93, 0.08), rgba(34, 121, 140, 0.16));
  border: 1px solid rgba(34, 121, 140, 0.2);
}

.notice-bucket-switch__item {
  border: 0;
  background: transparent;
  color: #3f5f70;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notice-bucket-switch__item:hover {
  color: #1e455c;
}

.notice-bucket-switch__item.is-active {
  color: #10384e;
  background: linear-gradient(135deg, #d7edf8, #f3f8df);
  box-shadow: 0 2px 6px rgba(16, 56, 78, 0.15);
  font-weight: 600;
}

.notice-manage-detail__head {
  margin-bottom: 16px;
}

.notice-row-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
  white-space: nowrap;
}

.notice-title-link {
  max-width: 100%;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
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
