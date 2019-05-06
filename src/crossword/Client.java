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
    private String sendString;
    private CrosswordCanvas canvas = new CrosswordCanvas();
    
    
    /*
     * Abstraction Function
     * AF(sendString, canvas) = client object that uses canvas to display and take in information.
     *      sendString represents the information that is sent in by the client
     * 
     * Rep Invariant:
     *  true (for now, until we implement the rest of the game)
     * 
     * Safety from Rep Exposure:
     *  TODO
     *  
     * Thread safety argument:
     *  TODO
     * 
     */
    

    /**
     * Check for valid Client rep
     */
    private void checkRep() {
        assert sendString != null;
        assert canvas != null;
                
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
        // Take the args and make it into a linked list
        final Queue<String> arguments = new LinkedList<>(List.of(args));
        
        // Stuff that we need
        final String host;
        final int port;
        
        // Create host/port by using try/except blocks
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
        
        final URL loadRequest = new URL("http://" + host + ":" + port + "/init/");
        
        // Create a new connection
        try (
                Socket socket = new Socket(host, port);
                InputStream stream = loadRequest.openStream();
                BufferedReader socketIn = new BufferedReader(new InputStreamReader(stream, UTF_8));
                PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8), true);
                BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            
//            // Creating the board
//            String wholeString = "";
//            String dimensions = socketIn.readLine();
//            wholeString += dimensions + "\n";
//            for (int i = 0; i < Integer.valueOf(dimensions.split("x")[0]); i++) {
//                wholeString += socketIn.readLine() + "\n";
//            }
//            String numWords = socketIn.readLine();
//            wholeString += numWords + "\n";
//            int numCount = 2 * Integer.valueOf(numWords);
//            for (int i = 0; i < numCount; i++) {
//                wholeString += socketIn.readLine() + "\n";
//            }
//            canvas.setCanvas(wholeString);
            // Creating the board
            launchGameWindow();
            
            while ( ! socket.isClosed()) {
                
                // Wait until we get notified by enter button
                try {
                    this.wait();
                }  catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // If the socket is closed then break because we don't have a connection
                if (socket.isClosed()) {
                    break;
                }
                
                // Send the input
                socketOut.println(sendString);
                readAndPrintBoard(socketIn, System.out);
            }
            System.out.println("connection closed");
        }

    }
    
    /**
     * Reads and prints the board
     * @param in stream for reading a text-protocol RESPONSE, closed on end-of-stream
     * @param out stream for printing a formatted board
     * @param showRaw if true, include raw lines read from in
     * @throws IOException if an error occurs communicating with the server
     */
    private static void readAndPrintBoard(BufferedReader in, PrintStream out) throws IOException {
        
        // If the response is null, connection was closed
        final String message = in.readLine();
        if (message == null) {
            in.close();
            return;
        }
        
        // But if it's not, then we want to parse the board and display it.
        final String[] sizeAndBoard = message.split(" ", 2);
        final String[] size = sizeAndBoard[0].split("x");
        final int rows = Integer.parseInt(size[0]);
        final int cols = Integer.parseInt(size[1]);
        final List<String> board = List.of(sizeAndBoard[1].split(" "));
        
        final int width = board.stream().mapToInt(Client::countCharacters)
                                        .max().getAsInt() + 1;
        for (int row = 0; row < rows; row++) {
            out.print("|");
            for (String spot : board.subList(row*cols, row*cols + cols)) {
                out.format("%" + (width - countCharacters(spot)) + "s", " ");
                out.print(spot);
            }
            out.println();
        }
    }
    
    /**
     * Counts the number of characters using locations of boundaries in the text
     * @param text text to count the number of characters
     * @return the number of characters using locations of boundaries in the text
     */
    private static int countCharacters(String text) {
        final BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);
        int chars = 0;
        while (it.next() != BreakIterator.DONE) { chars++; }
        return chars;
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
                sendString = textbox.getText();
                System.out.println(sendString);
                canvas.repaint();
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
    private void parse(String state) {
        switch (state) {
        case "start":
            break;
        case "choose":
            break;
        case "wait":
            break;
        case "play":
            break;
        case "show_score":
            break;
        }
    }
    
    /**
     * RECEIVES: 
     *  - START, "NEW GAME" 
     *  - START, "TRY AGAIN"
     */
    private void receiveStart() {
        
    }
    
    /**
     * SENDS: /start/playerID
     */
    private void sendStart() {  
    } 
    
    /**
     * RECEIVES: 
     *  - CHOOSE, "NEW"
     *  - CHOOSE, "TRY AGAIN"
     */
    private void receiveChoose() {
    }
    
    /**
     * SENDS: /choose/matchID/puzzleID/description
     */
    private void sendChoose() {
    }
    
    /**
     * RECEIVES:
     *  - WAIT, "WAITING"
     */
    private void receiveWait() {
    }
    
    /**
     * SENDS: /EXIT/STATE
     */
    private void sendExit() {
    }
    
    /**
     * RECEIVES:
     *  - PLAY, board, true
     *  - PLAY, board, false
     */
    private void receivePlay() {
    }
    
    /**
     * SENDS: /TRY/PLAYERID/MATCHID/WORDID/WORD
     */
    private void sendTry() {
    }
    
    /**
     * SENDS: /CHALLENGE/PLAYERID/MATCHID/WORDID/WORD
     */
    private void sendChallenge() {
    }
    
    /**
     * RECEIVES: SHOW_SCORE
     */
    private void receiveEnd() {
    }
    
    
}
