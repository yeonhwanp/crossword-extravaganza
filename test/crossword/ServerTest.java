package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for server
 */
public class ServerTest {
    
    /*
     * Testing strategy for Server.handleConnect():
     *  Partition the input as follows:
     *      Type of Cell: empty, non-existent
     *      Number of words: 0, 1, > 1
     *      Orientation: Across, Down, both
     *      Dimensions: 0, > 0
     * 
     */
    
    // Covers: Type of Cell: empty, non-existent
    //         Number of words: > 1
    //         Orientation: Across
    @Test
    public void testValidPuzzleSimple() throws IOException {
        
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWord = new WordTuple(1, 2, "hint", 1, "cat", "ACROSS");
        WordTuple secondWord = new WordTuple(2, 3, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        List<Match> matches = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        
        assertTrue(currentMatch.checkConsistency());
        
        matches.add(currentMatch);
        
        final Server server = new Server(matches, 4949);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/connect/");
        
        // in this test, we will just assert correctness of the server's output
        final InputStream input = valid.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
        
        String result = getResult(reader);
        
        String expected = "3x8\n" + 
                "########\n" + 
                "##???###\n" + 
                "###?????\n" + 
                "2\n" + 
                "1 2 ACROSS 1\n" + 
                "hint\n" + 
                "2 3 ACROSS 1\n" + 
                "hint\n";
        
        assertEquals(expected, result);
        input.close();
        reader.close();
        server.stop();
    }
    
    // Covers: Number of words: > 1
    //         Orientation: Down
    @Test
    public void testValidPuzzleDown() throws IOException {
        
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWord = new WordTuple(0, 1, "hint", 1, "cat", "DOWN");
        WordTuple secondWord = new WordTuple(0, 2, "hint", 1, "hi", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        List<Match> matches = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
        
        matches.add(currentMatch);
        
        final Server server = new Server(matches, 4949);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/connect/");
        
        // in this test, we will just assert correctness of the server's output
        final InputStream input = valid.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
        
        String result = getResult(reader);
        
        String expected = "3x3\n" + 
                "#??\n" + 
                "#??\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "0 2 DOWN 1\n" + 
                "hint\n";
        
        assertEquals(expected, result);
        input.close();
        reader.close();
        server.stop();
    }
    
    
    
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
    
    /**
     * Helper method to help parse resulting stream
     * @param reader to read stream
     * @return parsed result of the stream
     * @throws IOException if stream cannot be read
     */
    private static String getResult(BufferedReader reader) throws IOException {
        String result = "";
        String line = reader.readLine();
        
        
        while (line != null) {
            result += line + "\n"; // for testing/readability purposes only
            line = reader.readLine();
        }
        return result;
    }
    
}
