Vert.x Proxy Example
====================

## Prerequisistes
* Maven >= 3.0
* Java >= 1.8
* Current SNAPSHOT of Vert.x 'master' branch for core/apex/codetrans

## Running

```bash
mvn clean package exec:java
```


## Testing

Once running, this application listens on port 8080 and proxies requests to www.reddit.com

```bash
curl http://localhost:8080/r/java
```
