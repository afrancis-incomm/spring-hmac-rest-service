# spring-hmac-rest-service
An example of Spring REST endpoints secured by HMAC header authentication

This example application is based on the implementation of HMAC REST security as mentioned in this repository
https://github.com/kpavlov/spring-hmac-rest

Download the project and run it as a spring boot application.
To see the API calls in action, use the ApiClient class and run the main method when the Spring boot app is running.

TO DO
------
1) Right now, the client's username and password are hardcoded and set in memory. For a PROD application, we need to have a table where the client's user id and password is stored. Also, we need to have a service exposed which will allow us to add more clients for our authenticated rest services.
