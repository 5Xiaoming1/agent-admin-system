import { ref, reactive, type Ref } from "vue";
import type { PaginationParams, PaginatedResponse } from "@/types";

interface UseTableOptions<T> {
  fetchApi: (
    params: PaginationParams & Record<string, any>,
  ) => Promise<PaginatedResponse<T>>;
  defaultPageSize?: number;
  sortField?: string;
  sortOrder?: Ref<"ascending" | "descending" | null>;
}

export function useTable<T>(options: UseTableOptions<T>) {
  const { fetchApi, defaultPageSize = 10, sortField, sortOrder } = options;

  const tableData = ref<T[]>([]) as Ref<T[]>;
  const loading = ref(false);
  const pagination = reactive({
    page: 1,
    pageSize: defaultPageSize,
    total: 0,
  });
  const keyword = ref("");
  const extraParams = ref<Record<string, any>>({});

  async function fetchData() {
    loading.value = true;
    try {
      const params: PaginationParams & Record<string, any> = {
        page: pagination.page,
        pageSize: pagination.pageSize,
      };
      if (keyword.value) {
        params.keyword = keyword.value;
      }
      Object.assign(params, extraParams.value);
      const res = await fetchApi(params);
      let list = res.list;

      if (sortField && sortOrder?.value) {
        if (sortOrder.value === "ascending") {
          list = [...list].sort((a: any, b: any) => {
            const valA = a[sortField];
            const valB = b[sortField];
            if (typeof valA === "string" && typeof valB === "string") {
              return valA.localeCompare(valB, "zh-CN");
            }
            return valA > valB ? 1 : valA < valB ? -1 : 0;
          });
        } else if (sortOrder.value === "descending") {
          list = [...list].sort((a: any, b: any) => {
            const valA = a[sortField];
            const valB = b[sortField];
            if (typeof valA === "string" && typeof valB === "string") {
              return valB.localeCompare(valA, "zh-CN");
            }
            return valB > valA ? 1 : valB < valA ? -1 : 0;
          });
        }
      }

      tableData.value = list;
      pagination.total = res.total;
    } finally {
      loading.value = false;
    }
  }

  function handleSearch(val: string) {
    keyword.value = val;
    pagination.page = 1;
    fetchData();
  }

  function handlePageChange(page: number) {
    pagination.page = page;
    fetchData();
  }

  function handleSizeChange(size: number) {
    pagination.pageSize = size;
    pagination.page = 1;
    fetchData();
  }

  return {
    tableData,
    loading,
    pagination,
    keyword,
    extraParams,
    fetchData,
    handleSearch,
    handlePageChange,
    handleSizeChange,
  };
}
