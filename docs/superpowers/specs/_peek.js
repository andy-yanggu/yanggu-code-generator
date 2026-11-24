const fs = require('fs')
const p = process.argv[2]
const want = Number(process.argv[3])
const key = process.argv[4]
const lines = fs.readFileSync(p, 'utf8').split('\n')
const l = lines[want - 1] || ''
const i = l.indexOf(key)
console.log('LEN=' + l.length + ' IDX=' + i)
console.log(l.substring(Math.max(0, i - 600), Math.max(0, i - 600) + 2600))
