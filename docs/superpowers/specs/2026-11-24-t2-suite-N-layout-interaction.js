/* =============================================================================
 * T2 双跑对照 · Suite N（布局与交互批次：F25 / F05 / F17 / F18 / F20 / F21）
 * 日期：2026-11-24    靶点：模板组 80「单体java项目模板_复制」
 *
 * 用法（每段是一段独立的 async 箭头函数，顶格书写，装载器按 ^async \(\) => \{$ 切段）
 *   1) reload 页面 → 装载本文件（setInputFiles + FileReader 取源码，切段存 window.__SUITE_N）；
 *   2) 按 N0a → N0b → N0c → N0d → N1 → N2a → N2b → N3 → N4 → N5a → N5b 顺序跑；
 *   3) N0b/N0c/N0d 之间由外层改视口（browser_resize），N2a/N2b 之间由外层做真 drag（browser_drag），
 *      N5a/N5b 之间由外层发真 ESC 按键（page.keyboard.press）——页内合成事件不算证据；
 *   4) 换版本只改 window.__BTN：v2 = '模板v2'，v1 = '模板'。文件本身一字不改，两版跑同一份源码。
 *
 * 安全不变量（务必保持）
 *   - 本 suite 零写库：不点「是(Y) / 确定」，不点「保存」，不点「渲染」（试渲染会走生成链路落盘）；
 *   - 每段都记本阶段新增请求清单与非 GET 请求（writeReq），期望恒为空；
 *   - F25 必须在全新会话的第一个阶段取首次展开值：panelSizeInitialized 只在 init() 复位，
 *     而 variablePanelSize 不复位，一旦本会话拖过竖柄或先跑过其它 drag，值就不再可信；
 *   - 改过视口的段落在收尾必须复原到开局尺寸，否则后续段的宽度类指纹全部失真。
 *
 * 指纹口径
 *   - F（行为串）：两版必须逐字相同的业务断言项，k=v 多行拼接后 FNV-1a；
 *   - I（身份串）：版本身份与结构项（.tree-panel 是否在场、holder、抽屉标题、DOM 结构 dump），
 *     允许不同，但每项差异必须能逐条解释；
 *   - 每段各自重置 F/I，段级 fp 可独立比对（与 Suite S 一致）。
 * ========================================================================== */

/* -----------------------------------------------------------------------------
 * STAGE N0a — F25 首次展开值（干净会话，必须第一个跑；顺带建 helper 与结构 recon）
 *   量测对象是 .editor-splitter 容器宽（不随面板展开而变），不是编辑面板宽；
 *   夹紧链：raw=round(SE*0.28) → floor=min(320,SE-240) → upper=max(240,min(560,SE-floor))
 * ========================================================================== */
async () => {
	const BTN = window.__BTN || '模板v2'
	const ROW_TEXT = '单体java项目模板_复制'
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
	const hs = s => (s == null ? 'NULL' : fnv(String(s)) + '_' + String(s).length)
	const px = e => (e ? Math.round(e.getBoundingClientRect().width) : -1)

	const H = { BTN: BTN, ROW_TEXT: ROW_TEXT, sleep, vis, fnv, hs, px, F: [], I: [], REQ: [], TOASTS: [], FSCH: 0, reqPatched: false, toastPatched: false }
	window.__HN = H

	// ---- 请求 spy：页内合成不了网络，这里挂 XHR/fetch 记录，用于「零写库」自证
	H.spyReq = () => {
		if (H.reqPatched) {
			return 'ALREADY'
		}
		H.reqPatched = true
		const trim = u => String(u).replace(/^https?:\/\/[^/]+/, '')
		const xo = XMLHttpRequest.prototype.open
		XMLHttpRequest.prototype.open = function (m, u) {
			H.REQ.push(String(m).toUpperCase() + ' ' + trim(u))
			return xo.apply(this, Array.prototype.slice.call(arguments))
		}
		const fo = window.fetch
		window.fetch = function (a, b) {
			const m = (b && b.method) || (a && a.method) || 'GET'
			H.REQ.push(String(m).toUpperCase() + ' ' + trim(a && a.url ? a.url : a))
			return fo.apply(this, arguments)
		}
		document.addEventListener('fullscreenchange', () => {
			H.FSCH++
		})
		return 'OK'
	}
	// ---- toast spy：ElMessage 多为 500ms 自销，末尾采样会全漏
	H.spyToast = () => {
		if (H.toastPatched) {
			return 'ALREADY'
		}
		H.toastPatched = true
		new MutationObserver(muts => {
			muts.forEach(m => {
				m.addedNodes.forEach(n => {
					if (n.nodeType === 1 && n.classList && n.classList.contains('el-message')) {
						H.TOASTS.push(n.innerText.trim().replace(/\s+/g, ''))
					}
				})
			})
		}).observe(document.body, { childList: true, subtree: true })
		return 'OK'
	}
	H.reqMark = () => H.REQ.length
	H.stageReq = mark => H.REQ.slice(mark)
	H.writeReq = list => list.filter(r => !/^(GET|HEAD|OPTIONS) /.test(r))
	H.out = () => {
		const b = H.F.join('\n')
		const i = H.I.join('\n')
		return { fpBehav: fnv(b), behavLen: b.length, fpId: fnv(i), idLen: i.length, F: H.F, I: H.I }
	}

	// ---- 抽屉与按钮
	H.mainDrawer = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0)
	H.mainReady = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0 && d.querySelector('.el-tabs__item'))
	H.testDrawer = () => [...document.querySelectorAll('.el-drawer.template-test-drawer')].filter(vis)[0]
	H.box = () => [...document.querySelectorAll('.el-message-box')].filter(vis)[0]
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
	H.boxMsg = () => {
		const b = H.box()
		if (!b) {
			return null
		}
		const m = b.querySelector('.el-message-box__message')
		return m ? m.innerText.trim() : ''
	}
	H.boxBtns = () => (H.box() ? [...H.box().querySelectorAll('button')].map(b => (b.innerText.trim() === '' ? 'x' : b.innerText.trim())).join(',') : 'NO_BOX')
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
	// 子抽屉未开 append-to-body，overlay 置隐有延迟：固定 sleep 会把「正在关」误报成「关不掉」，改成轮询并顺带量出关闭耗时
	H.waitTestDrawerHidden = async maxMs => {
		const t0 = performance.now()
		while (performance.now() - t0 < maxMs) {
			if (!H.testDrawer()) {
				return Math.round(performance.now() - t0)
			}
			if (H.box()) {
				return 'BOX_AT_' + Math.round(performance.now() - t0)
			}
			await sleep(120)
		}
		return 'STILL'
	}
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
			for (let k = 0; k < 10; k++) {
				await sleep(200)
				if (!H.mainReady() && !H.mainDrawer()) {
					break
				}
			}
			await sleep(400)
		}
		return { drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length, boxes: [...document.querySelectorAll('.el-message-box')].filter(vis).length }
	}
	H.btn = scope => text => [...scope.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim() === text)
	H.open = async () => {
		const rows = [...document.querySelectorAll('tbody tr')].filter(vis)
		const idx = rows.findIndex(r => r.innerText.indexOf(ROW_TEXT) >= 0)
		const row = idx < 0 ? null : rows[idx]
		if (!row) {
			return { ok: false, why: 'ROW_MISMATCH:' + rows.slice(0, 6).map(r => r.innerText.replace(/\s+/g, '').slice(0, 12)).join('/') }
		}
		const b = H.btn(row)(H.BTN)
		if (!b) {
			return { ok: false, why: 'BTN_MISSING:' + H.BTN }
		}
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

	// ---- 布局量测对象
	H.splitter = d => d.querySelector('.editor-splitter')
	H.area = d => d.querySelector('.editor-area')
	H.editorBox = d => {
		const s = H.splitter(d)
		return s ? [...s.children].find(c => c.querySelector('.el-tabs')) || null : null
	}
	H.varBox = d => {
		const s = H.splitter(d)
		return s ? [...s.children].find(c => c.querySelector('.variable-panel')) || null : null
	}
	H.edge = d => d.querySelector('.variable-edge')
	// 以下三个 helper 必须排除嵌套在主抽屉 DOM 内的「模板测试」子抽屉：
	// test.vue 的 el-drawer 未开 append-to-body，子抽屉整棵树挂在主抽屉内部，
	// 全子树查询会把子抽屉的 tab / cm-editor 混进来造成探针污染。
	H.tabs = d => [...d.querySelectorAll('.el-tabs__item')].filter(t => vis(t) && !t.closest('.template-test-drawer'))
	H.tabSeq = d =>
		H.tabs(d)
			.map(t => t.id.replace(/^tab-/, '') + (t.classList.contains('is-active') ? '*' : '') + '=' + t.innerText.trim().replace(/\s+/g, ''))
			.join(',')
	H.navDump = d => {
		const n = [...d.querySelectorAll('.el-tabs__nav')].find(x => !x.closest('.template-test-drawer'))
		return n ? [...n.children].map(c => (c.className || '').split(' ')[0] + (c.id ? ':' + c.id.replace(/^tab-/, '') : '')).join('|') : 'NO_NAV'
	}
	H.cm = scope => {
		const a = [...scope.querySelectorAll('.cm-content')].filter(c => vis(c) && !c.closest('.template-test-drawer'))
		return a.length ? a[0] : null
	}
	H.codeWrap = d => {
		const eb = H.editorBox(d)
		return eb ? eb.querySelector('.el-main .el-scrollbar__wrap') : null
	}
	H.pathEl = d => {
		const eb = H.editorBox(d)
		return eb ? eb.querySelector('.text-tooltip-title') : null
	}
	H.toasts = () => [...document.querySelectorAll('.el-message')].filter(vis).map(m => m.innerText.trim().replace(/\s+/g, '')).join('|')

	// ---- 树节点
	H.dirNodes = d => [...d.querySelectorAll('.el-tree-node')].filter(n => n.querySelector(':scope > .el-tree-node__children'))
	H.fileNodes = d => [...d.querySelectorAll('.el-tree-node')].filter(n => !n.querySelector(':scope > .el-tree-node__children'))
	H.nodeId = n => n.getAttribute('data-key')
	H.nodeLabel = n => n.querySelector(':scope > .el-tree-node__content .custom-tree-node > div')
	H.nodeExpanded = n => n.classList.contains('is-expanded')
	H.nodeChecked = n => {
		const c = n.querySelector(':scope > .el-tree-node__content .el-checkbox')
		return c ? (c.classList.contains('is-checked') ? 'Y' : c.classList.contains('is-indeterminate') ? 'I' : 'N') : 'NO_BOX'
	}
	H.checkedTotal = d => [...d.querySelectorAll('.el-tree .el-checkbox')].filter(c => c.classList.contains('is-checked')).length
	H.tabIds = d => H.tabs(d).map(t => t.id.replace(/^tab-/, ''))

	// ============================ N0a 正文 ============================
	H.spyReq()
	H.spyToast()
	const mark0 = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(900) // 等 useElementSize（debounce 300ms）把容器宽度量到位

	const s = H.splitter(d)
	const recon = s ? [...s.children].map(c => (c.className || 'NOCLS') + '|' + px(c)).join(' ; ') : 'NO_SPLITTER'
	const SE0 = s ? Math.round(s.clientWidth) : -1
	const edgeBefore = H.edge(d)
	H.F.push('N0_area=' + (H.area(d) ? 'Y' : 'N'))
	H.F.push('N0_se0=' + SE0 + ',edgeW=' + px(edgeBefore) + ',edgeVis=' + (vis(edgeBefore) ? 'Y' : 'N'))

	const raw = Math.round(SE0 * 0.28)
	const floor = Math.max(0, Math.min(320, SE0 - 240))
	const upper = Math.max(240, Math.min(560, SE0 - floor))
	const expect = Math.min(Math.max(raw, 240), upper)

	const varBtn = H.btn(d)('变量')
	H.F.push('N0_varBtn=' + (varBtn ? 'Y' : 'N') + ',btnType=' + (varBtn ? (varBtn.classList.contains('el-button--primary') ? 'primary' : 'default') : 'NA'))
	if (!varBtn) {
		return { ABORT: 'VAR_BTN_MISSING', recon }
	}
	varBtn.click()
	await sleep(700)
	const vb = H.varBox(d)
	const eb = H.editorBox(d)
	const PW1 = px(vb)
	const EPW1 = px(eb)
	H.F.push('N0_pw1=' + PW1 + ',epw1=' + EPW1 + ',raw=' + raw + ',floor=' + floor + ',upper=' + upper + ',expect=' + expect + ',hit=' + (Math.abs(PW1 - expect) <= 1 ? 'Y' : 'N'))
	H.F.push('N0_edgeAfterVis=' + (vis(H.edge(d)) ? 'Y' : 'N') + ',panelMounted=' + (vb ? 'Y' : 'N') + ',panelRows=' + (d.querySelectorAll('.variable-panel').length))
	// 收起，把「保留 vs 重算」的判别留给 N0b/N0c（panelSizeInitialized 已置 true，不再按 28% 重算）
	H.btn(d)('变量').click()
	await sleep(600)
	H.F.push('N0_collapseBack=' + (H.varBox(d) ? 'Y' : 'N') + ',edgeBack=' + (vis(H.edge(d)) ? 'Y' : 'N'))

	H.SE0 = SE0
	H.PW1 = PW1
	H.reqBase = mark0

	H.I.push('btn=' + H.BTN)
	H.I.push('drawerTitle=' + ((d.querySelector('.el-drawer__title') || {}).innerText || '').trim().replace(/\s+/g, ''))
	H.I.push('treePanel=' + (d.querySelector('.tree-panel') ? 'Y' : 'N') + ',editorPanel=' + (d.querySelector('.editor-panel') ? 'Y' : 'N') + ',varPanel=' + (d.querySelector('.variable-panel') ? 'Y' : 'N'))
	H.I.push('recon=' + recon)
	H.I.push('vw=' + window.innerWidth + ',vh=' + window.innerHeight + ',dpr=' + window.devicePixelRatio)

	const reqs = H.stageReq(mark0)
	return Object.assign(
		{
			stage: 'N0a',
			SE0,
			PW1,
			EPW1,
			raw,
			floor,
			upper,
			expect,
			recon,
			reqCount: reqs.length,
			writeReq: H.writeReq(reqs).join(';') || 'NONE',
			drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE N0b — F25 中度窄（外层先把视口调到 SE≈620 再跑）：保留 vs 重算 判别
 *   判别力来源：retain=clamp(PW1) 与 recompute=clamp(round(SE*0.28)) 在 SE≈620 处分岔
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()
	await sleep(700) // resize 后等 useElementSize 重新量
	const s = H.splitter(d)
	const SE1 = s ? Math.round(s.clientWidth) : -1
	const floor1 = Math.max(0, Math.min(320, SE1 - 240))
	const upper1 = Math.max(240, Math.min(560, SE1 - floor1))
	const recompute = Math.min(Math.max(Math.round(SE1 * 0.28), 240), upper1)
	const retain = Math.min(Math.max(H.PW1, 240), upper1)

	const vbBtn = H.btn(d)('变量')
	if (!vbBtn) {
		return { ABORT: 'VAR_BTN_MISSING', SE1 }
	}
	vbBtn.click()
	await sleep(700)
	const box = H.varBox(d)
	const PW2 = H.px(box)
	const EPW2 = H.px(H.editorBox(d))
	const sum = PW2 + EPW2
	const branch = Math.abs(PW2 - retain) <= 1 && Math.abs(PW2 - recompute) > 1 ? 'RETAIN' : Math.abs(PW2 - recompute) <= 1 && Math.abs(PW2 - retain) > 1 ? 'RECOMPUTE' : Math.abs(retain - recompute) <= 1 ? 'AMBIG' : 'OTHER'
	H.F.push('N0b_se=' + SE1 + ',pw=' + PW2 + ',epw=' + EPW2 + ',sumVsSe=' + sum + '/' + SE1)
	H.F.push('N0b_floor=' + floor1 + ',upper=' + upper1 + ',retain=' + retain + ',recompute=' + recompute + ',branch=' + branch)
	H.btn(d)('变量').click()
	await sleep(500)
	H.F.push('N0b_writeReq=' + (H.writeReq(H.stageReq(mark)).join(';') || 'NONE'))
	H.I.push('N0b_pwStyle=' + (box ? getComputedStyle(box).width : 'NA') + ',flex=' + (box ? getComputedStyle(box).flexBasis : 'NA'))
	H.I.push('N0b_pw1Base=' + H.PW1 + ',edgeVis=' + (H.vis(H.edge(d)) ? 'Y' : 'N'))
	const reqs = H.stageReq(mark)
	return Object.assign({ stage: 'N0b', SE1, PW2, EPW2, retain, recompute, branch, reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE' }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE N0c — F25 极窄退让（外层把视口压到 SE<560 再跑）：编辑区下限退让、面板保 240
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()
	await sleep(700)
	const s = H.splitter(d)
	const SE2 = s ? Math.round(s.clientWidth) : -1
	const floor2 = Math.max(0, Math.min(320, SE2 - 240))
	const upper2 = Math.max(240, Math.min(560, SE2 - floor2))
	const vbBtn = H.btn(d)('变量')
	if (!vbBtn) {
		return { ABORT: 'VAR_BTN_MISSING', SE2 }
	}
	vbBtn.click()
	await sleep(700)
	const box = H.varBox(d)
	const PW3 = H.px(box)
	const eBox = H.editorBox(d)
	const EPW3 = H.px(eBox)
	const sum = PW3 + EPW3
	const splitterW = s ? Math.round(s.getBoundingClientRect().width) : -1
	H.F.push('N0c_se=' + SE2 + ',rectSe=' + splitterW + ',pw=' + PW3 + ',epw=' + EPW3 + ',sum=' + sum)
	H.F.push('N0c_floor=' + floor2 + ',upper=' + upper2 + ',yield=' + (EPW3 < 320 && PW3 >= 240 ? 'EDITOR_YIELDS' : PW3 < 240 ? 'PANEL_BELOW_MIN' : 'NO_YIELD'))
	// 竖柄最小宽度与面板 min 是否同时兜住
	H.F.push('N0c_panelMin=' + (box ? getComputedStyle(box).minWidth : 'NA') + ',edge=' + (H.vis(H.edge(d)) ? 'Y' : 'N'))
	H.btn(d)('变量').click()
	await sleep(500)
	H.F.push('N0c_writeReq=' + (H.writeReq(H.stageReq(mark)).join(';') || 'NONE'))
	H.I.push('N0c_vw=' + window.innerWidth + ',vh=' + window.innerHeight)
	const reqs = H.stageReq(mark)
	return Object.assign({ stage: 'N0c', SE2, PW3, EPW3, sum, splitterW, floor2, upper2, reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE' }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE N0d — F25 复原收口（外层已把视口调回开局尺寸）：宽度回落 + 关抽屉
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()
	await sleep(700)
	const s = H.splitter(d)
	const SE3 = s ? Math.round(s.clientWidth) : -1
	const vbBtn = H.btn(d)('变量')
	vbBtn.click()
	await sleep(700)
	const PW4 = H.px(H.varBox(d))
	const floor3 = Math.max(0, Math.min(320, SE3 - 240))
	const upper3 = Math.max(240, Math.min(560, SE3 - floor3))
	const expect3 = Math.min(Math.max(H.PW1, 240), upper3)
	H.F.push('N0d_se=' + SE3 + ',seBack=' + (Math.abs(SE3 - H.SE0) <= 1 ? 'Y' : 'N') + ',pw=' + PW4 + ',expect=' + expect3 + ',hit=' + (Math.abs(PW4 - expect3) <= 1 ? 'Y' : 'N'))
	H.btn(d)('变量').click()
	await sleep(500)
	const fc = await H.forceCloseMain()
	H.F.push('N0d_writeReq=' + (H.writeReq(H.stageReq(mark)).join(';') || 'NONE'))
	H.I.push('N0d_vw=' + window.innerWidth + ',closed=' + JSON.stringify(fc))
	const reqs = H.stageReq(mark)
	return Object.assign({ stage: 'N0d', SE3, PW4, expect3, closed: fc, reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE' }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE N1 — F05 单击节点：目录（只翻展开，不动勾选）/ 未打开文件 / 已在 tab 的文件
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(800)

	// ---- A) 单击一个未展开的目录
	const dirs = H.dirNodes(d)
	const dir = dirs.find(n => !H.nodeExpanded(n)) || dirs[0]
	if (!dir) {
		return { ABORT: 'NO_DIR_NODE' }
	}
	const dirId = H.nodeId(dir)
	const beforeA = { tabs: H.tabs(d).length, active: H.tabIds(d).join(','), checked: H.checkedTotal(d), dirChecked: H.nodeChecked(dir), dirExpanded: H.nodeExpanded(dir) ? 'Y' : 'N' }
	H.nodeLabel(dir).click()
	await sleep(800)
	const dirNow = d.querySelector('[data-key="' + dirId + '"]')
	const afterA = { tabs: H.tabs(d).length, active: H.tabIds(d).join(','), checked: H.checkedTotal(d), dirChecked: H.nodeChecked(dirNow), dirExpanded: H.nodeExpanded(dirNow) ? 'Y' : 'N' }
	H.F.push('N1_dir_id=' + dirId + ',tabs=' + beforeA.tabs + '>' + afterA.tabs + ',activeSame=' + (beforeA.active === afterA.active ? 'Y' : 'N'))
	H.F.push('N1_dir_expand=' + beforeA.dirExpanded + '>' + afterA.dirExpanded + ',checkedFlip=' + (beforeA.dirChecked === afterA.dirChecked ? 'N' : 'Y') + ',checkedTotal=' + beforeA.checked + '>' + afterA.checked)

	// ---- B) 单击一个不在 tab 里的文件（该目录展开后的子文件优先）
	const tabIds = H.tabIds(d)
	const kids = dirNow ? [...dirNow.querySelectorAll('.el-tree-node')].filter(n => !n.querySelector(':scope > .el-tree-node__children')) : []
	const cand = kids.find(n => !tabIds.includes(H.nodeId(n))) || H.fileNodes(d).find(n => !tabIds.includes(H.nodeId(n)))
	if (!cand) {
		return { ABORT: 'NO_UNOPENED_FILE' }
	}
	const fileId = H.nodeId(cand)
	const wrap = d.querySelector('.tree-scroll-wrapper').closest('.el-scrollbar__wrap')
	const beforeB = { tabs: H.tabs(d).length }
	H.nodeLabel(cand).click()
	await sleep(1000)
	const nodeNow = d.querySelector('[data-key="' + fileId + '"]')
	const nr = nodeNow.getBoundingClientRect()
	const wr = wrap.getBoundingClientRect()
	const inView = nr.top >= wr.top - 2 && nr.bottom <= wr.bottom + 2
	const ebNow = H.editorBox(d)
	const cm = H.cm(ebNow)
	const cw = H.codeWrap(d)
	const afterB = {
		tabs: H.tabs(d).length,
		activeId: (H.tabs(d).find(t => t.classList.contains('is-active')) || {}).id,
		isCurrent: nodeNow.classList.contains('is-current') ? 'Y' : 'N',
		checkedFlip: beforeA.checked !== H.checkedTotal(d) ? 'Y' : 'N'
	}
	H.F.push('N1_file_id=' + fileId + ',tabs=' + beforeB.tabs + '>' + afterB.tabs + ',activeIsFile=' + (String(afterB.activeId) === 'tab-' + fileId ? 'Y' : 'N'))
	H.F.push('N1_file_isCurrent=' + afterB.isCurrent + ',inView=' + (inView ? 'Y' : 'N') + ',scrollTop=' + (cw ? Math.round(cw.scrollTop) : -1) + ',cmLen=' + (cm ? cm.innerText.length : -1) + ',cmHash=' + H.hs(cm ? cm.innerText.slice(0, 400) : null))
	H.F.push('N1_file_checkedFlip=' + afterB.checkedFlip + ',tabLabel=' + H.hs((H.tabs(d).find(t => 'tab-' + fileId === t.id) || {}).innerText))

	// ---- C) 单击一个已在 tab 里的文件：tab 数不变，只切激活
	const other = H.tabs(d).find(t => t.id !== 'tab-' + fileId)
	const otherId = other ? other.id.replace(/^tab-/, '') : null
	const beforeC = { tabs: H.tabs(d).length }
	if (otherId) {
		const on = d.querySelector('[data-key="' + otherId + '"]')
		if (on && H.vis(on)) {
			H.nodeLabel(on).click()
			await sleep(800)
			H.F.push('N1_reclick_tabs=' + beforeC.tabs + '>' + H.tabs(d).length + ',activeSwitch=' + ((H.tabs(d).find(t => t.classList.contains('is-active')) || {}).id === 'tab-' + otherId ? 'Y' : 'N'))
		} else {
			H.F.push('N1_reclick=NODE_OFFSCREEN')
		}
	} else {
		H.F.push('N1_reclick=NO_OTHER_TAB')
	}

	const reqs = H.stageReq(mark)
	H.F.push('N1_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.F.push('N1_toasts=' + H.hs(H.TOASTS.join('|')))
	H.I.push('N1_tabSeq=' + H.tabSeq(d))
	H.I.push('N1_nav=' + H.navDump(d))
	const out = Object.assign({ stage: 'N1', beforeA, afterA, fileId, inView, reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE', toasts: H.TOASTS }, H.out())
	await H.forceCloseMain()
	return out
}

/* -----------------------------------------------------------------------------
 * STAGE N2a — F17 前置：凑 ≥4 个 tab，记录序与 drag 目标（drag 由外层 browser_drag 完成）
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(800)

	// 点够 4 个文件：优先挑可见叶子，逐个点标签
	const want = 4
	let guard = 0
	while (H.tabs(d).length < want && guard++ < 12) {
		const ids = H.tabIds(d)
		const next = H.fileNodes(d).find(n => !ids.includes(H.nodeId(n)) && H.vis(n))
		if (!next) {
			// 展开下一个目录再找
			const nd = H.dirNodes(d).filter(x => !H.nodeExpanded(x))[0]
			if (!nd) {
				break
			}
			H.nodeLabel(nd).click()
			await sleep(600)
			continue
		}
		H.nodeLabel(next).click()
		await sleep(600)
	}
	const seq = H.tabSeq(d)
	const ids = H.tabIds(d)
	H.F.push('N2a_tabCount=' + ids.length + ',seqHash=' + H.hs(seq) + ',seqLen=' + seq.length)
	H.F.push('N2a_active=' + ((H.tabs(d).find(t => t.classList.contains('is-active')) || {}).id || 'NA'))
	H.F.push('N2a_navBefore=' + H.navDump(d))
	const reqs = H.stageReq(mark)
	H.F.push('N2a_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.DRAG = ids.length >= 4 ? { from: ids[0], to: ids[ids.length - 1], seq, ids0: ids.length, active: (H.tabs(d).find(t => t.classList.contains('is-active')) || {}).id } : null
	H.I.push('N2a_seqRaw=' + seq)
	H.I.push('N2a_vw=' + window.innerWidth)
	return Object.assign({ stage: 'N2a', ids, seq, drag: H.DRAG, startSelector: H.DRAG ? '#tab-' + H.DRAG.from + ' .tab-label' : null, endSelector: H.DRAG ? '#tab-' + H.DRAG.to + ' .tab-label' : null, reqCount: reqs.length }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE N2b — F17 后置：外层真 drag 完成后读新序，并验 Vue 数据序与 DOM 序是否同步
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	const d = H.mainReady() || H.mainDrawer()
	if (!d || !H.DRAG) {
		return { ABORT: 'NO_DRAG_TARGET' }
	}
	await sleep(600)
	const seqAfter = H.tabSeq(d)
	const idsAfter = H.tabIds(d)
	const activeAfter = (H.tabs(d).find(t => t.classList.contains('is-active')) || {}).id
	H.F.push('N2b_seqChanged=' + (seqAfter !== H.DRAG.seq ? 'Y' : 'N') + ',seqHash=' + H.hs(seqAfter) + ',seqLen=' + seqAfter.length)
	H.F.push('N2b_activeIsMoved=' + (activeAfter === 'tab-' + H.DRAG.from ? 'Y' : 'N') + ',count=' + idsAfter.length + '>' + H.DRAG.ids0)
	H.F.push('N2b_navAfter=' + H.navDump(d))
	// Vue 重渲染纠偏探针：点另一个 tab 再点回被拖的 tab，看顺序是否被打回
	const victim = idsAfter.find(x => 'tab-' + x !== activeAfter)
	if (victim) {
		const vt = d.querySelector('#tab-' + victim)
		if (vt) {
			vt.click()
			await sleep(600)
			const back = d.querySelector('#tab-' + H.DRAG.from)
			if (back) {
				back.click()
				await sleep(600)
			}
			H.F.push('N2b_seqAfterReact=' + H.hs(H.tabSeq(d)) + ',stable=' + (H.tabSeq(d) === seqAfter ? 'Y' : 'N'))
		}
	}
	const cm = H.cm(H.editorBox(d))
	H.F.push('N2b_cmHash=' + H.hs(cm ? cm.innerText.slice(0, 400) : null))
	const reqs = H.stageReq(mark)
	H.F.push('N2b_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.I.push('N2b_seqRaw=' + seqAfter)
	H.I.push('N2b_dragFrom=' + H.DRAG.from + ',to=' + H.DRAG.to)
	const out = Object.assign({ stage: 'N2b', before: H.DRAG.seq, after: seqAfter, idsAfter, reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE' }, H.out())
	await H.forceCloseMain()
	return out
}

/* -----------------------------------------------------------------------------
 * STAGE N3 — F18 路径显示：前缀、跟随切换、过长省略与 tooltip
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(800)
	const readPath = () => {
		const el = H.pathEl(d)
		if (!el) {
			return { has: 'N' }
		}
		const cs = getComputedStyle(el)
		return { has: 'Y', text: (el.innerText || '').trim(), sw: Math.round(el.scrollWidth), cw: Math.round(el.clientWidth), to: cs.textOverflow, disp: cs.display, of: cs.overflow, ws: cs.whiteSpace }
	}
	const p1 = readPath()
	if (!p1.has) {
		return { ABORT: 'NO_PATH_EL' }
	}
	H.F.push('N3_p1_has=' + p1.has + ',prefix=' + (p1.text.indexOf('路径：') === 0 ? 'Y' : 'N') + ',hash=' + H.hs(p1.text) + ',len=' + p1.text.length)
	H.F.push('N3_p1_overflow=' + (p1.sw > p1.cw + 1 ? 'Y' : 'N') + ',sw=' + p1.sw + ',cw=' + p1.cw + ',textOverflow=' + p1.to + ',display=' + p1.disp + ',whiteSpace=' + p1.ws)

	// tooltip：合成 mouseenter 触发 EP tooltip
	const el = H.pathEl(d)
	el.dispatchEvent(new MouseEvent('mouseenter', { bubbles: true }))
	el.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }))
	await sleep(600)
	const pop = [...document.querySelectorAll('.el-popper')].filter(vis).map(p => p.innerText.trim()).filter(t => t.length > 0 && t.indexOf('路径：') !== 0)
	H.F.push('N3_tipShown=' + (pop.length ? 'Y' : 'N') + ',tipHash=' + H.hs(pop.join('|')))

	// 切到路径最长的另一个文件，验跟随与省略
	const files = H.fileNodes(d).filter(n => !H.tabIds(d).includes(H.nodeId(n)))
	const byLen = files.map(n => ({ id: H.nodeId(n), len: ((H.nodeLabel(n) || { innerText: '' }).innerText || '').length })).sort((a, b) => b.len - a.len)
	if (byLen.length) {
		const t = d.querySelector('[data-key="' + byLen[0].id + '"]')
		if (t && H.vis(t)) {
			H.nodeLabel(t).click()
			await sleep(900)
			const p2 = readPath()
			H.F.push('N3_follow=Y,hashDiff=' + (p1.text !== p2.text ? 'Y' : 'N') + ',p2Hash=' + H.hs(p2.text) + ',p2Len=' + p2.text.length + ',p2Overflow=' + (p2.sw > p2.cw + 1 ? 'Y' : 'N') + ',sw=' + p2.sw + ',cw=' + p2.cw)
		} else {
			H.F.push('N3_follow=NODE_OFFSCREEN')
		}
	} else {
		H.F.push('N3_follow=NO_MORE_FILE')
	}
	const reqs = H.stageReq(mark)
	H.F.push('N3_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.I.push('N3_p1Raw=' + p1.text)
	H.I.push('N3_p2Raw=' + ((readPath().text || '').slice(0, 200)))
	H.I.push('N3_tips=' + pop.join('|').slice(0, 120))
	const out = Object.assign({ stage: 'N3', p1, tips: pop.slice(0, 3), reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE' }, H.out())
	await H.forceCloseMain()
	return out
}

/* -----------------------------------------------------------------------------
 * STAGE N4 — F20 测试子抽屉：入口显隐、抽屉结构、渲染禁用态、关闭后主抽屉不变
 *   只打开与观察，绝不点「渲染」（走生成链路）
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(800)
	const eb = H.editorBox(d)
	const cmBefore = H.cm(eb)
	const hashBefore = H.hs(cmBefore ? cmBefore.innerText.slice(0, 400) : null)
	const tabsBefore = H.tabSeq(d)
	const testBtn = H.btn(d)('测试')
	H.F.push('N4_testBtn=' + (testBtn ? 'Y' : 'N') + ',activeType1=' + (H.tabs(d).find(t => t.classList.contains('is-active')) ? 'Y' : 'N'))
	if (!testBtn) {
		return { ABORT: 'NO_TEST_BTN', hashBefore, tabsBefore }
	}
	testBtn.click()
	await sleep(1200)
	const td = H.testDrawer() || [...document.querySelectorAll('.el-drawer')].filter(vis).filter(x => x !== d).pop()
	if (!td) {
		return { ABORT: 'NO_TEST_DRAWER' }
	}
	const title = ((td.querySelector('.el-drawer__title') || {}).innerText || '').trim().replace(/\s+/g, '')
	const tTabs = [...td.querySelectorAll('.el-tabs__item')].map(t => t.innerText.trim().replace(/\s+/g, '') + (t.classList.contains('is-active') ? '*' : '')).join(',')
	const btns = [...td.querySelectorAll('button')].filter(vis).map(b => (b.innerText.trim() || 'ICON') + (b.disabled ? ':dis' : ':ena')).join(',')
	const renderBtn = [...td.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim().indexOf('渲染') >= 0)
	const refreshBtn = [...td.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim().indexOf('刷新') >= 0)
	H.F.push('N4_title=' + H.hs(title) + ',len=' + title.length)
	H.F.push('N4_tabs=' + H.hs(tTabs) + ',tabsLen=' + tTabs.length + ',btnsHash=' + H.hs(btns))
	H.F.push('N4_render=' + (renderBtn ? (renderBtn.disabled ? 'dis' : 'ena') : 'NO_BTN') + ',refresh=' + (refreshBtn ? (refreshBtn.disabled ? 'dis' : 'ena') : 'NO_BTN'))
	const sels = [...td.querySelectorAll('.el-select')].length
	const ph = [...td.querySelectorAll('.el-select .el-select__placeholder,.el-select input')].map(e => ((e.innerText || e.value || e.placeholder || '') + '').trim()).join('|')
	H.F.push('N4_selects=' + sels + ',phHash=' + H.hs(ph))
	H.TOASTS.length = 0
	H.closeDrawerX(td)
	const goneMs = await H.waitTestDrawerHidden(4000)
	// 子抽屉若弹关闭确认框，先记文案再只点「取消/否(N)」：确定/是(Y) 可能落库，绝不碰
	const closeBoxMsg = H.boxMsg()
	const closeBoxBtns = H.boxBtns()
	await H.killBoxes()
	const cmAfter = H.cm(H.editorBox(d))
	const hashAfter = H.hs(cmAfter ? cmAfter.innerText.slice(0, 400) : null)
	const saveEl = H.btn(d)('保存')
	const dotN = [...d.querySelectorAll('.el-tree-node .el-badge__content.is-dot')].filter(x => x.style.display !== 'none').length
	H.F.push('N4_hashSame=' + (hashAfter === hashBefore ? 'Y' : 'N') + ',seqSame=' + (H.tabSeq(d) === tabsBefore ? 'Y' : 'N') + ',tabs=' + H.tabs(d).length)
	H.F.push('N4_saveDis=' + (saveEl ? (saveEl.disabled ? 'Y' : 'N') : 'NA') + ',treeDots=' + dotN + ',testGone=' + (typeof goneMs === 'number' ? 'CLOSED' : goneMs.indexOf('BOX') === 0 ? 'BOX' : 'STILL'))
	H.F.push('N4_closeBox=' + (closeBoxMsg === null ? 'NO_BOX' : H.hs(closeBoxMsg) + '/' + closeBoxBtns))
	const reqs = H.stageReq(mark)
	H.F.push('N4_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.F.push('N4_toasts=' + H.hs(H.TOASTS.join('|')))
	H.I.push('N4_reqs=' + reqs.join(',').slice(0, 200))
	H.I.push('N4_goneMs=' + goneMs)
	H.I.push('N4_btnsRaw=' + btns.slice(0, 160))
	H.I.push('N4_tabsRaw=' + tTabs.slice(0, 120))
	H.I.push('N4_phRaw=' + ph.slice(0, 120))
	const out = Object.assign({ stage: 'N4', title, tTabs, btns, renderDisabled: renderBtn ? renderBtn.disabled : null, selects: sels, ph, reqCount: reqs.length, writeReq: H.writeReq(reqs).join(';') || 'NONE' }, H.out())
	await H.forceCloseMain()
	return out
}

/* -----------------------------------------------------------------------------
 * STAGE N5a — F21 伪全屏：CSS 铺满 + 未进原生全屏（真 ESC 由外层发）
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(800)
	const area = H.area(d)
	if (!area) {
		return { ABORT: 'NO_EDITOR_AREA' }
	}
	const r0 = area.getBoundingClientRect()
	H.BEFORE = { w: Math.round(r0.width), h: Math.round(r0.height), t: Math.round(r0.top), l: Math.round(r0.left) }
	H.FSCH0 = H.FSCH
	const fsBtn = H.btn(d)('全屏')
	if (!fsBtn) {
		return { ABORT: 'NO_FULLSCREEN_BTN' }
	}
	fsBtn.click()
	await sleep(700)
	const r1 = area.getBoundingClientRect()
	const cs = getComputedStyle(area)
	const vw = window.innerWidth
	const vh = window.innerHeight
	H.F.push('N5a_cls=' + (area.classList.contains('full-screen-mode') ? 'Y' : 'N') + ',pos=' + cs.position + ',z=' + cs.zIndex)
	// 只用宽高判定，不用 left/top：标签页合成帧被饥饿时 transform 会停在 transition 起始值（实测可差 1441px）
	H.F.push('N5a_fitWH=' + (Math.abs(r1.width - vw) <= 2 && Math.abs(r1.height - vh) <= 2 ? 'FULL' : 'PARTIAL') + ',w=' + Math.round(r1.width) + '/' + vw + ',h=' + Math.round(r1.height) + '/' + vh)
	H.F.push('N5a_nativeFs=' + (document.fullscreenElement ? 'YES' : 'NULL') + ',fschDelta=' + (H.FSCH - H.FSCH0))
	H.F.push('N5a_btnText=' + ((H.btn(d)('退出') || H.btn(d)('全屏') || { innerText: 'NA' }).innerText || '').trim())
	H.I.push('N5a_before=' + JSON.stringify(H.BEFORE))
	H.I.push('N5a_afterRect=' + JSON.stringify({ l: Math.round(r1.left), t: Math.round(r1.top), w: Math.round(r1.width), h: Math.round(r1.height) }))
	const reqs = H.stageReq(mark)
	H.I.push('N5a_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	return Object.assign({ stage: 'N5a', before: H.BEFORE, after: { w: Math.round(r1.width), h: Math.round(r1.height), l: Math.round(r1.left), t: Math.round(r1.top) }, vw, vh, nativeFs: !!document.fullscreenElement, pos: cs.position, z: cs.zIndex, reqCount: reqs.length }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE N5b — F21 后置：外层发过真 ESC 后观察
 *   ESC 可能被 el-drawer 的 close-on-press-escape 吃掉直接关掉主抽屉，
 *   所以先记录抽屉是否存活，必要时重开再看全屏态是否残留，最后点「退出」验尺寸
 * ========================================================================== */
async () => {
	const H = window.__HN
	if (!H) {
		return { ABORT: 'RUN_N0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, px = H.px
	const mark = H.reqMark()
	let d = H.mainReady() || H.mainDrawer()
	const drawerAlive = !!d && vis(d)
	if (!drawerAlive) {
		const op = await H.open()
		if (!op.ok) {
			return { ABORT: 'REOPEN_FAIL', escClosedMain: 'Y' }
		}
		d = op.drawer
		await sleep(1200)
	}
	const area = H.area(d)
	if (!area) {
		return { ABORT: 'NO_EDITOR_AREA' }
	}
	H.F.push('N5b_escClosedMain=' + (drawerAlive ? 'N' : 'Y') + ',afterEsc=' + (area.classList.contains('full-screen-mode') ? 'STILL' : 'EXITED') + ',fsch=' + (H.FSCH - H.FSCH0) + ',nativeFs=' + (document.fullscreenElement ? 'YES' : 'NULL'))
	const exitBtn = H.btn(d)('退出') || H.btn(d)('全屏')
	if (!exitBtn) {
		return { ABORT: 'NO_TOGGLE_BTN', F: H.F, fpBehav: H.fnv(H.F.join('\n')) }
	}
	const rMid = area.getBoundingClientRect()
	H.F.push('N5b_btnNow=' + exitBtn.innerText.trim() + ',midW=' + Math.round(rMid.width) + '/' + window.innerWidth)
	exitBtn.click()
	await sleep(900)
	const r2 = area.getBoundingClientRect()
	const b = H.BEFORE
	H.F.push('N5b_restored=' + (Math.abs(Math.round(r2.width) - b.w) <= 2 && Math.abs(Math.round(r2.height) - b.h) <= 2 ? 'Y' : 'N') + ',w=' + Math.round(r2.width) + '/' + b.w + ',h=' + Math.round(r2.height) + '/' + b.h)
	H.F.push('N5b_cls=' + (area.classList.contains('full-screen-mode') ? 'Y' : 'N'))
	const reqs = H.stageReq(mark)
	H.F.push('N5b_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.I.push('N5b_btnText=' + ((H.btn(d)('全屏') || { innerText: 'NA' }).innerText || '').trim())
	const out = Object.assign({ stage: 'N5b', drawerAlive, after: { w: Math.round(r2.width), h: Math.round(r2.height) }, before: b, reqCount: reqs.length }, H.out())
	await H.forceCloseMain()
	return out
}
