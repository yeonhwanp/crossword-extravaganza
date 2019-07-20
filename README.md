# Crossword Extravaganza: Competitive real-time online crossword game!

Race against one of your other friends to guess more crossword puzzles correctly before the board is filled up!

![Alt desc](https://github.com/yeonhwanp/autowater/blob/master/docs/Front.png)

## Features
- Play on the same network or other the web with a command line interface or a more natural interface using Java's Swing package.
- Updates submissions in real time -- no more having to wait for a turn-by-turn game!
- Server side code ensures that no two actions on the same blanks cause breakage: asynchronous requests and responses are handled in a thread-safe manner.

## Dependencies
**Built With:**
- [Java JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Install
```
git clone git@github.com:yeonhwanp/crossword-extravaganza.git
```

## Usage
- **Server**: Upload to a designated web-server to run the code. 
- **Client**: Open `/src/client/client.ino` in Arduino and compile it into the ESP32.

## Game Rules
- A player will enter a word with the syntax `TRY <WORD_ID> WORD`. That word is not locked into the board until it is either confirmed with a `CONFIRM <WORD_ID> WORD` or a `CHALLENGE <WORD_ID> WORD` by a different player.
- If an entered word is inconsistent with that player's other submitted (but not yet confirmed) words, then the inconsistent words will be cleared out.
- Nothing will happen on entering a word that is inconsistent with another player's submitted words.
- The game ends when all words on the board are correct.
- Scoring works as follows:
  - 1 Point for each correct word
  - 2 Points for each correct Challenge
  - -1 Point for an incorrect Challenge


