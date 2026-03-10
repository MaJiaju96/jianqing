import { useActionLoading } from './useAsyncState';
import { showSuccessMessage } from '../utils/feedback';

export function useEntitySubmitAction(options) {
  const submitAction = useActionLoading();

  async function handleSubmit(action) {
    await submitAction.run(async () => {
      await action();
      options.closeDialog();
      await options.reloadData();
      showSuccessMessage(options.getSuccessText());
    });
  }

  return {
    submitLoading: submitAction.loading,
    handleSubmit
  };
}
