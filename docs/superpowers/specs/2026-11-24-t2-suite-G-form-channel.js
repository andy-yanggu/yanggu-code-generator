/* =============================================================================
 * T2 双跑对照 · Suite G（表单通道批次：F12 入口上下文注入 / F13 保存后链路）
 * 日期：2026-11-24    靶点：模板组 80「单体java项目模板_复制」，入口锚 = 根目录 2117
 *
 * 用法（每段是一段独立的 async 箭头函数，顶格书写，装载器按 ^async \(\) => \{$ 切段）
 *   0) 跑前外层先用 SQL 确认无 T2G 残留行（G0 也会自检，残留即 ABORT）；
 *   1) reload 页面 → 装载本文件（setInputFiles + FileReader 取源码，切段存 window.__SUITE_G）；
 *   2) 按 G0 → G1 → G2 → G3 → G4 → G5 → G6 顺序跑；
 *   3) 换版本只改 window.__BTN：v2 = '模板v2'，v1 = '模板'。文件本身一字不改，两版跑同一份源码；
 *   4) 每段前外层做 page.bringToFront()（合成帧饥饿会让 transform 停在起始值，见 Suite N 教训）。
 *
 * 写库口径（本批是 T2 里唯一必须写库的批次）
 *   - 只写自建 fixture：目录 T2GDIR、模板文件 T2GF.java。共 3 次写（POST /add ×2 + PUT /update ×1）；
 *   - 存量 78 行一律不写：G1 的「修改」入口只读回填后取消，G3 的编辑探测改完立即逐字还原且不点保存；
 *   - 收尾由外层物理 DELETE file_name IN ('T2GDIR','T2GF.java')，再用固定指纹 SQL 复核基线；
 *   - v1 与 v2 之间也必须先物理清理，否则 parent_id 不同的同名行会让 G3 找不到唯一节点。
 *
 * 指纹口径
 *   - F（行为串）：两版必须逐字相同。自增 id、请求原文、耗时一律不进 F（进 I 并注明）；
 *   - I（身份串）：版本身份 + 允许不同但需逐条解释的项（新行 id、DOM 结构 dump、请求体原文）。
 * ========================================================================== */

/* -----------------------------------------------------------------------------
 * STAGE G0 — helper 建立 + 预检 + 「节点右键 → 三个新建项」入口上下文矩阵（零写入）
 * ========================================================================== */
async () => {
	const BTN = window.__BTN || '模板v2'
	const ROW_TEXT = '单体java项目模板_复制'
	const DIR_ID = 2117
	const FILE_ID = 2142
	const FIX_DIR = 'T2GDIR'
	const FIX_FILE = 'T2GF.java'
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

	const H = { BTN, ROW_TEXT, DIR_ID, FILE_ID, FIX_DIR, FIX_FILE, sleep, vis, fnv, hs, F: [], I: [], REQ: [], BODIES: [], TOASTS: [], reqPatched: false, toastPatched: false, LASTM: '', LASTU: '', LASTFULL: '' }
	window.__HG = H

	// ---- 请求 spy：方法+路径进 REQ，非 GET 的请求体原文进 BODIES（F 只取键名列表，原文进 I）
	H.spyReq = () => {
		if (H.reqPatched) {
			return 'ALREADY'
		}
		H.reqPatched = true
		const trim = u => String(u).replace(/^https?:\/\/[^/]+/, '')
		const xo = XMLHttpRequest.prototype.open
		XMLHttpRequest.prototype.open = function (m, u) {
			H.LASTM = String(m).toUpperCase()
			H.LASTU = trim(u)
			H.REQ.push(H.LASTM + ' ' + H.LASTU)
			return xo.apply(this, Array.prototype.slice.call(arguments))
		}
		const xs = XMLHttpRequest.prototype.send
		// LASTFULL 不截断：PUT /update 回传整个 VO，正文超 400 字符，BODIES 的截断副本只供 I 串比对，键名取证走 LASTFULL
		XMLHttpRequest.prototype.send = function (b) {
			if (H.LASTM !== 'GET' && H.LASTM !== 'HEAD') {
				H.LASTFULL = String(b == null ? '' : b)
				H.BODIES.push(H.LASTM + ' ' + H.LASTU + ' :: ' + H.LASTFULL.slice(0, 400))
			}
			return xs.apply(this, arguments)
		}
		const fo = window.fetch
		window.fetch = function (a, b) {
			const m = String((b && b.method) || (a && a.method) || 'GET').toUpperCase()
			H.REQ.push(m + ' ' + trim(a && a.url ? a.url : a))
			return fo.apply(this, arguments)
		}
		return 'OK'
	}
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
	H.bodyKeys = () => {
		if (!H.LASTFULL) {
			return 'NO_BODY'
		}
		try {
			return Object.keys(JSON.parse(H.LASTFULL)).sort().join(',')
		} catch (e) {
			return 'UNPARSEABLE'
		}
	}
	H.out = () => {
		const b = H.F.join('\n')
		const i = H.I.join('\n')
		return { fpBehav: fnv(b), behavLen: b.length, fpId: fnv(i), idLen: i.length, F: H.F, I: H.I }
	}

	// ---- 抽屉（必须排除嵌在主抽屉里的「模板测试」子抽屉，见 Suite N 教训 2）
	H.mainDrawer = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0)
	H.mainReady = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0 && d.querySelector('.el-tree-node'))
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
			await sleep(800)
		}
		return n
	}
	H.closeDrawerX = drawer => {
		const x = drawer.querySelector('button[aria-label*="关闭"]') || drawer.querySelector('.el-drawer__close-btn')
		if (x) {
			x.click()
		}
	}
	H.forceCloseMain = async () => {
		for (let i = 0; i < 4; i++) {
			const d = H.mainDrawer()
			if (!d) {
				break
			}
			H.closeDrawerX(d)
			await sleep(700)
			await H.killBoxes()
			for (let k = 0; k < 10; k++) {
				await sleep(200)
				if (!H.mainDrawer()) {
					break
				}
			}
			await sleep(300)
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
					await sleep(700)
					return { ok: true, drawer: H.mainDrawer() || d, attempt, rowIdx: idx, rowCount: rows.length }
				}
			}
		}
		return { ok: false, why: 'TREE_NOT_READY_AFTER_3_CLICKS' }
	}

	// ---- tab / 编辑器 / 路径
	H.tabs = d => [...d.querySelectorAll('.el-tabs__item')].filter(t => vis(t) && !t.closest('.template-test-drawer'))
	H.tabIds = d => H.tabs(d).map(t => t.id.replace(/^tab-/, ''))
	H.activeTabId = d => {
		const a = H.tabs(d).filter(t => t.classList.contains('is-active'))[0]
		return a ? a.id.replace(/^tab-/, '') : 'NONE'
	}
	H.cm = scope => {
		const a = [...scope.querySelectorAll('.cm-content')].filter(c => vis(c) && !c.closest('.template-test-drawer'))
		return a.length ? a[0] : null
	}
	H.pathEl = d => {
		const t = [...d.querySelectorAll('.text-tooltip-title')].filter(x => vis(x) && !x.closest('.template-test-drawer'))[0]
		return t || null
	}
	H.pathText = d => {
		const p = H.pathEl(d)
		return p ? p.innerText.trim().replace(/\s+/g, '') : 'NO_PATH'
	}
	H.saveBtn = d => (H.btn(d)('保存') || null)

	// ---- 树节点
	H.node = (d, id) => [...d.querySelectorAll('.el-tree-node')].find(n => n.getAttribute('data-key') === String(id))
	H.nodeContent = (d, id) => {
		const n = H.node(d, id)
		return n ? n.querySelector(':scope > .el-tree-node__content') : null
	}
	H.nodeLabel = (d, id) => {
		const n = H.node(d, id)
		const l = n ? n.querySelector(':scope > .el-tree-node__content .custom-tree-node > div') : null
		return l ? l.innerText.trim().replace(/\s+/g, '') : 'NO_NODE'
	}
	H.nodeVisible = (d, id) => {
	 const c = H.nodeContent(d, id)
		return c && vis(c) ? 'Y' : 'N'
	}
	H.isExpanded = (d, id) => {
		const n = H.node(d, id)
		return n ? (n.classList.contains('is-expanded') ? 'Y' : 'N') : 'NO_NODE'
	}
	H.parentOf = (d, id) => {
		const n = H.node(d, id)
		if (!n) {
			return 'NO_NODE'
		}
		const p = n.parentElement && n.parentElement.closest('.el-tree-node')
		return p ? p.getAttribute('data-key') : 'ROOT'
	}
	H.allKeys = d => [...d.querySelectorAll('.el-tree-node')].map(n => n.getAttribute('data-key')).join(',')
	H.clickNode = async (d, id) => {
		const c = H.nodeContent(d, id)
		if (c) {
			c.click()
		}
		await sleep(450)
	}
	H.ensureExpanded = async (d, id) => {
		for (let i = 0; i < 3; i++) {
			if (H.isExpanded(d, id) === 'Y') {
				return 'ALREADY'
			}
			if (!H.nodeContent(d, id)) {
				return 'NO_NODE'
			}
			await H.clickNode(d, id)
		}
		return H.isExpanded(d, id) === 'Y' ? 'CLICKED' : 'FAIL'
	}

	// ---- 右键菜单（只发 contextmenu，不补 mousedown/pointerdown，否则会立刻被自身的关闭监听吃掉）
	H.menu = () => [...document.querySelectorAll('.context-menu')].filter(vis)[0]
	H.menuLabels = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		return [...m.querySelectorAll('li')]
			.filter(vis)
			.map(li => li.innerText.trim().replace(/\s+/g, ''))
			.join(',')
	}
	H.menuDom = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const lis = [...m.querySelectorAll('li')]
		return m.className + '|' + lis.filter(x => x.className.indexOf('context-menu__item') >= 0).length + '|' + lis.length
	}
	H.clickMenuItem = async label => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const li = [...m.querySelectorAll('li')].find(x => x.innerText.trim().replace(/\s+/g, '') === label)
		if (!li) {
			return 'ITEM_MISSING'
		}
		li.click()
		await sleep(600)
		return 'CLICKED'
	}
	H.rc = async el => {
		if (!el) {
			return 'NO_TARGET'
		}
		const r = el.getBoundingClientRect()
		const x = Math.round(r.left + Math.min(r.width, 120) / 2)
		const y = Math.round(r.top + r.height / 2)
		el.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true, clientX: x, clientY: y, button: 2 }))
		await sleep(60)
		el.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true, clientX: x, clientY: y, button: 2 }))
		await sleep(500)
		return H.menu() ? 'OPEN' : 'NOT_OPEN'
	}
	H.rcNode = async (d, id) => H.rc(H.nodeContent(d, id))
	H.rcBlank = async d => {
		const w = [...d.querySelectorAll('.el-tree')][0]
		return H.rc(w)
	}

	// ---- 弹窗
	H.dlg = () => [...document.querySelectorAll('.el-dialog')].filter(vis).find(x => !x.closest('.template-test-drawer')) || null
	H.dlgBtn = text => {
		const g = H.dlg()
		if (!g) {
			return null
		}
		return [...g.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim() === text)
	}
	H.dlgTitle = () => {
		const g = H.dlg()
		return g ? (((g.querySelector('.el-dialog__title') || {}).innerText || '').trim().replace(/\s+/g, '')) : 'NO_DLG'
	}
	H.dlgDump = () => {
		const g = H.dlg()
		if (!g) {
			return 'NO_DLG'
		}
		const items = [...g.querySelectorAll('.el-form-item')].filter(vis).map(it => {
			const lb = (((it.querySelector('.el-form-item__label') || {}).innerText || '')).trim().replace(/\s+/g, '')
			const inp = it.querySelector('input:not([type=radio]):not([type=checkbox]), textarea')
			const radios = it.querySelectorAll('.el-radio, .el-radio-button')
			const checked = [...it.querySelectorAll('.el-radio, .el-radio-button')].filter(r => r.className.indexOf('is-checked') >= 0).map(r => r.innerText.trim())
			const ctl = inp ? inp.tagName.toLowerCase() + (inp.disabled ? '/dis' : '/ena') : it.querySelector('.el-upload') ? 'upload' : 'other'
			const val = inp ? inp.value : ''
			return lb + '[' + ctl + (radios.length ? ' r=' + radios.length + ' c=' + (checked[0] || 'NONE') : '') + ']=' + hs(val)
		})
		return items.join(' ;; ')
	}
	H.dlgErrors = () => {
		const g = H.dlg()
		if (!g) {
			return 'NO_DLG'
		}
		return [...g.querySelectorAll('.el-form-item__error')].filter(vis).map(e => e.innerText.trim()).join(',')
	}
	H.setVal = (inp, v) => {
		const proto = inp.tagName === 'TEXTAREA' ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype
		Object.getOwnPropertyDescriptor(proto, 'value').set.call(inp, v)
		inp.dispatchEvent(new Event('input', { bubbles: true }))
		inp.dispatchEvent(new Event('change', { bubbles: true }))
		inp.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: 'Enter' }))
		inp.blur()
	}
	H.dlgInput = label => {
		const g = H.dlg()
		if (!g) {
			return null
		}
		const it = [...g.querySelectorAll('.el-form-item')].find(x => {
			const l = ((x.querySelector('.el-form-item__label') || {}).innerText || '').trim().replace(/\s+/g, '')
			return l === label
		})
		return it ? it.querySelector('input:not([type=radio]):not([type=checkbox]), textarea') : null
	}
	H.fill = async (label, value) => {
		const inp = H.dlgInput(label)
		if (!inp) {
			return 'FIELD_MISSING'
		}
		H.setVal(inp, value)
		await sleep(250)
		return inp.value === value ? 'SET' : 'DIFF:' + inp.value
	}
	H.waitDlgGone = async maxMs => {
		const t0 = performance.now()
		while (performance.now() - t0 < maxMs) {
			if (!H.dlg()) {
				return Math.round(performance.now() - t0)
			}
			await sleep(150)
		}
		return 'STILL'
	}
	H.cancelDlg = async () => {
		const b = H.dlgBtn('取消')
		if (!b) {
			return 'NO_CANCEL_BTN'
		}
		b.click()
		return await H.waitDlgGone(3000)
	}
	H.clickOk = async () => {
		const b = H.dlgBtn('确定')
		if (!b) {
			return 'NO_OK_BTN'
		}
		b.click()
		await sleep(1800)
		return 'CLICKED'
	}

	H.DIR_ID = DIR_ID
	H.FILE_ID = FILE_ID

	// ============================ G0 正文 ============================
	H.spyReq()
	H.spyToast()
	const mark0 = H.reqMark()
	const fc = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc }
	}
	const d = op.drawer
	await sleep(900)

	// 残留自检：上一轮物理清理没做完就绝不落笔
	const keys0 = H.allKeys(d)
	const leaked = [H.FIX_DIR, H.FIX_FILE].filter(n => keys0.split(',').some(k => H.nodeLabel(d, k) === n))
	if (leaked.length) {
		return { ABORT: 'FIXTURE_LEAK:' + leaked.join('/'), keys: keys0.length }
	}
	const expandState = await H.ensureExpanded(d, H.DIR_ID)
	H.F.push('G0_leak=' + (leaked.length ? 'Y' : 'N') + ',expand2117=' + expandState + ',dirVisible=' + H.nodeVisible(d, H.DIR_ID))
	H.F.push('G0_tabCount=' + H.tabs(d).length + ',active=' + H.activeTabId(d) + ',path=' + hs(H.pathText(d)))

	const NEW_ITEMS = ['新建目录', '新建模板文件', '新建二进制文件']
	const rcOpen = await H.rcNode(d, H.DIR_ID)
	H.F.push('G0_menuOnDir=' + rcOpen + ',' + H.menuLabels())
	H.I.push('G0_menuDom=' + H.menuDom())
	for (const it of NEW_ITEMS) {
		if (!H.menu()) {
			await H.rcNode(d, H.DIR_ID)
		}
		const ck = await H.clickMenuItem(it)
		await sleep(700)
		const dump = H.dlgDump()
		H.F.push('G0_' + it + ':title=' + H.dlgTitle() + ',dump=' + hs(dump))
		H.I.push('G0_' + it + '_raw=' + dump)
		const cg = await H.cancelDlg()
		H.F.push('G0_' + it + ':dlgGone=' + (cg === 'STILL' ? 'STILL' : 'CLOSED') + ',menuGone=' + (H.menu() ? 'STILL' : 'CLOSED'))
		await sleep(300)
	}

	H.I.push('btn=' + H.BTN)
	H.I.push('rowIdx=' + op.rowIdx + ',rowCount=' + op.rowCount + ',attempt=' + op.attempt)
	H.I.push('treePanel=' + (d.querySelector('.tree-panel') ? 'Y' : 'N') + ',editorPanel=' + (d.querySelector('.editor-panel') ? 'Y' : 'N'))
	H.I.push('keysLen=' + keys0.split(',').length + ',vw=' + window.innerWidth + ',vh=' + window.innerHeight)

	const reqs = H.stageReq(mark0)
	H.F.push('G0_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	return Object.assign(
		{
			stage: 'G0',
			reqCount: reqs.length,
			reqUniq: [...new Set(reqs.map(r => r.split(' ')[0] + ' ' + r.split(' ')[1]))].join(';'),
			writeReq: H.writeReq(reqs).join(';') || 'NONE',
			toasts: H.TOASTS.join('|') || 'NONE',
			drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE G1 — 空白右键（根级 parentId=0 / 路径 '/'）+ 文件节点「修改」回填（零写入）
 * ========================================================================== */
async () => {
	const H = window.__HG
	if (!H) {
		return { ABORT: 'RUN_G0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, hs = H.hs
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()

	// 空白右键
	const rcB = await H.rcBlank(d)
	H.F.push('G1_menuOnBlank=' + rcB + ',' + H.menuLabels())
	H.I.push('G1_blankMenuDom=' + H.menuDom())
	const ckB = await H.clickMenuItem('新建目录')
	await sleep(700)
	const dumpB = H.dlgDump()
	const pathValB = (H.dlgInput('路径') || {}).value
	H.F.push('G1_blankDir:title=' + H.dlgTitle() + ',pathIsRoot=' + (pathValB === '/' ? 'Y' : 'N') + ',dump=' + hs(dumpB))
	H.I.push('G1_blankDir_raw=' + dumpB + ' pathVal=' + pathValB)
	H.F.push('G1_blankDir_dlgGone=' + (H.cancelDlg() === 'STILL' ? 'STILL' : 'CLOSED'))
	await sleep(600)

	// 文件节点右键：菜单应只有「修改」
	const rcF = await H.rcNode(d, H.FILE_ID)
	H.F.push('G1_menuOnFile=' + rcF + ',' + H.menuLabels())
	const ckF = await H.clickMenuItem('修改')
	await sleep(1600) // 等 detail 回传
	const dumpF = H.dlgDump()
	H.F.push('G1_updateFile:title=' + H.dlgTitle() + ',dump=' + hs(dumpF))
	H.I.push('G1_updateFile_raw=' + dumpF)
	const fileNameVal = (H.dlgInput('文件名称') || {}).value
	const pathValF = (H.dlgInput('路径') || {}).value
	H.F.push('G1_updateFile:fileName=' + fileNameVal + ',path=' + hs(pathValF) + ',pathNoTail=' + (pathValF.indexOf(fileNameVal) < 0 ? 'Y' : 'N'))
	H.F.push('G1_updateFile_dlgGone=' + (H.cancelDlg() === 'STILL' ? 'STILL' : 'CLOSED'))
	await sleep(500)

	H.I.push('G1_btn=' + H.BTN)
	const reqs = H.stageReq(mark)
	H.F.push('G1_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	return Object.assign(
		{
			stage: 'G1',
			reqCount: reqs.length,
			reqUniq: [...new Set(reqs.map(r => r.split(' ')[0] + ' ' + r.split(' ')[1]))].join(';'),
			writeReq: H.writeReq(reqs).join(';') || 'NONE',
			toasts: H.TOASTS.join('|') || 'NONE',
			drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length
		},
		H.out()
	)
}


/* -----------------------------------------------------------------------------
 * STAGE G2 — 写库①：在 2117 下新建目录 T2GDIR（F13 目录分支：不进 tab、不换激活）
 * ========================================================================== */
async () => {
	const H = window.__HG
	if (!H) {
		return { ABORT: 'RUN_G0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, hs = H.hs
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()
	const bodyMark = H.BODIES.length

	const keysBefore = H.allKeys(d).split(',')
	const tabsBefore = H.tabs(d).length
	const activeBefore = H.activeTabId(d)
	const pathBefore = H.pathText(d)

	await H.ensureExpanded(d, H.DIR_ID)
	const rc = await H.rcNode(d, H.DIR_ID)
	const ck = await H.clickMenuItem('新建目录')
	await sleep(700)
	const filled = await H.fill('目录名称', H.FIX_DIR)
	H.F.push('G2_rc=' + rc + ',click=' + ck + ',fill=' + filled)
	const ok = await H.clickOk()
	const gone = await H.waitDlgGone(5000)
	await sleep(1200) // 等 initData 回树

	const keysAfter = H.allKeys(d).split(',')
	const added = keysAfter.filter(k => keysBefore.indexOf(k) < 0)
	const removed = keysBefore.filter(k => keysAfter.indexOf(k) < 0)
	const newId = added.length === 1 ? added[0] : null
	H.NEW_DIR_ID = newId

	H.F.push('G2_ok=' + ok + ',dlgGone=' + (gone === 'STILL' ? 'STILL' : 'CLOSED') + ',added=' + added.length + ',removed=' + removed.length)
	H.F.push('G2_nodeFound=' + (newId ? 'Y' : 'N') + ',label=' + (newId ? H.nodeLabel(d, newId) : 'NA') + ',parentIsAnchor=' + (newId ? (H.parentOf(d, newId) === String(H.DIR_ID) ? 'Y' : 'N') : 'NA') + ',visible=' + (newId ? H.nodeVisible(d, newId) : 'NA'))
	H.F.push('G2_tabDelta=' + (H.tabs(d).length - tabsBefore) + ',activeUnchanged=' + (H.activeTabId(d) === activeBefore ? 'Y' : 'N') + ',pathUnchanged=' + (H.pathText(d) === pathBefore ? 'Y' : 'N'))
	H.F.push('G2_expandedParent=' + H.isExpanded(d, H.DIR_ID) + ',selfExpanded=' + (newId ? H.isExpanded(d, newId) : 'NA'))

	const reqs = H.stageReq(mark)
	const writes = H.writeReq(reqs)
	H.F.push('G2_writes=' + writes.join(';'))
	H.F.push('G2_bodyKeys=' + H.bodyKeys())
	H.F.push('G2_toast=' + hs(H.TOASTS[H.TOASTS.length - 1] || 'NONE'))

	H.I.push('G2_newDirId=' + newId)
	H.I.push('G2_bodyRaw=' + (H.BODIES.slice(bodyMark).join(' || ') || 'NONE'))
	H.I.push('G2_reqs=' + reqs.join(';'))
	return Object.assign(
		{
			stage: 'G2',
			newDirId: newId,
			reqCount: reqs.length,
			writeReq: writes.join(';') || 'NONE',
			dialogOpen: !!H.dlg(),
			drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE G3 — 写库②：在 T2GDIR 下新建模板文件 T2GF.java
 *   F12 路径注入（目录自身全路径）+ F13 保存后链路（tabPush/激活/路径显示/编辑器）
 *   收尾附「新建文件的红点双向同步」探测（零写入：改一个字符再删掉，不点保存）
 * ========================================================================== */
async () => {
	const H = window.__HG
	if (!H) {
		return { ABORT: 'RUN_G0_FIRST' }
	}
	if (!H.NEW_DIR_ID) {
		return { ABORT: 'G2_DID_NOT_CREATE_DIR' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, hs = H.hs
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const DIR = H.NEW_DIR_ID
	const nodeDot = (id) => {
		const n = H.node(d, id)
		if (!n) {
			return 'NO_NODE'
		}
		const b = n.querySelector(':scope > .el-tree-node__content .el-badge__content')
		return b ? (b.style.display === 'none' ? 'off' : 'on') : 'NO_BADGE'
	}
	const tabDot = id => {
		const t = H.tabs(d).filter(x => x.id === 'tab-' + id)[0]
		if (!t) {
			return 'NO_TAB'
		}
		const b = t.querySelector('.el-badge__content')
		return b ? (b.style.display === 'none' ? 'off' : 'on') : 'NO_BADGE'
	}

	// state 探针（v1 走 setupState.templateTreeData，v2 走 props.treeState.state；按对象去重）
	if (!H.findStates) {
		H.findStates = () => {
			const seenI = new Set()
			const out = []
			const seenS = new Set()
			const els = [...document.querySelectorAll('.el-drawer'), ...document.querySelectorAll('.el-drawer *')]
			for (const el of els) {
				let c = el.__vueParentComponent
				while (c) {
					if (!seenI.has(c)) {
						seenI.add(c)
						const ss = c.setupState || {}
						if (ss.templateTreeData && Array.isArray(ss.templateTreeData.tabList) && Array.isArray(ss.templateTreeData.treeList)) {
							out.push({ kind: 'v1', st: ss.templateTreeData })
						}
						const ps = c.props && c.props.treeState && c.props.treeState.state
						if (ps && Array.isArray(ps.tabList) && Array.isArray(ps.treeList)) {
							out.push({ kind: 'v2', st: ps })
						}
					}
					c = c.parent
				}
			}
			return out.filter(s => {
				if (seenS.has(s.st)) {
					return false
				}
				seenS.add(s.st)
				return true
			})
		}
		H.findBy = (list, id) => {
			for (const n of list || []) {
				if (String(n.id) === String(id)) {
					return n
				}
				const f = n.children ? H.findBy(n.children, id) : null
				if (f) {
					return f
				}
			}
			return null
		}
		// 两套组件同时挂载，候选里既有活动版也有另一版的冷 state：用「DOM 实际 tab 序列 === state.tabList 的 id 序列」锁定活动 state
		H.pickState = dd => {
			const seq = H.tabIds(dd).join(',')
			const all = H.findStates()
			const hit = all.filter(s => (s.st.tabList || []).map(x => String(x.id)).join(',') === seq)[0] || null
			return { all: all.length, kinds: all.map(s => s.kind).join(','), hit }
		}
	}

	const mark = H.reqMark()
	const bodyMark = H.BODIES.length
	const tabsBefore = H.tabs(d).length

	await H.ensureExpanded(d, H.DIR_ID)
	const dirVisible = H.nodeVisible(d, DIR)
	H.F.push('G3_dirVisible=' + dirVisible + ',dirLabel=' + H.nodeLabel(d, DIR))

	const rc = await H.rcNode(d, DIR)
	const ck = await H.clickMenuItem('新建模板文件')
	await sleep(700)
	const dump = H.dlgDump()
	const pathVal = (H.dlgInput('路径') || {}).value
	H.F.push('G3_rc=' + rc + ',click=' + ck + ',title=' + H.dlgTitle() + ',dump=' + hs(dump))
	H.I.push('G3_dlgRaw=' + dump + ' pathVal=' + pathVal)
	const f1 = await H.fill('文件名称', H.FIX_FILE)
	const f2 = await H.fill('模板名称', 't2gTpl')
	H.F.push('G3_fill=' + f1 + '/' + f2)
	const ok = await H.clickOk()
	const gone = await H.waitDlgGone(5000)
	await sleep(1400) // 等 tabPush + initData + 激活项解析

	const tabsAfter = H.tabs(d)
	const newTab = tabsAfter.filter(t => t.innerText.trim().replace(/\s+/g, '') === H.FIX_FILE)[0]
	const newId = newTab ? newTab.id.replace(/^tab-/, '') : null
	H.NEW_FILE_ID = newId

	H.F.push('G3_ok=' + ok + ',dlgGone=' + (gone === 'STILL' ? 'STILL' : 'CLOSED') + ',tabDelta=' + (tabsAfter.length - tabsBefore))
	H.F.push('G3_newTab=' + (newId ? 'Y' : 'N') + ',activeIsNew=' + (H.activeTabId(d) === newId ? 'Y' : 'N') + ',tabCountNow=' + tabsAfter.length)
	H.F.push('G3_pathText=' + hs(H.pathText(d)) + ',cmMounted=' + (H.cm(d) ? 'Y' : 'N') + ',saveDisabled=' + (H.saveBtn(d) ? (H.saveBtn(d).disabled ? 'Y' : 'N') : 'NO_BTN'))
	H.F.push('G3_treeNodeFound=' + (newId && H.node(d, newId) ? 'Y' : 'N') + ',treeParentIsDir=' + (newId ? (H.parentOf(d, newId) === String(DIR) ? 'Y' : 'N') : 'NA'))

	// 引用同一性 + 红点双向同步：新建文件的 tab 对象来自 /add 回传，树节点来自随后的 /tree
	// cand/kinds 量的是「页面同时挂载了几套 state 实例」（v2 冷 v1 实例也在 → 2，v1 只 1），属环境伪差异，一律不进行为串
	const probe = H.pickState(d)
	const st = probe.hit
	H.F.push('G3_stateMatch=' + (st ? 'OK' : 'MISS'))
	H.I.push('G3_stateCand=' + probe.all + ',kinds=' + probe.kinds)
	if (st && newId) {
		const tabObj = (st.st.tabList || []).filter(x => String(x.id) === String(newId))[0]
		const treeObj = H.findBy(st.st.treeList, newId)
		H.F.push('G3_refSame=' + (tabObj && treeObj ? (tabObj === treeObj ? 'Y' : 'N') : 'MISSING') + ',tabObjKeys=' + (tabObj ? Object.keys(tabObj).sort().join(',') : 'NA'))
		H.F.push('G3_tabHasContentField=' + (tabObj ? ('templateContent' in tabObj ? 'Y' : 'N') : 'NA') + ',tabIsEditedType=' + (tabObj ? typeof tabObj.isEdited : 'NA'))
		H.I.push('G3_stateProbe=hitKind=' + st.kind + ',treeObjKeys=' + (treeObj ? Object.keys(treeObj).sort().join(',') : 'NA'))
	} else {
		H.F.push('G3_refSame=NO_PROBE')
		H.I.push('G3_stateProbe=hitKind=' + (st ? st.kind : 'NONE'))
	}

	// 编辑探测（零写入）：插一个字符看两处红点，再删掉看是否齐灭
	if (newId) {
		const cm = H.cm(d)
		if (cm) {
			cm.focus()
			document.execCommand('insertText', false, 'Z')
			await sleep(900)
			H.F.push('G3_editOn:tree=' + nodeDot(newId) + ',tab=' + tabDot(newId) + ',save=' + (H.saveBtn(d) && !H.saveBtn(d).disabled ? 'ena' : 'dis'))
			document.execCommand('delete', false, null)
			await sleep(900)
			H.F.push('G3_editOff:tree=' + nodeDot(newId) + ',tab=' + tabDot(newId) + ',save=' + (H.saveBtn(d) && !H.saveBtn(d).disabled ? 'ena' : 'dis'))
		} else {
			H.F.push('G3_editOn=NO_CM')
		}
	}

	const reqs = H.stageReq(mark)
	const writes = H.writeReq(reqs)
	H.F.push('G3_writes=' + writes.join(';'))
	H.F.push('G3_bodyKeys=' + H.bodyKeys())
	H.F.push('G3_toast=' + hs(H.TOASTS[H.TOASTS.length - 1] || 'NONE'))

	H.I.push('G3_newFileId=' + newId)
	H.I.push('G3_newDirId=' + DIR)
	H.I.push('G3_bodyRaw=' + (H.BODIES.slice(bodyMark).join(' || ') || 'NONE'))
	H.I.push('G3_pathVal=' + pathVal)
	return Object.assign(
		{
			stage: 'G3',
			newFileId: newId,
			reqCount: reqs.length,
			writeReq: writes.join(';') || 'NONE',
			pathText: H.pathText(d),
			toasts: H.TOASTS.slice(-3).join('|')
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE G4 — 写库③：修改 T2GF.java 的描述（F12 修改分支不信详情回传 + F13 更新链路）
 *   只改描述、不碰内容；PUT /update 请求体是否携带 templateContent 由 spy 取证
 * ========================================================================== */
async () => {
	const H = window.__HG
	if (!H) {
		return { ABORT: 'RUN_G0_FIRST' }
	}
	if (!H.NEW_FILE_ID) {
		return { ABORT: 'G3_DID_NOT_CREATE_FILE' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, hs = H.hs
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const FILE = H.NEW_FILE_ID
	const DIR = H.NEW_DIR_ID
	const mark = H.reqMark()
	const bodyMark = H.BODIES.length

	await H.ensureExpanded(d, H.DIR_ID)
	const tabsBefore = H.tabs(d).length
	const activeBefore = H.activeTabId(d)
	const pathBefore = H.pathText(d)
	const cmBefore = (H.cm(d) || {}).textContent || ''

	const rc = await H.rcNode(d, FILE)
	const labels = H.menuLabels()
	const ck = await H.clickMenuItem('修改')
	await sleep(1700) // 等 detail 回传
	const dump = H.dlgDump()
	const pathVal = (H.dlgInput('路径') || {}).value
	const nameVal = (H.dlgInput('文件名称') || {}).value
	const tplVal = (H.dlgInput('模板名称') || {}).value
	const hasUpload = !!(H.dlg() && (H.dlgInput('文件上传') || H.dlg().querySelector('.el-upload')))
	H.F.push('G4_rc=' + rc + ',menu=' + labels + ',click=' + ck + ',title=' + H.dlgTitle() + ',dump=' + hs(dump))
	H.F.push('G4_backfill:fileName=' + nameVal + ',templateName=' + hs(tplVal) + ',pathNoTail=' + (pathVal.indexOf(H.FIX_FILE) < 0 ? 'Y' : 'N') + ',hasUploadField=' + (hasUpload ? 'Y' : 'N'))
	H.I.push('G4_dlgRaw=' + dump + ' pathVal=' + pathVal)
	const f3 = await H.fill('描述', 'T2GDESC')
	H.F.push('G4_fillDesc=' + f3)
	const ok = await H.clickOk()
	const gone = await H.waitDlgGone(5000)
	await sleep(1400)

	const cmAfter = (H.cm(d) || {}).textContent || ''
	H.F.push('G4_ok=' + ok + ',dlgGone=' + (gone === 'STILL' ? 'STILL' : 'CLOSED') + ',tabDelta=' + (H.tabs(d).length - tabsBefore) + ',activeUnchanged=' + (H.activeTabId(d) === activeBefore ? 'Y' : 'N'))
	H.F.push('G4_pathUnchanged=' + (H.pathText(d) === pathBefore ? 'Y' : 'N') + ',cmTextUnchanged=' + (cmBefore === cmAfter ? 'Y' : 'N') + ',treeNodeStillThere=' + (H.node(d, FILE) ? 'Y' : 'N'))

	const probe4 = H.pickState(d)
	const st = probe4.hit
	if (st) {
		H.I.push('G4_stateMatch=cand=' + probe4.all + ',kind=' + st.kind)
		const tabObj = (st.st.tabList || []).filter(x => String(x.id) === String(FILE))[0]
		const treeObj = H.findBy(st.st.treeList, FILE)
		const td = tabObj ? tabObj.templateDesc : 'NO_TAB'
		const nd = treeObj ? treeObj.templateDesc : 'NO_TREE'
		H.F.push('G4_desc:tab=' + hs(td) + ',tree=' + hs(nd) + ',same=' + (td === nd ? 'Y' : 'N'))
		H.I.push('G4_descPair=' + JSON.stringify(td) + '/' + JSON.stringify(nd))
	} else {
		H.F.push('G4_desc=NO_PROBE')
	}

	const reqs = H.stageReq(mark)
	const writes = H.writeReq(reqs)
	H.F.push('G4_writes=' + writes.join(';'))
	H.F.push('G4_bodyKeys=' + H.bodyKeys())
	const lastBody = H.LASTFULL || ''
	let tcInBody = 'NO_BODY'
	try {
		const j = JSON.parse(lastBody)
		tcInBody = ('templateContent' in j ? (j.templateContent ? 'NONEMPTY' : 'EMPTY') : 'ABSENT') + ',type=' + j.templateType + ',hasId=' + (!!j.id)
	} catch (e) {
		tcInBody = 'UNPARSEABLE'
	}
	H.F.push('G4_bodyContent=' + tcInBody)
	H.F.push('G4_toast=' + hs(H.TOASTS[H.TOASTS.length - 1] || 'NONE'))

	H.I.push('G4_bodyRaw=' + (H.BODIES.slice(bodyMark).join(' || ') || 'NONE'))
	H.I.push('G4_bodyFullLen=' + lastBody.length)
	H.I.push('G4_reqs=' + reqs.join(';'))
	return Object.assign(
		{
			stage: 'G4',
			reqCount: reqs.length,
			writeReq: writes.join(';') || 'NONE',
			pathText: H.pathText(d),
			toasts: H.TOASTS.slice(-2).join('|')
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE G5 — 新建二进制文件的必填校验（零写入：提交被前端校验拦住）
 * ========================================================================== */
async () => {
	const H = window.__HG
	if (!H) {
		return { ABORT: 'RUN_G0_FIRST' }
	}
	if (!H.NEW_DIR_ID) {
		return { ABORT: 'G2_DID_NOT_CREATE_DIR' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, hs = H.hs
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()

	await H.ensureExpanded(d, H.DIR_ID)
	const rc = await H.rcNode(d, H.NEW_DIR_ID)
	const ck = await H.clickMenuItem('新建二进制文件')
	await sleep(700)
	const dump = H.dlgDump()
	H.F.push('G5_rc=' + rc + ',click=' + ck + ',title=' + H.dlgTitle() + ',dump=' + hs(dump))
	H.I.push('G5_dlgRaw=' + dump)
	const f1 = await H.fill('文件名称', 'T2GB.png')
	const upInp = H.dlgInput('文件上传')
	H.F.push('G5_fillName=' + f1 + ',uploadCtl=' + (upInp ? upInp.tagName.toLowerCase() : (d.querySelector('.el-dialog .el-upload') ? 'upload' : 'NA')))
	const ok = await H.clickOk()
	await sleep(900)
	H.F.push('G5_ok=' + ok + ',errors=' + hs(H.dlgErrors()) + ',dlgStillOpen=' + (H.dlg() ? 'Y' : 'N'))
	const reqsA = H.stageReq(mark)
	H.F.push('G5_writeReqBlocked=' + (H.writeReq(reqsA).join(';') || 'NONE'))
	const cg = await H.cancelDlg()
	H.F.push('G5_dlgGone=' + (cg === 'STILL' ? 'STILL' : 'CLOSED'))
	await sleep(500)

	const reqs = H.stageReq(mark)
	H.I.push('G5_reqs=' + reqs.join(';'))
	H.I.push('G5_btn=' + H.BTN)
	return Object.assign(
		{
			stage: 'G5',
			reqCount: reqs.length,
			writeReq: H.writeReq(reqs).join(';') || 'NONE',
			toasts: H.TOASTS.slice(-2).join('|'),
			dialogOpen: !!H.dlg()
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE G6 — 收口：关新建文件的 tab（应无未保存确认）+ 关抽屉 + 现场计数
 * ========================================================================== */
async () => {
	const H = window.__HG
	if (!H) {
		return { ABORT: 'RUN_G0_FIRST' }
	}
	H.F = []
	H.I = []
	const sleep = H.sleep, vis = H.vis, hs = H.hs
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()
	const FILE = H.NEW_FILE_ID

	// 关掉本轮新建文件的 tab（编辑探测已逐字还原，理应无确认框）
	const tab = FILE ? H.tabs(d).filter(t => t.id === 'tab-' + FILE)[0] : null
	H.F.push('G6_tabFound=' + (tab ? 'Y' : 'N') + ',tabsBefore=' + H.tabs(d).length)
	if (tab) {
		const x = tab.querySelector('.is-icon-close')
		if (x) {
			x.click()
		}
		await sleep(900)
		H.F.push('G6_closeConfirm=' + (H.box() ? 'Y' : 'N') + ',tabsAfter=' + H.tabs(d).length)
		if (H.box()) {
			const b = H.boxBtn('no') || H.boxBtn('cancel')
			if (b) {
				b.click()
			}
			await sleep(900)
			H.F.push('G6_afterNo=' + (H.box() ? 'STILL' : 'CLOSED') + ',tabs=' + H.tabs(d).length)
		}
	}

	// 关抽屉：全部干净则不该有确认框
	const dd = H.mainReady() || H.mainDrawer()
	if (dd) {
		H.closeDrawerX(dd)
		await sleep(1200)
	}
	H.F.push('G6_drawerConfirm=' + (H.box() ? 'Y' : 'N'))
	if (H.box()) {
		const b = H.boxBtn('no') || H.boxBtn('cancel')
		if (b) {
			b.click()
		}
		await sleep(1000)
	}
	const fc = await H.forceCloseMain()
	H.F.push('G6_drawers=' + fc.drawers + ',boxes=' + fc.boxes + ',msgVisible=' + [...document.querySelectorAll('.el-message')].filter(vis).length)

	// fixture 行此时仍在库中（外层物理 DELETE 才清），只作现场事实记录，不进 F
	H.I.push('G6_newDirId=' + (H.NEW_DIR_ID || 'NA') + ',newFileId=' + (H.NEW_FILE_ID || 'NA'))
	H.I.push('G6_btn=' + H.BTN)
	H.I.push('G6_toastTail=' + hs(H.TOASTS.slice(-3).join('|')))
	const reqs = H.stageReq(mark)
	H.F.push('G6_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	return Object.assign(
		{
			stage: 'G6',
			reqCount: reqs.length,
			writeReq: H.writeReq(reqs).join(';') || 'NONE',
			drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length,
			boxes: [...document.querySelectorAll('.el-message-box')].filter(vis).length,
			dialogs: [...document.querySelectorAll('.el-dialog')].filter(vis).length
		},
		H.out()
	)
}
