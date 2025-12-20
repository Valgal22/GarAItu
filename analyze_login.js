const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const nodes = flows.filter(n => n.name === 'Login Endpoint');
nodes.forEach(n => {
    const tab = flows.find(t => t.id === n.z);
    console.log(`Login Node ${n.id} is on tab ${n.z} (${tab.label}), disabled: ${tab.disabled}`);
});
