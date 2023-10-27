To generate self-signed certificates

On GIT base
- openssl req -x509 -newkey rsa:4096 -keyout key.key -out cert.pem -days 965 -subj '//CN=localhost' -nodes

On Linux
- openssl req -x509 -newkey rsa:4096 -keyout key.key -out cert.pem -days 965 -subj '/CN=localhost' -nodes