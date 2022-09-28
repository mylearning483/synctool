# Notes for using the Purview Catalog Service Postman Collection

In order to use this Postman collection, create a Postman environment called _Purview_ and set it to be the current environment.

Populate it with the following variables:

1. basePurviewUrl = https://MyLearning483.purview.azure.com
1. baseUrl = {{basePurviewUrl}}/catalog/api

Before you can call any API, you will need to fetch an access_token. To do this, first run the _*Get Access Token*_ call. If successful, this call will populate a variable called *access_token* in the _Purview_ environment. The other API calls will then use this token for authentication.
