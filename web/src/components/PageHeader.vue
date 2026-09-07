<template>
  <div class="page-header">
    <h2 class="page-title">{{ title }}</h2>
    <div class="page-actions">
      <slot name="actions" />
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
}>();
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .page-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}
</style>
