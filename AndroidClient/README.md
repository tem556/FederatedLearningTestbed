# Android Client

An implementation of a Federated Learning Client for Android devices.

## Build the Android Client apk file

The user must have proper GitHub authentication from the contributors to be able to make the apk file using gradle. This is because the Android Client file depends on the `CommonUtils` package.

- Collaborators can set the `GITHUB_TOKEN` and `GITHUB_USERNAME` environment variables to authenticate with the GitHub registry.
- Others can install the `CommonUtils` to the local Maven registry by running `mvn install` in the `CommonUtils` folder.

To build the Android Client apk file, under the `Client` folder, run `gradle build` with Java 11. This command will create an `app-debug.apk` file under the `app/build/outputs/apk/debug` folder. The user can then copy this file to the device of their choice and use it to install the application.

## Runing the Android Client application

Upon opening the application, the user will be asked to provide the `host`, the `port` number and to choose between the cifar10 and the Pneumonia dataset. Uponing entering the proper values for each field, the user should simply click on the `Start` button to start the training. The user is advised to check from the Server side to ensure that the training did indeed start, as the Android Client application  currently does not give an indication for it.
