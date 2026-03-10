<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <div class="jq-toolbar-shell">
        <div class="jq-toolbar-group jq-toolbar-group--filters">
          <el-input v-model="keywordInput" clearable placeholder="搜索用户名/IP/信息" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="loginTypeFilterInput" class="jq-toolbar-select--sm">
            <el-option label="全部方式" value="" />
            <el-option label="密码" value="password" />
          </el-select>
          <el-select v-model="statusFilterInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="" />
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
          <el-button :icon="Search" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <div class="jq-toolbar-group jq-toolbar-group--actions">
          <el-button class="jq-toolbar-icon-btn" :icon="RefreshRight" circle :loading="loading" @click="handleRefresh" />
        </div>
      </div>
    </template>
    <div class="jq-table-panel">
      <el-table :data="rows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="loginType" label="登录方式" width="100" />
        <el-table-column prop="loginIp" label="IP" width="130" />
        <el-table-column prop="userAgent" label="UA" min-width="260" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === STATUS_ENABLED ? 'success' : 'danger'">{{ scope.row.status === STATUS_ENABLED ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="信息" min-width="140" />
        <el-table-column prop="createdAt" label="时间" min-width="180" />
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
        @current-change="loadData"
        @size-change="handleSizeChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { RefreshRight, Search } from '@element-plus/icons-vue';
import {
  EMPTY_FILTER_VALUE,
  STATUS_ENABLED
} from '../../constants/app';
import { fetchLoginLogs } from '../../api/audit';
import { useAuditListPage } from '../../composables/useAuditListPage';

const keywordInput = ref('');
const statusFilterInput = ref(EMPTY_FILTER_VALUE);
const loginTypeFilterInput = ref(EMPTY_FILTER_VALUE);
const keyword = ref('');
const statusFilter = ref(EMPTY_FILTER_VALUE);
const loginTypeFilter = ref(EMPTY_FILTER_VALUE);
const {
  rows,
  total,
  pageNo,
  pageSize,
  pageSizes,
  loading,
  tableEmptyText,
  loadData,
  handleSizeChange,
  handleSearch,
  handleReset,
  handleRefresh
} = useAuditListPage({
  fetchPage: fetchLoginLogs,
  buildQuery: () => ({
    keyword: keyword.value.trim(),
    status: statusFilter.value,
    loginType: loginTypeFilter.value
  }),
  syncQuery: () => {
    keyword.value = keywordInput.value.trim();
    statusFilter.value = statusFilterInput.value;
    loginTypeFilter.value = loginTypeFilterInput.value;
  },
  resetInputs: () => {
    keywordInput.value = '';
    statusFilterInput.value = EMPTY_FILTER_VALUE;
    loginTypeFilterInput.value = EMPTY_FILTER_VALUE;
  }
});

onMounted(async () => {
  handleSearch();
});
</script>

<style scoped>
.table-pagination {
  margin-top: 0;
  justify-content: flex-end;
}
</style>
