package crossword;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crossword.Cell.Exist;


/*
 * NOTES/TODO:
 *  - fix the rep exposure
 *  - fix the toString() 
 *  - implement checking for whether or not match is over
 *  - check whether or not a guess is consistent
 *  - implement challenge rules
 *  - 
 */


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
    
    // Abstraction function:
    //    AF(matchName, matchDescription, words, gameBoard, players, scores, challengePts, state, rows, columns) = 
    //      A [rows x columns] crossword match with the name & description matchName/matchDescription respectively and a board with
    //      contents specified by gameBoard and words -- players, scores, challengePts represent the players and their respective
    //      points that they've accumulated throughout the game. Finally, the state represents the state of the match
    //      as specified in the final project handout.
    // Representation invariant:
    //    matchName cannot contain newlines, or tabs
    //    rows >= 0 && col >= 0
    //    ids in idToWordMap are >= 1, unique, and increasingly sequential.
    //
    // Safety from rep exposure:
    //    matchName, matchDescription, words, gameBoard, rows, columns are private and final
    //    players, scores, challengePts, state are final
    //    Match constructor takes in immutable types and 
    //    Other public methods only take in and return immutable types
    //   
    // Thread safety argument:
    //   TODO: Later
    
    
    private final String matchName;
    private final String matchDescription;
    private final List<Word> words;
    private final Map<Integer, Word> idToWordMap; 
    private final Cell[][] gameBoard;
    private final int rows;
    private final int columns;
//    private Map<String, Player> players;
    private final List<Player> players;
    private final Map<Player, Integer> scores;
    private final Map<Player, Integer> challengePts;
//    private GameState state;
    
    /**
     * Constructor for the Match object
     * @param matchName the name of the match
     * @param matchDescription the description of the match
     * @param wordTuples the words associated with this match
     */
    public Match(String matchName, String matchDescription, List<WordTuple> wordTuples) {
        this.matchName = matchName;
        this.matchDescription = matchDescription;
        this.words = new ArrayList<>();
        this.idToWordMap = new HashMap<Integer, Word>();

        for (WordTuple wordTuple : wordTuples) {
            Word newWord = new Word(wordTuple.getRow(), wordTuple.getCol(), wordTuple.getHint(), wordTuple.getID(),
                    wordTuple.getWord(), wordTuple.getDirection());
            this.words.add(newWord);
            this.idToWordMap.put(newWord.getID(), newWord);
        }
                
        int maxRow = 0;
        int maxColumn = 0;
        
        for(Word word : words) {
            maxRow = Math.max(maxRow, word.getRowUpperBound()+1);
            maxColumn = Math.max(maxColumn, word.getColumnUpperBound()+1);
        }
        
        this.rows = maxRow;
        this.columns = maxColumn;
        
        this.gameBoard = new Cell[rows][columns];
        
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                this.gameBoard[i][j] = new Cell(i, j, Exist.ABSENT);
            }
        }
        
        for(Word word : words) {
            final int rowLower = word.getRowLowerBound();
            final int rowHigher = word.getRowUpperBound();
            final int colLower = word.getColumnLowerBound();
            final int colHigher = word.getColumnUpperBound();
            
            // CAUTION CAUTION CAUTION: DO *NOT* CHANGE THIS
            for(int i = rowLower; i <= rowHigher; i++) { // NOTE: this order of iteration is CRUCIAL to maintaining the rep invariant, CANNOT CHANGE THIS
                for(int j = colLower; j <= colHigher; j++) {
                    if(this.gameBoard[i][j].isAbsent()) {
                        this.gameBoard[i][j] = new Cell(i, j, Exist.PRESENT); // be careful, we don't want to override any cells that already exist
                    }
                    word.addInvolvedCell(this.gameBoard[i][j]);
                    this.gameBoard[i][j].addWord(word);
                }
            }
        }
        
        players = new ArrayList<>();
        scores = new HashMap<>();
        challengePts = new HashMap<>();
        
        checkRep();
    }
    
    public void addPlayer(Player player) {
        players.add(player);
        scores.put(player, 0);
        challengePts.put(player, 0);
    }
    
    /**
     * Check for valid match rep
     */
    private void checkRep() {
//        assert matchName.matches("\" [^\"\r\n\t\\]* \"");
        assert rows >= 0;
        assert columns >= 0;
        
        int lower = 0;
        for (Integer i : idToWordMap.keySet()) {
            assert i > lower;
            lower = i;
        }
    }
    
    
//    /**
//     * Used to start the game once two players join.
//     */
//    public void startGame() {
//        throw new RuntimeException("not done implementing!");
//    }
//    
    
    /**
     * Decreases a player's challenge points
     * @param player the player to decrease challenge points for
     */
    private void decreaseChallenge(Player player) {
        final int currentChallenge = challengePts.get(player);
        challengePts.put(player, currentChallenge-1);
    }
    
    /**
     * Increments a player's challenge points
     * @param player the player to increase challenge points for
     */
    private void incrementChallenge(Player player) {
        final int currentChallenge = challengePts.get(player);
        challengePts.put(player, currentChallenge+1);
    }
    
    /**
     * Increase a player's score count
     * @param player the player to increase score for
     */
    public void incrementScore(Player player) {
        final int currentScore = scores.get(player);
        scores.put(player, currentScore+1);
    }
    
    /**
     * @param player the player we want to get the score for
     * @return the score of the given player
     */
    public int getScore(Player player) {
        return scores.get(player);
    }
    
    /**
     * @param player the player we want to get the challenge points for
     * @return the number of challenge points of the given player
     */
    public int getChallengePoints(Player player) {
        return challengePts.get(player);
    }
    
//    /**
//     * @return the current state of the game
//     */
//    public GameState getState() {
//        throw new RuntimeException("not done implementing!");
//    }
    
    /**
     * Checks if an attempted word is consistent according to the final project handout
     * @param player the player attempting the word
     * @param word the word being attempted to insert
     * @param tryWord the actual word inserted by the player
     * @return if the insert is valid or not
     */
    private boolean checkConsistentInsert(Player player, Word insertWord, String tryWord) {
        if(insertWord.isConfirmed() || (insertWord.hasOwner() && !player.equals(insertWord.getOwner()))) {
            return false;
        }
        
        if(insertWord.getLength() != tryWord.length()) {
            return false;
        }
        
        
    }
    
    /**
     * Inserts an attempt at a given location.
     * @param player the player attempting the insert
     * @param wordID the word being attempted
     * @param tryWord the guessed word
     */
    public boolean tryInsert(Player player, int wordID, String tryWord) {
        throw new RuntimeException("not done implementing!");
        
        // TODO: check consistency of the guess first
    }
    
    /**
     * Checks if an attempted challenge is valid according to the final project handout
     * @param player the player attempting the word
     * @param wordID the id of the word being attempted
     * @param challengeGuess the actual word used to challenge the other player's guess
     * @return if the challenge is valid or not
     */
    private boolean checkValidChallenge(Player player, int wordID, String challengeGuess) {
        throw new RuntimeException("not done implementing!");
    }
    
    /**
     * Challenges the other player's guess and increments/decrements challengepoints accordingly.
     * @param player the player doing the challenging
     * @param wordID the id of the word being challenged
     * @param challengeGuess the actual word used to challenge the other player's guesses
     */
    public boolean challenge(Player player, int wordID, String challengeGuess) {
        throw new RuntimeException("not done implementing!");
    }
    
    @Override
    public String toString() {
        String resultString = "";
        resultString += rows + "x" + columns + "\n";
        
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                resultString += gameBoard[i][j].toString();
            }
            resultString += "\n";
        }
        
        resultString += words.size() + "\n";
        
        for(Word word : words) {
            resultString += word.getRowLowerBound() + " " + word.getColumnLowerBound() + " " + word.getDirection().name() + " " + word.getID() + "\n";
            resultString += word.getHint() + "\n";
        }
        
        return resultString;
    }
    
    /**
     * Checks if this board is consistent with regards to the specifications laid out in the Final Project handout
     * @return true if the board is consistent with regards to the Final Project handout
     */
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
        
        for(int i = 0; i < words.size(); i++) { // iterating on the first word
            
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
                    
                    if (firstVertical) { // first word is vertical, so second is horizontal
                        final boolean consistent = verticalHorizontalConsistent(firstWord, secondWord);
                        if(!consistent) {
                            return false;
                        }
                    } else { // the second word is vertical, first word is horizontal
                        final boolean consistent = verticalHorizontalConsistent(secondWord, firstWord);
                        if(!consistent) {
                            return false;
                        }
                    }
                    
                }
            }
            
        }
        
        return true;
    }
    
    /**
     * Check if two one dimensional ranges, [firstLow, firstHigh] and [secondLow, secondHigh] intersect.
     * @param firstLow the beginning of the first range
     * @param firstHigh the end of the first range
     * @param secondLow the beginning of the second range
     * @param secondHigh the end of the second range
     * @return true iff the ranges overlap
     */
    private static boolean oneDimensionOverlap(int firstLow, int firstHigh, int secondLow, int secondHigh) {
        return firstLow <= secondHigh && secondLow <= firstHigh; // returns true iff [firstLow, firstHigh] and [secondLow, secondHigh] overlap
    }
    
    /**
     * Check if two words (the first must be vertical and second must be horizontal) are consistent with each other.
     * @param verticalWord the vertically aligned word
     * @param horizontalWord the horizontally aligned word
     * @return whether or not the two words (one vertical and one horizontal) are consistent with each other
     */
    private static boolean verticalHorizontalConsistent(Word verticalWord, Word horizontalWord) {
        assert verticalWord.isVertical();
        assert horizontalWord.isHorizontal();
        
        final int verticalLowerRow = verticalWord.getRowLowerBound(); // this refers to the vertical line's lower index boundary
        final int verticalHigherRow = verticalWord.getRowUpperBound();
        final int potentialCol = verticalWord.getColumnLowerBound(); // column of the potential intersection
        
        final int horizontalLowerCol = horizontalWord.getColumnLowerBound(); // horizontal line's left boundary
        final int horizontalHigherCol = horizontalWord.getColumnUpperBound();
        final int potentialRow = horizontalWord.getRowLowerBound(); // row of the potential intersection
        
        if (potentialCol <= horizontalHigherCol // lies in between the boundaries of the two words
                && potentialCol >= horizontalLowerCol 
                && potentialRow <= verticalHigherRow 
                && potentialRow >= verticalLowerRow) { // we have an intersection
            
            final int verticalWordIndex = potentialRow - verticalLowerRow;
            final int horizontalWordIndex = potentialCol - horizontalLowerCol;
            
            final char verticalWordChar = verticalWord.getCorrectCharAt(verticalWordIndex);
            final char horizontalWordChar = horizontalWord.getCorrectCharAt(horizontalWordIndex);
            
            if (verticalWordChar != horizontalWordChar) {
                return false;
            }
        }
        
        return true;
    }
    
    public String getMatchName() {
        return matchName;
    }
    
    public String getMatchDescription() {
        return matchDescription;
    }
    
    public void clearInconsistent(Word word, String newVal) {
        throw new RuntimeException("not done implementing!");
    }
    
//    public Cell getCell(int )
    
}
