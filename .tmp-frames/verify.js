const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';
const file = path.join(dir, 'arrows.ts');
const content = fs.readFileSync(file, 'utf-8');

console.log('=== First 200 chars ===');
console.log(content.substring(0, 200));
console.log('\n=== Last 50 chars ===');
console.log(content.substring(content.length - 50));
console.log('\n=== Has injectSvg import:', content.includes("import { injectSvg }"));
console.log('=== Has export const key:', content.includes("export const key"));
console.log('=== Has export const label:', content.includes("export const label"));
console.log('=== Has export const svg:', content.includes("export const svg"));
console.log('=== Total lines:', content.split('\n').length);
