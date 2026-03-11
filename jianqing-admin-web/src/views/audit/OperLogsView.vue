<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :search-loading="loading" :refresh-loading="loading" @search="handleSearch" @reset="handleReset" @refresh="handleRefresh">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索操作人/模块/地址" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="statusFilterInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="" />
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </template>
      </ListPageHeader>
    </template>
    <div class="jq-table-panel">
      <el-table :data="rows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="130" />
        <el-table-column prop="moduleName" label="模块" width="110" />
        <el-table-column prop="bizType" label="方法" width="90" />
        <el-table-column prop="requestUri" label="请求地址" min-width="220" />
        <el-table-column prop="requestIp" label="IP" width="130" />
        <el-table-column prop="costMs" label="耗时(ms)" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === STATUS_ENABLED ? 'success' : 'danger'">{{ scope.row.status === STATUS_ENABLED ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
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
import {
  EMPTY_FILTER_VALUE,
  STATUS_ENABLED
} from '../../constants/app';
import { fetchOperLogs } from '../../api/audit';
import ListPageHeader from '../../components/ListPageHeader.vue';
import { useAuditListPage } from '../../composables/useAuditListPage';
import { ignoreHandledError } from '../../utils/errors';

const keywordInput = ref('');
const statusFilterInput = ref(EMPTY_FILTER_VALUE);
const keyword = ref('');
const statusFilter = ref(EMPTY_FILTER_VALUE);
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
  fetchPage: fetchOperLogs,
  buildQuery: () => ({
    keyword: keyword.value.trim(),
    status: statusFilter.value
  }),
  syncQuery: () => {
    keyword.value = keywordInput.value.trim();
    statusFilter.value = statusFilterInput.value;
  },
  resetInputs: () => {
    keywordInput.value = '';
    statusFilterInput.value = EMPTY_FILTER_VALUE;
  }
});

onMounted(async () => {
  try {
    handleSearch();
  } catch (error) {
    ignoreHandledError(error);
  }
});
</script>

<style scoped>
.table-pagination {
  margin-top: 0;
  justify-content: flex-end;
}
</style>
