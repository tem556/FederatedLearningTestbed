#!/bin/bash

_server=01
_client_start=02
_client_end=03

echo "ssh to $(printf "tnbui@nsl-n%02d.qatar.cmu.local" $_server)"
ssh -T $(printf "tnbui@nsl-n%02d.qatar.cmu.local" $_server) <<-EOL
	cd testbed
	rm -rf testbed-client/**
	rm -rf testbed-server/model*
	tmux new-session -d 'java -jar Server-1.0-STABLE.jar --fl --minClients 2 --workdir ./testbed-server --datasetratio 1.0 --rounds 23'
	tmux ls
EOL

for _client in 2 4
do
	echo "ssh to $(printf "tnbui@nsl-n%02d.qatar.cmu.local" $_client)"
	ssh -T $(printf "tnbui@nsl-n%02d.qatar.cmu.local" $_client) <<-EOL
		cd testbed
		rm -rf testbed-client/**
		rm -rf testbed-server/model*
		tmux new-session -d 'java -jar Client-1.0-STABLE.jar --fl --nclients 1 --workdir ./testbed-client --host 172.20.216.42'
		tmux ls
	EOL
done
