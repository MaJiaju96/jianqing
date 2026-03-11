import { onMounted } from 'vue';
import { ignoreHandledError } from '../utils/errors';

export function usePageInitializer(action) {
  onMounted(async () => {
    try {
      await action();
    } catch (error) {
      ignoreHandledError(error);
    }
  });
}
