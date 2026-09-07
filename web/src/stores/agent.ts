import { defineStore } from "pinia";
import { ref } from "vue";
import type { Agent } from "@/types";

export const useAgentStore = defineStore("agent", () => {
  const agents = ref<Agent[]>([]);
  const loading = ref(false);

  function setAgents(list: Agent[]) {
    agents.value = list;
  }

  function addAgent(agent: Agent) {
    agents.value.unshift(agent);
  }

  function updateAgent(id: string, data: Partial<Agent>) {
    const index = agents.value.findIndex((a) => a.id === id);
    if (index !== -1) {
      agents.value[index] = { ...agents.value[index], ...data };
    }
  }

  function removeAgent(id: string) {
    agents.value = agents.value.filter((a) => a.id !== id);
  }

  return { agents, loading, setAgents, addAgent, updateAgent, removeAgent };
});
