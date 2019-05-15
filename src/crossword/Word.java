package crossword;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import crossword.Cell.Exist;

/**
 * Word of a crossword puzzle, that can be guessed or challenged
 *
 */
public class Word {
    
    // Abstraction function:
    //    AF(startRow, startCol, id, hint, correctValue, direction, confirmed, owner) =
    //      A word with the value correctValue with an "id" referenced by id that's a part of the crossword puzzle 
    //      that starts at the cell [startRow x startCol] with an orientation direction with a "hint" referenced by hint.
    //      confirmed and owner refer to if this word has been confirmed by either a challenge or a completed game with
    //      owner referencing the player who is "guessing" this word.
    //      
    // Representation invariant:
    //    startRow >= 0 && startCol >= 0
    //    id >= 1
    //    direction == "DOWN" or direction == "ACROSS"
    //    we must also have that the cells in involvedCells are sequential to the word, so involvedCells.get(0) + involvedCells.get(1) + .... forms the word
    //          in other words, we must have that (involvedCells.get(i).getRow() == involvedCells.get(i+1).getRow() AND involvedCells.get(i).getCol() < involvedCells.get(i+1).getCol())
    //          OR (involvedCells.get(i).getRow() < involvedCells.get(i+1).getRow() AND involvedCells.get(i).getCol() == involvedCells.get(i+1).getCol())
    //    involvedCells.size() == correctValue.length()
    //    if a cell is confirmed, it must have an owner, and the getValue must be the correctValue
    //
    // Safety from rep exposure:
    //    startRow, startCol, id, hint, correctValue, and direction are all private and final
    //    confirmed and owner are private and are only changed using methods of the class
    //    All methods take in and return immutable types
    //   
    // Thread safety argument:
    //   This class is not threadsafe, but it's OK because only Match accesses Word methods, and Match is threadsafe.
    
    public enum Direction {ACROSS, DOWN}
    public enum ChallengeResult {INVALID, INCORRECT, CORRECT}

    private final List<Cell> involvedCells;
    private final int startRow;
    private final int startCol;
    private final int id;
    private final String hint;
    private final String correctValue;
    private final Direction direction;
    private boolean confirmed;
    private Optional<Player> owner;
    
    /**
     * Construct a new Word object for our crossword puzzle
     * @param pRow the row of the starting point of the word (leftmost coordinate)
     * @param pCol the column of the starting point of the word (smallest column coordinate)
     * @param inputHint the hint for the word
     * @param pID the ID of the word
     * @param pValue the correct value of the word
     * @param pDirection the direction of the word
     */
    public Word(int pRow, int pCol, String inputHint, int pID, String pValue, String pDirection) {
        this.startRow = pRow;
        this.startCol = pCol;
        this.hint = inputHint;
        this.id = pID;
        this.correctValue = pValue;
        assert pDirection.equals("ACROSS") || pDirection.equals("DOWN");
        this.direction = pDirection.equals("ACROSS") ? Direction.ACROSS : Direction.DOWN;
        this.involvedCells = new ArrayList<>();
        
        this.confirmed = false;
        this.owner = Optional.empty();
        
//        checkRep();
    }
    
    
    /**
     * Check the rep invariant for Word
     */
    private void checkRep() {
        assert this.startRow >= 0 && this.startCol >= 0;
        assert this.id >= 1;
        assert this.direction != null;
        System.out.println(involvedCells.size());
        System.out.println(correctValue.length());
        assert involvedCells.size() == correctValue.length();
        if(this.confirmed) {
            assert this.hasOwner();
        }
        
        for(int i = 0; i < involvedCells.size()-1; i++) {
            if(this.direction == Direction.ACROSS) {
                assert (involvedCells.get(i).getRow() == involvedCells.get(i+1).getRow() && involvedCells.get(i).getCol() + 1 == involvedCells.get(i+1).getCol());
            }
            else {
                assert (involvedCells.get(i).getRow() + 1 == involvedCells.get(i+1).getRow() && involvedCells.get(i).getCol() == involvedCells.get(i+1).getCol());
            }
        }
        
        if(this.isConfirmed()) {
            assert correctValue.equals(this.getCurrentValue());
        }
                
    }
    
    /**
     * Get the ID of the current word
     * @return the ID of the word
     */
    public int getID() {
        checkRep();
        
        return id;
    }
    
    /**
     * Get the correct value of the word
     * @return the correct value
     */
    public String getCorrectValue() {
        checkRep();

        return correctValue;
    }
    
    /**
     * Gets the correct character at a certain index along the word
     * @param i the index to get the correct character of (0-indexed)
     * @return the correct character
     */
    public char getCorrectCharAt(int i) {
        checkRep();

        return correctValue.charAt(i);
    }
    
    /**
     * Return true iff the word is down
     * @return whether or not the word is down
     */
    public boolean isVertical() {
        checkRep();

        return direction == Direction.DOWN;
    }
    
    /**
     * Return true iff the word is across
     * @return whether or not the word is across
     */
    public boolean isHorizontal() {
        checkRep();

        return direction == Direction.ACROSS;
    }
    
    /**
     * Return the direction of the word
     * @return the direction of the word
     */
    public Direction getDirection() {
        checkRep();

        return direction;
    }
    
    /**
     * Get the hint corresponding to the word
     * @return the hint
     */
    public String getHint() {
        checkRep();

        return hint;
    }
    
    /**
     * Get the smallest row index that the word covers (0-indexed)
     * @return the smallest row index
     */
    public int getRowLowerBound() {
        checkRep();

        return startRow;
    }
    
    /**
     * Get the largest row index that the word covers (0-indexed)
     * @return the largest row index
     */
    public int getRowUpperBound() {
        checkRep();
        if (this.isVertical()) {
            return startRow + this.getLength() - 1;
        }
        else {
            return startRow;
        }
    }
    
    /**
     * Get the smallest column index that the word covers (0-indexed)
     * @return the smallest column index
     */
    public int getColumnLowerBound() {
        checkRep();
        return startCol;
    }
    
    /**
     * Get the largest column index that the word covers (0-indexed)
     * @return the largest column index
     */
    public int getColumnUpperBound() {
        checkRep();
        if (this.isHorizontal()) {
            return startCol + this.getLength() - 1;
        }
        else {
            return startCol;
        }
    }
    
    /**
     * Get the length of the correct word
     * @return the length of the correct word
     */
    public int getLength() {
        checkRep();
        return correctValue.length();
    }
    
    /**
     * Get whether or not the word has been confirmed
     * @return whether or not the word has been confirmed
     */
    public boolean isConfirmed() {
        checkRep();
        return confirmed;
    }
    
    /**
     * Set the owner of this word to be newOwner
     * @param newOwner the new owner of this word
     */
    public void setOwner(Player newOwner) {
        owner = Optional.of(newOwner);
        checkRep();
    }
    
    /**
     * Check if the word has an owner (and therefore if the word has an inserted value).
     * @return true iff the word has an owner
     */
    public boolean hasOwner() {
        checkRep();
        return owner.isPresent();
    }
    
    /**
     * Clear the word's owner.
     */
    public void clearOwner() {
        owner = Optional.empty();
        checkRep();
    }
    
    /**
     * Get the owner of this word
     * PRECONDITION: this word must have an owner
     * @return the owner of the word
     */
    public Player getOwner() {
        checkRep();
        if(!hasOwner()) {
            throw new RuntimeException("Tried calling get owner on a word that isn't owned!");
        }
        return owner.get();
    }
    
    /**
     * Set this word to be confirmed
     */
    public void setConfirmed() {
        confirmed = true;
        checkRep();
    }
    
    public void addInvolvedCells(Match currentMatch) {
        final int rowLower = this.getRowLowerBound();
        final int rowHigher = this.getRowUpperBound();
        final int colLower = this.getColumnLowerBound();
        final int colHigher = this.getColumnUpperBound();
        
        for(int i = rowLower; i <= rowHigher; i++) { // NOTE: this order of iteration is CRUCIAL to maintaining the rep invariant 
            for(int j = colLower; j <= colHigher; j++) {
                if(this.gameBoard[i][j].isAbsent()) {
                    this.gameBoard[i][j] = new Cell(i, j, Exist.PRESENT); // be careful, we don't want to override any cells that already exist
                }
//                word.addInvolvedCell(this.gameBoard[i][j]);
                this.gameBoard[i][j].addWord(word);
            }
        }
    }
    
    /**
     * Add a cell that corresponds to this given word.
     * @param cell the cell to include with this word
     */
    public void addInvolvedCell(Cell cell) {
        involvedCells.add(cell);
//        checkRep();
    }
    
    /**
     * Get the current value stored within the cells of this word, even if nobody owns it
     * @return the value of the string stored within the cells
     */
    public String getCurrentValue() {
        String wordValue = "";
        
        for(Cell cell : involvedCells) {
            wordValue += cell.getCurrentValue();
        }
        
        return wordValue;
    }
    
    /**
     * Checks if an attempted word is consistent according to the final project handout
     * @param player the player attempting the word
     * @param tryWord the actual word inserted by the player
     * @return if the insert is valid or not
     */
    public boolean checkConsistentInsert(Player player, String tryWord) {
        checkRep();
        
        if(this.isConfirmed() || (this.hasOwner() && !player.equals(this.getOwner()))) { // check if it's already confirmed or has a different owner
            return false;
        }
        
        if(this.getLength() != tryWord.length()) { // if the guess does not have the same length as this word, reject
            return false;
        }
        
        assert this.involvedCells.size() == this.getLength(); // just to check make sure that the sizes match up, we also need to check the ordering
        
        for(int i = 0; i < this.involvedCells.size(); i++) { // check that if the tried word has conflicts with words already on the board, that the player can change
            final Cell currentCell = this.involvedCells.get(i);
            
            if(currentCell.getCurrentValue() != tryWord.charAt(i) && !currentCell.canChangeValue(player)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * BE VERY CAREFUL WHEN IMPLEMENTING THIS METHOD, DON'T CLEAR CELLS PART OF OTHER WORDS!
     * Clears the cells of the word that do not belong to other inserted words on the board. This word will ONLY be
     * cleared if it has an owner. Then, it will set the word to have no owner.
     */
    public void clearThisInsertedWord() {
        checkRep();
        
        if(!hasOwner()) {
            return;
        }
        
        clearOwner();
        
        for(Cell cell : involvedCells) {
            if(!cell.isOwned()) {
                cell.clearValue();
            }
        }
    }
    
    private void byPassInsert(Player player, String insertValue) {
        
        for(int i = 0; i < this.involvedCells.size(); i++) { // iterate through the cells to clear out the inconsistencies
            final Cell currentCell = this.involvedCells.get(i);

            if(!currentCell.isBlank() && currentCell.getCurrentValue() != insertValue.charAt(i)) {
                currentCell.clearCorrespondingWords();
            }
        }
        
        for(int i = 0; i < this.involvedCells.size(); i++) { // iterate through the cells to put in the new word
            final Cell currentCell = this.involvedCells.get(i);

            final boolean changeValue = currentCell.changeValue(insertValue.charAt(i), player);
            assert changeValue;
        }
        
        setOwner(player);
        checkRep();
    }
    
    /**
     * Try to insert a new word tryWord into this word by player 
     * @param player the player attempting to insert the word into this slot
     * @param tryWord the word we are attempting
     * @return true iff the word was inserted (and was consistent, so it returns false iff it was inconsistent)
     */
    public boolean tryInsertNewWord(Player player, String tryWord) {
        checkRep();
        if(!checkConsistentInsert(player, tryWord)) {
            return false;
        }
        
        byPassInsert(player, tryWord);
        checkRep();
        return true;
    }
    
    /**
     * Check if a word is a consistent challenge, following the rules as specified the problem set handout
     * @param player the player that is making the challenge
     * @param challengeWord the word inputted by the challenging player
     * @return true iff it is a consistent challenge
     */
    public boolean checkConsistentChallenge(Player player, String challengeWord) {
        checkRep();
        
        if(!this.hasOwner() || player.equals(this.getOwner())) {
            return false;
        }
        
        if(this.isConfirmed()) {
            return false;
        }
        
        if(challengeWord.length() != this.getLength() || challengeWord.equals(this.getCurrentValue())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Try a challenge by player player on this word with challengeWord, under the condition that this is on the Match currentMatch
     * Challengers will gain/lose points according to correct challenge rules defined in project specification.
     * @param player must be a player within currentMatch
     * @param challengeWord the new challenge word
     * @param currentMatch the current match that this word and player are on
     * @return the result of the challenge in terms of ChallengeResult (INVALID if it wasn't consistent according to the rules, INCORRECT if the challenge was
     * incorrect, and CORRECT if the challenge is correct)
     */
    public ChallengeResult tryChallenge(Player player, String challengeWord, Match currentMatch) {
        checkRep();
        
        if(!checkConsistentChallenge(player, challengeWord)) { // the challenge was inconsistent, so it was invalid
            return ChallengeResult.INVALID;
        }
        
        final String currentValue = this.getCurrentValue();
        final String correct = this.getCorrectValue();
        
        if(correct.equals(currentValue)) { // original player was correct
            currentMatch.incrementScore(this.getOwner());
            currentMatch.decreaseChallenge(player);
            this.setConfirmed();
            return ChallengeResult.INCORRECT; // the challenging player got it incorrect
        }
        else if(correct.equals(challengeWord)) { // the challenging player got it correct
            byPassInsert(player, challengeWord);
            currentMatch.incrementScore(player);
            currentMatch.incrementChallengeByTwo(player);
            this.setConfirmed();
            return ChallengeResult.CORRECT;
        }
        else { // both players got it incorrect
            this.clearThisInsertedWord();
            currentMatch.decreaseChallenge(player);
            return ChallengeResult.INCORRECT;
        }
    }
    
    
    @Override
    public String toString() {
        checkRep();
        return this.id + ". " + this.correctValue + " at (" + this.startRow + "," + this.startCol + "), in the " + this.direction.name()
                + " direction, with the hint: " + this.hint;
    }
    
}
