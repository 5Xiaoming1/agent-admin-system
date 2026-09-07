export interface PaginationParams {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: string;
}

export interface PaginatedResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface ApiResponse<T = any> {
  code: number;
  data: T;
  message: string;
}
