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

import org.junit.jupiter.api.Test;

/**
 * Tests for server for our puzzle
 */
public class ServerTest {
    
    /*
     * Testing strategy for Server - these methods are private itself, but we can use URL's to execute them
     * 
     * Test init()
     * 
     * Test handleStart()
     *  fails precondition, passes precondition (with multiple players)
     *      passing precondition: no matches to play (only new games), matches to play
     *  
     * Test chooseNewMatch()
     *  fails precondition, passes precondition
     *      failure: non-unique match ID, non-existing puzzle ID
     *      
     * Test waitForJoin()
     *  
     * Test playMatch()
     *  fails precondition (matchID doesn't exist), passes precondition
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
     * 
     * Test watchMatches()
     *  watch for added matches to join
     *  watch for removed matches to join
     * 
     * Test watchBoard()
     *  try move updates board
     *  challenge move updates board
     *  any move finishes board
     * 
     * 
     */
    
    
    
    
    
    /*
     * Manual tests:
     * 
     * Test for waitForJoin()
     *  covers waitForJoin() method
     *  1. start a new match up with a new player
     *      - use NEW command to achieve this
     *      - see that the user cannot see any of the board yet
     *  2. start up another new client (different player)
     *  3. use the PLAY command to connect to the first match
     *      - see that both clients can now see the board and the current score
     * 
     * 
     * Test for watchForMatches()
     *  covers watch for added
     *  1. start a new client up with one player - get to the choose state by entering a valid playerID
     *  2. start up a new client with a different player
     *      - begin a new match, waiting for another player
     *  3. see that the first client can now see that match appear in the list of matches to join
     *  
     *  
     * Test for watchForMatches()
     *  covers watch for remove
     *  1. start a new client up with one player - get to the choose state by entering a valid playerID
     *  2. start up a new client with a different player
     *      - begin a new match
     *  3. start up a new client and join that match
     *  4. see that the first client can now see that the match disappears in the list of matches to join
     * 
     * 
     * Test for watchBoard()
     *  covers try move updates board
     *  1. start up a new game (with two clients)
     *  2. for one of the clients, make a try move that is valid
     *  3. see that the other client sees the board live-update to show this try information
     *  
     *  
     * Test for watchBoard()
     *  covers challenge move updates board
     *  1. start up a new game (with two clients)
     *  2. for one of the clients, make a try move that is valid
     *  3. for the second client, make a challenge move on that tried move
     *  3. see that the first client sees the board live-update to show this challenge information
     *  
     * Test for watchBoard()
     *  covers any move finishes up board
     *  1. start up a new game (with two clients)
     *  2. keep making moves (try and challenge) until there is one more word left to fill in (by try or by challenge)
     *  3. fill in this word with the first client
     *  4. see that the second client and the first client see the winner of the match, as well as scores
     * 
     */
    
    //covers init()
    @Test 
    public void testInit() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/init/");

        final InputStream input = valid.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("start\nnew game", result);
        
        
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
        
        assertEquals("start\ntry again", result);
        
        
    }
    
    //covers handleStart()
    //      passes precondition with multiple players, no matches to join yet
    @Test 
    public void testHandleStartPassesOnlyNewGames() throws IOException {
        
        final Server server = new Server("puzzles", 0);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/start/player1");
        valid.openStream();
        
        final URL valid2 = new URL("http://localhost:" + server.port() + "/start/player2");
        final InputStream input = valid2.openStream();
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("choose\nnew\n1\nsimple.puzzle\n0", result);
        
        
    }
    
    //covers handleStart()
    //      passes precondition with multiple players, some matches can be joined
    @Test 
    public void testHandleStartPassesMatchesExist() throws IOException {
        
        final Server server = new Server("puzzles", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/simple/simple.puzzle/yo").openStream();

        final URL valid2 = new URL("http://localhost:" + server.port() + "/start/player2");
        final InputStream input = valid2.openStream();
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("choose\nnew\n1\nsimple.puzzle\n1\nsimple\nyo", result);
        
        
    }
    
    //covers chooseNewMatch()
    //      passes precondition
    @Test 
    public void testChooseNewMatchMultiple() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/warmup.puzzle/PlayWarmup");
        
        final InputStream input = valid.openStream();
        
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("wait", result);
        
    }
    
    //covers chooseNewMatch()
    //      failed precondition - non-unique matchID
    @Test 
    public void testChooseNewMatchNonUniqueMatch() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
  
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/warmup.puzzle/PlayWarmup").openStream();
        final URL valid = new URL("http://localhost:" + server.port() + "/choose/player2/thisMatch/warmup.puzzle/hi");
        
        
        final InputStream input = valid.openStream();
        
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("choose\ntry again\n3\ncomments.puzzle\nstandard.puzzle\nwarmup.puzzle\n1\nthisMatch\nPlayWarmup", result);
        
    }
    
    //covers chooseNewMatch()
    //      failed precondition - non-existing puzzleID
    @Test 
    public void testChooseNewMatchNonExistingPuzzle() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/choose/player2/thisMatch/aaa.puzzle/hi");

        final InputStream input = valid.openStream();
        
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("choose\ntry again\n3\ncomments.puzzle\nstandard.puzzle\nwarmup.puzzle\n0", result);
        
    }
    
    //covers testPlayMatch()
    //      failed precondition
    @Test 
    public void testPlayMatchFails() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/warmup.puzzle/hi").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/play/player2/thisMatchA");

        final InputStream input = valid.openStream();
        
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        assertEquals("choose\ntry again\n3\ncomments.puzzle\nstandard.puzzle\nwarmup.puzzle\n1\nthisMatch\nhi", result);
        
    }
    
    //covers testPlayMatch()
    //      passes precondition
    @Test 
    public void testPlayMatchPasses() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/comments.puzzle/hi").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/play/player2/thisMatch");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String result = getResult(reader);
        
        String expected = "play\n" + 
                "new\n" + 
                "player2\n" + 
                "0\n" + 
                "0\n" + 
                "player1\n" + 
                "0\n" + 
                "0\n" + 
                "2x4\n" + 
                "####\n" + 
                "????\n" + 
                "1\n" + 
                "1 0 ACROSS 1 false false \n" + 
                "\"twinkle twinkle //not\"";
        
        assertEquals(expected, result);
        
    }
    
    
    
    //covers exit()
    //      gamestate == choose
    @Test 
    public void testExitChoose() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/exit/choose/player1");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        assertTrue(reader.readLine() == null);
        
    }
    
    
    //covers exit()
    //      gamestate == wait
    @Test 
    public void testExitWait() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/comments.puzzle/hi").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/exit/wait/player1/thisMatch");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "choose\n" + 
                "update\n" + 
                "3\n" + 
                "comments.puzzle\n" + 
                "standard.puzzle\n" + 
                "warmup.puzzle\n" + 
                "0";
        
        assertEquals(expected, getResult(reader));
        
    }
    
    //covers exit()
    //      gamestate == play
    @Test 
    public void testExitPlay() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/comments.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/exit/play/player1/thisMatch");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "show_score\n" + 
                "player2\n" + 
                "player1\n" + 
                "0\n" + 
                "0\n" + 
                "player2\n" + 
                "0\n" + 
                "0";
        
        assertEquals(expected, getResult(reader));
        
    }
    
    //covers exit()
    //      gamestate == show_score
    @Test 
    public void testExitShowScore() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/comments.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
        new URL("http://localhost:" + server.port() + "/exit/play/player1/thisMatch").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/exit/show_score/player1");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        assertTrue(reader.readLine() == null);
        
    }
    
    //covers tryPlay()
    //      fails precondition
    @Test 
    public void testPlayBadPrecondition() throws IOException {
        
        final Server server = new Server("one-puzzle", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/comments.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/try/player1/notMatch/1/hi");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        assertTrue(reader.readLine() == null);
        
    }
    
    //covers tryPlay()
    //      passes precondition
    //      valid request, ongoing
    @Test 
    public void testPlayPassesPreconditionValidOngoing() throws IOException {
        
        final Server server = new Server("test-puzzles", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/verysimple.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/try/player1/thisMatch/1/star");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "play\n" + 
                "success\n" + 
                "player1\n" + 
                "0\n" + 
                "0\n" + 
                "player2\n" + 
                "0\n" + 
                "0\n" + 
                "6x4\n" + 
                "##?#\n" + 
                "star\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "2\n" + 
                "1 0 ACROSS 1 true false player1\n" + 
                "\"twinkle twinkle\"\n" + 
                "0 2 DOWN 2 false false \n" + 
                "\"Farmers ______\"";
        
        assertEquals(expected, getResult(reader));
    }
    
    
    //covers tryPlay()
    //      passes precondition
    //      valid request, finished
    @Test 
    public void testPlayPassesPreconditionValidFinished() throws IOException {
        
        final Server server = new Server("test-puzzles", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/verysimple.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
        new URL("http://localhost:" + server.port() + "/try/player1/thisMatch/1/star").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/try/player1/thisMatch/2/market");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "show_score\n" + 
                "player1\n" + 
                "player1\n" + 
                "2\n" + 
                "0\n" + 
                "player2\n" + 
                "0\n" + 
                "0";
        
        assertEquals(expected, getResult(reader));
    }
 
    
    //covers challenge()
    //      passes precondition, valid, ongoing
    @Test 
    public void testChallengePassesPreconditionValidOngoing() throws IOException {
        
        final Server server = new Server("test-puzzles", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/verysimple.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
        new URL("http://localhost:" + server.port() + "/try/player1/thisMatch/1/star").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/challenge/player2/thisMatch/1/stab");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "play\n" + 
                "lostch\n" + 
                "player2\n" + 
                "-1\n" + 
                "-1\n" + 
                "player1\n" + 
                "1\n" + 
                "0\n" + 
                "6x4\n" + 
                "##?#\n" + 
                "star\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "2\n" + 
                "1 0 ACROSS 1 true true player1\n" + 
                "\"twinkle twinkle\"\n" + 
                "0 2 DOWN 2 false false \n" + 
                "\"Farmers ______\"";
        
        
        
        assertEquals(expected, getResult(reader));
    }
    
    
    //covers challenge()
    //      passes precondition, invalid
    @Test 
    public void testChallengeInvalidPrecondition() throws IOException {
        
        final Server server = new Server("test-puzzles", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/verysimple.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
        new URL("http://localhost:" + server.port() + "/try/player1/thisMatch/1/star").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/challenge/player1/thisMatch/1/stab");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "play\n" + 
                "invalidch\n" + 
                "player1\n" + 
                "0\n" + 
                "0\n" + 
                "player2\n" + 
                "0\n" + 
                "0\n" + 
                "6x4\n" + 
                "##?#\n" + 
                "star\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "##?#\n" + 
                "2\n" + 
                "1 0 ACROSS 1 true false player1\n" + 
                "\"twinkle twinkle\"\n" + 
                "0 2 DOWN 2 false false \n" + 
                "\"Farmers ______\"";
        
        
        
        assertEquals(expected, getResult(reader));
    }
    
    
  //covers challenge()
    //      passes precondition, valid, finished
    @Test 
    public void testChallengePassesPreconditionValidFinished() throws IOException {
        
        final Server server = new Server("test-puzzles", 0);
        server.start();
        
        new URL("http://localhost:" + server.port() + "/start/player1").openStream();
        new URL("http://localhost:" + server.port() + "/choose/player1/thisMatch/verysimple.puzzle/hi").openStream();
        new URL("http://localhost:" + server.port() + "/start/player2").openStream();
        new URL("http://localhost:" + server.port() + "/play/player2/thisMatch").openStream();
        new URL("http://localhost:" + server.port() + "/try/player2/thisMatch/1/star").openStream();
        new URL("http://localhost:" + server.port() + "/try/player1/thisMatch/2/markeb").openStream();
  
        final URL valid = new URL("http://localhost:" + server.port() + "/challenge/player2/thisMatch/2/market");

        final InputStream input = valid.openStream();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));

        String expected = "show_score\n" + 
                "player2\n" + 
                "player2\n" + 
                "4\n" + 
                "2\n" + 
                "player1\n" + 
                "0\n" + 
                "0";
        
        
        assertEquals(expected, getResult(reader));
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
        
        result = result.substring(0, result.length()-1); //get rid of last newline character
        
        return result;
    }
    
}
