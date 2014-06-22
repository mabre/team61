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
