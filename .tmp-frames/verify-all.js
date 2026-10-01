const fs = require('fs');
const path = require('path');

const dir = 'F:/project/idea/self/yanggu-code-generator/yanggu-code-generator-frontend/src/icons/iconfont/modules';
const files = fs.readdirSync(dir).filter(f => f.endsWith('.ts') && f !== 'index.ts' && f !== 'inject-svg.ts');

let allOk = true;
for (const file of files) {
  const content = fs.readFileSync(path.join(dir, file), 'utf-8');
  const hasKey = content.includes("export const key = ");
  const hasLabel = content.includes("export const label = ");
  const hasSvg = content.includes("export const svg = `");
  const noInject = !content.includes("import { injectSvg }");
  const noCall = !content.includes("injectSvg(");
  
  if (hasKey && hasLabel && hasSvg && noInject && noCall) {
    // Extract key and label for display
    const keyMatch = content.match(/export const key = '(.+)'/);
    const labelMatch = content.match(/export const label = '(.+)'/);
    console.log(`OK: ${file.padEnd(20)} key=${keyMatch?.[1]?.padEnd(15)} label=${labelMatch?.[1]}`);
  } else {
    console.log(`FAIL: ${file} key=${hasKey} label=${hasLabel} svg=${hasSvg} noInject=${noInject} noCall=${noCall}`);
    allOk = false;
  }
}

console.log(`\n${allOk ? 'ALL PASSED' : 'SOME FAILED'}`);
