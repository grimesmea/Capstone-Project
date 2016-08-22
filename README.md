#Android Nanodegree Capstone Project - PrickleFit

PrickleFit is simple Android pedometer application created as a Capstone project
for Udacity's Android Nanodegree program. The app allows you to unlock hedgehog
companions who you keep happy by staying active. The app uses Google Fit APIs
to sense, record, and query a user's step count data, using a selected Google
account.

##Installation

To install this app, clone the [GitHub repository](https://github.com/grimesmea/Capstone-Project).

An OAuth client ID is required for the app to access the Google Fit APIs. *Note:
a Google account is required in order to generate a client ID in the Google
Developers Console.* Follow instructions found at
(https://developers.google.com/fit/android/get-api-key) to get a client ID for
the app. Download the JSON client ID file generated in the API Manager, move it
to the project's *app/* directory with the file name *client_id.json*.

This project uses the Gradle build system. To build, use the `gradlew build`
command or use "Import Project" in Android Studio.

To install the app, use the `gradle installRelease` or `gradle installDebug`
command. In order for the app to begin tracking your steps, sign into a Google
account when prompted.

##Builds
To run the release build of the app, create a keystore and add a file
*release.properties* to the root directory of the project. Add the following
code snippet to the *release.properties* file using the keystore password,
key alias, and key password to complete.

```
keyStore=[FILE NAME OF THE KEYSTORE YOU CREATED]
keyStorePassword=[KEYSTORE PASSWORD]
keyAlias=[KEY ALIAS]
keyPassword=[KEY PASSWORD]
```
