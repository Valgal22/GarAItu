const http = require('http');

const options = {
    hostname: 'localhost',
    port: 1880,
    path: '/flows',
    method: 'GET'
};

const req = http.request(options, (res) => {
    let data = '';
    res.on('data', (chunk) => { data += chunk; });
    res.on('end', () => {
        try {
            let flows = JSON.parse(data);
            const activeTabId = '4f1c296af823412b'; // The tab we are working on

            // We need to create new nodes for Flask integration
            // WE WILL REPLACE existing /api/recognize and /api/upload flows or modify them.

            // 1. Define Nodes IDs (Generating random-ish IDs)
            const id_flask_req_upload = "flask_req_upload_" + Date.now();
            const id_spring_req_memory = "spring_req_memory_" + Date.now();
            const id_func_prep_upload = "func_prep_upload_" + Date.now();
            const id_func_prep_memory = "func_prep_memory_" + Date.now();

            const id_flask_req_recog = "flask_req_recog_" + Date.now();
            const id_spring_req_recog = "spring_req_recog_" + Date.now();
            const id_func_prep_recog_flask = "func_prep_rec_flask_" + Date.now(); // prepare flask
            const id_func_prep_recog_sb = "func_prep_rec_sb_" + Date.now(); // prepare SB

            // Helper to find node by endpoint
            const findHttpIn = (url) => flows.find(n => n.type === 'http in' && n.z === activeTabId && n.url === url);

            // --- Modify /api/upload (Add Memory) ---
            const uploadNode = findHttpIn('/api/upload');
            if (uploadNode) {
                // Remove old wires
                // We will rewrite the chain: UploadNode -> Func(PrepFlask) -> FlaskReq -> Func(PrepSB) -> SBReq -> HttpResponse
                // Wait, UploadNode gives us req.files or req.body?
                // GenericNetworkService sends multipart. Node-RED 'http in' with 'multipart' enabled parses it.
                // But we usually need 'http in' to NOT parse if we want to forward raw, OR parse and re-form.
                // Assuming standard Node-RED behavior where we get msg.req.files.

                // Let's create the chain.
                // 1. Func Prep Flask: Takes msg.req.files[0] (the image) and sends to Flask.
                // But wait, Flask needs multipart too?
                // To keep it simple, we assume Node-RED passes the body to Flask Request. 
                // We will point UploadNode -> FlaskReq directly? No, Flask might need specific format.
                // Let's assume Flask accepts the exact same Multipart body.

                // Actually, orchestrating Multipart in Node-RED is hard with standard nodes.
                // Strategy: We will direct the UploadNode to a Function that sets url to Flask, 
                // then Http Request to Flask.
                // Flask returns JSON { "embedding": ... }.
                // Then Function constructs JSON for Spring Boot { "name": ..., "embedding": ... }.
                // Then Http Request to Spring Boot.

                // FLASK REQUEST NODE
                const flaskUploadNode = {
                    id: id_flask_req_upload,
                    type: "http request",
                    z: activeTabId,
                    name: "Flask Embed",
                    method: "POST",
                    ret: "obj",
                    paytoqs: "ignore",
                    url: "http://localhost:5000/embed", // FLASK URL
                    tls: "",
                    persist: false,
                    proxy: "",
                    authType: "",
                    x: uploadNode.x + 200,
                    y: uploadNode.y,
                    wires: [[id_func_prep_memory]]
                };

                // PREP MEMORY NODE (Function)
                // We need to preserve the original fields (name, context) from the initial request!
                // Problem: Http Request to Flask will overwrite msg.payload and msg.req (maybe?).
                // msg.req usually persists or we should save it.
                const funcPrepMemory = {
                    id: id_func_prep_memory,
                    type: "function",
                    z: activeTabId,
                    name: "Prep Memory",
                    func: `
// Msg.payload is now Flask response: { embedding: [...] }
var emb = msg.payload.embeddingBase64 || msg.payload.embedding; 
// Check how Flask returns it. Assuming JSON with embeddingBase64 field for simplicity matching Java.
// If Flask returns valid Java-compatible Base64, good.
// The original request fields are in msg.req.body (if multipart parsed)

var name = msg.req.body.name;
var context = msg.req.body.context || msg.req.body.relationship; 

msg.payload = {
    "name": name,
    "context": context,
    "embeddingBase64": emb
};
msg.url = "http://localhost:8080/garAItu/group/memory";
return msg;
`,
                    outputs: 1,
                    noerr: 0,
                    initialize: "",
                    finalize: "",
                    libs: [],
                    x: uploadNode.x + 400,
                    y: uploadNode.y,
                    wires: [[id_spring_req_memory]]
                };

                // SPRING MEMORY REQ NODE
                const springMemoryNode = {
                    id: id_spring_req_memory,
                    type: "http request",
                    z: activeTabId,
                    name: "Save Memory",
                    method: "POST",
                    ret: "obj",
                    paytoqs: "ignore",
                    url: "", // Set by function
                    tls: "",
                    persist: false,
                    proxy: "",
                    authType: "",
                    x: uploadNode.x + 600,
                    y: uploadNode.y,
                    wires: [[uploadNode.wires[0][0]]] // Connect to the original Http Response (assuming it's the first wire of original upload node)
                    // Wait, original upload node points to "Upload (Spring Boot)". "Upload (Spring Boot)" points to "Http Response".
                    // We want to connect to "Http Response". We need to find it.
                };

                // Find the response node. 
                // The original wire of uploadNode goes to request node. The request node goes to response node.
                let originalReqId = uploadNode.wires[0][0];
                let originalReq = flows.find(n => n.id === originalReqId);
                let responseId = originalReq ? originalReq.wires[0][0] : null; // Assuming simple chain

                if (responseId) {
                    springMemoryNode.wires = [[responseId]];
                    // Now Update Upload Node wires to point to Flask
                    uploadNode.wires = [[id_flask_req_upload]];

                    flows.push(flaskUploadNode);
                    flows.push(funcPrepMemory);
                    flows.push(springMemoryNode);
                }
            }

            // --- Modify /api/groups/:id/recognize (Recognize) ---
            // Current node url might be /api/recognize. The User updated App to /api/groups/:id/recognize.
            // We need a NEW Http In node or update existing.
            // Let's UPDATE the existing 'Identify Face' node if exists, or Find one with /api/recognize.

            let recogNode = findHttpIn('/api/recognize');
            if (recogNode) {
                recogNode.url = '/api/groups/:id/recognize'; // Update URL pattern

                // FLASK RECOG NODE
                const flaskRecogNode = {
                    id: id_flask_req_recog,
                    type: "http request",
                    z: activeTabId,
                    name: "Flask Embed Recog",
                    method: "POST",
                    ret: "obj",
                    paytoqs: "ignore",
                    url: "http://localhost:5000/embed",
                    tls: "",
                    persist: false,
                    proxy: "",
                    authType: "",
                    x: recogNode.x + 200,
                    y: recogNode.y,
                    wires: [[id_func_prep_recog_sb]]
                };

                // PREP SB RECOG NODE
                const funcPrepSbRecog = {
                    id: id_func_prep_recog_sb,
                    type: "function",
                    z: activeTabId,
                    name: "Prep SB Recog",
                    func: `
var emb = msg.payload.embeddingBase64 || msg.payload.embedding; 
var groupId = msg.req.params.id;

msg.payload = {
    "embeddingBase64": emb,
    "minSim": 0.0,
    "top": 5
};
msg.url = "http://localhost:8080/garAItu/group/" + groupId + "/recognize";
return msg;
`,
                    outputs: 1,
                    noerr: 0,
                    initialize: "",
                    finalize: "",
                    libs: [],
                    x: recogNode.x + 400,
                    y: recogNode.y,
                    wires: [[id_spring_req_recog]]
                };

                // SPRING RECOG REQ NODE
                const springRecogNode = {
                    id: id_spring_req_recog,
                    type: "http request",
                    z: activeTabId,
                    name: "Recog (Spring)",
                    method: "POST",
                    ret: "obj",
                    paytoqs: "ignore",
                    url: "",
                    tls: "",
                    persist: false,
                    proxy: "",
                    authType: "",
                    x: recogNode.x + 600,
                    y: recogNode.y,
                    wires: [] // Need to find response node
                };

                let originalReqId = recogNode.wires[0][0];
                let originalReq = flows.find(n => n.id === originalReqId);
                let responseId = originalReq ? originalReq.wires[0][0] : null;

                if (responseId) {
                    springRecogNode.wires = [[responseId]];
                    recogNode.wires = [[id_flask_req_recog]];

                    flows.push(flaskRecogNode);
                    flows.push(funcPrepSbRecog);
                    flows.push(springRecogNode);
                }
            }

            // Send update
            const postData = JSON.stringify(flows);
            const postOptions = {
                hostname: 'localhost', port: 1880, path: '/flows', method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(postData) }
            };
            const postReq = http.request(postOptions, (postRes) => {
                console.log(`Update Status Code: ${postRes.statusCode}`);
            });
            postReq.write(postData);
            postReq.end();

        } catch (e) { console.error(e); }
    });
});
req.end();
