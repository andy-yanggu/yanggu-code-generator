const fs = require('fs')
const path = require('path')

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules'
const file = path.join(dir, 'arrows.ts')
const content = fs.readFileSync(file, 'utf-8')

console.log('=== First 300 chars (JSON) ===')
console.log(JSON.stringify(content.substring(0, 300)))
console.log('\n=== Last 100 chars (JSON) ===')
console.log(JSON.stringify(content.substring(content.length - 100)))
console.log('\n=== Has injectSvg import:', content.includes("import { injectSvg }"))
console.log('=== Has export const svg:', content.includes('export const svg'))
console.log('=== Has backslash-quote in content:', content.includes('\\"'))
console.log('=== Total length:', content.length)

// Check if SVG content has literal backslash-quote or just quotes
const injectIdx = content.indexOf('injectSvg(')
if (injectIdx !== -1) {
  const snippet = content.substring(injectIdx, injectIdx + 80)
  console.log('\n=== injectSvg call start (JSON) ===')
  console.log(JSON.stringify(snippet))
}
