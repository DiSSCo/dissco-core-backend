[![DOI](https://zenodo.org/badge/494444176.svg)](https://zenodo.org/badge/latestdoi/494444176)

# Backend DiSSCo
The backend provides the backend API's for the frontend.
It handles the logic and preprocesses the items for frontend optimization.
It consists of a set of REST API's which return JSON. 
Find/Search API's will be open, API's for create/update/delete will be protected by Oauth (Keycloak).
Data is retrieved from the `dissco` database and elasticSearch.

## API's
The API's are document with OpenAPI v3 and a Swagger endpoint.

## Properties

### Database
- `spring.datasource.url` - The JDBC url of the databse
- `spring.datasource.username` - Username of the databse user with suffecient rights
- `spring.datasource.password` - Password for the user

### ElasticSearch
- `elasticsearch.hostname` - Hostname of the elasticsearch instance
- `elasticsearch.port` - Portnumber of the elasticsearch instance
