const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const node = flows.find(n => n.id === '427255cffc287868');
const reqNodeId = node.wires[0][0];
const reqNode = flows.find(n => n.id === reqNodeId);
console.log('Login Proxies to:', reqNode.url);
