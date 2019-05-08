/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * TODO define restrictions on "Description"
 */

/**
 * HTTP web puzzle server.
 */
public class Server {
    
    private final HttpServer server;
//    private final List<String> validMatches;
//    private final Map<Integer, String> currentMatchesMap; // <STRING, MATCH>
//    private final Map<String, Player> allPlayers;
    private final String folderPath;
    
    private Set<String> validPuzzleNames;
    private final Set<Player> allPlayers;
    private final Map<String, String> mapIDToDescription;
    private final Map<String, Match> mapIDToMatch;
    
    private final Map<String, Match> twoPlayerMatches;
    
    
    
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
        // this.allMatches = matches;
        this.folderPath = folderPath;

        this.allPlayers = new HashSet<>();
        this.mapIDToDescription = new HashMap<>();
        this.mapIDToMatch = new HashMap<>();
        
        this.twoPlayerMatches = new HashMap<>();

        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());

        HeadersFilter headers = new HeadersFilter(Map.of(
                // allow requests from web pages hosted anywhere
                "Access-Control-Allow-Origin", "*",
                // all responses will be plain-text UTF-8
                "Content-Type", "text/plain; charset=utf-8"));
        List<Filter> filters = List.of(new ExceptionsFilter(), new LogFilter(), headers);

        // handle requests for paths that start with /init/
        HttpContext initRequest = server.createContext("/init/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                init(exchange);

            }
        });
        initRequest.getFilters().addAll(filters);

        // handle requests for paths that start with /start/
        HttpContext startRequest = server.createContext("/start/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {
                try {
                    validPuzzleNames = findValidPuzzles(folderPath);
                } catch (UnableToParseException e) {
                    e.printStackTrace();
                }
                handleStart(exchange);

            }
        });
        startRequest.getFilters().addAll(filters);

        // handle requests for paths that start with /choose/
        HttpContext chooseRequest = server.createContext("/choose/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                try {
                    chooseNewMatch(exchange);
                } catch (UnableToParseException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        chooseRequest.getFilters().addAll(filters);
        
        // handle requests for paths that start with /play/
        HttpContext playRequest = server.createContext("/play/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                playMatch(exchange);   

            }
        });
        playRequest.getFilters().addAll(filters);
        
        
        // handle requests for paths that start with /exit/
        HttpContext exitRequest = server.createContext("/exit/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                exit(exchange);   

            }
        });
        exitRequest.getFilters().addAll(filters);
        
        // handle requests for paths that start with /try/
        HttpContext tryRequest = server.createContext("/try/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                tryPlay(exchange);   

            }
        });
        tryRequest.getFilters().addAll(filters);

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
    private static void init(HttpExchange exchange) throws IOException {
        
        final String response;
        exchange.sendResponseHeaders(VALID, 0);

        String result = "start\n"
                + "NEW GAME";
        
        response = result;

        System.out.println("INIT RESPONSE: " + response);

        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.print(response);
        out.flush();

        // if you do not close the exchange, the response will not be sent!
        exchange.close();
        
    }
    
    /**
     * RECEIVE: a start request from the players with one parameter: "MY_PLAYER_ID"
     *  PRECONDITION: The ID must be unique (non-existing)
     * STATE:
     *  IF precondition: choose
     *      SEND: STATE, "NEW", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     *  ELSE: start
     *      SEND: STATE, "TRY AGAIN"
     */
    private void handleStart(HttpExchange exchange) throws IOException {
        
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        final String playerStr = path.substring(base.length());
        
        final String response;
        exchange.sendResponseHeaders(VALID, 0);
        
        Player potentialPlayer = new Player(playerStr);
        if (isUniquePlayer(potentialPlayer)) {
            
            allPlayers.add(potentialPlayer);
            response = getChooseResponse("NEW");
        }
        else {
            response = "start\n"
                    + "TRY AGAIN";

        }

        System.out.println("START RESPONSE: " + response);

        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.print(response);
        out.flush();

        // if you do not close the exchange, the response will not be sent!
        exchange.close();
        
    }
    
    /**
     * RECIEVE: A new match request in the form of: "player_ID match_ID puzzle_ID "Description"
     *  PRECONDITION: matchID must be unique, puzzle_ID must exist, 
     *      - matchID must be unique
     *      - puzzle_ID must exist
     *  STATE:
     *      - IF precondition: WAIT
     *          SEND: STATE, "WAITING"
     *          THEN: server.wait() until someone else connects to the board 
     *          STATE: PLAY
     *          SEND: STATE, new, board
     *      - ELSE: choose
     *          SEND: STATE, "TRY AGAIN", allMatches
     * @throws UnableToParseException 
     * @throws InterruptedException 
     */
    private void chooseNewMatch(HttpExchange exchange) throws IOException, UnableToParseException, InterruptedException {
        
        synchronized(folderPath) {
        
            // if you want to know the requested path:
            final String path = exchange.getRequestURI().getPath();
            
            // it will always start with the base path from server.createContext():
            final String base = exchange.getHttpContext().getPath();
            assert path.startsWith(base);
            final String idsAndDescription = path.substring(base.length());
            
            
            exchange.sendResponseHeaders(VALID, 0);
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            
            String[] names = idsAndDescription.split("/");
            String playerID = names[0];
            String matchID = names[1];
            String puzzleID = names[2];
            String description = names[3];
            
            if (!mapIDToDescription.containsKey(matchID) && validPuzzleNames.contains(puzzleID)) { //start new match
                
                
                Player existingPlayer = getPlayer(playerID);
                
                File puzzleFile = new File(folderPath + "/" +  puzzleID);
                Match puzzle = loadMatch(puzzleFile);
                puzzle.addPlayer(existingPlayer);
                
                mapIDToDescription.put(matchID, description);
                mapIDToMatch.put(matchID, puzzle);

                
                final String waitResponse = "WAIT\nWAITING";
                
                out.print(waitResponse);
                out.flush();
                exchange.close();
                
                
                while(puzzle.getNumberPlayers() < 2) {
                    folderPath.wait();
                }
                
                exchange.sendResponseHeaders(VALID, 0);
                OutputStream bodyAgain = exchange.getResponseBody();
                PrintWriter outAgain = new PrintWriter(new OutputStreamWriter(bodyAgain, UTF_8), true);
                
                
                final String playResponse;
                String playResult = "PLAY\nnew\n";
                playResult += puzzle.toString();
                playResponse = playResult;
                outAgain.print(playResponse);
                outAgain.flush();
                
                exchange.close();
                
            }
            else {
                
                final String response;
                response = getChooseResponse("TRY AGAIN");
                
                out.print(response);
                out.flush();
                exchange.close();
            }
            
            
        
        }
    }
    
    /**
     * RECEIVE: A play request in the form: "play playerID matchID"
     *  PRECONDITION: 
     *      - matchID must exist
     *  STATE:
     *      - IF precondition:
     *          - STATE = PLAY
     *          - SEND: STATE, new, board
     *      - ELSE:
     *          - STATE = choose
     *          - SEND: STATE, "TRY AGAIN", allMatches
     */
    private void playMatch(HttpExchange exchange) throws IOException {
        
        synchronized (folderPath) {
            
            // if you want to know the requested path:
            final String path = exchange.getRequestURI().getPath();
            
            // it will always start with the base path from server.createContext():
            final String base = exchange.getHttpContext().getPath();
            assert path.startsWith(base);
            final String playerAndMatch = path.substring(base.length());
            
            String[] playerAndMatchArray = playerAndMatch.split("/");
            String playerID = playerAndMatchArray[0];
            String matchID = playerAndMatchArray[1];
            
            exchange.sendResponseHeaders(VALID, 0);
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            
            if (mapIDToMatch.containsKey(matchID)) { // valid precondition, so play an existing match
                
                Player secondPlayer = getPlayer(playerID);
                Match matchToPlay = mapIDToMatch.get(matchID);
                
                matchToPlay.addPlayer(secondPlayer);
                
                mapIDToDescription.remove(matchID);
                mapIDToMatch.remove(matchID);
                
                twoPlayerMatches.put(matchID, matchToPlay);
                
                String validTemporary = "PLAY\n"
                        + "new\n";
                validTemporary += matchToPlay.toString();
                
                final String validResponse = validTemporary;
                out.print(validResponse);
                out.flush();
                
                folderPath.notifyAll();
    
            }
            else {
                
                final String invalidResponse = getChooseResponse("TRY AGAIN");
                out.print(invalidResponse);
                out.flush();
            }
     
            exchange.close();
        
        }
 
    }

    /**
     * RECEIVE: An exist request in the form "exit gameState (matchID)", where matchID only is in the request if state
     * is wait or play
     *   IF gameState == choose:
     *      - Close connection
     *   ELSE IF gameState == wait:
     *      - Terminate game
     *      - SEND: CHOOSE, "NEW"
     *   ELSE IF gameState == play:
     *      - Terminate game
     *      - SEND: SHOW_SCORE, score
     *   ELSE IF gameState == showScore:
     *      - Close connection
     */
    private void exit(HttpExchange exchange) throws IOException {
        
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        final String stateAndID = path.substring(base.length());
        
        String[] states = stateAndID.split("/");
        String gameState = states[0];
        
        exchange.sendResponseHeaders(VALID, 0);
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        
        if (gameState.equals("choose")) {
            exchange.close();
        }
        else if (gameState.equals("wait")) {
            
            String matchID = states[1];
            mapIDToDescription.remove(matchID);
            mapIDToMatch.remove(matchID);
            
            final String response = "CHOOSE\nNEW";
            out.print(response);
            out.flush();
            exchange.close();

        }
        else if (gameState.equals("play")) {
            
            String matchID = states[1];
            twoPlayerMatches.remove(matchID);
            final String response;
            String temporaryResponse = "SHOW_SCORE\n";
            Match currentMatch = mapIDToMatch.get(matchID);
//            temporaryResponse += currentMatch.getMatchScore();
            response = temporaryResponse;
            
            out.print(response);
            out.flush();
            exchange.close();
            
        }
        else if (gameState.equals("showScore")) {
            exchange.close();
        }
        
        
        
    }
    
    /**
     * RECEIVE: A try request in the form: "try playerID matchID wordID word"
     * PRECONDITION:
     *     - MATCH_ID must exist in currently playing matches
     *     - PLAYER_ID must be one of the players in the match
     * IF VALID REQUEST -> Ongoing (game logic):
     *     - SEND: PLAY, true, board
     * IF VALID_REQUEST -> Finish (game logic):
     *     - SEND: SHOW_SCORE, score
     * IF INVALID (game logic):
     *     - SEND: PLAY, false, board

     */
    private void tryPlay(HttpExchange exchange) throws IOException {
        
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        final String tryRequest = path.substring(base.length());
        
        exchange.sendResponseHeaders(VALID, 0);
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        
        
        String[] ids = tryRequest.split("/");
        String playerID = ids[0];
        String matchID = ids[1];
        String wordID = ids[2];
        String word = ids[3];
        
        if (twoPlayerMatches.containsKey(matchID)) {
            Match currentMatch = twoPlayerMatches.get(matchID);
            Player currentPlayer = getPlayer(playerID);
            if (currentMatch.containsPlayer(currentPlayer)) {
                
                boolean validTry = currentMatch.tryInsert(currentPlayer, Integer.valueOf(wordID), word);
                boolean matchFinished = currentMatch.isFinished();
                
                if (validTry && matchFinished) {
                    String finishedResponse = "SHOW_SCORE\n";
                    // finishedResponse += currentMatch.getMatchScore();
                    final String finished = finishedResponse;

                    out.print(finished);
                    out.flush();
                    exchange.close();

                }
                else {

                    String ongoingResponse = "PLAY\n";
                    ongoingResponse += String.valueOf(validTry) + "\n" + currentMatch.toString();
                    final String ongoing = ongoingResponse;

                    out.print(ongoing);
                    out.flush();
                    exchange.close();

                }


            }
        }
        
        
    }
    
    /**
     * RECEIVE: A try request in the form: "challenge playerID matchID wordID word"
     * PRECONDITION:
     *     - matchID must exist
     *     - playerID must be one of the players in the match
     * IF VALID CHALLENGE -> Ongoing (game logic):
     *     - SEND: PLAY, true, board
     * IF VALID_CHALLENGE -> Finish (game logic):
     *     - SEND: SHOW_SCORE, score
     * IF FAILED_CHALLENGE (game logic):
     *     - SEND: PLAY, false, board
     */
    private void challenge(HttpExchange exchange) throws IOException {
        
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        final String tryRequest = path.substring(base.length());
        
        exchange.sendResponseHeaders(VALID, 0);
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        
        
        String[] ids = tryRequest.split("/");
        String playerID = ids[0];
        String matchID = ids[1];
        String wordID = ids[2];
        String word = ids[3];
        
        if (twoPlayerMatches.containsKey(matchID)) {
            Match currentMatch = twoPlayerMatches.get(matchID);
            Player currentPlayer = getPlayer(playerID);
            if (currentMatch.containsPlayer(currentPlayer)) {
                
                boolean validChallenge = currentMatch.challenge(currentPlayer, Integer.valueOf(wordID), word);
                boolean matchFinished = currentMatch.isFinished();
                
                if (validChallenge && matchFinished) {
                    String finishedResponse = "SHOW_SCORE\n";
                    // finishedResponse += currentMatch.getMatchScore();
                    final String finished = finishedResponse;

                    out.print(finished);
                    out.flush();
                    exchange.close();

                }
                else {

                    String ongoingResponse = "PLAY\n";
                    ongoingResponse += String.valueOf(validChallenge) + "\n" + currentMatch.toString();
                    final String ongoing = ongoingResponse;

                    out.print(ongoing);
                    out.flush();
                    exchange.close();

                }


            }
        }
        
        
    }
    
    
    
    /**
     * Send the score
     */
    private void sendShowScore() {    
    }
    
    
    
    
    /**
     * Determines if the passed in player already is an existing player (unique or not)
     * @param player player to check uniqueness
     * @return if the passed in player already is an existing player
     */
    private boolean isUniquePlayer(Player player) {
        for (Player existingPlayer : allPlayers) {
            if (player.getID().equals(existingPlayer.getID())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Parses choose responses, which includes allMatches (all the matches that can be either played or began)
     * @param state state that client should switch to
     * @return parsed choose response
     */
    private String getChooseResponse(String state) {
        
        String visualOfPuzzles = "";
        for (String onePuzzle : validPuzzleNames) {
            visualOfPuzzles += onePuzzle + "\n";
        }
        
        String visualOfMatches = "";
        for (String matchID : mapIDToDescription.keySet()) {
            visualOfMatches += matchID + "\n";
            visualOfMatches += mapIDToDescription.get(matchID) + "\n";
        }

        String response = "choose\n"
                + state + "\n"
                + validPuzzleNames.size() + "\n"
                + visualOfPuzzles
                + mapIDToDescription.size() + "\n"
                + visualOfMatches;
        
        return response;
    }
    
    /**
     * Get the player that is currently playing using this server, that corresponds to a given player identifier
     * @param playerStr player identifier to match player to
     * @return player that matches playerStr
     */
    public Player getPlayer(String playerStr) {
        //TODO Is this rep exposure?
        
        for (Player player : allPlayers) {
            if (player.getID().equals(playerStr)) {
                return player;
            }
        }
        return new Player("");
    }
    
    
    /**
     * Find all the puzzles that are valid puzzles in a folder of possible puzzles
     * @param folderPath path to folder that holds the puzzles to check
     * @return all the puzzles that are valid puzzles
     * @throws IOException if we cannot properly load the match
     * @throws UnableToParseException if we for some reason cannot parse the file puzzle
     */
    private static Set<String> findValidPuzzles(String folderPath) throws IOException, UnableToParseException {
      File folder = new File(folderPath);
      Set<String> puzzles = new HashSet<>();
      for (File puzzle : folder.listFiles()) {
          Match match = loadMatch(puzzle);
          //need to check if this match is valid
          if (match.checkConsistency()) {
              puzzles.add(match.getMatchName());
          }
          
      }
      return puzzles;
      
    }
    
    
}
