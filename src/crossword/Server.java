/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
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

import crossword.Word.ChallengeResult;
import crossword.Word.TryResult;
import crossword.web.ExceptionsFilter;
import crossword.web.HeadersFilter;
import crossword.web.LogFilter;
import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;


/*
 * Concurrency design:
 * 
 * Synchronize every non-static method on a lock of the Server rep. This way, only one thread can change/access our rep 
 * data at a time. if we didn't do this, then there could be a lot of interleaving and potential overwriting of changed
 * reps.
 * 
 */

/**
 * HTTP web puzzle server, that handles all matches being played on this server.
 */
public class Server {
    
    private final HttpServer server;
    private final String folderPath;
    private Set<String> validPuzzleNames;
    private final Set<Player> allPlayers;
    private final Map<String, String> mapIDToDescription;
    private final Map<String, Match> mapIDToMatch;
    private final Map<String, Match> twoPlayerMatches;
    private final Map<String, String> mapIDToWinners;
    
    
    private static final int THIRD_INDEX = 3;
    private static final int FOURTH_INDEX = 4;
    
    
    /*
     * Abstraction Function:
     * AF(server, folderPath, validPuzzleNames, allPlayers, mapIDToDescription, mapIDToMatch, twoPlayerMatches, mapIDToWinners) =
     *  Server that is hosted on server, using path folderPath as the folder to read puzzles from. Puzzles that are valid
     *  puzzles (according to project handout) have IDs in validPuzzleNames. All the players that are currently playing are
     *  stored in allPlayers. The server contains a map mapIDToDescription mapping match ID's to the match description,
     *  where these matches only have one player and are waiting for another. In other words, mapIDToDescription.get(s) is the description
     *  of the match with ID s. The server also has a map mapIDToMatch that maps match IDs to actual matches 
     *  (these matches also have only one player). In other words, mapIDToMatch.get(s) is the Match object for match with ID s. 
     *  Any matches with two players that are currently being played are in twoPlayerMatches, which maps the match ID to the Match object itself.
     *  Any matches that has finished/terminated has its map ID in mapIDToWinner, where values are the players who are the most recent
     *      winners of that match, so mapIDToWinner.get(s) is the ID of the player that won the match with ID s. By most recent winner,
     *      this means the winner of the most recent time the matchID was used for a match (matchIDs can be recycled after termination).
     * 
     * Rep Invariant:
     * Every player in allPlayers should exist in either a value of mapIDToMatch (as a player of that match), or
     *  a value of twoPlayerMatches (again as a player of that match), but not both.
     * Every player should not have multiple locations (there cannot be duplicate players)
     * Every key in mapIDToDescription should exist in mapIDToMatch, and vice versa.
     * There should be no shared keys between mapIDToMatch, twoPlayerMatches, or mapIDToWinners
     * 
     * Safety from rep exposure:
     *  All fields, except validPuzzleNames, are private and final.
     *      server is mutated in start(), and stop(), but this is part of the expected behavior, so no unsafe rep exposure
     *      validPuzzleNames is mutated only in our constructor, but this is okay because it is part of the expected behavior.
     *          validPuzzleNames is never returned or taken in as an argument to any method, so we do not keep references of it
     *      folderPath is also immutable, so we have no rep exposure here, even when it is taken in as a parameter to other methods,
     *      allPlayers is mutated in handleStart, but this is part of expected behavior. It is not mutated, taken in as a parameter,
     *          or returned in any other method.
     *      mapIDToDescription is mutated in chooseNewMatch, playMatch, and exit, but this is expected client behavior, so it is not rep exposure.
     *          It is not mutated, taken in as a parameter, or returned in any other method.
     *      mapIDToMatch is mutated in chooseNewMatch, playMatch, and exit, but this is expected client behavior, so it is not rep exposure.
     *          It is not mutated, taken in as a parameter, or returned in any other method.
     *      twoPlayerMatches is mutated in playMatch and exit, but this is expected client behavior, so it is not rep exposure.
     *          It is not mutated, taken in as a parameter, or returned in any other method.
     *      mapIDToWinners is mutated in numerous methods, but this is expected client behavior, so it is not rep exposure.
     *      
     *      Overall, none of these rep fields are returned or taken in as arguments to any of our methods, and we do
     *      not keep references of them. A client would never be able to have direct access to any of our reps.
     *          We do return a mutable Match in parse(), but this is okay because this match is not part of our rep,
     *          since we are simply creating a match by parsing a file.
     *      
     * Thread safety argument:
     *  Every method that is non-static is locked by the rep folderPath. Therefore, all accesses to our rep are guarded by
     *  the lock on folderPath, which is an instance variable. This means that only one thread can access/change our rep
     *  at a time. This is essentially the same as monitor pattern, except we do not want to give access to the lock that
     *  we are using in server.
     *  
     *  In some methods (exit, tryInsert, challenge, and watchBoard), we also lock on the match users are playing on
     *  There is no case for deadlock, as the locks are always obtained in the order of server, then match itself.
     *  Now, any change to the current match is atomic, so we will not have thread safe issues here.
     *  All of our static methods are threadsafe because:
     *    The static methods only used local variables that are confined, so there is no behavior that is not threadsafe
     *  Private methods, though not synchronized, do not present thread safety issues because they are always called within
     *      public, synchronized methods. So, in order to call a private method, we need to first obtain the lock on 
     *      the rep of server anyway.
     *      
     *  
     *  
     */
    
    
    
    
    
    
    private static final int VALID = 200;

    /**
     * Start a Crossword Extravaganza server.
     * @param args The command line arguments should include only the folder where
     *             the puzzles are located.
     * @throws IOException if an error occurs starting the server
     */
    public static void main(String[] args) throws IOException {
        String folderPath = args[0];
        
        final Server server = new Server(folderPath, 4949);
        server.start();
        
    }
    
    /**
     * Create a new server object that clients can connect to
     * @param folderPath path to folder that contains all of the possible puzzles to play
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    protected Server(String folderPath, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.folderPath = folderPath;
        this.allPlayers = new HashSet<>();
        this.validPuzzleNames = new HashSet<>();
        this.mapIDToDescription = new HashMap<>();
        this.mapIDToMatch = new HashMap<>();
        this.twoPlayerMatches = new HashMap<>();
        this.mapIDToWinners = new HashMap<>();

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
        
        // handle requests for paths that start with /waitforjoin/
        HttpContext waitForJoinRequest = server.createContext("/waitforjoin/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                try {
                    waitForJoin(exchange);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }   

            }
        });
        waitForJoinRequest.getFilters().addAll(filters);
        
        
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
        
        // handle requests for paths that start with /challenge/
        HttpContext challengeRequest = server.createContext("/challenge/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                challenge(exchange);   

            }
        });
        challengeRequest.getFilters().addAll(filters);
        
        // handle requests for paths that start with /watchboard/
        HttpContext watchRequest = server.createContext("/watchboard/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                try {
                    watchBoard(exchange);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }   

            }
        });
        watchRequest.getFilters().addAll(filters);
        
        // handle requests for paths that start with /watchmatches/
        HttpContext watchMatchRequest = server.createContext("/watchmatches/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                try {
                    watchMatches(exchange);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }   

            }
        });
        watchMatchRequest.getFilters().addAll(filters);
        
        // handle requests for paths that start with /restart/
        HttpContext restartRequest = server.createContext("/restart/", new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {

                restart(exchange);   

            }
        });
        restartRequest.getFilters().addAll(filters);

        checkRep();
    }
    
    /**
     * Check for valid server rep
     */
    private void checkRep() {

        synchronized (folderPath) {

            assert server != null;
            assert folderPath != null;
            assert validPuzzleNames != null;

            for (Player player : allPlayers) { // assert each player has only one location (either mapIDToMatch or
                                               // twoPlayerMatches)
                int playerCount = 0;
                for (String matchID : mapIDToMatch.keySet()) {
                    Match oneMatch = mapIDToMatch.get(matchID);
                    if (oneMatch.containsPlayer(player)) {
                        playerCount++;
                    }
                }
                for (String matchID : twoPlayerMatches.keySet()) {
                    Match oneMatch = twoPlayerMatches.get(matchID);
                    if (oneMatch.containsPlayer(player)) {
                        playerCount++;
                    }
                }
                assert playerCount == 1;
            }

            assert mapIDToMatch.keySet().equals(mapIDToDescription.keySet());

            for (String matchID : twoPlayerMatches.keySet()) {
                assert !mapIDToMatch.keySet().contains(matchID);
            }

            assert mapIDToWinners != null;
        }

    }

    public static enum PuzzleGrammar {
        FILE, NAME, DESCRIPTION, ENTRY, WORDNAME, CLUE, DIRECTION, ROW, COL, STRING, STRINGIDENT, INT, SPACES, WHITESPACE, NEWLINES, COMMENT, COMMENTORWHITESPACE;
    }
    
    
    private static Parser<PuzzleGrammar> parser = makeParser();
    
    
    /**
     * Compile the grammar into a parser.
     * 
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    public static Parser<PuzzleGrammar> makeParser() {
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
     * @param puzzle file to parse
     * @return Expression parsed from the file
     * @throws UnableToParseException if unable to parse the puzzle correctly
     * @throws IOException if we cannot open the puzzle correctly
     */
    private static Match parse(final File puzzle) throws UnableToParseException, IOException {
        // parse the example into a parse tree
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzle);

        // display the parse tree in various ways, for debugging only
//         System.out.println("parse tree " + parseTree);
//         Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final Match match = makeBoard(parseTree);
        
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
        

        List<WordTuple> allWords = new ArrayList<>();
        
        //initiate Board constructor here - putting in name of puzzle, and description of puzzle
        
        for (int i = THIRD_INDEX; i < children.size(); i++) {
            
            //for every entry, use all of the printed information below to create a Word object
            //put this Word object into the set of words that the board holds on to
            
            
            ParseTree<PuzzleGrammar> entryTree = children.get(i);
            
            String wordname = entryTree.children().get(0).text();
            String hint = entryTree.children().get(1).text();
            String direction = entryTree.children().get(2).text();
            int row = Integer.valueOf(entryTree.children().get(THIRD_INDEX).text());
            int col = Integer.valueOf(entryTree.children().get(FOURTH_INDEX).text());
            
            WordTuple currentWord = new WordTuple(row, col, hint, wordname, direction);
          
            allWords.add(currentWord);
        }

        Match currentMatch = new Match(name, description, allWords);

        return currentMatch;
    }

    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        synchronized (folderPath) {
            return server.getAddress().getPort();
        }
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        synchronized (folderPath) {
            System.err.println("Server will listen on " + server.getAddress());
            server.start();
        }
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        synchronized (folderPath) {
            System.err.println("Server will stop");
            server.stop(0);
        }
    }
    
    
    /**
     * RECEIVE: New connection request
     * STATE: start
     * SEND: state, "new game"
     * @param exchange exchange to communicate with client
     * @throws IOException if an error occurs starting the server
     */
    private static void init(HttpExchange exchange) throws IOException {
        
        final String response;
        exchange.sendResponseHeaders(VALID, 0);

        response = "start\nnew game";
        
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.print(response);
        out.flush();

        exchange.close();
        
    }
    
    /**
     * RECEIVE: a start request from the players with one parameter: "start playerID"
     *  PRECONDITION: The ID must be unique (non-existing)
     * STATE:
     *  IF precondition: choose
     *      SEND: STATE, "new", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     *  ELSE: start
     *      SEND: STATE, "try again"
     * @param exchange exchange to communicate with client
     * @throws IOException if headers cannot be sent
     */
    private void handleStart(HttpExchange exchange) throws IOException {
        
        synchronized (folderPath) {

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
                response = getChooseResponse("new");
            } else {
                response = "start\n" + "try again";

            }


            // write the response to the output stream using UTF-8 character encoding
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            out.print(response);
            out.flush();

            exchange.close();

        }
    }
    
    
    
    /**
     * RECEIVE: a restart request from the players in the form of: "restart"
     *   - SEND: choose, "update", allMatches (matches with one player to join, and puzzles with no players to start a new match)
     * @param exchange exchange to communicate with client
     * @throws IOException if headers cannot be sent
     */
    private void restart(HttpExchange exchange) throws IOException {
        
        synchronized (folderPath) {

            final String response;
            exchange.sendResponseHeaders(VALID, 0);

            response = getChooseResponse("update");

            // write the response to the output stream using UTF-8 character encoding
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            out.print(response);
            out.flush();

            exchange.close();

        }
    }
    
    
    
    
    /**
     * RECIEVE: A new match request in the form of: "choose player_ID match_ID puzzle_ID "Description"
     *  PRECONDITION: matchID must be unique, puzzle_ID must exist, 
     *      - matchID must be unique
     *      - puzzle_ID must exist
     *  STATE:
     *      - IF precondition: wait
     *          SEND: STATE
     *      - ELSE: choose
     *          SEND: STATE, "try again", allMatches
     * @param exchange exchange to communicate with client
     * @throws IOException if headers cannot be sent
     * @throws UnableToParseException if we cannot parse the board
     * @throws InterruptedException if we improperly exit while waiting
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
            String description = names[THIRD_INDEX];
            
            if (isUniqueMatchID(matchID) && validPuzzleNames.contains(puzzleID)) { //start new match
                
                if (mapIDToWinners.containsKey(matchID)) { //client started a new match with a matchID equal to the matchID of a match
                                                        //that used to exist, but is now being replaced                    
                    mapIDToWinners.remove(matchID);
                }
                
                Player existingPlayer = getPlayer(playerID);
                
                File puzzleFile = new File(folderPath + "/" +  puzzleID);
                Match puzzle = parse(puzzleFile);
                puzzle.addPlayer(existingPlayer);
                
                mapIDToDescription.put(matchID, description);
                mapIDToMatch.put(matchID, puzzle);

                
                final String waitResponse = "wait";
                
                out.print(waitResponse);
                out.flush();
                exchange.close();
      
                folderPath.notifyAll();
                     
            }
            else {
                
                final String response;
                response = getChooseResponse("try again");
                
                out.print(response);
                out.flush();
                exchange.close();
            }

        }
    }
    
    /**
     * RECEIVE: A request in the form of: "waitforjoin playerID matchID"
     *  PRECONDITION:
     *      - matchID must exist in twoPlayerMatches
     *  STATE:
     *      - If precondition:
     *      THEN: folderPath.wait() until someone else connects to the board 
     *          STATE: play
     *          - SEND: STATE, new, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     * @param exchange exchange to communicate with client
     * @throws IOException if response headers cannot be sent
     * @throws InterruptedException if we close incorrectly while waiting
     */
    private void waitForJoin(HttpExchange exchange) throws IOException, InterruptedException {
        
        synchronized (folderPath) {
        
            // if you want to know the requested path:
            final String path = exchange.getRequestURI().getPath();
            
            // it will always start with the base path from server.createContext():
            final String base = exchange.getHttpContext().getPath();
            assert path.startsWith(base);
            final String playerAndMatchID = path.substring(base.length());
            
            String[] names = playerAndMatchID.split("/");
            String playerID = names[0];
            String matchID = names[1];
            
            
            Match matchToPlay = mapIDToMatch.get(matchID);
            Player player = getPlayer(playerID);

            
            while(matchToPlay.getNumberPlayers() < 2) {
                folderPath.wait();
            }
            
            Player otherPlayer = matchToPlay.getOtherPlayer(player);

            exchange.sendResponseHeaders(VALID, 0);
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            
            final String playResponse;
            String playResult = "play\nnew\n";

            playResult += playerID + "\n" + matchToPlay.getScore(player) + "\n" + matchToPlay.getChallengePoints(player)
                    + "\n" + otherPlayer.getID() + "\n" + matchToPlay.getScore(otherPlayer) + "\n"
                    + matchToPlay.getChallengePoints(otherPlayer) + "\n" + matchToPlay.toString();
 
            playResponse = playResult;
            out.print(playResponse);
            out.flush();

            exchange.close();

        }

    }
    
    /**
     * RECEIVE: A play request in the form: "play playerID matchID"
     *  PRECONDITION: 
     *      - matchID must exist
     *  STATE:
     *      - IF precondition:
     *          - STATE = play
     *          - SEND: STATE, new, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *      - ELSE:
     *          - STATE = choose
     *          - SEND: STATE, "try again", allMatches
     *  @param exchange exchange to communicate with client
     *  @throws IOException if headers cannot be sent
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

                
                Player secondPlayer = getPlayer(playerID); //second player to join the match
                Match matchToPlay = mapIDToMatch.get(matchID);
                
                matchToPlay.addPlayer(secondPlayer);
                
                mapIDToDescription.remove(matchID);
                mapIDToMatch.remove(matchID);
                
                twoPlayerMatches.put(matchID, matchToPlay);
                
                Player otherPlayer = matchToPlay.getOtherPlayer(secondPlayer);

                String validTemporary = "play\n" + "new\n";
                validTemporary += playerID + "\n" + matchToPlay.getScore(secondPlayer) + "\n"
                        + matchToPlay.getChallengePoints(secondPlayer) + "\n" + otherPlayer.getID() + "\n"
                        + matchToPlay.getScore(otherPlayer) + "\n" + matchToPlay.getChallengePoints(otherPlayer) + "\n"
                        + matchToPlay.toString();

                final String validResponse = validTemporary;
                out.print(validResponse);
                out.flush();
 
                folderPath.notifyAll();
    
            }
            else {
                
                final String invalidResponse = getChooseResponse("try again");
                out.print(invalidResponse);
                out.flush();

            }
     
            exchange.close();
        
        }
 
    }

    /**
     * RECEIVE: An exist request in the form "exit gameState playerID (matchID)", where matchID only is in the request if state
     * is wait or play
     *   IF gameState == choose:
     *      - Close connection
     *   ELSE IF gameState == wait:
     *      - Terminate game
     *      - SEND: choose, "update", allMatches
     *   ELSE IF gameState == play:
     *      - Terminate game
     *      - SEND: show_score, winner, myPlayer, score, challengePoints, otherPlayer, score2, challengePoints2
     *   ELSE IF gameState == show_score:
     *      - Close connection
     * @param exchange exchange to communicate with client
     * @throws if headers cannot be sent
     */
    private void exit(HttpExchange exchange) throws IOException {

        synchronized (folderPath) {

            // if you want to know the requested path:
            final String path = exchange.getRequestURI().getPath();

            // it will always start with the base path from server.createContext():
            final String base = exchange.getHttpContext().getPath();
            assert path.startsWith(base);
            final String stateAndID = path.substring(base.length());

            String[] states = stateAndID.split("/");

            String gameState = states[0];
            String playerID = states[1];

            exchange.sendResponseHeaders(VALID, 0);
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);

            if (gameState.equals("choose") || gameState.equals("show_score")) {

                Player playerToRemove = getPlayer(playerID);
                allPlayers.remove(playerToRemove);
                
                exchange.close();
                
            } else if (gameState.equals("wait")) {

                String matchID = states[2];
                
                mapIDToDescription.remove(matchID);
                mapIDToMatch.remove(matchID);

                final String response = getChooseResponse("update");
                out.print(response);
                out.flush();
                exchange.close();

                folderPath.notifyAll();

            } else if (gameState.equals("play")) {

                
                String matchID = states[2];
                
                Player quittingPlayer = getPlayer(playerID);
                
                Match currentMatch = twoPlayerMatches.get(matchID);
                
                synchronized (currentMatch) {
                
                    Player winner = currentMatch.getOtherPlayer(quittingPlayer); //since you're quitting, the other player automatically wins!
                    String winnerID = winner.getID();
                    
                    twoPlayerMatches.remove(matchID);
                    mapIDToWinners.put(matchID, winnerID);
                    
                    currentMatch.notifyAll();

                    final String finished = "show_score\n" + winnerID + "\n" + playerID + "\n"
                            + currentMatch.getScore(quittingPlayer) + "\n"
                            + currentMatch.getChallengePoints(quittingPlayer) + "\n" + winnerID + "\n"
                            + currentMatch.getScore(winner) + "\n" + currentMatch.getChallengePoints(winner);

                    out.print(finished);
                    out.flush();
                    exchange.close();
     
                }

            }
        }
    }
    
    /**
     * RECEIVE: A try request in the form: "try playerID matchID wordID word"
     * PRECONDITION:
     *     - MATCH_ID must exist in currently playing matches
     * IF VALID REQUEST -> Ongoing (game logic):
     *     - SEND: play, success, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     * IF VALID_REQUEST -> Finish (game logic):
     *     - SEND: show_score, winner, myPlayer, score, challengePoints, otherPlayer, score2, challengePoints2
     * IF INVALID (game logic):
     *   based on why the insert was rejected, send one of the following:
     *     - SEND: play, incorrect_length, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *     - SEND: play, wrong_id, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *     - SEND: play, inconsistent_current, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     * @param exchange exchange to communicate with client
     */
    private void tryPlay(HttpExchange exchange) throws IOException {
        
        synchronized (folderPath) {

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
            String word = ids[THIRD_INDEX];

            if (twoPlayerMatches.containsKey(matchID)) {
                Match currentMatch = twoPlayerMatches.get(matchID);
                
                synchronized (currentMatch) {
                    
                    Player currentPlayer = getPlayer(playerID);
                    
                    if (currentMatch.containsPlayer(currentPlayer)) {
    
                        TryResult typeOfTry = currentMatch.tryInsert(currentPlayer, Integer.valueOf(wordID), word);
                        boolean matchFinished = currentMatch.isFinished();
                       
    
                        if (typeOfTry == TryResult.SUCCESS && matchFinished) {
                            
                            Player otherPlayer = currentMatch.getOtherPlayer(currentPlayer);
                            
                            String winnerID = currentMatch.calculateWinner();
                            
                            twoPlayerMatches.remove(matchID);
                            mapIDToWinners.put(matchID, winnerID);

                            currentMatch.notifyAll();       
                            
                            final String finished = "show_score\n" + winnerID + "\n" + playerID + "\n" + currentMatch.getScore(currentPlayer)
                                    + "\n" + currentMatch.getChallengePoints(currentPlayer) + "\n" + otherPlayer.getID()
                                    + "\n" + currentMatch.getScore(otherPlayer) + "\n"
                                    + currentMatch.getChallengePoints(otherPlayer);
                            
                            out.print(finished);
                            out.flush();
                            exchange.close();
   
                        } else {
                            
                            Player otherPlayer = currentMatch.getOtherPlayer(currentPlayer);
                            
                            
                            String validTryStr = typeOfTry.name().toLowerCase();
    
                            final String ongoing = "play\n" + validTryStr + "\n" + playerID + "\n"
                                    + currentMatch.getScore(currentPlayer) + "\n"
                                    + currentMatch.getChallengePoints(currentPlayer) + "\n" + otherPlayer.getID() + "\n"
                                    + currentMatch.getScore(otherPlayer) + "\n"
                                    + currentMatch.getChallengePoints(otherPlayer) + "\n" + currentMatch.toString();

    
                            out.print(ongoing);
                            out.flush();
                            exchange.close();
                            
  
                        }
    
                    }
                }
            }
            exchange.close();
        }
        
        
        
    }
    
    /**
     * RECEIVE: A try request in the form: "challenge playerID matchID wordID word"
     * PRECONDITION:
     *     - matchID must exist
     *     - playerID must be one of the players in the match
     * IF VALID CHALLENGE -> Ongoing (game logic):
     *     if challenge correct:
     *     - SEND: play, wonch, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     *     if challenge incorrect:
     *     - SEND: play, lostch, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     * IF VALID_CHALLENGE -> Finish (game logic):
     *     - SEND: show_score, winner, myPlayer, score, challengePoints, otherPlayer, score2, challengePoints2
     * IF FAILED_CHALLENGE (game logic):
     *     - SEND: play, invalidch, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     * @param exchange exchange to communicate with client
     * @throws IOException if headers cannot be properly sent
     */
    private void challenge(HttpExchange exchange) throws IOException {
        
        synchronized (folderPath) {

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
            String word = ids[THIRD_INDEX];

            if (twoPlayerMatches.containsKey(matchID)) {
                Match currentMatch = twoPlayerMatches.get(matchID);
                
                synchronized (currentMatch) {
                
                    Player currentPlayer = getPlayer(playerID);
                    if (currentMatch.containsPlayer(currentPlayer)) {
    
                        ChallengeResult validChallenge = currentMatch.challenge(currentPlayer, Integer.valueOf(wordID), word);
                        boolean matchFinished = currentMatch.isFinished();
    
                        if (validChallenge == ChallengeResult.CORRECT && matchFinished) {
                            
                            
                            Player otherPlayer = currentMatch.getOtherPlayer(currentPlayer);
                            
                            String finishedResponse = "show_score\n";
                            String winnerID = currentMatch.calculateWinner();
                            
                            twoPlayerMatches.remove(matchID);
                            mapIDToWinners.put(matchID, winnerID);
                            
                            currentMatch.notifyAll();

                            finishedResponse += winnerID + "\n" + playerID + "\n" + currentMatch.getScore(currentPlayer)
                                    + "\n" + currentMatch.getChallengePoints(currentPlayer) + "\n" + otherPlayer.getID()
                                    + "\n" + currentMatch.getScore(otherPlayer) + "\n"
                                    + currentMatch.getChallengePoints(otherPlayer);
                            final String finished = finishedResponse;

                            out.print(finished);
                            out.flush();
                            exchange.close();
 
                        } else {
                            
                            Player otherPlayer = currentMatch.getOtherPlayer(currentPlayer);
                            
                            String typeOfChallenge;
                            if (validChallenge == ChallengeResult.CORRECT) typeOfChallenge = "wonch";
                            else if (validChallenge == ChallengeResult.INCORRECT) typeOfChallenge = "lostch";
                            else typeOfChallenge = "invalidch";

                            String ongoingResponse = "play\n" + typeOfChallenge + "\n" + playerID + "\n"
                                    + currentMatch.getScore(currentPlayer) + "\n"
                                    + currentMatch.getChallengePoints(currentPlayer) + "\n" + otherPlayer.getID() + "\n"
                                    + currentMatch.getScore(otherPlayer) + "\n"
                                    + currentMatch.getChallengePoints(otherPlayer) + "\n" + currentMatch.toString();

                            final String ongoing = ongoingResponse;

                            out.print(ongoing);
                            out.flush();
                            exchange.close();

                        }
    
                    }
                }
            }
        }
        
    }
    

    /**
     * RECEIVES: request to watch for other matches to be added or removed in the form of: watchMatches
     * SENDS: STATE, "update", allMatches
     * 
     * Wait and watch until other matches are added and removed from the list of playable matches (with one player already)
     * Communicate this information (live update) to the client
     * @param exchange exchange to communicate with client
     * @throws IOException if headers cannot be sent
     * @throws InterruptedException if we improperly exit while waiting
     */
    private void watchMatches(HttpExchange exchange) throws IOException, InterruptedException {
        
        synchronized (folderPath) {

            final String response;
            exchange.sendResponseHeaders(VALID, 0);

            String availableMatches = getChooseResponse("update");

            while (availableMatches.equals(getChooseResponse("update"))) {
                folderPath.wait();
            }

            response = getChooseResponse("update");

            // write the response to the output stream using UTF-8 character encoding
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            out.print(response);

            out.flush();
            exchange.close();

        }
        
    }
    
    
    /**
     * RECEIVES: watch request in the form of: watchBoard playerID matchID
     * SENDS: if move made finishes the match:
     *      - show_score, winner, myPlayer, score, challengePoints, otherPlayer, score2, challengePoints2
     *      else:
     *      - play, update, playerID, playerPoints, playerChallengePts, otherPlayerID, otherPlayerPts, otherPlayerChallengePts, board
     * 
     * Wait until the board changes, and when it does, show the newly changed board to the client
     * @param exchange exchange to communicate with client
     * @throws IOException if headers cannot be sent
     * @throws InterruptedException if we improperly exit while waiting
     */
    private void watchBoard(HttpExchange exchange) throws IOException, InterruptedException {
        
        synchronized (folderPath) {

            // if you want to know the requested path:
            final String path = exchange.getRequestURI().getPath();

            // it will always start with the base path from server.createContext():
            final String base = exchange.getHttpContext().getPath();
            assert path.startsWith(base);
            final String playerAndMatch = path.substring(base.length());
            String[] ids = playerAndMatch.split("/");
            final String playerID = ids[0];
            final String matchID = ids[1];

            Match matchToWatch = twoPlayerMatches.get(matchID);

            new Thread(new Runnable() {

                public void run() {
                    try {
                        synchronized (matchToWatch) {

                            final String response;
                            try {
                                exchange.sendResponseHeaders(VALID, 0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            String currentMatchState = matchToWatch.toString();

                            while (currentMatchState.equals(matchToWatch.toString())
                                    && !mapIDToWinners.containsKey(matchID)) {
                                try {
                                    matchToWatch.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            Player currentPlayer = getPlayer(playerID);
                            Player otherPlayer = matchToWatch.getOtherPlayer(currentPlayer);

                            if (mapIDToWinners.containsKey(matchID)) {

                                String winnerID = mapIDToWinners.get(matchID);
                                response = "show_score\n" + winnerID + "\n" + playerID + "\n"
                                        + matchToWatch.getScore(currentPlayer) + "\n"
                                        + matchToWatch.getChallengePoints(currentPlayer) + "\n" + otherPlayer.getID()
                                        + "\n" + matchToWatch.getScore(otherPlayer) + "\n"
                                        + matchToWatch.getChallengePoints(otherPlayer);

                            }

                            else {
                                response = "play\nupdate\n" + playerID + "\n" + matchToWatch.getScore(currentPlayer)
                                        + "\n" + matchToWatch.getChallengePoints(currentPlayer) + "\n"
                                        + otherPlayer.getID() + "\n" + matchToWatch.getScore(otherPlayer) + "\n"
                                        + matchToWatch.getChallengePoints(otherPlayer) + "\n" + matchToWatch.toString();
                            }

                            // write the response to the output stream using UTF-8 character encoding
                            OutputStream body = exchange.getResponseBody();
                            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
                            out.print(response);
                            out.flush();
                            exchange.close();
                        }
                    } catch (NullPointerException e) {
                        Thread.currentThread().interrupt(); // watch board no longer should go through, since match
                                                            // finished already
                        return;
                    }

                }
            }).start();

        }
    }

    /**
     * Determines if the passed in player already is an existing player (unique or
     * not)
     * 
     * @param player player to check uniqueness
     * @return if the passed in player already is an existing player
     */
    private boolean isUniquePlayer(Player player) {
        
        synchronized (folderPath) {

            for (Player existingPlayer : allPlayers) {
                if (player.getID().equals(existingPlayer.getID())) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Parses choose responses, which includes allMatches (all the matches that can be either played or began)
     * @param state state that client should switch to
     * @return parsed choose response
     */
    private String getChooseResponse(String state) {

        synchronized (folderPath) {

            String visualOfPuzzles = "";
            for (String onePuzzle : validPuzzleNames) {
                visualOfPuzzles += onePuzzle + "\n";
            }

            String visualOfMatches = "";
            for (String matchID : mapIDToDescription.keySet()) {
                visualOfMatches += matchID + "\n";
                visualOfMatches += mapIDToDescription.get(matchID) + "\n";
            }

            String response = "choose\n" + state + "\n" + validPuzzleNames.size() + "\n" + visualOfPuzzles
                    + mapIDToDescription.size() + "\n" + visualOfMatches;

            return response;
        }
    }
    
    /**
     * Get the player that is currently playing using this server, that corresponds to a given player identifier
     * @param playerStr player identifier to match player to
     * @return player that matches playerStr
     */
    private Player getPlayer(String playerStr) {

        synchronized (folderPath) {
            for (Player player : allPlayers) {
                if (player.getID().equals(playerStr)) {
                    return player;
                }
            }
            return new Player("");
        }
    }
    

    /**
     * Determines if the entered matchID is a unique match - that is, it doesn't exist in the set of already-existing matches
     * @param matchID matchID to check uniqueness
     * @return if entered matchID is unique
     */
    private boolean isUniqueMatchID(String matchID) {
        return !mapIDToDescription.containsKey(matchID) && !twoPlayerMatches.containsKey(matchID);
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
          Match match = parse(puzzle);
          //need to check if this match is valid
          if (match.checkConsistency()) {
              puzzles.add(puzzle.getName());
          }
          
      }
      return puzzles;
      
    }
    
    
}
