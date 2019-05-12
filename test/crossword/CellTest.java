package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import crossword.Cell.Exist;


/**
 * Tests for each cell
 *
 */
public class CellTest {

    /*
     * Testing strategy:
     * 
     * Test getRow()
     * 
     * Test getCol()
     * 
     * Test isPresent()
     *  is present, not present
     *  
     * Test isAbsent()
     *  is absent, not absent
     *  
     * Test changeValue()
     *  cell's value changes (true)
     *      passed in value is same as current value, or canChangeValue is true, no value entered yet
     *      this has already been changed
     *  does not change (false)
     *  
     * Test clearValue()
     *  cell's value clears (true), does not clear (false)
     *  
     * Test isOwned()
     *  is owned, is not owned
     *  
     * Test clearCorrespondingWords()
     * 
     * Test getCurrentValue()
     * 
     * Test isBlank()
     *  is blank, is not blank
     * 
     * Test addWord()
     * 
     * Test canChangeValue()
     *  can change, cannot change
     *  
     * Test toString()
     * 
     */
    
    //covers test getRow()
    @Test
    public void testGetRowSimple() {
        Cell tester = new Cell(1,2,Exist.ABSENT);
        assertEquals(1, tester.getRow());
    }
    
    //covers test getCol()
    @Test
    public void testGetColSimple() {
        Cell tester = new Cell(1,2,Exist.ABSENT);
        assertEquals(2, tester.getCol());
    }
    
    //covers test isPresent()
    //  not present
    @Test
    public void testIsPresentNotPresent() {
        Cell tester = new Cell(1,2,Exist.ABSENT);
        assertEquals(false, tester.isPresent());
    }
    
    //covers test isPresent()
    //  present
    @Test
    public void testIsPresentPresent() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        assertEquals(true, tester.isPresent());
    }
    
    //covers test isAbsent()
    //  is not absent
    @Test
    public void testIsAbsentNotAbsent() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        assertEquals(false, tester.isAbsent());
    }
    
    //covers test isAbsent()
    //  is absent
    @Test
    public void testIsAbsentAbsent() {
        Cell tester = new Cell(1,2,Exist.ABSENT);
        assertEquals(true, tester.isAbsent());
    }
    
    //covers changeValue(), value changes
    //  no value entered yet
    @Test
    public void testChangeValueNoneyet() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        assertTrue(tester.changeValue('h', player));
    }
    
    //covers changeValue(), value changes
    //  passed in value is same as current value
    @Test
    public void testChangeValueSame() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        tester.changeValue('h', player);
        assertTrue(tester.changeValue('h', player));
    }
    
    //covers changeValue(), value changes
    //  canChangeValue is true
    @Test
    public void testChangeValueCanChange() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        firstWord.setOwner(player);
        assertTrue(tester.changeValue('h', player));
    }
    
    //covers changeValue(), value changes
    //  canChangeValue is true, this has already been changed
    @Test
    public void testChangeValueCanChangeAgain() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        firstWord.setOwner(player);
        tester.addWord(firstWord);
        tester.changeValue('h', player);
        assertTrue(tester.changeValue('a', player));
    }
    
    //covers changeValue(), does not change
    @Test
    public void testChangeValueNoChange() {
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        firstWord.setOwner(player);
        firstWord.setConfirmed();
        tester.addWord(firstWord);
        assertTrue(!tester.changeValue('h', player));
    }
    
    
}
