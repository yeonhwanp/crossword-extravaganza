/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.BreakIterator;
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

    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 900;
    private static final int BOARD_PLAYER_LINES = 8;
    private boolean validInput = false;
    private String playerID;
    private String matchID;
    private String userInput;
    private CrosswordCanvas canvas = new CrosswordCanvas();
    
    // For the lock 
    private final Client thisLock = this;

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
    
    /*
     * Concurrency Design Comment:
     *  We are currently using two threads. One thread to process input by the user (and in essence, the sending
     *  and receiving of data associated with that request) and another thread to process active and live updates
     *  to the client's GUI. 
     * 
     *  We know that this is threadsafe because the two threads are never accessing the same variables, and while the
     *  canvas is the one thing shared by the two threads, it is ok because the methods that have access to the canvas are
     *  locked to this object.
     * 
     * 
     */

    /**
     * Check for valid Client rep
     */
    private void checkRep() {
        assert playerID.matches("^[a-zA-Z0-9]+$");
        assert matchID.matches("^[a-zA-Z0-9]+$");
    }
    
    public String getUserInput() {
        return userInput;
    }

    /**
     * Starter code to display a window with a CrosswordCanvas,
     * a text box to enter commands and an Enter button.
     * 
     * @param matchStr toString of client view of a match. Use this to display the puzzle, its hints, and any extra info.
     */
    public synchronized void launchGameWindow() {

        canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        JTextField textbox = new JTextField(30);
        textbox.setFont(new Font("Arial", Font.BOLD, 20));

        // Upon enter, want to load into sendString and prompt the main thread to send to the server
        JButton enterButton = new JButton("Enter");
        enterButton.addActionListener((event) -> {

            // This code executes every time the user presses the Enter
            // button. Recall from reading 24 that this code runs on the
            // Event Dispatch Thread, which is different from the main
            // thread.
            synchronized (thisLock) {
                userInput = textbox.getText();
                textbox.setText("");
                thisLock.notifyAll();
            }
        });

        enterButton.setSize(10, 10);

        JFrame window = new JFrame("Crossword Client");
        window.setLayout(new BorderLayout());
        window.add(canvas, BorderLayout.CENTER);

        JPanel contentPane = new JPanel();
        contentPane.add(textbox);
        contentPane.add(enterButton);

        window.add(contentPane, BorderLayout.SOUTH);

        window.setSize(CANVAS_WIDTH + 50, CANVAS_HEIGHT + 50);

        window.getContentPane().add(contentPane);

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
    
    /**
     * Constructs the response into one big string, properly formatted with newlines like it should be.
     */
    public String getResponse(BufferedReader response) throws IOException {
        String fullString = "";
        String line;
        while ((line = response.readLine()) != null) {
           fullString += line + "\n";
        }
        return fullString;
    }
    
    public String parseUserInput(String[] inputStrings) {
        
        String sendString;
        // Using the appropriate methods to send the request.
        switch (inputStrings[0]) {
        case "PLAY":
            sendString = sendPlay(inputStrings);
            break;
        case "NEW":
            sendString = sendChoose(inputStrings);
            break;
        case "TRY":
            sendString = sendTry(inputStrings);
            break;
        case "CHALLENGE":
            sendString = sendChallenge(inputStrings);
        case "EXIT":
            if (inputStrings.length == 1) {
                sendExit();
                validInput = true;
            }
            else {
                validInput = false;
                paintInvalidInput();
            }
            // New connect state
        default:
            sendString = sendStart();
            break;
        }
        
        return sendString;
    }

    /**
     * parses the string and does something
     */
    public synchronized void parseRequest(String response) throws IOException {
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
     * Receives a start response from the server and parses it into the canvas.
     * 
     * RECEIVES: 
     *  - START, "NEW GAME" 
     *  - START, "TRY AGAIN"
     */
    public synchronized void receiveStart(String[] response) throws IOException {
        String showState = response[1];
        canvas.setRequest("start", showState);
    }

    /**
     * Receives a choose response from the server and parses it into the canvas.
     * 
     * RECEIVES: 
     *  - CHOOSE, "NEW", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     *  - CHOOSE, "TRY AGAIN", allMatches
     */
    public synchronized void receiveChoose(String[] response) throws IOException {
        
        int lineCount = 0;

        // Set the state of the canvas
        String chooseState = response[lineCount];
        canvas.setRequest("choose", chooseState);
        lineCount++;

        // Set the player ID
        if (chooseState.equals("NEW")) {
            playerID = userInput;
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
     *  - WAIT, "WAITING"
     */
    public synchronized void receiveWait() {
        canvas.setRequest("wait", "");
        matchID = userInput;
    }

    /**
     * RECEIVES:
     *  - PLAY, new, board
     *  - PLAY, true, board
     *  - PLAY, false, board
     */
    public synchronized void receivePlay(String[] response) {
        
        int lineCount = 0;

        // Set the state of the canvas
        String chooseState = response[lineCount];
        canvas.setRequest("play", chooseState);
        lineCount++;

        if (chooseState.equals("new")) {
            matchID = userInput;
        }

        // Set the board of the game
        String boardString = parseBoard(getSubarray(response, lineCount));
        canvas.setBoard(boardString);

        this.notifyAll();
    }

    /**
     * RECEIVES: SHOW_SCORE, winner, board
     */
    public synchronized void receiveEnd(String[] response) {
        canvas.setRequest("show_score", "");
    }

    /**
     * SENDS: /start/playerID
     */
    public synchronized String sendStart() {  
        String sendString = "";
        if (canvas.getState() == "START" && !userInput.equals("")) {
            sendString = "/start/" + userInput;
            validInput = true;
        }
        else {
            validInput = false;
        }
        return sendString;
    }

    /**
     * SENDS: /choose/playerID/matchID/puzzleID/description
     */
    public synchronized String sendChoose(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == "CHOOSE" && inputStrings.length == 4) {
            sendString = "/new/" + playerID + "/" + inputStrings[1] + "/" + inputStrings[2] + "/" + inputStrings[3];
            validInput = false;
        }
        else {
            validInput = true;
        }
        return sendString;
    }

    /**
     * if current client state is in the wait or play state:
     *  SENDS: /exit/state/matchID
     * else:
     *  SENDS: /exit/state
     */
    public synchronized String sendExit() {
        String sendString = "";
        if (canvas.getState() == "WAIT" || canvas.getState() == "PLAY") {
            sendString = "/exit/" + canvas.getState().toLowerCase() + "/" + matchID;
        }
        else {
            sendString = "/exit/" + canvas.getState().toLowerCase();
        }
        return sendString;
    }

    /**
     * SENDS: /play/playerID/matchID
     */
    public synchronized String sendPlay(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == "PLAY" && inputStrings.length == 2) {
            sendString = "/play/" + playerID + "/" + inputStrings[1];
        }
        else {
            validInput = false;
        }
        return sendString = "";
    }



    /**
     * SENDS: /TRY/PLAYERID/MATCHID/WORDID/WORD
     */
    public synchronized String sendTry(String[] inputStrings) {
        String sendString;
        if (canvas.getState() == "PLAY" && inputStrings.length == 3) {
            sendString = "/try/" + playerID + "/" +  matchID + "/" + inputStrings[1] + "/" + inputStrings[2];
        }
        else {
            validInput = false;
        }
        return sendString = "";
    }

    /**
     * SENDS: /CHALLENGE/PLAYERID/MATCHID/WORDID/WORD
     */
    public synchronized String sendChallenge(String[] inputStrings) {
        String sendString = "";
        if (canvas.getState() == "PLAY" && inputStrings.length == 3) {
            sendString = "/challenge/" + playerID + "/" +  matchID + "/" + inputStrings[1] + "/" + inputStrings[2];
        }
        else {
            validInput = false;
        }
        return sendString;
    }


    /**
     * Tells the user that there is an invalid input. 
     */
    public synchronized void paintInvalidInput() {
    }


    /**
     * Parses the board
     * @param socketIn
     * @return
     * @throws IOException
     */
    public static String parseBoard(String[] boardArray) {

        int lineCount = 0;
        String boardString = "";

        String[] dimensions = boardArray[lineCount].split("x");
        lineCount++;
        
        for (int i = 0; i < Integer.valueOf(dimensions[0]); i++) {
            boardString += boardArray[lineCount] + "\n";
            lineCount++;
        }

        String numberOfWords = boardArray[lineCount];
        lineCount++;
        
        for (int i = 0; i < Integer.valueOf(numberOfWords); i++) {
            boardString += boardArray[lineCount] + "\n";
            lineCount++;
        }

        for (int i = 0; i < BOARD_PLAYER_LINES; i++) {
            if (i != BOARD_PLAYER_LINES - 1) {
                boardString += boardArray[lineCount] + "\n";
            }
            else {
                boardString += boardArray[lineCount];
            }
            lineCount++;
        }
        return boardString;
    }
    
    /**
     * Returns the subarray starting at some index start to the end of the array.
     * Obtained from: https://www.techiedelight.com/get-subarray-array-specified-indexes-java/
     */
    public String[] getSubarray(String[] input, int start) {
        return IntStream.range(start, input.length+1).mapToObj(i -> input[i]).toArray(String[]::new);
    }


}
