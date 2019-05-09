package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    /*
     * Test addPlayer()
     * 
     * Test getNumberPlayers()
     *  0 players, 1 player, 2 players
     * 
     * Test decreaseChallenge
     * 
     * Test incrementChallengeByTwo
     * 
     * Test incrementScore()
     * 
     * Test getScore()
     * 
     * Test getChallengePoints
     * 
     * Test tryInsert()
     *  valid insert, invalid insert
     * 
     * Test challenge()
     *  valid challenge, invalid challenge
     *  Original word incorrect, challenge correct
     *  original word correct, challenge incorrect
     *  original word incorrect, challenge incorrect
     *  
     * Test getMatchName()
     * 
     * Test getMatchDescription()
     * 
     * 
     * Test isFinished()
     *  finished, not finished
     * 
     * Test containsPlayer()
     *  contains player, does not contain
     * 
     * 
     * 
     * 
     */

    
    //covers size of words: 0
    @Test
    public void testCheckConsistencyEmpty() {
        List<WordTuple> words = new ArrayList<>();
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 1
    @Test
    public void testCheckConsistencyOneWord() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(1, 2, "hint", 1, "cat", "ACROSS");
        words.add(firstWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both across, different rows
    @Test
    public void testCheckConsistencyAcrossAcossDifferentRows() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(3, 2, "hint", 1, "cat", "ACROSS");
        WordTuple secondWordTuple = new WordTuple(1, 2, "hint", 1, "splat", "ACROSS");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both across, same rows, no overlap
    @Test
    public void testCheckConsistencyAcrossAcossSameRowsNoOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(1, 2, "hint", 1, "cat", "ACROSS");
        WordTuple secondWordTuple = new WordTuple(1, 200, "hint", 1, "splat", "ACROSS");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both across, same rows, yes overlap
    @Test
    public void testCheckConsistencyAcrossAcossSameRowsYesOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(1, 2, "hint", 1, "cat", "ACROSS");
        WordTuple secondWordTuple = new WordTuple(1, 1, "hint", 1, "splat", "ACROSS");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both down, different columns, no overlap
    @Test
    public void testCheckConsistencyDownDownDifferentColumns() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(1, 2, "hint", 1, "cat", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 1, "hint", 1, "splat", "DOWN");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both down, same columns, no overlap
    @Test
    public void testCheckConsistencyDownDownSameColumnsNoOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(100, 2, "hint", 1, "cat", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      both down, same columns, no overlap
    @Test
    public void testCheckConsistencyDownDownSameColumnsYesOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(5, 2, "hint", 1, "cat", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one across then one down, no overlap
    //      Number of overlaps: 0
    @Test
    public void testCheckConsistencyAcrossDownNoOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(100, 200, "hint", 1, "cat", "ACROSS");
        WordTuple secondWordTuple = new WordTuple(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one across one down, yes overlap
    //      Number of overlaps: 1
    @Test
    public void testCheckConsistencyAcrossDownYesOverlapDifferentLetter() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(1, 2, "hint", 1, "cat", "ACROSS");
        WordTuple secondWordTuple = new WordTuple(1, 2, "hint", 1, "splat", "DOWN");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one across one down, yes overlap, same letter
    @Test
    public void testCheckConsistencyAcrossDownYesOverlapSameLetter() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(1, 0, "hint", 1, "cat", "ACROSS");
        WordTuple secondWordTuple = new WordTuple(0, 1, "hint", 1, "daa", "DOWN");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: 2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossYesOverlapDifferentLetter() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 1, "hint", 1, "dba", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 0, "hint", 1, "cat", "ACROSS");
        
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossMoreWordTuplesYesOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 1, "hint", 1, "dba", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 0, "hint", 1, "cat", "ACROSS");
        WordTuple thirdWordTuple = new WordTuple(2, 0, "hint", 1, "hey", "ACROSS");
        
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        words.add(thirdWordTuple);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossMoreWordTuplesNoOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(3, 1, "hint", 1, "dba", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 0, "hint", 1, "cat", "ACROSS");
        WordTuple thirdWordTuple = new WordTuple(2, 0, "hint", 1, "bad", "ACROSS");
        
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        words.add(thirdWordTuple);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    @Test
    public void testCheckConsistencyDownAcrossMoreWordTuplesBothOverlapInvalid() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        WordTuple secondWordTuple = new WordTuple(0, 9, "hint", 1, "cat", "DOWN");
        WordTuple thirdWordTuple = new WordTuple(1, 8, "hint", 1, "bad", "ACROSS");
        
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        words.add(thirdWordTuple);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(!currentMatch.checkConsistency());
    }
    
    //covers size of words: >2
    //      one down then one across, yes overlap, same letter
    //      Number of overlaps: >1
    @Test
    public void testCheckConsistencyDownAcrossMoreWordTuplesMultipleOverlapValid() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        WordTuple secondWordTuple = new WordTuple(0, 9, "hint", 1, "cat", "DOWN");
        WordTuple thirdWordTuple = new WordTuple(1, 8, "hint", 1, "bab", "ACROSS");
        
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        words.add(thirdWordTuple);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        assertTrue(currentMatch.checkConsistency());
    }
    
    // covers toString non-empty string for name
    //      non-empty description
    //      number of entries: >1
    @Test
    public void testToStringSimple() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        WordTuple secondWordTuple = new WordTuple(0, 9, "hint", 1, "cat", "DOWN");
        WordTuple thirdWordTuple = new WordTuple(1, 8, "hint", 1, "bab", "ACROSS");
        
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        words.add(thirdWordTuple);
        
        
        
        Match currentMatch = new Match("Match name", "Match description", words);
        
        String expected = "3x11\n" + 
                "#########??\n" + 
                "########???\n" + 
                "#########??\n" + 
                "3\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n" + 
                "0 9 DOWN 1\n" + 
                "hint\n" + 
                "1 8 ACROSS 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    
    //covers addPlayer()
    @Test
    public void testAddPlayer() {
        
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        currentMatch.addPlayer(new Player("hi"));
        assertTrue(currentMatch.containsPlayer(new Player("hi")));
        
    }
    
    //covers getNumberPlayers
    //  0 players
    @Test
    public void testGetNumberPlayers0() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        assertEquals(0, currentMatch.getNumberPlayers());
    }
    
    //covers getNumberPlayers
    //  1 players
    @Test
    public void testGetNumberPlayers1() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        currentMatch.addPlayer(new Player("yo"));
        assertEquals(1, currentMatch.getNumberPlayers());
    }
    
    //covers getNumberPlayers
    //  2 players
    @Test
    public void testGetNumberPlayers2() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        currentMatch.addPlayer(new Player("yo"));
        currentMatch.addPlayer(new Player("a"));
        assertEquals(2, currentMatch.getNumberPlayers());
    }
    
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
    
}
