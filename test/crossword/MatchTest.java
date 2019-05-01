package crossword;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;


public class MatchTest {


    // Testing strategy for checkConsistency():
    //  Partition the input as follows:
    //
    //      Size of words: 0, 1, >1
    //      Type of directions: all across, all down, both across and down
    //          all across: different rows, same row
    //              same row: no overlap, yes overlap
    //          all down: different columns, same column
    //              same column: no overlap, yes overlap
    //          both across and down: no overlap, yes overlap
    //              Yes overlap with same letter, different letter
    //              Down word compared with vertical, across word compared with down
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    
    //covers size of words: 0
    @Test
    public void testCheckConsistencyEmpty() {
        List<Word> words = new ArrayList<>();
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 1
    @Test
    public void testCheckConsistencyOneWord() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        words.add(firstWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      both across, different rows
    @Test
    public void testCheckConsistencyAcrossAcossDifferentRows() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(3, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      both across, same rows, no overlap
    @Test
    public void testCheckConsistencyAcrossAcossSameRowsNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 200, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      both across, same rows, yes overlap
    @Test
    public void testCheckConsistencyAcrossAcossSameRowsYesOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 1, "hint", 1, "splat", "ACROSS");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      both down, different columns, yes overlap
    @Test
    public void testCheckConsistencyDownDownDifferentColumns() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "DOWN");
        Word secondWord = new Word(1, 1, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      both down, same columns, no overlap
    @Test
    public void testCheckConsistencyDownDownSameColumnsNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(100, 2, "hint", 1, "cat", "DOWN");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    
    //covers size of words: >1
    //      one across one down, no overlap
    @Test
    public void testCheckConsistencyAcrossDownNoOverlap() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(100, 200, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      one across one down, yes overlap
    @Test
    public void testCheckConsistencyAcrossDownYesOverlapDifferentLetter() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 2, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >1
    //      one across one down, yes overlap, same letter
    @Test
    public void testCheckConsistencyAcrossDownYesOverlapSameLetter() {
        List<Word> words = new ArrayList<>();
        Word firstWord = new Word(1, 0, "hint", 1, "cat", "ACROSS");
        Word secondWord = new Word(0, 1, "hint", 1, "daa", "DOWN");
        words.add(firstWord);
        words.add(secondWord);
        
        Map<Integer, Word> map = new HashMap<>();
        Match currentMatch = new Match("Match name", "Match description", words, map);
        assertTrue(currentMatch.checkConsistency());
    }
    
    
    
    
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
    
}
