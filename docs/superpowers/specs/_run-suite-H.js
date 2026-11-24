// Suite H 双跑 runner：两版只差 sessionStorage.__T2_BTN，脚本文件与执行序列完全一致
// 调用前需先 set：sessionStorage.setItem('__T2_BTN', '模板v2' | '模板')
async (page) => {
	const FILE = 'E:/project/idea_project/self/yanggu-code-generator/docs/superpowers/specs/2026-11-24-t2-suite-H-dark-zindex.js';
	const EXPECT = '587FEF81';
	await page.reload({ waitUntil: 'networkidle' });
	const BTN = await page.evaluate(() => sessionStorage.getItem('__T2_BTN'));
	await page.evaluate(() => {
		let i = document.getElementById('__suite_up_h');
		if (!i) { i = document.createElement('input'); i.type = 'file'; i.id = '__suite_up_h'; i.style.cssText = 'position:fixed;left:-9999px;top:0'; document.body.appendChild(i); }
	});
	await page.setInputFiles('#__suite_up_h', FILE);
	const load = await page.evaluate(async btn => {
		const fnv = s => { let h = 0x811c9dc5; for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 0x01000193) >>> 0; } return h.toString(16).toUpperCase().padStart(8, '0'); };
		const src = await document.getElementById('__suite_up_h').files[0].text();
		window.__SUITE_H = src.split(/\n(?=async \(\) => \{)/);
		window.__BTN = btn;
		return { chars: src.length, fnv: fnv(src), expect: '587FEF81', parts: window.__SUITE_H.length, btn: window.__BTN };
	}, BTN);
	if (!BTN || load.fnv !== EXPECT) { return { ABORT: 'LOAD_FAILED', BTN, load }; }
	await page.waitForFunction(() => document.querySelectorAll('tbody tr').length > 0, null, { timeout: 20000 });
	const run = async n => { await page.bringToFront(); return await page.evaluate(async k => await eval('(' + window.__SUITE_H[k] + ')')(), n); };
	const h0 = await run(1);
	if (h0.ABORT) { return { load, h0 }; }
	const h1a = await run(2);
	let moved = null;
	if (h1a.x != null) {
		moved = [h1a.x, h1a.y];
		await page.mouse.move(h1a.x - 4, h1a.y - 3);
		await page.waitForTimeout(200);
		await page.mouse.move(h1a.x, h1a.y);
		await page.waitForTimeout(450);
	}
	const h1b = await run(3);
	// 指针停回与版本无关的固定点，否则 H2 的 item hover 态会被「上一段算出的本版本 li 中心」带进行为串
	await page.mouse.move(5, 400);
	await page.waitForTimeout(200);
	const h2 = await run(4);
	const h3 = await run(5);
	return { BTN, load, moved, h0, h1a, h1b, h2, h3 };
}
