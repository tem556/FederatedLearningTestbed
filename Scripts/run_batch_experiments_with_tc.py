#!/usr/bin/python3

import subprocess
import os
import shutil
from distutils.dir_util import copy_tree
import datetime
import json
from time import sleep

def run_experiment(args):
    if len(args) == 0:
        return

    current_time = datetime.datetime.now()

    dataset_ratio = args[0]
    num_rounds = args[1]
    clients = args[2]
    num_clients = str(len(clients))
    latency = args[3]
    rate = args[4]
    packet_loss = args[5]
    
    conf = {
        'dataset_ratio': dataset_ratio,
        'rounds': num_rounds,
        'clients': clients,
        'latency': latency,
        'rate': rate,
        'packet_loss': packet_loss
    }

    experiment_name = f"experiment-{current_time.year}-{current_time.month}-{current_time.day}-{current_time.hour}-{current_time.minute}-{current_time.second}"
    
    print(f'running experiment dataset ratio = {dataset_ratio} #clients = {num_clients} #rounds = {num_rounds}..., save as {experiment_name}')
    
    # start server
    os.system("rm -rf testbed-client/** && rm -rf testbed-server/model* && rm -rf testbed-server/logs/** && sudo tcdel eth0 --all ")
    os.system(f"sudo tcset eth0 --rate {rate} --delay {latency} --loss {packet_loss}")
    print('traffic control set')
    
    
    server_proc = subprocess.Popen(["/usr/lib/jvm/java-8-openjdk-amd64/bin/java",
                                    "-jar", "Server-1.1-SNAPSHOT.jar",
                                    "--fl",
                                    "--minClients", num_clients,
                                    "--workdir", "./testbed-server",
                                    "--datasetratio", dataset_ratio,
                                    "--rounds", num_rounds])
    print('server started')
    sleep(3)
    
    try:
        for clientaddr in clients:
            print(f"ssh to {clientaddr}")
            os.system(f"ssh {clientaddr} 'sudo tcdel eth0 --all && sudo tcset eth0 --rate {rate} --delay {latency} --loss {packet_loss} && cd testbed && rm -rf testbed-client/** && rm -rf testbed-server/model* && tmux new-session -d \"/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar Client-1.1-SNAPSHOT.jar --fl --nclients 1 --workdir ./testbed-client --host 139.162.27.251\" && tmux ls'")
    except:
        print("error!")
        server_proc.kill()
        return
    
    server_proc.wait()
    
    res_path = f'res/{experiment_name}'
    os.mkdir(res_path)
    copy_tree('testbed-server/logs', res_path)
    shutil.copy('server-logs/app.log', res_path)
    
    # save the experiment configuration
    conf_file = open(f"{res_path}/conf.json", "w")
    json.dump(conf, conf_file, indent=4)
    conf_file.close()
    
    next_args = args[3:]
    run_experiment(next_args)

    sleep(10)
    
run_experiment((
    "1.0", "33", ["root@139.162.27.108", "root@139.162.27.112", "root@139.162.27.237"], "60ms", "30Mbps", "0.1%",
    "1.0", "33", ["root@139.162.27.108", "root@139.162.27.112", "root@139.162.27.237"], "100ms", "30Mbps", "0.1%",
    "1.0", "33", ["root@139.162.27.108", "root@139.162.27.112", "root@139.162.27.237"], "60ms", "30Mbps", "0.3%",
    "1.0", "33", ["root@139.162.27.108", "root@139.162.27.112", "root@139.162.27.237"], "60ms", "10Mbps", "0.1%"
))
