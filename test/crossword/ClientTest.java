package crossword;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ClientTest {

    /*
     * Testing Strategy
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
    
    
}
