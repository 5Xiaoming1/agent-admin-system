<template>
  <div class="agent-list-page">
    <PageHeader title="Agent 管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增 Agent
        </el-button>
      </template>
    </PageHeader>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索 Agent 名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="typeFilter"
        placeholder="类型筛选"
        clearable
        style="width: 140px; margin-left: 12px"
      >
        <el-option label="对话" value="chat" />
        <el-option label="任务" value="task" />
        <el-option label="工作流" value="workflow" />
      </el-select>
      <el-select
        v-model="statusFilter"
        placeholder="状态筛选"
        clearable
        style="width: 140px; margin-left: 12px"
      >
        <el-option label="启用" value="active" />
        <el-option label="禁用" value="inactive" />
      </el-select>
      <el-button type="primary" @click="handleSearch" style="margin-left: 12px"
        >查询</el-button
      >
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%"
      @sort-change="handleSortChange"
    >
      <el-table-column
        prop="name"
        label="名称"
        min-width="140"
        sortable="custom"
      />
      <el-table-column
        prop="description"
        label="描述"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column prop="type" label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" size="small">
            {{ typeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 'active' ? 'success' : 'info'"
            size="small"
          >
            {{ row.status === "active" ? "启用" : "禁用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="tokens"
        label="Token 用量"
        width="120"
        align="right"
      >
        <template #default="{ row }">
          {{ row.tokens.toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)"
            >详情</el-button
          >
          <el-button link type="primary" @click="handleEdit(row)"
            >编辑</el-button
          >
          <el-button
            link
            :type="row.status === 'active' ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === "active" ? "禁用" : "启用" }}
          </el-button>
          <el-popconfirm
            title="确定删除该 Agent？"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <AgentForm
      v-model:visible="formVisible"
      :editing-item="editingItem"
      @success="fetchData"
    />

    <AgentDetail v-model:visible="detailVisible" :agent="detailItem" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { Agent } from "@/types";
import PageHeader from "@/components/PageHeader.vue";
import AgentForm from "./AgentForm.vue";
import AgentDetail from "./AgentDetail.vue";
import { getAgentList, deleteAgent, toggleAgentStatus } from "@/api/agents";

const loading = ref(false);
const tableData = ref<Agent[]>([]);
const keyword = ref("");
const typeFilter = ref("");
const statusFilter = ref("");
const sortOrder = ref<"ascending" | "descending" | null>(null);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
});

const formVisible = ref(false);
const editingItem = ref<Agent | null>(null);

const detailVisible = ref(false);
const detailItem = ref<Agent | null>(null);

async function fetchData() {
  loading.value = true;
  try {
    const res = await getAgentList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined,
    });
    let list = res.list;

    if (sortOrder.value) {
      if (sortOrder.value === "ascending") {
        list = [...list].sort((a, b) => a.name.localeCompare(b.name, "zh-CN"));
      } else if (sortOrder.value === "descending") {
        list = [...list].sort((a, b) => b.name.localeCompare(a.name, "zh-CN"));
      }
    }

    tableData.value = list;
    pagination.total = res.total;
  } finally {
    loading.value = false;
  }
}

function handleSortChange({
  prop,
  order,
}: {
  prop: string;
  order: string | null;
}) {
  if (prop === "name") {
    sortOrder.value = order as "ascending" | "descending" | null;
    fetchData();
  }
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  keyword.value = "";
  statusFilter.value = "";
  pagination.page = 1;
  fetchData();
}

function handleAdd() {
  editingItem.value = null;
  formVisible.value = true;
}

function handleEdit(row: Agent) {
  editingItem.value = row;
  formVisible.value = true;
}

function handleDetail(row: Agent) {
  detailItem.value = row;
  detailVisible.value = true;
}

async function handleDelete(id: string) {
  await deleteAgent(id);
  ElMessage.success("删除成功");
  fetchData();
}

async function handleToggleStatus(row: Agent) {
  const newStatus = row.status === "active" ? "inactive" : "active";
  await toggleAgentStatus(row.id, newStatus);
  ElMessage.success(newStatus === "active" ? "已启用" : "已禁用");
  fetchData();
}

function typeTag(type: string): string {
  const map: Record<string, string> = {
    chat: "",
    task: "success",
    workflow: "warning",
  };
  return map[type] || "";
}

function typeLabel(type: string): string {
  const map: Record<string, string> = {
    chat: "对话",
    task: "任务",
    workflow: "工作流",
  };
  return map[type] || type;
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.agent-list-page {
  .search-bar {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
  }

  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
