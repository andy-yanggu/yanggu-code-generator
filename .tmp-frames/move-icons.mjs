import { readFileSync, writeFileSync, readdirSync } from 'fs'
import { join } from 'path'

const MODULES_DIR = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules'

function readFile(name) {
  return readFileSync(join(MODULES_DIR, name), 'utf-8').replace(/\r\n/g, '\n')
}
function writeFile(name, content) {
  writeFileSync(join(MODULES_DIR, name), content.replace(/\n/g, '\r\n'), 'utf-8')
}
function getAllIds(content) {
  return [...content.matchAll(/<symbol id="(icon-[^"]+)"/g)].map(m => m[1])
}
function extractSymbol(content, id) {
  const re = new RegExp(`<symbol id="${id}"[^]*?</symbol>`)
  const m = content.match(re)
  return m ? m[0] : null
}
function removeSymbols(content, ids) {
  const idSet = new Set(ids)
  const lines = content.split('\n')
  const result = []
  let inRemoved = false
  for (const line of lines) {
    if (inRemoved) {
      if (line.includes('</symbol>')) inRemoved = false
      continue
    }
    if (line.includes('<symbol')) {
      const m = line.match(/id="(icon-[^"]+)"/)
      if (m && idSet.has(m[1])) {
        if (!line.includes('</symbol>')) inRemoved = true
        continue
      }
    }
    result.push(line)
  }
  return result.join('\n')
}
function insertSymbols(content, symbols) {
  if (!symbols.length) return content
  const block = '\n' + symbols.join('\n') + '\n'
  return content.replace('</svg>`', block + '</svg>`')
}
function updateHeader(content, remainingIcons) {
  return content.replace(
    /(\*\s*)(\d+)\s*个图标[:：]\s*([\s\S]*?)(\s*\*\/)/,
    (_, pre, _n, _list, post) => {
      const n = remainingIcons.length
      const list = remainingIcons.map(id => id.replace('icon-', '')).join(', ')
      return `${pre}${n} 个图标: ${list}${post}`
    }
  )
}

// Find which module contains deleteteam and deleteuser
const files = readdirSync(MODULES_DIR).filter(f => f.endsWith('.ts') && !['index.ts', 'inject-svg.ts'].includes(f))
for (const f of files) {
  const c = readFile(f)
  if (c.includes('icon-deleteteam') || c.includes('icon-deleteuser')) {
    console.log(`Found in ${f}: deleteteam=${c.includes('icon-deleteteam')}, deleteuser=${c.includes('icon-deleteuser')}`)
  }
}

// Move them
const ids = ['icon-deleteteam', 'icon-deleteuser']
let srcFile = null
for (const f of files) {
  const c = readFile(f)
  if (ids.every(id => c.includes(`id="${id}"`))) { srcFile = f; break }
}

if (!srcFile) { console.log('Source not found!'); process.exit(1) }

console.log(`\nMoving from ${srcFile} → user.ts`)

let srcContent = readFile(srcFile)
let destContent = readFile('user.ts')

// Extract
const symbols = ids.map(id => extractSymbol(srcContent, id)).filter(Boolean)
console.log(`Extracted ${symbols.length} symbols`)

// Remove from source
srcContent = removeSymbols(srcContent, ids)
// Insert into dest
destContent = insertSymbols(destContent, symbols)

// Update headers
const srcIds = getAllIds(srcContent)
const destIds = getAllIds(destContent)
srcContent = updateHeader(srcContent, srcIds)
destContent = updateHeader(destContent, destIds)

// Write
writeFile(srcFile, srcContent)
writeFile('user.ts', destContent)

console.log(`${srcFile}: ${srcIds.length} icons remaining`)
console.log(`user.ts: ${destIds.length} icons now`)
console.log('Done!')
