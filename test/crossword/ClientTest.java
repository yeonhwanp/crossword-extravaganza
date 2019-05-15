package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;

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
     * Testing Strategy for Client.parseResponse():
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
     *  - receiveEnd
     *  
     *  Testing Strategy for getUserID(): Same, try to change (w/out using new)
     *  Testing Strategy for getMatchID(): Same, try to change (w/out using new)
     *  Testing Strategy for getState(): START, CHOOSE, WAIT, PLAY, SHOW_SCORE
     *  Testing Strategy for getBoardText(): same board, different board
     *  Testing Strategy for getMatches(): same matchlist, different matchlist
     */
    
    /*
     * receiveChoose(): new game with 0 files
     * getState(): START
     * getUserID(): Same
     */
    @Test
    public void testReceiveStart0() throws IOException, InterruptedException {
        
        final Client testClient = new Client(HOST, PORT);
        
        // Always need this
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START Player1";
        testClient.parseUserInput(userInput);
        
        assertEquals(ClientState.START, testClient.getState());
        
        String serverResponse = "choose\nnew\n0\n0";
        testClient.parseResponse(serverResponse, userInput);
        assertEquals("Player1", testClient.getUserID(), "Expected correct id");
        assertEquals("0\n0\n", testClient.getMatches(), "Expected correct matches");
        
    }
    
    /*
     * receiveChoose: new game with 1 file
     * getState(): CHOOSE
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
        assertEquals(ClientState.CHOOSE, testClient.getState());
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
     * getMatchID(): Try to change
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
     * getMatches()
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
     * getBoardText(): same
     * getState(): PLAY
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
        assertEquals(ClientState.PLAY, testClient.getState());
    }
    
    /*
     * receivePlay: update
     * getBoardText(): Different
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
        
        String serverResponse3 = "play\nupdate\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS true false chris\nmyclue";
        testClient.parseResponse(serverResponse3, userInput2);
        
        assertEquals("willy\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS true false chris\nmyclue", testClient.getBoardText(), "Expected correct matches");
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
     * getState(): SHOW_SCORE
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
        
        assertEquals(ClientState.SHOW_SCORE, testClient.getState());
    }
    
    /*
     * Testing Strategy for Client.parseUserInput():
     *  Possible raw Inputs:
     *      - START: 
     *      - NEW 
     *      - PLAY 
     *      - EXIT:
     *          - From WAIT (manual), PLAY, CHOOSE, SEND_SCORE
     *          - For wait, the user can test it by first opening up the client, 
     *                      entering START testID, then entering NEW matchid somepuzzle.puzzle "desc",
     *                      and when the user enters the waiting state, the user should enter EXIT and
     *                      be redirected back to the CHOOSE state.
     *      - TRY
     *      - CHALLENGE  
     */
    
    // Covers Client.parseUserInput(): Raw input: START
    @Test
    public void testParseUserStart() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        testClient.parseResponse(receiveInit, "");
        String testString = testClient.parseUserInput("START test");
        assertEquals("/start/test", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: NEW
    @Test
    public void testParseUserNEW() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String testString = testClient.parseUserInput("NEW testID thisIsgreat.puzzle \"hello\"");
        assertEquals("/choose/player25/testID/thisIsgreat.puzzle/hello", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: PLAY
    @Test
    public void testParseUserPLAY() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String testString = testClient.parseUserInput("PLAY testID");
        assertEquals("/play/player25/testID", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: EXIT when in CHOOSE
    @Test
    public void testParseUserExitChoose() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String testString = testClient.parseUserInput("EXIT");
        assertEquals("/exit/choose/player25", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: EXIT when in PLAY
    @Test
    public void testParseUserExitPlay() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY something";
        testClient.parseUserInput(userInput2);
        
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse2, userInput2);
        
        String testString = testClient.parseUserInput("EXIT");
        assertEquals("/exit/play/player25/something", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: TRY
    @Test
    public void testParseUserTry() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY something";
        testClient.parseUserInput(userInput2);
        
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse2, userInput2);
        
        String testString = testClient.parseUserInput("TRY 1 DOO");
        assertEquals("/try/player25/something/1/DOO", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: CHALLENGE
    @Test
    public void testParseUserChallenge() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY something";
        testClient.parseUserInput(userInput2);
        
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse2, userInput2);
        
        String testString = testClient.parseUserInput("CHALLENGE 1 DOO");
        assertEquals("/challenge/player25/something/1/DOO", testString);
    }
    
    // Covers Client.parseUserInput(): Raw input: EXIT when in SEND_SCORE
    @Test
    public void testParseUserExitShowScore() throws IOException {
        
        // Always need this
        final Client testClient = new Client(HOST, PORT);
        String receiveInit = "start\nnew game";
        
        testClient.parseResponse(receiveInit, "");
        
        String userInput = "START player25";
        testClient.parseUserInput(userInput);
        
        String serverResponse = "choose\nnew\n1\nthisIsgreat.puzzle\n1\nCURRENT\nlmao\n";
        testClient.parseResponse(serverResponse, userInput);
        
        String userInput2 = "PLAY something";
        testClient.parseUserInput(userInput2);
        
        String serverResponse2 = "play\nnew\nwilly\n0\n0\nchris\n0\n0\n1x4\n????\n1\n0 0 ACROSS false false\nmyclue";
        testClient.parseResponse(serverResponse2, userInput2);
        
        testClient.parseUserInput("EXIT");
        String serverResponse3 = "show_score\nwilly\nwilly\n3\n0\nchris\n0\n0";
        testClient.parseResponse(serverResponse3, "EXIT");
        String testString2 = testClient.parseUserInput("EXIT");
        assertEquals("/exit/show_score/player25", testString2);
    }
    
    
    
    
}
