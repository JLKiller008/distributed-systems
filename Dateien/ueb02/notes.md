To perform an http request to the Server, the following command can be used:

```bash
curl -i http://localhost:45123/
```

Here is an example output from the server

```text
HTTP/1.1 200 OK
Content-Type: text/html; charset=UTF-8
Content-Length: 310
Connection: close

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Distributed System Test Page</title>
</head>
<body>
	<h1>This is a test page for the Server-Client exercise 2 in Distributed Systems!</h1>
</body>
</html>
```

## Client

Start the server first, then compile and run the client from the repository root:

The client makes the same `GET /` request in two ways:

1. `requestWithUrl(new URL(address))` demonstrates `java.net.URL` and
   `HttpURLConnection`.
2. `requestWithHttpClient(address)` accepts the address as a normal `String`
   and uses the modern `java.net.http.HttpClient` API.

An optional first argument can be used to request another HTTP URL:

```bash
java Dateien.ueb02.Client http://localhost:45123/
```
