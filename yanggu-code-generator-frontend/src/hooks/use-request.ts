import { ref } from 'vue'

export const useRequest = () => {
	const loading = ref(false)

	const request = <T>(fn: () => Promise<T>): Promise<T> => {
		loading.value = true
		return fn().finally(() => {
			loading.value = false
		})
	}

	return { loading, request }
}
