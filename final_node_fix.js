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
            let modified = false;

            flows.forEach(node => {
                // Confirm Invite Endpoint (HTTP In) method is GET
                if (node.id === '307fe2cf325d9def') {
                    node.method = 'get';
                    modified = true;
                }

                // Update Set URL (Function) to fix path, forward headers, and SET METHOD TO POST
                if (node.id === 'a0336666254ecbf2') {
                    node.func = "msg.url = \"http://localhost:8080/garAItu/group/\" + msg.req.params.id + \"/invite\";\nmsg.method = \"POST\";\nmsg.headers = {\n    \"X-Session-Id\": msg.req.headers[\"x-session-id\"]\n};\nreturn msg;";
                    modified = true;
                    console.log('Final Update: Function node logic fixed with Headers and POST method.');
                }

                // Ensure the HTTP request node (Invite Spring Boot) method is POST
                if (node.id === '059bd6c0830ea0dd') {
                    node.method = 'POST';
                    modified = true;
                }
            });

            if (modified) {
                const postData = JSON.stringify(flows);
                const postOptions = {
                    hostname: 'localhost',
                    port: 1880,
                    path: '/flows',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Content-Length': Buffer.byteLength(postData)
                    }
                };

                const postReq = http.request(postOptions, (postRes) => {
                    console.log(`Update Status Code: ${postRes.statusCode}`);
                    postRes.on('data', (d) => { process.stdout.write(d); });
                });

                postReq.on('error', (e) => { console.error('Post Error:', e); });
                postReq.write(postData);
                postReq.end();
            } else {
                console.log('Nodes not found');
            }
        } catch (e) {
            console.error('JSON Parse Error:', e.message);
        }
    });
});

req.on('error', (e) => { console.error('Get Error:', e); });
req.end();
