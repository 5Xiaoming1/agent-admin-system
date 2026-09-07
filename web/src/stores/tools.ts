import { defineStore } from "pinia";
import { ref } from "vue";
import type { Tool } from "@/types";

export const useToolsStore = defineStore("tools", () => {
  const toolList = ref<Tool[]>([]);
  const loading = ref(false);

  function setToolList(list: Tool[]) {
    toolList.value = list;
  }

  function addTool(item: Tool) {
    toolList.value.unshift(item);
  }

  function updateToolItem(id: string, data: Partial<Tool>) {
    const index = toolList.value.findIndex((t) => t.id === id);
    if (index !== -1) {
      toolList.value[index] = { ...toolList.value[index], ...data };
    }
  }

  function removeTool(id: string) {
    toolList.value = toolList.value.filter((t) => t.id !== id);
  }

  return {
    toolList,
    loading,
    setToolList,
    addTool,
    updateToolItem,
    removeTool,
  };
});
