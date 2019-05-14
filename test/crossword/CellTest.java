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
     *  correspondingWord size: 0, 1, >1
     * 
     * Test getCurrentValue()
     *  is a absent cell
     *  is a ?
     *  is a lettered cell
     * 
     * Test isBlank()
     *  is blank, is not blank
     * 
     * Test addWord() - covered by a few overlapping test cases like testChangeValueNoChange()
     * 
     * Test canChangeValue()
     *  can change, cannot change
     *  
     * Test toString()
     *  contains absent cell
     *  contains ? cell
     *  contains lettered cell
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
    //  covers addWord()
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
    //  covers addWord()
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
    
    
    //covers clearValue()
    //   value does clear
    //  covers addWord()
    @Test
    public void testClearValueYesClear() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        Player player = new Player("hey");
        tester.addWord(firstWord);
        tester.changeValue('c', player);
        
        assertTrue(tester.clearValue());
        assertEquals('?', tester.getCurrentValue());
        
    }
    
    //covers isOwned
    //   no owner
    //  covers addWord()
    @Test
    public void testIsOwnedNone() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        tester.addWord(firstWord);
        
        assertTrue(!tester.isOwned());
        
    }
    
    //covers isOwned
    //   yes owner
    @Test
    public void testIsOwnedYes() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        Player player = new Player("hey");
        tester.addWord(firstWord);
        tester.changeValue('c', player);
        firstWord.setOwner(player);
        
        assertTrue(tester.isOwned());
        
    }
    
    
    //covers clearValue()
    //   value does not clear
    @Test
    public void testClearValueNoClear() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        Player player = new Player("hey");
        tester.addWord(firstWord);
        firstWord.setOwner(player);
        
        assertTrue(!tester.clearValue());
        
    }
    
    
    //covers clearCorrespondingWords()
    //  size: 0
    @Test
    public void testClearCorrespondingWords0() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        
        tester.clearCorrespondingWords();
        
        assertEquals('?', tester.getCurrentValue());
        
    }
    
    //covers clearCorrespondingWords()
    //  size: 1
    @Test
    public void testClearCorrespondingWords1() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Word firstWord = new Word(1, 2, "hint", 1, "ca", "ACROSS");
        Player player = new Player("hey");
        tester.addWord(firstWord);
        firstWord.setOwner(player);
        firstWord.addInvolvedCell(tester);
        firstWord.addInvolvedCell(new Cell(1,3,Exist.PRESENT));
        firstWord.tryInsertNewWord(player, "ca");
        
        assertEquals('c', tester.getCurrentValue());
        
        tester.clearCorrespondingWords();
        
        assertEquals('?', tester.getCurrentValue());
        
    }
    
    //covers clearCorrespondingWords()
    //  size: >1
    @Test
    public void testClearCorrespondingWordsMoreOne() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Word firstWord = new Word(1, 2, "hint", 1, "aw", "ACROSS");
        Word secondWord = new Word(0, 2, "hint", 1, "ca", "DOWN");
        Player player = new Player("hey");
        
        tester.addWord(firstWord);
        firstWord.setOwner(player);
        firstWord.addInvolvedCell(tester);
        firstWord.addInvolvedCell(new Cell(1,3,Exist.PRESENT));
        firstWord.tryInsertNewWord(player, "aw");
        
        tester.addWord(secondWord);
        secondWord.setOwner(player);
        secondWord.addInvolvedCell(new Cell(0,2,Exist.PRESENT));
        secondWord.addInvolvedCell(tester);
        secondWord.tryInsertNewWord(player, "da");
        
        assertEquals('a', tester.getCurrentValue());
        
        tester.clearCorrespondingWords();
        
        assertEquals('?', tester.getCurrentValue());
        
    }
    
    //covers getCurrentValue()
    //  is a ?
    @Test
    public void testGetCurrentValue() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        
        assertEquals('?', tester.getCurrentValue());
        
    }
    
    //covers getCurrentValue()
    //  is absent
    @Test
    public void testGetCurrentValueAbsent() {
        
        Cell tester = new Cell(1,2,Exist.ABSENT);
        
        assertEquals('?', tester.getCurrentValue());
        
    }
    
    //covers getCurrentValue()
    //  is lettered cell
    @Test
    public void testGetCurrentValueLettered() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        tester.changeValue('h', player);
        assertEquals('h', tester.getCurrentValue());
        
    }
    
    //covers isBlank()
    //  is blank
    @Test
    public void testIsBlankYes() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        assertTrue(tester.isBlank());
        
    }
    
    //covers isBlank()
    //  is not blank
    @Test
    public void testIsBlankNo() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        tester.changeValue('h', new Player("yo"));
        assertTrue(!tester.isBlank());
        
    }
    
    
    //covers canChangeValue()
    //  can change
    @Test
    public void testCanChangeValueYes() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        firstWord.setOwner(player);
        tester.addWord(firstWord);
        
        assertTrue(tester.canChangeValue(player));
    }
    
    //covers canChangeValue()
    //  cannot change
    @Test
    public void testCanChangeValueNo() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        Player player = new Player("hi");
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        firstWord.setOwner(player);
        firstWord.setConfirmed();
        tester.addWord(firstWord);
        
        assertTrue(!tester.canChangeValue(player));
    }
    
    //covers toString
    //  contains absent cell
    @Test
    public void testToStringAbsent() {
        
        Cell tester = new Cell(1,2,Exist.ABSENT);
        assertEquals("#", tester.toString());
        
    }
    
    //covers toString
    //  contains ? cell
    @Test
    public void testToStringQuestion() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        assertEquals("?", tester.toString());
        
    }
    
    //covers toString
    //  contains lettered cell
    @Test
    public void testToStringLettered() {
        
        Cell tester = new Cell(1,2,Exist.PRESENT);
        tester.changeValue('a', new Player("hi"));
        assertEquals("a", tester.toString());
        
    }
    
}
