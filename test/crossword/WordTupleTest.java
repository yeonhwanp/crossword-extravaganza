package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WordTupleTest {

    /*
     * Testing strategy:
     * 
     * Test getWord()
     * 
     * Test getHint()
     * 
     * Test getDirection() - ACROSS, DOWN
     * 
     * Test getRow()
     * 
     * Test getCol()
     * 
     * Test equals() - equals, not equals
     *  not equals differences:
     *      word
     *      hint
     *      direction
     *      startRow
     *      startCol
     * 
     * Test hashCode()
     * 
     * 
     */
    
    
    //covers getWord()
    @Test
    public void testGetWord() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals("cat", first.getWord());
        
    }
    
    //covers getHint()
    @Test
    public void testGetHint() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals("hint", first.getHint());
        
    }
    
    //covers getDirection() - across
    @Test
    public void testGetDirectionAcross() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "ACROSS");
        assertEquals("ACROSS", first.getDirection());
        
    }
    
    //covers getDirection() - down
    @Test
    public void testGetDirectionDown() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals("DOWN", first.getDirection());
        
    }
    
    //covers getRow()
    @Test
    public void testGetRow() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals(0, first.getRow());
        
    }
    
    //covers getCol()
    @Test
    public void testGetCol() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals(1, first.getCol());
        
    }
    
    //covers equals() is equal
    @Test
    public void testEqualsYes() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        WordTuple second = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals(first, second);
        
    }
    
    //covers equals() not equal - word
    @Test
    public void testEqualsNotWord() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "a", "DOWN");
        WordTuple second = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertTrue(!first.equals(second));
        
    }
    
    //covers equals() not equal - hint
    @Test
    public void testEqualsNotHint() {
        
        WordTuple first = new WordTuple(0, 1, "a", "cat", "DOWN");
        WordTuple second = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertTrue(!first.equals(second));
        
    }
    
    //covers equals() not equal - direction
    @Test
    public void testEqualsNotDirection() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "ACROSS");
        WordTuple second = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertTrue(!first.equals(second));
        
    }
    
    //covers equals() not equal - row
    @Test
    public void testEqualsNotRow() {
        
        WordTuple first = new WordTuple(1, 1, "hint", "cat", "DOWN");
        WordTuple second = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertTrue(!first.equals(second));
        
    }
    
    //covers equals() not equal - column
    @Test
    public void testEqualsNotCol() {
        
        WordTuple first = new WordTuple(0, 2, "hint", "cat", "DOWN");
        WordTuple second = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertTrue(!first.equals(second));
        
    }
    
    //covers hashCode()
    @Test
    public void testHashcode() {
        
        WordTuple first = new WordTuple(0, 1, "hint", "cat", "DOWN");
        assertEquals(32848, first.hashCode());
        
    }
    
    
}
