<template>
  <div class="home-page">
    <el-card class="hero-card">
      <div class="hero-content">
        <h1 class="title">关于系统</h1>
        <div class="version-info">版本 v{{ version }}</div>
      </div>
    </el-card>

    <el-card class="tech-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>技术栈</span>
        </div>
      </template>
      <div class="tech-list">
        <div class="tech-item" v-for="item in techStack" :key="item.name">
          <span class="tech-label">{{ item.name }}</span>
          <el-tag>{{ item.value }}</el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import pkg from "@/../package.json";

const version = computed(() => pkg.version);

const techStack = [
  { name: "核心框架", value: "Vue 3.5" },
  { name: "类型系统", value: "TypeScript 5" },
  { name: "构建工具", value: "Vite 6" },
  { name: "UI组件库", value: "Element Plus 2" },
  { name: "图表库", value: "ECharts 5" },
  { name: "路由", value: "Vue Router 4" },
  { name: "状态管理", value: "Pinia 2" },
  { name: "HTTP客户端", value: "Axios 1" },
  { name: "CSS方案", value: "SCSS" },
];
</script>

<style scoped lang="scss">
.home-page {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card {
  border: none;
  background: transparent;

  .hero-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .title {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }

  .version-info {
    font-size: 14px;
    color: #909399;
  }
}

.tech-card {
  border: none;
  background: transparent;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.tech-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tech-item {
  display: flex;
  align-items: center;
  gap: 16px;

  .tech-label {
    width: 100px;
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
}
</style>
