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
                if (node.id === '059bd6c0830ea0dd') {
                    node.method = 'POST';
                    node.name = 'Invite (Spring Boot) [POST]';
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
                    console.log(`Status Code: ${postRes.statusCode}`);
                    postRes.on('data', (d) => { process.stdout.write(d); });
                });

                postReq.on('error', (e) => { console.error(e); });
                postReq.write(postData);
                postReq.end();
            } else {
                console.log('Node not found');
            }
        } catch (e) {
            console.error('JSON Parse Error:', e.message);
            console.log('Data received:', data.substring(0, 100));
        }
    });
});

req.on('error', (e) => { console.error(e); });
req.end();
