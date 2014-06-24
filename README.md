# Short Manual

## System Requirements

Java 8 must be installed. On Ubuntu-based system with an older version of Java (check `java -v`), execute the following commands:

    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java8-installer

## Run the Game

In order to help us with finding bugs, you should create a log file when running the game by starting it using this command (we’ll add an automatic logging feature in the future):

    java -jar team61.jar 2>&1 | tee `date +%H%M%S`.log

If you happen not to use a Linux-based system, use the ordinary

    java -jar team61.jar

command (or double-click if it works for you).

## How to play a local game

1. Click on “Start local game”.
2. Choose the teams and a game style. (These can be changed using the “Customize” option in the main menu.) 
3. Click on “Continue”.
4. The active team is shown in the top left corner. Use the left/right/up (or A/D/W) keys to move the active figure.
5. Press 1, 2, 3, or 4 to select a weapon. Press up/down/left/right (or W/S/A/D) to move the cross hair.
6. Press space to shoot. (NB: When there is no munition for the chosen weapon, nothing happens)
7. When only one team is left, a game over window is shown.

## How to play a network game

### Start Server

1. Click on “Start network game”.
2. Enter your name.
3. Click on “Host a game”. (NB: If you’re playing over the Internet, make sure that your firewall does not block connections on port 61421. You might need to enable port-forwarding in the settings of your router.)
4. Change the game settings.
5. Wait for all players being ready.
6. Click on “Start”.
7. The game can be paused by pressing P or ESC.

### Connect as Spectator

1. Click on “Start network game”.
2. Enter your name and the ip address of the host.
3. Click on “Join a game”.
4. Depending on the game status, you see the lobby (and you have to wait till the host starts the game) or the running game.

### Connect as Player

1. Follow steps 1–4 of “Connect as Spectator”
2. Uncheck “Spectator”.
3. If the maximum number of teams is not reached, you can now choose your team profile.
4. Click on “Ready” to inform the server of the changes you made.
5. Wait for the host to start the game.

## Information for Developers

### Import into IDE

#### IntelliJ

You can download the free community edition of IntelliJ from [jetbrains.com/idea](http://www.jetbrains.com/idea/).

##### Import Project
1. Click on “File” → “Import Project…”
2. Choose the folder which contains the `src` directory. Click on “Ok”.
3. Choose “Create project from existing sources,” and click on “Next”.
4. Leave the default project location, and click on “Next”.
5. Make sure that `src` is detected as java source directory, and click on “Next”.
6. No external libraries are needed, click on “Next”.
7. No dependencies are needed, click on “Next”.
8. Choose SDK version 1.8 (JDK home path might be something like `/usr/lib/jvm/java-8-oracle`). Click on “Finish”.

##### Change Project Settings
1. Click on “File” → “Project Structure…”.
2. Under “Project,” set language level to 8.0.
3. Click on “Ok”.

##### Create Build Configuration
1. Click on “Run” → “Edit Configurations”.
2. Click on “Add New Configuration” → “Application”.
3. Set main class to `afrobob`.
4. Click on “Ok”.

#### other IDEs

Please refer to the manual of your IDE. If you can provide step-by-step instructions for your favourite IDE, we are willing to add them here.

### Generating JavaDoc (using IntelliJ)

1. Click on “Tools” → “Generate JavaDoc…”.
2. Choose “Custom Scope”.
3. Include “Production Classes” recursively.
4. Exclude “de.hhu.propra.team61.io.json” recursively. (The JavaDoc of this library is not well-formed.)
5. Choose a sensible output directory.
6. You might want to chose output level “private” and check “Open generated documentation in browser”.
7. Click on “Ok”.
