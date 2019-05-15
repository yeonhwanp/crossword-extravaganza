package crossword;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.swing.SwingUtilities;

import crossword.Client.ClientState;

/**
 * Client manager to receive responses and communicate with server
 */
public class ClientManager {

    /*
     * Abstraction Function
     * AF() = A program to manage connections between a CrosswordExtravaganaza client and server.                                                                                                                         
     * 
     * Rep Invariant:
     *  true
     * 
     * Safety from Rep Exposure:
     *  No variables
     *  
     * Thread safety argument:
     *  Client is threadsafe and each ClientManager handles only one instance of Client in connecToServer()
     *  connectToServer is only ever run once and even if it were run multiple times, each instance of
     *  the variables are confined to each method call
     *  GUI updates are wrapped in a SwingUtilities.invokeLater()
     *  There are two threads which rely on the state of the same client at any given moment and we
     *      acknowledge that interleaving is possible up to the lines that are synchronized. However,
     *      his is ok for two reasons:
     *          1. The method calls after the if statements are run independently of methods in the client referenced
     *             and all new variables/methods are confined  
     *          2. The part that is synchronized checks for the initial condition again before running the rest of the 
     *             code to make sure that the condition holds true while running the rest of the code.
     *             
     *             TODO this is pretty bad lol
     *  receiveResponse() is not synchronized but all referenced variables are confined to the method call. TOOD correct?
     *  Only public method is the main() method which is only ever run once per lifecycle. TODO correct?
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
     * Connects to server, sends requests and receives responses from the server.
     * @param args command line arguments that should include only the server address.
     * @throws UnknownHostException if the server/socket is unknown host.
     * @throws IOException if we cannot connect with URL, or by socket.
     */
    private void connectToServer(String[] args) throws UnknownHostException, IOException {

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

        // Create a new client object to use
        Client client = new Client(host, port);

        // ========= PARSING LAUNCH ARGUMENTS ========= //

        // Send initial GET request and parse the response
        final URL loadRequest = new URL("http://" + host + ":" + port + "/init/");
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(loadRequest.openStream(), UTF_8));
        String initialRequest = receiveResponse(socketIn);

        SwingUtilities.invokeLater(() -> {
        try {
            client.parseResponse(initialRequest, "");
        } catch (IOException e) {
            e.printStackTrace();
        } 
        client.launchGameWindow();
        });
        
        socketIn.close();

        // watch match list
        new Thread(() -> {
            while (true) {
                    try {
                        if (client.getState() == ClientState.CHOOSE) {
                            final URL sendURL = new URL("http://" + host + ":" + port + "/watchmatches/");
                            final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(sendURL.openStream(), UTF_8));
                            // Get the response into one big line then parse it
                            final String response = receiveResponse(responseBuffer);
                            SwingUtilities.invokeLater( () -> {
                            synchronized(client) {
                                if (client.getState() == ClientState.CHOOSE) {
                                    try {
                                        client.parseResponse(response, "");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        responseBuffer.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    client.repaint();
                                }
                            }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }).start();

        // watch board
        new Thread(() -> {
            while (true) {
                    try {
                        if (client.getState() == ClientState.PLAY) {
                            final URL sendURL = new URL("http://" + host + ":" + port + "/watchboard/" + client.getUserID() + "/" + client.getMatchID());
                            final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(sendURL.openStream(), UTF_8));
                            // Get the response into one big line then parse it
                            final String response = receiveResponse(responseBuffer);
                            SwingUtilities.invokeLater( () -> {
                            synchronized(client) {
                                if (client.getState() == ClientState.PLAY) {
                                    try {
                                        client.parseResponse(response, "");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        responseBuffer.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    client.repaint();
                                }
                            }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }).start();

    }

    /**
     * Constructs the response into one big string, properly formatted with newlines kept, as read through response
     * @return the constructed response in a string form, with newlines kept
     * @throws IOException if the response line cannot be read
     */
    public static String receiveResponse(final BufferedReader response) throws IOException {
        String fullString = "";
        String line;
        while ((line = response.readLine()) != null) {
            fullString += line + "\n";
        }
        return fullString;
    }

}
