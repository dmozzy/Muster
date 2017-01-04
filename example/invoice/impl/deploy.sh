#!/bin/sh
gradle clean build buildZip
aws cloudformation package --template-file InvoiceService.yaml --output-template-file InvoiceServiceOutput.yaml --s3-bucket $1
aws cloudformation deploy --template-file InvoiceServiceOutput.yaml --stack-name InvoiceService

