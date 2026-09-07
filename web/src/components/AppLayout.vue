<template>
  <el-container class="app-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="app-aside">
      <div class="logo">
        <span v-if="!collapsed">Agent管理系统</span>
        <span v-else>A</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/data">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/agent">
          <el-icon><Service /></el-icon>
          <span>Agent管理</span>
        </el-menu-item>
        <el-menu-item index="/knowledgebase">
          <el-icon><Collection /></el-icon>
          <span>知识库管理</span>
        </el-menu-item>
        <el-menu-item index="/tools">
          <el-icon><SetUp /></el-icon>
          <span>Tools管理</span>
        </el-menu-item>
        <el-menu-item index="/setup">
          <el-icon><Setting /></el-icon>
          <span>配置管理</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/system/login-log">
            <el-icon><Document /></el-icon>
            <span>登录日志</span>
          </el-menu-item>
          <el-menu-item index="/system/operation-log">
            <el-icon><Tickets /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/system/about">
            <el-icon><InfoFilled /></el-icon>
            <span>关于系统</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapsed" :size="20">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/data' }"
              >首页</el-breadcrumb-item
            >
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <AiChat />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar
                v-if="userStore.avatar"
                :src="userStore.avatar"
                :size="32"
                shape="circle"
                class="user-avatar"
              />
              <el-avatar v-else :size="32" shape="circle" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.username || "未登录" }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="switch">
                  <el-icon><SwitchButton /></el-icon>
                  切换账号
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";
import { ElMessage, ElMessageBox } from "element-plus";
import AiChat from "@/components/AiChat.vue";

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const userStore = useUserStore();

const collapsed = computed(() => appStore.collapsed);
const activeMenu = computed(() => route.path);
const currentTitle = computed(() => (route.meta?.title as string) || "");

function toggleCollapsed() {
  appStore.toggleCollapsed();
}

function handleCommand(command: string) {
  switch (command) {
    case "profile":
      router.push("/system/profile");
      break;
    case "switch":
      ElMessageBox.confirm("确定要切换账号吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          userStore.logout();
        })
        .catch(() => {});
      break;
    case "logout":
      ElMessageBox.confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          userStore.logout();
          ElMessage.success("已退出登录");
        })
        .catch(() => {});
      break;
  }
}
</script>

<style scoped lang="scss">
.app-layout {
  height: 100vh;
}

.app-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  :deep(.el-menu) {
    border-top: 2px solid #1a2a3a !important;
    border-bottom: 2px solid #1a2a3a !important;
    border-right: none;
  }

  :deep(.el-menu-item) {
    border-bottom: 2px solid #1a2a3a !important;
    margin: 0;
  }

  :deep(.el-menu-item:last-child) {
    border-bottom: none !important;
  }

  :deep(.el-menu-item.is-active) {
    border-bottom: 2px solid #1a2a3a !important;
  }
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    .user-avatar {
      flex-shrink: 0;
    }

    .username {
      font-size: 14px;
      color: #333;
      max-width: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .collapse-btn {
    cursor: pointer;
    color: #333;
    &:hover {
      color: #409eff;
    }
  }
}

.app-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
