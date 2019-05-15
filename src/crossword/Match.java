package crossword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import crossword.Cell.Exist;
import crossword.Word.ChallengeResult;

/**
 * Ongoing match of Crossword Extravaganza to be played by two players.
 *
 */
public class Match {
    
    // Abstraction function:
    //    AF(matchName, matchDescription, words, idToWordMap, gameBoard, rows, columns, players, scores, challengePts, gameStarted) = 
    //      A [rows x columns] crossword match with the name & description matchName/matchDescription respectively and a board with
    //      contents specified by gameBoard and words -- players, scores, challengePts represent the players and their respective
    //      points that they've accumulated throughout the game, so we have that:
    //      (scores.get(i) is the number of correct words entered by player i, challengePts is # challenge points the player i has).
    //      We also have that the game is started if and only if gameStarted is true.
    
    // Rep invariant: 
    //    matchName cannot contain newlines, or tabs
    //    rows >= 0 && col >= 0
    //    ids in idToWordMap are >= 1, unique, and increasingly sequential.
    //    every word in the list words appears as a value in idToWordMap and vice versa
    //    players.size() == 2 (there are exactly two players)
    //    scores.keySet().size() == 2
    //    challengePts.keySet().size() == 2
    //    same players in players, scores, and challengePts
    //    if gameStarted is true, must have two players, otherwise must have at most 1 player
    //    
    //
    // Safety from rep exposure:
    //    matchName, matchDescription, words, gameBoard, rows, columns are private and final
    //    players, scores, challengePts, state are final
    //    Match constructor takes in immutable types and 
    //    Other public methods only take in and return immutable types
    //   
    // Thread safety argument:
    //   We use the monitor pattern and synchronize every method with a lock on this object, which ensures thread safety 
    //   because only one thread can be in a given method at any given time. 
    
    
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
    private boolean gameStarted;
    
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
        
        int counter = 1;

        for (WordTuple wordTuple : wordTuples) {
            Word newWord = new Word(wordTuple.getRow(), wordTuple.getCol(), wordTuple.getHint(), counter,
                    wordTuple.getWord(), wordTuple.getDirection());

            
            this.words.add(newWord);
           
            counter++;
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
            
            this.idToWordMap.put(word.getID(), word);
        }
        
        players = new ArrayList<>();
        scores = new HashMap<>();
        challengePts = new HashMap<>();
        
        checkRep();
    }
    
    /**
     * Add a player to this match
     * @param player player to add
     */
    public synchronized void addPlayer(Player player) {
        players.add(player);
        scores.put(player, 0);
        challengePts.put(player, 0);
        
        if(players.size() == 2) {
            this.startGame();
        }
        
        this.notifyAll();
        checkRep();
    }
    
    /**
     * Find the number of players currently playing this match.
     * @return number of current players.
     */
    public synchronized int getNumberPlayers() {
        this.notifyAll();
        checkRep();
        
        return players.size();
    }
    
    /**
     * Start the game.
     */
    private synchronized void startGame() {
        this.gameStarted = true;
        
        this.notifyAll();
        checkRep();
    }
    
    private synchronized boolean gameIsStarted() {
        this.notifyAll();
        checkRep();
        
        return this.gameStarted;
    }
    
    /**
     * Check for valid match rep invariant
     */
    private synchronized void checkRep() {
//        assert matchName.matches("\"[^\"\r\n\t\\]*\"");
//        assert matchDescription.matches("\"([^\"\\r\\n\\\\] | '\\\\' [\\\\nrt] )*\"");
        assert rows >= 0;
        assert columns >= 0;
        
        for(int i = 1; i <= idToWordMap.size(); i++) {
            assert idToWordMap.containsKey(i);
        }
        
        assert checkSetEquality(new HashSet<>(words), new HashSet<>(idToWordMap.values()));
        
        if(this.gameStarted) {
            assert this.players.size() == 2;
        }
        else {
            assert this.players.size() <= 1;
        }
        
        assert checkSetEquality(scores.keySet(), new HashSet<>(players));
        assert checkSetEquality(scores.keySet(), challengePts.keySet());
    }
    
    /**
     * Check if two sets have the exact same elements
     * @param first the first set
     * @param second the second set
     * @return true iff the two sets contain the exact same elements, as determined by the .equals() method of the type E
     */
    private synchronized <E> boolean checkSetEquality(Set<E> first, Set<E> second) {
        if(first.size() != second.size()) return false;
        
        for(E e : first) {
            if(!second.contains(e)) return false;
        }
        
        for(E e : second) {
            if(!first.contains(e)) return false;
        }
        
        return true;
    }
    
    /**
     * Decreases a player's challenge points
     * @param player the player to decrease challenge points for
     */
    public synchronized void decreaseChallenge(Player player) {
        final int currentChallenge = challengePts.get(player);
        challengePts.put(player, currentChallenge-1);
        
        this.notifyAll();
        checkRep();
    }
    
    /**
     * Increments a player's challenge points by 2
     * @param player the player to increase challenge points for by 2
     */
    public synchronized void incrementChallengeByTwo(Player player) {
        final int currentChallenge = challengePts.get(player);
        challengePts.put(player, currentChallenge+2);
        
        this.notifyAll();
        checkRep();
    }
    
    /**
     * Increase a player's score count
     * @param player the player to increase score for
     */
    public synchronized void incrementScore(Player player) {
        final int currentScore = scores.get(player);
        scores.put(player, currentScore+1);
        
        this.notifyAll();
        checkRep();
    }
    
    /**
     * @param player the player we want to get the (cumulative, including challenge points) score for
     * @return the score of the given player (which is number of words confirmed correct + challenge points)
     */
    public synchronized int getScore(Player player) {
        this.notifyAll();
        checkRep();
        
        return scores.get(player) + challengePts.get(player);
    }
    
    /**
     * @param player the player we want to get the challenge points for
     * @return the number of challenge points of the given player
     */
    public synchronized int getChallengePoints(Player player) {
        this.notifyAll();
        checkRep();
        
        return challengePts.get(player);
    }
    
    /**
     * Inserts an attempt at a given location.
     * @param player the player attempting the insert
     * @param wordID the ID of the word being attempted
     * @param tryWord the guessed word
     * @return true iff the insert succeeded, false if the consistency check failed
     */
    public synchronized boolean tryInsert(Player player, int wordID, String tryWord) {
        if(!idToWordMap.containsKey(wordID)) return false;
        
        final Word word = idToWordMap.get(wordID);
        
        this.notifyAll();
        checkRep();
        
        return word.tryInsertNewWord(player, tryWord);
    }
    
    /**
     * Challenges the other player's guess and increments/decrements challenge points accordingly.
     * @param player the player doing the challenging
     * @param wordID the id of the word being challenged
     * @param challengeGuess the actual word used to challenge the other player's guesses
     * @return the result of the challenge in terms of ChallengeResult (INVALID if it wasn't consistent according to the rules, INCORRECT if the challenge was
     * incorrect, and CORRECT if the challenge is correct)
     */
    public synchronized ChallengeResult challenge(Player player, int wordID, String challengeGuess) {
        if(!idToWordMap.containsKey(wordID)) return ChallengeResult.INVALID;

        final Word word = idToWordMap.get(wordID);
        
        this.notifyAll();
        checkRep();
        
        return word.tryChallenge(player, challengeGuess, this);
    }
    
    @Override
    public synchronized String toString() {
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
            resultString += word.getRowLowerBound() + " " + word.getColumnLowerBound() + " " + word.getDirection().name() + " " 
        + word.getID() + " " + String.valueOf(word.hasOwner()) + " " + String.valueOf(word.isConfirmed()) + " " + (word.hasOwner() ? String.valueOf(word.getOwner()) : "") + "\n";
            resultString += word.getHint() + "\n";
        }
        
        return resultString;
    }
    
    /**
     * Checks if this board is consistent with regards to the specifications laid out in the Final Project handout
     * @return true if the board is consistent with regards to the Final Project handout
     */
    public synchronized boolean checkConsistency() {
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
    private synchronized boolean oneDimensionOverlap(int firstLow, int firstHigh, int secondLow, int secondHigh) {
        return firstLow <= secondHigh && secondLow <= firstHigh; // returns true iff [firstLow, firstHigh] and [secondLow, secondHigh] overlap
    }
    
    /**
     * Check if two words (the first must be vertical and second must be horizontal) are consistent with each other.
     * @param verticalWord the vertically aligned word
     * @param horizontalWord the horizontally aligned word
     * @return whether or not the two words (one vertical and one horizontal) are consistent with each other
     */
    private synchronized boolean verticalHorizontalConsistent(Word verticalWord, Word horizontalWord) {
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
    
    /**
     * Get the name of the match
     * @return the name of the match
     */
    public synchronized String getMatchName() {
        this.notifyAll();
        checkRep();
        
        return matchName;
    }
    
    /**
     * Get the description of the match
     * @return the description of the match
     */
    public synchronized String getMatchDescription() {
        this.notifyAll();
        checkRep();
        
        return matchDescription;
    }
    
    /**
     * Determines if this current match is finished, where finished is defined by project handout rules
     * If the current match is finished, then it sets all words with owners to be confirmed and updates the scores of the players accordingly
     * @return true iff match is finished (and all words with owners 
     */
    public synchronized boolean isFinished() {
        for(Word word : this.words) {
            final String currentValue = word.getCurrentValue();
            final String correctValue = word.getCorrectValue();
            if(!currentValue.equals(correctValue)) {
                this.notifyAll();
                checkRep();
                
                return false;
            }
        }
        
        // at this point, we know that the match is done for sure, so we confirm all unconfirmed words and update the score
        
        for(Word word : this.words) {
            if(word.hasOwner() && !word.isConfirmed()) {
                word.setConfirmed();
                incrementScore(word.getOwner());
            }
        }
        
        this.notifyAll();
        checkRep();
        
        return true;
    }
    
    /**
     * Find the winner's player ID of a finished match. If there's a tie, returns "TIE"
     * @return the ID of the winner of the match, based on total points
     */
    public synchronized String calculateWinner() { 
        
        int score1 = this.getScore(players.get(0));
        int score2 = this.getScore(players.get(1));
        if (score1 > score2) {
            return players.get(0).getID();
        }
        else if (score1 < score2) {
            return players.get(1).getID();
        }
        else {
            return "TIE";
        }
        
    }
    
    /**
     * Determines if this match contains given player
     * @param player player to check existence of
     * @return if match contains given player
     */
    public synchronized boolean containsPlayer(Player player) {
        this.notifyAll();
        checkRep();
        
        return players.contains(player);
    }
    
    
    /**
     * Get the other player that is currently playing, where the other player is the player that is NOT the
     * passed in player. It is required that this match has two players (alternatively, the game has started)
     * @param player player to match player against
     * @return other player that does not match player
     */
    public synchronized Player getOtherPlayer(Player player) {
        
        this.notifyAll();
        checkRep();
        
        assert this.gameIsStarted();
        
        if (players.get(0).equals(player)) {
            return players.get(1);
        }
        else {
            return players.get(0);
        }
        
    }
}
