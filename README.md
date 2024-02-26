# tinyurl

**Setup:**

* Requires a MySQL instance to store urls.
* Update [application.properties](src/main/resources/application.properties) with your database's host, username and password.

**Building and deploying locally:**

* gradle build
* gradle bootRun

**Usage:**

* Create a short url: 'curl 127.0.0.1:8080/url -X POST -d '{"longUrl": "example.com"}' -H "Content-Type: application/json"'
* Redirect to a long url: 'curl 127.0.0.1:8080/url/{shortUrl}'
* Delete a short url: 'curl -X DELETE 127.0.0.1:8080/url/{shortUrl}'
* Get short url usage statistics: 'curl 127.0.0.1:8080/urlStats/{shortUrl}'