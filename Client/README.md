# Client

An implementation of a Federated Learning Client

## Build Docker image

Remember to change [GITHUB_USERNAME] and [GITHUB_TOKEN] to your own in Dockerfile for authentication

```
docker build -t [name] .
```

## Run Docker image as container

```
docker run --network="host" -e PORT=[port] [image name] --fl --workdir [path to work dir] --nclients [number of clients] --host [host] --port [port]
```