import { get, post, put, del, instance } from "@/utils/request";
import type {
  KnowledgeBase,
  KnowledgeFile,
  PaginatedResponse,
  PaginationParams,
} from "@/types";
import { KNOWLEDGE } from "./paths";

export function getKnowledgeList(
  params: PaginationParams & { keyword?: string; type?: string },
): Promise<PaginatedResponse<KnowledgeBase>> {
  return get<PaginatedResponse<KnowledgeBase>>(KNOWLEDGE.LIST, params);
}

export function getKnowledgeById(id: string): Promise<KnowledgeBase> {
  return get<KnowledgeBase>(KNOWLEDGE.DETAIL(id));
}

export function createKnowledge(
  data: Omit<KnowledgeBase, "id" | "createdAt" | "updatedAt" | "documentCount">,
): Promise<KnowledgeBase> {
  return post<KnowledgeBase>(KNOWLEDGE.CREATE, data);
}

export function updateKnowledge(
  id: string,
  data: Partial<Omit<KnowledgeBase, "id" | "createdAt" | "updatedAt">>,
): Promise<KnowledgeBase> {
  return put<KnowledgeBase>(KNOWLEDGE.UPDATE(id), data);
}

export function deleteKnowledge(id: string): Promise<void> {
  return del(KNOWLEDGE.DELETE(id)).then(() => {});
}

export function createKnowledgeWithFile(data: {
  name: string;
  description: string;
  type: "document" | "qa" | "web";
  file?: File;
}): Promise<KnowledgeBase> {
  const formData = new FormData();
  formData.append("name", data.name);
  formData.append("description", data.description);
  formData.append("type", data.type);
  if (data.file) {
    formData.append("file", data.file);
  }

  return post<KnowledgeBase>(KNOWLEDGE.CREATE, formData as any);
}

export function getAllKnowledgeBases(): Promise<KnowledgeBase[]> {
  return get<KnowledgeBase[]>(KNOWLEDGE.ALL);
}

export function getKnowledgeFiles(
  knowledgeId: string,
): Promise<KnowledgeFile[]> {
  return get<KnowledgeFile[]>(KNOWLEDGE.FILES(knowledgeId));
}

export function uploadKnowledgeFile(
  knowledgeId: string,
  file: File,
): Promise<KnowledgeFile> {
  const formData = new FormData();
  formData.append("file", file);
  return post<KnowledgeFile>(
    KNOWLEDGE.UPLOAD_FILE(knowledgeId),
    formData as any,
  );
}

export function deleteKnowledgeFile(
  knowledgeId: string,
  fileName: string,
): Promise<void> {
  return del(KNOWLEDGE.DELETE_FILE(knowledgeId, fileName)).then(() => {});
}

export async function downloadKnowledgeFile(
  knowledgeId: string,
  fileName: string,
): Promise<void> {
  const response = await instance.get(
    KNOWLEDGE.DOWNLOAD_FILE(knowledgeId, fileName),
    {
      responseType: "blob",
    },
  );
  const blob = new Blob([response as any]);
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
