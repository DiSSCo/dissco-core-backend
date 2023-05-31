[![DOI](https://zenodo.org/badge/494444176.svg)](https://zenodo.org/badge/latestdoi/494444176)

# Backend DiSSCo
The backend provides the backend APIs for the frontend.
It handles the logic and preprocesses the items for frontend optimization.
In general the find/search APIs are open and APIs for create/update/delete are protected by Oauth (Keycloak).

All endpoints are based on the [JSON:API](https://jsonapi.org/) specification.
It follows the guidelines and best practices described in [BiCIKL Deliverable 1.3](https://docs.google.com/document/d/1RgngKSPabEs-Pir6vA25iFDgVorbEZe7duT7L7vQ7QI)

The backend provides APIs for the following objects:
- Digital Specimens
- Digital Media Objects
- Annotations
- Users
- Organisations

## Storage solutions
In general there are three places where data on these objects are stored:
- Postgres database
The Postgres database stores the latest active version of the object.
It is used in retrieving a specific object based on the id.
It can also be used to combine objects based on their relationships.

- Elasticsearch
This data storage is used for searching and aggregating.
It is used for data discovery and provides endpoints for the legend and the search fields.
In general, it does not return a single object, but a paginated list of objects.
Additionally, it can provide aggregations showing how many items comply to the search criteria.

- MongoDB
MongoDB is used for provenance storage and stores the historical data.
This data storage is used for displaying previous versions of the data.
To display older versions of the data we rely on this type of storage.

## API documentation
The APIs are documented through OpenAPI v3 and Swagger.
The swagger endpoint is: https://sandbox.dissco.tech/api/swagger-ui/index.html#/
The OpenAPI endpoint is: https://sandbox.dissco.tech/api/v3/api-docs

As we use generic objects, the API documentation is not as detailed as we would like.
We are looking at additional options for more detailed documentation.

### Digital Specimens
For Digital Specimen, we only provide search/read functionality through the APIs.
As the other endpoints, it is following a generic structure.
`{endpoint}/{prefix}/{suffix}`
Optionally, this is followed by a version `/{version}` for a specific version.
It can also be followed by a particular view on the data such as `/full`
The full endpoint provide all specimen information including all connected information.
This means all annotations, all connected digital media objects and all annotations on the connected objects.
We also provide a JSON-LD view on the data on the `/jsonld` endpoint.
This endpoint does not comply the JSON:API standard, but follows the JSON-LD implementation.

Additionally, there are several aggregation and search endpoints.
In general the can by filtered by using the following structure.
`/search?country=Netherlands&midsLevel=1&country=Finland`
Multiple key=value pairs can be used.
When one key should have multiple values, the same key can be repeated.
This gives us a generic way to filter searches and aggregations.
For all terms on which can be filtered see [this class](./src/main/java/eu/dissco/backend/domain/MappingTerms.java).
This class provides a list of terms with their simplified name (used in the endpoint) and their full name (used in Elasticsearch).

### Digital Media Objects
For Digital Media Objects, we provide read functionality through the APIs.
Just as the Digital Specimen endpoints it follows the structure.
`endpoint/prefix/suffix`
Optionally, this is followed by a version `/{version}` for a specific version.
There are limited set of endpoints as most search will happen through the digital specimen.

### Annotations
For annotations,k we provide read/create/update and tombstone functionality through the APIs.
Just as the Digital Specimen endpoints it follows the structure.
`endpoint/prefix/suffix`
Optionally, this is followed by a version `/{version}` for a specific version.
There are limited set of endpoints as most search will happen through the digital specimen.

The create/update and tombstone endpoints are protected through authentication.
The actions posted to these endpoints are forwarded to the annotation processor.
This processor manages all the annotations and is the only application authorize to create or change annotations.

### User
For users, we provide a set of read/create/update/delete functionality through the APIs.
When a user is registered it will be created in the database.
A user can update his or her information through the profile page.
The backend will then update the user information in the database.

### Organisation
The organisation endpoints provide read functionality for the organisations.
These organisations are inserted in the database and need to be updated manually.
The organisation endpoints can be used by forms and user queries.

### Organisation document
The organisation documents can be used to insert documents for a particular organisation.
These endpoints can be used to insert the result of forms and user queries.

## Run locally
To run the system locally, it can be run from an IDEA.
Clone the code and fill in the application properties (see below).
The application needs a connection to a Postgres database, MongoDB and Elasticsearch.
For creation and modification of annotations it needs a reachable annotation processor service.

## Run as Container
The application can also be run as container.
It will require the environmental values described below.
The container can be built with the Dockerfile, which can be found in the root of the project.

## Environmental variables
The following backend specific properties can be configured:

```
# Database properties
spring.datasource.url=# The JDBC url to the PostgreSQL database to connect with
spring.datasource.username=# The login username to use for connecting with the database
spring.datasource.password=# The login password to use for connecting with the database

#Elasticsearch properties
elasticsearch.hostname=# The hostname of the Elasticsearch cluster
elasticsearch.port=# The port of the Elasticsearch cluster

#Oauth properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=# The URI to the JWT issuer
spring.security.oauth2.authorizationserver.endpoint.jwk-set-uri=# The URI to the JWT OpenId certifications 

#MongoDB properties
mongo.connection-string=# Connection string to MongoDB
mongo.database=# Database name of MongoDB

#Feign clients
feign.annotations=# Path to annotation proccessor endpoint