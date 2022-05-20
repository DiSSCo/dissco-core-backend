# Backend DiSSCo
The backend provides the backend API's for the frontend.
It handles the logic and preprocesses the items for frontend optimization.
It consists of a set of REST API's which return JSON. 
Find/Search API's will be open, API's for create/update/delete will be protected by Oauth (Keycloak).
At the moment we use the Cordra SDK Java Client to connect with Cordra.

## API's
The API's are document with OpenAPI v3 and a Swagger endpoint.

## Properties

### Cordra
- `cordra.host` - The address where we can find cordra
- `cordra.username` - The username to login
- `cordra.password` - The password to login
- `cordra.type` - Used in the / call to only retrieve relevant specimen of the same type
