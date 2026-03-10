<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <div class="jq-toolbar-shell">
        <div class="jq-toolbar-group jq-toolbar-group--filters">
          <el-input v-model="keywordInput" clearable placeholder="搜索操作人/模块/地址" class="jq-toolbar-field" @keyup.enter="handleSearch" />
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
import { RefreshRight, Search } from '@element-plus/icons-vue';
import {
  DEFAULT_AUDIT_PAGE_SIZE,
  EMPTY_FILTER_VALUE,
  PAGE_SIZE_OPTIONS,
  STATUS_ENABLED
} from '../../constants/app';
import { fetchOperLogs } from '../../api/audit';
import { useTableFeedback } from '../../composables/useAsyncState';

const rows = ref([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(DEFAULT_AUDIT_PAGE_SIZE);
const pageSizes = PAGE_SIZE_OPTIONS;
const keywordInput = ref('');
const statusFilterInput = ref(EMPTY_FILTER_VALUE);
const keyword = ref('');
const statusFilter = ref(EMPTY_FILTER_VALUE);
const tableFeedback = useTableFeedback();
const loading = tableFeedback.loading;
const tableEmptyText = tableFeedback.emptyText;

async function loadData() {
  await tableFeedback.run(async () => {
    const page = await fetchOperLogs({
      page: pageNo.value,
      size: pageSize.value,
      keyword: keyword.value.trim(),
      status: statusFilter.value
    });
    rows.value = page.records || [];
    total.value = Number(page.total || 0);
  });
}

function handleSizeChange() {
  pageNo.value = 1;
  loadData();
}

function handleSearch() {
  pageNo.value = 1;
  keyword.value = keywordInput.value.trim();
  statusFilter.value = statusFilterInput.value;
  loadData();
}

function handleReset() {
  keywordInput.value = '';
  statusFilterInput.value = EMPTY_FILTER_VALUE;
  handleSearch();
}

function handleRefresh() {
  loadData();
}

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
