/**
 * loading 蒙层 spinner 接入常量（两种形态、单源）。
 *
 * EP 的蒙层是 createApp 独立渲染的 DOM，挂不了 Vue 组件；但 spinner 的值会作为
 * innerHTML 注入 svg.circular（element-plus loading.mjs），用 <use> 引用 sprite 里的 icon-loading，
 * 几何就仍然单源于 ui-core.ts 的 symbol（symbol 自带 viewBox，外层 svg 的 viewBox 不影响比例）。
 * custom-class 挂 custom.scss 里的步进旋转与语义色覆盖规则。
 * - directive 形态：与 v-loading 同元素 v-bind="LOADING_MASK_ATTRS"
 * - API 形态：ElLoading.service({ ...LOADING_MASK_OPTIONS })
 */
export const LOADING_MASK_OPTIONS = {
	spinner: '<use xlink:href="#icon-loading"></use>',
	customClass: 'yanggu-loading-mask'
} as const

/** directive 形态（v-loading）attribute 片段，与 API 形态同一字面量派生 */
export const LOADING_MASK_ATTRS = {
	'element-loading-spinner': LOADING_MASK_OPTIONS.spinner,
	'element-loading-custom-class': LOADING_MASK_OPTIONS.customClass
} as const