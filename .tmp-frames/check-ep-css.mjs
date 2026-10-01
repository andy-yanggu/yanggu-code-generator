import { readFileSync } from 'fs'
const js = readFileSync('F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/node_modules/element-plus/es/components/col/index.mjs', 'utf8')
console.log('=== ElCol component source ===')
console.log(js.substring(0, 2000))
