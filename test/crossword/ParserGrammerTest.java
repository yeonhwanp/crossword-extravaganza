package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import crossword.Server.PuzzleGrammar;
import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;

public class ParserGrammerTest {

    private Parser<PuzzleGrammar> parser = Server.makeParser();
    
    /*
     * Testing strategy for parser:
     * 
     * No whitespace, no comments anywhere
     * Whitespace, newlines between lines of puzzle
     * Comments in description
     *      comments in one line, comments anywhere
     * Newline in middle of word entry
     * 
     * 
     * 
     */
    
    
    
    //covers whitespace between lines of puzzle
    @Test public void testParserSimple() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/warmup.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", 1, "star", "ACROSS"));
        expectedWords.add(new WordTuple(0, 2, "\"Farmers ______\"", 2, "market", "DOWN"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
        

    }
    
    //covers backslash in description
    @Test public void testParserBackslashDescription() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/backslash.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle \\\\not\"", 1, "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers comment inside description
    @Test public void testParserCommentDescription() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/comments.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle //comment\"", 1, "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers newline in middle of wordEntry
    @Test public void testParserNewlineDescription() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/newlineDesc.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", 1, "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }

    private static String getName(ParseTree<PuzzleGrammar> parseTree) {
        final List<ParseTree<PuzzleGrammar>> children = parseTree.children();
        ParseTree<PuzzleGrammar> nameTree = children.get(0);
        String name = nameTree.children().get(0).text();

        return name;
    }

    private static String getDescription(ParseTree<PuzzleGrammar> parseTree) {
        final List<ParseTree<PuzzleGrammar>> children = parseTree.children();
        ParseTree<PuzzleGrammar> descriptionTree = children.get(1);
        String description = descriptionTree.children().get(0).text();
        return description;
    }

    private static List<WordTuple> getWordTuples(ParseTree<PuzzleGrammar> parseTree) {
        final List<ParseTree<PuzzleGrammar>> children = parseTree.children();

        List<WordTuple> allWords = new ArrayList<>();
        
        for (int i = 3; i < children.size(); i++) {

            ParseTree<PuzzleGrammar> entryTree = children.get(i);

            String wordname = entryTree.children().get(0).text();
            String hint = entryTree.children().get(1).text();
            String direction = entryTree.children().get(2).text();
            int row = Integer.valueOf(entryTree.children().get(3).text());
            int col = Integer.valueOf(entryTree.children().get(4).text());

            WordTuple currentWord = new WordTuple(row, col, hint, i - 2, wordname, direction);

            allWords.add(currentWord);
        }
        return allWords;
    }


    @Test public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            assert false;
        }, "make sure assertions are enabled with VM argument '-ea'");
    }

    /**
     * Get string of an entire puzzle
     * 
     * @param puzzle file of puzzle to parse
     * @return string of entire puzzle
     * @throws IOException if puzzle cannot be read
     */
    private String getPuzzleString(File puzzle) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(puzzle));
        String fullPuzzle = "";
        String line = reader.readLine();
        while (line != null) {
            fullPuzzle += line;
            line = reader.readLine();
        }
        reader.close();
        return fullPuzzle;
    }

}
