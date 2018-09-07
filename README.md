Tili-tili
=========

URL shortener

Time spent: ~ 3 hours

How to run:
--
* install local postgres server with user `postgres` and empty password. Create database 'tili'
* run `./gradlew run`

How to test
--
First check [Swagger API](/api/swagger.yaml)

Shorten url:
`curl -X POST http://localhost:8080/short-urls -d "{\"url\":\"http://ya.ru\"}"`

Retrieve all urls:
`curl http://localhost:8080/short-urls` 

Thought process:
--
1. MVP: postgres + jvm backend
It's undesirable for id to be just sequential numbers (easy to guess) therefore we need better schema.
Would like to avoid coordination, perhaps 8 random symbols will be enough
In the real world I'd rather store last N shortened urls in cookies, and I don't see use case for admin page
(how would you present millions of links on a page? For what reason?)

2. Use cassandra or other KV storage. Go for eventual consistency when adding new link to account's link list
(or even use Foundation DB to have real transaction - but that's not really needed).

3. Implement whole thing using AWS: api gateway + lambda + dynamodb + S3 (for serving static part of front-end).

4. Idea get stable id by calculating it as a hash("user id" + "url string") to 5 bytes (and then do url-safe Base64 encode to 8 characters).
Insert on conflict: overwrite

Design choices:
--
* I chose Kotlin because this is modern java replacement.

* I chose **vertx** over **Spring Boot** because vertx feels more like a library. 
I can just use it as http server and have my own DI or config or persistence. 
The downside is more code that I have to write. This project is too small to see this benefit, 
but in real world it totally pays off. Also vertx is [quite performant](https://www.techempower.com/benchmarks/#section=data-r16&hw=ph&test=query&f=zijunz-zik0zj-zik0zj-zik0zj-zik0zj-zijbpb-zik0zj-4zsov).

* I chose to use coroutines because they allow to write async code that looks like sequential code.
I understand that this rises the bar of complexity and recognise necessity of providing proper trainings for all contributors of such projects.   

* I chose not to use DI-framework, mocking library, ORM. In my experience those things substantially
complicate troubleshooting. For example if I forget to declare some dependecy my app will fail at compile time 
instead of runtime.    
 
Limitations:
--
* No front end 
* No registration/authorization
* No endpoint for admin
* Tests for ShortenedUrlsHandler and ShortenedUrlDao are missing
(I prefer to have dao test with real postgres through test containers)
* no config - all params are hardcoded (I prefer to use [typesafe config](https://github.com/lightbend/config))
* No docker compose with database included (to ease local env setup)
