import { computed, ref } from 'vue';
import { fetchDictOptions } from '../api/system';
import { COMMON_STATUS_OPTIONS, DEPT_STATUS_OPTIONS } from '../constants/app';

const DICT_FALLBACK_OPTIONS = {
  sys_common_status: COMMON_STATUS_OPTIONS,
  sys_dept_status: DEPT_STATUS_OPTIONS
};

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
    return resolveOptions(dictType);
  }

  function getLabel(dictType, value, fallback = '-') {
    const labelMap = buildLabelMap(resolveOptions(dictType));
    const resolved = labelMap[String(value)];
    return resolved ?? fallback;
  }

  function getTagType(dictType, value, fallback) {
    const options = resolveOptions(dictType);
    const current = options.find((item) => String(item.value) === String(value));
    return current?.colorType || fallback;
  }

  function resolveOptions(dictType) {
    const options = dictOptionsMap.value[dictType];
    if (options && options.length) {
      return options;
    }
    return DICT_FALLBACK_OPTIONS[dictType] || [];
  }

  function buildLabelMap(options) {
    return options.reduce((result, item) => {
      result[String(item.value)] = item.label;
      return result;
    }, {});
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
