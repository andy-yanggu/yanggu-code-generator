import { ref } from 'vue'

export const useInitForm = () => {
	const addOrUpdateRef = ref()
	const addOrUpdateHandle = (id?: number) => {
		addOrUpdateRef.value.init(id)
	}

	return {
		addOrUpdateRef,
		addOrUpdateHandle
	}
}
