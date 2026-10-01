const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';
const content = fs.readFileSync(path.join(dir, 'arrows.ts'), 'utf-8');

console.log('=== First 150 chars (JSON) ===');
console.log(JSON.stringify(content.substring(0, 150)));
console.log('\n=== Has sort export:', content.includes('export const sort'));
