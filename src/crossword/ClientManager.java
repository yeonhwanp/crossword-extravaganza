package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class ClientManager {

    private final Client client;

    /*
     * Abstraction Function: TODO
     * Rep Invariant: TODO
     * Safety from Rep Exposure: TODO
     * Thread safety argument: TODO
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
     */

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
        ClientManager thisClient = new ClientManager();
        thisClient.connectToServer(args);
    }
    
    /**
     * Creates a new ClientManager object
     */
    public ClientManager() {
        client = new Client();
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
        String initialRequest = receiveResponse(socketIn);
        
        client.parseResponse(initialRequest);
        client.launchGameWindow();
        socketIn.close();

        // Thread to handle outgoing messages. Never want this to end until someone does end
        new Thread(() -> {
            while (true) {
                synchronized(client) {
                    
                    // Waiting for button press to send message
                    try {
                        client.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Sending URL stuffs
                    try {
                        String userInput = client.getUserInput();
                        String extension = client.parseUserInput(userInput);
                        
                        // OK BUT WE NEED TO DEAL WITH INVALID INPUTS AND SHOW SOMETHING LOL
                        
                        // Send GET request
                        URL test = new URL("http://" + host + ":" + port + extension);
                        BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(test.openStream(), UTF_8));

                        // Get the response into one big line then parse it
                        String response = receiveResponse(responseBuffer);
                        client.parseResponse(response);
                        responseBuffer.close();
                        
                        client.repaint();
                        
                        // Waiting for a player
                        if (client.isWaiting()) {
                            URL waitResponse = new URL("http://" + host + ":" + port + "/waitforjoin/" + client.getMatchID());
                            BufferedReader joinedBuffer = new BufferedReader(new InputStreamReader(waitResponse.openStream(), UTF_8));

                            // Get the response into one big line then parse it
                            String joinedResponse = receiveResponse(joinedBuffer);
                            client.parseResponse(joinedResponse);
                            joinedBuffer.close();
                            client.repaint();
                            
                            System.out.println("in here!");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //        // Thread to handle watches
        //        new Thread(() -> {
        //            while (true) { 
        //                URL test;
        //                try {
        //                    test = new URL("http://" + host + ":" + port + sendString);
        //                    BufferedReader response = new BufferedReader(new InputStreamReader(test.openStream(), UTF_8));
        //                    
        //                    String watchState = response.readLine();
        //                    parseRequest(watchState, response);
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                }
        //                canvas.repaint();
        //            }
        //        }).start();
    }
    
    /**
     * Constructs the response into one big string, properly formatted with newlines like it should be.
     */
    private static String receiveResponse(BufferedReader response) throws IOException {
        String fullString = "";
        String line;
        while ((line = response.readLine()) != null) {
            fullString += line + "\n";
        }
        return fullString;
    }

}
