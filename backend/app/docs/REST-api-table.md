| Method | Resource Path or URI | I | S | Description | Query String | Returns | RCs             |
|--------|-----------------------|---|---|-------------|--------------|---------|-----------------|
| GET    | {resource}            | Y | Y | Get an array of resources of the resource type *(recommend a default limit to the number of rows returned that can be overridden with the limit parameter)* | Optional: <br> offset = 0 - n <br> limit = 0 - n <br> columns=[CSV] <br> where=[key]=[CSV] <br> orderby=[CSV] | Array of JSON Objects or, when the columns parameter is present, an array of JSON maps | 200, 204        |
| GET    | {resource}/{ID}       | Y | Y | Get a specific resource matching the ID | Optional: columns=[CSV] | Array of JSON Objects or, when the columns parameter is present, an array of JSON maps | 200, 204        |
| POST   | {resource}            | N | N | Create a new resource of the type | JSON Object | Location URI of new resource in HTTP header | 201             |
| POST   | {resource}/{id}       | * | * | Update a specific resource *(use PUT instead)* |  |  | 405 Not Allowed |
| PUT    | {resource}/{ID}       | Y | N | Replace specific resource with a new copy | JSON Object | Location URI of updated resource in HTTP header | 204             |
| PATCH  | {resource}/{ID}       | Y | N | Update one or more attributes of a resource | JSON Object containing modified or deleted (null) attributes | | 200             |
| DELETE | {resource}/{ID}       | Y | N | Remove a specific resource |  |  | 204             |
| DELETE | {resource}            | Y | N | Remove selected resources of a type *(must be qualified with where)* | Required: where=[key]=[CSV] | 204 (405 if no where) |