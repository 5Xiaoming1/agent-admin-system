<template>
  <div class="user-list-page">
    <PageHeader title="用户管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </template>
    </PageHeader>

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
      <el-table-column prop="id" label="ID" width="80" sortable="custom" />
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar
            v-if="row.avatar"
            :src="row.avatar"
            :size="40"
            shape="circle"
          />
          <el-avatar v-else :size="40" shape="circle">
            <el-icon><User /></el-icon>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column
        prop="admin"
        label="管理员等级"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <el-tag v-if="row.admin === 2" type="danger" size="small"
            >超级管理员</el-tag
          >
          <el-tag v-else-if="row.admin === 1" type="warning" size="small"
            >管理员</el-tag
          >
          <el-tag v-else type="info" size="small">普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? "启用" : "禁用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)"
            >编辑</el-button
          >
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? "禁用" : "启用" }}
          </el-button>
          <el-popconfirm
            v-if="row.admin !== 2"
            title="确定删除该用户？"
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

    <UserForm
      v-model:visible="formVisible"
      :editing-item="editingItem"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { User } from "@element-plus/icons-vue";
import type { User as UserType } from "@/types";
import PageHeader from "@/components/PageHeader.vue";
import UserForm from "./UserForm.vue";
import { getUserList, deleteUser, toggleUserStatus } from "@/api/system";

const loading = ref(false);
const tableData = ref<UserType[]>([]);
const keyword = ref("");
const statusFilter = ref<string | null>(null);
const sortOrder = ref<"ascending" | "descending" | null>("ascending");

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
});

const formVisible = ref(false);
const editingItem = ref<UserType | null>(null);

async function fetchData() {
  loading.value = true;
  try {
    const res = await getUserList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
    });
    let list = res.list;

    // 默认根据ID升序排序
    if (sortOrder.value === "ascending" || !sortOrder.value) {
      list = [...list].sort((a, b) => a.id - b.id);
    } else if (sortOrder.value === "descending") {
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

function handleAdd() {
  editingItem.value = null;
  formVisible.value = true;
}

function handleEdit(row: User) {
  editingItem.value = row;
  formVisible.value = true;
}

async function handleDelete(id: number) {
  await deleteUser(id);
  ElMessage.success("删除成功");
  fetchData();
}

async function handleToggleStatus(row: UserType) {
  const newStatus = row.status === 1 ? 0 : 1;
  await toggleUserStatus(row.id, newStatus as 0 | 1);
  ElMessage.success(newStatus === 1 ? "已启用" : "已禁用");
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.user-list-page {
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
