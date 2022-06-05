#!/bin/bash

mkdir testbed
cd testbed

wget http://$MY_IP/assets/Server-1.0-STABLE.jar
wget http://$MY_IP/assets/Client-1.0-STABLE.jar

mkdir testbed-client
mkdir testbed-server
cd testbed-server

wget http://$MY_IP/assets/newmodel.zip
wget http://$MY_IP/assets/cifar-10-binary.tar.gz
tar -xvzf cifar-10-binary.tar.gz
mv cifar-10-batches-bin cifar-10
