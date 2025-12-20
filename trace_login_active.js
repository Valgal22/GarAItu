const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const node = flows.find(n => n.id === '427255cffc287868');
console.log('Login Node:', JSON.stringify(node, null, 2));

const wires = node.wires[0];
wires.forEach(wId => {
    const target = flows.find(n => n.id === wId);
    console.log(`Target ${wId}: ${target.type} (${target.name})`);
    if (target.type === 'http request') console.log('HTTP Request URL:', target.url);
});
