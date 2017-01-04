#!/bin/sh
cd accounts/impl
./deploy.sh $1
cd ../../invoice/impl
./deploy.sh $1
cd ../../products/impl
./deploy.sh $1
cd ../../order/impl
./deploy.sh $1
