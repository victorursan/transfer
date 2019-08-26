# Transfer

## Challenge

Design and implement a RESTful API for money transfers between accounts.
## Requirements
```
java: 12
gradle: 5.6
```
## Run
```
> gradle run
```
### Add client

* Request:

```
curl --request POST \
  --url http://localhost:8080/clients \
  --header 'content-type: application/json' \
  --data '{
	"fullName": "John Doe",
	"balance": 10
    }'
```
* Response:

```
{
  "id": 1,
  "name": "John Doe",
  "balance": 10
}
```

### Get Client

Request:

```
curl --request GET \
  --url http://localhost:8080/clients/1
```

Response:

```
{
  "id": 1,
  "name": "John Doe",
  "balance": 10
}
```


### Add Transfer

* Request:

```
curl --request POST \
  --url http://localhost:8080/transfers \
  --header 'content-type: application/json' \
  --data '{
	"fromId": 1,
	"toId": 2,
	"amount": 10
}'
```

* Response:

```
{
  "id": 1,
  "fromId": 1,
  "toId": 2,
  "amount": 10
}
```
