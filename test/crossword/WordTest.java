package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import crossword.Cell.Exist;
import crossword.Word.Direction;
import crossword.Word.TryResult;

/**
 * Tests for mutable word object
 * @author christophercheung
 *
 */
public class WordTest {

    
    /* Testing strategy 
     *  Test getCorrectValue
     *  
     *  Test getCorrectCharAt
     *  
     *  Test isVertical - input is vertical, horizontal
     *  
     *  Test isHorizontal - input is vertical, horizontal
     *  
     *  Test getRowLowerBound - input is ACROSS, DOWN
     *  
     *  Test getRowUpperBound - input is ACROSS, DOWN
     *  
     *  Test getColumnLowerBound - input is ACROSS, DOWN
     *  
     *  Test getColumnUpperBound - input is ACROSS, DOWN
     *  
     *  Test getLength - empty word, non-empty
     *  
     *  Test isConfirmed - default (unchanged) value, confirmed
     *  
     *  Test setConfirmed
     *  
     *  Test toString - DOWN direction, ACROSS direction
     *  
     *  Test getOwner - default (unchanged) value: no owner, has owner, had an owner before (was cleared)
     *  
     *  Test setOwner - word originally had no owner, originally had an owner
     *  
     *  Test hasOwner - no owner, owner
     *  
     *  Test clearOwner
     *  
     *  Test addInvolvedCell - no cell has been added yet, another cell already exists
     *  
     *  Test getCurrentValue - no current value, current value exists
     *  
     *  Test checkConsistentInsert
     *   consistent insert
     *      nothing has been inserted yet
     *      changes own, previously entered word
     *   non-consistent insert
     *      already confirmed word
     *      wrong word size
     *  
     *  Test clearThisInsertedWord - no owner, yes owner
     *  
     *  Test tryInsertNewWord
     *   consistent insert
     *      nothing has been inserted yet
     *      changes own, previously entered word
     *   non-consistent insert
     *      already confirmed word
     *      wrong word size
     *  
     *  Test checkConsistentChallenge()
     *   Valid Challenge:
     *      None of the invalid cases below
     *   Invalid challenge:
     *      Challenger is trying to challenge own word
     *      Word to challenge is already confirmed
     *      Word to challenge is same as word you are proposing
     *      Proposed word is incorrect length
     *  
     *  Test tryChallenge
     *      Because tryChallenge takes in a Match, and because there is no way to retrieve the Word object we want to
     *      test on, we cannot directly call word.tryChallenge because we don't have access to these words. However, this method
     *      is called directly by Match.java's challenge() method, and the only difference is that challenge() first checks if
     *      the passed in wordID is an existing ID (see challenge() test cases).
     *      As such, all of tryChallenge's partitions are the same as the partitions described in MatchTest for challenge(), and
     *      we can test for tryChallenge based on these.
     *  
     *  
     */



    
    
    //covers getCorrectValue
    @Test
    public void testGetCorrectValue() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals("cat", firstWord.getCorrectValue());
    }
    
    //covers getCorrectCharAt
    @Test
    public void testGetCorrectCharAt() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals("cat", firstWord.getCorrectValue());
    }
    
    //covers isVertical - vertical input
    @Test
    public void testIsVerticalInputVertical() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertTrue(firstWord.isVertical());
    }
    
    //covers isVertical - horizontal input
    @Test
    public void testIsVerticalInputHorizontal() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertTrue(!firstWord.isVertical());
    }
    
    //covers isHorizontal - vertical input
    @Test
    public void testIsHorizontalInputVertical() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertTrue(!firstWord.isHorizontal());
    }
    
    //covers isHorizontal - horizontal input
    @Test
    public void testIsHorizontalInputHorizontal() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertTrue(firstWord.isHorizontal());
    }
    
    
    //covers getRowLowerBound - across
    @Test
    public void testGetRowLowerBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals(3, firstWord.getRowLowerBound());
    }
    
    //covers getRowLowerBound - down
    @Test
    public void testGetRowLowerBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertEquals(3, firstWord.getRowLowerBound());
    }
    
    //covers getRowUpperBound - across
    @Test
    public void testGetRowUpperBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals(3, firstWord.getRowUpperBound());
    }
    
    //covers getRowUpperBound - down
    @Test
    public void testGetRowUpperBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertEquals(5, firstWord.getRowUpperBound());
    }
    
    //covers getColumnLowerBound - across
    @Test
    public void testGetColumnLowerBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals(2, firstWord.getColumnLowerBound());
    }
    
    //covers getRowLowerBound - down
    @Test
    public void testGetColLowerBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertEquals(2, firstWord.getColumnLowerBound());
    }
    
    //covers getColumnUpperBound - across
    @Test
    public void testGetColumnUpperBoundAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals(4, firstWord.getColumnUpperBound());
    }
    
    //covers getColumnUpperBound - down
    @Test
    public void testGetColumnUpperBoundDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertEquals(2, firstWord.getColumnUpperBound());
    }
    
    //covers getLength
    //  nonempty
    @Test
    public void testGetLength() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertEquals(3, firstWord.getLength());
    }
    
    //covers getLength
    //  empty
    @Test
    public void testGetLengthEmpty() {
        Word firstWord = new Word(3, 2, "hint", 1, "", "DOWN");
        addCells(firstWord);
        assertEquals(0, firstWord.getLength());
    }
    
    //covers isConfirmed, not confirmed
    @Test
    public void testIsConfirmedDefault() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        assertTrue(!firstWord.isConfirmed());
    }
    
    //covers isConfirmed, confirmed
    //      covers setConfirmed
    @Test
    public void testIsConfirmedYes() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        
        Player player = new Player("hi");
        firstWord.setOwner(player);
        firstWord.tryInsertNewWord(player, "cat");
        firstWord.setConfirmed();
        assertTrue(firstWord.isConfirmed());
    }
    
    
    
    //covers toString, DOWN direction
    @Test
    public void testToStringDown() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "DOWN");
        addCells(firstWord);
        String expected = "1. cat at (3,2), in the DOWN direction, with the hint: hint";
        assertEquals(expected, firstWord.toString());
    }
    
    //covers toString, ACROSS direction
    @Test
    public void testToStringAcross() {
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        String expected = "1. cat at (3,2), in the ACROSS direction, with the hint: hint";
        assertEquals(expected, firstWord.toString());
    }
    
    //covers getOwner, no owner (default)
    @Test
    public void testGetOwnerDefault() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertThrows(RuntimeException.class, () -> { firstWord.getOwner(); });
    }
    
    //covers getOwner, yes owner
    @Test
    public void testGetOwnerYes() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        Player yo = new Player("yo");
        firstWord.setOwner(yo);
        assertEquals(yo, firstWord.getOwner());
    }
    
    //covers getOwner, no owner but had one before
    @Test
    public void testGetOwnerBefore() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        Player yo = new Player("yo");
        firstWord.setOwner(yo);
        firstWord.clearOwner();
        assertThrows(RuntimeException.class, () -> { firstWord.getOwner(); });
    }
    
    //covers setOwner, no owner before
    @Test
    public void testSetOwnerNoneBefore() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        Player yo = new Player("a");
        firstWord.setOwner(yo);
        assertEquals(yo, firstWord.getOwner());
    }
    
    //covers setOwner, yes owner before
    @Test
    public void testSetOwnerYesBefore() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        
        Player yo = new Player("a");
        Player a = new Player("bbb");
        firstWord.setOwner(yo);
        firstWord.setOwner(a);
        assertEquals(a, firstWord.getOwner());
    }
    
    //covers hasOwner, no owner
    @Test
    public void testHasOwnerNo() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals(false, firstWord.hasOwner());
    }
    
    //covers hasOwner, yes owner
    @Test
    public void testHasOwnerYes() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        Player yo = new Player("a");
        firstWord.setOwner(yo);
        assertEquals(true, firstWord.hasOwner());
    }
    
    //covers clearOwner
    @Test
    public void testClearOwner() {
        
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        Player yo = new Player("a");
        firstWord.setOwner(yo);
        firstWord.clearOwner();
        assertThrows(RuntimeException.class, () -> { firstWord.getOwner(); });
    }
    
    
    //covers getCurrentValue, no currentValue
    @Test
    public void testGetCurrentValueNone() {
        
        Word firstWord = new Word(0, 1, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals("???", firstWord.getCurrentValue());
        
    }
    
    //covers getCurrentValue, currentValue exists
    //  covers addInvolved no cell has been added yet, another cell already exists
    @Test
    public void testGetCurrentValueYes() {
        
        Word firstWord = new Word(0, 1, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        assertEquals("???", firstWord.getCurrentValue());
        
    }
    
    
    

    //covers checkConsistentInsert
    //      valid insert, changes own previously entered word
    //      valid insert, nothing has been inserted yet
    @Test
    public void testCheckConsistentInsertValidOwn() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        assertEquals(TryResult.SUCCESS, firstWord.checkConsistentInsert(player, "bbb"));
        
    }
    
    //covers checkConsistentInsert
    //      invalid insert, already confirmed word
    @Test
    public void testCheckConsistentInsertInvalidAlreadyConfirmed() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        firstWord.tryInsertNewWord(player, "cat");
        firstWord.setConfirmed();
        assertEquals(TryResult.INCONSISTENT_CURRENT, firstWord.checkConsistentInsert(player, "bbb"));
        
    }
    
    //covers checkConsistentInsert
    //      invalid insert, wrong word size
    @Test
    public void testCheckConsistentInsertInvalidWrongSize() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        assertEquals(TryResult.INCORRECT_LENGTH, firstWord.checkConsistentInsert(player, "c"));
        
    }
    
    //covers clearThisInsertedWord
    //      no owner
    @Test
    public void testClearThisInsertedWordNoOwner() {
        
        Word firstWord = makeCatWord();
        firstWord.clearThisInsertedWord();
        assertEquals("???", firstWord.getCurrentValue());
        
    }
    
    //covers clearThisInsertedWord
    //      yes owner
    @Test
    public void testClearThisInsertedWordYesOwner() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        firstWord.setOwner(player);
        firstWord.clearThisInsertedWord();
        assertEquals("???", firstWord.getCurrentValue());
        
    }
    

    //covers tryInsertNewWord
    //      valid insert, changes own previously entered word
    //      valid insert, nothing has been inserted yet
    @Test
    public void testTryInsertNewWordOwn() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        assertEquals(TryResult.SUCCESS, firstWord.tryInsertNewWord(player, "bbb"));
        assertEquals("bbb", firstWord.getCurrentValue());
        
    }
    
    //covers tryInsertNewWord
    //      invalid insert, already confirmed word
    @Test
    public void testTryInsertNewWordInvalidConfirmed() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        firstWord.tryInsertNewWord(player, "cat");
        firstWord.setConfirmed();
        assertEquals(TryResult.INCONSISTENT_CURRENT, firstWord.tryInsertNewWord(player, "bbb"));
        assertEquals("cat", firstWord.getCurrentValue());
        
    }
    
    //covers tryInsertNewWord
    //      invalid insert, wrong word size
    @Test
    public void testTryInsertNewWordInvalidSize() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        assertEquals(TryResult.INCORRECT_LENGTH, firstWord.tryInsertNewWord(player, "c"));
        
    }
    
    
    
    

    //covers checkConsistentChallenge valid challenge
    @Test
    public void testCheckConsistentChallengeValid() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        Player player2 = new Player("ahey");
        
        firstWord.tryInsertNewWord(player, "lol");
        assertTrue(firstWord.checkConsistentChallenge(player2, "dan"));
        
    }
    
    //covers checkConsistentChallenge invalid challenge
    //      own word
    @Test
    public void testCheckConsistentChallengeInvalidOwn() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        
        firstWord.tryInsertNewWord(player, "lol");
        assertTrue(!firstWord.checkConsistentChallenge(player, "dan"));
        
    }
    
    //covers checkConsistentChallenge invalid challenge
    //      already confirmed
    @Test
    public void testCheckConsistentChallengeInvalidConfirmed() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        Player player2 = new Player("aaa");
        firstWord.tryInsertNewWord(player, "cat");
        
        firstWord.setConfirmed();
        
        assertTrue(!firstWord.checkConsistentChallenge(player2, "dan"));
        
    }
    
    //covers checkConsistentChallenge invalid challenge
    //      same word proposing
    @Test
    public void testCheckConsistentChallengeInvalidSameWord() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        Player player2 = new Player("aaa");
        firstWord.tryInsertNewWord(player, "cat");
        
        assertTrue(!firstWord.checkConsistentChallenge(player2, "cat"));
        
    }
    
    //covers checkConsistentChallenge invalid challenge
    //      incorrect length
    @Test
    public void testCheckConsistentChallengeInvalidLength() {
        
        Word firstWord = makeCatWord();
        Player player = new Player("hey");
        Player player2 = new Player("aaa");
        firstWord.tryInsertNewWord(player, "cat");
        
        assertTrue(!firstWord.checkConsistentChallenge(player2, "d"));
        
    }
    
    
    /**
     * Manually add cells to the word so it is a valid word. Emulates the functionality of Match constructor,
     * which also adds cells to the word
     * @param word word to add cells to
     */
    private static void addCells(Word word) {
        
        final int rowLower = word.getRowLowerBound();
        final int rowHigher = word.getRowUpperBound();
        final int colLower = word.getColumnLowerBound();
        final int colHigher = word.getColumnUpperBound();
        
        if (word.getDirection() == Direction.ACROSS) {
           
            
            for (int i = colLower; i <= colHigher; i++) {
                Cell cellToAdd = new Cell(rowLower, i, Exist.PRESENT);
                word.addInvolvedCell(cellToAdd);
            }
        }
        else {
            
            for (int i = rowLower; i <= rowHigher; i++) {
                Cell cellToAdd = new Cell(i, colLower, Exist.PRESENT);
                word.addInvolvedCell(cellToAdd);
            }
        }
        
        
    }
    
    /**
     * Method to make cat word, with attached cells
     * @return cat word with cells attached
     */
    private static Word makeCatWord() {
        Word firstWord = new Word(0, 0, "hint", 1, "cat", "ACROSS");
        addCells(firstWord);
        return firstWord;
    }
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
   
    
    
    
    
}
