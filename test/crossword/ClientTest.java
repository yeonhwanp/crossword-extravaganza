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

import org.junit.jupiter.api.Test;

import crossword.Client.ClientState;

/**
 * Tests for client
 *
 */
public class ClientTest {
    
    private static String HOST = "localhost";
    private static int PORT = 4949;

    /*
     * Testing Strategy
     * 
     * receiveChoose:
     *  - NEW:
     *      - number of matches: 0, 1, >1
     *      - ongoing matches: 0, 1, >1
     *  - TRY AGAIN:
     *      - number of matches: 0, 1, >1
     *      
     * 
     * Manual test:
     *  - connect to server at port 4949
     *  - verify that the displayed board is consistent with the warmup.puzzle in one-puzzle
     *  - verify that for each word on the board, the number of cells in that direction are exactly the length of the word
     *  - verify that the hints are displayed
     */
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (0, 0)
//     */
//    @Test
//    public void testNew00() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n0\n0";
//        final String expected = "0\n0\n";
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
//    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (1, 0)
//     */
//    @Test
//    public void testNew10() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n1\nHello.PUZZLE\n0";
//        final String expected = "1\nHello.PUZZLE\n0\n";
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
//    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (>1, 0)
//     */
//    @Test
//    public void testNewG10() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n0";
//        final String expected = "3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n0\n";
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
//    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (0, 1)
//     */
//    @Test
//    public void testNew01() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n0\n1\nwow_running.pi";
//        final String expected = "0\n1\nwow_running.pi\n";
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
//    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (0, >1)
//     */
//    @Test
//    public void testNew0G1() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n0\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img";
//        final String expected = "0\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img\n";
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
//    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (>1, >1)
//     */
//    @Test
//    public void testNewG1G1() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img";
//        final String expected = "3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img\n";
//        
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
//    
//    /*
//     * Partitions covered:
//     * receiveChoose: NEW (>1, >1)
//     */
//    @Test
//    public void testChooseTryAgainG1G1() throws IOException {
//        final Client testClient = new Client();
//        
//        final String input = "NEW\n3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img";
//        final String expected = "3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img\n";
//        
//        final Reader inputReader = new StringReader(input);
//        
//        final BufferedReader reader = new BufferedReader(inputReader);
//        
//        testClient.receiveChoose(reader);
//        final String response = testClient.getMatchList();
//        
//        assertEquals(expected, response);
//    }
    
    // more tests for receiveChoose's TRY AGAIN
    
    
    
    
    
    
    
    
    
    
    
    /*
     * Testing Strategy
     * 
     * receiveResponse:
     *  - receiveChoose:
     *    a. new game - 0, 1, >1 files in server responses
     *    b. try again - 0, 1, >1 matches in server responses
     *  - receiveWait:
     *    a. MANUAL TEST: check to make sure that it waits for another player to connect
     *    b. MANUAL TEST: check to make sure that the only valid command is exit
     *  - receivePlay:
     *    a. 
     *  - receiveEnd:
     *    a. 
     *    
     * 
     */
    
    
    /*
     * receiveChoose: new game with 0 files
     */
    @Test
    public void testReceiveStart0() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
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
        
        assertEquals("player23", testClient.getUserID(), "Expected correct id");
        assertEquals("1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao", testClient.getMatches(), "Expected correct matches");
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
    
//    /*
//     * receiveWait: check updated game state
//     */
//    @Test
//    public void testReceiveWait() throws IOException {
//        
//        // Always need this
//        final Client testClient = new Client(HOST, PORT);
//        String receiveInit = "start\nnew game";
//        testClient.parseResponse(receiveInit, "");
//        
//        String userInput = "START player23";
//        testClient.parseUserInput(userInput);
//        
//        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
//        testClient.parseResponse(serverResponse, userInput);
//        
//        String userInput2 = "NEW WHaaTTTEee.puzzle";
//        String serverResponse2 = "wait";
//        
//        testClient.parseResponse(serverResponse2, userInput2);
//        
//        assertEquals(ClientState.WAIT, testClient.getState(), "Expected correct state");
//    }
    
    
}
