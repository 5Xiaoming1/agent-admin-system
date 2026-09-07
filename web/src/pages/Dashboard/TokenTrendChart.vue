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
import { LineChart } from "echarts/charts";
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { useRequest } from "@/composables";
import { getTokenTrend } from "@/api/dashboard";
import type { EChartsOption } from "echarts";

use([
  CanvasRenderer,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
]);

const { data: trendData, loading, execute } = useRequest(getTokenTrend);

execute();

const chartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "axis" },
  grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
  xAxis: {
    type: "category",
    boundaryGap: false,
    data: trendData.value?.map((item) => item.date) || [],
  },
  yAxis: { type: "value" },
  series: [
    {
      name: "Token 用量",
      type: "line",
      smooth: true,
      areaStyle: {
        color: {
          type: "linear",
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: "rgba(64,158,255,0.3)" },
            { offset: 1, color: "rgba(64,158,255,0.05)" },
          ],
        },
      },
      data: trendData.value?.map((item) => item.value) || [],
      itemStyle: { color: "#409eff" },
    },
  ],
}));
</script>

<style scoped>
.chart-wrapper {
  min-height: 320px;
}
</style>
