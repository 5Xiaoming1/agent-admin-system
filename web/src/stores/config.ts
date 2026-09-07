import { defineStore } from "pinia";
import { ref } from "vue";
import type { ApiConfig } from "@/types";

export const useConfigStore = defineStore("config", () => {
  const configs = ref<ApiConfig[]>([]);
  const loading = ref(false);

  function setConfigs(list: ApiConfig[]) {
    configs.value = list;
  }

  function addConfig(config: ApiConfig) {
    configs.value.unshift(config);
  }

  function updateConfig(id: string, data: Partial<ApiConfig>) {
    const index = configs.value.findIndex((c) => c.id === id);
    if (index !== -1) {
      configs.value[index] = { ...configs.value[index], ...data };
    }
  }

  function removeConfig(id: string) {
    configs.value = configs.value.filter((c) => c.id !== id);
  }

  function toggleStatus(id: string, status: "active" | "inactive") {
    const index = configs.value.findIndex((c) => c.id === id);
    if (index !== -1) {
      configs.value[index].status = status;
    }
  }

  return {
    configs,
    loading,
    setConfigs,
    addConfig,
    updateConfig,
    removeConfig,
    toggleStatus,
  };
});
