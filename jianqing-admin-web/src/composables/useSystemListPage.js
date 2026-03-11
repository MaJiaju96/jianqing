import { DEFAULT_LIST_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../constants/app';
import { useListPage } from './useListPage';

export function useSystemListPage(options) {
  return useListPage({
    mode: 'local',
    defaultPageSize: options.defaultPageSize ?? DEFAULT_LIST_PAGE_SIZE,
    pageSizes: options.pageSizes ?? PAGE_SIZE_OPTIONS,
    initialKeywordInput: options.initialKeywordInput,
    initialFilterInput: options.initialFilterInput,
    initialKeyword: options.initialKeyword,
    initialFilterValue: options.initialFilterValue,
    filterRows: options.filterRows,
    mapRows: options.mapRows,
    loadData: options.loadData
  });
}
