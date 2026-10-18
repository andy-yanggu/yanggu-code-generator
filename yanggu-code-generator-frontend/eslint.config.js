import tsPlugin from '@typescript-eslint/eslint-plugin'
import tsParser from '@typescript-eslint/parser'
import vuePlugin from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import unusedImports from 'eslint-plugin-unused-imports'
import eslintConfigPrettier from 'eslint-config-prettier'

export default [
	{
		ignores: ['dist/**', 'node_modules/**', 'public/**']
	},
	...vuePlugin.configs['flat/recommended'],
	{
		files: ['**/*.{ts,vue}'],
		languageOptions: {
			parser: vueParser,
			parserOptions: {
				parser: tsParser,
				ecmaVersion: 2022,
				sourceType: 'module'
			}
		},
		plugins: {
			'@typescript-eslint': tsPlugin,
			'unused-imports': unusedImports
		},
		rules: {
			curly: 'error',
			// 全局规则：统一使用 const 箭头/函数表达式定义函数，禁止 function 声明
			'func-style': ['error', 'expression'],
			'vue/multi-word-component-names': 'off',
			// CRUD 体系的 hook 由页面层创建，以 props 传给纯展示组件，父子共享同一个 reactive 对象。
			// 子组件写 formHook.dialog.visible 改的是 hook 内部的 ref，props 引用并未被替换，
			// 数据不会不同步；这属于架构约定，不是 props 突变。
			// shallowOnly 保留「整体替换 prop」（props.x = v）的检测，放行「改 prop 内部属性」（props.x.y = v）。
			'vue/no-mutating-props': ['error', { shallowOnly: true }],
			'no-unused-vars': 'off',
			'@typescript-eslint/no-unused-vars': [
				'error',
				{
					args: 'all',
					argsIgnorePattern: '^_',
					varsIgnorePattern: '^_',
					caughtErrors: 'all',
					caughtErrorsIgnorePattern: '^_'
				}
			],
			'unused-imports/no-unused-imports': 'error',
			'unused-imports/no-unused-vars': [
				'warn',
				{
					vars: 'all',
					varsIgnorePattern: '^_',
					args: 'after-used',
					argsIgnorePattern: '^_'
				}
			]
		}
	},
	eslintConfigPrettier
]
