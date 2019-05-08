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

/**
 * Tests for client
 * @author christophercheung
 *
 */
public class ClientTest {

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
    
    @Test
    public void testNew00() throws IOException {
        final Client testClient = new Client();
        
        final String input = "NEW\n0\n0";
        final String expected = "0\n0\n";
        final Reader inputReader = new StringReader(input);
        
        final BufferedReader reader = new BufferedReader(inputReader);
        
        testClient.receiveChoose(reader);
        final String response = testClient.getMatchList();
        
        assertEquals(expected, response);
    }
    
    @Test
    public void testNew10() throws IOException {
        final Client testClient = new Client();
        
        final String input = "NEW\n1\nHello.PUZZLE\n0";
        final String expected = "1\nHello.PUZZLE\n0\n";
        final Reader inputReader = new StringReader(input);
        
        final BufferedReader reader = new BufferedReader(inputReader);
        
        testClient.receiveChoose(reader);
        final String response = testClient.getMatchList();
        
        assertEquals(expected, response);
    }
    
    @Test
    public void testNewG10() throws IOException {
        final Client testClient = new Client();
        
        final String input = "NEW\n3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n0";
        final String expected = "3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n0\n";
        final Reader inputReader = new StringReader(input);
        
        final BufferedReader reader = new BufferedReader(inputReader);
        
        testClient.receiveChoose(reader);
        final String response = testClient.getMatchList();
        
        assertEquals(expected, response);
    }
    
    @Test
    public void testNew01() throws IOException {
        final Client testClient = new Client();
        
        final String input = "NEW\n0\n1\nwow_running.pi";
        final String expected = "0\n1\nwow_running.pi\n";
        final Reader inputReader = new StringReader(input);
        
        final BufferedReader reader = new BufferedReader(inputReader);
        
        testClient.receiveChoose(reader);
        final String response = testClient.getMatchList();
        
        assertEquals(expected, response);
    }
    
    @Test
    public void testNew0G1() throws IOException {
        final Client testClient = new Client();
        
        final String input = "NEW\n0\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img";
        final String expected = "0\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img\n";
        final Reader inputReader = new StringReader(input);
        
        final BufferedReader reader = new BufferedReader(inputReader);
        
        testClient.receiveChoose(reader);
        final String response = testClient.getMatchList();
        
        assertEquals(expected, response);
    }
    
    @Test
    public void testNewG1G1() throws IOException {
        final Client testClient = new Client();
        
        final String input = "NEW\n3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img";
        final String expected = "3\nHello.PUZZLE\nlmao.hi\nPLSgiveA\n4\nwow_running.pi\nitIS5AM\nsleep.isgood\nPLEASEhelp.img\n";
        
        final Reader inputReader = new StringReader(input);
        
        final BufferedReader reader = new BufferedReader(inputReader);
        
        testClient.receiveChoose(reader);
        final String response = testClient.getMatchList();
        
        assertEquals(expected, response);
    }
}
