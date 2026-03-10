import { ref } from 'vue';
import { DEFAULT_AUDIT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../constants/app';
import { ignoreHandledError } from '../utils/errors';
import { useTableFeedback } from './useAsyncState';
export function useAuditListPage(options) {
  const rows = ref([]);
  const total = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(DEFAULT_AUDIT_PAGE_SIZE);
  const pageSizes = PAGE_SIZE_OPTIONS;
  const tableFeedback = useTableFeedback();
  const loading = tableFeedback.loading;
  const tableEmptyText = tableFeedback.emptyText;

  async function loadData() {
    await tableFeedback.run(async () => {
      const page = await options.fetchPage({
        page: pageNo.value,
        size: pageSize.value,
        ...options.buildQuery()
      });
      rows.value = page.records || [];
      total.value = Number(page.total || 0);
    });
  }

  function handleSizeChange() {
    pageNo.value = 1;
    loadData().catch(ignoreHandledError);
  }

  function handleSearch() {
    pageNo.value = 1;
    options.syncQuery();
    loadData().catch(ignoreHandledError);
  }

  function handleReset() {
    options.resetInputs();
    handleSearch();
  }

  function handleRefresh() {
    loadData().catch(ignoreHandledError);
  }

  return {
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
  };
}
