Akka Remote Example in Java
=========================

## Introduction
This project was made while testing the Akka Remoting features.
It contains five modules; a SmsApi, a SmsValidator, a SmsDao and a SmsHttp. 

##### Shared
The has the Messages that are shared between all the Actors. 

##### SmsApi
The SmsApi is a actor which is used to pump Sms Messages from while loop. 

##### SmsDao
The SmsDao writes the Sms into DB

##### SmsValidator
The SmsValidator is used to perform basic validation, DNC and CC.

##### SmsHttp
The SmsHttp is to send POST & GET requests via HTTP.

##### SmsGrpc
The SmsGrpc is to simulate gRPC client and Server for sending SMS


## How to run
You can run the program like every ordinary Java main program. Make sure you have `mvn clean install`ed the project before to get the Akka dependency.
It's important to run the projects in the following order:

1. SmsDao
2. SmsValidator
3. SmsHttp
4. SmsGrpc
5. SmsApi
