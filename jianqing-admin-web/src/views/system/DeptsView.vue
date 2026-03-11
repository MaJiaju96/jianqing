<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="pageLoading" @search="handleSearch" @reset="handleReset" @refresh="handleRefresh">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索部门名称" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="filterInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="all" />
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </template>
        <template #actions>
          <el-button v-if="canAdd" type="primary" :icon="Plus" @click="openCreate">新增部门</el-button>
        </template>
      </ListPageHeader>
    </template>
    <div class="jq-table-panel">
      <el-table
        :data="pagedRows"
        row-key="id"
        stripe
        :empty-text="tableEmptyText"
        height="100%"
      >
        <el-table-column label="部门名称" min-width="220">
          <template #default="scope">
            <div class="dept-name-cell" :style="{ paddingLeft: `${Math.max(0, (scope.row.level - 1) * 20)}px` }">
              <span>{{ scope.row.deptName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="负责人" min-width="140">
          <template #default="scope">
            {{ leaderNameMap[scope.row.leaderUserId] || (scope.row.leaderUserId > 0 ? `用户#${scope.row.leaderUserId}` : '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="sortNo" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === STATUS_ENABLED ? 'success' : 'info'">{{ scope.row.status === STATUS_ENABLED ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <div class="action-group">
              <el-button v-if="canAdd" type="primary" link :disabled="submitLoading || deleteLoadingId === scope.row.id" @click="openCreateChild(scope.row)">新增子部门</el-button>
              <el-button v-if="canEdit" type="primary" link :disabled="submitLoading || deleteLoadingId === scope.row.id" @click="openEdit(scope.row)">编辑</el-button>
              <el-button v-if="canDelete" type="danger" link :loading="deleteLoadingId === scope.row.id" :disabled="submitLoading" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="上级部门">
          <el-select v-model="form.parentId" style="width: 100%;">
            <el-option label="顶级部门" :value="ROOT_PARENT_ID" />
            <el-option v-for="item in deptOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.leaderUserId" style="width: 100%;">
            <el-option label="未设置" :value="0" />
            <el-option v-for="item in leaderOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%;">
            <el-option label="启用" :value="STATUS_ENABLED" />
            <el-option label="停用" :value="STATUS_DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import ListPageHeader from '../../components/ListPageHeader.vue';
import { DEFAULT_LIST_PAGE_SIZE, PAGE_SIZE_OPTIONS, ROOT_PARENT_ID, STATUS_DISABLED, STATUS_ENABLED } from '../../constants/app';
import { createDept, deleteDept, fetchDepts, fetchUsers, updateDept } from '../../api/system';
import { useTableFeedback } from '../../composables/useAsyncState';
import { useEntityDeleteAction } from '../../composables/useEntityDeleteAction';
import { useEntityDialogForm } from '../../composables/useEntityDialogForm';
import { usePermissionGroup } from '../../composables/usePermissions';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { useEntitySubmitAction } from '../../composables/useEntitySubmitAction';
import { useSystemListPage } from '../../composables/useSystemListPage';
import { filterDeptTree, flattenDeptOptions, flattenDeptRows } from './deptTreeUtils';

const rows = ref([]);
const userRows = ref([]);

const tableFeedback = useTableFeedback();
const pageLoading = tableFeedback.loading;
const tableEmptyText = tableFeedback.emptyText;

const { canAdd, canEdit, canDelete } = usePermissionGroup({
  canAdd: 'system:dept:add',
  canEdit: 'system:dept:edit',
  canDelete: 'system:dept:delete'
});

const {
  keywordInput,
  filterInput,
  pageNo,
  pageSize,
  pageSizes,
  total,
  pagedRows,
  handleSearch,
  handleReset,
  handleRefresh
} = useSystemListPage({
  initialFilterInput: 'all',
  initialFilterValue: 'all',
  defaultPageSize: DEFAULT_LIST_PAGE_SIZE,
  pageSizes: PAGE_SIZE_OPTIONS,
  loadData,
  filterRows: ({ keyword, filterValue }) => filterDeptTree(rows.value, keyword.trim(), filterValue),
  mapRows: (items) => flattenDeptRows(items)
});
const deptOptions = computed(() => flattenDeptOptions(rows.value));
const leaderOptions = computed(() => userRows.value.map((item) => ({
  id: item.id,
  label: `${item.nickname}（${item.username}）`
})).sort((a, b) => a.id - b.id));
const leaderNameMap = computed(() => userRows.value.reduce((result, item) => {
  result[item.id] = `${item.nickname}（${item.username}）`;
  return result;
}, {}));

const { dialogVisible, isEdit, editingId, form, openCreate, openEdit, closeDialog } = useEntityDialogForm({
  createForm: () => ({
    parentId: ROOT_PARENT_ID,
    deptName: '',
    leaderUserId: 0,
    phone: '',
    email: '',
    sortNo: 0,
    status: STATUS_ENABLED
  }),
  mapForm: (row) => ({
    parentId: row.parentId ?? ROOT_PARENT_ID,
    deptName: row.deptName,
    leaderUserId: row.leaderUserId,
    phone: row.phone,
    email: row.email,
    sortNo: row.sortNo,
    status: row.status
  })
});

const { deleteLoadingId, handleDelete } = useEntityDeleteAction({
  entityLabel: '部门',
  getRowLabel: (row) => row.deptName,
  deleteEntity: deleteDept,
  reloadData: loadData,
  successText: '删除部门'
});

const { submitLoading, handleSubmit: runSubmit } = useEntitySubmitAction({
  closeDialog,
  reloadData: loadData,
  getSuccessText: () => (isEdit.value ? '更新部门' : '新增部门')
});

async function loadData() {
  await tableFeedback.run(async () => {
    const [depts, users] = await Promise.all([fetchDepts(), fetchUsers()]);
    rows.value = depts;
    userRows.value = users;
  });
}

function openCreateChild(row) {
  openCreate((nextForm) => {
    nextForm.parentId = row.id;
  });
}

async function handleSubmit() {
  if (!form.value.deptName) {
    return;
  }
  await runSubmit(async () => {
    if (isEdit.value) {
      await updateDept(editingId.value, form.value);
    } else {
      await createDept(form.value);
    }
  });
}

usePageInitializer(async () => {
  await loadData();
  handleSearch();
});
</script>

<style scoped>
.action-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-pagination {
  margin-top: 0;
  justify-content: flex-end;
}

.dept-name-cell {
  display: flex;
  align-items: center;
  min-height: 22px;
}
</style>
