# Short Manual

Please refer to the user manual for more detailed and additional information. Information for developers can be found at the end of this file.

## System Requirements

Java 8 must be installed. On Ubuntu-based system with an older version of Java (check `java -version`), execute the following commands:

    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java8-installer

## Run the Game

In order to help us with finding bugs, you should create a log file when running the game by starting it using this command (we’ll add an automatic logging feature in the future):

    java -jar team61.jar 2>&1 | tee `date +%H%M%S`.log

If you happen not to use a Linux-based system, use the ordinary

    java -jar team61.jar

command (or double-click if it works for you). Please make sure that the execution directory is the one containing the .jar file.

## How to play a local game

1. Click on “Start local game”.
2. Choose the teams and a game style. (These can be changed using the “Customize” option in the main menu.) 
3. Click on “Continue”.
4. The active team is shown in the top left corner. Use the left/right/up (or A/D/W) keys to move the active figure.
5. Press a number to select a weapon. Press up/down/left/right (or W/S/A/D) to move the cross hair.
6. Press space to shoot.
7. When only one team is left, a game over window is shown.

## How to play a network game

### Start Server

1. Click on “Start network game”.
2. Enter your name.
3. Click on “Host a game”. (NB: If you’re playing over the Internet, make sure that your firewall does not block connections on port 61421. You might need to enable port-forwarding in the settings of your router.)
4. Change the game settings.
5. Wait for all players being ready.
6. Click on “Start”.
7. The game can be paused by pressing P, ESC, or F1.

### Connect as Spectator

1. Click on “Start network game”.
2. Enter your name and the ip address of the host.
3. Click on “Join a game”.
4. Depending on the game status, you see the lobby (and you have to wait till the host starts the game) or the running game.

### Connect as Player

1. Follow the steps of “Connect as Spectator”
2. Uncheck “Spectator”.
3. If the maximum number of teams is not reached, you can now choose your team profile.
4. Click on “Ready” to inform the server of the changes you made.
5. Wait for the host to start the game.

### How to set up a connection with a computer being connected using a patch cable (aka How to get an ip with a LAN connection)

NB: Modern hardware does not require to use a crossover cable.

1. Open the tool of your operating system for creating new network connections (connection editor or the like).
2. Create a new cable connection.
3. Set “cloned mac address” to the hardware address of your network card (eg. see `ifconfig`, section `ethX`).
4. Set “IPv4 Method” to “Manual”.
5. Add address “10.0.0.x” (x must be unique for each player), network mask “255.255.255.0”.
6. Give the connection a sensible name, eg. “Afrobob”.
7. Connect.

## Information for Developers

### Import into IDE

#### IntelliJ

You can download the free community edition of IntelliJ from [jetbrains.com/idea](http://www.jetbrains.com/idea/).

##### Import Project
1. Click on “File” → “Import Project…”
2. Choose the folder which contains the `src` directory. Click on “OK”.
3. Choose “Create project from existing sources,” and click on “Next”.
4. Leave the default project location, and click on “Next”.
5. Make sure that `src` is detected as java source directory, and click on “Next”.
6. Add the `lib` directory as external library, and click on “Next”.
7. No dependencies are needed, click on “Next”.
8. Choose SDK version 1.8 (JDK home path might be something like `/usr/lib/jvm/java-8-oracle`). Click on “Finish”.

##### Change Project Settings
1. Click on “File” → “Project Structure…”.
2. Under “Project,” set language level to 8.0.
3. Click on “OK”.

##### Create Run Configuration
1. Click on “Run” → “Edit Configurations”.
2. Click on “Add New Configuration” → “Application”.
3. Set main class to `afrobob`.
4. Click on “OK”.

##### Create .jar Build Configuration 
1. Click on “File” → “Project Structure…”.
2. Under “Artifacts”, click on “Add” → “Jar” → “From modules with dependencies”.
3. Choose “Afrobob” as “Main Class”.
4. In both dialogs, click on “OK”.
5. Click on “Build” → “Build artifacts…” → “Build”.
6. Copy (or link) the `resources` directory to `out/artifacts/*_jar/`.

#### other IDEs

Please refer to the manual of your IDE. If you can provide step-by-step instructions for your favourite IDE, we are willing to add them here.

### Generating JavaDoc

#### CLI

1. `cd` to the `src` directory.
2. Execute `javadoc -d ../doc -subpackages de -private -exclude de.hhu.propra.team61.io.json`.

#### IntelliJ

1. Click on “Tools” → “Generate JavaDoc…”.
2. (optional, to suppress some errors) Choose “Custom Scope”.
3. (optional) Include “Production Classes” recursively.
4. (optional) Exclude “de.hhu.propra.team61.io.json” recursively. (The JavaDoc of this library is not well-formed.)
5. Choose a sensible output directory.
6. You might want to chose output level “private” and check “Open generated documentation in browser”.
7. Click on “OK”.
