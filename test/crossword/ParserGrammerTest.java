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

/**
 * Tests for parser grammar
 */
public class ParserGrammerTest {

    private Parser<PuzzleGrammar> parser = Server.makeParser();
    
    /*
     * Testing strategy for parser:
     * 
     * No whitespace, no comments anywhere
     * Whitespace, newlines between literals of puzzle
     *      single newlines, multiple newlines
     * Comments
     *      comments following text (inside a literal)
     *      comments in between literals
     *          comments on same line as literal, comments on their own line
     *          multi-line comments (consecutive lines)
     * Backslash
     *      backslash inside literal
     *      contains \n, \r, \t
     * Hyphens
     * 
     * 
     */
    
    
    //covers no whitespace or comments anywhere
    @Test public void testParserSimple() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/warmup.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        expectedWords.add(new WordTuple(0, 2, "\"Farmers ______\"", "market", "DOWN"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
        

    }
    
    //covers whitespace between literals of puzzle
    @Test public void testParserSimpleWhitespace() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/warmup.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        expectedWords.add(new WordTuple(0, 2, "\"Farmers ______\"", "market", "DOWN"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
        

    }
    
    //covers backslash in literal (description)
    //   \n
    @Test public void testParserBackslashDescription() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/backslash.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle \\\\n ot\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers backslash in literal (description)
    //   \r, \t
    @Test public void testParserBackslashDescriptionOthers() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/backslash2.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle \\\\r to \\\\t get started\"", description);
        
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
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle //comment \\\\n\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers comment between literals (description and direction)
    //      comments on same line as literal
    @Test public void testParserCommentBetweenDescriptionDirection() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/commentWithNewline.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers comment after literal (description)
    //      comments on their own line
    @Test public void testParserCommentInDescriptionOwnLine() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/commentAfterDescription.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    
    //covers newline in between literals (single newline)
    @Test public void testParserNewlineInBetween() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/newlineDesc.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers newline in between literals (multiple newline)
    @Test public void testParserNewlineInBetweenMultiple() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/multipleNewLines.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "star", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    //covers hyphens
    @Test public void testParserHyphens() throws UnableToParseException, IOException {
        final File puzzleFile = new File("test-puzzles/dash.puzzle");
        final ParseTree<PuzzleGrammar> parseTree = parser.parse(puzzleFile);
        final String name = getName(parseTree);
        final String description = getDescription(parseTree);
        final List<WordTuple> words = getWordTuples(parseTree);
        
        List<WordTuple> expectedWords = new ArrayList<>();
        expectedWords.add(new WordTuple(1, 0, "\"twinkle twinkle\"", "st-ar", "ACROSS"));
        
        assertEquals("\"Easy\"", name);
        assertEquals("\"An easy puzzle to get started\"", description);
        
        for (int i = 0; i < words.size(); i++) {
            WordTuple w = words.get(i);
            WordTuple expW = expectedWords.get(i);
            assertTrue(w.equals(expW));
        }
    }
    
    /**
     * Find the name of the puzzle
     * @param parseTree tree to find the name of
     * @return name of the puzzle
     */
    private static String getName(ParseTree<PuzzleGrammar> parseTree) {
        final List<ParseTree<PuzzleGrammar>> children = parseTree.children();
        ParseTree<PuzzleGrammar> nameTree = children.get(0);
        String name = nameTree.children().get(0).text();

        return name;
    }

    /**
     * Find the description of the puzzle
     * @param parseTree tree to find the description of
     * @return description of the puzzle
     */
    private static String getDescription(ParseTree<PuzzleGrammar> parseTree) {
        final List<ParseTree<PuzzleGrammar>> children = parseTree.children();
        ParseTree<PuzzleGrammar> descriptionTree = children.get(1);
        String description = descriptionTree.children().get(0).text();
        return description;
    }

    /**
     * Find all the words entries of the puzzle, in word tuple form
     * @param parseTree tree to find the words of
     * @return words of the puzzle, in word tuple form
     */
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

            WordTuple currentWord = new WordTuple(row, col, hint, wordname, direction);

            allWords.add(currentWord);
        }
        return allWords;
    }
    
    
    


    @Test public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            assert false;
        }, "make sure assertions are enabled with VM argument '-ea'");
    }


}
