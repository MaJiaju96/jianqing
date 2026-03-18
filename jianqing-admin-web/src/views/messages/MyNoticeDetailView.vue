<template>
  <el-card class="jq-glass-card notice-detail-page" shadow="never" v-loading="loading">
    <template #header>
      <div class="notice-detail-head">
        <div>
          <div class="jq-page-title">消息详情</div>
          <div class="jq-page-subtitle">查看站内通知全文与阅读状态。</div>
        </div>
        <div class="notice-detail-head__actions">
          <el-button @click="router.push('/messages/mine')">返回列表</el-button>
        </div>
      </div>
    </template>

    <template v-if="detail">
      <div class="notice-hero" :class="`notice-hero--${detail.level?.toLowerCase() || 'normal'}`">
        <div class="notice-hero__meta">
          <el-tag :type="levelTagType(detail.level)">{{ levelText(detail.level) }}</el-tag>
          <span>发布时间：{{ formatDateTimeText(detail.publishedAt) }}</span>
          <span>阅读时间：{{ formatDateTimeText(detail.readAt, '刚刚已读') }}</span>
        </div>
        <div class="notice-hero__title">{{ detail.title }}</div>
      </div>

      <el-descriptions :column="2" border class="notice-detail-meta">
        <el-descriptions-item label="消息状态">{{ detail.readStatus === 1 ? '已读' : '未读' }}</el-descriptions-item>
        <el-descriptions-item label="提醒方式">{{ detail.popupEnabled === 1 ? '首页弹窗提醒' : '仅列表展示' }}</el-descriptions-item>
        <el-descriptions-item label="生效开始">{{ formatDateTimeText(detail.validFrom) }}</el-descriptions-item>
        <el-descriptions-item label="生效结束">{{ formatDateTimeText(detail.validTo) }}</el-descriptions-item>
      </el-descriptions>

      <el-card shadow="never" class="notice-content-card">
        <template #header>通知正文</template>
        <div class="notice-content">{{ detail.content }}</div>
      </el-card>

      <el-card shadow="never" class="notice-content-card">
        <template #header>备注</template>
        <div class="notice-content">{{ detail.remark || '-' }}</div>
      </el-card>
    </template>
  </el-card>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchMyNoticeDetail } from '../../api/system';
import { NOTICE_LEVEL_OPTIONS } from '../../constants/app';
import { formatDateTimeText } from '../../utils/datetime';
import { ignoreHandledError } from '../../utils/errors';

const route = useRoute();
const router = useRouter();
const detail = ref(null);
const loading = ref(false);

function levelText(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.label || level || '-';
}

function levelTagType(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.tagType || 'info';
}

async function loadDetail(id) {
  if (!id) {
    return;
  }
  loading.value = true;
  try {
    detail.value = await fetchMyNoticeDetail(id);
  } catch (error) {
    ignoreHandledError(error);
  } finally {
    loading.value = false;
  }
}

watch(() => route.params.id, (id) => loadDetail(id), { immediate: true });
</script>

<style scoped>
.notice-detail-page {
  min-height: calc(100vh - 132px);
}

.notice-detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.notice-hero {
  padding: 22px 24px;
  border-radius: 18px;
  color: #16303b;
  background: linear-gradient(135deg, #d8eef6, #f7f5e8);
}

.notice-hero--important {
  background: linear-gradient(135deg, #ffe7b8, #fff6df);
}

.notice-hero--urgent {
  background: linear-gradient(135deg, #ffd3cf, #fff0ec);
}

.notice-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  font-size: 13px;
}

.notice-hero__title {
  margin-top: 16px;
  font-size: 28px;
  font-weight: 700;
}

.notice-detail-meta,
.notice-content-card {
  margin-top: 18px;
}

.notice-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--jq-card-text, #31444d);
}
</style>
