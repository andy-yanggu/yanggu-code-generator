const fs = require('fs');
const content = fs.readFileSync('F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules/index.ts', 'utf-8');
const lines = content.split('\n');

// Find lines with import.meta.glob
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('import.meta.glob') || lines[i].includes('rawModules')) {
    console.log(`Line ${i + 1}: ${lines[i]}`);
  }
}
