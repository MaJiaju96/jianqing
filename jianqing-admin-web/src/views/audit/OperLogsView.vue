<template>
  <el-card class="jq-glass-card" shadow="never">
    <template #header>
      <div class="header-row">
        <h2 class="jq-page-title">操作日志</h2>
        <div class="toolbar-right">
          <el-input v-model="keyword" clearable placeholder="搜索操作人/模块/地址" style="width: 220px;" />
          <el-select v-model="statusFilter" style="width: 140px;">
            <el-option label="全部状态" value="" />
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
          <el-button @click="handleSearch">查询</el-button>
        </div>
      </div>
    </template>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="操作人" width="130" />
      <el-table-column prop="moduleName" label="模块" width="110" />
      <el-table-column prop="bizType" label="方法" width="90" />
      <el-table-column prop="requestUri" label="请求地址" min-width="220" />
      <el-table-column prop="requestIp" label="IP" width="130" />
      <el-table-column prop="costMs" label="耗时(ms)" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" min-width="180" />
    </el-table>
    <el-pagination
      class="table-pagination"
      background
      layout="total, sizes, prev, pager, next"
      :total="total"
      :page-sizes="[10, 20, 50]"
      v-model:current-page="pageNo"
      v-model:page-size="pageSize"
      @current-change="loadData"
      @size-change="handleSizeChange"
    />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchOperLogs } from '../../api/audit';

const rows = ref([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(20);
const keyword = ref('');
const statusFilter = ref('');

async function loadData() {
  const page = await fetchOperLogs({
    page: pageNo.value,
    size: pageSize.value,
    keyword: keyword.value.trim(),
    status: statusFilter.value
  });
  rows.value = page.records || [];
  total.value = Number(page.total || 0);
}

function handleSizeChange() {
  pageNo.value = 1;
  loadData();
}

function handleSearch() {
  pageNo.value = 1;
  loadData();
}

onMounted(async () => {
  await loadData();
});
</script>

<style scoped>
.table-pagination {
  margin-top: 14px;
  justify-content: flex-end;
}

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
</style>
