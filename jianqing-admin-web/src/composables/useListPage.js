import { computed, ref, watch } from 'vue';
import { PAGE_SIZE_OPTIONS } from '../constants/app';
import { ignoreHandledError } from '../utils/errors';
import { useTableFeedback } from './useAsyncState';

export function useListPage(options) {
  const pageNo = ref(1);
  const pageSize = ref(options.defaultPageSize);
  const pageSizes = options.pageSizes ?? PAGE_SIZE_OPTIONS;

  if (options.mode === 'remote') {
    return createRemoteListPage({
      pageNo,
      pageSize,
      pageSizes,
      ...options
    });
  }

  return createLocalListPage({
    pageNo,
    pageSize,
    pageSizes,
    ...options
  });
}

function createRemoteListPage(options) {
  const rows = ref([]);
  const total = ref(0);
  const tableFeedback = useTableFeedback();
  const loading = tableFeedback.loading;
  const tableEmptyText = tableFeedback.emptyText;

  async function loadData() {
    await tableFeedback.run(async () => {
      const page = await options.fetchPage({
        page: options.pageNo.value,
        size: options.pageSize.value,
        ...options.buildQuery()
      });
      rows.value = page.records || [];
      total.value = Number(page.total || 0);
    });
  }

  function handleSizeChange() {
    options.pageNo.value = 1;
    loadData().catch(ignoreHandledError);
  }

  function handleSearch() {
    options.pageNo.value = 1;
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
    pageNo: options.pageNo,
    pageSize: options.pageSize,
    pageSizes: options.pageSizes,
    loading,
    tableEmptyText,
    loadData,
    handleSizeChange,
    handleSearch,
    handleReset,
    handleRefresh
  };
}

function createLocalListPage(options) {
  const keywordInput = ref(options.initialKeywordInput ?? '');
  const filterInput = ref(options.initialFilterInput);
  const keyword = ref(options.initialKeyword ?? '');
  const filterValue = ref(options.initialFilterValue);

  const filteredRows = computed(() => options.filterRows({
    keyword: keyword.value,
    filterValue: filterValue.value
  }));

  const displayRows = computed(() => {
    if (options.mapRows) {
      return options.mapRows(filteredRows.value);
    }
    return filteredRows.value;
  });

  const total = computed(() => displayRows.value.length);

  const pagedRows = computed(() => {
    const start = (options.pageNo.value - 1) * options.pageSize.value;
    return displayRows.value.slice(start, start + options.pageSize.value);
  });

  watch([keyword, filterValue], () => {
    options.pageNo.value = 1;
  });

  function handleSearch() {
    keyword.value = keywordInput.value.trim();
    filterValue.value = filterInput.value;
  }

  function handleReset() {
    keywordInput.value = options.initialKeywordInput ?? '';
    filterInput.value = options.initialFilterInput;
    handleSearch();
  }

  async function handleRefresh() {
    await options.loadData().catch(ignoreHandledError);
  }

  return {
    keywordInput,
    filterInput,
    keyword,
    filterValue,
    pageNo: options.pageNo,
    pageSize: options.pageSize,
    pageSizes: options.pageSizes,
    filteredRows,
    displayRows,
    pagedRows,
    total,
    handleSearch,
    handleReset,
    handleRefresh
  };
}
