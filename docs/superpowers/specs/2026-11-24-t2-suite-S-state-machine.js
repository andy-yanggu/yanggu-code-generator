/* =============================================================================
 * T2 双跑对照 · Suite S（状态机批次：F02 / F03 / F07 / F14 / F16 / F19）
 * 日期：2026-11-24    靶点：模板组 80「单体java项目模板_复制」（列表第 4 行 tbody tr[3]）
 *
 * 用法（每个阶段是一段独立的 async 箭头函数，可单独粘进浏览器 MCP 的 function 参数）
 *   1) 先跑 S0：它打开抽屉、做选择器预检门禁、把公共 helper 挂到 window.__H，并记住入口按钮文案；
 *   2) 之后按 S1 → S2 → S3 → S4 顺序跑；任一段可单独重跑（开头都会 forceCloseMain 取干净起点）；
 *   3) 换版本只改 S0 里的 BTN 一行：v2 = '模板v2'，v1 = '模板'。同一份 S1~S4 源码不动。
 *
 * 安全不变量（务必保持）
 *   - 清理逻辑只点「取消 / 否(N)」，永不点「是(Y) / 确定」：这两个是写库/重载分支；
 *   - 写库只发生在 S1（单条 updateContent ×2）与 S3（批量 updateContentBatch ×1 + 复原 updateContent ×2），
 *     每段末尾都把内容改回基线并自证 hash 复原；其余阶段零写入；
 *   - S0 门禁 P_gate != PASS 时，S1/S3 直接拒绝执行写操作并只返回门禁值。
 *
 * 指纹口径
 *   - F（行为指纹）：只放两版必须逐字相同的业务断言项，拼成 k=v|k=v 后 FNV-1a；
 *   - I（身份自检）：放版本身份项（.tree-panel 可见数、holder、菜单 DOM 类名等），允许不同，单独哈希；
 *   - 两段串都要记录；行为串 fpBehav 必须完全相同，身份串差异必须能逐项解释。
 * ========================================================================== */

/* -----------------------------------------------------------------------------
 * STAGE S0 — 预检 + 基线快照（零写入）
 * ========================================================================== */
async () => {
	const BTN = '模板v2' // ← 唯一需要按版本修改的一行：v1 用 '模板'，v2 用 '模板v2'
	const ROW_TEXT = '单体java项目模板_复制' // 组 80；行号不固定，靠本串在可见行中定位
	const MARK = '@@SAVE@@' // 8 字符，可完全复原
	const sleep = ms => new Promise(r => setTimeout(r, ms))
	const vis = e => !!e && e.checkVisibility()
	const fnv = s => {
		let h = 0x811c9dc5
		for (let i = 0; i < s.length; i++) {
			h ^= s.charCodeAt(i)
			h = Math.imul(h, 0x01000193) >>> 0
		}
		return h.toString(16).toUpperCase().padStart(8, '0')
	}
	// 短指纹：内容/文案一律哈希 + 长度，避免把正文塞进指纹串
	const hs = s => (s == null ? 'NULL' : fnv(String(s)) + '_' + String(s).length)
	const lines = s => String(s == null ? '' : s).split('\n').length

	const H = { BTN: BTN, MARK: MARK, fnv: fnv, hs: hs, vis: vis, sleep: sleep, F: [], I: [] }
	window.__H = H

	H.btn = scope => text => [...scope.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim() === text)
	H.mainDrawer = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0)
	H.testDrawer = () => [...document.querySelectorAll('.el-drawer.template-test-drawer')].filter(vis)[0]
	H.box = () => [...document.querySelectorAll('.el-message-box')].filter(vis)[0]
	H.boxMsg = () => {
		const b = H.box()
		if (!b) {
			return null
		}
		const m = b.querySelector('.el-message-box__message')
		return m ? m.innerText.trim() : ''
	}
	H.boxBtns = () => (H.box() ? [...H.box().querySelectorAll('button')].map(b => (b.innerText.trim() === '' ? 'x' : b.innerText.trim())).join(',') : 'NO_BOX')
	// 只按「取消 / 否(N)」丢弃，永不触发写库分支
	H.boxBtn = which => {
		const b = H.box()
		if (!b) {
			return null
		}
		const btns = [...b.querySelectorAll('button')].filter(vis)
		if (which === 'x') {
			return btns.find(x => x.innerText.trim() === '') || b.querySelector('.el-message-box__headerbtn')
		}
		const want = { yes: '是(Y)', no: '否(N)', ok: '确定', cancel: '取消' }[which]
		return btns.find(x => x.innerText.trim() === want)
	}
	H.killBoxes = async () => {
		let n = 0
		for (let i = 0; i < 4; i++) {
			if (!H.box()) {
				break
			}
			const b = H.boxBtn('cancel') || H.boxBtn('no')
			if (!b) {
				break
			}
			b.click()
			n++
			await sleep(900)
		}
		return n
	}
	H.closeDrawerX = drawer => {
		const x = drawer.querySelector('button[aria-label*="关闭"]') || drawer.querySelector('.el-drawer__close-btn')
		if (x) {
			x.click()
		}
	}
	// 干净起点：关子抽屉/主抽屉（脏则丢弃），确保 destroy-on-close 后每次都是 fresh
	H.forceCloseMain = async () => {
		for (let i = 0; i < 4; i++) {
			const td = H.testDrawer()
			if (td) {
				H.closeDrawerX(td)
				await sleep(700)
				await H.killBoxes()
			}
			const d = H.mainReady() || H.mainDrawer()
			if (!d) {
				break
			}
			H.closeDrawerX(d)
			await sleep(800)
			await H.killBoxes()
			// 等抽屉真正卸载再返回，避免紧接着的重开撞上离场动画
			for (let k = 0; k < 10; k++) {
				await sleep(200)
				if (!H.mainReady() && !H.mainDrawer()) {
					break
				}
			}
			await sleep(400)
		}
		return { drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length, boxes: [...document.querySelectorAll('.el-message-box')].filter(vis).length, ready: !!H.mainReady() }
	}
	// 抽屉已真正挂载且内容就绪：destroy-on-close 下 .el-tabs__item 只在挂载期存在，
	// 用它做就绪信号可以避开入场动画 opacity=0 被 checkVisibility() 误判不可见的 race
	H.mainReady = () =>
		[...document.querySelectorAll('.el-drawer')].find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0 && d.querySelector('.el-tabs__item'))
	H.open = async () => {
		const rows = [...document.querySelectorAll('tbody tr')].filter(vis)
		// 按组名大小写敏感定位，不能写死行号：列表按更新时间倒序，今天新建的「单体Java项目模板_复制v2」（大写 J）
		// 插到首行会把组 80 顶下去，而行名子串大小写不一致所以不会误中
		const idx = rows.findIndex(r => r.innerText.indexOf(ROW_TEXT) >= 0)
		const row = idx < 0 ? null : rows[idx]
		if (!row) {
			return { ok: false, why: 'ROW_MISMATCH:' + rows.slice(0, 6).map(r => r.innerText.replace(/\s+/g, '').slice(0, 12)).join('/') }
		}
		H.rowIdx = idx
		const b = H.btn(row)(BTN)
		if (!b) {
			return { ok: false, why: 'BTN_MISSING:' + BTN }
		}
		// 关闭动画与重开之间可能有竞态：点一次没起来就补点，最多 3 次，每次轮询 3s
		for (let attempt = 1; attempt <= 3; attempt++) {
			b.click()
			for (let i = 0; i < 15; i++) {
				await sleep(200)
				const d = H.mainReady()
				if (d) {
					await sleep(600)
					return { ok: true, drawer: H.mainDrawer() || d, attempt: attempt }
				}
			}
		}
		return { ok: false, why: 'DRAWER_NOT_READY_AFTER_3_CLICKS' }
	}
	H.dot = el => {
		const b = el ? el.querySelector('.el-badge__content.is-dot') : null
		if (!b) {
			return 'NO_BADGE'
		}
		return b.style.display === 'none' ? 'off' : 'on'
	}
	H.treeDot = id => H.dot(document.querySelector('[data-key="' + id + '"]'))
	H.tabDot = id => H.dot(document.querySelector('#tab-' + id))
	H.tabSeq = drawer =>
		[...drawer.querySelectorAll('.el-tabs__item')]
			.map(t => t.id.replace(/^tab-/, '') + (t.classList.contains('is-active') ? '*' : '') + '=' + t.innerText.trim().replace(/\s+/g, ''))
			.join(',')
	H.cm = scope => [...scope.querySelectorAll('.cm-content')].filter(vis)[0]
	H.mark = async (drawer, text) => {
		const cm = H.cm(drawer)
		if (!cm) {
			return 'NO_CM'
		}
		cm.focus()
		const ok = document.execCommand('insertText', false, text)
		await sleep(500)
		return ok ? 'OK' : 'FAILED'
	}
	H.unmark = async (drawer, n) => {
		const cm = H.cm(drawer)
		if (!cm) {
			return 'NO_CM'
		}
		cm.focus()
		for (let i = 0; i < n; i++) {
			document.execCommand('delete')
		}
		await sleep(500)
		return 'OK'
	}
	H.saveBtn = drawer => H.btn(drawer)('保存')
	H.saveState = drawer => {
		const b = H.saveBtn(drawer)
		return b ? (b.disabled ? 'dis' : 'ena') : 'NO_BTN'
	}
	H.toasts = () => [...document.querySelectorAll('.el-message')].filter(vis).map(m => m.innerText.trim().replace(/\s+/g, '')).join('|')
	// toast  spy：ElMessage 多为 500ms 自销，阶段末尾采样会全部漏掉，故边跑边记录出现过的文案
	H.TOASTS = []
	H.pushToast = t => {
		t = (t || '').trim().replace(/\s+/g, '')
		if (t && H.TOASTS[H.TOASTS.length - 1] !== t) {
			H.TOASTS.push(t)
		}
	}
	H.resetToasts = () => {
		H.TOASTS.length = 0
	}
	H.toastLog = () => H.hs(H.TOASTS.join(','))
	if (window.__TOAST_OBS) {
		window.__TOAST_OBS.disconnect()
	}
	window.__TOAST_OBS = new MutationObserver(muts => {
		for (const m of muts) {
			for (const n of m.addedNodes) {
				if (n.nodeType !== 1) {
					continue
				}
				if (n.classList && n.classList.contains('el-message')) {
					H.pushToast(n.innerText)
				}
				if (n.querySelectorAll) {
					;[...n.querySelectorAll('.el-message')].forEach(e => H.pushToast(e.innerText))
				}
			}
		}
	})
	window.__TOAST_OBS.observe(document.body, { childList: true, subtree: true })
	H.setInput = (input, v) => {
		const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set
		setter.call(input, v)
		input.dispatchEvent(new Event('input', { bubbles: true }))
	}
	H.searchInput = drawer => [...drawer.querySelectorAll('.el-input__inner')].filter(vis).find(i => i.placeholder === '请输入目录/文件名称')

	// 运行时 state：两套组件同时挂载，必须按 visible===true 锁定活动实例（探针口径，见 feature-record 脚注）
	H.findState = () => {
		const els = document.querySelectorAll('.el-drawer *')
		let seed = null
		for (let i = 0; i < els.length; i++) {
			if (els[i].__vueParentComponent) {
				seed = els[i].__vueParentComponent
				break
			}
		}
		if (!seed) {
			return { kind: 'NO_SEED', st: null, holder: 'NA', visCand: 0 }
		}
		let root = seed
		while (root.parent) {
			root = root.parent
		}
		const seen = new Set()
		const queue = [root]
		const hits = []
		while (queue.length && seen.size < 4000) {
			const inst = queue.shift()
			if (!inst || seen.has(inst)) {
				continue
			}
			seen.add(inst)
			const ss = inst.setupState
			if (ss) {
				const nm = (inst.type && (inst.type.__name || inst.type.name)) || 'anon'
				if (ss.templateTreeData && ss.templateTreeData.treeList) {
					hits.push({ kind: 'v1', st: ss.templateTreeData, name: nm, open: ss.templateTreeData.visible === true })
				} else if (ss.state && ss.state.treeList && ss.state.tabList) {
					// v2 重构后 visible 已上移容器（state 不再承载抽屉显隐），改按容器自己的 setupState.visible 判活
					hits.push({ kind: 'v2', st: ss.state, name: nm, open: ss.visible === true })
				}
			}
			const stack = [inst.subTree]
			while (stack.length) {
				const v = stack.pop()
				if (!v || typeof v !== 'object') {
					continue
				}
				if (v.component) {
					queue.push(v.component)
				}
				if (Array.isArray(v.children)) {
					for (const c of v.children) {
						if (c && typeof c === 'object') {
							stack.push(c)
						}
					}
				}
				if (Array.isArray(v.dynamicChildren)) {
					for (const c of v.dynamicChildren) {
						if (c && typeof c === 'object') {
							stack.push(c)
						}
					}
				}
			}
		}
		// 按 state 对象归组：v2 的 tree-v2/tree-panel/editor-panel 命中同一份对象引用（共享引用的正面证据），
		// 其中只要容器报 open 就认定该对象属于当前活动抽屉
		const groups = new Map()
		for (const h of hits) {
			if (!groups.has(h.st)) {
				groups.set(h.st, { kind: h.kind, st: h.st, holders: [], open: false })
			}
			const g = groups.get(h.st)
			g.holders.push(h.name)
			if (h.open) {
				g.open = true
			}
		}
		const gs = [...groups.values()]
		const active = gs.filter(g => g.open)
		return {
			kind: active.length ? active[0].kind : 'NOT_FOUND',
			st: active.length ? active[0].st : null,
			holder: gs.map(g => g.holders.join('+')).join('|') || 'NONE',
			visCand: active.length,
			refShare: active.length ? active[0].holders.length : 0,
			objCount: gs.length
		}
	}
	H.byId = (id, list) => {
		for (const t of list) {
			if (t.id === id) {
				return t
			}
			if (t.children && t.children.length) {
				const r = H.byId(id, t.children)
				if (r) {
					return r
				}
			}
		}
		return undefined
	}
	// 与共用件 buildFileList 同序：DFS，templateType!=0 即文件
	H.fileList = list => {
		const out = []
		for (const t of list) {
			if (t.templateType !== 0) {
				out.push(t)
			} else if (t.children && t.children.length) {
				out.push(...H.fileList(t.children))
			}
		}
		return out
	}
	H.contentOf = (st, id) => {
		const n = H.byId(id, st.treeList)
		return n ? { len: (n.templateContent || '').length, hash: H.hs(n.templateContent), lines: lines(n.templateContent), edited: !!n.isEdited, origHash: H.hs(n.originalTemplateContent), name: n.fileName, parentId: n.parentId } : null
	}
	// 元素存在才点击，缺失时由调用方把 MISSING 写进同一个键：两版键集必须一致
	H.ck = el => {
		if (!el) {
			return false
		}
		el.click()
		return true
	}
	// 取脏口径与 handleClose 相同：递归 treeList，不是 tabList
	H.dirtyInTree = st => {
		let n = 0
		const walk = list => {
			for (const t of list) {
				if (t.isEdited) {
					n++
				}
				if (t.children && t.children.length) {
					walk(t.children)
				}
			}
		}
		walk(st.treeList)
		return n
	}
	// 整篇回写 S0 基线原文：全选 + insertText，不依赖光标位置（tab 关掉重开后只能用这条通道复原）
	H.restoreToBase = async (drawer, id) => {
		const cm = H.cm(drawer)
		if (!cm) {
			return 'NO_CM'
		}
		if (!H.BASE || H.BASE[id] == null) {
			return 'NO_BASELINE'
		}
		cm.focus()
		document.execCommand('selectAll')
		const ok = document.execCommand('insertText', false, H.BASE[id])
		await sleep(700)
		return ok ? 'OK' : 'FAILED'
	}
	// id -> 节点扁平表，祖先链只能靠 state 的 parentId 算：折叠子树在 DOM 里根本没渲染，
	// 用 [data-key] 反推 parentElement 会先撞上 NO_NODE（鸡生蛋）
	H.flatMap = () => {
		const m = {}
		const walk = arr => {
			for (const t of arr || []) {
				m[t.id] = t
				if (t.children && t.children.length) {
					walk(t.children)
				}
			}
		}
		walk(H.st && H.st.treeList)
		return m
	}
	H.ancestorIds = id => {
		const m = H.flatMap()
		const out = []
		let cur = m[id],
			guard = 0
		while (cur && cur.parentId && m[cur.parentId] && guard++ < 32) {
			out.unshift(m[cur.parentId].id)
			cur = m[cur.parentId]
		}
		return out
	}
	H.expandTo = async (id, drawer) => {
		const path = H.ancestorIds(id)
		for (const aid of path) {
			const an = drawer.querySelector('[data-key="' + aid + '"]')
			if (!an) {
				return 'PATH_BROKEN_' + aid
			}
			const icon = an.querySelector(':scope > .el-tree-node__content .el-tree-node__expand-icon')
			if (icon && !icon.classList.contains('is-leaf') && !an.classList.contains('is-expanded')) {
				icon.click()
				await sleep(250)
			}
		}
		const el = drawer.querySelector('[data-key="' + id + '"]')
		if (!el) {
			return 'NO_NODE'
		}
		return vis(el) ? 'VISIBLE' : 'HIDDEN'
	}
	H.openNode = async (id, drawer) => {
		const exp = await H.expandTo(id, drawer)
		const node = drawer.querySelector('[data-key="' + id + '"] .custom-tree-node')
		if (!node) {
			return 'NO_CLICK_TARGET:' + exp
		}
		node.click()
		await sleep(700)
		return exp
	}
	H.expandedCount = drawer => [...drawer.querySelectorAll('.el-tree-node.is-expanded')].filter(vis).length

	/* ---------- 正式执行 ---------- */
	const res = { stage: 'S0' }
	await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		res.F = ['P_gate=ABORT_' + op.why]
		res.fpBehav = H.fnv(res.F.join('|'))
		return res
	}
	const drawer = op.drawer
	const fs = H.findState()
	if (!fs.st) {
		res.F = ['P_gate=STATE_NOT_FOUND']
		res.fpBehav = H.fnv(res.F.join('|'))
		return res
	}
	H.drawer = drawer
	H.st = fs.st

	// 身份自检（不计入行为差分）
	H.I.push('P_rowIdx=' + H.rowIdx)
	H.I.push('P_rowCount=' + [...document.querySelectorAll('tbody tr')].filter(vis).length)
	H.I.push('P_ver_treePanel=' + [...document.querySelectorAll('.tree-panel')].filter(vis).length)
	H.I.push('P_holder=' + fs.holder)
	H.I.push('P_visCand=' + fs.visCand)
	H.I.push('P_refShare=' + fs.refShare + '/objCount=' + fs.objCount)
	H.I.push('P_kind=' + fs.kind)

	// 选择器预检门禁
	const sel = {
		closeIcon: !!drawer.querySelector('.el-tabs__item .is-icon-close'),
		tabItem: drawer.querySelectorAll('.el-tabs__item').length,
		searchInput: !!H.searchInput(drawer),
		saveBtn: !!H.saveBtn(drawer),
		refreshBtn: !!H.btn(drawer)('刷新'),
		testBtn: !!H.btn(drawer)('测试'),
		cm: !!H.cm(drawer),
		tabLabel: !!drawer.querySelector('.el-tabs__item .tab-label'),
		treeBadge: !!drawer.querySelector('[data-key] .el-badge__content.is-dot'),
		nodeKey: !!drawer.querySelector('[data-key]')
	}
	res.sel = sel
	const gateOk = Object.values(sel).every(v => typeof v === 'boolean' ? v : v > 0)

	// 行为断言：F01 打开入口
	const title = ((drawer.querySelector('.el-drawer__title') || {}).innerText || '').trim()
	H.F.push('F01_title=' + H.hs(title))
	H.F.push('F01_hasGroupName=' + (title.indexOf(ROW_TEXT) >= 0 ? 'Y' : 'N'))

	// 靶点文件：DFS 前 4 个 templateType=1 文件（第 4 个用于 joinFileName 折叠分支）
	const files = H.fileList(fs.st.treeList).filter(t => t.templateType === 1).slice(0, 4)
	H.FILES = files.map(t => ({ id: t.id, name: t.fileName, parentId: t.parentId }))
	H.F.push('P_files=' + H.FILES.map(f => f.id + ':' + f.name).join(','))
	H.F.push('P_baseline=' + files.map(f => f.id + '=' + H.hs(f.templateContent) + '/' + (f.templateContent || '').length + '/' + lines(f.templateContent)).join(','))
	H.BASE = {}
	files.forEach(f => {
		H.BASE[f.id] = f.templateContent || ''
	})
	fs.st.baselineHash = {}
	files.forEach(f => {
		fs.st.baselineHash[f.id] = H.hs(f.templateContent)
	})
	H.F.push('P_baseSaved=' + H.FILES.map(f => f.id + ':' + String(H.BASE[f.id]).length).join(','))

	// fresh open 初始态
	H.F.push('P_tabSeq0=' + H.hs(H.tabSeq(drawer)))
	H.F.push('P_active0=' + fs.st.activeItemId)
	H.F.push('P_tabCount0=' + fs.st.tabList.length)
	H.F.push('P_expanded0=' + H.expandedCount(drawer))
	H.F.push('P_treeDots0=' + [...drawer.querySelectorAll('[data-key] .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length)
	H.F.push('P_tabDots0=' + [...drawer.querySelectorAll('.el-tabs__item .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length)
	H.F.push('P_save0=' + H.saveState(drawer))
	H.F.push('P_search0=[' + (H.searchInput(drawer) || { value: 'NO_INPUT' }).value + ']')
	H.F.push('P_varEdge=' + (((drawer.querySelector('.variable-edge') || {}).innerText || '').replace(/\s+/g, '') || 'NO_EDGE'))
	H.F.push('P_gate=' + (gateOk ? 'PASS' : 'FAIL'))

	res.F = H.F.slice()
	res.fpBehav = H.fnv(res.F.join('|'))
	res.fpBehavLen = res.F.join('|').length
	res.fpId = H.fnv(H.I.join('|'))
	res.Fstr = res.F.join('|')
	res.Istr = H.I.join('|')
	res.msgBoxCount = [...document.querySelectorAll('.el-message-box')].filter(vis).length
	res.drawerCount = [...document.querySelectorAll('.el-drawer')].filter(vis).length
	return res
}

/* -----------------------------------------------------------------------------
 * STAGE S1 — F19 保存落库链路（写 2 次 updateContent：一次落 marker、一次复原）
 * ========================================================================== */
async () => {
	const H = window.__H
	const sleep = H.sleep,
		vis = H.vis,
		hs = H.hs
	if (!H || !H.FILES || !H.FILES.length) {
		return { stage: 'S1', F: ['F19_gate=S0_NOT_RUN'] }
	}
	await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { stage: 'S1', F: ['F19_gate=OPEN_FAIL_' + op.why] }
	}
	const drawer = op.drawer
	H.drawer = drawer
	const st = H.findState().st
	if (!st) {
		return { stage: 'S1', F: ['F19_gate=STATE_NOT_FOUND'] }
	}
	H.st = st
	const F = []
	const A = H.FILES[0]
	const base = st.baselineHash[A.id]
	F.push('F19_target=' + A.id + ':' + A.name + ':' + base)

	// 打开靶点文件（若首 tab 已是它则跳过点击）
	if (!drawer.querySelector('#tab-' + A.id)) {
		F.push('F19_open=' + (await H.openNode(A.id, drawer)))
		await sleep(500)
	}
	const c0 = H.contentOf(st, A.id)
	F.push('F19_before=' + c0.hash + '/' + c0.len + '/' + c0.lines + '/dot=' + H.treeDot(A.id) + ',' + H.tabDot(A.id) + '/save=' + H.saveState(drawer))

	// ① 插入 marker
	F.push('F19_mark=' + (await H.mark(drawer, H.MARK)))
	const c1 = H.contentOf(st, A.id)
	F.push('F19_marked=' + c1.hash + '/' + c1.len + '/dot=' + H.treeDot(A.id) + ',' + H.tabDot(A.id) + '/save=' + H.saveState(drawer) + '/edited=' + (c1.edited ? 'Y' : 'N'))
	if (c1.len !== c0.len + H.MARK.length) {
		F.push('F19_gate=MARK_LEN_UNEXPECTED')
	}

	// ② 点「保存」→ 单条 updateContent 落库
	const save = H.saveBtn(drawer)
	if (!save || save.disabled) {
		F.push('F19_gate=SAVE_NOT_CLICKABLE')
		return { stage: 'S1', F: F, fpBehav: H.fnv(F.join('|')), Fstr: F.join('|') }
	}
	H.resetToasts()
	save.click()
	await sleep(1200)
	F.push('F19_saveToast=' + H.toastLog())
	const c2 = H.contentOf(st, A.id)
	F.push('F19_afterSave=' + c2.hash + '/' + c2.len + '/dot=' + H.treeDot(A.id) + ',' + H.tabDot(A.id) + '/save=' + H.saveState(drawer) + '/edited=' + (c2.edited ? 'Y' : 'N'))
	// 保存成功后 originalTemplateContent 应前移到含 marker 的内容（红点灭、按钮回灰）
	F.push('F19_origAdvanced=' + (c2.origHash === c2.hash ? 'Y' : 'N'))

	// ③ 复原：先逐字退格；不成立则整篇回写基线；仍不成立就拒绝再保存，避免把非基线内容写库
	F.push('F19_unmark=' + (await H.unmark(drawer, H.MARK.length)))
	const c3 = H.contentOf(st, A.id)
	F.push('F19_reverted=' + (c3.hash === base ? 'Y' : 'N') + '/' + c3.hash + '/' + c3.len + '/dot=' + H.treeDot(A.id) + ',' + H.tabDot(A.id))
	if (c3.hash !== base) {
		F.push('F19_fallback=' + (await H.restoreToBase(drawer, A.id)))
		const c3b = H.contentOf(st, A.id)
		F.push('F19_fallbackToBase=' + (c3b.hash === base ? 'Y' : 'N'))
		if (c3b.hash !== base) {
			F.push('F19_gate=RESTORE_FAILED_MANUAL_ID_' + A.id)
			return { stage: 'S1', F: F, fpBehav: H.fnv(F.join('|')), Fstr: F.join('|'), warn: 'DB 仍含 marker，需人工复原 id=' + A.id }
		}
	}
	H.resetToasts()
	F.push('F19_restoreClick=' + (H.ck(H.saveBtn(drawer)) ? 'clicked' : 'MISSING'))
	await sleep(1200)
	F.push('F19_restoreToast=' + H.toastLog())
	const c4 = H.contentOf(st, A.id)
	F.push('F19_final=' + c4.hash + '/' + c4.len + '/dot=' + H.treeDot(A.id) + ',' + H.tabDot(A.id) + '/save=' + H.saveState(drawer))
	F.push('F19_restoredToBase=' + (c4.hash === base ? 'Y' : 'N'))

	return { stage: 'S1', F: F, fpBehav: H.fnv(F.join('|')), fpLen: F.join('|').length, Fstr: F.join('|'), boxes: [...document.querySelectorAll('.el-message-box')].filter(vis).length }
}

/* -----------------------------------------------------------------------------
 * STAGE S2 — F14 切换/关闭单 tab + F16 批量关闭（零写入：只走 X / 否(N)）
 * ========================================================================== */
async () => {
	const H = window.__H
	const sleep = H.sleep,
		vis = H.vis,
		hs = H.hs
	if (!H || !H.FILES) {
		return { stage: 'S2', F: ['F14_gate=S0_NOT_RUN'] }
	}
	await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { stage: 'S2', F: ['F14_gate=OPEN_FAIL_' + op.why] }
	}
	const drawer = op.drawer
	H.drawer = drawer
	const st = H.findState().st
	H.st = st
	const F = []
	const I = []
	const [A, B, C] = H.FILES
	const tabClose = id => drawer.querySelector('#tab-' + id + ' .is-icon-close')
	const menuItem = label => {
		const menus = [...document.querySelectorAll('.context-menu')].filter(vis)
		const all = []
		menus.forEach(m => all.push(...[...m.querySelectorAll('li')].filter(vis)))
		return all.find(li => li.innerText.trim() === label)
	}
	const menuLabels = () => {
		const menus = [...document.querySelectorAll('.context-menu')].filter(vis)
		const out = []
		menus.forEach(m => out.push(...[...m.querySelectorAll('li')].filter(vis).map(li => li.innerText.trim())))
		return out.join(',')
	}
	const rightClickTab = async id => {
		const t = drawer.querySelector('#tab-' + id)
		if (!t) {
			return 'NO_TAB'
		}
		t.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true, clientX: t.getBoundingClientRect().left + 12, clientY: t.getBoundingClientRect().top + 8 }))
		await sleep(600)
		return 'SENT'
	}

	// 开 3 个 tab
	for (const f of [A, B, C]) {
		if (!drawer.querySelector('#tab-' + f.id)) {
			F.push('F14_open' + f.id + '=' + (await H.openNode(f.id, drawer)))
			await sleep(400)
		}
	}
	F.push('F14_tabs3=' + hs(H.tabSeq(drawer)) + '/n=' + st.tabList.length + '/active=' + st.activeItemId)

	// ① F14 点击切换：激活 C，编辑器内容应切到 C 基线
	F.push('F14_clickTabC=' + (H.ck(drawer.querySelector('#tab-' + C.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
	await sleep(700)
	const cc = H.contentOf(st, C.id) || {}
	const cTab = drawer.querySelector('#tab-' + C.id)
	F.push('F14_clickC=' + st.activeItemId + '/' + cc.hash + '/' + cc.len + '/isActive=' + (cTab ? (cTab.classList.contains('is-active') ? 'Y' : 'N') : 'NO_TAB'))

	// ② F14 关闭干净 tab（C）：不弹确认，激活位按「右侧优先、无则左侧末尾」回落
	F.push('F14_closeIconC=' + (H.ck(tabClose(C.id)) ? 'clicked' : 'MISSING'))
	await sleep(700)
	F.push('F14_closeClean=' + (H.box() ? 'BOX_APPEARED' : 'no_box') + '/tabs=' + hs(H.tabSeq(drawer)) + '/active=' + st.activeItemId + '/dots=' + H.treeDot(C.id) + ',' + H.tabDot(C.id))

	// ③ F14 关闭脏 tab（B）：弹三态确认；先验 X 态（留在抽屉）
	F.push('F14_markB=' + (await H.mark(drawer, '@@T1@@')))
	const b1 = H.contentOf(st, B.id)
	F.push('F14_dirtyB=' + b1.hash + '/' + b1.len + '/dot=' + H.treeDot(B.id) + ',' + H.tabDot(B.id) + '/save=' + H.saveState(drawer))
	F.push('F14_closeIconB=' + (H.ck(tabClose(B.id)) ? 'clicked' : 'MISSING'))
	await sleep(800)
	const msg1 = H.boxMsg()
	F.push('F14_dirtyBox=' + hs(msg1) + '/hasTplWord=' + ((msg1 || '').indexOf('模板文件') >= 0 ? 'Y' : 'N') + '/btns=' + H.boxBtns() + '/drawerOpen=' + [...document.querySelectorAll('.el-drawer')].filter(vis).length)
	F.push('F14_xBtn=' + (H.ck(H.boxBtn('x')) ? 'clicked' : 'MISSING'))
	await sleep(700)
	F.push('F14_xBranch=' + (H.box() ? 'BOX_STILL' : 'box_gone') + '/tabStill=' + !!drawer.querySelector('#tab-' + B.id) + '/dot=' + H.treeDot(B.id) + ',' + H.tabDot(B.id) + '/active=' + st.activeItemId)

	// ④ 再关一次走 否(N)：丢弃，tab 关闭、红点灭、零写入
	F.push('F14_closeIconB2=' + (H.ck(tabClose(B.id)) ? 'clicked' : 'MISSING'))
	await sleep(800)
	F.push('F14_noBtn=' + (H.ck(H.boxBtn('no')) ? 'clicked' : 'MISSING'))
	await sleep(900)
	const b2 = H.contentOf(st, B.id)
	F.push('F14_noBranch=' + hs(H.tabSeq(drawer)) + '/active=' + st.activeItemId + '/dot=' + H.treeDot(B.id) + ',' + H.tabDot(B.id) + '/edited=' + (b2.edited ? 'Y' : 'N') + '/boxes=' + [...document.querySelectorAll('.el-message-box')].filter(vis).length)

	// ⑤ F16 右键菜单显隐序列（单 tab 时「关闭其他/全部」应缺席）
	F.push('F16_menuA=' + (await rightClickTab(A.id)))
	F.push('F16_labels1=' + menuLabels())
	I.push('F16_menuDom=' + [...document.querySelectorAll('.context-menu')].filter(vis).map(m => m.className + '|' + m.querySelectorAll('.context-menu__item').length + '|' + m.querySelectorAll('li').length).join(','))
	// 重新开三个 tab 观察完整菜单
	for (const f of [B, C]) {
		if (!drawer.querySelector('#tab-' + f.id)) {
			await H.openNode(f.id, drawer)
			await sleep(400)
		}
	}
	F.push('F16_tabs3b=' + hs(H.tabSeq(drawer)))
	await rightClickTab(B.id)
	F.push('F16_labelsMid=' + menuLabels() + '/n=' + st.tabList.length)
	// 把 C 也标脏，留一个 clean 一个 dirty 供「关闭未改」
	if (!H.contentOf(st, C.id).edited) {
		F.push('F16_clickTabC=' + (H.ck(drawer.querySelector('#tab-' + C.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
		await sleep(600)
		F.push('F16_markC=' + (await H.mark(drawer, '@@T2@@')))
	}

	// ⑥ F16 关闭未改：直接过滤、无确认弹窗
	await rightClickTab(A.id)
	const un = menuItem('关闭未改')
	F.push('F16_uneditedItem=' + (un ? 'found' : 'MISSING'))
	H.ck(un)
	await sleep(900)
	F.push('F16_unedited=' + (H.box() ? 'BOX_APPEARED' : 'no_box') + '/tabs=' + hs(H.tabSeq(drawer)) + '/n=' + st.tabList.length + '/active=' + st.activeItemId + '/allEdited=' + st.tabList.every(t => t.isEdited))

	// ⑦ F16 关闭全部 → 否(N)：tabs 清空 + 空态文案（F28 顺带片段）
	let rc7 = 'NO_TABS'
	if (st.tabList.length) {
		rc7 = await rightClickTab(st.tabList[0].id)
	}
	const ca = st.tabList.length ? menuItem('关闭全部') : null
	F.push('F16_allPre=' + rc7 + '/n=' + st.tabList.length)
	F.push('F16_allItem=' + (ca ? 'found' : 'MISSING'))
	H.ck(ca)
	await sleep(900)
	const msg2 = H.boxMsg()
	F.push('F16_allBox=' + hs(msg2) + '/hasTplWord=' + ((msg2 || '').indexOf('模板文件') >= 0 ? 'Y' : 'N') + '/btns=' + H.boxBtns())
	F.push('F16_allNoBtn=' + (H.ck(H.boxBtn('no')) ? 'clicked' : 'MISSING'))
	await sleep(1000)
	const emptyText = [...drawer.querySelectorAll('.el-text')].filter(vis).map(e => e.innerText.trim()).join('|')
	F.push('F16_allNo=' + (H.box() ? 'BOX_STILL' : 'box_gone') + '/tabs=' + hs(H.tabSeq(drawer)) + '/n=' + st.tabList.length + '/active=' + st.activeItemId + '/empty=' + hs(emptyText))

	// 复原现场：重开 A 留一个 tab
	await H.openNode(A.id, drawer)
	await sleep(500)
	F.push('F16_tail=' + hs(H.tabSeq(drawer)) + '/active=' + st.activeItemId + '/treeDots=' + [...drawer.querySelectorAll('[data-key] .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length)

	return { stage: 'S2', F: F, fpBehav: H.fnv(F.join('|')), fpLen: F.join('|').length, fpId: H.fnv(I.join('|')), Fstr: F.join('|'), Istr: I.join('|') }
}

/* -----------------------------------------------------------------------------
 * STAGE S3 — F16 是(Y) 批量落库（1 次 updateContentBatch + 2 次 updateContent 复原）
 * ========================================================================== */
async () => {
	const H = window.__H
	const sleep = H.sleep,
		vis = H.vis,
		hs = H.hs
	if (!H || !H.FILES) {
		return { stage: 'S3', F: ['F16y_gate=S0_NOT_RUN'] }
	}
	await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { stage: 'S3', F: ['F16y_gate=OPEN_FAIL_' + op.why] }
	}
	const drawer = op.drawer
	H.drawer = drawer
	const st = H.findState().st
	H.st = st
	const F = []
	const [A, B] = H.FILES
	const menuItem = label => {
		const menus = [...document.querySelectorAll('.context-menu')].filter(vis)
		const all = []
		menus.forEach(m => all.push(...[...m.querySelectorAll('li')].filter(vis)))
		return all.find(li => li.innerText.trim() === label)
	}
	const baseA = st.baselineHash[A.id]
	const baseB = st.baselineHash[B.id]
	F.push('F16y_targets=' + A.id + ':' + baseA + ',' + B.id + ':' + baseB)

	// 两个文件都标脏（写入走批量端点，复原交给 restoreToBase）
	if (!drawer.querySelector('#tab-' + A.id)) {
		await H.openNode(A.id, drawer)
		await sleep(400)
	}
	F.push('F16y_tabA=' + (H.ck(drawer.querySelector('#tab-' + A.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
	await sleep(600)
	F.push('F16y_markA=' + (await H.mark(drawer, '@@B1@@')))
	if (!drawer.querySelector('#tab-' + B.id)) {
		await H.openNode(B.id, drawer)
		await sleep(400)
	}
	F.push('F16y_tabB=' + (H.ck(drawer.querySelector('#tab-' + B.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
	await sleep(600)
	F.push('F16y_markB=' + (await H.mark(drawer, '@@B2@@')))
	const dA = H.contentOf(st, A.id),
		dB = H.contentOf(st, B.id)
	F.push('F16y_dirty=' + dA.hash + '/' + dA.len + ',' + dB.hash + '/' + dB.len + '/dots=' + H.treeDot(A.id) + H.tabDot(A.id) + '/' + H.treeDot(B.id) + H.tabDot(B.id))

	// 右键「关闭全部」→ 是(Y)：批量 updateContentBatch 落库后关闭 tab
	const t = drawer.querySelector('#tab-' + A.id)
	if (!t) {
		F.push('F16y_gate=TAB_A_MISSING_SKIP_WRITE')
		return { stage: 'S3', F: F, fpBehav: H.fnv(F.join('|')), Fstr: F.join('|') }
	}
	t.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true, clientX: t.getBoundingClientRect().left + 12, clientY: t.getBoundingClientRect().top + 8 }))
	await sleep(600)
	const ca = menuItem('关闭全部')
	if (!ca) {
		F.push('F16y_gate=MENU_ALL_MISSING')
		return { stage: 'S3', F: F, fpBehav: H.fnv(F.join('|')), Fstr: F.join('|') }
	}
	ca.click()
	await sleep(900)
	const msg = H.boxMsg()
	F.push('F16y_box=' + hs(msg) + '/hasTplWord=' + ((msg || '').indexOf('模板文件') >= 0 ? 'Y' : 'N') + '/names=' + ((msg || '').indexOf(A.name) >= 0 && (msg || '').indexOf(B.name) >= 0 ? 'Y' : 'N') + '/btns=' + H.boxBtns())
	H.resetToasts()
	F.push('F16y_yesBtn=' + (H.ck(H.boxBtn('yes')) ? 'clicked' : 'MISSING'))
	await sleep(1800)
	F.push('F16y_toast=' + H.toastLog())
	F.push('F16y_afterYes=' + 'tabs=' + hs(H.tabSeq(drawer)) + '/n=' + st.tabList.length + '/active=' + st.activeItemId + '/treeDots=' + [...drawer.querySelectorAll('[data-key] .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length)
	const sA = H.contentOf(st, A.id),
		sB = H.contentOf(st, B.id)
	// 是(Y) 分支应把 originalTemplateContent 前移、isEdited 清零（DB 此刻带 marker，S3-restore 再改回）
	F.push('F16y_state=' + sA.hash + '/' + (sA.edited ? 'Y' : 'N') + ',' + sB.hash + '/' + (sB.edited ? 'Y' : 'N') + '/origSync=' + (sA.origHash === sA.hash && sB.origHash === sB.hash ? 'Y' : 'N'))

	// 复原：逐个重开 → 整篇回写 S0 基线原文 → 单条 updateContent 保存（hash 不等于基线则拒绝写库）
	for (const f of [A, B]) {
		const base = f.id === A.id ? baseA : baseB
		if (!drawer.querySelector('#tab-' + f.id)) {
			await H.openNode(f.id, drawer)
			await sleep(500)
		}
		F.push('F16y_revertTab' + f.id + '=' + (H.ck(drawer.querySelector('#tab-' + f.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
		await sleep(600)
		F.push('F16y_reopen' + f.id + '=' + hs(H.contentOf(st, f.id).hash))
		F.push('F16y_rewrite' + f.id + '=' + (await H.restoreToBase(drawer, f.id)))
		const r = H.contentOf(st, f.id)
		F.push('F16y_revert' + f.id + '=' + (r.hash === base ? 'Y' : 'N') + '/' + r.hash + '/' + r.len + '/dot=' + H.treeDot(f.id) + ',' + H.tabDot(f.id))
		if (r.hash !== base) {
			F.push('F16y_gate=RESTORE_FAILED_MANUAL_ID_' + f.id)
			continue
		}
		const sb = H.saveBtn(drawer)
		if (!sb || sb.disabled) {
			F.push('F16y_gate=SAVE_NOT_CLICKABLE_' + f.id)
			continue
		}
		sb.click()
		await sleep(1400)
		const fin = H.contentOf(st, f.id)
		F.push('F16y_restore' + f.id + '=' + hs(fin.hash) + '/' + fin.len + '/dot=' + H.treeDot(f.id) + ',' + H.tabDot(f.id) + '/save=' + H.saveState(drawer) + '/toBase=' + (fin.hash === base ? 'Y' : 'N'))
	}
	F.push('F16y_finalTabs=' + hs(H.tabSeq(drawer)) + '/treeDots=' + [...drawer.querySelectorAll('[data-key] .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length + '/boxes=' + [...document.querySelectorAll('.el-message-box')].filter(vis).length)

	return { stage: 'S3', F: F, fpBehav: H.fnv(F.join('|')), fpLen: F.join('|').length, Fstr: F.join('|') }
}

/* -----------------------------------------------------------------------------
 * STAGE S4 — F02 两处文案（带/不带「模板文件」+ 折叠分支 + 无脏不弹）+ F03 复位 + F07 两分支
 * ========================================================================== */
async () => {
	const H = window.__H
	const sleep = H.sleep,
		vis = H.vis,
		hs = H.hs
	if (!H || !H.FILES) {
		return { stage: 'S4', F: ['F02_gate=S0_NOT_RUN'] }
	}
	if (H.FILES.length < 4) {
		return { stage: 'S4', F: ['F02_gate=NEED_4_FILES_GOT_' + H.FILES.length] }
	}
	const F = []
	const I = []
	const [A, B, C, D] = H.FILES
	let tMsg = null // 测试子抽屉文案，供与主抽屉同文件文案做成对断言
	const menuItem = label => {
		const menus = [...document.querySelectorAll('.context-menu')].filter(vis)
		const all = []
		menus.forEach(m => all.push(...[...m.querySelectorAll('li')].filter(vis)))
		return all.find(li => li.innerText.trim() === label)
	}
	const snapshot = drawer => {
		const st = H.st
		return 'tabs=' + hs(H.tabSeq(drawer)) + '/n=' + st.tabList.length + '/active=' + st.activeItemId + '/exp=' + H.expandedCount(drawer) + '/search=[' + (H.searchInput(drawer) || { value: 'NA' }).value + ']' + '/dots=' + [...drawer.querySelectorAll('[data-key] .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length + ',tabDots=' + [...drawer.querySelectorAll('.el-tabs__item .el-badge__content.is-dot')].filter(b => b.style.display !== 'none').length + '/edge=' + (((drawer.querySelector('.variable-edge') || {}).innerText || '').replace(/\s+/g, '') || 'NO_EDGE')
	}

	/* ---- ① 折叠分支：4 个脏 → 主抽屉 X → 文案「A、B、C...等4个模板文件」→ 否(N) 丢弃 ---- */
	await H.forceCloseMain()
	let op = await H.open()
	if (!op.ok) {
		return { stage: 'S4', F: ['F02_gate=OPEN_FAIL_' + op.why] }
	}
	let drawer = op.drawer
	H.drawer = drawer
	H.st = H.findState().st
	const st = H.st
	for (const f of [A, B, C, D]) {
		if (!drawer.querySelector('#tab-' + f.id)) {
			await H.openNode(f.id, drawer)
			await sleep(400)
		}
		F.push('F02_tab' + f.id + '=' + (H.ck(drawer.querySelector('#tab-' + f.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
		await sleep(500)
		F.push('F02_mark' + f.id + '=' + (await H.mark(drawer, '@@K@@')))
	}
	F.push('F02_dirtySet=' + st.tabList.filter(t => t.isEdited).length + '/' + hs(st.tabList.filter(t => t.isEdited).map(t => t.fileName).join(',')))
	// handleClose 取脏递的是 treeList，不是 tabList；此页只开了 4 个 tab，两个口径应同为 4
	F.push('F02_dirtyTree=' + H.dirtyInTree(st))
	H.closeDrawerX(drawer)
	await sleep(900)
	const foldMsg = H.boxMsg()
	F.push('F02_foldBox=' + hs(foldMsg) + '/hasEtc=' + ((foldMsg || '').indexOf('...等') >= 0 ? 'Y' : 'N') + '/hasTplWord=' + ((foldMsg || '').indexOf('模板文件') >= 0 ? 'Y' : 'N') + '/btns=' + H.boxBtns())
	F.push('F02_foldNoBtn=' + (H.ck(H.boxBtn('no')) ? 'clicked' : 'MISSING'))
	await sleep(1000)
	F.push('F02_foldDiscard=' + (H.box() ? 'BOX_STILL' : 'box_gone') + '/drawers=' + [...document.querySelectorAll('.el-drawer')].filter(vis).length)

	/* ---- ② F03 重复打开复位 ---- */
	op = await H.open()
	drawer = op.drawer
	H.drawer = drawer
	H.st = H.findState().st
	F.push('F03_reopen=' + snapshot(drawer))
	F.push('F03_contentBack=' + [A, B, C, D].map(f => f.id + ':' + (H.contentOf(H.st, f.id).hash === H.st.baselineHash[f.id] ? 'Y' : 'N')).join(','))

	/* ---- ③ F07 刷新：取消分支（状态全保） ---- */
	// 顺序：先造非初始展开态 + 标脏 B，再设搜索词（过滤后树上点不到 B，必须在设词前完成编辑）
	await H.expandTo(B.id, drawer)
	await sleep(300)
	if (!drawer.querySelector('#tab-' + B.id)) {
		await H.openNode(B.id, drawer)
		await sleep(500)
	}
	F.push('F07_tabB=' + (H.ck(drawer.querySelector('#tab-' + B.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
	await sleep(500)
	F.push('F07_markB=' + (await H.mark(drawer, '@@R@@')))
	const search = H.searchInput(drawer)
	F.push('F07_setSearch=' + (search ? 'OK' : 'MISSING'))
	if (search) {
		H.setInput(search, 'VO')
		await sleep(700)
	}
	const beforeRefresh = snapshot(drawer)
	const bHash = H.contentOf(H.st, B.id).hash
	F.push('F07_before=' + beforeRefresh + '/' + bHash)
	F.push('F07_refreshBtn1=' + (H.ck(H.btn(drawer)('刷新')) ? 'clicked' : 'MISSING'))
	await sleep(800)
	const rfMsg = H.boxMsg()
	F.push('F07_box=' + hs(rfMsg) + '/btns=' + H.boxBtns() + '/hasYesNo=' + ((H.boxBtn('yes') || H.boxBtn('no')) ? 'Y' : 'N'))
	H.resetToasts()
	F.push('F07_cancelBtn=' + (H.ck(H.boxBtn('cancel')) ? 'clicked' : 'MISSING'))
	await sleep(900)
	F.push('F07_cancelToast=' + H.toastLog())
	F.push('F07_cancelled=' + snapshot(drawer) + '/' + H.contentOf(H.st, B.id).hash + '/same=' + (bHash === H.contentOf(H.st, B.id).hash ? 'Y' : 'N'))

	/* ---- ④ F07 刷新：确定分支（全量回落 + 重发 GET tree） ---- */
	F.push('F07_refreshBtn2=' + (H.ck(H.btn(drawer)('刷新')) ? 'clicked' : 'MISSING'))
	await sleep(800)
	H.resetToasts()
	F.push('F07_okBtn=' + (H.ck(H.boxBtn('ok')) ? 'clicked' : 'MISSING'))
	await sleep(2200)
	F.push('F07_okToast=' + H.toastLog())
	F.push('F07_refreshed=' + snapshot(drawer))
	F.push('F07_dirtyDropped=' + (H.contentOf(H.st, B.id).hash === H.st.baselineHash[B.id] ? 'Y' : 'N') + '/edited=' + (H.contentOf(H.st, B.id).edited ? 'Y' : 'N') + '/save=' + H.saveState(drawer))

	/* ---- ⑤ F02「不带模板文件」那一处：测试子抽屉（共用件 test.vue，两版都走它） ---- */
	if (!drawer.querySelector('#tab-' + A.id)) {
		await H.openNode(A.id, drawer)
		await sleep(500)
	}
	F.push('F02_tabA5=' + (H.ck(drawer.querySelector('#tab-' + A.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
	await sleep(600)
	const tb = H.btn(drawer)('测试')
	if (!tb) {
		F.push('F02_testBtn=MISSING')
	} else {
		const mainBefore = snapshot(drawer)
		tb.click()
		await sleep(1800)
		const tdr = H.testDrawer()
		F.push('F02_testDrawer=' + (tdr ? '1' : '0'))
		if (tdr) {
			const tTitle = ((tdr.querySelector('.el-drawer__title') || {}).innerText || '').trim()
			F.push('F02_testTitle=' + hs(tTitle))
			// 切到「原始模板」tab，编辑它自己的编辑器（isEdit = editTemplateContent !== originalTemplateContent）
			const rawTab = [...tdr.querySelectorAll('.el-tabs__item')].filter(vis).find(x => x.innerText.indexOf('原始模板') >= 0)
			F.push('F02_rawTab=' + (rawTab ? hs(rawTab.innerText.trim()) : 'MISSING') + '/pane=' + (rawTab ? rawTab.getAttribute('aria-controls') : 'NA'))
			if (rawTab) {
				rawTab.click()
				await sleep(900)
				const paneId = rawTab.getAttribute('aria-controls')
				const scopeEl = paneId ? tdr.querySelector('#' + paneId) : tdr
				const cm = (scopeEl && [...scopeEl.querySelectorAll('.cm-content')].filter(vis)[0]) || H.cm(tdr)
				F.push('F02_testCm=' + (cm ? 'found' : 'MISSING') + '/cmTotal=' + [...tdr.querySelectorAll('.cm-content')].filter(vis).length)
				if (cm) {
					cm.focus()
					document.execCommand('insertText', false, '@@X@@')
					await sleep(700)
					F.push('F02_testEdited=1')
					H.closeDrawerX(tdr)
					await sleep(900)
					tMsg = H.boxMsg()
					F.push('F02_testBox=' + hs(tMsg) + '/hasTplWord=' + ((tMsg || '').indexOf('模板文件') >= 0 ? 'Y' : 'N') + '/nameHit=' + ((tMsg || '').indexOf(A.name) >= 0 ? 'Y' : 'N') + '/btns=' + H.boxBtns() + '/drawers=' + [...document.querySelectorAll('.el-drawer')].filter(vis).length)
					F.push('F02_testNoBtn=' + (H.ck(H.boxBtn('no')) ? 'clicked' : 'MISSING'))
					await sleep(1200)
					F.push('F02_testClosed=' + (H.testDrawer() ? 'STILL_OPEN' : 'gone') + '/mainUnchanged=' + (snapshot(drawer) === mainBefore ? 'Y' : 'N') + '/boxes=' + [...document.querySelectorAll('.el-message-box')].filter(vis).length)
				}
			}
		}
	}

	/* ---- ⑤b F02「带模板文件」那一处：主抽屉同文件单脏，与 ⑤ 构成成对断言 ---- */
	if (!drawer.querySelector('#tab-' + A.id)) {
		await H.openNode(A.id, drawer)
		await sleep(500)
	}
	F.push('F02_tabA6=' + (H.ck(drawer.querySelector('#tab-' + A.id + ' .tab-label')) ? 'clicked' : 'MISSING'))
	await sleep(600)
	F.push('F02_markA6=' + (await H.mark(drawer, '@@K@@')))
	F.push('F02_mainDirtyTree=' + H.dirtyInTree(H.st))
	H.closeDrawerX(drawer)
	await sleep(900)
	const mainMsg = H.boxMsg()
	F.push('F02_mainSingleBox=' + hs(mainMsg) + '/hasTplWord=' + ((mainMsg || '').indexOf('模板文件') >= 0 ? 'Y' : 'N') + '/nameHit=' + ((mainMsg || '').indexOf(A.name) >= 0 ? 'Y' : 'N') + '/btns=' + H.boxBtns())
	// 两版必须同结论：主抽屉文案去掉「模板文件」四字后与子抽屉文案逐字相等（预期 Y）
	F.push('F02_pairCheck=' + (tMsg != null && mainMsg != null ? (mainMsg.replace('模板文件', '') === tMsg ? 'Y' : 'N') : 'NA'))
	F.push('F02_pairLenDelta=' + (tMsg != null && mainMsg != null ? mainMsg.length - tMsg.length : 'NA'))
	F.push('F02_mainNoBtn=' + (H.ck(H.boxBtn('no')) ? 'clicked' : 'MISSING'))
	await sleep(1000)
	F.push('F02_mainDiscard=' + (H.box() ? 'BOX_STILL' : 'box_gone') + '/drawers=' + [...document.querySelectorAll('.el-drawer')].filter(vis).length)

	/* ---- ⑥ F02 第 0 态：无脏时关抽屉不弹确认，直接关 ---- */
	await H.forceCloseMain()
	op = await H.open()
	if (!op.ok || !op.drawer) {
		F.push('F02_cleanState=OPEN_FAIL_' + (op.why || 'NO_DRAWER'))
	} else {
		drawer = op.drawer
		H.drawer = drawer
		H.st = H.findState().st
		F.push('F02_cleanState=' + snapshot(drawer) + '/anyEdited=' + H.st.tabList.some(t => t.isEdited) + '/dirtyTree=' + H.dirtyInTree(H.st))
		H.closeDrawerX(drawer)
		await sleep(1100)
		F.push('F02_cleanClose=' + (H.box() ? 'BOX_APPEARED' : 'no_box') + '/drawers=' + [...document.querySelectorAll('.el-drawer')].filter(vis).length)
	}

	return { stage: 'S4', F: F, fpBehav: H.fnv(F.join('|')), fpLen: F.join('|').length, fpId: H.fnv(I.join('|')), Fstr: F.join('|'), Istr: I.join('|') }
}
