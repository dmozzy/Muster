#!/bin/sh
gradle clean build buildZip
aws cloudformation package --template-file AccountsService.yaml --output-template-file AccountsServiceOutput.yaml --s3-bucket $1
aws cloudformation deploy --template-file AccountsServiceOutput.yaml --stack-name AccountsService

