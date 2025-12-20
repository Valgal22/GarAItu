const fs = require('fs');
let content = fs.readFileSync('current_flows.json', 'utf16le');
if (content.charCodeAt(0) === 0xFEFF) {
    content = content.slice(1);
}
const flows = JSON.parse(content);

const tabs = flows.filter(n => n.type === 'tab');
tabs.forEach(t => console.log(`Tab ${t.id} (${t.label}), disabled: ${t.disabled}`));

const inviteNodes = flows.filter(n => n.name === 'Invite Endpoint');
inviteNodes.forEach(n => {
    const tab = flows.find(t => t.id === n.z);
    console.log(`Invite Node ${n.id} is on tab ${n.z}, tab disabled: ${tab ? tab.disabled : 'unknown'}`);
});
