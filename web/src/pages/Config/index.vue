<template>
  <div class="config-page">
    <PageHeader title="配置管理">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
    </PageHeader>

    <div class="config-table">
      <div class="table-toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索配置名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
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
        :data="tableData"
        v-loading="loading"
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column
          prop="name"
          label="名称"
          min-width="150"
          sortable="custom"
        />
        <el-table-column prop="apiKey" label="API Key" min-width="200">
          <template #default="{ row }">
            {{ maskKey(row.apiKey) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="baseUrl"
          label="Base URL"
          min-width="250"
          show-overflow-tooltip
        />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              link
              :type="row.status === 'active' ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === "active" ? "禁用" : "启用" }}
            </el-button>
            <el-popconfirm
              title="确定删除该配置吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
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
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <ConfigForm ref="formRef" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { ElMessage } from "element-plus";
import type { ApiConfig } from "@/types";
import { useTable } from "@/composables";
import { getConfigList, deleteConfig, toggleConfigStatus } from "@/api/config";
import PageHeader from "@/components/PageHeader.vue";
import StatusTag from "@/components/StatusTag.vue";
import ConfigForm from "./ConfigForm.vue";

const statusFilter = ref("");
const sortOrder = ref<"ascending" | "descending" | null>(null);

const {
  tableData,
  loading,
  pagination,
  keyword,
  extraParams,
  fetchData,
  handlePageChange,
  handleSizeChange,
} = useTable<ApiConfig>({
  fetchApi: getConfigList,
  sortField: "name",
  sortOrder,
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

watch(statusFilter, (val) => {
  extraParams.value.status = val || undefined;
});

const formRef = ref<InstanceType<typeof ConfigForm>>();

onMounted(() => {
  fetchData();
});

function handleAdd() {
  formRef.value?.openDialog("add");
}

function handleEdit(row: ApiConfig) {
  formRef.value?.openDialog("edit", row, row.id);
}

async function handleDelete(id: string) {
  await deleteConfig(id);
  ElMessage.success("删除成功");
  fetchData();
}

async function handleToggleStatus(row: ApiConfig) {
  const newStatus = row.status === "active" ? "inactive" : "active";
  await toggleConfigStatus(row.id, newStatus);
  ElMessage.success(`${newStatus === "active" ? "启用" : "禁用"}成功`);
  fetchData();
}

async function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  keyword.value = "";
  statusFilter.value = "";
  fetchData();
}

function maskKey(key: string): string {
  if (!key) return "****";
  return key.slice(0, 4) + "****" + key.slice(-4);
}

function formatTime(time: string): string {
  if (!time) return "-";
  return new Date(time).toLocaleString("zh-CN");
}
</script>

<style scoped lang="scss">
.config-page {
  .config-table {
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
