# Ickenham

[![Build Status](https://travis-ci.org/enpassant/ickenham.svg?branch=master)](https://travis-ci.org/enpassant/ickenham)
[![Coverage Status](https://img.shields.io/coveralls/enpassant/ickenham.svg)](https://img.shields.io/coveralls/enpassant/ickenham)

Ickenham is fast and concise template system, largely compatible with [Handlebars](https://handlebarsjs.com/) and Mustache templates.

Installation
============

You can add the Ickenham as a dependency in following ways.

You can find available versions [here](https://mvnrepository.com/artifact/com.github.enpassant/ickenham).

### SBT users

```scala
"com.github.enpassant" %% "ickenham" % "1.1.0"
```

### Maven users

```xml
<dependency>
  <groupId>com.github.enpassant</groupId>
  <artifactId>ickenham_${scala.version}</artifactId>
  <version>1.1.0</version>
</dependency>
```

### Benchmarks 2018

Template engines benchmarked with the project [Spring compareing template engine](https://github.com/jreijn/spring-comparing-template-engines/).

In case you want to benchmark the different template engines I would recommend using [wrk](https://github.com/wg/wrk) HTTP benchmarking tool.

```
wrk -c 100 -d 10 -t 10 http://localhost:8080/jsp
```
This runs benchmark for 10 seconds, using 10 threads, and keeping 100 HTTP connections open.

These tests were done on a local machine with the following specs:

```
Ubuntu 16.04 LTS
2,6-3,6 GHz Intel Core i7
Java(TM) SE Runtime Environment (build 1.8.0_91-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.91-b14, mixed mode)
Apache Tomcat 7.0.72 with 512M RAM
```

Results in order (best to worst):
0. _Empty page_ (without any template): **40.000** req/s
1. _Mustache_: **34.500** req/s
2. _Velocity_: **32.000** req/s
3. _Pebble_: **30.000** req/s
4. _Freemarker_: **29.000** req/s
5. _JSP_: **27.000** req/s
6. _**Ickenham**_ (Scala, Handlebars syntax): **26.500** req/s
7. _Jtwig_: **25.000** req/s
8. _Chunk_: **20.500** req/s
9. _Thymeleaf_: **20.500** req/s
10. _Handlebars_: **19.000** req/s
11. _Jade_: **16.000** req/s (150-200 error)
12. _Scalate_ (Scala): **10.000** req/s
