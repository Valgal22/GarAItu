const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) content = content.slice(1);
const flows = JSON.parse(content);

const funcNode = flows.find(n => n.id === '84c66cb82101c234');
console.log('Function node wires to:', funcNode.wires);

const reqNodeId = funcNode.wires[0][0]; // Assuming first wire
const reqNode = flows.find(n => n.id === reqNodeId);
console.log('HTTP Request node:', reqNode.name, 'wires to:', reqNode.wires);

if (reqNode.wires && reqNode.wires[0]) {
    reqNode.wires[0].forEach(wId => {
        const target = flows.find(n => n.id === wId);
        console.log(`Response mapping target: ${target.type} (${target.name})`);
        if (target.type === 'function') console.log('Response Mapping Code:', target.func);
    });
}
