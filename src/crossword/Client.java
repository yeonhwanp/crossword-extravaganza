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
    private boolean ongoingGame = true;
    private String playerID;
    private String matchID;
    private String userInput;
    private String sendString;
    private CrosswordCanvas canvas = new CrosswordCanvas();

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


    /**
     * Check for valid Client rep
     */
    private void checkRep() {
        assert playerID.matches("^[a-zA-Z0-9]+$");
        assert matchID.matches("^[a-zA-Z0-9]+$");

    }

    /**
     * Start a Crossword Extravaganza client.
     * 
     * Given the server address, connect to the server. The server will send over a client's view of a match (toString), which holds
     * length, position, and orientation of the words, as well as their associated hint.
     * 
     * Then, the client should display this information as a puzzle. The information is displayed via CrosswordCanvas
     * 
     * @param args command line arguments that should include only the server address.
     * @throws IOException if client cannot properly connect to the server.
     * @throws UnknownHostException if the server is unknown host.
     */
    public static void main(String[] args) throws UnknownHostException, IOException {

        // Create a new client object and have it connect
        Client thisClient = new Client();
        thisClient.connectToServer(args);

    }

    /**
     * Connects to server, sends requests and receives responses from the server.
     * @param args command line arguments that should include only the server address.
     * @throws UnknownHostException if the server/socket is unknown host.
     * @throws IOException if we cannot connect with URL, or by socket.
     */
    private synchronized void connectToServer(String[] args) throws UnknownHostException, IOException {

        // ========= PARSING LAUNCH ARGUMENTS ========= //
        final Queue<String> arguments = new LinkedList<>(List.of(args));
        final String host;
        final int port;
        try {
            host = arguments.remove();
        } catch (NoSuchElementException nse) {
            throw new IllegalArgumentException("missing HOST", nse);
        }
        try {
            port = Integer.parseInt(arguments.remove());
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IllegalArgumentException("missing or invalid PORT", e);
        }
        // ========= PARSING LAUNCH ARGUMENTS ========= //

        // Send initial GET request and parse the response
        final URL loadRequest = new URL("http://" + host + ":" + port + "/init/");
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(loadRequest.openStream(), UTF_8));
        String state = socketIn.readLine();
        parseRequest(state, socketIn);
        socketIn.close();
        launchGameWindow();

        // Thread to handle outgoing messages. Never want this to end until someone does end
        new Thread(() -> {
            while (true) {
                synchronized(this) { //TODO THIS IS WRONG, use outer class instead
                    // Waiting for button press to send message
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Sending URL stuffs
                    try {

                        // Splitting up the input provided by the user.
                        String[] inputStrings = userInput.split(" ");

                        // Using the appropriate methods to send the request.
                        switch (inputStrings[0]) {
                        case "PLAY":
                            sendPlay(inputStrings);
                            break;
                        case "NEW":
                            sendChoose(inputStrings);
                            break;
                        case "TRY":
                            sendTry(inputStrings);
                            break;
                        case "CHALLENGE":
                            sendChallenge(inputStrings);
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
                            sendStart();
                            break;
                        }

                        if (validInput) {
                            // Send GET request
                            URL test = new URL("http://" + host + ":" + port + sendString);
                            BufferedReader response = new BufferedReader(new InputStreamReader(test.openStream(), UTF_8));

                            // Parse response then close stream
                            String newState = response.readLine();
                            System.out.println(newState);
                            parseRequest(newState, response);
                            response.close();
                        }

                        else {
                            paintInvalidInput(); 
                        }

                        canvas.repaint();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // Thread to handle watches
        new Thread(() -> {
            while (true) { 
                URL test;
                try {
                    test = new URL("http://" + host + ":" + port + sendString);
                    BufferedReader response = new BufferedReader(new InputStreamReader(test.openStream(), UTF_8));
                    
                    String watchState = response.readLine();
                    parseRequest(watchState, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                canvas.repaint();
            }
        }).start();
    }
    
    /**
     * @return the playerID
     */
    public String getPlayerID() {
        throw new RuntimeException("Not implemented yet!");
    }
    
    /**
     * @return return the current match that the client is a part of
     */
    public String getMatchID() {
        throw new RuntimeException("Not implemented yet!");
    }
    
    /**
     * @return the user's last input into the GUI
     */
    public String getUserInput() {
        throw new RuntimeException("Not implemented yet!");  
    }
    
    /**
     * @return the extension to the URL (get request)
     */
    public String getSendString() {
        throw new RuntimeException("Not implemented yet!");
    }
    
    /**
     * @return the list of all legitimate puzzle IDs as well as ongoing matchIDs that only have one player
     */
    public synchronized String getMatchList() {
        return canvas.getListOfMatches();
    }

    private void handleInputs(BufferedReader socketIn) {
    }

    /**
     * Starter code to display a window with a CrosswordCanvas,
     * a text box to enter commands and an Enter button.
     * 
     * @param matchStr toString of client view of a match. Use this to display the puzzle, its hints, and any extra info.
     */
    private synchronized void launchGameWindow() {

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
            synchronized (this) {
                userInput = textbox.getText();
                textbox.setText("");
                this.notifyAll();
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
     * parses the string and does something
     */
    private synchronized void parseRequest(String state, BufferedReader socketIn) throws IOException {
        switch (state) {
        case "start":
            receiveStart(socketIn);
            break;
        case "choose":
            receiveChoose(socketIn);
            break;
        case "wait":
            receiveWait(socketIn);
            break;
        case "play":
            receivePlay(socketIn);
            break;
        case "show_score":
            receiveEnd(socketIn);
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
     *  
     * @param socketIn The input stream from the server
     * @throws IOException 
     */
    public synchronized void receiveStart(BufferedReader socketIn) throws IOException {
        String showState = socketIn.readLine();
        canvas.setRequest("start", showState);
    }

    /**
     * Receives a choose response from the server and parses it into the canvas.
     * 
     * RECEIVES: 
     *  - CHOOSE, "NEW", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     *  - CHOOSE, "TRY AGAIN", allMatches
     *  
     * @param socketIn The input stream from the server
     * @throws IOException 
     */
    public synchronized void receiveChoose(BufferedReader socketIn) throws IOException {

        // Set the state of the canvas
        String chooseState = socketIn.readLine();
        canvas.setRequest("choose", chooseState);

        System.out.println(userInput);

        // Set the player ID
        if (chooseState.equals("NEW")) {
            playerID = userInput;
        }

        String puzzleMatchString = "";

        // Parsing through available puzzles
        String numberOfNew = socketIn.readLine();
        puzzleMatchString += numberOfNew + "\n";
        for (int i = 0; i < Integer.valueOf(numberOfNew); i++) {
            puzzleMatchString += socketIn.readLine() + "\n";
        }

        // Parsing through available matches
        String numberOfCurrent = socketIn.readLine();
        puzzleMatchString += numberOfCurrent + "\n";
        for (int i = 0; i < Integer.valueOf(numberOfCurrent) * 2; i++) {
            if (i != Integer.valueOf(numberOfCurrent)*2 - 1) {
                puzzleMatchString += socketIn.readLine() + "\n";
            }
            else {
                puzzleMatchString += socketIn.readLine();
            }
        }

        canvas.setList(puzzleMatchString);
    }

    /**
     * RECEIVES:
     *  - WAIT, "WAITING"
     */
    public synchronized void receiveWait(BufferedReader socketIn) {
        canvas.setRequest("wait", "");
        matchID = userInput;
    }

    /**
     * RECEIVES:
     *  - PLAY, new, board
     *  - PLAY, true, board
     *  - PLAY, false, board
     * @throws IOException 
     */
    public synchronized void receivePlay(BufferedReader socketIn) throws IOException {

        // Set the state of the canvas
        String chooseState = socketIn.readLine();
        chooseState += socketIn.readLine();
        canvas.setRequest("play", chooseState);

        if (chooseState.equals("new")) {
            matchID = userInput;
        }

        // Set the board of the game
        String boardString = parseBoard(socketIn);
        canvas.setBoard(boardString);

        this.notifyAll();
    }

    /**
     * RECEIVES: SHOW_SCORE
     */
    public synchronized void receiveEnd(BufferedReader socketIn) {
        canvas.setRequest("show_score", "");
    }

    /**
     * SENDS: /start/playerID
     */
    public synchronized void sendStart() {  
        if (canvas.getState() == "START" && !userInput.equals("")) {
            sendString = "/start/" + userInput;
            validInput = true;
        }
        else {
            validInput = false;
        }
    }

    /**
     * SENDS: /choose/playerID/matchID/puzzleID/description
     */
    public synchronized void sendChoose(String[] inputStrings) {
        if (canvas.getState() == "CHOOSE" && inputStrings.length == 4) {
            sendString = "/new/" + playerID + "/" + inputStrings[1] + "/" + inputStrings[2] + "/" + inputStrings[3];
            validInput = false;
        }
        else {
            validInput = true;
        }
    }

    /**
     * if current client state is in the wait or play state:
     *  SENDS: /exit/state/matchID
     * else:
     *  SENDS: /exit/state
     */
    public synchronized void sendExit() {
        if (canvas.getState() == "WAIT" || canvas.getState() == "PLAY") {
            sendString = "/exit/" + canvas.getState().toLowerCase() + "/" + matchID;
        }
        else {
            sendString = "/exit/" + canvas.getState().toLowerCase();
        }
    }

    /**
     * SENDS: /play/playerID/matchID
     */
    public synchronized void sendPlay(String[] inputStrings) {
        if (canvas.getState() == "PLAY" && inputStrings.length == 2) {
            sendString = "/play/" + playerID + "/" + inputStrings[1];
        }
        else {
            validInput = false;
        }
    }



    /**
     * SENDS: /TRY/PLAYERID/MATCHID/WORDID/WORD
     */
    public synchronized void sendTry(String[] inputStrings) {
        if (canvas.getState() == "PLAY" && inputStrings.length == 3) {
            sendString = "/try/" + playerID + "/" +  matchID + "/" + inputStrings[1] + "/" + inputStrings[2];
        }
        else {
            validInput = false;
        }
    }

    /**
     * SENDS: /CHALLENGE/PLAYERID/MATCHID/WORDID/WORD
     */
    public synchronized void sendChallenge(String[] inputStrings) {
        if (canvas.getState() == "PLAY" && inputStrings.length == 3) {
            sendString = "/challenge/" + playerID + "/" +  matchID + "/" + inputStrings[1] + "/" + inputStrings[2];
        }
        else {
            validInput = false;
        }
    }


    /**
     * Tells the user that there is an invalid input. 
     */
    private synchronized void paintInvalidInput() {
    }


    /**
     * Parses the board
     * @param socketIn
     * @return
     * @throws IOException
     */
    private static String parseBoard(BufferedReader socketIn) throws IOException {

        String boardString = "";

        String[] dimensions = socketIn.readLine().split("x");
        for (int i = 0; i < Integer.valueOf(dimensions[0]); i++) {
            boardString += socketIn.readLine() + "\n";
        }

        String numberOfWords = socketIn.readLine();
        for (int i = 0; i < Integer.valueOf(numberOfWords); i++) {
            boardString += socketIn.readLine() + "\n";
        }

        for (int i = 0; i < BOARD_PLAYER_LINES; i++) {
            if (i != BOARD_PLAYER_LINES - 1) {
                boardString += socketIn.readLine() + "\n";
            }
            else {
                boardString += socketIn.readLine();
            }
        }

        return boardString;
    }


}
