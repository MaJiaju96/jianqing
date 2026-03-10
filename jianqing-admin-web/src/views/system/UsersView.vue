<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <div class="jq-toolbar-shell">
        <div class="jq-toolbar-group jq-toolbar-group--filters">
          <el-input v-model="keywordInput" clearable placeholder="搜索用户名/昵称" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="statusFilterInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="all" />
            <el-option label="启用" :value="STATUS_ENABLED" />
            <el-option label="禁用" :value="STATUS_DISABLED" />
          </el-select>
          <el-button :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <div class="jq-toolbar-group jq-toolbar-group--actions">
          <el-button class="jq-toolbar-icon-btn" :icon="RefreshRight" circle :loading="pageLoading" @click="handleRefresh" />
          <el-button v-if="canAdd" type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
        </div>
      </div>
    </template>
    <div class="jq-table-panel">
      <el-table :data="pagedRows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column label="所属部门" min-width="160">
          <template #default="scope">
            {{ deptNameMap[scope.row.deptId] || `部门#${scope.row.deptId}` }}
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === STATUS_ENABLED ? 'success' : 'danger'">{{ scope.row.status === STATUS_ENABLED ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <div class="user-action-row">
               <el-button v-if="canEdit" type="primary" link :loading="roleSaving && currentUser?.id === scope.row.id" @click="openAssignRoles(scope.row)">分配角色</el-button>
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
        :total="filteredRows.length"
        :page-sizes="pageSizes"
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="560px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password placeholder="编辑时可留空" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="真实姓名">
        <el-input v-model="form.realName" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.mobile" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="所属部门">
        <el-select v-model="form.deptId" style="width: 100%;">
          <el-option v-for="item in deptOptions" :key="item.id" :label="item.label" :value="item.id" />
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

  <el-dialog v-model="roleDialogVisible" title="分配角色" width="520px" append-to-body>
    <div class="assign-title">当前用户：{{ currentUser?.username }}</div>
    <el-checkbox-group v-model="checkedRoleIds">
      <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id">
        {{ role.roleName }}（{{ role.roleCode }}）
      </el-checkbox>
    </el-checkbox-group>
    <template #footer>
       <el-button :disabled="roleSaving" @click="roleDialogVisible = false">取消</el-button>
       <el-button type="primary" :loading="roleSaving" @click="handleSaveRoles">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { Plus, RefreshRight, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  DEFAULT_LIST_PAGE_SIZE,
  PAGE_SIZE_OPTIONS,
  STATUS_DISABLED,
  STATUS_ENABLED,
  STATUS_FILTER_ALL
} from '../../constants/app';
import {
  assignUserRoles,
  createUser,
  deleteUser,
  fetchDepts,
  fetchRoles,
  fetchUserRoleIds,
  fetchUsers,
  updateUser
} from '../../api/system';
import { useActionLoading, useRowActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { usePermissionGroup } from '../../composables/usePermissions';
import { showSuccessMessage } from '../../utils/feedback';
import { isValidEmail, isValidMobile } from '../../utils/validators';

const rows = ref([]);
const keywordInput = ref('');
const statusFilterInput = ref(STATUS_FILTER_ALL);
const keyword = ref('');
const statusFilter = ref(STATUS_FILTER_ALL);
const pageNo = ref(1);
const pageSize = ref(DEFAULT_LIST_PAGE_SIZE);
const pageSizes = PAGE_SIZE_OPTIONS;
const dialogVisible = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const roleDialogVisible = ref(false);
const currentUser = ref(null);
const allRoles = ref([]);
const checkedRoleIds = ref([]);
const deptRows = ref([]);
const form = ref({
  username: '',
  password: '',
  nickname: '',
  realName: '',
  mobile: '',
  email: '',
  deptId: 0,
  status: STATUS_ENABLED
});

const { canAdd, canEdit, canDelete } = usePermissionGroup({
  canAdd: 'system:user:add',
  canEdit: 'system:user:edit',
  canDelete: 'system:user:remove'
});

const filteredRows = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  if (!query) {
    return rows.value.filter((item) => statusFilter.value === STATUS_FILTER_ALL || item.status === statusFilter.value);
  }
  return rows.value.filter((item) => {
    const keywordMatched = item.username.toLowerCase().includes(query)
      || (item.nickname || '').toLowerCase().includes(query)
      || (item.realName || '').toLowerCase().includes(query);
    const statusMatched = statusFilter.value === STATUS_FILTER_ALL || item.status === statusFilter.value;
    return keywordMatched && statusMatched;
  });
});

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value;
  return filteredRows.value.slice(start, start + pageSize.value);
});

const tableFeedback = useTableFeedback();
const submitAction = useActionLoading();
const roleAction = useActionLoading();
const deleteAction = useRowActionLoading();
const pageLoading = tableFeedback.loading;
const submitLoading = submitAction.loading;
const roleSaving = roleAction.loading;
const deleteLoadingId = deleteAction.loadingId;
const tableEmptyText = tableFeedback.emptyText;
const deptOptions = computed(() => flattenDeptOptions(deptRows.value));
const deptNameMap = computed(() => buildDeptNameMap(deptRows.value));

watch([keyword, statusFilter], () => {
  pageNo.value = 1;
});

function resetForm() {
  form.value = {
    username: '',
    password: '',
    nickname: '',
    realName: '',
    mobile: '',
    email: '',
    deptId: resolveDefaultDeptId(),
    status: STATUS_ENABLED
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
    username: row.username,
    password: '',
    nickname: row.nickname,
    realName: row.realName,
    mobile: row.mobile,
    email: row.email,
    deptId: row.deptId || resolveDefaultDeptId(),
    status: row.status
  };
  dialogVisible.value = true;
}

async function loadData() {
  await tableFeedback.run(async () => {
    const [users, depts] = await Promise.all([fetchUsers(), fetchDepts()]);
    rows.value = users;
    deptRows.value = depts;
  });
}

function handleSearch() {
  keyword.value = keywordInput.value.trim();
  statusFilter.value = statusFilterInput.value;
}

function handleReset() {
  keywordInput.value = '';
  statusFilterInput.value = STATUS_FILTER_ALL;
  handleSearch();
}

async function handleRefresh() {
  await loadData();
}

async function handleSubmit() {
  if (!form.value.username || !form.value.nickname) {
    ElMessage.warning('请填写用户名和昵称');
    return;
  }
  if (!isEdit.value && !form.value.password) {
    ElMessage.warning('新增用户需要填写密码');
    return;
  }
  if (form.value.mobile && !isValidMobile(form.value.mobile)) {
    ElMessage.warning('手机号格式不正确');
    return;
  }
  if (form.value.email && !isValidEmail(form.value.email)) {
    ElMessage.warning('邮箱格式不正确');
    return;
  }
  await submitAction.run(async () => {
    if (isEdit.value) {
      await updateUser(editingId.value, form.value);
    } else {
      if (form.value.password.length < 6) {
        ElMessage.warning('密码长度至少 6 位');
        return;
      }
      await createUser(form.value);
    }
    dialogVisible.value = false;
    await loadData();
    showSuccessMessage(isEdit.value ? '更新用户' : '新增用户');
  });
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」吗？`, '提示', { type: 'warning' });
  await deleteAction.run(row.id, async () => {
    await deleteUser(row.id);
    await loadData();
    showSuccessMessage('删除用户');
  });
}

async function openAssignRoles(row) {
  currentUser.value = row;
  const [roles, roleIds] = await Promise.all([fetchRoles(), fetchUserRoleIds(row.id)]);
  allRoles.value = roles;
  checkedRoleIds.value = roleIds;
  roleDialogVisible.value = true;
}

async function handleSaveRoles() {
  if (!currentUser.value) {
    return;
  }
  await roleAction.run(async () => {
    await assignUserRoles(currentUser.value.id, checkedRoleIds.value);
    roleDialogVisible.value = false;
    showSuccessMessage('分配角色');
  });
}

function flattenDeptOptions(nodes, prefix = '') {
  return nodes.flatMap((node) => {
    const current = [{ id: node.id, label: `${prefix}${node.deptName}` }];
    return current.concat(flattenDeptOptions(node.children || [], `${prefix}${node.deptName} / `));
  });
}

function resolveDefaultDeptId() {
  return deptOptions.value[0]?.id ?? 0;
}

function buildDeptNameMap(nodes, prefix = '') {
  return nodes.reduce((result, node) => {
    result[node.id] = `${prefix}${node.deptName}`;
    Object.assign(result, buildDeptNameMap(node.children || [], `${prefix}${node.deptName} / `));
    return result;
  }, {});
}

onMounted(async () => {
  await loadData();
  handleSearch();
});
</script>

<style scoped>
.table-pagination {
  margin-top: 0;
  justify-content: flex-end;
}

.assign-title {
  margin-bottom: 12px;
  color: var(--jq-card-subtitle);
}

.user-action-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
</style>
