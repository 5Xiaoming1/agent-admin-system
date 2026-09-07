<template>
  <div class="operation-log-page">
    <PageHeader title="操作日志" />

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="moduleFilter"
        placeholder="模块筛选"
        clearable
        style="width: 140px; margin-left: 12px"
      >
        <el-option label="用户管理" value="用户管理" />
        <el-option label="智能体管理" value="智能体管理" />
        <el-option label="系统设置" value="系统设置" />
        <el-option label="知识库管理" value="知识库管理" />
        <el-option label="工具管理" value="工具管理" />
      </el-select>
      <el-button type="primary" @click="handleSearch" style="margin-left: 12px"
        >查询</el-button
      >
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="action-bar">
      <el-button
        type="danger"
        size="small"
        @click="handleBatchDelete"
        :disabled="selectedRows.length === 0"
      >
        批量删除{{ selectedRows.length > 0 ? ` (${selectedRows.length})` : "" }}
      </el-button>
      <el-popconfirm
        title="确定删除全部操作日志？"
        width="220"
        @confirm="handleDeleteAll"
      >
        <template #reference>
          <el-button type="danger" size="small">全部删除</el-button>
        </template>
      </el-popconfirm>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%"
      @sort-change="handleSortChange"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" sortable="custom" />
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="module" label="模块" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ row.module }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="actionTagType(row.action)" size="small">
            {{ row.action }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="description"
        label="描述"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column prop="ip" label="IP地址" width="160" />
      <el-table-column prop="createTime" label="操作时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-popconfirm
            title="确定删除该条日志？"
            width="200"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { OperationLog } from "@/types";
import PageHeader from "@/components/PageHeader.vue";
import {
  getOperationLogList,
  deleteOperationLog,
  deleteAllOperationLogs,
} from "@/api/system";

const loading = ref(false);
const tableData = ref<OperationLog[]>([]);
const keyword = ref("");
const moduleFilter = ref("");
const sortOrder = ref<"ascending" | "descending" | null>("descending");
const selectedRows = ref<OperationLog[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
});

function actionTagType(action: string) {
  switch (action) {
    case "新增":
      return "success";
    case "修改":
      return "warning";
    case "删除":
      return "danger";
    case "查询":
      return "info";
    default:
      return "";
  }
}

async function fetchData() {
  loading.value = true;
  try {
    const res = await getOperationLogList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      module: moduleFilter.value || undefined,
    });
    let list = res.list;

    // 默认根据ID倒序排序
    if (sortOrder.value === "ascending") {
      list = [...list].sort((a, b) => a.id - b.id);
    } else {
      list = [...list].sort((a, b) => b.id - a.id);
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
  if (prop === "id") {
    sortOrder.value = order as "ascending" | "descending" | null;
    fetchData();
  }
}

function handleSelectionChange(rows: OperationLog[]) {
  selectedRows.value = rows;
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  keyword.value = "";
  moduleFilter.value = "";
  pagination.page = 1;
  fetchData();
}

async function handleDelete(id: number) {
  await deleteOperationLog([id]);
  ElMessage.success("删除成功");
  fetchData();
}

async function handleBatchDelete() {
  const ids = selectedRows.value.map((row) => row.id);
  await deleteOperationLog(ids);
  ElMessage.success(`成功删除 ${ids.length} 条日志`);
  selectedRows.value = [];
  fetchData();
}

async function handleDeleteAll() {
  await deleteAllOperationLogs();
  ElMessage.success("已删除全部日志");
  selectedRows.value = [];
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.operation-log-page {
  .search-bar {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
  }

  .action-bar {
    margin-bottom: 12px;
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
