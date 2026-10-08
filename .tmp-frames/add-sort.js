const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';

// Display order (matches current modules array order)
const order = [
  'brand', 'brand-fill', 'error', 'file', 'user',
  'communication', 'media', 'editor', 'data', 'navigation',
  'circle', 'square', 'arrows', 'status', 'currency',
  'ui-core', 'ui-tools', 'device', 'business', 'dev', 'misc'
];

for (let i = 0; i < order.length; i++) {
  const key = order[i];
  const file = path.join(dir, `${key}.ts`);
  let content = fs.readFileSync(file, 'utf-8');

  // Insert sort field after key line
  content = content.replace(
    /(export const key = '[^']+'\n)/,
    `$1export const sort = ${i + 1}\n`
  );

  fs.writeFileSync(file, content, 'utf-8');
  console.log(`OK: ${key}.ts → sort = ${i + 1}`);
}

console.log('\nDone!');
