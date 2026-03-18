<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="loading" @search="handleSearch" @reset="handleReset" @refresh="loadRows">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索标题" class="jq-toolbar-field" @keyup.enter="handleSearch" />
          <el-select v-model="readStatusInput" class="jq-toolbar-select--sm">
            <el-option label="全部状态" value="all" />
            <el-option label="未读" :value="0" />
            <el-option label="已读" :value="1" />
          </el-select>
          <el-select v-model="levelInput" class="jq-toolbar-select--sm">
            <el-option label="全部级别" value="all" />
            <el-option v-for="item in NOTICE_LEVEL_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
        <template #actions>
          <el-button :disabled="markAllLoading" @click="handleMarkAllRead">全部已读</el-button>
        </template>
      </ListPageHeader>
    </template>

    <div class="jq-table-panel">
      <el-table :data="pagedRows" stripe :empty-text="tableEmptyText" height="100%">
        <el-table-column label="状态" width="96">
          <template #default="scope">
            <el-badge :is-dot="scope.row.readStatus === 0" :hidden="scope.row.readStatus === 1">
              <el-tag :type="scope.row.readStatus === 0 ? 'danger' : 'info'">{{ scope.row.readStatus === 0 ? '未读' : '已读' }}</el-tag>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="通知标题" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <el-button link type="primary" class="notice-title-btn" @click="openDetail(scope.row)">{{ scope.row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="100">
          <template #default="scope">
            <el-tag :type="levelTagType(scope.row.level)">{{ levelText(scope.row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提醒" width="90">
          <template #default="scope">
            {{ scope.row.popupEnabled === 1 ? '弹窗' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" min-width="180" />
        <el-table-column prop="readAt" label="阅读时间" min-width="180">
          <template #default="scope">
            {{ scope.row.readAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row)">查看详情</el-button>
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
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
import { fetchMyNotices, markAllMyNoticesRead } from '../../api/system';
import { DEFAULT_LIST_PAGE_SIZE, NOTICE_LEVEL_OPTIONS, PAGE_SIZE_OPTIONS } from '../../constants/app';
import { useActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { useSystemListPage } from '../../composables/useSystemListPage';
import { ignoreHandledError } from '../../utils/errors';
import { showSuccessMessage } from '../../utils/feedback';

const router = useRouter();
const rows = ref([]);
const readStatusInput = ref('all');
const levelInput = ref('all');
const tableFeedback = useTableFeedback();
const markAllAction = useActionLoading();
const loading = tableFeedback.loading;
const tableEmptyText = tableFeedback.emptyText;
const markAllLoading = markAllAction.loading;

const {
  keywordInput,
  filterInput,
  pageNo,
  pageSize,
  pageSizes,
  pagedRows,
  total,
  handleSearch: runListSearch,
  handleReset: runListReset
} = useSystemListPage({
  defaultPageSize: DEFAULT_LIST_PAGE_SIZE,
  pageSizes: PAGE_SIZE_OPTIONS,
  initialFilterInput: { readStatus: 'all', level: 'all' },
  initialFilterValue: { readStatus: 'all', level: 'all' },
  loadData: loadRows,
  filterRows: ({ keyword, filterValue }) => {
    const query = keyword.trim().toLowerCase();
    return rows.value.filter((item) => {
      const keywordMatched = !query || (item.title || '').toLowerCase().includes(query);
      const readMatched = filterValue.readStatus === 'all' || item.readStatus === filterValue.readStatus;
      const levelMatched = filterValue.level === 'all' || item.level === filterValue.level;
      return keywordMatched && readMatched && levelMatched;
    });
  }
});

async function loadRows() {
  await tableFeedback.run(async () => {
    rows.value = await fetchMyNotices();
  });
}

function handleSearch() {
  filterInput.value = {
    readStatus: readStatusInput.value,
    level: levelInput.value
  };
  runListSearch();
}

function handleReset() {
  readStatusInput.value = 'all';
  levelInput.value = 'all';
  filterInput.value = {
    readStatus: 'all',
    level: 'all'
  };
  runListReset();
}

async function handleMarkAllRead() {
  try {
    await ElMessageBox.confirm('确认将全部消息标记为已读吗？', '提示', { type: 'warning' });
    await markAllAction.run(async () => {
      await markAllMyNoticesRead();
      showSuccessMessage('已全部标记已读');
      await loadRows();
    });
  } catch (error) {
    ignoreHandledError(error);
  }
}

function levelText(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.label || level || '-';
}

function levelTagType(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.tagType || 'info';
}

function openDetail(row) {
  router.push(`/messages/mine/${row.noticeId}`);
}

usePageInitializer(async () => {
  await loadRows();
  handleSearch();
});
</script>

<style scoped>
.notice-title-btn {
  max-width: 100%;
  justify-content: flex-start;
  padding: 0;
}
</style>
