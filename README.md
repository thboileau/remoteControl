# Bluetooth remote control

## Goal

* Pilot presentations from my Android phone.
* The remote control is connected without cable
* The remote control allows to move forward, backward or stop the presentation

## Solution

* leverage bluetooth connection
* the bluetooth client is my android phone since it initiates the communication
* the bluetooth server is the PC that hosts the presentation

The Bluetooth client part is an Android application.

The Bluetooth server is develop in Java language (structured as a Maven project) and leverages the [bluecove library](http://bluecove.org/).

## Build Android app

### installation of the development environment

#### Install the JDK 8

```
sudo apt-get install openjdk-8-jdk-headless
```

#### Install the Android SDK manager

[Sownload the SDK manager](https://developer.android.com/studio/index.html#downloads)
and extract the zip in `/opt/android-sdk/`.

Check that the `/opt/android-sdk/tools/bin/sdkmanager` directory exists.

#### Solve Android SDK manager issues

You may notice issues when running the SDK manager.

```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
sudo apt-get install adb

# optional?
sudo apt-get install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386

# 
unset JAVA_OPTS

/opt/android-sdk/tools/bin/sdkmanager --update
touch /home/thboileau/.android/repositories.cfg 
```

#### Install the Android SDK and build tools.

```
/opt/android-sdk/tools/bin/sdkmanager "platform-tools" "platforms;android-28" "build-tools;28.0.3"
```

Check that the following directories exist:
`/opt/android-sdk/build-tools/28.0.3/`

`/opt/android-sdk/platforms/android-28`

### environment variables

```
export PROJ=~/remoteControl/HelloAndroid
export BUILD_TOOLS_VERSION=28.0.3
export ANDROID_BUILD_TOOLS_DIR=/opt/android-sdk/build-tools/$BUILD_TOOLS_VERSION/
export ANDROID_PLATFORM_DIR=/opt/android-sdk/platforms/android-28
export JAVA8_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

### Initiate the keystore to sign the apk file

Signing the apk is mandatory in order to install it on the phone. The following step is used to initiate the keystore called each you will have to sign the apk.
`mykey.keystore` represents the path to the keystore.

```
keytool -genkeypair -validity 365 -keystore mykey.keystore -keyalg RSA -keysize 2048
```

### build steps

```
# build

cd $ANDROID_BUILD_TOOLS_DIR
./aapt package -f -m -J $PROJ/src -M $PROJ/AndroidManifest.xml -S $PROJ/res -I $ANDROID_PLATFORM_DIR/android.jar

## Compile

cd $PROJ
$JAVA8_HOME/bin/javac -d obj -classpath src -bootclasspath $ANDROID_PLATFORM_DIR/android.jar src/org/thboileau/remoteControl/*.java

## Translation to classes.dex read by the Dalvik runtime
cd $ANDROID_BUILD_TOOLS_DIR
./dx --dex --output=$PROJ/bin/classes.dex $PROJ/obj


## put everything in APK
./aapt package -f -m -F $PROJ/bin/remoteControl.unaligned.apk -M $PROJ/AndroidManifest.xml -S $PROJ/res -I $ANDROID_PLATFORM_DIR/android.jar
cp $PROJ/bin/classes.dex .
./aapt add $PROJ/bin/remote.unaligned.apk classes.dex

#Align the apk
./zipalign -f 4 $PROJ/bin/remoteControl.unaligned.apk $PROJ/bin/remote.apk

# Sign the package

## sign the apk
./apksigner sign --ks mykey.keystore $PROJ/bin/remote.apk
```

### Install

```
adb install -r $PROJ/bin/remote.apk
```

### Run from the PC

```
adb shell am start -n org.thboileau.remoteControl/.MainActivity
```
