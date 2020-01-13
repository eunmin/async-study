var net = require('net');

function connect() {
    var client = new net.Socket();
    return new Promise(resolve => {
        client.connect(50007, 'localhost', () => {
            resolve(client);
        });
    });
}

function send(client, id) {
    return new Promise(resolve => {
        var message = 'GET /' + id + '?10 HTTP/1.0\r\n\r\n'
        console.log("send request " + id);
        client.write(message);
        resolve(message);
    });
}

function recv(client) {
    return new Promise(resolve => {
        client.on('data', (data) => {
            resolve(data.toString())
            client.destroy();
        });
    });
}

async function main() {
    const client = await connect();
    await send(client, 2);
    const r = await recv(client);
    console.log(r);
}

main();


