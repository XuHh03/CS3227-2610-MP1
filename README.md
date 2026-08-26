# NutriByte

NutriByte is a Java pantry inventory and expiry-management application.

## Setting up in IntelliJ

Prerequisite: JDK 25.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. Locate `src/main/java/nutribyte/ui/NutriByte.java`.
1. Right-click the file and choose `Run 'NutriByte.main()'`.

You can also run the application with Gradle:

```shell
./gradlew run
```

To build and run the fat JAR:

```shell
./gradlew shadowJar
java -jar build/libs/nutribyte.jar
```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
