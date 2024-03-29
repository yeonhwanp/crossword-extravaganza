/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Crossword game client for server.
 */
public class Client {

    // Holds all of the information regarding the board details
    public enum ClientState {START, CHOOSE, WAIT, PLAY, SHOW_SCORE}

    // Holds all of the information regarding the player and their actions themselves
    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 900;
    private static final int TEXTFIELD_SIZE = 10;
    private static final int TEXTBOX_FONT_SIZE = 20;
    private static final int ENTERBUTTON_SIZE = 10;
    private static final int CANVAS_ADD = 50;
    private static final int CHOOSE_INPUT_LENGTH = 3;
    private static final int END_PLAYER_LINES = 6;
    private final String host;
    private final int port;
    private boolean exit = false;
    private String playerID = "";
    private String matchID = "";
    private CrosswordCanvas canvas = new CrosswordCanvas();

    // A simple alias to this object for locking
    private final Client thisLock = this;
    
    // NOT ACTUALLY USED FOR THE REP, JUST FOR TESTING PURPOSES.
    private boolean debugging = false;

    /*
     * Abstraction Function
     * AF(host, port, playerID, matchID, canvas, exit) = A client interacting with a CrosswordExtravagnaza client through
     *                                             a UI displayed by canvas and is connected to a CrosswordExtravagnaza 
     *                                             server at the url http://host:port with a unique identifying playerID 
     *                                             and a matchID if currently in an ongoing game. exit represents whether the user
     *                                             has terminated the connection between the server or not.
     * 
     * Rep Invariant:
     *  host is alphanumeric
     *  port >= 0
     *  The chosen playerID is alphanumeric
     *  The chosen matchID is alphanumeric
     * 
     * Safety from Rep Exposure:
     *  host and port are private, final, and immutable
     *  playerID, exit, matchID are private
     *  canvas is a mutable type but is never returned to the client or taken in as a parameter from a method, so it is safe from rep exposure
     *  all public methods take in and return immutable types, so it's safe to directly alias them. Hence, this is SRE
     *  
     * Thread safety argument:
     *  host and port are private, final, and immutable
     *  All accesses to playerID, matchID, and canvas happen within Client methods
     *      which are all guarded by Client's lock except for handleCommand and parseResponse.
     *      However, this is ok because all of the variables in handleCommand and parseReponse are confined
     *      and they do not access any internal reps. The methods they call on do access the rep. However, this is
     *      ok because they themselves are synchronized so only one thread can touch the reps at  a time.
     *      
     *      receiveWait() is a method used by parseResponse which is not synchronized. However, this is ok because it is
     *      synchronized in the places that it needs to be (where it touches the internal rep of Client). It releases the lock
     *      so that when it is reading input from a BufferedReader, it does not result in holding onto the lock for an indefinite
     *      amount of time. When it does need to touch the reps again after reading from the ResponseBuffer, however, it synchronizes
     *      back onto Client and checks that the state at which it was called in is still satisfied.
     *      
     *      getSubArray() and parseBoard() are not synchronized but they are static and as such do not touch the rep, and all variables
     *      are confined to the caller.
     *      
     *  Static methods do not touch any part of the rep and all variables inside of them are confined
     *      within that method.
     */
    
    //NOTE: on my 15 inch macbook pro (2016), the message for incorrect commands is pretty visible. However, TODO
    //      on a 13 inch macbook pro (2015 and prior) the message barely peeks out through the bottom. TODO
    //      Not really a todo... just a note for Czarina! TODO


    private synchronized void checkRep() {
        assert host.matches("^[a-zA-Z0-9]+$");
        assert port >= 0;
        if (!playerID.equals("")) {
            assert playerID.matches("^[a-zA-Z0-9]+$");
        }
        if (!matchID.equals("")) {
            assert matchID.matches("^[a-zA-Z0-9]+$");
        }
    }

    /**
     * Constructor for the Client object.
     * @param host the host that the client intends to connect to
     * @param port a port number that the client intends to use when
     *             connecting to the address as defined by host
     */
    public Client(String host, int port) { 
        this.host = host;
        this.port = port;
        checkRep();
    }

    /**
     * Launch a window for the user to interact with the Client with a textbox to type commands
     * as well as an enter button to submit entries in the textbox.
     */
    public synchronized void launchGameWindow() {

        canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        JTextField textbox = new JTextField(TEXTFIELD_SIZE);
        textbox.setFont(new Font("Arial", Font.BOLD, TEXTBOX_FONT_SIZE));

        // Upon enter, want to load into sendString and prompt the main thread to send to the server
        JButton enterButton = new JButton("Enter");
        enterButton.addActionListener((event) -> {

            // This code executes every time the user presses the Enter
            // button. Recall from reading 24 that this code runs on the
            // Event Dispatch Thread, which is different from the main
            // thread.
            String textboxInput = textbox.getText();
            textbox.setText("");
            if (textboxInput.equals("!")) {
                debugging = !debugging;
            }
            else {
                new Thread(() -> {
                    handleCommand(textboxInput);
                 }).start();
            }
        });

        enterButton.setSize(ENTERBUTTON_SIZE, ENTERBUTTON_SIZE);

        JFrame window = new JFrame("Crossword Client");
        window.setLayout(new BorderLayout());
        window.add(canvas, BorderLayout.CENTER);

        JPanel contentPane = new JPanel();
        contentPane.add(textbox);
        contentPane.add(enterButton);

        window.add(contentPane, BorderLayout.SOUTH);

        window.setSize(CANVAS_WIDTH + CANVAS_ADD, CANVAS_HEIGHT + CANVAS_ADD);

        window.getContentPane().add(contentPane);

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
        checkRep();
    }

    /**
     * Wrapper method to call other methods in response to a command submission
     * from the user.
     * 
     * @param userInput an input from a user.
     */
    private void handleCommand(String userInput) {
        try {
            // Formulate the URL
            String extension = parseUserInput(userInput);
            URL test = new URL("http://" + host + ":" + port + extension);
            
            if (debugging) {System.out.println("OUT: " + test);}
            
            // Send GET request, formulate response into one string
            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(test.openStream(), UTF_8));
            String response = ClientManager.receiveResponse(responseBuffer);
            
            if (debugging) {
              System.out.println("RESPONSE");
              System.out.println("-----------------------");
              System.out.println(response);
              System.out.println("-----------------------");
            }

            // Don't parse the repsonse if we've already exited
            if (exit) {System.exit(0);}
            
            // Otherwise, parse the message and close the buffer
            parseResponse(response, userInput);
            responseBuffer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            synchronized(thisLock) {
//                SwingUtilities.invokeLater(() -> {
                    canvas.setRequest(getState(), "try again");
                    repaint();
//                });
            }
        }
        checkRep();
    }

    // ========= OBSERVER METHODS ========= // 

    /**
     * @return the current player's ID
     */
    public synchronized String getUserID() {
        return playerID;
    }

    /**
     * @return the ID of the match being played at the moment.
     */
    public synchronized String getMatchID() {
        return matchID;
    }

    /**
     * @return the state of the game on the client side
     *         as specified by the project handout.
     */
    public synchronized ClientState getState() {
        return canvas.getState();
    }

    /** 
     * @return a text representation + all necessary information encompassed within a single string.
     */
    public synchronized String getBoardText() {
        return canvas.getCurrentBoard();
    }

    /**
     * @return the list of valid puzzles to start and a list of available matches to connect to.
     *         The format of the string should be as follows (dashes indicate newlines):
     *          -  INT an integer representing how many valid puzzles there are
     *          -  ... IDs of valid puzzles, each separated by a newline
     *          -  INT an integer representing how many available matches there are
     *          -  ... IDs of available matches \n descriptions associated with each ID
     */
    public synchronized String getMatches() {
        return canvas.getListOfMatches();
    }
    
    // ========= OBSERVER METHODS ========= // 

    // ========= PUBLIC METHODS ========= //

    /**
     * Parses the user's raw input from the canvas and returns appropriate web protocol extension.
     * @param userInput a valid raw input from user 
     * @return the proper extension for the GET request to send over to the server.
     *  - START player_ID -> /start/playerID
     *  - PLAY match_ID -> /play/player_ID/match_ID
     *  - NEW match_ID puzzle_ID "Description" -> /choose/playerID/matchID/Description
     *      - Description must be alphanumeric
     *  - EXIT -> 
     *      - If the client is in a choose wait or play state: /exit/state/player_ID/match_ID
     *      - Else (Except START): /exit/state
     *  - TRY id word -> /try/player_ID/match_ID/id/word
     *  - CHALLENGE id word -> /challenge/player_ID/match_ID/id/word
     */
    public synchronized String parseUserInput(String userInput) {
        String[] inputStrings = userInput.split(" "); 
        String[] commandInfo = getSubarray(inputStrings, 1);
        String sendString = "";
        
        if (userInput.equals("NEW MATCH") && canvas.getState() == ClientState.SHOW_SCORE) {
            sendString = "/restart/";
            return sendString;
        }

        // Using the appropriate methods to send the request.
        switch (inputStrings[0]) {
        case "PLAY":
            sendString = sendPlay(commandInfo);
            break;
        case "NEW":
            sendString = sendChoose(commandInfo);
            break;
        case "TRY":
            sendString = sendTry(commandInfo);
            break;
        case "CHALLENGE":
            sendString = sendChallenge(commandInfo);
            break;
        case "EXIT":
            sendString = sendExit(commandInfo);
            break;
        case "START":
            sendString = sendStart(commandInfo);
            break;
        default:
            throw new IllegalArgumentException();
        }
        
        checkRep();
        return sendString;
    }

    /**
     * Parses the response from the server and updates the canvas/client accordingly
     * @param response a valid response from the server as defined by @link[receiveStart(), receiveChoose(), receiveWait(), receivePlay(), receiveEnd()]
     * @param lastInput the player's last input
     * @throws IOException if receiveWait cannot properly wait - parsed response is not correct, or closed incorrectly
     */
    public void parseResponse(String response, String lastInput) throws IOException {
        
        String[] splitResponse = response.split("\n");
        String[] rest = getSubarray(splitResponse, 1);

        switch (splitResponse[0]) {
        case "start":
            receiveStart(rest);
            repaint();
            break;
        case "choose":
            receiveChoose(rest, lastInput);
            repaint();
            break;
        case "wait":
            receiveWait(lastInput);
                repaint();
            break;
        case "play":
            receivePlay(rest, lastInput);
            repaint();
            break;
        case "show_score":
            receiveEnd(rest);
            repaint();
            break;
        default:
            throw new RuntimeException("Should never reach here");
        }
        checkRep();
    }

    /**
     * Refreshes the GUI
     */
    public synchronized void repaint() {
        SwingUtilities.invokeLater(() -> {canvas.repaint();});
        checkRep();
    }

    // ========= PUBLIC METHODS ========= //

    // NOTE: commas within RECEIVES or SEND comments indicate a newline

    /**
     * Parses a valid start response from the server and updates the GUI accordingly.
     * @param response the substring of the response from the server after "start" split along newlines.
     * 
     * RECEIVES: 
     *  - "start", "new game" 
     *  - "start", "try again"
     */
    private synchronized void receiveStart(String[] response) {
        String startState = response[0];
        canvas.setRequest(ClientState.START, startState);
        checkRep();
    }

    /**
     * Receives a valid choose response from the server and updates the GUI accordingly.
     * @param response the substring of the response from the server after "choose" split along newlines.
     * @param lastInput the last input of the player.
     * 
     * RECEIVES: 
     *  - "choose", "new", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     *  - "choose", "update", allMatches
     *  - "choose", "try again", allMatches
     */
    private synchronized void receiveChoose(String[] response, String lastInput) {

        int lineCount = 0;

        // Set the state of the canvas
        String chooseState = response[lineCount];
        canvas.setRequest(ClientState.CHOOSE, chooseState);
        lineCount++;
        
        // Set the player ID
        if (chooseState.equals("new")) {
            playerID = lastInput.split(" ")[1];
        }

        String puzzleMatchString = "";

        // Parsing through available puzzles
        String numberOfNew = response[lineCount];
        puzzleMatchString += numberOfNew + "\n";
        lineCount++;
        for (int i = 0; i < Integer.valueOf(numberOfNew); i++) {
            puzzleMatchString += response[lineCount] + "\n";
            lineCount++;
        }

        // Parsing through available matches
        String numberOfCurrent = response[lineCount];
        lineCount++;

        puzzleMatchString += numberOfCurrent + "\n";
        for (int i = 0; i < Integer.valueOf(numberOfCurrent) * 2; i++) {
            if (i != Integer.valueOf(numberOfCurrent)*2 - 1) {
                puzzleMatchString += response[lineCount] + "\n";
            }
            else {
                puzzleMatchString += response[lineCount];
            }
            lineCount++;
        }
        canvas.setList(puzzleMatchString);
        checkRep();
    }

    /**
     * Receives a valid wait response from the server and updates the GUI accordingly.
     * @param lastInput the last input from the player
     * @throws IOException if receiveWait cannot properly wait - parsed response is not correct, or closed incorrectly
     * 
     * RECEIVES:
     *  - "wait"
     */
    private void receiveWait(String lastInput) throws IOException {
        synchronized (thisLock) {
            canvas.setRequest(ClientState.WAIT, "");
            matchID = lastInput.split(" ")[1];
            this.repaint(); 
        }

        URL waitResponse = new URL("http://" + host + ":" + port + "/waitforjoin/" + getUserID() + "/" + getMatchID());
        BufferedReader joinedBuffer = new BufferedReader(new InputStreamReader(waitResponse.openStream(), UTF_8));
        // Get the response into one big line then parse it
        String joinedResponse = ClientManager.receiveResponse(joinedBuffer);


        // Make sure that you didn't change the state
        synchronized (thisLock) {
            if (getState() == ClientState.WAIT) {
                this.parseResponse(joinedResponse, lastInput);
                joinedBuffer.close(); 
            }
        }
        checkRep();
    }

    /**
     * Receives a valid update to the board and updates the GUI accordingly.
     * @param response the substring of the response from the server after "play" split along newlines.
     * @param lastInput the last input of the player.
     * 
     * RECEIVES:
     *  - "play", "new", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *  - "play", "update", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *  - "play", "wrong_id", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board 
     *  - "play", "incorrect_length", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *  - "play", "inconsistent_current", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *  - "play", "success", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board   
     *  - "play", "wonch", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board 
     *  - "play", "lostch", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *  - "play", "invalidch", playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *  
     *  Should only receive true/false when challenge. New should only be sent on initial CHOOSE/PLAY request. Otherwise, always update.
     */
    private synchronized void receivePlay(String[] response, String lastInput) {
        int lineCount = 0;

        // Set the state of the canvas
        String chooseState = response[lineCount];
        canvas.setRequest(ClientState.PLAY, chooseState);
        lineCount++;

        if (chooseState.equals("new")) {
            matchID = lastInput.split(" ")[1];
        }

        // Set the board of the game
        String boardString = parseBoard(getSubarray(response, lineCount));
        canvas.setBoard(boardString);

        this.notifyAll();
        checkRep();
    }

    /**
     * Receives a valid show_score repsonse from the server and updates the GUI accordingly.
     * @param response the substring of the response from the server after "show_score" split along newlines.
     * 
     * RECEIVES: "show_score", winner, myPlayer, score, challengePts, otherPlayer, score2, challengePts2
     */
    private synchronized void receiveEnd(String[] response) {
        int lineCount = 0;

        // Set the state of the canvas
        String winner = response[lineCount];
        canvas.setRequest(ClientState.SHOW_SCORE, winner);
        lineCount++;

        String endString = "";

        for (int i = 0; i < END_PLAYER_LINES; i++) {
            endString += response[lineCount] + "\n";
            lineCount++;
        }
        canvas.setScore(endString);
        checkRep();
    }

    /**
     * A method that takes in a valid START command in the form of: START player_ID
     * and returns a string representing the extension to the URL to a CrosswordExtravaganza server.
     * The client must be on the START state (as defined in the project handout) to run this method.
     * @param inputStrings the input from the player
     * @return a string with the format of: /start/player_ID 
     */
    private synchronized String sendStart(String[] inputStrings) {  
        String sendString = "";
        if (canvas.getState() == ClientState.START 
                && inputStrings.length == 1
                && inputStrings[0].matches("^[a-zA-Z0-9]+$") ) {
            sendString = "/start/" + inputStrings[0];
        }
        else {
            throw new IllegalArgumentException();
        }
        checkRep();
        return sendString;
    }

    /**
     * A method that takes in a valid NEW command in the form of: NEW match_ID puzzle_ID "Description"
     * and returns a string representing the extension to the URL to a CrosswordExtravaganza server.
     * The client must be on the CHOOSE state (as defined in the project handout) to run this method.
     * @param inputStrings the input from the player
     * @param lastInput the last input from the player
     * @return a string with the format of: /choose/player_ID/match_ID/puzzle_ID/description
     */
    private synchronized String sendChoose(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.CHOOSE 
                && inputStrings.length == CHOOSE_INPUT_LENGTH 
                && inputStrings[0].matches("^[a-zA-Z0-9]+$")
                && inputStrings[1].matches("^[a-zA-Z0-9.]+$")
                && inputStrings[2].matches("\"[a-zA-Z0-9]+\"")) {
            sendString = "/choose/" + playerID + "/" + inputStrings[0] + "/" + inputStrings[1] + "/" + inputStrings[2].replaceAll("\"", "");
        }
        else {
            throw new IllegalArgumentException();
        }
        checkRep();
        return sendString;
    }

    /**
     * A method that takes in a valid PLAY command in the form of: PLAY match_ID 
     * and returns a string representing the extension to the URL to a CrosswordExtravaganza server.
     * The client must be on the PLAY state (as defined in the project handout) to run this method.
     * @param inputStrings the input from the player
     * @param lastInput the last input from the player
     * @return a string with the format of: /play/player_ID/match_ID
     */
    private synchronized String sendPlay(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.CHOOSE 
                && inputStrings.length == 1
                && inputStrings[0].matches("^[a-zA-Z0-9]+$")) {
            sendString = "/play/" + playerID + "/" + inputStrings[0];
        }
        else {
            throw new IllegalArgumentException();
        }
        checkRep();
        return sendString;
    }

    /**
     * A method that takes in a valid EXIT command in the form of: EXIT
     * and returns a string representing the extension to the URL to a CrosswordExtravaganza server.
     * The client must be on any of CHOOSE, WAIT PLAY, or SEND SCORE states 
     * (as defined in the project handout) to run this method.
     * @param inputStrings the input from the player
     * @param lastInput the last input from the player
     * @return a string with the format of: 
     *  - /exit/state/player_ID/match_ID (if there is an active game or is waiting for another player to join)
     *  - /exit/state/player_ID (if the player is currently viewing the list of games to play or at the end screen)
     */
    private synchronized String sendExit(String[] inputStrings) {
        String sendString = "";
        if (inputStrings.length == 0) {
            if (canvas.getState() == ClientState.WAIT || canvas.getState() == ClientState.PLAY) {
                sendString = "/exit/" + canvas.getState().toString().toLowerCase() + "/" + playerID + "/" + matchID;
            }
            else if (canvas.getState() == ClientState.CHOOSE || canvas.getState() == ClientState.SHOW_SCORE){
                sendString = "/exit/" + canvas.getState().toString().toLowerCase() + "/" + playerID;
                exit = true;
            }
        }
        else {
            throw new IllegalArgumentException();
        }

        checkRep();
        return sendString;
    }

    /**
     * A method that takes in a valid TRY command in the form of: TRY id word
     * and returns a string representing the extension to the URL to a CrosswordExtravaganza server.
     * The client must be on the PLAY state (as defined in the project handout) to run this method.
     * @param inputStrings the input from the player
     * @param lastInput the last input from the player
     * @return a string with the format of: /try/playerID/matchID/wordID/word
     */
    private synchronized String sendTry(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.PLAY 
                && inputStrings.length == 2
                && inputStrings[0].matches("^\\d+$")
                && inputStrings[1].matches("^[A-Za-z-]+$")) { 
            sendString = "/try/" + playerID + "/" +  matchID + "/" + inputStrings[0] + "/" + inputStrings[1];
        }
        else {
            throw new IllegalArgumentException();
        }
        checkRep();
        return sendString;
    }

    /**
     * A method that takes in a valid CHALLENGE command in the form of: TRY id word
     * and returns a string representing the extension to the URL to a CrosswordExtravaganza server.
     * The client must be on the PLAY state (as defined in the project handout) to run this method.
     * @param inputStrings the input from the player
     * @param lastInput the last input from the player
     * @return a string with the format of: /challenge/playerID/matchID/wordID/word
     */
    private synchronized String sendChallenge(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.PLAY 
                && inputStrings.length == 2
                && inputStrings[0].matches("^\\d+$") 
                && inputStrings[1].matches("^[A-Za-z-]+$")) { 
            sendString = "/challenge/" + playerID + "/" +  matchID + "/" + inputStrings[0] + "/" + inputStrings[1];
        }
        else {
            throw new IllegalArgumentException();
        }
        checkRep();
        return sendString;
    }

    /**
     * Parses the board into a single string
     * @param boardArray an array containing each line of the board
     * @return board as a single string, each newline separated by "\n"
     */
    private static String parseBoard(String[] boardArray) {
        String boardString = "";

        for (int i = 0; i < boardArray.length; i++) {
            if (i != boardArray.length - 1) {
                boardString += boardArray[i] + "\n";
            }
            else {
                boardString += boardArray[i];
            }
        }
        return boardString;
    }

    /**
     * Returns the subarray starting at some index start to the end of the array.
     * Obtained from: https://www.techiedelight.com/get-subarray-array-specified-indexes-java/
     */
    private static String[] getSubarray(String[] input, int start) {
        return IntStream.range(start, input.length).mapToObj(i -> input[i]).toArray(String[]::new);
    }


}
