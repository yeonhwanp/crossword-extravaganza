package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

/**
 * Tests for client manager
 *
 */
public class ClientManagerTest {

    /*
     * Testing Strategy
     * 
     * receiveResponse:
     *  - 0 lines
     *  - 1 line
     *  - > 1 line
     */
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    /*
     * Paritions covered: 0 lines
     */
    @Test
    public void testReceiveResponse0() throws IOException {
        final String readerText = "";
        final Reader inputReader = new StringReader(readerText);
        final BufferedReader bfReader = new BufferedReader(inputReader);
        
        final String result = ClientManager.receiveResponse(bfReader);
        final String expected = "";
        
        assertEquals(expected, result);
    }
    
    /*
     * Paritions covered: 1 lines
     */
    @Test
    public void testReceiveResponse1() throws IOException {
        final String readerText = "12 Hello! Nice to MEET you...";
        final Reader inputReader = new StringReader(readerText);
        final BufferedReader bfReader = new BufferedReader(inputReader);
        
        final String result = ClientManager.receiveResponse(bfReader);
        final String expected = readerText + "\n";
        
        assertEquals(expected, result);
    }
    /*
     * Paritions covered: >1 lines
     */
    @Test
    public void testReceiveResponseG1() throws IOException {
        final String readerText = "12 Hello! Nice to MEET you...\n wow PLEASE give us an A   \n  THIS is great??";
        final Reader inputReader = new StringReader(readerText);
        final BufferedReader bfReader = new BufferedReader(inputReader);
        
        final String result = ClientManager.receiveResponse(bfReader);
        final String expected = readerText + "\n";
        
        assertEquals(expected, result);
    }
}
