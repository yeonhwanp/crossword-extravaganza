package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class PlayerTest {

    /*
     * Testing strategy:
     * 
     * Test getID()
     * 
     * Test equals()
     *  equals, not equals
     *  
     * Test hashCode()
     * 
     */
    
    
    //covers getID()
    @Test
    public void testGetID() {
        
        Player player = new Player("hello");
        assertEquals("hello", player.getID());

    }
    
    
    //covers equals()
    //  equals
    @Test
    public void testEqualsYes() {
        
        Player player = new Player("hello");
        assertEquals(new Player("hello"), player);

    }
    
    //covers equals()
    //  not equals
    @Test
    public void testEqualsNo() {
        
        Player player = new Player("hello");
        assertTrue(!player.equals(new Player("helloa")));

    }
    
    //covers hashCode()
    @Test
    public void testHashcode() {
        
        Player player = new Player("a12");
        assertEquals(94786, player.hashCode());

    }
    
    
}
