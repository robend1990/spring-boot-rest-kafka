# Documentation

## How to run

- To run tests and build docker image execute command `./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=user_service`
- Go to `docker-compose` directory and execute `docker-compose up` command

This will run following docker containers:
- user_service: Spring Boot application exposed on port 8080 that serves REST API with CRUD operations for users management
- db: postgres database that is used by `user_service` to store information about users
- kafka: Single node kafka cluster that is used by `user_service` to sends domain events
- kafdrop: UI Tool exposed on port 9000 to easily view events stored in kafka topic

## User Service API

### Create User

`POST http://localhost:8080/users`

Accepts `application/json` compliant with following schema:

```json
{
  "first_name": "Alice",
  "last_name": "Doe",
  "nickname": "AD",
  "password": "passwd",
  "email": "alice@gmail.com",
  "country": "UK"
}
```
As part of validation it is not possible to create a user with email that is already occupied by another user.
Also `first_name, last_name, password and email` cannot be blank. Email also need to be valid.
Password is never returned back to a user through REST API and is hashed before storing it in database.
In database there is also information when a user was created and when it was updated. This is also not returned through API.
When User is created then this fact is registered as event in `users` kafka topic. Other Services can subscribe to that topic if they want to be notified about that fact.

In case of successfully processed request `201` status code is returned with `Location` header and `application/json` representing a user:

```json
{
  "id": "a59bb1a0-8296-4849-a6a0-cb024fda709b",
  "first_name": "Alice",
  "last_name": "Doe",
  "nickname": "AD",
  "email": "alice@gmail.com",
  "country": "UK"
}
```
In case of failure (for example validation issues) `4xx` status code is returned with `application/json` that contains details about the error. For example:

```json
[
    {
        "message": "must be a well-formed email address",
        "fieldName": "email"
    },
    {
        "message": "must not be blank",
        "fieldName": "password"
    }
]
```

### Update user

`PUT http://localhost:8080/users/{userID}`

Accepts `application/json` compliant with the same schema as in `POST` request.
Validation rules are also the same as in POST

**User is updated in its entirety!**  
In case of successfully processed request `204` status code is returned

In case of failure (for example validation issues, not found user) `4xx` status code is returned with `application/json` that contains details about the error. For example:

```json
{
  "message": "No user with id a59bb1a0-8296-4849-a6a0-cb024fda709c found"
}
```

### Delete user

`DELETE http://localhost:8080/users/{userID}`

If User with given id was found then it is removed from database and an event representing that fact is send to kafka `users` topic

In case of successfully processed request `204` status code is returned.

### Search Users

`GET http://localhost:8080/users{OptionalQueryParameters}`

Returns paginated list of users as `application/json`,  for example:

```json
{
    "content": [
        {
            "id": "efe9d692-1526-4b77-b42b-d4f45fe19e73",
            "first_name": "Alice",
            "last_name": "Bob",
            "nickname": "AB123",
            "email": "alice@gmail.com",
            "country": "UK"
        },
        {
            "id": "226d22a5-7c4c-48d2-9415-6496cef5f2bb",
            "first_name": "John",
            "last_name": "Bob",
            "nickname": "JB123",
            "email": "john@gmail.com",
            "country": "UK"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "pageNumber": 0,
        "pageSize": 20,
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 2,
    "last": true,
    "first": true,
    "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
    },
    "numberOfElements": 2,
    "size": 20,
    "number": 0,
    "empty": false
}
```

Supported query params:
- size: Defines how many items should be shown per page (defaults to 20)
- page: Defines page number, starting from 0 (defaults to 0)
- first_name: Search users by firstname
- last_name: Search users by lastname
- email: Search users by email
- country: Search users by country

**All Query params can be used in any combination**

##Kafdrop

In order to view domain events go to `http://localhost:9000`.
In `Topics` view click on `users` -> `view messages` -> `view messages`. 
Example view:
![Alt text](kafdrop.png?raw=true "Kafdrop")