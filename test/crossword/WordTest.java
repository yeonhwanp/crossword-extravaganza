package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class WordTest {

    
    // Testing strategy 
    //
    //  Test getCorrectValue
    //  Test getCorrectCharAt
    //  Test isVertical - input is vertical, horizontal
    //  Test isHorizontal - input is vertical, horizontal
    //  Test getRowLowerBound - input is ACROSS, DOWN
    //  Test getRowUpperBound - input is ACROSS, DOWN
    //  Test getColumnLowerBound - input is ACROSS, DOWN
    //  Test getColumnUpperBound - input is ACROSS, DOWN
    //  Test getLength
    //  Test isConfirmed - default (unchanged) value
    //  Test toString
    //
    //

    
    //covers getCorrectValue
    @Test
    public void testGetCorrectValue() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertEquals("cat", firstWord.getCorrectValue());
    }
    
    //covers getCorrectCharAt
    @Test
    public void testGetCorrectCharAt() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertEquals("cat", firstWord.getCorrectValue());
    }
    
    //covers isVertical - vertical input
    @Test
    public void testIsVerticalInputVertical() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertTrue(firstWord.isVertical());
    }
    
    //covers isVertical - horizontal input
    @Test
    public void testIsVerticalInputHorizontal() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertTrue(!firstWord.isVertical());
    }
    
    //covers isHorizontal - vertical input
    @Test
    public void testIsHorizontalInputVertical() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertTrue(!firstWord.isHorizontal());
    }
    
    //covers isHorizontal - horizontal input
    @Test
    public void testIsHorizontalInputHorizontal() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertTrue(firstWord.isHorizontal());
    }
    
    
    //covers getRowLowerBound - across
    @Test
    public void testGetRowLowerBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertEquals(3, firstWord.getRowLowerBound());
    }
    
    //covers getRowLowerBound - down
    @Test
    public void testGetRowLowerBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertEquals(3, firstWord.getRowLowerBound());
    }
    
    //covers getRowUpperBound - across
    @Test
    public void testGetRowUpperBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertEquals(3, firstWord.getRowUpperBound());
    }
    
    //covers getRowUpperBound - down
    @Test
    public void testGetRowUpperBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertEquals(5, firstWord.getRowUpperBound());
    }
    
    //covers getColumnLowerBound - across
    @Test
    public void testGetColumnLowerBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertEquals(2, firstWord.getColumnLowerBound());
    }
    
    //covers getRowLowerBound - down
    @Test
    public void testGetColLowerBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertEquals(2, firstWord.getColumnLowerBound());
    }
    
    //covers getColumnUpperBound - across
    @Test
    public void testGetColumnUpperBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        assertEquals(4, firstWord.getColumnUpperBound());
    }
    
    //covers getColumnUpperBound - down
    @Test
    public void testGetColumnUpperBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertEquals(2, firstWord.getColumnUpperBound());
    }
    
    //covers getLength
    @Test
    public void testGetLength() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertEquals(3, firstWord.getLength());
    }
    
    //covers isConfirmed
    @Test
    public void testIsConfirmedDefault() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        assertTrue(!firstWord.isConfirmed());
    }
    
    //covers toString
    @Test
    public void testToString() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        String expected = "1. cat at (3,2), in the DOWN direction, with the hint: hint";
        assertEquals(expected, firstWord.toString());
    }
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
    
    
}
