<template>
  <div class="knowledge-page">
    <PageHeader title="知识库管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增知识库
        </el-button>
      </template>
    </PageHeader>

    <div class="knowledge-table">
      <div class="table-toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索知识库名称"
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
          <el-option label="文档" value="document" />
          <el-option label="问答" value="qa" />
          <el-option label="网页" value="web" />
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
          min-width="150"
          sortable="custom"
        />
        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="documentCount"
          label="文档数量"
          width="100"
          align="center"
        />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-popconfirm
              title="确定删除该知识库？"
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

    <KnowledgeForm
      v-model:visible="formVisible"
      :editing-item="editingItem"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { ElMessage } from "element-plus";
import type { KnowledgeBase } from "@/types";
import { useTable } from "@/composables";
import PageHeader from "@/components/PageHeader.vue";
import KnowledgeForm from "./KnowledgeForm.vue";
import { getKnowledgeList, deleteKnowledge } from "@/api/knowledge";

const typeFilter = ref("");
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
} = useTable<KnowledgeBase>({
  fetchApi: getKnowledgeList,
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

watch(typeFilter, (val) => {
  extraParams.value.type = val || undefined;
});

const formVisible = ref(false);
const editingItem = ref<KnowledgeBase | null>(null);

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
  fetchData();
}

function handleAdd() {
  editingItem.value = null;
  formVisible.value = true;
}

function handleEdit(row: KnowledgeBase) {
  editingItem.value = row;
  formVisible.value = true;
}

async function handleDelete(id: string) {
  await deleteKnowledge(id);
  ElMessage.success("删除成功");
  fetchData();
}

function typeTag(type: string): string {
  const map: Record<string, string> = {
    document: "",
    qa: "success",
    web: "warning",
  };
  return map[type] || "";
}

function typeLabel(type: string): string {
  const map: Record<string, string> = {
    document: "文档",
    qa: "问答",
    web: "网页",
  };
  return map[type] || type;
}

function statusTag(status: string): string {
  const map: Record<string, string> = {
    active: "success",
    inactive: "info",
  };
  return map[status] || "";
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    active: "启用",
    inactive: "禁用",
  };
  return map[status] || status;
}
</script>

<style scoped lang="scss">
.knowledge-page {
  .knowledge-table {
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
