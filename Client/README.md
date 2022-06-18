# Client

An implementation of a Federated Learning Client

## Build Docker image

Make sure to set the environment variables GITHUB_USERNAME and GITHUB_TOKEN for authentication

```
docker build --build-arg GITHUB_USERNAME=$GITHUB_USERNAME --build-arg GITHUB_TOKEN=$GITHUB_TOKEN -t [name] .
```

## Run Docker image as container

```
docker run --network="host" -e PORT=[port] [image name] --fl --workdir [path to work dir] --nclients [number of clients] --host [host] --port [port]
```