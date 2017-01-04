#!/bin/sh
gradle clean build buildZip
aws cloudformation package --template-file OrderService.yaml --output-template-file OrderServiceOutput.yaml --s3-bucket $1
aws cloudformation deploy --template-file OrderServiceOutput.yaml --stack-name OrderService

