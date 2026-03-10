import { ElMessageBox } from 'element-plus';
import { useRowActionLoading } from './useAsyncState';
import { showSuccessMessage } from '../utils/feedback';

export function useEntityDeleteAction(options) {
  const deleteAction = useRowActionLoading();

  async function handleDelete(row) {
    try {
      await ElMessageBox.confirm(`确认删除${options.entityLabel}「${options.getRowLabel(row)}」吗？`, '提示', {
        type: 'warning'
      });
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return;
      }
      throw error;
    }
    await deleteAction.run(row.id, async () => {
      await options.deleteEntity(row.id, row);
      await options.reloadData();
      showSuccessMessage(options.successText);
    });
  }

  return {
    deleteLoadingId: deleteAction.loadingId,
    handleDelete
  };
}
