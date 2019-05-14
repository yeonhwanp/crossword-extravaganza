/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

// TODO: check for invalid command inputs
// TODO: Reset matchID if exit or join different game etc.

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    private static final int BOARD_PLAYER_LINES = 8;
    private static final int TEXTFIELD_SIZE = 10;
    private static final int TEXTBOX_FONT_SIZE = 20;
    private static final int ENTERBUTTON_SIZE = 10;
    private static final int CANVAS_ADD = 50;
    private final String host;
    private final int port;
    private String playerID;
    private String matchID;
    private String textboxInput;
    private String lastInput;
    private CrosswordCanvas canvas = new CrosswordCanvas();
    
    // For the lock 
    private final Client thisLock = this;
    
    /**
     * Create a new Client object
     * @param host the host we're connecting to
     * @param port the port we're connecting to
     */
    public Client(String host, int port) { 
        this.host = host;
        this.port = port;
    }

    /*
     * Abstraction Function
     * AF(playerID, matchID, userInput, sendString, canvas) = client represented by the ID playerID
     *                                                        that is playing on the game represented by matchID
     *                                                        on the game GUI canvas that and has just
     *                                                        inputed a string into the GUI represented by userInput
     *                                                        and the current GET request by sendString.
     * 
     * Rep Invariant:
     *  playerID alphanumeric
     *  matchID only contains alphanumeric
     * 
     * Safety from Rep Exposure:
     *  All variables except canvas are private and immutable
     *  Methods take in mutable objects but do not copy them to the rep
     *  Methods never return mutable objects or references to such mutable objects
     *  
     * Thread safety argument:
     *  All of the variables within the class are ever only touched by one thread
     *  playerID, matchID, userInput, sendString, validInput, are handled by thread one
     *  canvas is touched by both threads but the methods that touch it are synchornized to an internal rep
     *      such that two threads cannot get or modify the canvas at the same time.
     * 
     */
    
    private void checkRep() {
        assert playerID.matches("^[a-zA-Z0-9]+$");
        assert matchID.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Display a window with a CrosswordCanvas, a text box to enter commands, and an Enter button.
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
            synchronized (thisLock) {
                textboxInput = textbox.getText();
                textbox.setText("");
                thisLock.notifyAll();
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
    }
    
    // ========= OBSERVER METHODS ========= // 
    
    /**
     * @return The input from the user after they've pressed enter on the canvas.
     */
    public synchronized String getUserInput() {
        return textboxInput;
    }
    
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
     * @return the state of the game (client side).
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
     * @return the list of available matches.
     */
    public synchronized String getMatches() {
        return canvas.getListOfMatches();
    }
    
    /**
     * @return for testing
     */
    public synchronized String getRequestState() {
        return canvas.getRequestState();
    }
    
    /**
     * @return last input
     */
    public synchronized String getLastInput() {
        return lastInput;
    }
    
    // ========= OBSERVER METHODS ========= // 
    
    // ========= PUBLIC METHODS ========= //
    
    /**
     * Parses the user's raw input from the canvas and returns appropriate web protocol.
     * @param userInput the user's raw input from the canvas textbox
     * @return the proper extension for the GET request to send over to the server
     */
    public synchronized String parseUserInput(String userInput) {
        
        lastInput = userInput;
        
        String[] inputStrings = userInput.split(" "); 
        String[] commandInfo = getSubarray(inputStrings, 1);
        String sendString = "";
        
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
            throw new RuntimeException("User input error. Should never reach here.");
        }
        
        return sendString;
    }

    /**
     * Parses the response from the server and updates the canvas/client accordingly
     * @param response the response form the server
     * @throws IOException 
     */
    public synchronized void parseResponse(String response) throws IOException {
        String[] splitResponse = response.split("\n");
        String[] rest = getSubarray(splitResponse, 1);
        
        switch (splitResponse[0]) {
        case "start":
            receiveStart(rest);
            break;
        case "choose":
            receiveChoose(rest);
            break;
        case "wait":
            receiveWait();
            break;
        case "play":
            receivePlay(rest);
            break;
        case "show_score":
            receiveEnd(rest);
            break;
        default:
            throw new RuntimeException("Should never reach here");
        }
    }
    
    /**
     * Refreshes the canvas
     */
    public synchronized void repaint() {
        canvas.repaint();
    }
    
    // ========= PUBLIC METHODS ========= //

    /**
     * Receives a start response from the server and parses it into the canvas.
     * 
     * RECEIVES: 
     *  - START, "NEW GAME" 
     *  - START, "TRY AGAIN"
     */
    private synchronized void receiveStart(String[] response) {
        String startState = response[0];
        canvas.setRequest("start", startState);
    }

    /**
     * Receives a choose response from the server and parses it into the canvas.
     * 
     * RECEIVES: 
     *  - CHOOSE, "NEW", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     *  - CHOOSE, "TRY AGAIN", allMatches
     */
    private synchronized void receiveChoose(String[] response) {
        
        int lineCount = 0;

        // Set the state of the canvas
        String chooseState = response[lineCount];
        canvas.setRequest("choose", chooseState);
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
    }

    /**
     * RECEIVES:
     *  - WAIT
     * @throws IOException 
     */
    private void receiveWait() throws IOException {
        synchronized (thisLock) {
            canvas.setRequest("wait", "");
            matchID = lastInput.split(" ")[1];
            this.repaint(); 
        }
        
        // Send back response
        URL waitResponse = new URL("http://" + host + ":" + port + "/waitforjoin/" + getUserID() + "/" + getMatchID());
        BufferedReader joinedBuffer = new BufferedReader(new InputStreamReader(waitResponse.openStream(), UTF_8));

        // Get the response into one big line then parse it
        String joinedResponse = ClientManager.receiveResponse(joinedBuffer);
        synchronized (thisLock) {
            this.parseResponse(joinedResponse);
            joinedBuffer.close(); 
        }
    }

    /**
     * RECEIVES:
     *  - PLAY, new, board
     *  - PLAY, update, board
     *  - PLAY, true, board
     *  - PLAY, false, board
     */
    private synchronized void receivePlay(String[] response) {
        int lineCount = 0;

        // Set the state of the canvas
        String chooseState = response[lineCount];
        canvas.setRequest("play", chooseState);
        lineCount++;

        if (chooseState.equals("new")) {
            matchID = lastInput.split(" ")[1];
        }

        // Set the board of the game
        String boardString = parseBoard(getSubarray(response, lineCount));
        canvas.setBoard(boardString);

        this.notifyAll();
    }

    /**
     * RECEIVES: SHOW_SCORE, winner, board
     */
    private synchronized void receiveEnd(String[] response) {
        canvas.setRequest("show_score", "");
    }

    /**
     * SENDS: /start/playerID
     */
    private synchronized String sendStart(String[] inputStrings) {  
        String sendString = "";
        if (canvas.getState() == ClientState.START && !lastInput.equals("")) {
            sendString = "/start/" + inputStrings[0];
        }
        else {
            throw new RuntimeException("Wrong start format.");
        }
        return sendString;
    }
    
    /**
     * SENDS: /choose/playerID/matchID/puzzleID/description
     */
    private synchronized String sendChoose(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.CHOOSE && inputStrings.length == 3) {
            sendString = "/choose/" + playerID + "/" + inputStrings[0] + "/" + inputStrings[1] + "/" + inputStrings[2].replaceAll("\"", "");
        }
        else {
            throw new RuntimeException("Wrong new format.");
        }
        return sendString;
    }
    
    /**
     * SENDS: /play/playerID/matchID
     */
    private synchronized String sendPlay(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.CHOOSE && inputStrings.length == 1) {
            sendString = "/play/" + playerID + "/" + inputStrings[0];
        }
        else {
            throw new RuntimeException("Wrong play format.");
        }
        return sendString;
    }

    /**
     * if current client state is in the wait or play state:
     *  SENDS: /exit/state/matchID
     * else:
     *  SENDS: /exit/state
     */
    private synchronized String sendExit(String[] inputStrings) {
        String sendString = "";
        if (inputStrings.length == 0) {
            if (canvas.getState() == ClientState.WAIT || canvas.getState() == ClientState.PLAY) {
                sendString = "/exit/" + canvas.getState().toString().toLowerCase() + "/" + matchID;
            }
            else {
                sendString = "/exit/" + canvas.getState().toString().toLowerCase();
            }
        }
        else {
            throw new RuntimeException("Wrong exit format");
        }
        
        return sendString;
    }

    /**
     * SENDS: /TRY/PLAYERID/MATCHID/WORDID/WORD
     */
    private synchronized String sendTry(String[] inputStrings) {
        String sendString = "";
        System.out.println(canvas.getState() + " STATE");
        if (canvas.getState() == ClientState.PLAY && inputStrings.length == 2) {
            sendString = "/try/" + playerID + "/" +  matchID + "/" + inputStrings[0] + "/" + inputStrings[1];
        }
        else {
            throw new RuntimeException("Wrong try format.");
        }
        return sendString;
    }

    /**
     * SENDS: /CHALLENGE/PLAYERID/MATCHID/WORDID/WORD
     */
    private synchronized String sendChallenge(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == ClientState.PLAY && inputStrings.length == 2) {
            sendString = "/challenge/" + playerID + "/" +  matchID + "/" + inputStrings[0] + "/" + inputStrings[1];
        }
        else {
            throw new RuntimeException("Wrong challenge format.");
        }
        return sendString;
    }

    /**
     * Parses the board
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
    
    //TODO: some method to tell the user that they've inputed something invalid
    
    /**
     * Returns the subarray starting at some index start to the end of the array.
     * Obtained from: https://www.techiedelight.com/get-subarray-array-specified-indexes-java/
     */
    private static String[] getSubarray(String[] input, int start) {
        return IntStream.range(start, input.length).mapToObj(i -> input[i]).toArray(String[]::new);
    }


}
