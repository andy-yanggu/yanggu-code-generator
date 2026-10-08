const fs = require('fs');
const content = fs.readFileSync('F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules/index.ts', 'utf-8');
const lines = content.split('\n');
console.log(`Total lines: ${lines.length}`);
console.log('--- Lines 1-15 ---');
for (let i = 0; i < 15 && i < lines.length; i++) {
  console.log(`${i + 1}: ${lines[i]}`);
}
console.log('--- Lines 55-80 ---');
for (let i = 54; i < 80 && i < lines.length; i++) {
  console.log(`${i + 1}: ${lines[i]}`);
}
console.log('\n=== Has import.meta.glob:', content.includes('import.meta.glob'));
console.log('=== Has explicit import brand:', content.includes("import * as brand from"));
console.log('=== Has .sort:', content.includes('.sort((a, b)'));
