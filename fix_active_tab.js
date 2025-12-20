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
            const activeTabId = '4f1c296af823412b';

            flows.forEach(node => {
                if (node.z === activeTabId) {
                    // 1. Invite HTTP In
                    if (node.name === 'Invite Endpoint' && node.type === 'http in') {
                        node.method = 'get';
                        modified = true;
                        console.log('Fixed Active Invite Inbound to GET');
                    }
                    // 2. Set URL Function
                    if (node.name === 'Set URL & Headers' || node.name === 'Set URL') {
                        node.func = "msg.url = \"http://localhost:8080/garAItu/group/\" + msg.req.params.id + \"/invite\";\nmsg.method = \"POST\";\nmsg.headers = {\n    \"X-Session-Id\": msg.req.headers[\"x-session-id\"]\n};\nreturn msg;";
                        modified = true;
                        console.log('Fixed Active Function logic (Path + POST + Headers)');
                    }
                    // 3. HTTP Request
                    if (node.name === 'Invite (Spring Boot)') {
                        node.method = 'POST';
                        modified = true;
                        console.log('Fixed Active HTTP Request to POST');
                    }
                }
            });

            if (modified) {
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
            }
        } catch (e) { console.error(e); }
    });
});
req.end();
