import json
import uuid
import shutil
import os

def create_header_node(x, y, next_node_id, tab_id, name="Add Headers"):
    return {
        "id": str(uuid.uuid4()).replace('-', '')[:16],
        "type": "function",
        "z": tab_id,
        "name": name,
        "func": 'msg.headers = {\n    "X-Session-Id": msg.req.headers["x-session-id"],\n    "Content-Type": "application/json"\n};\nreturn msg;',
        "outputs": 1,
        "noerr": 0,
        "initialize": "",
        "finalize": "",
        "libs": [],
        "x": x,
        "y": y,
        "wires": [[next_node_id]]
    }

# Try reading as UTF-16 (likely), then UTF-8
filename = 'current_flows.json'
try:
    with open(filename, 'r', encoding='utf-16') as f:
        flows = json.load(f)
except Exception as e:
    print(f"UTF-16 read failed: {e}, trying UTF-8")
    with open(filename, 'r', encoding='utf-8') as f:
        flows = json.load(f)

# Find targets
endpoints = {
    "/api/groups/create": "Add Headers (Create)",
    "/api/groups/join": "Add Headers (Join)"
}

modified = False
node_map = {n['id']: n for n in flows}

# Iterate over a copy to allow appending to flows list safely
for node in list(flows):
    if node.get('type') == 'http in' and node.get('url') in endpoints:
        print(f"Found node: {node.get('name', 'Unnamed')} ({node['id']}) for {node['url']}")
        
        if not node.get('wires') or len(node['wires']) == 0 or len(node['wires'][0]) == 0:
            print("  No wires found, skipping")
            continue
            
        next_node_id = node['wires'][0][0]
        next_node = node_map.get(next_node_id)
        
        if next_node and next_node['type'] == 'function' and "X-Session-Id" in next_node.get('func', ''):
             print(f"  Next node {next_node_id} is already a header injector. Skipping.")
             continue

        # Calculate position for new node (mid-point)
        nx = float(node.get('x', 0))
        ny = float(node.get('y', 0))
        next_x = float(next_node.get('x', nx + 200)) if next_node else nx + 200
        next_y = float(next_node.get('y', ny)) if next_node else ny
        
        new_x = (nx + next_x) / 2
        new_y = (ny + next_y) / 2
        
        # Create new node
        new_node = create_header_node(
            new_x, 
            new_y, 
            next_node_id, 
            node.get('z'), 
            endpoints[node['url']]
        )
        
        # Update wires of the current node to point to the new node
        node['wires'][0][0] = new_node['id']
        
        # Add new node to flows
        flows.append(new_node)
        modified = True
        print(f"  Patched! Added node {new_node['id']} between {node['id']} and {next_node_id}")

if modified:
    # Backup
    if not os.path.exists(filename + '.bak'):
        shutil.copy(filename, filename + '.bak')
        print(f"Backed up to {filename}.bak")
    
    # Save as UTF-8 (Node-RED standard)
    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(flows, f, indent=4)
    print("Saved current_flows.json (UTF-8)")
else:
    print("No changes needed.")
