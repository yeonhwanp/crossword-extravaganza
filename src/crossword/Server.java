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
 */

/**
 * HTTP web puzzle server.
 */
public class Server {
    
    private final HttpServer server;
    private final List<String> allMatches;
    private final List<Match> waitingMatches;
    
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
        File folder = new File(folderPath);
        for (File puzzle : folder.listFiles()) {
            Match match = loadMatch(puzzle);
            
            List<Match> matches = new ArrayList<>();
            
            
            //need to check if this match is valid
            if (match.checkConsistency()) {
                matches.add(match);
                
                final Server server = new Server(matches, 4949);
                server.start();
            }
            
            //do we need to stop the server? I feel like we don't?
            break;
        }
    }
    
    /**
     * Create a new server object that clients can connect to
     * @param matches different matches that can be played by players on this server
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    protected Server(List<Match> matches, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.allMatches = matches;
        
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
                communicatePuzzle(matches.get(0), exchange);
                //for now we just use first match in matches, since only single puzzle
            }
        });
        look.getFilters().addAll(filters);
        
        
        checkRep();
    }
    
    /**
     * Check for valid server rep
     */
    private void checkRep() {
        assert server != null;
        assert allMatches != null;
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
    
    
    /**
     * Given a match, send a readable version of the match to the client.
     * @param match match to send a readable version of
     * @param exchange exchange used to connect with client
     * @throws IOException if response headers cannot be sent
     */
    private static void communicatePuzzle(Match match, HttpExchange exchange) throws IOException {
        
        final String response;
        exchange.sendResponseHeaders(VALID, 0);
        
        response = match.toString(); //string of puzzle that the client should see
        
        System.out.println("RESPONSE: " + response);
        
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
        
        
    }




    // ============ PARSING ============ //




    private static enum PuzzleGrammar {
        FILE, NAME, DESCRIPTION, ENTRY, WORDNAME, CLUE, DIRECTION, ROW, COL, STRING, STRINGIDENT, INT, SPACES, WHITESPACE;
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
        String name = nameTree.text();
        
        ParseTree<PuzzleGrammar> descriptionTree = children.get(1);
        String description = descriptionTree.text();
        
        
        System.out.println("");
        System.out.println("puzzle name: " + name);
        System.out.println("puzzle description: " + description);
        System.out.println("");

        List<Word> allWords = new ArrayList<>();
        
        //initiate Board constructor here - putting in name of puzzle, and description of puzzle
        
        for (int i = 2; i < children.size(); i++) {
            
            //for every entry, use all of the printed information below to create a Word object
            //put this Word object into the set of words that the board holds on to
            
            ParseTree<PuzzleGrammar> entryTree = children.get(i);
            
            String wordname = entryTree.children().get(0).text();
            String hint = entryTree.children().get(1).text();
            String direction = entryTree.children().get(2).text();
            int row = Integer.valueOf(entryTree.children().get(3).text());
            int col = Integer.valueOf(entryTree.children().get(4).text());
            
            Word currentWord = new Word(row, col, hint, i - 1, wordname, direction);
            
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
    
}
