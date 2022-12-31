# Server

An implementation of a Federated Learning Server

## Requirements
The user must have proper github authentication from the contributors to be able to make the jar using maven. This is because the Server file depends on a the `CommonUtils` package. They can do this by setting the `GITHUB_TOKEN` and `GITHUB_USERNAME` environment variables.

To use the server folder for installing the jar file, run `mvn compile` and then `mvn package`inside it. This saves the JAR file in `/target` from where it can be run.

To install the CIFAR-10 dataset, visit https://www.cs.toronto.edu/~kriz/cifar.html and install the CIFAR-10 binary version. The `workdir` mentioned below must contain a folder called `cifar-10`, this must contain the data batches (e.g. data_batch_1.bin).

The `newmodel.zip` file is an untrained cifar-10 model stored in deeplearning4j's native format for deep learning models. The server reads from this file to initialize the model, and then sends it over to the clients. This file must also be in the `workdir` mentioned below along with the `cifar-10` folder.

## Run Server jar file
To run the Server file, the user must have Java 8 installed. Java 11 and above can lead to segfault issues due to instability of Deeplearning4j.
```
java -jar [path] [--fl/--ml] --workdir [workdir] [--config] --numClients [numClients] --datasetratio [datasetratio] --rounds [#rounds] --port [port]
```


| Option | Description                                              | Type   | Default | 
|--------|----------------------------------------------------------|--------|---------|
| `path` | Path to jar file | path|  None 
| `--fl/--ml`   | Use `--fl` for decentralized Federated Learning and `--ml` for centralized normal Machine Learning| `option` | `--fl`  
| `workdir` | Working directory where the server saves the model and the training results. The server also expects the `cifar-10` folder and the `newmodel.zip` mentioned above. | path | Current Directory 
| `[--config]` | Include if config.json file is to be used, otherwise data will be divided evenly among clients | bool | not included
| `numClients` | The minimum amount of Clients before that server starts training | Int | 1  
| `datasetratio`| Percentage of the total dataset that is to be used for training. Ranges from 0 (min) to 1 (max) | float | 1
| `rounds` | Number of rounds the training should be done | Int | 3
| `port` | Port that will be used by the client to connect | Int | 4062



## Config

Configuring how the data and different classes are divided among the clients can be done through the config.json file. The config file must exist within the `workdir` the user sets from command line. The user must also add '--config true' to tell the Server that the config.json file must be used, otherwise the dataset is divided evenly among clients.  

### Data Distribution
Decides how data is distributed among the clients. Can be done through two ways, distribution with uniform label distribution, and ununiform label distribution. 

**Uniform label distribution:** Set `evenLabelDistributionByClient` to true.  In this case, you only control how much data each client receives, but they all have the same percentage of classes. You can control this type of distribution by passing `distributionRatiosByClient` an array of size `minClients`. Index i in this array would be a float between 0 and 1, and would decide how much data the ith client would take. 

Example: `"distributionRatiosByClient": [0.3, 0.3, 0.4]` In this case, we would have three clients, and first one would get 30% percent of the data and the last one 40%.

**Ununiform label distribution**: Set `evenLabelDistributionByClient` to false. In this case, you not only control both how much data clients receive but also how the classes are distributed among them. You can control this type of distribution by passing `distributionRatiosByLabels` an array of dimensions `minClients * Number of classes`. I.e. jth column in ith row decides how much of class j would the ith clients get. The sum of a single colomn and row must be equal to 1. 

Example: `"distributionRatiosByLabels": `

 [[0.5, 0.25, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5], 
				   [0.25, 0.5, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25],
				   [0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25]]

Here the first row refers to how much classes the first client is getting. The third entry for examples tell us that the first clients is getting 50% of the third class. 
## Build Docker image

Remember to set the working directory (is set to the current directory by default). The working directory should contain the new model and the CIFAR10 dataset

```
docker build -t [name] --build-arg WDIR=[path to working directory] --secret id=mvn_settings,src=[path to settings.xml] .
```

## Run Docker image

```
docker run --network="host" -e PORT=[port] [image name] --fl --port [port] --workdir [path to working directory] --minClients [minimum Clients] --datasetratio [ratio of the dataset] --rounds [rounds]
```

 


