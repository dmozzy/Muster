

Muster - Herding your Java AWS Lambda functions
==========================================

Overview
--------
Muster is a proof of concept framework that attempts to simplify AWS Lambda development and deployment in Java. It aims to overcome some of the issues with using AWS Lambda instead of a traditional application server. Muster hasn't been used in a production system.

Features
--------
 - Multiple services/methods inside a single deployed Lambda function,
 - Built in idempotency,
 - Type safety across remote Lambda calls,
 - Asynchronous calls to multiple simultaneous remote Lambda calls,
 - Prewarms  and keeps warm a number of Lambda functions,
 - Scales up Lambda functions and retries / makes multiple calls to get quicker responses whilst scaling up.

How Muster solves some of the limitations with AWS Lambda Java Functions
-----------------


----------


 **Slow startup times.**
A Java Lambda function can take 10 seconds or more to start (in extreme circumstances). Muster works around this by:

 - Prewarming - A configurable number of functions are kept warm. The prewarm call is triggered every 5 minutes as it appears to be sufficient to keep the Lambda warm.
 - Retriggering - When Muster detects a call has taken more than 1 second, it automatically retriggers the call. The built in idempotency will ensure that only 1 call is actually processed.
 - Larger Memory settings - A default 512mb (configurable) memory size is used. This has the side effect of a faster cpu and better startup times.
 - Mutliple service per Lambda - Having many services inside the one Lambda means that even infrequently used functions are more likely to be in a warm state.
 


----------


 
 **Too many functions.**
 Any decent size web service will end up having a lot of services. As Lambda functions need to be deployed independently, this can result in hundreds of deployable units. The dependencies between these can become unwieldly.
 Muster tries to solve this via:
 
 - Multiple functions and services deployed into a single Lambda.
 - Type safety between Muster functions even when deployed in another Lambda.
 - Gradle dependency management used to manage compile and runtime dependencies.
 


----------


**Consistency**
When running an application things will go wrong. The administrator needs to be able to determine when something goes wrong and have to have the ability to correct any of the data inconsistencies caused. Muster tries to solve this via:

 - Recording of calls - When a Muster Service is annotated with 'idempotency = true', Muster will record the status, request and response of the call. This data is stored in the 'MusterCall' Dynamo DB table.
 - Retry - Idempotent calls can be retried (with the same request reference). If the call has previously succeeded, the recorded result will be returned. Otherwise the failed call will be retried. If a parent call is retried, it will generate the same requestReference for its child calls and thus these will also be idempotent.
 
It is envisaged that in the future an interface will need to be created over the MusterCall Dynamo Db table to provide for better visibility and error correction.

----------
**Performance**
As Lambda functions only support a single request thread at a time, it means that you will potentially need a large number of Lambda instances to support even a moderately used webservice. Also you are charged for the invocation time. Muster attempts to improve performance by

 - Asynchronous calls - A Muster call to a remote Muster Function will make the call asynchronously and return a future. Thus multiple child calls can be in progress at once.
 - Configurable Idempotency - As the idempotency check requires a read and write to Dynamo DB, this adds an overhead. For calls that don't need to be idempotent (e.g. read only services) the idempotency can be disabled.

Remote Lambda Call Code Example
--------
    	
	//Call remote ProductsService lambda with type safety
	Future<Product> productFuture = orchestrationManager.call(ProductsService.GetProductFunction.class, order.getProductNumber());
	
	//Call remote AccountsService lambda with type safety
	Future<Account> accountFuture = orchestrationManager.call(AccountsService.GetAccountFunction.class, order.getAccountNumber());
					
			
	//Wait for remote lambda calls to return.
	Product product = productFuture.get();
	Account account = accountFuture.get();

Service Configuration YAML example
--------

    AWSTemplateFormatVersion: '2010-09-09'
    Transform: 'AWS::Serverless-2016-10-31'
    Description: 'Example Muster Account Service'
    Resources:
      InvoiceService:
        Type: 'AWS::Serverless::Function'
        Properties:
          Handler: 'com.github.dmozzy.muster.apilambda.MusterLambdaHandler::lambdaEntry'
          Runtime: java8
          CodeUri: ./build/distributions/impl.zip
          Description: ''
          MemorySize: 512
          Timeout: 15
          Role: !Sub "arn:aws:iam::${AWS::AccountId}:role/MusterRole"
          Events:
            PreWarmLambdas:
              Type: Schedule
              Properties:
                Schedule: rate(5 minutes)
          Environment:
            Variables:
              ServiceClasses: com.github.dmozzy.muster.example.invoice.impl.InvoiceServiceImpl
              PreWarmCount: 3

Service Definition Example
--------

    public interface InvoiceService {
    	@MusterServiceConfiguration(service="InvoiceService", name="ListInvoices", idempotency=false)
    	public interface ListInvoicesFunction extends MusterService<Void, List<Invoice>> {
    		@Override
    		List<Invoice> execute(Void input);
    	}
    
    	@MusterServiceConfiguration(service="InvoiceService", name="SaveInvoice", idempotency=true)
    	public interface SaveInvoiceFunction extends MusterService<Invoice, Invoice> {
    		@Override
    		Invoice execute(Invoice input);
    	}	
    }

Getting Started
--------
**Warning: this is not for the faint of heart and assumes a good understanding of AWS, IAM, API Gateway, Lambda and Dynamo DB. It also assumes that you have the aws cli installed and are running on Mac OSX or Linux.**

Currently Muster is only used in a test project. Below are the instructions for installing and running the example project.


 1. Setup an S3 bucket that can be used for deployment. This will need to be used when running the deployment later.
 2. Cd into the api-lambda folder.
 3. Run the following to configure the Persistent Stack: `aws cloudformation deploy --template-file MusterPersistentStack.yaml --stack-name PersistentStack --capabilities CAPABILITY_NAMED_IAM`
 4. Cd into the examples folder and run the following command: `./deployAll <S3 Bucket Name>`
 5. You will need to edit the API Gateway Order Service that has been created. You will need to do the following:
	 1. Go to API Gateway
	 2. Click on OrderService
	 3. Click on ANY (Under / -> OrderService)
	 4. Click on "Integration Request"
	 5. Untick "Use Lambda Proxy integration"
	 6. Click on "<- Method Request"
	 7. Click on "Method Response" and add the 200 response code.
	 8. Republish the OrderService

You should now be able to call the OrderService using the API Gateway URL.

An example post body is:

        {
    	  "requestReference": "1",
    	  "method": "CreateOrder",
    	  "data": {
    	    "accountNumber": "A123",
    	    "quantity": 1,
    	    "productNumber": "D1"
    	  }
    }

This can be used to call API Gateway or in the Lambda console as Test data.

Results
-------
Although a simple example and representative of the real world, the Muster example project can easily handle ~ 100 concurrent requests. This is with a configuration of max 1000 Lambdas and 25 units of Dynamo DB Read and Write.

![MusterTest.jmx Jmeter test](https://github.com/dmozzy/Muster/blob/master/images/PerformanceScreenshot.png)


This is the result of running the included MusterTest.jmx file.

> Written with [StackEdit](https://stackedit.io/).


