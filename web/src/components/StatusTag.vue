<template>
  <el-tag :type="tagType" size="small">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  status: string;
}>();

const tagType = computed(() => {
  switch (props.status) {
    case "active":
    case "ready":
      return "success";
    case "inactive":
      return "info";
    default:
      return "info";
  }
});

const label = computed(() => {
  switch (props.status) {
    case "active":
      return "启用";
    case "inactive":
      return "禁用";
    case "ready":
      return "就绪";
    default:
      return props.status;
  }
});
</script>
