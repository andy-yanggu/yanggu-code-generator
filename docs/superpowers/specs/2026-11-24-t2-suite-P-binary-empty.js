/* =============================================================================
 * T2 双跑对照 · Suite P（F27 二进制展示 + F28 空态引导：数据受阻项的实跑批次）
 * 日期：2026-11-24
 *
 * 靶点（本批不在组 80，组 80 既无二进制节点也非空态，属数据受阻，故换靶）
 *   - F28 空态：自建夹具模板组 id 93「__T2FIXTURE_EMPTY__」（type=0、组下 0 条模板），
 *     由外层 SQL `INSERT INTO gen_template_group(...)` 建立，批次全程结束后
 *     `DELETE FROM gen_template WHERE template_group_id=93` + `DELETE FROM gen_template_group WHERE id=93` 物理回收；
 *   - F27 非图片分支：夹具组下由 P1 自己新建的二进制节点（fileName=`T2P-NOTIMAGE`、
 *     上传文件名 `fixture.jar`，8 字节固定内容 → base64 可复现），跑完物理删除；
 *   - F27 图片分支：存量真实数据 组 79「前端项目模板（VOC）」的 `404.png`（id 2112、父链 2015→2027→2111、
 *     template_content=68926 字符 base64 PNG），零写入。
 *
 * 用法（每段是顶格书写的独立 async 箭头函数，装载器按 ^async \(\) => \{$ 切段）
 *   1) reload → 注入隐藏 file input → setInputFiles(本文件绝对路径) → 页内 f.text() + FNV 核对磁盘值；
 *   2) window.__BTN：v2 = '模板v2'，v1 = '模板'；两版跑同一份源码，文件一字不改；
 *   3) 顺序 P0 → P1 → P2 → P3，段间只靠 window.__HP 传递 helper；每段前 page.bringToFront()；
 *   4) 两版之间外层必须把夹具组恢复成 0 条模板（P1 每轮都会新建 1 条）。
 *
 * 口径（两串，X 为空集且这是跑批前就定死的）
 *   - F（行为串）：两版必须逐字相同；
 *   - I（身份串）：允许不同且需逐条解释 = 入口按钮文案、行号、请求序列、自增 id、视口、组件挂载标记；
 *   - X（预期差异串）：**空**。写脚本前已逐行核对源码：
 *       F28 空态 = v1 `tree.vue` L68-81 与 v2 `tree-panel.vue` L56-69 的 `#empty` 插槽逐字同构；
 *       F27 两分支 = v1 `tree.vue` L228-237 与 v2 `editor-panel.vue` L71-80 逐字同构，
 *                     且 `imageTypeList`（v1 tree.vue L545 / v2 use-template-tree-state.ts L35）数组内容相同；
 *       新建表单是同一个 `form.vue`（两版共用），上传/校验通道不可能分叉。
 *     既然结构同源，就不预先豁免任何一项：本批任何 F 差异都按「真回归或探针缺陷」处理，不当预期差异吃掉。
 *
 * 已知环境事实（影响取证的三条）
 *   1. el-tree 的非虚拟渲染在这里表现为「祖先未展开时子节点不进 DOM」（实测组 79 抽屉首屏只有 15 个 .el-tree-node，
 *      而该组有 99 条模板），所以点目标二进制节点前必须**自顶向下逐级点开**祖先，且已展开的层不能重复点（点一下会折叠）；
 *   2. 抽屉 init 会自动激活 DFS 首个文件：夹具组空态时 tabs=0（正是 F28 要断的前提），
 *      组 79 会自动开 `favicon.ico`（id 2116，也是图片分支，src 前缀 data:image/x-icon），
 *      所以 P2 把「自动激活态」和「点开 404.png 后」分别断言，避免把 el-image 计数当成 404.png 的专属证据；
 *   3. 代码区只有一个共享 `el-main`（tab-pane 只放 label），所以 imageCount/cm 计数不受多余 tab 影响。
 *
 * 暗色与 hover 在本批无关：不翻 html.dark、不依赖指针位置（Suite H 的两条纪律在此体现为「都不触发」）。
 *
 * 本批首跑（v2 试点）当场探出并修掉的三条探针坑，后续双跑必须沿用：
 *   1. **写账不能按「非 GET 即写」判**：模板组分页查询是 `POST /gen/templateGroup/page`，只读；
 *      首版把列表搜索记成了写请求（P2 `count=1`、P3 `sessionWrites=4`），会让「本批只写 1 条」这条取证失真；
 *      现按语义端点集判（page/list/detail/tree/cascaderData/variableCatalog/info/get/export 都算读）。
 *   2. **本轮新建行的自增 id 绝不进行为串**：v2 试点新建的 `T2P-NOTIMAGE` 拿到 id 900123，v1 轮会是 900124，
 *      首版把它拼在 `P1_tabDump` 里就是自造假差异；现 P1 一律用 `tabNames`（只名字+ACT），id 只进 I。
 *   3. **el-image 的 `is-loading` 是时序双稳态**：data URI 的 load 可能早于 EP 监听器挂上，试点采到
 *      `el-image__inner is-loading` 而 `complete=true/natural=1100x731`；现取类名前先 `waitImgSettled`，
 *      并把 SETTLED/TIMEOUT 本身落档，避免把「什么时候采」写进「采到了什么」。
 * ========================================================================== */

/* -----------------------------------------------------------------------------
 * STAGE P0 — helper 建立 + F28 空态引导（夹具空组，零写入）
 * ========================================================================== */
async () => {
	const BTN = window.__BTN || '模板v2'
	const FIXTURE_GROUP = '__T2FIXTURE_EMPTY__'
	const VOC_KEYWORD = 'VOC'
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
	const txt = e => (((e || {}).innerText || '')).trim().replace(/\s+/g, ' ')

	const P = {
		BTN,
		FIXTURE_GROUP,
		VOC_KEYWORD,
		sleep,
		vis,
		fnv,
		hs,
		txt,
		F: [],
		I: [],
		X: [],
		REQ: [],
		BODIES: [],
		LASTFULL: '',
		reqPatched: false
	}
	window.__HP = P

	// ---- 请求 spy：记录 method+path（去 query）与非 GET 请求体原文
	P.spyReq = () => {
		if (P.reqPatched) {
			return 'ALREADY'
		}
		P.reqPatched = true
		const trim = u => String(u).replace(/^https?:\/\/[^/]+/, '').split('?')[0]
		const xo = XMLHttpRequest.prototype.open
		XMLHttpRequest.prototype.open = function (m, u) {
			P.LASTM = String(m).toUpperCase()
			P.REQ.push(P.LASTM + ' ' + trim(u))
			return xo.apply(this, Array.prototype.slice.call(arguments))
		}
		const xs = XMLHttpRequest.prototype.send
		XMLHttpRequest.prototype.send = function (b) {
			if (typeof b === 'string' && b.length) {
				P.BODIES.push(b)
				P.LASTFULL = b
			}
			return xs.apply(this, Array.prototype.slice.call(arguments))
		}
		const fo = window.fetch
		window.fetch = function (a, b) {
			const m = String((b && b.method) || (a && a.method) || 'GET').toUpperCase()
			P.REQ.push(m + ' ' + trim(a && a.url ? a.url : a))
			if (b && typeof b.body === 'string') {
				P.BODIES.push(b.body)
				P.LASTFULL = b.body
			}
			return fo.apply(this, arguments)
		}
		return 'OK'
	}
	P.reqMark = () => P.REQ.length
	P.stageReq = mark => P.REQ.slice(mark)
	// 「非 GET 即写库」在本页不成立：模板组分页查询是 POST /gen/templateGroup/page（只读）。
	// 写账必须按语义端点判，否则列表页搜索会被记成写请求，把「本批只写了 1 条」这条取证带偏。
	P.READ_POST_PAT = /\/(page|list|detail|tree|cascaderData|variableCatalog|info|get|export)$/i
	P.isWrite = r => {
		const m = r.split(' ')[0]
		const p = r.split(' ')[1] || ''
		if (/^(GET|HEAD|OPTIONS)$/.test(m)) {
			return false
		}
		return !P.READ_POST_PAT.test(p)
	}
	P.reqPaths = list => [...new Set(list)].sort().join(';')
	P.writePaths = list => [...new Set(list.filter(P.isWrite))].sort().join(';')
	P.writeCount = list => list.filter(P.isWrite).length
	P.readPaths = list => [...new Set(list.filter(r => !P.isWrite(r)))].sort().join(';')
	P.bodyKeys = () => {
		try {
			return Object.keys(JSON.parse(P.LASTFULL))
				.sort()
				.join(',')
		} catch (e) {
			return 'UNPARSEABLE'
		}
	}
	P.bodyPick = fn => {
		try {
			return String(fn(JSON.parse(P.LASTFULL)))
		} catch (e) {
			return 'UNPARSEABLE'
		}
	}

	// ---- 列表页搜索 + 开抽屉（夹具组只有 1 行命中；组 79 用 'VOC' 关键字命中，绕开全角括号）
	P.drawerOf = () =>
		[...document.querySelectorAll('.el-drawer')]
			.filter(vis)
			.find(x => ((x.querySelector('.el-drawer__title') || {}).innerText || '').indexOf('模板配置') === 0)
	P.box = () => [...document.querySelectorAll('.el-message-box')].filter(vis)[0] || null
	P.setVal = (inp, v) => {
		const proto = inp.tagName === 'TEXTAREA' ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype
		Object.getOwnPropertyDescriptor(proto, 'value').set.call(inp, v)
		inp.dispatchEvent(new Event('input', { bubbles: true }))
		return 'SET'
	}
	P.clickBtn = (scope, label) => {
		const b = [...scope.querySelectorAll('button')].filter(vis).find(x => x.innerText.trim() === label)
		if (b) {
			b.click()
		}
		return b ? 'CLICKED' : 'NO_BTN'
	}
	P.forceCloseMain = async () => {
		for (let i = 0; i < 4; i++) {
			const box = P.box()
			if (box) {
				// 本批不编辑内容，理论上不出确认框；这里一律点「否(N)」丢弃，绝不点「是(Y)」写库
				const no = [...box.querySelectorAll('button')].filter(vis).find(b => b.innerText.trim().indexOf('否(N)') >= 0) || box.querySelector('.el-message-box__close')
				if (no) {
					no.click()
					await sleep(600)
				}
			}
			const d = P.drawerOf()
			if (!d) {
				break
			}
			const x = d.querySelector('button[aria-label*="关闭"]') || d.querySelector('.el-drawer__close-btn')
			if (x) {
				x.click()
			}
			await sleep(700)
			for (let k = 0; k < 12; k++) {
				await sleep(200)
				if (!P.drawerOf()) {
					break
				}
			}
		}
		return {
			drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length,
			boxes: [...document.querySelectorAll('.el-message-box')].filter(vis).length
		}
	}
	P.gotoGroup = async keyword => {
		const inp = [...document.querySelectorAll('form input.el-input__inner')].filter(vis).find(i => i.placeholder === '请输入模板组名称')
		if (!inp) {
			return { ok: false, why: 'NO_SEARCH_INPUT' }
		}
		P.setVal(inp, keyword)
		await sleep(300)
		if (P.clickBtn(document, '查询') !== 'CLICKED') {
			return { ok: false, why: 'NO_QUERY_BTN' }
		}
		let rows = []
		for (let i = 0; i < 25; i++) {
			await sleep(200)
			rows = [...document.querySelectorAll('tbody tr')].filter(vis)
			if (rows.length === 1) {
				break
			}
		}
		if (rows.length !== 1) {
			return { ok: false, why: 'ROWS_' + rows.length }
		}
		const b = [...rows[0].querySelectorAll('button')].filter(vis).find(x => x.innerText.trim() === BTN)
		if (!b) {
			return { ok: false, why: 'BTN_MISSING:' + BTN }
		}
		for (let attempt = 1; attempt <= 3; attempt++) {
			b.click()
			for (let i = 0; i < 25; i++) {
				await sleep(200)
				const d = P.drawerOf()
				if (d) {
					await sleep(1200)
					return { ok: true, drawer: d, attempt, rowCount: rows.length, rowIdx: rows.length ? [...document.querySelectorAll('tbody tr')].indexOf(rows[0]) : -1 }
				}
			}
		}
		return { ok: false, why: 'DRAWER_NOT_OPEN' }
	}

	// ---- 树与节点
	P.node = (d, id) => d.querySelector('.el-tree-node[data-key="' + id + '"]')
	P.nodeContent = (d, id) => {
		const n = P.node(d, id)
		return n ? n.querySelector(':scope > .el-tree-node__content') : null
	}
	P.clickNode = async (d, id) => {
		const c = P.nodeContent(d, id)
		if (!c) {
			return 'NO_NODE'
		}
		c.click()
		await sleep(700)
		return 'CLICKED'
	}
	// 祖先链 id 自顶向下（来源：跑批前用 SQL 查出的 parent 链，不是运行时猜的）；已展开的层绝不重复点
	P.reveal = async (d, chain) => {
		const log = []
		for (const id of chain) {
			const n = P.node(d, id)
			if (!n) {
				log.push(id + ':MISSING')
				return { ok: false, log }
			}
			if (n.classList.contains('is-expanded')) {
				log.push(id + ':ALREADY')
				continue
			}
			await P.clickNode(d, id)
			const after = P.node(d, id)
			log.push(id + ':' + (after && after.classList.contains('is-expanded') ? 'E' : 'C'))
		}
		return { ok: true, log }
	}
	P.nodeIcon = (d, id) => {
		const c = P.nodeContent(d, id)
		if (!c) {
			return 'NO_NODE'
		}
		const u = c.querySelector('use')
		return u ? u.getAttribute('xlink:href') || u.getAttribute('href') || 'NO_HREF' : 'NO_USE'
	}
	P.nodeLabel = (d, id) => {
		const c = P.nodeContent(d, id)
		return c ? txt(c) : 'NO_NODE'
	}
	P.tabs = d => [...d.querySelectorAll('.el-tabs__item')].filter(t => vis(t) && !t.closest('.template-test-drawer'))
	P.tabDump = d => P.tabs(d).map(t => t.id.replace(/^tab-/, '') + '/' + txt(t) + '/' + (t.classList.contains('is-active') ? 'ACT' : '-')).join(';')
	// 只取名字不取 id：P1 的 tab 是本轮新建的自增行，两版 id 必不同，带 id 进行为串就是自造假差异
	P.tabNames = d => P.tabs(d).map(t => txt(t) + '/' + (t.classList.contains('is-active') ? 'ACT' : '-')).join(';')
	P.cm = d => [...d.querySelectorAll('.cm-editor')].filter(vis).length
	P.headerBtns = d => [...d.querySelectorAll('.el-header button')].filter(vis).map(b => txt(b) + '=' + b.disabled).join('|')
	P.mainText = d => {
		const m = d.querySelector('.el-main')
		return m ? txt(m) : 'NO_MAIN'
	}
	P.images = d => [...d.querySelectorAll('.el-image')].filter(vis)
	P.imgProbe = d => {
		const im = P.images(d)[0]
		const inner = im ? im.querySelector('img') : null
		if (!inner) {
			return { count: P.images(d).length, inner: 'NO_INNER' }
		}
		const src = inner.getAttribute('src') || ''
		return {
			count: P.images(d).length,
			imgCls: inner.className,
			wrapCls: im.className,
			prefix: src.slice(0, 26),
			len: src.length,
			natural: inner.naturalWidth + 'x' + inner.naturalHeight,
			complete: inner.complete ? 'Y' : 'N',
			objFit: getComputedStyle(inner).objectFit
		}
	}
	P.waitImg = async (d, ms) => {
		const t0 = performance.now()
		while (performance.now() - t0 < ms) {
			const im = P.images(d)[0]
			const inner = im ? im.querySelector('img') : null
			if (inner && inner.complete && inner.naturalWidth > 0) {
				return 'LOADED'
			}
			await sleep(150)
		}
		return 'TIMEOUT'
	}
	// EP 的 el-image 在 load 回调里才摸掉 is-loading，data URI 的 load 可能早于监听器挂上 → 类名是个时序双稳态，
	// 先等它落定再取 imgCls，避免把「什么时候采」写进「采到了什么」
	P.waitImgSettled = async (d, ms) => {
		const t0 = performance.now()
		while (performance.now() - t0 < ms) {
			const im = P.images(d)[0]
			const inner = im ? im.querySelector('img') : null
			if (inner && inner.complete && inner.naturalWidth > 0 && inner.className.indexOf('is-loading') < 0) {
				return 'SETTLED'
			}
			await sleep(150)
		}
		return 'TIMEOUT'
	}

	// ---- 右键（Suite H 纪律①：真实右键 = pointerdown → mousedown → contextmenu，一个都不能缺）
	P.evs = (el, list) => {
		const r = el.getBoundingClientRect()
		const x = Math.round(r.left + Math.min(r.width, 120) / 2)
		const y = Math.round(r.top + r.height / 2)
		for (const t of list) {
			const Ctor = t.startsWith('pointer') ? PointerEvent : MouseEvent
			el.dispatchEvent(new Ctor(t, { bubbles: true, cancelable: true, clientX: x, clientY: y, button: t === 'contextmenu' ? 2 : 0 }))
		}
		return { x, y }
	}
	P.menus = () => [...document.querySelectorAll('.context-menu')].filter(vis)
	P.menu = () => P.menus()[0] || null
	P.rc = async el => {
		if (!el) {
			return 'NO_TARGET'
		}
		P.evs(el, ['pointerdown', 'mousedown'])
		await sleep(60)
		P.evs(el, ['contextmenu'])
		await sleep(600)
		return P.menu() ? 'OPEN' : 'NOT_OPEN'
	}
	P.rcBlank = async d => P.rc(d.querySelector('.el-tree'))
	P.menuLabels = () => {
		const m = P.menu()
		return m ? [...m.querySelectorAll('li')].filter(vis).map(li => txt(li)).join('|') : 'NO_MENU'
	}
	P.clickMenuItem = async label => {
		const m = P.menu()
		if (!m) {
			return 'NO_MENU'
		}
		const li = [...m.querySelectorAll('li')].find(x => txt(x) === label)
		if (!li) {
			return 'ITEM_MISSING'
		}
		li.click()
		await sleep(900)
		return 'CLICKED'
	}
	P.clickAway = async (x, y) => {
		for (const t of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']) {
			const Ctor = t.startsWith('pointer') ? PointerEvent : MouseEvent
			document.body.dispatchEvent(new Ctor(t, { bubbles: true, cancelable: true, clientX: x, clientY: y, button: 0 }))
			await sleep(30)
		}
	}
	P.closeAll = async () => {
		for (let i = 0; i < 3 && P.menus().length > 0; i++) {
			await P.clickAway(5, 400)
			await sleep(300)
		}
		return P.menus().length
	}

	// ---- 新增弹窗（表单是两版共用的同一个 form.vue）
	P.dlg = () => [...document.querySelectorAll('.el-dialog')].filter(vis).find(x => !x.closest('.template-test-drawer')) || null
	P.dlgTitle = () => {
		const g = P.dlg()
		return g ? txt(g.querySelector('.el-dialog__title')) : 'NO_DLG'
	}
	P.dlgItem = label => {
		const g = P.dlg()
		if (!g) {
			return null
		}
		return [...g.querySelectorAll('.el-form-item')].filter(vis).find(x => txt(x.querySelector('.el-form-item__label')) === label) || null
	}
	P.dlgInput = label => {
		const it = P.dlgItem(label)
		return it ? it.querySelector('input:not([type=radio]):not([type=checkbox]), textarea') : null
	}
	P.dlgDump = () => {
		const g = P.dlg()
		if (!g) {
			return 'NO_DLG'
		}
		return [...g.querySelectorAll('.el-form-item')]
			.filter(vis)
			.map(it => {
				const lb = txt(it.querySelector('.el-form-item__label'))
				const inp = it.querySelector('input:not([type=radio]):not([type=checkbox]), textarea')
				const checked = [...it.querySelectorAll('.el-radio')].filter(r => r.className.indexOf('is-checked') >= 0).map(r => txt(r))
				return lb + '[' + (inp ? inp.type : it.querySelector('.el-upload') ? 'upload' : 'other') + (checked.length ? ' c=' + checked.join(',') : '') + ']'
			})
			.join(';;')
	}
	P.dlgErrors = () => {
		const g = P.dlg()
		return g ? [...g.querySelectorAll('.el-form-item__error')].filter(vis).map(e => txt(e)).join(',') : 'NO_DLG'
	}
	P.radioChecked = label => {
		const it = P.dlgItem(label)
		if (!it) {
			return 'NO_ITEM'
		}
		return [...it.querySelectorAll('.el-radio')].filter(r => r.className.indexOf('is-checked') >= 0).map(r => txt(r)).join(',') || 'NONE'
	}
	P.waitDlgGone = async ms => {
		const t0 = performance.now()
		while (performance.now() - t0 < ms) {
			if (!P.dlg()) {
				return 'CLOSED'
			}
			await sleep(150)
		}
		return 'STILL'
	}
	P.toasts = () => [...document.querySelectorAll('.el-message')].filter(vis).map(m => txt(m))

	// 入口按钮属「谁在跑」的身份信息，不是行为 → 只进 I（Suite G/H 的同一口径：写进行为串必产假差异）
	P.I.push('P0_btn=' + BTN)
	P.spyReq()
	const fc0 = await P.forceCloseMain()
	P.F.push('P0_preClean=' + fc0.drawers + '/' + fc0.boxes)

	const op = await P.gotoGroup(FIXTURE_GROUP)
	if (!op.ok) {
		return { ABORT: 'P0_' + op.why, fc0 }
	}
	const d = op.drawer
	// 空态块可能随首帧就出现，这里再等一次并记录等待结果（不等就说明探针读的是脏态）
	let emptySeen = 'NO'
	for (let i = 0; i < 12; i++) {
		if (d.querySelector('.el-tree__empty-block')) {
			emptySeen = 'YES'
			break
		}
		await sleep(150)
	}
	const empty = d.querySelector('.el-empty')
	const nodes = [...d.querySelectorAll('.el-tree-node')].length
	const eText = empty ? txt(empty.querySelector('.el-empty__description')) : 'NO_EMPTY'
	P.F.push('P0_title=' + txt(d.querySelector('.el-drawer__title')))
	P.F.push('P0_treeNodes=' + nodes + ',emptyBlock=' + [...d.querySelectorAll('.el-tree__empty-block')].length + ',emptySeen=' + emptySeen)
	P.F.push('P0_emptyCls=' + (empty ? empty.className : 'NONE') + ',emptyHostCls=' + (empty ? empty.parentElement.className : 'NONE'))
	P.F.push('P0_emptyDescRows=' + (empty ? [...empty.querySelectorAll('.el-empty__description .el-row')].length : -1) + ',emptyImage=' + (empty ? [...empty.querySelectorAll('.el-empty__image')].length : -1))
	P.F.push('P0_emptyText=' + hs(eText))
	P.F.push('P0_emptyLine1=' + (eText.indexOf('暂无数据') === 0 ? 'Y' : 'N') + ',line2=' + (eText.indexOf('请在空白处点击右键新建目录、模板文件或者二进制文件') > 0 ? 'Y' : 'N'))
	P.F.push('P0_editorHint=' + (d.innerText.indexOf('请点击左侧的模板进行查看和修改') >= 0 ? 'Y' : 'N') + ',tabs=' + P.tabs(d).length + ',cm=' + P.cm(d) + ',headerBtns=' + P.headerBtns(d))

	const mark1 = P.reqMark()
	const rcB = await P.rcBlank(d)
	P.F.push('P0_blankMenu=' + rcB + ',' + P.menuLabels())
	P.I.push('P0_blankMenuDom=' + (P.menu() ? P.menu().tagName.toLowerCase() + '.' + P.menu().className : 'NONE'))
	P.F.push('P0_menuClose=' + (await P.closeAll()))
	const reqs1 = P.stageReq(mark1)
	P.F.push('P0_writeReq=' + (P.writePaths(reqs1) || 'NONE') + ',count=' + P.writeCount(reqs1))

	P.I.push('P0_rowIdx=' + op.rowIdx + ',rowCount=' + op.rowCount + ',attempt=' + op.attempt)
	P.I.push('P0_total=' + ((document.querySelector('.el-pagination__total') || {}).innerText || 'NA'))
	P.I.push('P0_emptyTextRaw=' + eText)
	P.I.push('P0_treePanel=' + (d.querySelector('.tree-panel') ? 'Y' : 'N') + ',editorPanel=' + (d.querySelector('.editor-panel') ? 'Y' : 'N'))
	P.I.push('P0_reqs=' + reqs1.join(';'))
	P.I.push('P0_vw=' + window.innerWidth + ',vh=' + window.innerHeight)
	P.X.push('P0_expectedDiff=EMPTY_SET(源码同构，见文件头)')

	P.out = () => ({
		fpBehav: fnv(P.F.join('\n')),
		behavLen: P.F.join('\n').length,
		fpId: fnv(P.I.join('\n')),
		idLen: P.I.join('\n').length,
		fpX: fnv(P.X.join('\n')),
		xLen: P.X.join('\n').length,
		F: P.F,
		I: P.I,
		X: P.X
	})
	P.reset = () => {
		P.F = []
		P.I = []
		P.X = []
	}
	return Object.assign({ stage: 'P0', drawers: [...document.querySelectorAll('.el-drawer')].filter(vis).length }, P.out())
}

/* -----------------------------------------------------------------------------
 * STAGE P1 — F27 非图片二进制分支（夹具组内新建 fixture.jar，本批唯一写请求）
 * ========================================================================== */
async () => {
	const P = window.__HP
	if (!P) {
		return { ABORT: 'RUN_P0_FIRST' }
	}
	P.reset()
	const { sleep, vis, hs, txt } = P
	const d = P.drawerOf()
	if (!d) {
		return { ABORT: 'NO_DRAWER(先跑P0)' }
	}
	const mark = P.reqMark()

	const rc = await P.rcBlank(d)
	P.F.push('P1_rc=' + rc + ',menu=' + P.menuLabels())
	const ck = await P.clickMenuItem('新建二进制文件')
	P.F.push('P1_click=' + ck + ',title=' + P.dlgTitle())
	const dump = P.dlgDump()
	P.F.push('P1_dump=' + hs(dump))
	P.I.push('P1_dumpRaw=' + dump)
	P.F.push('P1_radio=' + P.radioChecked('模板类型') + ',pathIsRoot=' + ((P.dlgInput('路径') || {}).value === '/' ? 'Y' : 'N'))
	const upInp = P.dlgInput('文件上传')
	P.F.push('P1_uploadCtl=' + (upInp ? upInp.tagName.toLowerCase() + '/' + upInp.type + '.' + upInp.className : 'NO_INPUT'))
	const nameInp = P.dlgInput('文件名称')
	if (!upInp || !nameInp) {
		return { ABORT: 'P1_FIELD_MISSING', upload: !!upInp, fileName: !!nameInp, dump: P.dlgDump() }
	}

	const fillName = P.setVal(nameInp, 'T2P-NOTIMAGE')
	await sleep(300)
	P.F.push('P1_fillName=' + fillName + ',nameVal=' + nameInp.value)

	// 固定 8 字节内容 + 固定文件名 + 固定 MIME → base64 逐字符可复现（非图片扩展名是本段的断言对象）
	const dt = new DataTransfer()
	dt.items.add(new File([Uint8Array.from([0x50, 0x4b, 0x03, 0x04, 0xde, 0xad, 0xbe, 0xef])], 'fixture.jar', { type: 'application/octet-stream' }))
	upInp.files = dt.files
	upInp.dispatchEvent(new Event('change', { bubbles: true }))
	await sleep(1200)
	const chip = [...(P.dlg() || document).querySelectorAll('.el-upload-list__item')].filter(vis).map(x => txt(x)).join(',')
	P.F.push('P1_fileChip=' + chip + ',requiredErrCleared=' + (P.dlgErrors().indexOf('文件不能为空') >= 0 ? 'Y' : 'N'))

	P.F.push('P1_ok=' + P.clickBtn(P.dlg() || document, '确定'))
	const gone = await P.waitDlgGone(5000)
	await sleep(1500)
	P.F.push('P1_dlgGone=' + gone + ',toasts=' + hs(P.toasts().join('|')))

	const keys = [...d.querySelectorAll('.el-tree-node')].map(n => n.getAttribute('data-key'))
	const newId = keys.filter(k => P.nodeLabel(d, k) === 'T2P-NOTIMAGE')[0] || 'NOT_FOUND'
	P.F.push('P1_emptyBlockGone=' + [...d.querySelectorAll('.el-tree__empty-block')].filter(vis).length + ',treeNodes=' + keys.length)
	P.F.push('P1_nodeLabel=' + P.nodeLabel(d, newId) + ',nodeIcon=' + P.nodeIcon(d, newId))
	P.F.push('P1_tabNames=' + P.tabNames(d) + ',cm=' + P.cm(d))

	P.F.push('P1_clickNode=' + (await P.clickNode(d, newId)))
	await sleep(1200)
	P.F.push('P1_activeTab=' + P.tabNames(d))
	P.F.push('P1_previewText=' + hs(P.mainText(d)) + ',noSupportWord=' + (d.innerText.indexOf('文件暂不支持预览（目前只支持图片）') >= 0 ? 'Y' : 'N'))
	P.F.push('P1_imageCount=' + P.images(d).length + ',cm=' + P.cm(d) + ',headerBtns=' + P.headerBtns(d))

	const reqs = P.stageReq(mark)
	const writes = reqs.filter(P.isWrite)
	P.F.push('P1_writes=' + writes.length + ',' + P.writePaths(reqs))
	P.F.push('P1_bodyKeys=' + P.bodyKeys())
	P.F.push('P1_body:fileName=' + P.bodyPick(j => j.fileName) + ',binName=' + P.bodyPick(j => j.binaryOriginalFileName) + ',type=' + P.bodyPick(j => j.templateType) + ',parentId=' + P.bodyPick(j => j.parentId) + ',groupId=' + P.bodyPick(j => j.templateGroupId))
	P.F.push('P1_bodyContent=' + P.bodyPick(j => (j.templateContent || '').slice(0, 26)) + ',len=' + P.bodyPick(j => (j.templateContent || '').length))
	P.I.push('P1_newNodeId=' + newId)
	P.I.push('P1_bodyRaw=' + (writes.length ? P.LASTFULL : 'NONE'))
	P.I.push('P1_reqs=' + reqs.join(';'))
	P.I.push('P1_btn=' + P.BTN)
	P.X.push('P1_expectedDiff=EMPTY_SET(共用 form.vue)')
	return Object.assign({ stage: 'P1', reqCount: reqs.length, writeReq: writes.join(';') || 'NONE', toasts: P.toasts().join('|') }, P.out())
}

/* -----------------------------------------------------------------------------
 * STAGE P2 — F27 图片分支（组 79 存量 404.png id 2112，零写入）
 * ========================================================================== */
async () => {
	const P = window.__HP
	if (!P) {
		return { ABORT: 'RUN_P0_FIRST' }
	}
	P.reset()
	const { sleep, vis, hs, txt } = P
	const mark = P.reqMark()

	const fc = await P.forceCloseMain()
	P.F.push('P2_preClean=' + fc.drawers + '/' + fc.boxes)
	const op = await P.gotoGroup(P.VOC_KEYWORD)
	if (!op.ok) {
		return { ABORT: 'P2_' + op.why, fc }
	}
	const d = op.drawer
	P.F.push('P2_title=' + txt(d.querySelector('.el-drawer__title')) + ',nodes=' + [...d.querySelectorAll('.el-tree-node')].length)
	// 自动激活态：init 会开 DFS 首个文件 favicon.ico(2116)，它同样是图片分支 → 先把它单独落档，
	// 否则后面的 imageCount=1 说不清是 404.png 的还是 favicon.ico 的
	P.F.push('P2_autoTab=' + P.tabDump(d))
	const autoImg = P.imgProbe(d)
	P.F.push('P2_autoImg=' + autoImg.count + ',' + autoImg.prefix + ',len=' + autoImg.len + ',' + autoImg.natural)

	const rv = await P.reveal(d, [2015, 2027, 2111])
	P.F.push('P2_reveal=' + (rv.ok ? 'OK' : 'FAIL') + ',' + rv.log.join(','))
	if (!rv.ok || !P.node(d, 2112)) {
		return { ABORT: 'P2_TARGET_NOT_REVEALED', log: rv.log }
	}
	P.F.push('P2_nodeLabel=' + P.nodeLabel(d, 2112) + ',nodeIcon=' + P.nodeIcon(d, 2112))
	P.F.push('P2_click=' + (await P.clickNode(d, 2112)))
	const loadState = await P.waitImg(d, 4000)
	const clsState = await P.waitImgSettled(d, 3000)
	const ip = P.imgProbe(d)
	P.F.push('P2_tabDump=' + P.tabDump(d) + ',waitImg=' + loadState + ',waitCls=' + clsState)
	P.F.push('P2_imageCount=' + ip.count + ',wrapCls=' + ip.wrapCls + ',imgCls=' + ip.imgCls)
	P.F.push('P2_src=' + ip.prefix + ',len=' + ip.len + ',complete=' + ip.complete)
	P.F.push('P2_natural=' + ip.natural + ',objFit=' + ip.objFit)
	P.F.push('P2_noSupport=' + (d.innerText.indexOf('暂不支持') >= 0 ? 'Y' : 'N') + ',mainText=' + hs(txt(d.querySelector('.el-main'))) + ',cm=' + P.cm(d))
	P.F.push('P2_headerBtns=' + P.headerBtns(d))

	const reqs = P.stageReq(mark)
	P.F.push('P2_writeReq=' + (P.writePaths(reqs) || 'NONE') + ',count=' + P.writeCount(reqs))
	P.I.push('P2_rowIdx=' + op.rowIdx + ',attempt=' + op.attempt + ',rowCount=' + op.rowCount)
	P.I.push('P2_reqs=' + reqs.join(';'))
	P.I.push('P2_btn=' + P.BTN)
	P.X.push('P2_expectedDiff=EMPTY_SET')
	return Object.assign({ stage: 'P2', reqCount: reqs.length, writeReq: P.writePaths(reqs) || 'NONE' }, P.out())
}

/* -----------------------------------------------------------------------------
 * STAGE P3 — 收口：关抽屉（不该有未保存确认）+ 列表复位 + 现场清零 + 会话请求账
 * ========================================================================== */
async () => {
	const P = window.__HP
	if (!P) {
		return { ABORT: 'RUN_P0_FIRST' }
	}
	P.reset()
	const { vis, hs } = P
	const d = P.drawerOf()
	if (d) {
		const x = d.querySelector('button[aria-label*="关闭"]') || d.querySelector('.el-drawer__close-btn')
		if (x) {
			x.click()
		}
		await P.sleep(1200)
	}
	P.F.push('P3_drawerConfirm=' + (P.box() ? 'Y' : 'N'))
	if (P.box()) {
		P.F.push('P3_boxText=' + hs(txt(P.box())))
		const no = [...P.box().querySelectorAll('button')].filter(vis).find(b => b.innerText.trim().indexOf('否(N)') >= 0)
		if (no) {
			no.click()
		}
		await P.sleep(1000)
	}
	const fc = await P.forceCloseMain()
	P.clickBtn(document, '重置')
	for (let i = 0; i < 15; i++) {
		await P.sleep(200)
		if ([...document.querySelectorAll('tbody tr')].filter(vis).length > 1) {
			break
		}
	}
	P.F.push('P3_drawers=' + fc.drawers + ',dialogs=' + [...document.querySelectorAll('.el-dialog')].filter(vis).length + ',menus=' + P.menus().length)
	P.F.push('P3_boxes=' + [...document.querySelectorAll('.el-message-box')].filter(vis).length + ',msgs=' + [...document.querySelectorAll('.el-message')].filter(vis).length + ',overlay=' + [...document.querySelectorAll('.el-overlay')].filter(vis).length)
	P.F.push('P3_resetRows=' + [...document.querySelectorAll('tbody tr')].filter(vis).length + ',darkResidue=' + (document.documentElement.classList.contains('dark') ? 'Y' : 'N'))
	P.F.push('P3_bodyLen=' + P.BODIES.length)

	const session = P.REQ
	P.F.push('P3_sessionWrites=' + P.writeCount(session) + ',' + (P.writePaths(session) || 'NONE'))
	P.F.push('P3_readPaths=' + P.readPaths(session))
	P.I.push('P3_reqSeq=' + session.join(';'))
	P.I.push('P3_reqTotal=' + session.length)
	P.I.push('P3_btn=' + P.BTN)
	P.I.push('P3_vw=' + window.innerWidth + ',vh=' + window.innerHeight)
	return Object.assign({ stage: 'P3', sessionReq: session.length, writePaths: P.writePaths(session) || 'NONE' }, P.out())
}
