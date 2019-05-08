package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * Tests for server for our puzzle
 */
public class ServerTest {
    
    /*
     * Testing strategy for Server:
     * 
     * Test init()
     * 
     * Test handleStart()
     *  fails precondition, passes precondition (with multiple players)
     *      passing precondition: no matches to play (only new games), matches to play
     *  
     * Test chooseNewMatch()
     *  fails precondition, passes precondition
     *  
     * Test playMatch()
     *  fails precondition, passes precondition
     * 
     * Test exit()
     *  gameState: choose, wait, play, showScore
     *  
     * Test tryPlay()
     *  fails precondition, passes precondition
     *  valid request: ongoing, finished board
     *  invalid request (game logic)
     *  
     * Test challenge()
     *  fails precondition, passes precondition
     *  valid challenge: ongoing, finished board
     *  invalid challenge (game logic)
     * 
     * Test showScore()
     * 
     * 
     */
    
    
    //covers init()
    @Test 
    public void testInit() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/init/");

        // in this test, we will just assert correctness of the server's output
        final InputStream input = valid.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("start\nNEW GAME", result);
        
        
    }
    
    //covers handleStart()
    //      fails precondition
    @Test 
    public void testHandleStartFails() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/start/player1");
        valid.openStream();
        
        final URL valid2 = new URL("http://localhost:" + server.port() + "/start/player1");
        final InputStream input = valid2.openStream();
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("start\nTRY AGAIN", result);
        
        
    }
    
    //covers handleStart()
    //      passes precondition with multiple players, no twoPlayerMatches yet
    @Test 
    public void testHandleStartPassesOnlyNewGames() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/start/player1");
        valid.openStream();
        
        final URL valid2 = new URL("http://localhost:" + server.port() + "/start/player2");
        final InputStream input = valid2.openStream();
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("choose\nNEW\n2\ncomments.puzzle\nwarmup.puzzle\n0", result);
        
        
    }
    
    
    
//    // Covers: Type of Cell: empty, non-existent
//    //         Number of words: > 1
//    //         Orientation: Across
//    @Test
//    public void testValidPuzzleSimple() throws IOException {
//        
//        List<WordTuple> words = new ArrayList<>();
//        WordTuple firstWord = new WordTuple(1, 2, "hint", 1, "cat", "ACROSS");
//        WordTuple secondWord = new WordTuple(2, 3, "hint", 1, "splat", "ACROSS");
//        words.add(firstWord);
//        words.add(secondWord);
//        
//        List<Match> matches = new ArrayList<>();
//        Match currentMatch = new Match("Match name", "Match description", words);
//        
//        assertTrue(currentMatch.checkConsistency());
//        
//        matches.add(currentMatch);
//        
//        final Server server = new Server(matches, 4949);
//        server.start();
//        
//        final URL valid = new URL("http://localhost:" + server.port() + "/connect/");
//        
//        // in this test, we will just assert correctness of the server's output
//        final InputStream input = valid.openStream();
//        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
//        
//        String result = getResult(reader);
//        
//        String expected = "3x8\n" + 
//                "########\n" + 
//                "##???###\n" + 
//                "###?????\n" + 
//                "2\n" + 
//                "1 2 ACROSS 1\n" + 
//                "hint\n" + 
//                "2 3 ACROSS 1\n" + 
//                "hint\n";
//        
//        assertEquals(expected, result);
//        input.close();
//        reader.close();
//        server.stop();
//    }
//    
//    // Covers: Number of words: > 1
//    //         Orientation: Down
//    @Test
//    public void testValidPuzzleDown() throws IOException {
//        
//        List<WordTuple> words = new ArrayList<>();
//        WordTuple firstWord = new WordTuple(0, 1, "hint", 1, "cat", "DOWN");
//        WordTuple secondWord = new WordTuple(0, 2, "hint", 1, "hi", "DOWN");
//        words.add(firstWord);
//        words.add(secondWord);
//        
//        List<Match> matches = new ArrayList<>();
//        Match currentMatch = new Match("Match name", "Match description", words);
//        assertTrue(currentMatch.checkConsistency());
//        
//        matches.add(currentMatch);
//        
//        final Server server = new Server(matches, 4949);
//        server.start();
//        
//        final URL valid = new URL("http://localhost:" + server.port() + "/connect/");
//        
//        // in this test, we will just assert correctness of the server's output
//        final InputStream input = valid.openStream();
//        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
//        
//        String result = getResult(reader);
//        
//        String expected = "3x3\n" + 
//                "#??\n" + 
//                "#??\n" + 
//                "#?#\n" + 
//                "2\n" + 
//                "0 1 DOWN 1\n" + 
//                "hint\n" + 
//                "0 2 DOWN 1\n" + 
//                "hint\n";
//        
//        assertEquals(expected, result);
//        input.close();
//        reader.close();
//        server.stop();
//    }
    
    
    
    
    
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
        
        result = result.substring(0, result.length()-1); //get rid of last newline character
        
        return result;
    }
    
}
