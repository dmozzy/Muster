#!/bin/sh
gradle clean build buildZip
aws cloudformation package --template-file ProductsService.yaml --output-template-file ProductsServiceOutput.yaml --s3-bucket $1
aws cloudformation deploy --template-file ProductsServiceOutput.yaml --stack-name ProductsService

