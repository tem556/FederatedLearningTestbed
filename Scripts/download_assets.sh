#!/bin/bash

for _client in 1 2 3 4
do
	echo "ssh to $(printf "tnbui@nsl-n%02d.qatar.cmu.local" $_client)"
	ssh -T $(printf "tnbui@nsl-n%02d.qatar.cmu.local" $_client) <<-EOL
		[ -d testbed ] && rm -rf testbed
		mkdir testbed
		cd testbed
		tmux new-session -d 'wget http://$MY_IP/assets/Server-1.1-SNAPSHOT.jar'
		tmux new-session -d 'wget http://$MY_IP/assets/Client-1.1-SNAPSHOT.jar'
		mkdir testbed-client
		mkdir testbed-server
		cd testbed-server
		tmux new-session -d 'wget http://$MY_IP/assets/newmodel.zip'
		tmux new-session -d 'wget http://$MY_IP/assets/cifar-10-binary.tar.gz && tar -xvzf cifar-10-binary.tar.gz && mv cifar-10-batches-bin cifar-10'
	EOL
done
