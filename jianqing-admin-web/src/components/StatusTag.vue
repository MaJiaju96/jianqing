<template>
  <el-tag :type="resolvedType">{{ resolvedText }}</el-tag>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: [Number, String, Boolean],
    required: true
  },
  enabledValue: {
    type: [Number, String, Boolean],
    default: 1
  },
  enabledText: {
    type: String,
    default: '启用'
  },
  disabledText: {
    type: String,
    default: '禁用'
  },
  enabledType: {
    type: String,
    default: 'success'
  },
  disabledType: {
    type: String,
    default: 'danger'
  }
});

const enabled = computed(() => props.status === props.enabledValue);
const resolvedText = computed(() => (enabled.value ? props.enabledText : props.disabledText));
const resolvedType = computed(() => (enabled.value ? props.enabledType : props.disabledType));
</script>
