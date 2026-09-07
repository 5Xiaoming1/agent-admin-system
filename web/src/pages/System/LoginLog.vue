<template>
  <div class="login-log-page">
    <PageHeader title="登录日志" />

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="statusFilter"
        placeholder="状态筛选"
        clearable
        style="width: 140px; margin-left: 12px"
      >
        <el-option label="成功" value="success" />
        <el-option label="失败" value="fail" />
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
        title="确定删除全部登录日志？"
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
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="ip" label="IP地址" width="160" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 'success' ? 'success' : 'danger'"
            size="small"
          >
            {{ row.status === "success" ? "成功" : "失败" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="message"
        label="信息"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column prop="loginTime" label="登录时间" width="170" />
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
import type { LoginLog } from "@/types";
import PageHeader from "@/components/PageHeader.vue";
import {
  getLoginLogList,
  deleteLoginLog,
  deleteAllLoginLogs,
} from "@/api/system";

const loading = ref(false);
const tableData = ref<LoginLog[]>([]);
const keyword = ref("");
const statusFilter = ref<string | null>(null);
const sortOrder = ref<"ascending" | "descending" | null>("descending");
const selectedRows = ref<LoginLog[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
});

async function fetchData() {
  loading.value = true;
  try {
    const res = await getLoginLogList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
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

function handleSelectionChange(rows: LoginLog[]) {
  selectedRows.value = rows;
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  keyword.value = "";
  statusFilter.value = null;
  pagination.page = 1;
  fetchData();
}

async function handleDelete(id: number) {
  await deleteLoginLog([id]);
  ElMessage.success("删除成功");
  fetchData();
}

async function handleBatchDelete() {
  const ids = selectedRows.value.map((row) => row.id);
  await deleteLoginLog(ids);
  ElMessage.success(`成功删除 ${ids.length} 条日志`);
  selectedRows.value = [];
  fetchData();
}

async function handleDeleteAll() {
  await deleteAllLoginLogs();
  ElMessage.success("已删除全部日志");
  selectedRows.value = [];
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.login-log-page {
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
