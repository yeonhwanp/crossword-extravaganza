package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import crossword.Client.ClientState;

/**
 * Tests for client
 *
 */
public class ClientTest {
    
    private static final String HOST = "localhost";
    private static final int PORT = 4949;
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }

    
    /*
     * Testing Strategy
     * 
     * parseResponse:
     *  - receiveChoose:
     *    a. new game - 0, 1, >1 files in server responses
     *    b. try again - 0, 1, >1 matches in server responses
     *  - receiveWait:
     *    a. MANUAL TEST: check to make sure that it waits for another player to connect
     *    b. MANUAL TEST: check to make sure that the only valid command is exit
     *  - receivePlay:
     *    a. Provided parameter:
     *      - new
     *      - update
     *      - wrong_id
     *      - incorrect_length
     *      - inconsistent_current
     *      - success
     *      - wonch
     *      - lostch
     *      - invalidch
     *  - receiveEnd:
     *    
     * 
     */
    
    
    /*
     * receiveChoose: new game with 0 files
     */
    @Test
    public void testReceiveStart0() throws IOException, InterruptedException {
        
        final Client testClient = new Client(HOST, PORT);
        
        // Always need this
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START Player1";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n0\n0";
        testClient.parseResponse(serverResponse, userInput);
        assertEquals("Player1", testClient.getUserID(), "Expected correct id");
        assertEquals("0\n0\n", testClient.getMatches(), "Expected correct matches");
        
    }
    
    /*
     * receiveChoose: new game with 1 file
     */
    @Test
    public void testReceiveStart1() throws IOException {
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
    }
    
    /*
     * receiveChoose: new game with >1 file
     */
    @Test
    public void testReceiveStartG1() throws IOException {
       
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START hmmm";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n3\nthisIsgreat.puzzle\nHELLO.puzzle\nPlsGiveUsA.puzzle\n2\nCURRENT\nlmao\nTESTing\nMhm\n";
        testClient.parseResponse(serverResponse, userInput);
        
        assertEquals("hmmm", testClient.getUserID(), "Expected correct id");
        assertEquals("3\nthisIsgreat.puzzle\nHELLO.puzzle\nPlsGiveUsA.puzzle\n2\nCURRENT\nlmao\nTESTing\nMhm", testClient.getMatches(), "Expected correct matches");
    }
    
    /*
     * receiveChoose: try again - 0 matches
     */
    @Test
    public void testReceiveChooseTryAgain0() throws IOException {

        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START hmmm";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n3\nthisIsgreat.puzzle\nHELLO.puzzle\nPlsGiveUsA.puzzle\n2\nCURRENT\nlmao\nTESTing\nMhm\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY whatever.puzzle";
        String serverResponse2 = "choose\ntry again\n0\n0\n";
           
        testClient.parseResponse(serverResponse2, userInput2);

        assertEquals("hmmm", testClient.getUserID(), "Expected correct id");
        assertEquals("0\n0\n", testClient.getMatches(), "Expected correct matches");
    }
    
    /*
     * receiveChoose: try again - 1 match
     */
    @Test
    public void testReceiveChooseTryAgain1() throws IOException {
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START Player3";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n0\n0";
        
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY whatever.puzzle";
        String serverResponse2 = "choose\ntry again\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
            
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("Player3", testClient.getUserID(), "Expected correct id");
        assertEquals("1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao", testClient.getMatches(), "Expected correct matches");
    }
    
    /*
     * receiveChoose: try again - >1 match
     */
    @Test
    public void testReceiveChooseTryAgainG1() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY WHaaTTTEee.puzzle";
        String serverResponse2 = "choose\ntry again\n3\nthisIsgreat.puzzle\nHELLO.puzzle\nPlsGiveUsA.puzzle\n2\nCURRENT\nlmao\nTESTing\nMhm\n";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("3\nthisIsgreat.puzzle\nHELLO.puzzle\nPlsGiveUsA.puzzle\n2\nCURRENT\nlmao\nTESTing\nMhm", testClient.getMatches(), "Expected correct matches");
    }
    
    /*
     * receivePlay: new
     */
    @Test
    public void testReceivePlayNew() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: update
     */
    @Test
    public void testReceivePlayUpdate() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\nupdate\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: wrong_id
     */
    @Test
    public void testReceivePlayWrongID() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\nwrong_id\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: incorrect_length
     */
    @Test
    public void testReceivePlayIncorrectLength() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\nincorrect_length\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: inconsistent_current
     */
    @Test
    public void testReceivePlayInconsistent() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\ninconsistent_current\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: success
     */
    @Test
    public void testReceivePlaySuccess() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\nsuccess\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: success
     */
    @Test
    public void testReceivePlayWonch() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player23";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\nwonch\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: lostch
     */
    @Test
    public void testReceivePlayLostch() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START christopher";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("christopher", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\nlostch\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receivePlay: lostch
     */
    @Test
    public void testReceivePlayInvalidch() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player25", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "play\ninvalidch\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    /*
     * receiveEnd
     */
    @Test
    public void testReceivePlayRecieveEnd() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY thisIsgreatpuzzle";
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        
        testClient.parseResponse(serverResponse2, userInput2);
        
        assertEquals("player25", testClient.getUserID(), "Expected correct id");
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
        
        String serverResponse3 = "show_score\nwilly\nwilly\n3\n0\nchris\n0\n0";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue", testClient.getBoardText(), "Expected correct matches");
    }
    
    
    
    
    
}
