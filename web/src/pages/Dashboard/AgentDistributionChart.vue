<template>
  <div v-loading="loading" class="chart-wrapper">
    <v-chart
      v-if="!loading"
      ref="chartRef"
      :option="chartOption"
      autoresize
      style="height: 320px"
    />
    <el-empty v-else description="暂无数据" :image-size="80" />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart } from "echarts/charts";
import { TooltipComponent, LegendComponent } from "echarts/components";
import VChart from "vue-echarts";
import { useRequest } from "@/composables";
import { getAgentDistribution } from "@/api/dashboard";
import type { EChartsOption } from "echarts";

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent]);

const { data: distData, loading, execute } = useRequest(getAgentDistribution);

execute();

const chartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "item" },
  legend: { orient: "vertical", right: "5%", top: "center" },
  series: [
    {
      name: "Agent 分布",
      type: "pie",
      radius: ["45%", "75%"],
      center: ["40%", "50%"],
      itemStyle: { borderRadius: 4, borderColor: "#fff", borderWidth: 2 },
      label: { show: false },
      data: distData.value || [],
    },
  ],
}));
</script>

<style scoped>
.chart-wrapper {
  min-height: 320px;
}
</style>
