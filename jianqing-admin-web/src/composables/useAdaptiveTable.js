import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

export function useAdaptiveTable(options = {}) {
  const offset = options.offset ?? 340;
  const minHeight = options.minHeight ?? 260;
  const viewportHeight = ref(typeof window === 'undefined' ? 900 : window.innerHeight);
  const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth);

  function updateViewportSize() {
    viewportHeight.value = window.innerHeight;
    viewportWidth.value = window.innerWidth;
  }

  onMounted(() => {
    updateViewportSize();
    window.addEventListener('resize', updateViewportSize);
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', updateViewportSize);
  });

  const resolvedOffset = computed(() => {
    if (viewportWidth.value <= 768) {
      return offset + 170;
    }
    if (viewportWidth.value <= 1280) {
      return offset + 70;
    }
    return offset;
  });

  const tableHeight = computed(() => `${Math.max(minHeight, viewportHeight.value - resolvedOffset.value)}px`);

  return {
    tableHeight
  };
}
