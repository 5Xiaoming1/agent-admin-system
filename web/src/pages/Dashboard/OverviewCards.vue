<template>
  <el-row :gutter="20" class="overview-cards">
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic title="总 Token 用量" :value="data.totalTokens">
          <template #suffix>
            <span class="unit">tokens</span>
          </template>
        </el-statistic>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic
          title="活跃 Agent"
          :value="data.activeAgentCount"
          :value-style="{ color: '#67c23a' }"
        >
          <template #suffix>
            <span class="unit">/ {{ data.totalAgentCount }} 个</span>
          </template>
        </el-statistic>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic
          title="知识库数量"
          :value="data.knowledgeCount"
          :value-style="{ color: '#409eff' }"
        >
          <template #suffix>
            <span class="unit">个</span>
          </template>
        </el-statistic>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic
          title="工具数量"
          :value="data.toolCount"
          :value-style="{ color: '#e6a23c' }"
        >
          <template #suffix>
            <span class="unit">个</span>
          </template>
        </el-statistic>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { useRequest } from "@/composables";
import { getDashboardOverview } from "@/api/dashboard";

const data = reactive({
  totalTokens: 0,
  activeAgentCount: 0,
  totalAgentCount: 0,
  knowledgeCount: 0,
  toolCount: 0,
});

const { loading, execute } = useRequest(getDashboardOverview);

execute().then((res) => {
  if (res) {
    Object.assign(data, res);
  }
});
</script>

<style scoped lang="scss">
.overview-cards {
  margin-bottom: 20px;

  .unit {
    font-size: 14px;
    color: #909399;
    font-weight: 400;
  }
}
</style>
