import { computed, ref } from 'vue';

export function useTableFeedback() {
  const loading = ref(false);
  const emptyText = computed(() => (loading.value ? '加载中...' : '暂无数据'));

  async function run(action) {
    loading.value = true;
    try {
      return await action();
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    emptyText,
    run
  };
}

export function useActionLoading() {
  const loading = ref(false);

  async function run(action) {
    loading.value = true;
    try {
      return await action();
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    run
  };
}

export function useRowActionLoading() {
  const loadingId = ref(null);

  async function run(id, action) {
    loadingId.value = id;
    try {
      return await action();
    } finally {
      loadingId.value = null;
    }
  }

  return {
    loadingId,
    run
  };
}
