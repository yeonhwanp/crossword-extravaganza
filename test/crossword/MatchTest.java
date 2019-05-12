package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


public class MatchTest {


    /* Testing strategy for checkConsistency():
     *   Partition the input as follows:
     * 
     *      Size of words: 0, 1, 2, >2
     *      Type of directions: all across, all down, both across and down
     *          all across: different rows, same row
     *              same row: no overlap, yes overlap
     *          all down: different columns, same column
     *              same column: no overlap, yes overlap
     *          both across and down: no overlap, yes overlap
     *              Yes overlap with same letter, different letter
     *              Down word compared with across, across word compared with down
     *              Number of overlaps: 0, 1, >1
     *          
     *
     * Testing strategy for toString():
     *  Partition the input as follows:
     *      Type of match name: empty string, non-empty string
     *      Type of match description: empty string, non-empty string
     *      Number of entries: 0, 1, >1
     *  More partitions:
     *      Word has been inserted, word has been challenged, word has been taken off board
     *  These extra partitions are covered within tests for tryInsert() and challenge()
     *      
     *          
     *
     * Test addPlayer()
     *      this has had no players added yet, has had one player added so far
     * 
     * Test getNumberPlayers()
     *  0 players, 1 player, 2 players
     * 
     * Test decreaseChallenge()
     *  has already been decreased
     * 
     * Test incrementChallengeByTwo()
     *  has already been incremented
     *  has been decreased before
     * 
     * Test incrementScore()
     *  has already been incremented before
     * 
     * Test getScore()
     *  normal score only, challenge score only, both normal and challenge
     * 
     * Test getChallengePoints
     *  has been incremented by two, has been decreased
     * 
     * Test tryInsert()
     *  valid insert
     *      no inconsistencies with pre-entered words by others
     *          no overlap of words, overlap of words
     *      changes own, previously entered word
     *  invalid insert
     *      inconsistent with pre-entered words by others
     *          tries to enter same word that has already been entered
     *          overlaps with other word
     *      wrong word size
     *      word already confirmed
     *      ID insert doesn't exist
     * 
     * Test challenge()
     *  valid challenge, invalid challenge
     *  Valid Challenge:
     *      Original word incorrect, challenge correct
     *          deletes inconsistent words, deletes no words
     *      original word correct, challenge incorrect
     *      original word incorrect, challenge incorrect
     *          attached words (words that share a letter with original) are not erased, no attached words
     *  Invalid challenge:
     *      Challenger is trying to challenge own word
     *      Word to challenge is already confirmed
     *      Word to challenge is same as word you are proposing
     *      Proposed word is incorrect length
     *      ID inserted does not exist
     *      
     *  
     * Test getMatchName()
     * 
     * Test getMatchDescription()
     *  no comments, comments
     * 
     * 
     * Test isFinished()
     *  finished
     *      was finished through a try, finished through a challenge
     *  not finished
     *      no moves made yet (this is untouched)
     *      this not untouched
     * 
     * Test containsPlayer()
     *  contains player, does not contain
     * 
     * 
     * Methods oneDimensionOverlap and verticalHorizontalConsistent are fully covered within checkConsistency() partition
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
    
    // covers toString empty string for name
    //      empty description
    //      number of entries: 1
    @Test
    public void testToStringEmptyStringEmptyDescription() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        
        words.add(firstWordTuple);

        Match currentMatch = new Match("", "", words);
        
        String expected = "3x11\n" + 
                "##########?\n" + 
                "##########?\n" + 
                "##########?\n" + 
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    // covers toString empty string for name
    //      empty description
    //      number of entries: 0
    @Test
    public void testToStringEmptyStringEmptyDescriptionNoEntry() {
        List<WordTuple> words = new ArrayList<>();

        Match currentMatch = new Match("", "", words);
        
        String expected = "0x0\n" +
                "0\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    
    
    //covers addPlayer()
    //      no player has been added before
    @Test
    public void testAddPlayer() {
        
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        currentMatch.addPlayer(new Player("hi"));
        assertTrue(currentMatch.containsPlayer(new Player("hi")));
        
    }
    
    //covers addPlayer()
    //      one player has been added before
    @Test
    public void testAddPlayerOne() {
        
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        currentMatch.addPlayer(new Player("before"));
        currentMatch.addPlayer(new Player("hi"));
        assertTrue(currentMatch.containsPlayer(new Player("hi")));
        assertTrue(currentMatch.containsPlayer(new Player("before")));
        
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
    
    //covers decreaseChallenge(), getChallengePoints()
    @Test
    public void testDecreaseChallenge() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.decreaseChallenge(yo);
        assertEquals(-1, currentMatch.getChallengePoints(yo));
    }
    
    //covers decreaseChallenge(), getChallengePoints()
    //      has already been decreased
    @Test
    public void testDecreaseChallengeAgain() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.decreaseChallenge(yo);
        currentMatch.decreaseChallenge(yo);
        assertEquals(-2, currentMatch.getChallengePoints(yo));
    }
    
    //covers incrementChallengeByTwo(), getChallengePoints()
    @Test
    public void testIncrementChallengeByTwo() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.incrementChallengeByTwo(yo);
        assertEquals(2, currentMatch.getChallengePoints(yo));
    }
    
    //covers incrementChallengeByTwo(), getChallengePoints()
    //      has already been incremented
    @Test
    public void testIncrementChallengeByTwoAgain() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.incrementChallengeByTwo(yo);
        currentMatch.incrementChallengeByTwo(yo);
        assertEquals(4, currentMatch.getChallengePoints(yo));
    }
    
    //covers incrementChallengeByTwo() and decreaseChallenge, getChallengePoints()
    @Test
    public void testIncrementChallengeAndDecreaseChallenge() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.decreaseChallenge(yo);
        currentMatch.incrementChallengeByTwo(yo);
        assertEquals(1, currentMatch.getChallengePoints(yo));
    }
    
    //covers incrementScore(), getScore()
    //      normal score only
    @Test
    public void testIncrementScore() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.incrementScore(yo);
        assertEquals(1, currentMatch.getScore(yo));
    }
    
    //covers getScore()
    //      challengeScore only
    @Test
    public void testIncrementChallengeGetScore() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.incrementChallengeByTwo(yo);
        assertEquals(2, currentMatch.getScore(yo));
    }
    
    //covers getScore()
    //      normal score and challengeScore
    @Test
    public void testGetScoreBoth() {
        List<WordTuple> words = new ArrayList<>();
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        currentMatch.incrementChallengeByTwo(yo);
        currentMatch.incrementScore(yo);
        currentMatch.incrementScore(yo);
        assertEquals(4, currentMatch.getScore(yo));
    }
    
    ///////////////////////////////////////////////INSERT TESTS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////INSERT TESTS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////INSERT TESTS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////INSERT TESTS///////////////////////////////////////////////////////
    
    
    //covers tryInsert()
    //      valid insert, no inconsistencies with pre-entered words (no overlap)
    @Test
    public void testTryInsertValidNoOverlap() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        words.add(firstWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);

        assertTrue(currentMatch.tryInsert(yo, 1, "aba"));
        
        String expected = "3x11\n" + 
                "##########a\n" + 
                "##########b\n" +
                "##########a\n" +
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        
        assertEquals(expected, currentMatch.toString());
        
    }
    
    //covers tryInsert()
    //      valid insert, no inconsistencies with pre-entered words (yes overlap)
    @Test
    public void testTryInsertValidOverlap() {
        
        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);

        currentMatch.tryInsert(yo, 1, "aba");
        assertTrue(currentMatch.tryInsert(dude, 2, "obo"));
        
        String expected = "3x3\n" + 
                "#a#\n" + 
                "obo\n" + 
                "#a#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    //covers tryInsert()
    //      valid insert, replaces own word
    @Test
    public void testTryInsertValidReplacesOwn() {
        
        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);

        currentMatch.tryInsert(yo, 1, "now");
        assertTrue(currentMatch.tryInsert(yo, 1, "aba"));
        
        String expected = "3x3\n" + 
                "#a#\n" + 
                "?b?\n" + 
                "#a#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
        
    }
    
    //covers tryInsert()
    //      invalid insert, inconsistent with other words already entered by others (same word ID)
    @Test
    public void testTryInsertInvalidOthers() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        words.add(firstWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);

        currentMatch.tryInsert(yo, 1, "now");
        assertTrue(!currentMatch.tryInsert(dude, 1, "aba"));
        
        String expected = "3x11\n" + 
                "##########n\n" + 
                "##########o\n" +
                "##########w\n" +
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
        
    }
    
    //covers tryInsert()
    //      invalid insert, already confirmed
    @Test
    public void testTryInsertInvalidConfirmed() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "dba", "DOWN");
        words.add(firstWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);

        currentMatch.tryInsert(yo, 1, "now");
        currentMatch.challenge(dude, 1, "dba");
        
        assertTrue(!currentMatch.tryInsert(dude, 1, "hey"));
        
        String expected = "3x11\n" + 
                "##########d\n" + 
                "##########b\n" +
                "##########a\n" +
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
        
    }
    
    //covers tryInsert()
    //      invalid insert, inconsistent with other words already entered by others (overlaps at letter)
    @Test
    public void testTryInsertInvalidOthersOverlap() {
        
        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);

        currentMatch.tryInsert(yo, 1, "now");
        assertTrue(!currentMatch.tryInsert(dude, 2, "aba"));
        
        String expected = "3x3\n" + 
                "#n#\n" + 
                "?o?\n" + 
                "#w#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    //covers tryInsert()
    //      invalid insert, incorrect length of word
    @Test
    public void testTryInsertInvalidLength() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "cat", "DOWN");
        words.add(firstWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        
        assertTrue(!currentMatch.tryInsert(yo, 1, "a"));
        
        String expected = "3x11\n" + 
                "##########?\n" + 
                "##########?\n" +
                "##########?\n" +
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    //covers tryInsert()
    //      invalid insert, non-existent ID
    @Test
    public void testTryInsertInvalidID() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "cat", "DOWN");
        words.add(firstWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        
        assertTrue(!currentMatch.tryInsert(yo, 3, "a"));
        
        String expected = "3x11\n" + 
                "##########?\n" + 
                "##########?\n" +
                "##########?\n" +
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        
    }
    
    
    ///////////////////////////////////////////////CHALLENGE TESTS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////CHALLENGE TESTS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////CHALLENGE TESTS///////////////////////////////////////////////////////
    
    //covers challenge() valid challenge original incorrect, challenge correct
    //      deletes inconsistent words
    @Test
    public void testChallengeValidOriginalIncorrectYesDeletions() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "bro");
        currentMatch.tryInsert(yo, 1, "cry");
        
        
        assertTrue(currentMatch.challenge(dude, 2, "mab"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "mab\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(2, currentMatch.getChallengePoints(dude));
 
    }
    
    
    //covers challenge() valid challenge original incorrect, challenge correct
    //      deletes no words
    @Test
    public void testChallengeValidOriginalIncorrectNoDeletions() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "dad");
        
        assertTrue(currentMatch.challenge(dude, 2, "mab"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "mab\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(2, currentMatch.getChallengePoints(dude));
 
    }
    
    //covers challenge() valid challenge 
    //      original correct, challenge incorrect
    @Test
    public void testChallengeValidOriginalCorrectChallengeIncorrect() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "mab");
        
        assertTrue(currentMatch.challenge(dude, 2, "bro"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "mab\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(-1, currentMatch.getChallengePoints(dude));
        assertEquals(1, currentMatch.getScore(yo));
 
    }
    
    //covers challenge() valid challenge 
    //      original incorrect, challenge incorrect, no attached words
    @Test
    public void testChallengeValidOriginalIncorrectChallengeIncorrectNoAttached() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "hia");
        
        assertTrue(currentMatch.challenge(dude, 2, "bro"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "???\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(-1, currentMatch.getChallengePoints(dude));
        assertEquals(0, currentMatch.getScore(yo));
 
    }
    
    //covers challenge() valid challenge 
    //      original incorrect, challenge incorrect, attached words are not erased
    @Test
    public void testChallengeValidOriginalIncorrectChallengeIncorrectYesAttached() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 1, "aia");
        currentMatch.tryInsert(yo, 2, "hia");
        
        assertTrue(currentMatch.challenge(dude, 1, "bro"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "hia\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(-1, currentMatch.getChallengePoints(dude));
        assertEquals(0, currentMatch.getScore(yo));
 
    }
    
    //covers challenge() invalid challenge 
    //      Challenger trying to challenge own word
    @Test
    public void testChallengeInvalidOwnWord() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "aia");
        
        assertTrue(!currentMatch.challenge(yo, 2, "bro"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "aia\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(0, currentMatch.getChallengePoints(dude));
 
    }
    
    
    //covers challenge() invalid challenge 
    //      Word to challenge is already confirmed
    @Test
    public void testChallengeInvalidConfirmedWord() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "mab");
        currentMatch.challenge(dude, 2, "bro");
        
        assertTrue(!currentMatch.challenge(dude, 2, "iii"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "mab\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(-1, currentMatch.getChallengePoints(dude));
 
    }
    
    
    
    //covers challenge() invalid challenge 
    //      Word to challenge is same as proposed word
    @Test
    public void testChallengeInvalidSameWord() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "mab");
        
        assertTrue(!currentMatch.challenge(dude, 2, "mab"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "mab\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(0, currentMatch.getChallengePoints(dude));
 
    }
    
    //covers challenge() invalid challenge 
    //      Proposed word is incorrect length
    @Test
    public void testChallengeInvalidIncorrectLength() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "mab");
        
        assertTrue(!currentMatch.challenge(dude, 2, "a"));
        
        String expected = "3x3\n" + 
                "#?#\n" + 
                "mab\n" + 
                "#?#\n" + 
                "2\n" + 
                "0 1 DOWN 1\n" + 
                "hint\n" + 
                "1 0 ACROSS 2\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
        assertEquals(0, currentMatch.getChallengePoints(dude));
 
    }
    
    //covers challenge() invalid challenge 
    //      non-existent ID
    @Test
    public void testChallengeInvalidID() {

        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 10, "hint", 1, "cat", "DOWN");
        words.add(firstWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        
        assertTrue(!currentMatch.challenge(yo, 3, "a"));
        
        String expected = "3x11\n" + 
                "##########?\n" + 
                "##########?\n" +
                "##########?\n" +
                "1\n" + 
                "0 10 DOWN 1\n" + 
                "hint\n";
        assertEquals(expected, currentMatch.toString());
 
    }
    
    
    
    //////////////////////////////////////////////GET MATCH NAME TESTS////////////////////////////////////////////
    
    //covers getMatchName
    @Test
    public void testGetMatchName() {

        Match currentMatch = makeTwoWordMatch();
        assertEquals("Match name", currentMatch.getMatchName());
        
    }

    //////////////////////////////////////////////GET DESCRIPTION NAME TESTS////////////////////////////////////////////
    
    //covers getMatchDescription
    //      no comments
    @Test
    public void testGetMatchDescriptionNoComments() {

        Match currentMatch = makeTwoWordMatch();
        assertEquals("Match description", currentMatch.getMatchDescription());
        
    }
    
    //covers getMatchDescription
    //      yes comments
    @Test
    public void testGetMatchDescriptionYesComments() {

        Match currentMatch = new Match("Match name", "Match description \\\n hi", new ArrayList<>());
        assertEquals("Match description \\\n hi", currentMatch.getMatchDescription());
        
    }
    
    //covers isFinished()
    //  is actually finished, through try
    @Test
    public void testIsFinishedYesTry() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "mab");
        currentMatch.tryInsert(yo, 1, "cat");
        
        assertTrue(currentMatch.isFinished());
        
    }
    
    //covers isFinished()
    //  is actually finished, through challenge
    //  also tests isConfirmed() - true (see WordTest.java for reference)
    @Test
    public void testIsFinishedYesChallenge() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 2, "mab");
        currentMatch.tryInsert(yo, 1, "dab");
        
        currentMatch.challenge(dude, 1, "cat");
        
        assertTrue(currentMatch.isFinished());
        
    }
    
    //covers isFinished()
    //  is not finished, no moves made yet (this is untouched)
    @Test
    public void testIsFinishedNoUntouched() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        assertTrue(!currentMatch.isFinished());
        
    }
    
    //covers isFinished()
    //  is not finished, this not untouched
    @Test
    public void testIsFinishedNoTouched() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        currentMatch.tryInsert(yo, 1, "aaa");
        
        assertTrue(!currentMatch.isFinished());
        
    }
    
    
    //covers containsPlayer()
    //      contains player
    @Test
    public void testContainsPlayerYes() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        currentMatch.addPlayer(yo);
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        assertTrue(currentMatch.containsPlayer(yo));
    }
    
    
    //covers containsPlayer()
    //      contains player
    @Test
    public void testContainsPlayerNo() {

        Match currentMatch = makeTwoWordMatch();
        Player yo = new Player("yo");
        Player dude = new Player("dude");
        currentMatch.addPlayer(dude);
        
        assertTrue(!currentMatch.containsPlayer(yo));
    }
    
    
    /**
     * Helper method to make a new match with two words, cat and map, that  overlap at letter 'a'.
     * @return match stated above
     */
    private static Match makeTwoWordMatch() {
        List<WordTuple> words = new ArrayList<>();
        WordTuple firstWordTuple = new WordTuple(0, 1, "hint", 1, "cat", "DOWN");
        WordTuple secondWordTuple = new WordTuple(1, 0, "hint", 2, "mab", "ACROSS");
        words.add(firstWordTuple);
        words.add(secondWordTuple);
        
        Match currentMatch = new Match("Match name", "Match description", words);
        return currentMatch;
    }
    
    
    
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
    
}
