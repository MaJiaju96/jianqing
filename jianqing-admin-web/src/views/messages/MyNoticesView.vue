<template>
  <el-card class="jq-glass-card jq-list-page" shadow="never">
    <template #header>
      <ListPageHeader :refresh-loading="loading" @search="handleListSearch" @reset="handleListReset" @refresh="loadRows">
        <template #filters>
          <el-input v-model="keywordInput" clearable placeholder="搜索标题" class="jq-toolbar-field" @keyup.enter="handleListSearch" />
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
          <el-button :disabled="batchReadLoading || !selectedUnreadCount" @click="handleBatchMarkRead(false)">批量已读</el-button>
          <el-button :disabled="batchReadLoading || !selectedUnreadCount" @click="handleBatchMarkRead(true)">批量已读(排除紧急)</el-button>
          <el-button :disabled="markAllLoading" @click="handleMarkAllRead">全部已读</el-button>
        </template>
      </ListPageHeader>
    </template>

    <div class="jq-table-panel">
      <el-table
        ref="tableRef"
        :data="pagedRows"
        row-key="noticeId"
        stripe
        :empty-text="tableEmptyText"
        height="100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />
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
        <el-table-column label="内容摘要" min-width="260" show-overflow-tooltip>
          <template #default="scope">
            {{ previewContent(scope.row.content) }}
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
        <el-table-column label="发布时间" min-width="180">
          <template #default="scope">
            {{ formatDateTimeText(scope.row.publishedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="readAt" label="阅读时间" min-width="180">
          <template #default="scope">
            {{ formatDateTimeText(scope.row.readAt) }}
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
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import ListPageHeader from '../../components/ListPageHeader.vue';
import { fetchMyNotices, markAllMyNoticesRead, markMyNoticeRead } from '../../api/system';
import { DEFAULT_LIST_PAGE_SIZE, NOTICE_LEVEL_OPTIONS, PAGE_SIZE_OPTIONS } from '../../constants/app';
import { useActionLoading, useTableFeedback } from '../../composables/useAsyncState';
import { usePageInitializer } from '../../composables/usePageInitializer';
import { useSystemListPage } from '../../composables/useSystemListPage';
import { formatDateTimeText } from '../../utils/datetime';
import { ignoreHandledError } from '../../utils/errors';
import { showSuccessMessage } from '../../utils/feedback';

const router = useRouter();
const rows = ref([]);
const readStatusInput = ref('all');
const levelInput = ref('all');
const tableFeedback = useTableFeedback();
const markAllAction = useActionLoading();
const batchReadAction = useActionLoading();
const loading = tableFeedback.loading;
const tableEmptyText = tableFeedback.emptyText;
const markAllLoading = markAllAction.loading;
const batchReadLoading = batchReadAction.loading;
const tableRef = ref(null);
const selectedRows = ref([]);
const selectedUnreadCount = computed(() => selectedRows.value.filter((item) => item.readStatus === 0).length);

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
    selectedRows.value = [];
    if (tableRef.value) {
      tableRef.value.clearSelection();
    }
  });
}

function handleSelectionChange(value) {
  selectedRows.value = Array.isArray(value) ? value : [];
}

function handleListSearch() {
  filterInput.value = {
    readStatus: readStatusInput.value,
    level: levelInput.value
  };
  runListSearch();
}

function handleListReset() {
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
      notifyNoticeRefresh();
    });
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function handleBatchMarkRead(excludeUrgent) {
  const candidates = selectedRows.value.filter((item) => {
    if (item.readStatus !== 0) {
      return false;
    }
    if (excludeUrgent && item.level === 'URGENT') {
      return false;
    }
    return true;
  });
  if (!candidates.length) {
    ElMessage.warning(excludeUrgent ? '当前选中消息里没有可标记已读的非紧急通知' : '请先勾选至少一条未读消息');
    return;
  }
  const title = excludeUrgent ? '确认将选中的非紧急消息标记为已读吗？' : '确认将选中的消息标记为已读吗？';
  try {
    await ElMessageBox.confirm(title, '提示', { type: 'warning' });
    await batchReadAction.run(async () => {
      const settled = await runBatchMarkRead(candidates, 4);
      const successCount = settled.filter((item) => item.status === 'fulfilled').length;
      const failedCount = settled.length - successCount;
      if (successCount) {
        showSuccessMessage(`已标记 ${successCount} 条消息为已读`);
      }
      if (failedCount) {
        ElMessage.warning(`${failedCount} 条消息处理失败，请刷新后重试`);
      }
      await loadRows();
      handleListSearch();
      notifyNoticeRefresh();
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

function previewContent(content) {
  if (!content) {
    return '-';
  }
  const normalized = String(content).replace(/\s+/g, ' ').trim();
  if (!normalized) {
    return '-';
  }
  return normalized.length > 80 ? `${normalized.slice(0, 80)}...` : normalized;
}

function notifyNoticeRefresh() {
  window.dispatchEvent(new CustomEvent('jq-notice-refresh'));
}

async function runBatchMarkRead(items, limit) {
  const safeLimit = Math.max(1, Number(limit) || 1);
  const results = new Array(items.length);
  let currentIndex = 0;

  async function worker() {
    while (currentIndex < items.length) {
      const index = currentIndex;
      currentIndex += 1;
      try {
        await markMyNoticeRead(items[index].noticeId);
        results[index] = { status: 'fulfilled' };
      } catch (error) {
        results[index] = { status: 'rejected', reason: error };
        ignoreHandledError(error);
      }
    }
  }

  const workers = [];
  for (let i = 0; i < Math.min(safeLimit, items.length); i += 1) {
    workers.push(worker());
  }
  await Promise.all(workers);
  return results;
}

usePageInitializer(async () => {
  await loadRows();
  handleListSearch();
});
</script>

<style scoped>
.notice-title-btn {
  max-width: 100%;
  justify-content: flex-start;
  padding: 0;
}
</style>
