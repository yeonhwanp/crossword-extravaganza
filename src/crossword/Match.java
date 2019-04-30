package crossword;

import java.util.List;
import java.util.Map;

public class Match {
    
    // Rep: Map<String ID, Player player>
    //      Map<Player player, int score>
    //      Map<Player player, int challengePts>
    //      Cell[][] board
    //      Map<String ID, Word word> words
    //      State state
    
    // Methods: decreaseChallenge()
    //          increaseChallenge()
    //          incrementScore()
    //          getState()
    //          checkValidInsert()
    //          insertWord()
    //          checkValidChallenge()
    //          challenge()
    //          toString() [Need this in this implementation to send over the server]
    
    // Handlers: tryWord()
    //           challengeWord()
    //           exit()
    //           watch()
    // These handlers should all return the updated board after completion
    
    private final Map<String, Word> words; // Replaced List with Map to account for word IDs
    private final Cell[][] gameBoard;
    private Map<String, Player> players;
    private Map<Player, Integer> scores;
    private Map<Player, Integer> challengePts;
    private GameState state;
    
    /**
     * Used to start the game once two players join.
     */
    public void startGame() {
        
    }
    
    /**
     * Decreases a player's challenge points
     * @param player the player to decrease challenge points for
     */
    private void decreaseChallenge(Player player) {
    }
    
    /**
     * Increments a player's challenge points
     * @param player the player to increase challenge points for
     */
    private void incrementChallenge(Player player) {
        
    }
    
    /**
     * Increase a player's score count
     * @param player the player to increase score for
     */
    public void incrementScore(Player player) {
        
    }
    
    /**
     * @param player the player we want to get the score for
     * @return the score of the given player
     */
    public int getScore(Player player) {
        
    }
    
    /**
     * @return the current state of the game
     */
    public GameState getState() {
        
    }
    
    /**
     * Checks if an attempted word is valid according to the final project handout
     * @param player the player attempting the word
     * @param wordID the id of the word being attempted
     * @param wordString the actual word inserted by the player
     * @return if the insert is valid or not
     */
    public boolean CheckValidInsert(Player player, String wordID, String wordString) {
        
    }
    
    /**
     * Inserts an attempt at a given location.
     * @param player the player attempting the insert
     * @param wordID the word being attempted
     * @param wordString the guessed word
     */
    public void insertWord(Player player, String wordID, String wordString) {
        
    }
    
    /**
     * Checks if an attempted challenge is valid according to the final project handout
     * @param player the player attempting the word
     * @param wordID the id of the word being attempted
     * @param wordString the actual word used to challenge the other player's guess
     * @return if the challenge is valid or not
     */
    public boolean checkValidChallenge(Player player, String wordID, String wordString) {
        
    }
    
    /**
     * Challenges the other player's guess and increments/decrements challengepoints accordingly.
     * @param player the player doing the challenging
     * @param wordID the id of the word being challenged
     * @param wordString the actual word used to challenge the other player's guesses
     */
    public void challenge(Player player, String wordID, String wordString) {
        
    }
    
    @Override
    public String toString() {
        
    }
    
    

    
    public boolean checkConsistency() {
        /*
         * IMPLEMENTATION IDEA
         * 
         * Go through every pair of words on the board and check:
         *    1. If the words have the same orientation (both DOWN or both ACROSS), then they cannot intersect anywhere
         *          a. Since they have the same orientation, if they DO intersect, they must have the same row or same column
         *          b. The problem reduces to finding whether or not two 1D ranges overlap (well known trick for doing this), or can just use brute force
         *    2. If the words have different orientations, they can intersect at most one point
         *          a. Brute force can be used, or we can find the intersections of the lines, and check if the intersection point lies in both segments
         *    3. Check to make sure that the words are different (since all words must be unique)
         */
        
        for(int i = 0; i < words.size(); i++) {
            
            final Word firstWord = words.get(i);
            final String firstValue = firstWord.getCorrectValue();
            final boolean firstVertical = firstWord.isVertical();

            for(int j = i+1; j < words.size(); j++) {
                
                final Word secondWord = words.get(j);
                final String secondValue = secondWord.getCorrectValue();
                final boolean secondVertical = secondWord.isVertical();
                
                if(firstValue.equals(secondValue)) { // #3: every word must be unique
                    return false;
                }
                
                if(firstVertical && secondVertical) { // #1: the words are the same orientation (VERTICAL)
                    // words with the same orientation CANNOT intersect, we check their bounding rectangles
                    final int firstCol = firstWord.getColumnLowerBound();
                    final int secondCol = secondWord.getColumnLowerBound();
                    
                    if(firstCol != secondCol) {
                        continue; // if the columns don't match, there's no way the pair overlaps
                    }
                    
                    final int firstLowerRow = firstWord.getRowLowerBound();
                    final int firstHigherRow = firstWord.getRowUpperBound();
                    
                    final int secondLowerRow = secondWord.getRowLowerBound();
                    final int secondHigherRow = secondWord.getRowUpperBound();
                    
                    if(oneDimensionOverlap(firstLowerRow, firstHigherRow, secondLowerRow, secondHigherRow)) {
                        return false;
                    }
                }
                else if(!firstVertical && !secondVertical) { // #1: the words are the same orientation (HORIZONTAL)
                    final int firstRow = firstWord.getRowLowerBound();
                    final int secondRow = secondWord.getRowLowerBound();
                    
                    if(firstRow != secondRow) {
                        continue; // if the rows don't match, then this pair definitely doesn't overlap
                    }
                    
                    final int firstLowerCol = firstWord.getColumnLowerBound();
                    final int firstHigherCol = firstWord.getColumnUpperBound();
                    
                    final int secondLowerCol = secondWord.getColumnLowerBound();
                    final int secondHigherCol = secondWord.getColumnUpperBound();
                    
                    if(oneDimensionOverlap(firstLowerCol, firstHigherCol, secondLowerCol, secondHigherCol)) {
                        return false;
                    }
                }
                else { // #2: check the point of intersection for words with different orientations
                    
                    throw new RuntimeException("not done implementing!");
                }
            }
            
        }
        
        return true;
    }
    
    private static boolean oneDimensionOverlap(int firstLow, int firstHigh, int secondLow, int secondHigh) {
        return firstLow <= secondHigh && secondLow <= firstHigh; // returns true iff [firstLow, firstHigh] and [secondLow, secondHigh] overlap
    }
    
    
}
