<template>
  <el-drawer
    :model-value="visible"
    title="Agent 详情"
    size="480px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <template v-if="agent">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">
          {{ agent.name }}
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ agent.description || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag size="small">{{ agent.type }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="agent.status === 'active' ? 'success' : 'info'"
            size="small"
          >
            {{ agent.status === "active" ? "启用" : "禁用" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="配置ApiKey">
          {{ agent.setupId }}
        </el-descriptions-item>
        <el-descriptions-item label="Token 用量">
          {{ agent.tokens.toLocaleString() }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ agent.createdAt }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ agent.updatedAt }}
        </el-descriptions-item>
      </el-descriptions>
    </template>
    <el-empty v-else description="暂无数据" />
  </el-drawer>
</template>

<script setup lang="ts">
import type { Agent } from "@/types";

defineProps<{
  visible: boolean;
  agent: Agent | null;
}>();

defineEmits<{
  (e: "update:visible", val: boolean): void;
}>();
</script>
