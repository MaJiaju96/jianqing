<template>
  <div class="jq-toolbar-shell">
    <div class="jq-toolbar-group jq-toolbar-group--filters">
      <slot name="filters" />
      <el-button :icon="Search" :loading="searchLoading" @click="$emit('search')">查询</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </div>
    <div class="jq-toolbar-group jq-toolbar-group--actions">
      <el-button
        v-if="showRefresh"
        class="jq-toolbar-icon-btn"
        :icon="RefreshRight"
        circle
        :loading="refreshLoading"
        @click="$emit('refresh')"
      />
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
import { RefreshRight, Search } from '@element-plus/icons-vue';

defineProps({
  searchLoading: {
    type: Boolean,
    default: false
  },
  refreshLoading: {
    type: Boolean,
    default: false
  },
  showRefresh: {
    type: Boolean,
    default: true
  }
});

defineEmits(['search', 'reset', 'refresh']);
</script>
