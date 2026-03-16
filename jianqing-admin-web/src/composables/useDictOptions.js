import { computed, ref } from 'vue';
import { fetchDictOptions } from '../api/system';

export function useDictOptions() {
  const dictOptionsMap = ref({});

  const dictLabelMap = computed(() => Object.keys(dictOptionsMap.value).reduce((result, dictType) => {
    result[dictType] = dictOptionsMap.value[dictType].reduce((labelMap, item) => {
      labelMap[String(item.value)] = item.label;
      return labelMap;
    }, {});
    return result;
  }, {}));

  async function loadDictOptions(dictTypes) {
    const uniqueTypes = Array.from(new Set((dictTypes || []).filter(Boolean)));
    if (!uniqueTypes.length) {
      return;
    }
    const entries = await Promise.all(uniqueTypes.map(async (dictType) => {
      const options = await fetchDictOptions(dictType);
      return [dictType, options];
    }));
    dictOptionsMap.value = {
      ...dictOptionsMap.value,
      ...Object.fromEntries(entries)
    };
  }

  function getOptions(dictType) {
    return dictOptionsMap.value[dictType] || [];
  }

  function getLabel(dictType, value, fallback = '-') {
    const labelMap = dictLabelMap.value[dictType] || {};
    const resolved = labelMap[String(value)];
    return resolved ?? fallback;
  }

  function getTagType(dictType, value, fallback) {
    const options = dictOptionsMap.value[dictType] || [];
    const current = options.find((item) => String(item.value) === String(value));
    return current?.colorType || fallback;
  }

  return {
    loadDictOptions,
    getOptions,
    getLabel,
    getTagType,
    dictOptionsMap,
    dictLabelMap
  };
}
