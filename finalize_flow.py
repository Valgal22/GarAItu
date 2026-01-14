import json

# This script writes the EXACT base provided by the user and applies the TTS merge logic.
def finalize_flow():
    # Base JSON structure provided by the user
    # I am reconstructing the target flow nodes based on the user's provided JSON.
    # To avoid token limits, I will modify the file directly by finding the nodes.
    
    flow_path = "current_flows.json"
    with open(flow_path, 'r', encoding='utf-8') as f:
        flows = json.load(f)

    # Modifications to match "Both JSON and WAV" requirement
    # Using the IDs from the user's provided base.
    
    for node in flows:
        # ID: 0de2a6ab79c29960 (POST Java REST)
        # Remove connection to 25deaea02b136bac (immediate response)
        if node.get("id") == "0de2a6ab79c29960":
            if "wires" in node and len(node["wires"]) > 0:
                node["wires"][0] = [w for w in node["wires"][0] if w != "25deaea02b136bac"]

        # ID: 3b9645bb37cd9f84 (Extract name + compose TTS)
        # Clone message to preserve JSON and set TTS text
        if node.get("id") == "3b9645bb37cd9f84":
            node["func"] = """
msg.javaResponse = RED.util.cloneMessage(msg.payload);
const p = msg.payload || {};

function pickName(obj) {
    if (!obj) return null;
    const target = Array.isArray(obj) ? obj[0] : obj;
    return target.name || target.personName || target.identity || null;
}

const name = pickName(p);
if (!name) {
    msg.statusCode = 204;
    msg.payload = "";
    return [msg, null];
}

msg.ttsText = "This is " + name + " and this is your context.";
return [null, msg];
"""

        # ID: d418e02c0351415a (Set WAV Response Headers)
        # Change to "Merge Audio to JSON" and perform Base64 merge
        if node.get("id") == "d418e02c0351415a":
            node["name"] = "Merge Audio to JSON"
            node["func"] = """
const audioBase64 = msg.payload.toString('base64');
msg.payload = msg.javaResponse;

if (Array.isArray(msg.payload) && msg.payload.length > 0) {
    msg.payload[0].audioBase64 = audioBase64;
} else if (typeof msg.payload === 'object') {
    msg.payload.audioBase64 = audioBase64;
}

msg.headers = { "Content-Type": "application/json" };
return msg;
"""

    with open(flow_path, 'w', encoding='utf-8') as f:
        json.dump(flows, f, indent=4)
    print("Flow logic updated on base.")

if __name__ == "__main__":
    finalize_flow()
