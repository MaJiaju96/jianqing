<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="pageLoading" @search="handleSearch" @reset="handleReset" @refresh="handleRefresh">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索角色名称/编码" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="filterInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="all" />
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
          </el-select>
        </template>
        <template #actions>
          <el-button v-if="canAdd" type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
        </template>
      </ListPageHeader>
    </template>
    <div class="jq-table-panel">
      <el-table :data="pagedRows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="180" />
        <el-table-column prop="roleCode" label="角色编码" min-width="180" />
        <el-table-column label="数据范围" min-width="120">
          <template #default="scope">
            <el-tag effect="plain">{{ dataScopeText(scope.row.dataScope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getTagType(COMMON_STATUS_DICT, scope.row.status, scope.row.status === STATUS_ENABLED ? 'success' : 'danger')">
              {{ getLabel(COMMON_STATUS_DICT, scope.row.status, scope.row.status === STATUS_ENABLED ? '启用' : '禁用') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <div class="role-action-row">
               <el-button v-if="canEdit" type="primary" link :loading="menuSaving && currentRole?.id === scope.row.id" @click="openAssignMenus(scope.row)">分配菜单</el-button>
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
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="520px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="角色名称">
        <el-input v-model="form.roleName" />
      </el-form-item>
      <el-form-item label="角色编码">
        <el-input v-model="form.roleCode" />
      </el-form-item>
      <el-form-item label="数据范围">
        <el-select v-model="form.dataScope" style="width: 100%;">
          <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%;">
          <el-option v-for="item in statusOptions" :key="item.value" :value="Number(item.value)" :label="item.label" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
       <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
       <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="menuDialogVisible" title="分配菜单" width="620px" append-to-body>
    <div class="assign-title">当前角色：{{ currentRole?.roleName }}</div>
    <div class="assign-toolbar">
      <el-select v-model="assignTypeFilter" style="width: 150px;" @change="handleAssignTypeChange">
        <el-option label="全部权限" value="all" />
        <el-option label="菜单权限" value="menu" />
        <el-option label="按钮权限" value="button" />
      </el-select>
    </div>
    <el-tree
      ref="menuTreeRef"
      :data="menuTree"
      node-key="id"
      show-checkbox
      default-expand-all
      :props="{ label: 'menuName', children: 'children' }"
      :filter-node-method="filterAssignNode"
      class="role-menu-tree"
    >
      <template #default="{ data }">
        <div class="menu-node-row">
          <span>{{ data.menuName }}</span>
          <div class="menu-node-meta">
            <el-tag size="small" :type="menuTypeTag(data.menuType)">{{ menuTypeText(data.menuType) }}</el-tag>
            <span class="menu-node-perms">{{ data.perms || data.routePath || '-' }}</span>
          </div>
        </div>
      </template>
    </el-tree>
    <template #footer>
       <el-button :disabled="menuSaving" @click="menuDialogVisible = false">取消</el-button>
       <el-button type="primary" :loading="menuSaving" @click="handleSaveMenus">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
import {
  DEFAULT_LIST_PAGE_SIZE,
  MENU_TYPE_BUTTON,
  MENU_TYPE_DIRECTORY,
  MENU_TYPE_PAGE,
  DATA_SCOPE_ALL,
  DATA_SCOPE_DEPT,
  DATA_SCOPE_OPTIONS,
  DATA_SCOPE_SELF,
  PAGE_SIZE_OPTIONS,
  STATUS_ENABLED,
  STATUS_FILTER_ALL
} from '../../constants/app';
import {
  assignRoleMenus,
  createRole,
  deleteRole,
  fetchMenus,
  fetchRoleMenuIds,
  fetchRoles,
  updateRole
} from '../../api/system';
import { useActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { useEntityDeleteAction } from '../../composables/useEntityDeleteAction';
import { useEntityDialogForm } from '../../composables/useEntityDialogForm';
import { usePermissionGroup } from '../../composables/usePermissions';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { useDictOptions } from '../../composables/useDictOptions';
import { useEntitySubmitAction } from '../../composables/useEntitySubmitAction';
import { useSystemListPage } from '../../composables/useSystemListPage';
import { showSuccessMessage } from '../../utils/feedback';
import { isValidRoleCode } from '../../utils/validators';
import { getMenuTypeTag as menuTypeTag, getMenuTypeText as menuTypeText } from './menuMeta';

const rows = ref([]);
const COMMON_STATUS_DICT = 'sys_common_status';
const menuDialogVisible = ref(false);
const currentRole = ref(null);
const menuTree = ref([]);
const menuTreeRef = ref(null);
const assignTypeFilter = ref('all');
const dataScopeOptions = DATA_SCOPE_OPTIONS;

const { canAdd, canEdit, canDelete } = usePermissionGroup({
  canAdd: 'system:role:add',
  canEdit: 'system:role:edit',
  canDelete: 'system:role:remove'
});

const {
  keywordInput,
  filterInput,
  pageNo,
  pageSize,
  pageSizes,
  pagedRows,
  total,
  handleSearch,
  handleReset,
  handleRefresh
} = useSystemListPage({
  initialFilterInput: STATUS_FILTER_ALL,
  initialFilterValue: STATUS_FILTER_ALL,
  defaultPageSize: DEFAULT_LIST_PAGE_SIZE,
  pageSizes: PAGE_SIZE_OPTIONS,
  loadData,
  filterRows: ({ keyword, filterValue }) => {
    const query = keyword.trim().toLowerCase();
    return rows.value.filter((item) => {
      const keywordMatched = !query
        || item.roleName.toLowerCase().includes(query)
        || item.roleCode.toLowerCase().includes(query);
      const statusMatched = filterValue === STATUS_FILTER_ALL || item.status === filterValue;
      return keywordMatched && statusMatched;
    });
  }
});

const tableFeedback = useTableFeedback();
const menuAction = useActionLoading();
const pageLoading = tableFeedback.loading;
const menuSaving = menuAction.loading;
const tableEmptyText = tableFeedback.emptyText;
const { loadDictOptions, getOptions, getLabel, getTagType } = useDictOptions();
const statusOptions = computed(() => getOptions(COMMON_STATUS_DICT));

const { dialogVisible, isEdit, editingId, form, openCreate, openEdit, closeDialog } = useEntityDialogForm({
  createForm: () => ({
    roleName: '',
    roleCode: '',
    dataScope: DATA_SCOPE_ALL,
    status: STATUS_ENABLED
  }),
  mapForm: (row) => ({
    roleName: row.roleName,
    roleCode: row.roleCode,
    dataScope: row.dataScope,
    status: row.status
  })
});

const { deleteLoadingId, handleDelete } = useEntityDeleteAction({
  entityLabel: '角色',
  getRowLabel: (row) => row.roleName,
  deleteEntity: deleteRole,
  reloadData: loadData,
  successText: '删除角色'
});

const { submitLoading, handleSubmit: runSubmit } = useEntitySubmitAction({
  closeDialog,
  reloadData: loadData,
  getSuccessText: () => (isEdit.value ? '更新角色' : '新增角色')
});

async function loadData() {
  await tableFeedback.run(async () => {
    rows.value = await fetchRoles();
  });
}

async function handleSubmit() {
  if (!form.value.roleName || !form.value.roleCode) {
    ElMessage.warning('请填写角色名称和编码');
    return;
  }
  if (!isValidRoleCode(form.value.roleCode)) {
    ElMessage.warning('角色编码仅支持字母数字及 : _ -');
    return;
  }
  await runSubmit(async () => {
    if (isEdit.value) {
      await updateRole(editingId.value, form.value);
    } else {
      await createRole(form.value);
    }
  });
}

async function openAssignMenus(row) {
  currentRole.value = row;
  assignTypeFilter.value = 'all';
  await menuAction.run(async () => {
    const [menus, checkedIds] = await Promise.all([fetchMenus(), fetchRoleMenuIds(row.id)]);
    menuTree.value = menus;
    menuDialogVisible.value = true;
    await nextTick();
    if (menuTreeRef.value) {
      // 先清空上一次勾选，再按叶子节点回填，避免父节点 ID 触发整棵子树级联勾选。
      menuTreeRef.value.setCheckedKeys([]);
      menuTreeRef.value.setCheckedKeys(checkedIds, true);
      menuTreeRef.value.filter(assignTypeFilter.value);
    }
  });
}

function handleAssignTypeChange(value) {
  if (menuTreeRef.value) {
    menuTreeRef.value.filter(value);
  }
}

function filterAssignNode(value, data) {
  if (value === 'menu') {
    return data.menuType === MENU_TYPE_DIRECTORY || data.menuType === MENU_TYPE_PAGE;
  }
  if (value === 'button') {
    return data.menuType === MENU_TYPE_BUTTON;
  }
  return true;
}

function dataScopeText(dataScope) {
  if (dataScope === DATA_SCOPE_DEPT) {
    return '本部门';
  }
  if (dataScope === DATA_SCOPE_SELF) {
    return '仅本人';
  }
  return '全部数据';
}

async function handleSaveMenus() {
  if (!currentRole.value || !menuTreeRef.value) {
    return;
  }
  const checkedKeys = menuTreeRef.value.getCheckedKeys(false);
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys();
  const ids = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]));
  await menuAction.run(async () => {
    await assignRoleMenus(currentRole.value.id, ids);
    menuDialogVisible.value = false;
    showSuccessMessage('分配菜单');
  });
}

usePageInitializer(async () => {
  await loadDictOptions([COMMON_STATUS_DICT]);
  await loadData();
  handleSearch();
});
</script>

<style scoped>
.assign-title {
  margin-bottom: 12px;
  color: var(--jq-card-subtitle);
}

.assign-toolbar {
  margin-bottom: 12px;
}

.menu-node-row {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 10px;
}

.menu-node-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-node-perms {
  font-size: 12px;
  color: var(--jq-card-muted-text);
}

.role-menu-tree {
  border: 1px solid rgba(120, 140, 180, 0.2);
  border-radius: 10px;
  padding: 10px;
}

.table-pagination {
  margin-top: 0;
  justify-content: flex-end;
}

.role-action-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
</style>
