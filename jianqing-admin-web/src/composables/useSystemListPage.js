import { computed, ref, watch } from 'vue';
import { DEFAULT_LIST_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../constants/app';
import { ignoreHandledError } from '../utils/errors';

export function useSystemListPage(options) {
  const keywordInput = ref(options.initialKeywordInput ?? '');
  const filterInput = ref(options.initialFilterInput);
  const keyword = ref(options.initialKeyword ?? '');
  const filterValue = ref(options.initialFilterValue);
  const pageNo = ref(1);
  const pageSize = ref(options.defaultPageSize ?? DEFAULT_LIST_PAGE_SIZE);
  const pageSizes = options.pageSizes ?? PAGE_SIZE_OPTIONS;

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
    const start = (pageNo.value - 1) * pageSize.value;
    return displayRows.value.slice(start, start + pageSize.value);
  });

  watch([keyword, filterValue], () => {
    pageNo.value = 1;
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
    pageNo,
    pageSize,
    pageSizes,
    filteredRows,
    displayRows,
    pagedRows,
    total,
    handleSearch,
    handleReset,
    handleRefresh
  };
}
