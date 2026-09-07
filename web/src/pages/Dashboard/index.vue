<template>
  <div class="dashboard-page">
    <PageHeader title="数据管理" />

    <OverviewCards />

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>Token 用量趋势</span>
          </template>
          <TokenTrendChart />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>Agent 分布</span>
          </template>
          <AgentDistributionChart />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <span>Agent 用量排行</span>
          </template>
          <AgentUsageChart />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import PageHeader from "@/components/PageHeader.vue";
import OverviewCards from "./OverviewCards.vue";
import TokenTrendChart from "./TokenTrendChart.vue";
import AgentUsageChart from "./AgentUsageChart.vue";
import AgentDistributionChart from "./AgentDistributionChart.vue";
</script>

<style scoped lang="scss">
.dashboard-page {
  .chart-row {
    margin-bottom: 20px;
  }

  .chart-card {
    :deep(.el-card__header) {
      font-weight: 600;
      font-size: 16px;
    }
  }
}
</style>
