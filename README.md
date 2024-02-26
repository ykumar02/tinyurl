# tinyurl

**Setup:**

* Requires a MySQL instance to store urls and usage statistics.
* Update [application.properties](src/main/resources/application.properties) with your database's host, username and password.

**Building and deploying locally:**

* `gradle build`
* `gradle bootRun`

**Usage:**

| Command                        | Example                                                                                               |
|--------------------------------|-------------------------------------------------------------------------------------------------------|
| Create a short url             | `curl 127.0.0.1:8080/url -X POST -d '{"longUrl": "example.com"}' -H "Content-Type: application/json"` |
| Redirect to a long url         | `curl 127.0.0.1:8080/url/example`                                                                     |
| Delete a short url             | `curl -X DELETE 127.0.0.1:8080/url/example`                                                           |
| Get short url usage statistics | `curl 127.0.0.1:8080/urlStats/example`                                                                |

**Test Coverage:**

* Build report: `gradle jacocoTestReport`
* Open report: `open build/reports/jacoco/test/html/tinyurl/index.html`
* Coverage report:
  ![test-coverage-report.png](src%2Ftest%2Fresources%2Ftest-coverage-report.png)
