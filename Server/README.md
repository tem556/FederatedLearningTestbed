# Server

An implementation of a Federated Learning Server

## Set configure file

The configure file must exist within the working directory the user sets from command line. The user must also add '''--config true''' to tell the Server that the config.json file must be used, otherwise the dataset it is divided evenly among clients.  

## Build Docker image

Remember to set the working directory (is set to the current directory by default). The working directory should contain the new model and the CIFAR10 dataset

```
docker build -t [name] --build-arg WDIR=[path to working directory] --secret id=mvn_settings,src=[path to settings.xml] .
```

## Run Docker image

```
docker run --network="host" -e PORT=[port] [image name] --fl --port [port] --workdir [path to working directory] --minClients [minimum Clients] --datasetratio [ratio of the dataset] --rounds [rounds]
```

 


