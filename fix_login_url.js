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
                // Correct Login (Spring Boot) node URL in the active tab (id: 4318bf79ded9b079)
                // Or find it by name if on the active tab
                if (node.name === 'Login (Spring Boot)' && node.z === '4f1c296af823412b') {
                    node.url = "http://localhost:8080/garAItu/auth/login";
                    modified = true;
                    console.log('Fixed Login URL to: ' + node.url);
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
                });

                postReq.on('error', (e) => { console.error('Post Error:', e); });
                postReq.write(postData);
                postReq.end();
            } else {
                console.log('Login node not found on active tab');
            }
        } catch (e) {
            console.error('JSON Parse Error:', e.message);
        }
    });
});

req.on('error', (e) => { console.error('Get Error:', e); });
req.end();
