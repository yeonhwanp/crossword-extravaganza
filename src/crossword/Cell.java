package crossword;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

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
    //   TODO: Later
    
    public enum Exist {PRESENT, ABSENT}

    private final int row; // (0-indexed)
    private final int col; 
    private String value;
    private final List<Word> correspondingWords;
    private final Exist existState;
    
    private static final String EMPTY_CELL = "?";
    
    /**
     * Constructor for a cell 
     * @param pRow the row that this cell is located at on the gameboard
     * @param pCol the column that this cell is located at on the gameboard
     * @param state A state representing if this cell is part of the gameboard or not 
     */
    public Cell(int pRow, int pCol, Exist state) {
        row = pRow;
        col = pCol;
        value = EMPTY_CELL; // empty cell represents no character there
        correspondingWords = new ArrayList<Word>();
        existState = state;
        
        checkRep();
    }
    
    private void checkRep() {
        // TODO
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
            value = String.valueOf(pValue);
            return true;
        }
        
        return false;
    }
    
    /**
     * Clears the cell of its value
     * @param player the player implementing the change
     * @return true if the cell's value has been changed, false otherwise
     */
    public boolean clearValue(Player player) {
        if(canChangeValue(player))
        {
            value = EMPTY_CELL;
            return true;
        }
        
        return false;
    }
    
    /**
     * @return the value hosted in this cell
     */
    public String getValue() {
        return value;
    }
    
    /**
     * @return true if the cell doesn't contain value and is part of the gameboard, false otherwise
     */
    public boolean isEmpty() {
        throw new RuntimeException("not done implementing");
    }
    
    /**
     * Adds a word to the guesses associated with the value of the cell
     * @param word the word being guessed
     */ 
    public void addWord(Word word) {
        correspondingWords.add(word);
    }
    
    /**
     * Checks the validity of changeability of this cell
     * @param player the player that wants to change this cell's value
     * @return true if the cell's value is changeable in accordance with the final project handout
     */
    public boolean canChangeValue(Player player) {
        throw new RuntimeException("not done implementing");
    }
    
    @Override
    public String toString() {
        if(isAbsent()) {
            return "#";
        }
        return getValue();
    }
}
