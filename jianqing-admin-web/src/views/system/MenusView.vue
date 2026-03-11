<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="pageLoading" @search="handleSearch" @reset="handleReset" @refresh="handleRefresh">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索菜单名称/权限" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="filterInput" class="jq-toolbar-select--md">
            <el-option label="全部权限" value="all" />
            <el-option label="目录/菜单" value="menu" />
            <el-option label="仅按钮权限" value="button" />
          </el-select>
        </template>
        <template #actions>
          <el-button v-if="canAdd" type="primary" :icon="Plus" @click="openCreateRoot">新增根菜单</el-button>
        </template>
      </ListPageHeader>
    </template>
    <div class="jq-table-panel">
      <el-table
        :data="pagedRows"
        row-key="id"
        stripe
        default-expand-all
        :tree-props="{ children: 'children' }"
        :empty-text="tableEmptyText"
        height="100%"
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
import { onMounted, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
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
import { useTableFeedback } from '../../composables/useAsyncState';
import { useEntityDeleteAction } from '../../composables/useEntityDeleteAction';
import { useEntityDialogForm } from '../../composables/useEntityDialogForm';
import { usePermissionGroup } from '../../composables/usePermissions';
import { useEntitySubmitAction } from '../../composables/useEntitySubmitAction';
import { useSystemListPage } from '../../composables/useSystemListPage';
import { ignoreHandledError } from '../../utils/errors';
import { isValidPermissionCode } from '../../utils/validators';
import { getMenuTypeTag as menuTypeTag, getMenuTypeText as menuTypeText, matchMenuType as matchType } from './menuMeta';

const rows = ref([]);

const { canAdd, canEdit, canDelete } = usePermissionGroup({
  canAdd: 'system:menu:add',
  canEdit: 'system:menu:edit',
  canDelete: 'system:menu:remove'
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
  initialFilterInput: 'all',
  initialFilterValue: 'all',
  defaultPageSize: DEFAULT_LIST_PAGE_SIZE,
  pageSizes: PAGE_SIZE_OPTIONS,
  loadData,
  filterRows: ({ keyword, filterValue }) => filterMenuTree(rows.value, keyword.trim().toLowerCase(), filterValue)
});

const tableFeedback = useTableFeedback();
const pageLoading = tableFeedback.loading;
const tableEmptyText = tableFeedback.emptyText;

const { dialogVisible, isEdit, editingId, form, openCreate, openEdit, closeDialog } = useEntityDialogForm({
  createForm: () => ({
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
  }),
  mapForm: (row) => ({
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
  })
});

const { deleteLoadingId, handleDelete } = useEntityDeleteAction({
  entityLabel: '菜单',
  getRowLabel: (row) => row.menuName,
  deleteEntity: deleteMenu,
  reloadData: loadData,
  successText: '删除菜单'
});

const { submitLoading, handleSubmit: runSubmit } = useEntitySubmitAction({
  closeDialog,
  reloadData: loadData,
  getSuccessText: () => (isEdit.value ? '更新菜单' : '新增菜单')
});

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

function openCreateRoot() {
  openCreate((nextForm) => {
    nextForm.parentId = 0;
  });
}

function openCreateChild(row) {
  openCreate((nextForm) => {
    nextForm.parentId = row.id;
  });
}

async function loadData() {
  await tableFeedback.run(async () => {
    rows.value = await fetchMenus();
  });
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
  await runSubmit(async () => {
    if (isEdit.value) {
      await updateMenu(editingId.value, form.value);
    } else {
      await createMenu(form.value);
    }
  });
}

onMounted(async () => {
  try {
    await loadData();
    handleSearch();
  } catch (error) {
    ignoreHandledError(error);
  }
});
</script>

<style scoped>
.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.menu-name-text {
  font-weight: 400;
}

.table-pagination {
  margin-top: 0;
  justify-content: flex-end;
}
</style>
