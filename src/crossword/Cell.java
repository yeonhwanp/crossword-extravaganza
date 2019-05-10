package crossword;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

/**
 * Cell, or space of a board to represent a letter of a word.
 *
 */
public class Cell {
    
    // Abstraction function:
    //    AF(row, col, value, correspondingWords, existState) =
    //      A cell on the crossword puzzle at the location [row x col] with the value given by value which corresponds
    //      to one or two words contained in correspondingWords. ExistState defines whether or not this cell is part of
    //      the crossword puzzle.
    // Representation invariant:
    //    row >= 0 && col >= 0
    //    len(value) <= 1
    //
    // Safety from rep exposure:
    //    Constructor takes in immutable types
    //    All methods take in and return immutable types except for addWord
    //    addWord takes in a mutable type but this is intended rep exposure because words need to be shared between cells
    //        because each word occupies multiple cells
    //   
    // Thread safety argument:
    //   This class is not threadsafe, but it's OK because only Match accesses Cell methods, and Match is threadsafe.
    
    public enum Exist {PRESENT, ABSENT}

    private final int row; // (0-indexed)
    private final int col; 
    private char value;
    private final List<Word> correspondingWords;
    private final Exist existState;
    
    private static final char EMPTY_CELL = '?';
    
    /**
     * Constructor for a cell 
     * @param pRow the row that this cell is located at on the gameboard
     * @param pCol the column that this cell is located at on the gameboard
     * @param state A state representing if this cell is part of the gameboard or not 
     */
    public Cell(int pRow, int pCol, Exist state) {
        this.row = pRow;
        this.col = pCol;
        this.value = EMPTY_CELL; // empty cell represents no character there
        this.correspondingWords = new ArrayList<Word>();
        this.existState = state;
        
        checkRep();
    }
    
    /**
     * TODO fix this up
     */
    private void checkRep() {
        assert row >= 0;
        assert col >= 0;
        assert correspondingWords.size() <= 2;
        assert existState != null;
    }
    
    /**
     * @return the row that this cell is located at on the gameboard
     */
    public int getRow() {
        return row;
    }
    
    /**
     * @return the column that this cell is located at on the gameboard
     */
    public int getCol() {
        return col;
    }
    
    /**
     * @return true if this cell is part of the gameboard, false otherwise
     */
    public boolean isPresent() {
        return existState == Exist.PRESENT;
    }
    
    /**
     * @return true if this cell is part of the gameboard, false otherwise
     */
    public boolean isAbsent() {
        return existState == Exist.ABSENT;
    }
    
    /**
     * Change the value of the letter that this cell hosts
     * @param pValue the new value of this cell
     * @param player the player implementing the change
     * @return true if the cell's value has been changed, false otherwise
     */
    public boolean changeValue(char pValue, Player player) {
        if(canChangeValue(player))
        {
            value = pValue;
            
            checkRep();
            return true;
        }
        
        checkRep();
        return false;
    }
    
    /**
     * Clears the cell of its value, only if none of the words that host this cell are controlled by a player
     * @return true iff the clear went through (so none of the corresponding words has an owner)
     */
    public boolean clearValue() {
        if(this.isOwned()) {
            return false;
        }
        
        value = EMPTY_CELL;
        checkRep();
        return true;
    }
    
    /**
     * Checks if the cell is owned (so that one of the corresponding words that hosts this cell is controlled by someone)
     * @return true iff one of the words corresponding to this cell has an owner
     */
    public boolean isOwned() {
        for(Word word : correspondingWords) {
            if(word.hasOwner()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear all of the words that this cell maps to
     */
    public void clearCorrespondingWords() {
        for(Word word : correspondingWords) {
            word.clearThisInsertedWord();
        }
    }
    
    /**
     * @return the value hosted in this cell
     */
    public char getCurrentValue() {
        return value;
    }
    
    /**
     * @return true if the cell doesn't contain value and is part of the gameboard, false otherwise
     */
    public boolean isBlank() {
        return isPresent() && getCurrentValue() == EMPTY_CELL;
    }
    
    /**
     * Adds a word to the guesses associated with the value of the cell
     * @param word the word being guessed
     */ 
    public void addWord(Word word) {
        correspondingWords.add(word);
    }
    
    /**
     * Checks the validity of changeability of this cell, so this can only be changed if the words it's part of are
     * neither confirmed nor controlled by a different player
     * @param player the player that wants to change this cell's value
     * @return true if the cell's value is changeable in accordance with the final project handout
     */
    public boolean canChangeValue(Player player) {
        for(Word word : correspondingWords) {
            if(word.isConfirmed() || (word.hasOwner() && !player.equals(word.getOwner()))) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        if(isAbsent()) {
            return "#";
        }
        return String.valueOf(this.getCurrentValue());
    }
}
