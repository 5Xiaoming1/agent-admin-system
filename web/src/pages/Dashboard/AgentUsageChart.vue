<template>
  <div v-loading="loading" class="chart-wrapper">
    <v-chart
      v-if="!loading"
      ref="chartRef"
      :option="chartOption"
      autoresize
      style="height: 360px"
    />
    <el-empty v-else description="暂无数据" :image-size="80" />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart } from "echarts/charts";
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { useRequest } from "@/composables";
import { getAgentUsage } from "@/api/dashboard";
import type { EChartsOption } from "echarts";

use([
  CanvasRenderer,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
]);

const { data: usageData, loading, execute } = useRequest(getAgentUsage);

execute();

const chartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
  grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
  xAxis: { type: "value" },
  yAxis: {
    type: "category",
    data: (usageData.value || [])
      .slice(0, 10)
      .map((item) => item.name)
      .reverse(),
    inverse: true,
  },
  series: [
    {
      name: "Token 用量",
      type: "bar",
      data: (usageData.value || [])
        .slice(0, 10)
        .map((item) => item.tokens)
        .reverse(),
      itemStyle: {
        color: {
          type: "linear",
          x: 0,
          y: 0,
          x2: 1,
          y2: 0,
          colorStops: [
            { offset: 0, color: "#409eff" },
            { offset: 1, color: "#79bbff" },
          ],
        },
        borderRadius: [0, 4, 4, 0],
      },
    },
  ],
}));
</script>

<style scoped>
.chart-wrapper {
  min-height: 360px;
}
</style>
