<template>
  <el-card class="jq-glass-card" shadow="never">
    <template #header>
      <div class="header-row">
        <h2 class="jq-page-title">角色管理</h2>
        <div class="toolbar-right">
          <el-input v-model="keyword" clearable placeholder="搜索角色名称/编码" style="width: 220px;" />
          <el-select v-model="statusFilter" style="width: 140px;">
            <el-option label="全部状态" value="all" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button v-if="canAdd" type="primary" @click="openCreate">新增角色</el-button>
        </div>
      </div>
    </template>
    <el-table :data="pagedRows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" min-width="180" />
      <el-table-column prop="roleCode" label="角色编码" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <div class="role-action-row">
            <el-button v-if="canEdit" type="primary" link @click="openAssignMenus(scope.row)">分配菜单</el-button>
            <el-button v-if="canEdit" type="primary" link @click="openEdit(scope.row)">编辑</el-button>
            <el-button v-if="canDelete" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="table-pagination"
      background
      layout="total, sizes, prev, pager, next"
      :total="filteredRows.length"
      :page-sizes="[10, 20, 50]"
      v-model:current-page="pageNo"
      v-model:page-size="pageSize"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="520px">
    <el-form label-width="90px">
      <el-form-item label="角色名称">
        <el-input v-model="form.roleName" />
      </el-form-item>
      <el-form-item label="角色编码">
        <el-input v-model="form.roleCode" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%;">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="menuDialogVisible" title="分配菜单" width="620px">
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
      <el-button @click="menuDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSaveMenus">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  assignRoleMenus,
  createRole,
  deleteRole,
  fetchMenus,
  fetchRoleMenuIds,
  fetchRoles,
  updateRole
} from '../../api/system';
import { hasPerm } from '../../utils/permission';

const rows = ref([]);
const keyword = ref('');
const statusFilter = ref('all');
const pageNo = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const menuDialogVisible = ref(false);
const currentRole = ref(null);
const menuTree = ref([]);
const menuTreeRef = ref(null);
const assignTypeFilter = ref('all');
const form = ref({
  roleName: '',
  roleCode: '',
  status: 1
});

const canAdd = computed(() => hasPerm('system:role:add'));
const canEdit = computed(() => hasPerm('system:role:edit'));
const canDelete = computed(() => hasPerm('system:role:remove'));

const filteredRows = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  if (!query) {
    return rows.value.filter((item) => statusFilter.value === 'all' || item.status === statusFilter.value);
  }
  return rows.value.filter((item) => {
    const keywordMatched = item.roleName.toLowerCase().includes(query) || item.roleCode.toLowerCase().includes(query);
    const statusMatched = statusFilter.value === 'all' || item.status === statusFilter.value;
    return keywordMatched && statusMatched;
  });
});

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value;
  return filteredRows.value.slice(start, start + pageSize.value);
});

watch(keyword, () => {
  pageNo.value = 1;
});

watch(statusFilter, () => {
  pageNo.value = 1;
});

function resetForm() {
  form.value = {
    roleName: '',
    roleCode: '',
    status: 1
  };
}

function openCreate() {
  isEdit.value = false;
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row) {
  isEdit.value = true;
  editingId.value = row.id;
  form.value = {
    roleName: row.roleName,
    roleCode: row.roleCode,
    status: row.status
  };
  dialogVisible.value = true;
}

async function loadData() {
  rows.value = await fetchRoles();
}

async function handleSubmit() {
  if (!form.value.roleName || !form.value.roleCode) {
    ElMessage.warning('请填写角色名称和编码');
    return;
  }
  if (!/^[a-zA-Z0-9:_-]{2,64}$/.test(form.value.roleCode)) {
    ElMessage.warning('角色编码仅支持字母数字及 : _ -');
    return;
  }
  if (isEdit.value) {
    await updateRole(editingId.value, form.value);
  } else {
    await createRole(form.value);
  }
  dialogVisible.value = false;
  await loadData();
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' });
  await deleteRole(row.id);
  await loadData();
}

async function openAssignMenus(row) {
  currentRole.value = row;
  assignTypeFilter.value = 'all';
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
}

function handleAssignTypeChange(value) {
  if (menuTreeRef.value) {
    menuTreeRef.value.filter(value);
  }
}

function filterAssignNode(value, data) {
  if (value === 'menu') {
    return data.menuType === 1 || data.menuType === 2;
  }
  if (value === 'button') {
    return data.menuType === 3;
  }
  return true;
}

function menuTypeText(menuType) {
  if (menuType === 1) {
    return '目录';
  }
  if (menuType === 2) {
    return '菜单';
  }
  return '按钮';
}

function menuTypeTag(menuType) {
  if (menuType === 1) {
    return 'info';
  }
  if (menuType === 2) {
    return 'success';
  }
  return 'warning';
}

async function handleSaveMenus() {
  if (!currentRole.value || !menuTreeRef.value) {
    return;
  }
  const checkedKeys = menuTreeRef.value.getCheckedKeys(false);
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys();
  const ids = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]));
  await assignRoleMenus(currentRole.value.id, ids);
  menuDialogVisible.value = false;
}

onMounted(async () => {
  await loadData();
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
  margin-top: 14px;
  justify-content: flex-end;
}

.role-action-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
</style>
