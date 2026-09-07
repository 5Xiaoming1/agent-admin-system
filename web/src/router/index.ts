import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import AppLayout from "@/components/AppLayout.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/pages/Login/index.vue"),
    meta: { title: "登录", noAuth: true },
  },
  {
    path: "/",
    component: AppLayout,
    redirect: "/data",
    children: [
      {
        path: "data",
        name: "Dashboard",
        component: () => import("@/pages/Dashboard/index.vue"),
        meta: { title: "首页", icon: "DataAnalysis" },
      },
      {
        path: "agent",
        name: "Agent",
        component: () => import("@/pages/Agents/index.vue"),
        meta: { title: "Agent管理", icon: "Service" },
      },
      {
        path: "knowledgebase",
        name: "KnowledgeBase",
        component: () => import("@/pages/Knowledge/index.vue"),
        meta: { title: "知识库管理", icon: "Collection" },
      },
      {
        path: "tools",
        name: "Tools",
        component: () => import("@/pages/Tools/index.vue"),
        meta: { title: "Tools管理", icon: "SetUp" },
      },
      {
        path: "setup",
        name: "Config",
        component: () => import("@/pages/Config/index.vue"),
        meta: { title: "配置管理", icon: "Setting" },
      },
      {
        path: "system",
        name: "System",
        redirect: "/system/user",
        meta: { title: "系统管理", icon: "Tools" },
        children: [
          {
            path: "user",
            name: "SystemUser",
            component: () => import("@/pages/System/UserList.vue"),
            meta: { title: "用户管理", icon: "User" },
          },
          {
            path: "login-log",
            name: "SystemLoginLog",
            component: () => import("@/pages/System/LoginLog.vue"),
            meta: { title: "登录日志", icon: "Document" },
          },
          {
            path: "operation-log",
            name: "SystemOperationLog",
            component: () => import("@/pages/System/OperationLog.vue"),
            meta: { title: "操作日志", icon: "Tickets" },
          },
          {
            path: "profile",
            name: "SystemProfile",
            component: () => import("@/pages/System/Profile.vue"),
            meta: { title: "个人信息", icon: "User" },
          },
          {
            path: "about",
            name: "SystemAbout",
            component: () => import("@/pages/Home/index.vue"),
            meta: { title: "关于系统", icon: "InfoFilled" },
          },
        ],
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token");
  if (to.meta.noAuth) {
    next();
  } else if (!token) {
    next("/login");
  } else {
    next();
  }
});

export default router;
