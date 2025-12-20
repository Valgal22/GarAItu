const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const activeTabId = '4f1c296af823412b';
const nodes = flows.filter(n => n.z === activeTabId && n.type === 'http request');
nodes.forEach(n => {
    console.log(`Node: ${n.name}, URL: ${n.url}`);
});
