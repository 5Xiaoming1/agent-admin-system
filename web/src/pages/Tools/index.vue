<template>
  <div class="tools-page">
    <PageHeader title="Tools管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增工具
        </el-button>
      </template>
    </PageHeader>

    <div class="tools-table">
      <div class="table-toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索工具名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="typeFilter"
          placeholder="类型筛选"
          clearable
          style="width: 140px"
        >
          <el-option label="API" value="api" />
          <el-option label="函数" value="function" />
          <el-option label="内置" value="builtin" />
        </el-select>
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 140px"
        >
          <el-option label="启用" value="active" />
          <el-option label="禁用" value="inactive" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
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
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="code"
          label="代码"
          min-width="160"
          show-overflow-tooltip
        />
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
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
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
              title="确定删除该工具？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
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
    </div>

    <ToolForm
      v-model:visible="formVisible"
      :editing-item="editingItem"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { ElMessage } from "element-plus";
import type { Tool } from "@/types";
import { useTable } from "@/composables";
import PageHeader from "@/components/PageHeader.vue";
import ToolForm from "./ToolForm.vue";
import { getToolList, deleteTool, toggleToolStatus } from "@/api/tools";

const typeFilter = ref("");
const statusFilter = ref("");
const sortOrder = ref<"ascending" | "descending" | null>(null);

const { tableData, loading, pagination, keyword, extraParams, fetchData } =
  useTable<Tool>({
    fetchApi: getToolList,
    sortField: "name",
    sortOrder,
  });

watch(typeFilter, (val) => {
  extraParams.value.type = val || undefined;
});

watch(statusFilter, (val) => {
  extraParams.value.status = val || undefined;
});

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

const formVisible = ref(false);
const editingItem = ref<Tool | null>(null);

onMounted(() => {
  fetchData();
});

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  keyword.value = "";
  typeFilter.value = "";
  statusFilter.value = "";
  fetchData();
}

function handleAdd() {
  editingItem.value = null;
  formVisible.value = true;
}

function handleEdit(row: Tool) {
  editingItem.value = row;
  formVisible.value = true;
}

async function handleDelete(id: string) {
  await deleteTool(id);
  ElMessage.success("删除成功");
  fetchData();
}

async function handleToggleStatus(row: Tool) {
  const newStatus = row.status === "active" ? "inactive" : "active";
  await toggleToolStatus(row.id, newStatus);
  ElMessage.success(newStatus === "active" ? "已启用" : "已禁用");
  fetchData();
}

function typeTag(type: string): string {
  const map: Record<string, string> = {
    api: "",
    function: "success",
    builtin: "warning",
  };
  return map[type] || "";
}

function typeLabel(type: string): string {
  const map: Record<string, string> = {
    api: "API",
    function: "函数",
    builtin: "内置",
  };
  return map[type] || type;
}
</script>

<style scoped lang="scss">
.tools-page {
  .tools-table {
    background: #fff;
    border-radius: 4px;
    padding: 20px;
  }

  .table-toolbar {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .table-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
