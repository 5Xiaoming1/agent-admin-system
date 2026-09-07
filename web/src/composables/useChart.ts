import { ref, onMounted, onUnmounted, type Ref } from "vue";
import type { EChartsOption } from "echarts";

export function useChart(options: Ref<EChartsOption>) {
  const chartRef =
    ref<InstanceType<(typeof import("vue-echarts"))["default"]>>();
  const chartOption = options;

  function resize() {
    chartRef.value?.resize();
  }

  function dispose() {
    chartRef.value?.dispose();
  }

  let resizeHandler: (() => void) | null = null;

  onMounted(() => {
    resizeHandler = () => resize();
    window.addEventListener("resize", resizeHandler);
  });

  onUnmounted(() => {
    if (resizeHandler) {
      window.removeEventListener("resize", resizeHandler);
    }
    dispose();
  });

  return {
    chartRef,
    chartOption,
    resize,
    dispose,
  };
}
