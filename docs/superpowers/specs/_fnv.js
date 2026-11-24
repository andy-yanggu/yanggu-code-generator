// 计算套件脚本的磁盘侧 FNV-1a32（与页内 f.text() 的 UTF-16 码元序列同口径），用于装载双侧校验
const fs = require('fs')
const p = process.argv[2]
const t = fs.readFileSync(p, 'utf8')
let h = 0x811c9dc5
for (let i = 0; i < t.length; i++) {
	h ^= t.charCodeAt(i)
	h = Math.imul(h, 0x01000193) >>> 0
}
const stages = t.split(/\n(?=async \(\) => \{)/).length - 1
console.log('chars=' + t.length + ' bytes=' + fs.statSync(p).size + ' fnv=' + h.toString(16).toUpperCase().padStart(8, '0') + ' stages=' + stages)
