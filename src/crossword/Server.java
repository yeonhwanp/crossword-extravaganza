/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import crossword.web.ExceptionsFilter;
import crossword.web.HeadersFilter;
import crossword.web.LogFilter;
import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;

/*
 * TODO NOTES:
 * - Multiple matches
 * - Seeing all matches that work, seeing all the matches that can be connected to
 * - Receive input from the client based on the state of the match
 *      - Implement CHOOSE: PLAY, NEW, EXIT
 *      - Implement PLAY: TRY, CHALLENGE, EXIT
 * - TODO Go to OH and ask about changing the file midway
 * - handle player ID's
 */

/**
 * HTTP web puzzle server.
 */
public class Server {
    
    private final HttpServer server;
//    private final List<String> validMatches;
//    private final Map<String, String> validMatchesMap;
    private final List<Match> waitingMatches;
    private final Map<Integer, String> currentMatchesMap; // <STRING, MATCH>
    private final Map<String, Player> allPlayers;
    
    /*
     * Abstraction Function:
     * AF(server, allMatches) = Server that is played on server, with allMatches being the possible matches that can be played
     *      by players
     * 
     * Rep Invariant:
     * true
     * 
     * Safety from rep exposure:
     *  server and allMatches are both private and final
     *      server is mutated in the Server constructor (parameter as well), start(), and stop(), but this is part of the expected behavior, so no rep exposure
     *      allMatches allMatches is taken in as a parameter only to the constructor, but since our constructor is private,
     *          no rep exposure here. Though the constructor calls on other methods, these methods that have access to matches
     *          are private, so no rep exposure.
     *          
     *          
     * Thread safety argument:
     *  TODO
     */
    
    
    
    
    
    
    private static final int VALID = 200;
    private static final int INVALID = 404;

    /**
     * Start a Crossword Extravaganza server.
     * @param args The command line arguments should include only the folder where
     *             the puzzles are located.
     * @throws IOException 
     * @throws UnableToParseException 
     */
    public static void main(String[] args) throws IOException, UnableToParseException {
        String folderPath = args[0];
        
        final Server server = new Server(folderPath, 4949);
        server.start();
        
//        File folder = new File(folderPath);
//        for (File puzzle : folder.listFiles()) {
//            Match match = loadMatch(puzzle);
//            
//            List<Match> matches = new ArrayList<>();
//            
//            
//            //need to check if this match is valid
//            if (match.checkConsistency()) {
//                matches.add(match);
//                
//                final Server server = new Server(matches, 4949);
//                server.start();
//            }
//            
//            //do we need to stop the server? I feel like we don't?
//            break;
//        }
    }
    
    /**
     * Create a new server object that clients can connect to
     * @param matches different matches that can be played by players on this server
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    protected Server(String folderPath, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
//        this.allMatches = matches;
//        this.validMatchesMap = new HashMap<>(); //TODO CHANGE THESE TWO 
        this.currentMatchesMap = new HashMap<>(); //TODO
        this.waitingMatches = new ArrayList<>();
        
        this.allPlayers = new HashMap<>();
        
        
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());
        
        HeadersFilter headers = new HeadersFilter(Map.of(
                // allow requests from web pages hosted anywhere
                "Access-Control-Allow-Origin", "*",
                // all responses will be plain-text UTF-8
                "Content-Type", "text/plain; charset=utf-8"
                ));
        List<Filter> filters = List.of(new ExceptionsFilter(), new LogFilter(), headers);
        
        // handle requests for paths that start with /connect/
        HttpContext look = server.createContext("/connect/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                choosePlayerId(exchange);
                
//                communicatePuzzle(matches.get(0), exchange);
                //for now we just use first match in matches, since only single puzzle
            }
        });
        look.getFilters().addAll(filters);
        
        
        // handle requests for paths that start with /choose/
        HttpContext choose = server.createContext("/choose/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                System.out.println("hiiii");
                
                System.out.println("WHAT");
                final Map<String, String> validMatchesMap;
                
                try {
                    validMatchesMap = findValidMatches(folderPath);
                    showMatches(validMatchesMap, exchange);
  
                } catch (UnableToParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
//                communicatePuzzle(matches.get(0), exchange);
                //for now we just use first match in matches, since only single puzzle
            }
        });
        choose.getFilters().addAll(filters);
        
        
        checkRep();
    }
    
    /**
     * Check for valid server rep
     */
    private void checkRep() {
        assert server != null;
//        assert validMatchesMap != null;
    }
    
    /**
     * Given a file, make a match object that contains match information given in puzzle, according to the grammar in the project handout
     * @param puzzle file pathname leading to the puzzle to parse
     * @return match object that contains match information given in puzzle
     * @throws IOException if we cannot read the file passed in
     * @throws UnableToParseException if we cannot parse the string of the puzzle
     */
    private static Match loadMatch(File puzzle) throws IOException, UnableToParseException {
        BufferedReader reader = new BufferedReader(new FileReader(puzzle));
        String fullPuzzle = "";
        String line = reader.readLine();
        while (line != null) {
            fullPuzzle += line;
            line = reader.readLine();
        }
        reader.close();
        Match parsedMatch = parse(fullPuzzle);
        
        
        return parsedMatch;
    }
    
    ///////////////////////////////////////////////START STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////START STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////START STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////START STATE///////////////////////////////////////////////////
    
    
    private void choosePlayerId(HttpExchange exchange) throws IOException {
        
        final String response;
        exchange.sendResponseHeaders(VALID, 0);
        
        String result = "";
        result += "Please enter your player ID:";
        
        response = result;
        
        System.out.println(response);
        
        
        
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();

        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.print(response);
        out.flush();
        
        
        InputStream input = exchange.getRequestBody();
        BufferedReader readFromClient = new BufferedReader(new InputStreamReader(input, UTF_8));
        
        String clientId = "";
        String clientLine = readFromClient.readLine();
        while (clientLine != null) {
            clientId += clientLine;
            clientLine = readFromClient.readLine();
        }
        
        if (allPlayers.containsKey(clientId)) { //player already exists
            final String chooseAnother = "Sorry, that ID is already in use. Choose another:";
            out.print(chooseAnother);
            out.flush();
        }
        else { //player doesn't exist yet, so make a new player and put it into the map
            Player newPlayer = new Player(clientId);
            allPlayers.put(clientId, newPlayer);
            
            //TODO MAKE NEW MATCH FOR PLAYERS, LET THEM PLAY IT!!!
            
        }

        
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
        
    }
    
    
    
    ///////////////////////////////////////////////END START STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END START STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END START STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END START STATE///////////////////////////////////////////////////
    
    
    
    
    ///////////////////////////////////////////////CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////CHOOSE STATE///////////////////////////////////////////////////
    
    private static Map<String, String> findValidMatches(String folderPath) throws IOException, UnableToParseException {
        
        File folder = new File(folderPath);
        Map<String, String> valids = new HashMap<>();
        
        for (File puzzle : folder.listFiles()) {
            Match match = loadMatch(puzzle);
            if (match.checkConsistency()) {
                valids.put(match.getMatchName(), match.getMatchDescription());
            }         
                    
        }
        return valids;
        
        
    }
    
    
    private void showMatches(Map<String, String> validMatches, HttpExchange exchange) throws IOException {
        
        final String response;
        exchange.sendResponseHeaders(VALID, 0);
        
        String result = "";
        for (int id : currentMatchesMap.keySet()) {
            result += "PLAY " + currentMatchesMap.get(id);
        }
        result += "\n";
        
        
        int nextMatchId = currentMatchesMap.size();
        for (String puzzleId : validMatches.keySet()) {
            result += "NEW " + nextMatchId + " " + puzzleId + " " + validMatches.get(puzzleId);
        }
        
        result += "\n";
        result += "EXIT";
        
        response = result;
        
        System.out.println("RESPONSE::: " + response);
        
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    
    ///////////////////////////////////////////////END CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END CHOOSE STATE///////////////////////////////////////////////////
    ///////////////////////////////////////////////END CHOOSE STATE///////////////////////////////////////////////////
    
//    /**
//     * Given a match, send a readable version of the match to the client.
//     * @param match match to send a readable version of
//     * @param exchange exchange used to connect with client
//     * @throws IOException if response headers cannot be sent
//     */
//    private static void communicatePuzzle(Match match, HttpExchange exchange) throws IOException {
//        
//        final String response;
//        exchange.sendResponseHeaders(VALID, 0);
//        
//        response = match.toString(); //string of puzzle that the client should see
//        
//        System.out.println("RESPONSE: " + response);
//        
//        // write the response to the output stream using UTF-8 character encoding
//        OutputStream body = exchange.getResponseBody();
//        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
//        out.print(response);
//        out.flush();
//        
//        // if you do not close the exchange, the response will not be sent!
//        exchange.close();
//        
//        
//    }




    // ============ PARSING ============ //




    private static enum PuzzleGrammar {
        FILE, NAME, DESCRIPTION, ENTRY, WORDNAME, CLUE, DIRECTION, ROW, COL, STRING, STRINGIDENT, INT, SPACES, WHITESPACE, NEWLINES;
    }
    
    
    private static Parser<PuzzleGrammar> parser = makeParser();
    
    
    /**
     * Compile the grammar into a parser.
     * 
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<PuzzleGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            final File grammarFile = new File("src/crossword/PuzzleGrammar.g");
            return Parser.compile(grammarFile, PuzzleGrammar.FILE);
            
        // Parser.compile() throws two checked exceptions.
        // Translate these checked exceptions into unchecked RuntimeExceptions,
        // because these failures indicate internal bugs rather than client errors
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }
    
    
    /**
     * Parse a string into an expression.
     * 
     * @param string string to parse
     * @return Expression parsed from the string
     * @throws UnableToParseException if the string doesn't match the Expression grammar
     */
    private static Match parse(final String string) throws UnableToParseException {
        // parse the example into a parse tree
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(string);

        // display the parse tree in various ways, for debugging only
//         System.out.println("parse tree " + parseTree);
//         Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final Match match = makeBoard(parseTree);
        // System.out.println("AST " + expression);
        
        return match;
    }
    
    /**
     * Using a parseTree, construct a Match object 
     * @param parseTree parse tree to parse through in order to find information needed to construct a match object
     * @return Match object that is a parsed match based on information from the parseTree
     */
    private static Match makeBoard(ParseTree<PuzzleGrammar> parseTree) {
        final List<ParseTree<PuzzleGrammar>> children = parseTree.children();
        ParseTree<PuzzleGrammar> nameTree = children.get(0);
        String name = nameTree.children().get(0).text();
        
        ParseTree<PuzzleGrammar> descriptionTree = children.get(1);
        String description = descriptionTree.children().get(0).text();
        
        
        System.out.println("");
        System.out.println("puzzle name: " + name);
        System.out.println("puzzle description: " + description);
        System.out.println("");

        List<WordTuple> allWords = new ArrayList<>();
        
        //initiate Board constructor here - putting in name of puzzle, and description of puzzle
        
        for (int i = 3; i < children.size(); i++) {
            
            //for every entry, use all of the printed information below to create a Word object
            //put this Word object into the set of words that the board holds on to
            
            ParseTree<PuzzleGrammar> entryTree = children.get(i);
            
            String wordname = entryTree.children().get(0).text();
            String hint = entryTree.children().get(1).text();
            String direction = entryTree.children().get(2).text();
            int row = Integer.valueOf(entryTree.children().get(3).text());
            int col = Integer.valueOf(entryTree.children().get(4).text());
            
//            Word currentWord = new Word(row, col, hint, i - 1, wordname, direction);
            WordTuple currentWord = new WordTuple(row, col, hint, i-1, wordname, direction);
            
            System.out.println("wordname: "+ wordname);
            System.out.println("hint: "+ hint);
            System.out.println("direction: "+ direction);
            System.out.println("row: "+ row);
            System.out.println("col: "+ col);
            System.out.println("");
            
            
            allWords.add(currentWord);
        }
        
        Match currentMatch = new Match(name, description, allWords);
        
        return currentMatch;
    }
    
    // ============ END PARSING ============ //
    
    
    
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return server.getAddress().getPort();
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        System.err.println("Server will stop");
        server.stop(0);
    }
    
    /*
     * TO STRING IMPLEMENTATION
     * -> First line should always indicate what state we're in.
     */
    
    /**
     * RECEIVE: New connection request
     * STATE: start
     * SEND: state, "NEW GAME"
     */
    private void init(HttpExchange exchange) throws IOException {
    }
    
    /**
     * RECEIVE: a start request from the players with one parameter: "MY_PLAYER ID"
     *  PRECONDITION: The ID must be unique (non-existing)
     * STATE:
     *  IF precondition: choose
     *      SEND: STATE, "NEW"
     *  ELSE: start
     *      SEND: STATE, "TRY AGAIN"
     */
    private void start(HttpExchange exchange) throws IOException {
    }
    
    /**
     * RECIEVE: A new match request in the form of: "NEW match_ID puzzle_ID "Description"
     *  PRECONDITION: matchID must be unique, puzzle_ID must exist, 
     *      - matchID must be unique
     *      - matchID must have no whitespace
     *      - puzzle_ID must exist
     *      - TODO ASSUMPTION that description comes from the .puzzle file
     *  STATE:
     *      - IF precondition: WAIT
     *          SEND: STATE, "WAITING"
     *          THEN: server.wait() until someone else connects to the board 
     *          STATE: PLAY
     *          SEND: STATE, board
     *      - ELSE: choose
     *          SEND: STATE, "TRY AGAIN"
     */
    private void newMatch(HttpExchange exchange) throws IOException {
    }
    
    /**
     * RECEIVE: A play request in the form: "PLAY match_ID"
     *  PRECONDITION: 
     *      - matchID must exist
     *  STATE:
     *      - IF precondition:
     *          - STATE = PLAY
     *          - SEND: STATE, board
     *      - ELSE:
     *          - SEND: STATE, "TRY_AGAIN"
     */
    private void play(HttpExchange exchange) throws IOException {
    }

    /**
     * RECEIVE: An exist request in the form "EXIT GAME_STATE"
     *   IF GAME_STATE == CHOOSE:
     *      - CLOSE CONNECTION
     *   ELSE IF GAME_STATE == WAIT:
     *      - Terminate game
     *      - SEND: NEW, "NEW_GAME"
     *   ELSE IF GAME_STATE == PLAY:
     *      - Terminate game
     *      - SEND: SHOW_SCORE, score
     *   ELSE IF GAME_STATE == SHOW_SCORE:
     *      - Close connection
     */
    private void exit(HttpExchange exchange) throws IOException {
    }
    
    /**
     * RECEIVE: A try request in the form: "TRY PLAYER_ID MATCH_ID WORD_ID word"
     * PRECONDITION:
     *     - MATCH_ID must exist
     *     - PLAYER_ID must be one of the players in the match
     * IF VALID REQUEST -> Ongoing (game logic):
     *     - SEND: PLAY, board, true
     * IF VALID_REQUEST -> Finish (game logic):
     *     - SEND: SHOW_SCORE, score
     * IF INVLAID (game logic):
     *     - SEND: PLAY, board, false
     */
    private void tryPlay(HttpExchange exchange) throws IOException {
    }
    
    /**
     * RECEIVE: A try request in the form: "CHALLENGE PLAYER_ID MATCH_ID WORD_ID word"
     * PRECONDITION:
     *     - MATCH_ID must exist
     *     - PLAYER_ID must be one of the players in the match
     * IF VALID CHALLENGE -> Ongoing (game logic):
     *     - SEND: PLAY, board, true
     * IF VALID_CHALLENGE -> Finish (game logic):
     *     - SEND: SHOW_SCORE, score
     * IF FAILED_CHALLENGE (game logic):
     *     - SEND: PLAY, board, false
     */
    private void challenge(HttpExchange exchange) throws IOException {
    }
    
    /**
     * Send the score
     */
    private String sendSHOWSCORE() {    
    }
    
    
    
    
    
}
