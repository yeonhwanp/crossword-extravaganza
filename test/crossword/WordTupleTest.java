package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import crossword.Cell.Exist;

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
    
    //covers equals() not equal - column
    @Test
    public void testEqualsNot() {
        
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
