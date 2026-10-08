const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';
const files = fs.readdirSync(dir).filter(f => f.endsWith('.ts') && f !== 'index.ts' && f !== 'inject-svg.ts');

console.log(`Found ${files.length} files to process\n`);

for (const file of files) {
  const filePath = path.join(dir, file);
  const content = fs.readFileSync(filePath, 'utf-8');
  const key = path.basename(file, '.ts');

  // Extract label from comment: * 方向箭头与三角 (14 个图标)
  const labelMatch = content.match(/\* (.+?) \(\d+ 个图标\)/);
  if (!labelMatch) {
    console.log(`SKIP (no label): ${file}`);
    continue;
  }
  const label = labelMatch[1];

  // Find injectSvg("...") call
  const injectStart = content.indexOf('injectSvg("');
  if (injectStart === -1) {
    console.log(`SKIP (no injectSvg): ${file}`);
    continue;
  }

  // Content starts after injectSvg("
  const contentStart = injectStart + 'injectSvg("'.length;

  // Find closing ") - scan for unescaped " followed by )
  let contentEnd = -1;
  for (let i = contentStart; i < content.length - 1; i++) {
    if (content[i] === '\\' && content[i + 1] === '"') {
      i++; // skip escaped quote
      continue;
    }
    if (content[i] === '"' && content[i + 1] === ')') {
      contentEnd = i;
      break;
    }
  }

  if (contentEnd === -1) {
    console.log(`SKIP (no closing): ${file}`);
    continue;
  }

  // Extract SVG content (with \" escapes) and unescape
  let svgContent = content.substring(contentStart, contentEnd);
  svgContent = svgContent.replace(/\\"/g, '"');

  // Get comment block (everything before import line)
  const importIdx = content.indexOf("import { injectSvg }");
  const comment = content.substring(0, importIdx).replace(/[\r\n]+$/, '');

  // Build new content
  const newContent = comment + '\n\n' +
    "export const key = '" + key + "'\n" +
    "export const label = '" + label + "'\n\n" +
    'export const svg = `' + svgContent + '`\n';

  fs.writeFileSync(filePath, newContent, 'utf-8');
  console.log(`OK: ${file} (label: ${label}, svg: ${svgContent.length} chars)`);
}

console.log('\nDone!');
