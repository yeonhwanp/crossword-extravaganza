package crossword;

/**
 * Client manager to receive responses and communicate with server
 */
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import crossword.Client.ClientState;

public class ClientManager {

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

        client.parseResponse(initialRequest, ""); // TODO check this line "" is ok?
        client.launchGameWindow();
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
                        synchronized(client) {
                            if (client.getState() == ClientState.CHOOSE) {
                                client.parseResponse(response, "");
                                responseBuffer.close();
                                client.repaint();  
                            }
                        }

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
                        synchronized(client) {
                            if (client.getState() == ClientState.PLAY) {
                                client.parseResponse(response, "");
                                responseBuffer.close();
                                client.repaint();  
                            }
                        }
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
