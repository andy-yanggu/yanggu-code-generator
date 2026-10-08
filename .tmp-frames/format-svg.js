const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';
const files = fs.readdirSync(dir).filter(f => f.endsWith('.ts') && f !== 'index.ts' && f !== 'inject-svg.ts');

console.log(`Found ${files.length} files to format\n`);

for (const file of files) {
  const filePath = path.join(dir, file);
  let content = fs.readFileSync(filePath, 'utf-8');

  // Normalize line endings to LF
  content = content.replace(/\r\n/g, '\n');

  // Extract the SVG template literal content (between backticks)
  const svgStart = content.indexOf('export const svg = `');
  if (svgStart === -1) {
    console.log(`SKIP: ${file} (no svg template literal)`);
    continue;
  }

  const contentStart = svgStart + 'export const svg = `'.length;
  const contentEnd = content.lastIndexOf('`');

  if (contentEnd <= contentStart) {
    console.log(`SKIP: ${file} (malformed template literal)`);
    continue;
  }

  let svgContent = content.substring(contentStart, contentEnd);

  // Format: put each <symbol on its own line
  // Split before each <symbol (but not the first one which is right after <svg>)
  svgContent = svgContent.replace(/<symbol/g, '\n<symbol');

  // Also add a newline before </svg>
  svgContent = svgContent.replace(/<\/svg>/, '\n</svg>');

  // Rebuild: everything before svg template + formatted svg
  const before = content.substring(0, contentStart);
  const newContent = before + svgContent + '`\n';

  fs.writeFileSync(filePath, newContent, 'utf-8');

  const symbolCount = (svgContent.match(/<symbol/g) || []).length;
  console.log(`OK: ${file} (${symbolCount} symbols formatted)`);
}

console.log('\nDone!');
