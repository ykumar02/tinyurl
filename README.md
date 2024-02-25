# tinyurl

**Setup:**

1. Requires a MySQL instance to store urls.
2. Update [application.properties](resources/application.properties) with your database's host, username and password.

**Building and deploying locally:**

1. gradle build
2. gradle bootRun

**Usage:**

1. Create a short url: 'curl 127.0.0.1:8080/url -X POST -d '{"longUrl": "example.com"}' -H "Content-Type: application/json"'
2. Redirect a long url: 'curl 127.0.0.1:8080/url/{shortUrl}'
3. Delete a short url: 'curl -X DELETE 127.0.0.1:8080/url/{shortUrl}'