import { DEFAULT_AUDIT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../constants/app';
import { useListPage } from './useListPage';

export function useAuditListPage(options) {
  return useListPage({
    mode: 'remote',
    defaultPageSize: DEFAULT_AUDIT_PAGE_SIZE,
    pageSizes: PAGE_SIZE_OPTIONS,
    fetchPage: options.fetchPage,
    buildQuery: options.buildQuery,
    syncQuery: options.syncQuery,
    resetInputs: options.resetInputs
  });
}
