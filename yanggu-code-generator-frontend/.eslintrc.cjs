module.exports = {
	root: true,
	env: {
		node: true
	},
	extends: [
		'plugin:vue/vue3-recommended',
		'plugin:prettier/recommended',
		'@vue/typescript/recommended' // 添加 TypeScript 推荐规则
	],
	parser: 'vue-eslint-parser',
	parserOptions: {
		parser: '@typescript-eslint/parser',
		ecmaVersion: 2022,
		ecmaFeatures: {
			jsx: true
		},
		sourceType: 'module' // 添加模块化支持
	},
	plugins: [
		'@typescript-eslint',
		'unused-imports' // 添加未使用导入检测插件
	],
	rules: {
		curly: 'error',
		'vue/multi-word-component-names': 'off',
		// 添加未使用变量检测规则
		'no-unused-vars': 'off', // 禁用基础规则
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
		// 添加未使用导入检测规则
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
}
