import { IHooksOptions } from '@/hooks/interface'
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export const useCrud = (options: IHooksOptions) => {
	const defaultOptions: IHooksOptions = {
		dataListApi: () => Promise.resolve({ data: [] }),
		deleteListApi: () => Promise.resolve(),
		createdIsNeed: true,
		isPage: true,
		primaryKey: 'id',
		exportUrl: '',
		queryForm: {},
		dataList: [],
		order: '',
		asc: false,
		pageNum: 1,
		pageSize: 10,
		total: 0,
		pageSizes: [10, 20, 50, 100, 200],
		dataListLoading: false,
		dataListSelections: []
	}

	const mergeDefaultOptions = (options: any, props: any): IHooksOptions => {
		for (const key in options) {
			if (!Object.getOwnPropertyDescriptor(props, key)) {
				props[key] = options[key]
			}
		}
		return props
	}

	// 覆盖默认值
	const state = mergeDefaultOptions(defaultOptions, options)

	onMounted(() => {
		if (state.createdIsNeed) {
			query()
		}
	})

	const query = () => {
		if (!state.dataListApi) {
			return
		}

		state.dataListLoading = true

		const pageQueryForm = {
			...state.queryForm,
			pageNum: state.pageNum,
			pageSize: state.pageSize
		}
		if (state.order) {
			pageQueryForm.orderItemList = [{ column: state.order, asc: state.asc }]
		}
		state.dataListApi(pageQueryForm).then((res: any) => {
			state.dataList = state.isPage ? res.data.records : res.data
			state.total = state.isPage ? res.data.total : 0
		})

		state.dataListLoading = false
	}

	// 加载数据列表
	const getDataList = () => {
		state.pageNum = 1
		query()
	}

	//pageSize发生变化
	const sizeChangeHandle = (val: number) => {
		state.pageNum = 1
		state.pageSize = val
		query()
	}

	//pageNum发生变化
	const currentChangeHandle = (val: number) => {
		state.pageNum = val
		query()
	}

	// 多选
	const selectionChangeHandle = (selections: any[]) => {
		state.dataListSelections = selections.map((item: any) => state.primaryKey && item[state.primaryKey])
	}

	// 排序
	const sortChangeHandle = (data: any) => {
		const order = data.order
		if (order === 'ascending') {
			state.order = data.prop
			state.asc = true
		} else if (order === 'descending') {
			state.order = data.prop
			state.asc = false
		} else {
			state.order = ''
		}
		getDataList()
	}

	const deleteBatchHandle = (key?: number | string) => {
		let data: any[] = []
		if (key) {
			data = [key]
		} else {
			data = state.dataListSelections ? state.dataListSelections : []

			if (data.length === 0) {
				ElMessage.warning('请选择删除记录')
				return
			}
		}

		ElMessageBox.confirm('确定进行删除操作?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				if (state.deleteListApi) {
					state.deleteListApi({ data }).then(() => {
						ElMessage.success('删除成功')

						query()
					})
				}
			})
			.catch(() => {})
	}

	return { getDataList, sizeChangeHandle, currentChangeHandle, selectionChangeHandle, sortChangeHandle, deleteBatchHandle }
}
