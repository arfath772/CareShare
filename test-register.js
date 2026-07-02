const https = require('https');

const data = JSON.stringify({
  firstName: 'Mohammed',
  lastName: 'Arfath',
  email: 'mohammedarf46982@gmail.com',
  password: 'TestPass123!',
  confirmPassword: 'TestPass123!'
});

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/register',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
};

const req = https.request(options, res => {
  console.log(`statusCode: ${res.statusCode}`);
  res.on('data', d => process.stdout.write(d));
});

req.on('error', error => {
  console.error(error);
});

req.write(data);
req.end();
