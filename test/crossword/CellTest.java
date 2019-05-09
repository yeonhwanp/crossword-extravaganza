package crossword;

import crossword.Cell.Exist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


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
     *  cell's value changes (true), does not change (false)
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
    
    
}
