const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';
const content = fs.readFileSync(path.join(dir, 'arrows.ts'), 'utf-8');

// Show lines 1-12
const lines = content.split('\n');
for (let i = 0; i < Math.min(12, lines.length); i++) {
  console.log(`${i + 1}: ${lines[i]}`);
}
