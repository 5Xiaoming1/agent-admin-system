import { ref, type Ref } from "vue";

export function useRequest<T = any>(requestFn: (...args: any[]) => Promise<T>) {
  const data = ref<T | null>(null) as Ref<T | null>;
  const loading = ref(false);
  const error = ref<Error | null>(null);

  async function execute(...args: any[]) {
    loading.value = true;
    error.value = null;
    try {
      const result = await requestFn(...args);
      data.value = result;
      return result;
    } catch (e) {
      error.value = e as Error;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  return { data, loading, error, execute };
}
