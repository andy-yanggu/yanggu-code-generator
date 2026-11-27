import { describe, expect, it } from 'vitest'
import { useTemplateEditTabs } from './use-template-edit-tabs'
import type { Tree } from './tree-utils'

const makeNode = (over: Partial<Tree> & { id: number }): Tree =>
	({
		fileName: 'a.ftl',
		templateType: 1,
		templateContent: '',
		originalTemplateContent: '',
		isEdited: false,
		...over
	}) as Tree

describe('discardEdits / applyEditRollback 反向语义', () => {
	it('AC-12 discardEdits 内容回基线并清标记', () => {
		const node = makeNode({ id: 1, templateContent: 'edited', originalTemplateContent: 'base', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === node.id ? node : undefined))
		domain.discardEdits([node])
		expect(node.templateContent).toBe('base')
		expect(node.isEdited).toBe(false)
	})

	it('AC-13 discardEdits 不推进基线（红线）', () => {
		const node = makeNode({ id: 1, templateContent: 'edited', originalTemplateContent: 'base', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === node.id ? node : undefined))
		domain.discardEdits([node])
		expect(node.originalTemplateContent).toBe('base')
	})

	it('AC-15 批量清标记不依赖 watch（非激活节点也逐个落标记）', () => {
		const a = makeNode({ id: 1, templateContent: 'x', originalTemplateContent: 'b1', isEdited: true })
		const b = makeNode({ id: 2, templateContent: 'y', originalTemplateContent: 'b2', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === a.id ? a : undefined))
		domain.discardEdits([a, b])
		expect([a.isEdited, b.isEdited]).toEqual([false, false])
		expect([a.templateContent, b.templateContent]).toEqual(['b1', 'b2'])
	})

	it('AC-16 保存推进基线后 discardEdits 回到新基线', () => {
		const node = makeNode({ id: 1, templateContent: 'v1', originalTemplateContent: 'v0', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === node.id ? node : undefined))
		domain.applyEditRollback([node])
		expect(node.originalTemplateContent).toBe('v1')
		node.templateContent = 'v2'
		node.isEdited = true
		domain.discardEdits([node])
		expect(node.templateContent).toBe('v1')
	})
})
