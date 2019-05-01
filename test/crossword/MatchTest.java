package crossword;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


public class MatchTest {


    // Testing strategy for checkConsistency():
    //  Partition the input as follows:
    //
    //      Size of words: 0, 1, 2, >2
    //      Type of directions: all across, all down, both across and down
    //          all across: different rows, same row
    //              same row: no overlap, yes overlap
    //          all down: different columns, same column
    //              same column: no overlap, yes overlap
    //          both across and down: no overlap, yes overlap
    //              Yes overlap with same letter, different letter
    //              Down word compared with across, across word compared with down
    //              Number of overlaps: 0, 1, >1
    //          
    //
    // Testing strategy for toString():
    //  Partition the input as follows:
    //      Type of match name: empty string, non-empty string
    //      Type of match description: empty string, non-empty string
    //      Number of entries: 0, 1, >1
    //      
    //          
    //

    
    //covers size of words: 0
    @Test
    public void testCheckConsistencyEmpty() {
        List<Word> words = new ArrayList<>();
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 1
    @Test
    public void testCheckConsistencyOneWord() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        words.add(firstWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both across, different rows
    @Test
    public void testCheckConsistencyAcrossAcossDifferentRows() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both across, same rows, no overlap
    @Test
    public void testCheckConsistencyAcrossAcossSameRowsNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 200, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both across, same rows, yes overlap
    @Test
    public void testCheckConsistencyAcrossAcossSameRowsYesOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 1, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both down, different columns, no overlap
    @Test
    public void testCheckConsistencyDownDownDifferentColumns() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "DOWN");
        Word secondWord = new Word(1, 1, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both down, same columns, no overlap
    @Test
    public void testCheckConsistencyDownDownSameColumnsNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(100, 2, "hint", 1, "cat", "DOWN");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both down, same columns, no overlap
    @Test
    public void testCheckConsistencyDownDownSameColumnsYesOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(5, 2, "hint", 1, "cat", "DOWN");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one across then one down, no overlap
    //      Number of overlaps: 0
    @Test
    public void testCheckConsistencyAcrossDownNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(100, 200, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one across one down, yes overlap
    //      Number of overlaps: 1
    @Test
    public void testCheckConsistencyAcrossDownYesOverlapDifferentLetter() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one across one down, yes overlap, same letter
    @Test
    public void testCheckConsistencyAcrossDownYesOverlapSameLetter() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 0, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(0, 1, "hint", 1, "daa", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossYesOverlapDifferentLetter() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(0, 1, "hint", 1, "dba", "DOWN");
        Word secondWord = new Word(1, 0, "hint", 1, "cat", "ACROSS");
        
        words.add(firstWord);
        words.add(secondWord);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossMoreWordsYesOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(0, 1, "hint", 1, "dba", "DOWN");
        Word secondWord = new Word(1, 0, "hint", 1, "cat", "ACROSS");
        Word thirdWord = new Word(2, 0, "hint", 1, "hey", "ACROSS");
        
        words.add(firstWord);
        words.add(secondWord);
        words.add(thirdWord);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossMoreWordsNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(3, 1, "hint", 1, "dba", "DOWN");
        Word secondWord = new Word(1, 0, "hint", 1, "cat", "ACROSS");
        Word thirdWord = new Word(2, 0, "hint", 1, "bad", "ACROSS");
        
        words.add(firstWord);
        words.add(secondWord);
        words.add(thirdWord);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossMoreWordsBothOverlapInvalid() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(0, 10, "hint", 1, "dba", "DOWN");
        Word secondWord = new Word(0, 9, "hint", 1, "cat", "DOWN");
        Word thirdWord = new Word(1, 8, "hint", 1, "bad", "ACROSS");
        
        words.add(firstWord);
        words.add(secondWord);
        words.add(thirdWord);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    //      Number of overlaps: >1
    @Test
    public void testCheckConsistencyDownAcrossMoreWordsMultipleOverlapValid() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(0, 10, "hint", 1, "dba", "DOWN");
        Word secondWord = new Word(0, 9, "hint", 1, "cat", "DOWN");
        Word thirdWord = new Word(1, 8, "hint", 1, "bab", "ACROSS");
        
        words.add(firstWord);
        words.add(secondWord);
        words.add(thirdWord);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
    
}
