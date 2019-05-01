package crossword;

import java.util.Optional;

import crossword.Cell.Exist;

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
    //    id >= 0 && id should be unique
    //    direction == "DOWN" or direction == "ACROSS"
    //
    // Safety from rep exposure:
    //    startRow, startCol, id, hint, correctValue, and direction are all private and final
    //    confirmed and owner are private and are only changed using methods of the class
    //    All methods take in and return immutable types
    //   
    // Thread safety argument:
    //   TODO: Later
    
    public enum Direction {ACROSS, DOWN}

//    private final List<Cell> involvedCells; //can fix this later after warmup
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
     * @param pDirectionStr the direction of the word
     */
    public Word(int pRow, int pCol, String inputHint, int pID, String pValue, String pDirectionStr) {
        this.startRow = pRow;
        this.startCol = pCol;
        this.hint = inputHint;
        this.id = pID;
        this.correctValue = pValue;
        this.direction = pDirectionStr.equals("ACROSS") ? Direction.ACROSS : Direction.DOWN;
        
        this.confirmed = false;
    }
    
    /**
     * Get the ID of the current word
     * @return the ID of the word
     */
    public int getID() {
        return id;
    }
    
    /**
     * Get the correct value of the word
     * @return the correct value
     */
    public String getCorrectValue() {
        return correctValue;
    }
    
    /**
     * Gets the correct character at a certain index along the word
     * @param i the index to get the correct character of (0-indexed)
     * @return the correct character
     */
    public char getCorrectCharAt(int i) {
        return correctValue.charAt(i);
    }
    
    /**
     * Return true iff the word is down
     * @return whether or not the word is down
     */
    public boolean isVertical() {
        return direction == Direction.DOWN;
    }
    
    /**
     * Return true iff the word is across
     * @return whether or not the word is across
     */
    public boolean isHorizontal() {
        return direction == Direction.ACROSS;
    }
    
    /**
     * Return the direction of the word
     * @return the direction of the word
     */
    public Exist getDirection() {
        return direction;
    }
    
    /**
     * Get the smallest row index that the word covers (0-indexed)
     * @return the smallest row index
     */
    public int getRowLowerBound() {
        return startRow;
    }
    
    /**
     * Get the largest row index that the word covers (0-indexed)
     * @return the largest row index
     */
    public int getRowUpperBound() {
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
        return startCol;
    }
    
    /**
     * Get the largest column index that the word covers (0-indexed)
     * @return the largest column index
     */
    public int getColumnUpperBound() {
        if (this.isHorizontal()) {
            return startCol + this.getLength() - 1;
        }
        else {
            return startCol;
        }
    }
    
    /**
     * Get the length of the word
     * @return the length
     */
    public int getLength() {
        return correctValue.length();
    }
    
    /**
     * Get whether or not the word has been confirmed
     * @return whether or not the word has been confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    @Override
    public String toString() {
        return this.id + ". " + this.correctValue + " at (" + this.startRow + "," + this.startCol + "), in the " + this.direction.name()
                + " direction, with the hint: " + this.hint;
    }
    
}
