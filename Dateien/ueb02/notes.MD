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

