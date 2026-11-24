/* T2 Suite 语法自检：把磁盘上的 suite 按 `async () => {` 切成若干段，逐段编译。
 * 用途：suite 是通过 setInputFiles 送进页面后 eval 安装的，装之前先在这里确认能 parse。
 * 运行：node 2026-11-24-t2-check-syntax.mjs <suiteFile> <expectStages>
 *      不传参时默认 Suite S（5 段）；Suite N 传 2026-11-24-t2-suite-N-layout-interaction.js 11
 */
import fs from 'node:fs'

const FILE = process.argv[2] ? new URL('./' + process.argv[2], import.meta.url).pathname.replace(/^\//, '') : new URL('./2026-11-24-t2-suite-S-state-machine.js', import.meta.url).pathname.replace(/^\//, '')
const EXPECT = Number(process.argv[3] || 5)
const src = fs.readFileSync(FILE, 'utf8')
const parts = src.split(/\n(?=async \(\) => \{)/).filter(s => s.startsWith('async () =>'))

let ok = 0
parts.forEach((c, i) => {
	try {
		new Function('(' + c + ')')
		ok++
	} catch (e) {
		console.log('PARSE_FAIL stage#' + i + ' :: ' + e.message)
	}
})

// 畸形指纹键扫描：F.push 一次塞两个元素会让空键混进指纹串
const bad = []
src.split('\n').forEach((line, i) => {
	if (/F\.push\([^)]*,\s*H\.(resetToasts|mark|sleep)\(/.test(line)) bad.push('L' + (i + 1) + ': ' + line.trim())
	if (/^\s*F\.pop\(\)\s*$/.test(line)) bad.push('L' + (i + 1) + ': ' + line.trim())
})

console.log('FILE=' + FILE.split('/').pop() + ' STAGES=' + parts.length + ' EXPECT=' + EXPECT + ' ALL_PARSE_OK=' + (ok === parts.length && parts.length === EXPECT) + ' OK=' + ok + ' BYTES=' + src.length)
console.log('SUSPECT_PUSH=' + (bad.length ? '\n  ' + bad.join('\n  ') : 'none'))

// 源文本摘要：与页内 FileReader 读到的同一份文本做逐字比对，证明跑的就是磁盘上这份
// fnv 与 suite 内的 FNV-1a 完全同算法（遍历 UTF-16 code unit），BYTES 此处指字符数
const fnv = s => {
	let h = 0x811c9dc5
	for (let i = 0; i < s.length; i++) {
		h ^= s.charCodeAt(i)
		h = Math.imul(h, 0x01000193) >>> 0
	}
	return h.toString(16).toUpperCase().padStart(8, '0')
}
console.log('CHARS=' + src.length + ' OCTETS=' + Buffer.byteLength(src, 'utf8') + ' FNV=' + fnv(src) + ' BOM=' + (src.charCodeAt(0) === 0xfeff ? 'YES' : 'NO') + ' NL=' + (src.includes('\r\n') ? 'CRLF' : 'LF'))
