/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
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

/**
 * HTTP web puzzle server.
 */
public class Server {
    
    private final HttpServer server;

    
    // LoadBoard()/StartServer?
    // Also need List<String> validFiles and List<Match> ActiveMatches;

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
            Match firstMatch = loadMatch(puzzle);
            break;
        }
    }
    
    
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
    
    public Server(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
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
                communicatePuzzle(exchange);
            }
        });
        look.getFilters().addAll(filters);
        
    }
    
    
    
    
    private void communicatePuzzle(HttpExchange exchange) {
        // TODO Auto-generated method stub
        
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
    public static Match parse(final String string) throws UnableToParseException {
        // parse the example into a parse tree
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(string);

        // display the parse tree in various ways, for debugging only
//         System.out.println("parse tree " + parseTree);
//         Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final Match expression = makeBoard(parseTree);
        // System.out.println("AST " + expression);
        
        return expression;
    }
    
    
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

        //Set<Word> allWords = new HashSet<>();
        
        //initiate Board constructor here - putting in name of puzzle, and description of puzzle
        
        for (int i = 2; i < children.size(); i++) {
            
            //for every entry, use all of the printed information below to create a Word object
            //put this Word object into the set of words that the board holds on to
            
            ParseTree<PuzzleGrammar> entryTree = children.get(i);
            
            String wordname = entryTree.children().get(0).text();
            String clue = entryTree.children().get(1).text();
            String direction = entryTree.children().get(2).text();
            int row = Integer.valueOf(entryTree.children().get(3).text());
            int col = Integer.valueOf(entryTree.children().get(4).text());
            
            System.out.println("wordname: "+ wordname);
            System.out.println("clue: "+ clue);
            System.out.println("direction: "+ direction);
            System.out.println("row: "+ row);
            System.out.println("col: "+ col);
            System.out.println("");
        }
        
        return null;
    }
    
    // ============ PARSING ============ //
}
