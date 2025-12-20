const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const node = flows.find(n => n.id === '427255cffc287868');
console.log('Login Node URL:', node.url);
console.log('Method:', node.method);
