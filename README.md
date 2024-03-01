# Simple Java Console Application using Realm-Kotlin.

<img src="./Screenshots/DemoConsoleJVM.gif" width="600">

This application demonstrates the usage of Realm Kotlin SDK in a regular console app (no Multiplatform).
It is powered by Kotlin/JVM support from Realm-Kotlin SDK.

It uses also an experimental feature of encryption which uses a callback to provide the AES key from native memory, it also disposes of the key after the Realm is open. 
## Run from IntelliJ IDEA

Navigate to `src/main/kotlin/io.realm/example/main.kt`
Click on `Run MainKt` 
<img src="./Screenshots/run.png" width="600">

## Standalone jar

```Gradle
./gradlew --refresh-dependencies clean jar
java -jar build/libs/JVM_Console-1.0.0.jar
```

