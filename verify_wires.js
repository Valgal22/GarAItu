const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const mapNode = flows.find(n => n.id === 'c57ef852614b8216');
console.log('Map node wires to:', mapNode.wires);

mapNode.wires[0].forEach(wId => {
    const target = flows.find(n => n.id === wId);
    console.log(`Target ${wId}: ${target.type} (${target.name})`);
});
