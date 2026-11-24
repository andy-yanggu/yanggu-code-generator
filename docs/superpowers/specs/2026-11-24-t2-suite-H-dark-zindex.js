/* =============================================================================
 * T2 双跑对照 · Suite H（F29 暗色与层级：预期差异项，逐值解释）
 * 日期：2026-11-24    靶点：模板组 80「单体java项目模板_复制」，锚 = 目录 2117 / 文件 2142
 *
 * 用法（每段是顶格书写的独立 async 箭头函数，装载器按 ^async \(\) => \{$ 切段）
 *   1) reload → 注入隐藏 file input → setInputFiles(本文件绝对路径) → 页内 f.text() + FNV 核对磁盘值；
 *   2) window.__BTN：v2 = '模板v2'，v1 = '模板'；两版跑同一份源码，文件一字不改；
 *   3) 顺序 H0 → H1a →（外层 page.mouse.move 到 H1a 返回的坐标）→ H1b → H2；
 *   4) 每段前 page.bringToFront()（合成帧饥饿会让 transform 停在起始值，见 Suite N 教训）。
 *
 * 本批全程零写入：只做右键与取样式，任何非 GET 请求出现即判失败。
 *
 * 口径（本批是三串，不是两串）
 *   - F（行为串）：两版必须逐字相同 = 计算样式取值中「设计上应当相同」的那批 + 弹层落位与命中；
 *   - I（身份串）：允许不同且需逐条解释 = 菜单挂载点链、菜单元素 tag/class、请求清单、视口；
 *   - X（预期差异串）：F29 性质决定了必然存在差异，且差异属设计意图而非回归，故单列一串，
 *     两版各取各值、逐项写进文档解释，不参与 fpBehav 判等。若不单列，整串 fpBehav 会被
 *     已知差异污染，反而看不出真正该守住的取值有没有漂移 —— 这是本批的核心取证纪律。
 *     X 的成员在写脚本前就由源码定死（不得跑完再挑）：
 *       x1 限高滚动：v2 .context-menu 有 max-height:calc(100vh-16px)+overflow-y:auto，v1 只有 overflow:hidden（T0 新增的最后防线）；
 *       x2 Esc 关闭：v2 ContextMenu 有 keydown Escape，v1 tree.vue 全文无 Esc 处理器（T0 共享件新增的关闭通道）；
 *       x3 菜单串扰：v1 两个顶层 mousedown 都用 document.querySelector('.context-menu') 取首个匹配，v2 用本实例 $el 判定；
 *       x5 同开前置态：x3 靠「先外部 pointerdown 再 contextmenu」的路径，两版都会先关掉上一个菜单，
 *          对串扰无判定力；x5 改用纯 contextmenu（不经外部指针事件）构造同开态，把「能否同开」单独落档；
 *       x4 容器标记：v1 根元素是 ul 且自带 list-style:none，v2 根是 div、list-style:none 在其内层 ul 上 ——
 *          ls 一律改在「真正装 li 的那个元素」上取，因此 x4 不进 X（已归一化）。
 *
 * 指针停靠约定（本批踩过的坑，写进 runner）：H1 的真实 hover 坐标是从「本版本菜单的 li 中心」算出来的，
 * 两版菜单锚点差 5px（v1 直接用光标坐标，v2 走 computeMenuPosition），若让指针原地不动继续跑 H2，
 * 菜单重开后一版命中 hover、一版没命中，item 的 bg/clr 就会把「指针停在哪」写进行为串 → 伪差异。
 * 因此 runner 在 H2/H3 前必须先把指针移到与版本无关的固定点 (5,400)（不在抽屉内，不会开任何菜单）。
 *   注：root 的 color 仍留在 F —— v2 Teleport 到 body 后继承源从抽屉变成 body，
 *       若取色发生漂移就是真回归（F29 要断言的正是「取色只依赖 EP 语义变量」），不能预先豁免。
 *
 * 暗色手法说明：不点 navbar 的主题开关，而是直接翻 `document.documentElement.classList` 的 `dark`。
 * 原因：①开关走 View Transition + 扩散/收缩动画，本浏览器在 MCP 下 rAF 500ms 只跳 1 次，动画可能长时间挂住；
 *       ②F29 要断言的是「菜单取色是否只依赖 EP 语义变量」，与开关实现无关；
 *       ③类名翻转在段内自还原（H2 末行断言 dark 类已摘除），不留环境残渣。
 * ========================================================================== */

/* -----------------------------------------------------------------------------
 * STAGE H0 — helper 建立 + 亮色下的层级/取色/落位/串扰量测
 * ========================================================================== */
async () => {
	const BTN = window.__BTN || '模板v2'
	const ROW_TEXT = '单体java项目模板_复制'
	const DIR_ID = 2117
	const FILE_ID = 2142
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

	const H = { BTN, ROW_TEXT, DIR_ID, FILE_ID, sleep, vis, fnv, hs, F: [], I: [], X: [], REQ: [], reqPatched: false, LASTM: '', LASTU: '' }
	window.__HH = H

	// ---- 只读 spy：本批不允许任何写请求
	H.spyReq = () => {
		if (H.reqPatched) {
			return 'ALREADY'
		}
		H.reqPatched = true
		const trim = u => String(u).replace(/^https?:\/\/[^/]+/, '')
		const xo = XMLHttpRequest.prototype.open
		XMLHttpRequest.prototype.open = function (m, u) {
			H.LASTM = String(m).toUpperCase()
			H.REQ.push(H.LASTM + ' ' + trim(u))
			return xo.apply(this, Array.prototype.slice.call(arguments))
		}
		const fo = window.fetch
		window.fetch = function (a, b) {
			const m = String((b && b.method) || (a && a.method) || 'GET').toUpperCase()
			H.REQ.push(m + ' ' + trim(a && a.url ? a.url : a))
			return fo.apply(this, arguments)
		}
		return 'OK'
	}
	H.reqMark = () => H.REQ.length
	H.stageReq = mark => H.REQ.slice(mark)
	H.writeReq = list => list.filter(r => !/^(GET|HEAD|OPTIONS) /.test(r))

	// ---- 抽屉与树
	H.mainDrawer = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0)
	H.mainReady = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(d => !d.classList.contains('template-test-drawer') && ((d.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0 && d.querySelector('.el-tree-node'))
	H.btn = scope => text => [...scope.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim() === text)
	H.forceCloseMain = async () => {
		for (let i = 0; i < 4; i++) {
			// 本批不编辑内容，理论上不出确认框；留着兜底，避免残留 message-box 让后面的右键探针吃到假命中
			const box = [...document.querySelectorAll('.el-message-box')].filter(vis)[0]
			if (box) {
				const c = box.querySelector('.el-message-box__btns button:last-child') || box.querySelector('.el-message-box__close')
				if (c) {
					c.click()
					await sleep(500)
				}
			}
			const d = H.mainDrawer()
			if (!d) {
				break
			}
			const x = d.querySelector('button[aria-label*="关闭"]') || d.querySelector('.el-drawer__close-btn')
			if (x) {
				x.click()
			}
			await sleep(700)
			for (let k = 0; k < 10; k++) {
				await sleep(200)
				if (!H.mainDrawer()) {
					break
				}
			}
		}
		return [...document.querySelectorAll('.el-drawer')].filter(vis).length
	}
	H.open = async () => {
		const rows = [...document.querySelectorAll('tbody tr')].filter(vis)
		const idx = rows.findIndex(r => r.innerText.indexOf(ROW_TEXT) >= 0)
		const row = idx < 0 ? null : rows[idx]
		if (!row) {
			return { ok: false, why: 'ROW_MISMATCH' }
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
					return { ok: true, drawer: d, rowIdx: idx, rowCount: rows.length, attempt }
				}
			}
		}
		return { ok: false, why: 'TREE_NOT_READY' }
	}
	H.nodeContent = (d, id) => {
		const n = [...d.querySelectorAll('.el-tree-node')].find(x => x.getAttribute('data-key') === String(id))
		return n ? n.querySelector(':scope > .el-tree-node__content') : null
	}
	H.clickNode = async (d, id) => {
		const c = H.nodeContent(d, id)
		if (c) {
			c.click()
		}
		await sleep(450)
	}
	H.ensureExpanded = async (d, id) => {
		for (let i = 0; i < 3; i++) {
			const n = [...d.querySelectorAll('.el-tree-node')].find(x => x.getAttribute('data-key') === String(id))
			if (n && n.classList.contains('is-expanded')) {
				return 'ALREADY'
			}
			if (!n) {
				return 'NO_NODE'
			}
			await H.clickNode(d, id)
		}
		return 'FAIL'
	}
	H.tabs = d => [...d.querySelectorAll('.el-tabs__item')].filter(t => vis(t) && !t.closest('.template-test-drawer'))

	// ---- 菜单：真实右键会产生 pointerdown → mousedown → contextmenu 三个事件，
	// 合成时一个都不能缺：v1 的外点关闭挂在 document mousedown，v2 挂在 document pointerdown（捕获阶段），
	// 只发 contextmenu 会两版都不关；只发 mousedown 则 v2 不关 —— 两种都会把「关闭通道差异」误读成「行为差异」。
	H.evs = (el, list) => {
		const r = el.getBoundingClientRect()
		const x = Math.round(r.left + Math.min(r.width, 120) / 2)
		const y = Math.round(r.top + r.height / 2)
		for (const t of list) {
			const Ctor = t.startsWith('pointer') ? PointerEvent : MouseEvent
			el.dispatchEvent(new Ctor(t, { bubbles: true, cancelable: true, clientX: x, clientY: y, button: t === 'contextmenu' ? 2 : 0 }))
		}
		return { x, y }
	}
	H.menus = () => [...document.querySelectorAll('.context-menu')].filter(vis)
	H.menu = () => H.menus()[0] || null
	H.rc = async el => {
		if (!el) {
			return 'NO_TARGET'
		}
		H.evs(el, ['pointerdown', 'mousedown'])
		await sleep(60)
		H.evs(el, ['contextmenu'])
		await sleep(500)
		return H.menu() ? 'OPEN' : 'NOT_OPEN'
	}
	// 只发 contextmenu（x5 专用）：专测「未经外部指针事件时，两菜单能否同开」
	H.rcCtx = async el => {
		if (!el) {
			return H.menus().length
		}
		H.evs(el, ['contextmenu'])
		await sleep(500)
		return H.menus().length
	}
	H.clickAway = async (x, y) => {
		for (const t of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']) {
			const Ctor = t.startsWith('pointer') ? PointerEvent : MouseEvent
			document.body.dispatchEvent(new Ctor(t, { bubbles: true, cancelable: true, clientX: x, clientY: y, button: 0 }))
			await sleep(30)
		}
	}
	H.closeAll = async () => {
		for (let i = 0; i < 3 && H.menus().length > 0; i++) {
			await H.clickAway(5, 400)
			await sleep(300)
		}
		return H.menus().length
	}
	H.rcNode = async (d, id) => H.rc(H.nodeContent(d, id))
	H.rcCtxNode = async (d, id) => H.rcCtx(H.nodeContent(d, id))
	H.rcTab = async d => {
		const t = H.tabs(d).filter(x => x.classList.contains('is-active'))[0] || H.tabs(d)[0]
		return H.rc(t)
	}
	H.escClose = async () => {
		document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
		await sleep(400)
		return H.menus().length
	}

	// ---- 样式量测（只取「取值」，不取挂载点：挂载点属身份串）
	H.rootVar = name => getComputedStyle(document.documentElement).getPropertyValue(name).trim()
	// 真正装 li 的容器：v1 就是根 ul，v2 是内层 ul.context-menu__list —— 列表标记类取值在同一语义元素上比，否则是假差异
	H.listEl = () => {
		const m = H.menu()
		if (!m) {
			return null
		}
		return m.querySelector('ul') || m
	}
	// core：设计上两版应当相同的取值，进 F
	H.menuCore = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const cs = getComputedStyle(m)
		const le = H.listEl()
		return [
			'pos=' + cs.position,
			'z=' + cs.zIndex,
			'bg=' + cs.backgroundColor,
			'bd=' + cs.borderTopWidth + '/' + cs.borderTopColor,
			'rad=' + cs.borderTopLeftRadius,
			'sh=' + cs.boxShadow,
			'miw=' + cs.minWidth,
			'maw=' + cs.maxWidth,
			'pad=' + cs.padding,
			'ls=' + getComputedStyle(le).listStyleType,
			'clr=' + cs.color
		].join('|')
	}
	// ext：x1 预期差异（限高滚动为 T0 新增），只进 X
	H.menuExt = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const cs = getComputedStyle(m)
		return 'mah=' + cs.maxHeight + '|ovf=' + cs.overflowY + '|ovx=' + cs.overflowX
	}
	H.itemCss = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const li = [...m.querySelectorAll('li')].filter(vis)[0]
		if (!li) {
			return 'NO_ITEM'
		}
		const cs = getComputedStyle(li)
		return 'bg=' + cs.backgroundColor + '|clr=' + cs.color + '|fs=' + cs.fontSize + '|pad=' + cs.padding + '|rad=' + cs.borderTopLeftRadius + '|disp=' + cs.display + '|gap=' + cs.gap + '|cur=' + cs.cursor
	}
	H.themeVars = () => ['--el-bg-color-overlay', '--el-border-color', '--el-text-color-primary', '--el-fill-color-dark', '--el-index-popper', '--el-bg-color', '--el-color-primary'].map(v => v.replace('--el-', '') + '=' + H.rootVar(v)).join(',')
	// 变量名→实际计算值的等价性只能这么验：用同一变量做背景探一下，比较两个 computed 值（不能拿 #ebedf0 与 rgb(235,237,240) 直接比字面量）
	H.varAsRgb = name => {
		const p = document.createElement('div')
		p.style.cssText = 'position:fixed;left:-9999px;top:0;width:1px;height:1px;background-color:var(' + name + ')'
		document.body.appendChild(p)
		const v = getComputedStyle(p).backgroundColor
		p.remove()
		return v
	}
	H.layerOf = () => {
		const d = H.mainDrawer()
		const o = d ? d.parentElement : null
		const tabItem = d ? H.tabs(d)[0] : null
		return [
			'drawerZ=' + (d ? getComputedStyle(d).zIndex : 'NA'),
			'overlayZ=' + (o && o.classList && o.classList.contains('el-overlay') ? getComputedStyle(o).zIndex : 'NA'),
			'testZ=' + (tabItem ? getComputedStyle(tabItem.closest('.el-tabs__header') || tabItem).zIndex : 'NA')
		].join(',')
	}
	// 真·可见性证据：菜单中心点击命中的是不是菜单自己（v1 在抽屉层内、v2 在 body 层，都要能压过抽屉）
	H.hitTest = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const r = m.getBoundingClientRect()
		const el = document.elementFromPoint(Math.round(r.left + r.width / 2), Math.round(r.top + Math.min(r.height / 2, 12)))
		if (!el) {
			return 'NULL_HIT'
		}
		return (m === el || m.contains(el) ? 'IN_MENU' : 'OUTSIDE:' + el.tagName.toLowerCase() + '.' + String(el.className).split(' ')[0]) + ',clip=' + (r.width > 0 && r.height > 0 ? 'OK' : 'ZERO')
	}
	// 挂载点链（身份项）：父级一路到 body，记录是否被抽屉包着
	H.chain = () => {
		const m = H.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const chain = []
		let p = m.parentElement
		let depth = 0
		while (p && p !== document.body && depth < 8) {
			chain.push(p.tagName.toLowerCase() + '.' + String(p.className).split(' ')[0])
			p = p.parentElement
			depth++
		}
		return 'menu=' + m.tagName.toLowerCase() + '.' + String(m.className).split(' ')[0] + ' < ' + chain.join(' < ') + ' < BODY | inDrawer=' + (!!m.closest('.el-drawer') ? 'Y' : 'N') + ' | directChildOfBody=' + (m.parentElement === document.body ? 'Y' : 'N')
	}
	H.dump = tag => {
		H.F.push(tag + '_core=' + H.menuCore())
		H.X.push('x1_' + tag + '_ext=' + H.menuExt())
		H.F.push(tag + '_item=' + H.itemCss())
		H.F.push(tag + '_vars=' + hs(H.themeVars()))
		H.F.push(tag + '_layer=' + H.layerOf())
		H.F.push(tag + '_hit=' + H.hitTest())
		H.I.push(tag + '_chain=' + H.chain())
		H.I.push(tag + '_rootTag=' + (H.menu() ? H.menu().tagName.toLowerCase() + '.' + String(H.menu().className) : 'NA'))
	}

	// ============================ H0 正文 ============================
	H.spyReq()
	const mark0 = H.reqMark()
	const fc0 = await H.forceCloseMain()
	const op = await H.open()
	if (!op.ok) {
		return { ABORT: op.why, fc0 }
	}
	const d = op.drawer
	await sleep(900)
	H.F.push('H0_darkClass=' + (document.documentElement.classList.contains('dark') ? 'Y' : 'N'))
	const expand = await H.ensureExpanded(d, H.DIR_ID)
	// 先点一个文件把 tab 打开，供后面的 tab 右键串扰探针使用
	await H.clickNode(d, H.FILE_ID)
	H.F.push('H0_expand=' + expand + ',tabCount=' + H.tabs(d).length)

	const rcA = await H.rcNode(d, H.DIR_ID)
	H.F.push('H0_treeMenu=' + rcA + ',visible=' + H.menus().length)
	H.dump('H0')

	// 菜单越界夹取（属 T0 computeMenuPosition 的行为面）：把光标压到右下角极值再看是否仍在视口内
	const rcB = await H.escClose()
	H.X.push('x2_H0_afterEsc=' + rcB)
	const rcC = await H.rcTab(d)
	H.F.push('H0_tabMenu=' + rcC + ',visible=' + H.menus().length)
	H.I.push('H0_tabMenuChain=' + H.chain())
	// x3：两版都开时才判定串扰；但本探针的前置 pointerdown 会先关掉上一个菜单，因此同开态需由 x5 单独构造
	H.X.push('x3_H0_noCrossTalk=' + (H.menus().length === 1 ? 'Y' : 'N') + ',visible=' + H.menus().length)
	await H.escClose()
	// x5：只发 contextmenu，不经过任何外部指针事件 —— 检验两版能否同开（串扰的前置态），不进入 F
	const co1 = await H.rcCtxNode(d, H.DIR_ID)
	const co2 = await H.rcCtx(H.tabs(d).filter(x => x.classList.contains('is-active'))[0] || H.tabs(d)[0])
	H.X.push('x5_H0_coOpenTree=' + co1 + ',coOpenTab=' + co2)
	H.I.push('x5_menus=' + H.menus().map(m => m.tagName.toLowerCase() + '.' + String(m.className).split(' ')[0]).join('/'))
	// 收口：一次忠实的外部点击必须把所有菜单关掉（两版都应为 0，属行为断言，进 F）
	H.F.push('H0_closeAll=' + (await H.closeAll()))

	H.I.push('btn=' + H.BTN + ',rowIdx=' + op.rowIdx + ',rowCount=' + op.rowCount + ',attempt=' + op.attempt)
	H.I.push('vw=' + window.innerWidth + ',vh=' + window.innerHeight + ',dpr=' + window.devicePixelRatio)
	const reqs0 = H.stageReq(mark0)
	H.F.push('H0_writeReq=' + (H.writeReq(reqs0).join(';') || 'NONE'))
	H.out = () => {
		const b = H.F.join('\n')
		const i = H.I.join('\n')
		const x = H.X.join('\n')
		return { fpBehav: fnv(b), behavLen: b.length, fpId: fnv(i), idLen: i.length, fpDiff: fnv(x), diffLen: x.length, F: H.F, I: H.I, X: H.X }
	}
	return Object.assign(
		{
			stage: 'H0',
			reqCount: reqs0.length,
			writeReq: H.writeReq(reqs0).join(';') || 'NONE'
		},
		H.out()
	)
}

/* -----------------------------------------------------------------------------
 * STAGE H1a — 打开节点右键菜单并回传首个菜单项的视口中心坐标（供外层真 hover）
 * ========================================================================== */
async () => {
	const H = window.__HH
	if (!H) {
		return { ABORT: 'RUN_H0_FIRST' }
	}
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const rc = await H.rcNode(d, H.DIR_ID)
	const m = H.menu()
	const li = m ? [...m.querySelectorAll('li')].filter(H.vis)[0] : null
	if (!li) {
		return { ABORT: 'NO_ITEM', rc }
	}
	const r = li.getBoundingClientRect()
	H.HOVER_BOX = { rc, x: Math.round(r.left + r.width / 2), y: Math.round(r.top + r.height / 2) }
	return { stage: 'H1a', rc: rc, x: H.HOVER_BOX.x, y: H.HOVER_BOX.y, w: Math.round(r.width), h: Math.round(r.height) }
}

/* -----------------------------------------------------------------------------
 * STAGE H1b — 读 hover 后的计算色（hover 必须由外层真实鼠标移动触发）
 * ========================================================================== */
async () => {
	const H = window.__HH
	if (!H) {
		return { ABORT: 'RUN_H0_FIRST' }
	}
	H.F = []
	H.I = []
	H.X = []
	const d = H.mainReady() || H.mainDrawer()
	const m = H.menu()
	const li = m ? [...m.querySelectorAll('li')].filter(H.vis)[0] : null
	const hovered = li ? li.matches(':hover') : false
	const cs = li ? getComputedStyle(li) : null
	H.F.push('H1_hovered=' + (hovered ? 'Y' : 'N') + ',bg=' + (cs ? cs.backgroundColor : 'NA') + ',clr=' + (cs ? cs.color : 'NA'))
	// hover 底色与文字色必须等于同主题下 EP 变量的计算值（不比字面量，见 varAsRgb 注释）
	H.F.push('H1_varEq=bg:' + (cs && cs.backgroundColor === H.varAsRgb('--el-fill-color-dark') ? 'Y' : 'N') + ',clr:' + (cs && cs.color === H.varAsRgb('--el-text-color-primary') ? 'Y' : 'N'))
	if (d) {
		H.I.push('H1_chain=' + H.chain())
	}
	const left = await H.escClose()
	H.X.push('x2_H1_menuClosedByEsc=' + (left === 0 ? 'Y' : 'N') + ',left=' + left)
	H.I.push('H1_hoverPoint=' + JSON.stringify(H.HOVER_BOX || null))
	return Object.assign({ stage: 'H1b', menus: H.menus().length }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE H2 — 暗色（翻 html.dark）后同一套量测，末行断言类名已还原
 * ========================================================================== */
async () => {
	const H = window.__HH
	if (!H) {
		return { ABORT: 'RUN_H0_FIRST' }
	}
	H.F = []
	H.I = []
	H.X = []
	const d = H.mainReady() || H.mainDrawer()
	if (!d) {
		return { ABORT: 'NO_DRAWER' }
	}
	const mark = H.reqMark()
	const before = document.documentElement.classList.contains('dark')
	document.documentElement.classList.add('dark')
	await H.sleep(500)
	H.F.push('H2_darkApplied=' + (document.documentElement.classList.contains('dark') && !before ? 'Y' : 'N') + ',before=' + (before ? 'Y' : 'N'))
	// 暗色下 hover 两个取色变量的最终计算值：不依赖指针位置，与 H1_varEq（hover 规则本身已证）组合即覆盖「暗色 hover」
	H.F.push('H2_darkHoverVar=' + H.varAsRgb('--el-fill-color-dark') + '/' + H.varAsRgb('--el-text-color-primary'))

	const rc = await H.rcNode(d, H.DIR_ID)
	H.F.push('H2_treeMenu=' + rc + ',visible=' + H.menus().length)
	H.dump('H2')
	H.I.push('H2_bodyBg=' + getComputedStyle(document.body).backgroundColor)

	const left = await H.escClose()
	document.documentElement.classList.remove('dark')
	await H.sleep(400)
	H.F.push('H2_restored=' + (document.documentElement.classList.contains('dark') ? 'STILL_DARK' : 'LIGHT'))
	H.X.push('x2_H2_menuClosedByEsc=' + (left === 0 ? 'Y' : 'N') + ',left=' + left)
	H.F.push('H2_varBack=' + H.rootVar('--el-bg-color-overlay') + '/' + H.rootVar('--el-fill-color-dark'))

	const reqs = H.stageReq(mark)
	H.F.push('H2_writeReq=' + (H.writeReq(reqs).join(';') || 'NONE'))
	H.I.push('H2_reqs=' + reqs.join(';'))
	return Object.assign({ stage: 'H2', reqCount: reqs.length, dark: document.documentElement.classList.contains('dark') }, H.out())
}

/* -----------------------------------------------------------------------------
 * STAGE H3 — 收口：关抽屉 + 现场与暗色残留自检
 * ========================================================================== */
async () => {
	const H = window.__HH
	if (!H) {
		return { ABORT: 'RUN_H0_FIRST' }
	}
	H.F = []
	H.I = []
	H.X = []
	const dd = H.mainReady() || H.mainDrawer()
	if (dd) {
		const x = dd.querySelector('button[aria-label*="关闭"]') || dd.querySelector('.el-drawer__close-btn')
		if (x) {
			x.click()
		}
		await H.sleep(1200)
	}
	const left = await H.forceCloseMain()
	H.F.push('H3_drawers=' + left + ',menus=' + H.menus().length + ',dark=' + (document.documentElement.classList.contains('dark') ? 'Y' : 'N'))
	H.F.push('H3_writeReqTotal=' + (H.writeReq(H.REQ).join(';') || 'NONE'))
	H.I.push('H3_totalReq=' + H.REQ.length + ',uniq=' + [...new Set(H.REQ.map(r => r.split('?')[0]))].join(';'))
	return Object.assign(
		{
			stage: 'H3',
			drawers: left,
			menus: H.menus().length,
			dialogs: [...document.querySelectorAll('.el-dialog')].filter(H.vis).length,
			boxes: [...document.querySelectorAll('.el-message-box')].filter(H.vis).length
		},
		H.out()
	)
}
