# Client

An implementation of a Federated Learning Client.

## Requirements
To run the Client file, the user must have Java 8 installed. Java 11 and above can lead to segfault issues due to instability of Deeplearning4j.

The user must also have proper github authentication from the contributors to be able to make the jar using maven. This is because the Client file depends on a the `CommonUtils` package.

## Run Client jar file
```
java  -jar [path] [--fl/--ml] --workdir [workdir] --nclients [nclients] --host [Server IP] --port [port]
```


| Option | Description                                              | Type   | Default | 
|--------|----------------------------------------------------------|--------|---------|-----------|
| `path` | Path to jar file | path|  None 
| `--fl/--ml`   | Use `--fl` for decentralized Federated Learning and `--ml` for centralized normal Machine Learning| `option` | `--fl`  
| `workdir` | Working directory where the client saves the received model and dataset | path | Current Directory 
| `nclients` | Not sure yet | Int | 1  
| `Server IP` | IP address of the Server so the client can connect to it | Address | 127.0.0.1 
| `port` | Port that was set on the Server side | Int | 4062



## Build Docker image

Make sure to set the environment variables GITHUB_USERNAME and GITHUB_TOKEN for authentication.

```
docker build --build-arg GITHUB_USERNAME=$GITHUB_USERNAME --build-arg GITHUB_TOKEN=$GITHUB_TOKEN -t [name] .
```

## Run Docker image as container

```
docker run --network="host" -e PORT=[port] [image name] --fl --workdir [path to work dir] --nclients [number of clients] --host [host] --port [port]
