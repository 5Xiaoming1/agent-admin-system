import { defineStore } from "pinia";
import { ref } from "vue";
import type { KnowledgeBase } from "@/types";

export const useKnowledgeStore = defineStore("knowledge", () => {
  const knowledgeList = ref<KnowledgeBase[]>([]);
  const loading = ref(false);

  function setKnowledgeList(list: KnowledgeBase[]) {
    knowledgeList.value = list;
  }

  function addKnowledge(item: KnowledgeBase) {
    knowledgeList.value.unshift(item);
  }

  function updateKnowledgeItem(id: string, data: Partial<KnowledgeBase>) {
    const index = knowledgeList.value.findIndex((k) => k.id === id);
    if (index !== -1) {
      knowledgeList.value[index] = { ...knowledgeList.value[index], ...data };
    }
  }

  function removeKnowledge(id: string) {
    knowledgeList.value = knowledgeList.value.filter((k) => k.id !== id);
  }

  return {
    knowledgeList,
    loading,
    setKnowledgeList,
    addKnowledge,
    updateKnowledgeItem,
    removeKnowledge,
  };
});
