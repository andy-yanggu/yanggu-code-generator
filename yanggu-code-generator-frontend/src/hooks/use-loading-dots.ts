import type { LoadingInstance } from 'element-plus'
import type { MaybeRefOrGetter } from 'vue'

/** 点号步进周期（毫秒） */
const DOTS_INTERVAL = 400

/** 点号循环拍数：'' → '.' → '..' → '...' 共 4 拍 */
const DOTS_CYCLE = 4

/**
 * 指令形态（v-loading）的动态点号文案 hook。
 *
 * loading 为真时每 DOTS_INTERVAL 在基准文案后步进一个点号并循环，结束即暂停归位；
 * 挂载时已在 loading（如 iframe 页初始为 true）同样能启动，卸载由 useIntervalFn 自动清理。
 * 返回的文案直接绑定 element-loading-text 即可。
 *
 * @param baseText 基准文案（不含点号），支持 getter 以响应文案切换（如弹窗提交/加载两态）
 * @param loading loading 状态
 */
export const useLoadingDots = (baseText: MaybeRefOrGetter<string>, loading: MaybeRefOrGetter<boolean>) => {
	const dotCount = ref(0)
	const { pause, resume } = useIntervalFn(
		() => {
			dotCount.value = (dotCount.value + 1) % DOTS_CYCLE
		},
		DOTS_INTERVAL,
		{ immediate: false }
	)

	watch(
		() => toValue(loading),
		active => {
			if (active) {
				resume()
			} else {
				pause()
				dotCount.value = 0
			}
		},
		{ immediate: true }
	)

	return computed(() => `${toValue(baseText)}${'.'.repeat(dotCount.value)}`)
}

/**
 * API 形态（ElLoading.service）的动态点号：命令式启动，返回停止函数。
 *
 * service 的 text 只在创建时读取一次，动画须借实例的 setText 逐步刷新；
 * 调用方在关闭蒙层前先调用停止函数清掉定时器（通常放 finally：先 stop 再 close）。
 *
 * @param instance ElLoading.service 返回的实例
 * @param baseText 基准文案（不含点号）
 */
export const startLoadingDots = (instance: LoadingInstance, baseText: string) => {
	let dotCount = 0
	const timer = setInterval(() => {
		dotCount = (dotCount + 1) % DOTS_CYCLE
		instance.setText(`${baseText}${'.'.repeat(dotCount)}`)
	}, DOTS_INTERVAL)
	return () => clearInterval(timer)
}
