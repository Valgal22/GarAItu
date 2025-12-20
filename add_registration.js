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
            const activeTabId = '4f1c296af823412b';

            // Check if Register In node already exists
            if (flows.find(n => n.name === 'Register Endpoint' && n.z === activeTabId)) {
                console.log('Register Endpoint already exists.');
                return;
            }

            const responseNodeId = '2b841042463b1070'; // Use existing Return Context

            const regIn = {
                "id": "reg_in_node",
                "type": "http in",
                "z": activeTabId,
                "name": "Register Endpoint",
                "url": "/api/auth/register",
                "method": "post",
                "upload": false,
                "skipBodyParsing": false,
                "x": 140,
                "y": 60,
                "wires": [["reg_req_node"]]
            };

            const regReq = {
                "id": "reg_req_node",
                "type": "http request",
                "z": activeTabId,
                "name": "Register (Spring Boot)",
                "method": "POST",
                "ret": "obj",
                "paytoqs": "ignore",
                "url": "http://localhost:8080/garAItu/auth/register",
                "x": 430,
                "y": 60,
                "wires": [[responseNodeId]]
            };

            flows.push(regIn);
            flows.push(regReq);

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
            });
            postReq.write(postData);
            postReq.end();

        } catch (e) { console.error(e); }
    });
});
req.end();
