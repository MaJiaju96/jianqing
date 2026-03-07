<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <div class="header-row">
        <h2 class="jq-page-title">菜单权限</h2>
        <div class="toolbar-right">
          <el-input v-model="keywordInput" clearable placeholder="搜索菜单名称/权限" style="width: 220px;" @keyup.enter="handleSearch" />
          <el-select v-model="typeFilterInput" style="width: 170px;">
            <el-option label="全部权限" value="all" />
            <el-option label="目录/菜单" value="menu" />
            <el-option label="仅按钮权限" value="button" />
          </el-select>
          <el-button @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button v-if="canAdd" type="primary" @click="openCreateRoot">新增根菜单</el-button>
        </div>
      </div>
    </template>
    <div class="menu-legend-row">
      <span class="legend-title">权限图例：</span>
      <el-tag size="small" type="info">目录</el-tag>
      <el-tag size="small" type="success">菜单</el-tag>
      <el-tag size="small" type="warning">按钮</el-tag>
    </div>
    <el-table
      :data="pagedRows"
      row-key="id"
      stripe
      default-expand-all
      :tree-props="{ children: 'children' }"
      :empty-text="tableEmptyText"
      :height="tableHeight"
      class="jq-menu-table"
    >
      <el-table-column label="权限名称" min-width="260" show-overflow-tooltip>
        <template #default="scope">
          <div class="menu-name-cell">
            <span class="menu-name-text">{{ scope.row.menuName }}</span>
            <el-tag size="small" effect="plain" :type="menuTypeTag(scope.row.menuType)">{{ menuTypeText(scope.row.menuType) }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="层级" width="80" align="center" />
      <el-table-column label="权限类型" width="110" align="center">
        <template #default="scope">
          <el-tag :type="menuTypeTag(scope.row.menuType)">{{ menuTypeText(scope.row.menuType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="perms" label="权限标识" min-width="180" show-overflow-tooltip />
      <el-table-column prop="routePath" label="路由路径" min-width="150" show-overflow-tooltip />
      <el-table-column prop="component" label="组件" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === STATUS_ENABLED ? 'success' : 'danger'">{{ scope.row.status === STATUS_ENABLED ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可见" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.visible === STATUS_ENABLED ? 'info' : 'warning'">{{ scope.row.visible === STATUS_ENABLED ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="scope">
           <el-button v-if="canAdd && scope.row.menuType !== MENU_TYPE_BUTTON" type="primary" link :disabled="submitLoading || deleteLoadingId === scope.row.id" @click="openCreateChild(scope.row)">新增子菜单</el-button>
           <el-button v-if="canEdit" type="primary" link :disabled="submitLoading || deleteLoadingId === scope.row.id" @click="openEdit(scope.row)">编辑</el-button>
           <el-button v-if="canDelete" type="danger" link :loading="deleteLoadingId === scope.row.id" :disabled="submitLoading" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="table-pagination"
      background
      layout="total, sizes, prev, pager, next"
      :total="filteredRows.length"
      :page-sizes="pageSizes"
      v-model:current-page="pageNo"
      v-model:page-size="pageSize"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="560px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="父菜单ID">
        <el-input-number v-model="form.parentId" :min="0" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="菜单类型">
        <el-select v-model="form.menuType" style="width: 100%;">
            <el-option :value="MENU_TYPE_DIRECTORY" label="目录" />
            <el-option :value="MENU_TYPE_PAGE" label="菜单" />
            <el-option :value="MENU_TYPE_BUTTON" label="按钮" />
        </el-select>
      </el-form-item>
      <el-form-item label="菜单名称">
        <el-input v-model="form.menuName" />
      </el-form-item>
      <el-form-item label="路由路径">
        <el-input v-model="form.routePath" />
      </el-form-item>
      <el-form-item label="组件">
        <el-input v-model="form.component" />
      </el-form-item>
      <el-form-item label="权限标识">
        <el-input v-model="form.perms" />
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="form.icon" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sortNo" :min="0" />
      </el-form-item>
      <el-form-item label="可见">
        <el-select v-model="form.visible" style="width: 100%;">
            <el-option :value="STATUS_ENABLED" label="显示" />
            <el-option :value="STATUS_DISABLED" label="隐藏" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%;">
            <el-option :value="STATUS_ENABLED" label="启用" />
            <el-option :value="STATUS_DISABLED" label="禁用" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
       <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
       <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  DEFAULT_LIST_PAGE_SIZE,
  MENU_TYPE_BUTTON,
  MENU_TYPE_DIRECTORY,
  MENU_TYPE_PAGE,
  PAGE_SIZE_OPTIONS,
  STATUS_DISABLED,
  STATUS_ENABLED
} from '../../constants/app';
import { createMenu, deleteMenu, fetchMenus, updateMenu } from '../../api/system';
import { useActionLoading, useRowActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { useAdaptiveTable } from '../../composables/useAdaptiveTable';
import { usePermissionGroup } from '../../composables/usePermissions';
import { showSuccessMessage } from '../../utils/feedback';
import { isValidPermissionCode } from '../../utils/validators';

const rows = ref([]);
const keywordInput = ref('');
const typeFilterInput = ref('all');
const keyword = ref('');
const typeFilter = ref('all');
const pageNo = ref(1);
const pageSize = ref(DEFAULT_LIST_PAGE_SIZE);
const pageSizes = PAGE_SIZE_OPTIONS;
const dialogVisible = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const form = ref({
  parentId: 0,
  menuType: MENU_TYPE_PAGE,
  menuName: '',
  routePath: '',
  component: '',
  perms: '',
  icon: '',
  sortNo: 0,
  visible: STATUS_ENABLED,
  status: STATUS_ENABLED
});

const { canAdd, canEdit, canDelete } = usePermissionGroup({
  canAdd: 'system:menu:add',
  canEdit: 'system:menu:edit',
  canDelete: 'system:menu:remove'
});

const filteredRows = computed(() => {
  const value = keyword.value.trim().toLowerCase();
  return filterMenuTree(rows.value, value, typeFilter.value);
});

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value;
  return filteredRows.value.slice(start, start + pageSize.value);
});

const tableFeedback = useTableFeedback();
const { tableHeight } = useAdaptiveTable();
const submitAction = useActionLoading();
const deleteAction = useRowActionLoading();
const pageLoading = tableFeedback.loading;
const submitLoading = submitAction.loading;
const deleteLoadingId = deleteAction.loadingId;
const tableEmptyText = tableFeedback.emptyText;

function filterMenuTree(nodes, query, filterType, level = 1) {
  return (nodes || []).reduce((result, item) => {
    const children = filterMenuTree(item.children || [], query, filterType, level + 1);
    const matched = (item.menuName || '').toLowerCase().includes(query)
      || (item.perms || '').toLowerCase().includes(query)
      || (item.routePath || '').toLowerCase().includes(query)
      || (item.component || '').toLowerCase().includes(query);
    const typeMatched = matchType(item.menuType, filterType);
    const shouldKeepCurrentNode = (matched || !query) && typeMatched;
    if (shouldKeepCurrentNode || children.length > 0) {
      result.push({
        ...item,
        level,
        children
      });
    }
    return result;
  }, []);
}

function matchType(menuType, filterType) {
  if (filterType === 'menu') {
    return menuType === MENU_TYPE_DIRECTORY || menuType === MENU_TYPE_PAGE;
  }
  if (filterType === 'button') {
    return menuType === MENU_TYPE_BUTTON;
  }
  return true;
}

function menuTypeText(menuType) {
  if (menuType === MENU_TYPE_DIRECTORY) {
    return '目录';
  }
  if (menuType === MENU_TYPE_PAGE) {
    return '菜单';
  }
  return '按钮';
}

function menuTypeTag(menuType) {
  if (menuType === MENU_TYPE_DIRECTORY) {
    return 'info';
  }
  if (menuType === MENU_TYPE_PAGE) {
    return 'success';
  }
  return 'warning';
}

function resetForm(parentId = 0) {
  form.value = {
    parentId,
    menuType: MENU_TYPE_PAGE,
    menuName: '',
    routePath: '',
    component: '',
    perms: '',
    icon: '',
    sortNo: 0,
    visible: STATUS_ENABLED,
    status: STATUS_ENABLED
  };
}

function openCreateRoot() {
  isEdit.value = false;
  editingId.value = null;
  resetForm(0);
  dialogVisible.value = true;
}

function openCreateChild(row) {
  isEdit.value = false;
  editingId.value = null;
  resetForm(row.id);
  dialogVisible.value = true;
}

function openEdit(row) {
  isEdit.value = true;
  editingId.value = row.id;
  form.value = {
    parentId: row.parentId,
    menuType: row.menuType,
    menuName: row.menuName,
    routePath: row.routePath,
    component: row.component,
    perms: row.perms,
    icon: row.icon,
    sortNo: row.sortNo,
    visible: row.visible,
    status: row.status
  };
  dialogVisible.value = true;
}

async function loadData() {
  await tableFeedback.run(async () => {
    rows.value = await fetchMenus();
  });
}

watch([keyword, typeFilter], () => {
  pageNo.value = 1;
});

function handleSearch() {
  keyword.value = keywordInput.value.trim();
  typeFilter.value = typeFilterInput.value;
}

function handleReset() {
  keywordInput.value = '';
  typeFilterInput.value = 'all';
  handleSearch();
}

async function handleSubmit() {
  if (!form.value.menuName) {
    ElMessage.warning('请填写菜单名称');
    return;
  }
  if (form.value.perms && !isValidPermissionCode(form.value.perms)) {
    ElMessage.warning('权限标识仅支持字母数字及 : _ -');
    return;
  }
  await submitAction.run(async () => {
    if (isEdit.value) {
      await updateMenu(editingId.value, form.value);
    } else {
      await createMenu(form.value);
    }
    dialogVisible.value = false;
    await loadData();
    showSuccessMessage(isEdit.value ? '更新菜单' : '新增菜单');
  });
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」吗？`, '提示', { type: 'warning' });
  await deleteAction.run(row.id, async () => {
    await deleteMenu(row.id);
    await loadData();
    showSuccessMessage('删除菜单');
  });
}

onMounted(async () => {
  await loadData();
  handleSearch();
});
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.menu-legend-row {
  margin: 4px 0 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-title {
  color: var(--jq-card-subtitle);
  font-size: 12px;
}

.jq-menu-table {
  margin-top: 6px;
}

.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.menu-name-text {
  font-weight: 500;
}

.table-pagination {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
