package crossword;

import java.util.Optional;

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
    
    public Word(int pRow, int pCol, String inputHint, int pID, String pValue, String pDirectionStr) {
        this.startRow = pRow;
        this.startCol = pCol;
        this.hint = inputHint;
        this.id = pID;
        this.correctValue = pValue;
        this.direction = pDirectionStr.equals("ACROSS") ? Direction.ACROSS : Direction.DOWN;
        
        this.confirmed = false;
    }
    
    public int getID() {
        return id;
    }
    
    public String getCorrectValue() {
        return correctValue;
    }
    
    public char getCorrectCharAt(int i) {
        return correctValue.charAt(i);
    }
    
    public boolean isVertical() {
        return direction == Direction.DOWN;
    }
    
    public boolean isHorizontal() {
        return direction == Direction.ACROSS;
    }
    
    public int getRowLowerBound() {
        return startRow;
    }
    
    public int getRowUpperBound() {
        if (this.isVertical()) {
            return startRow + this.getLength() - 1;
        }
        else {
            return startRow;
        }
    }
    
    public int getColumnLowerBound() {
        return startCol;
    }
    
    public int getColumnUpperBound() {
        if (this.isHorizontal()) {
            return startCol + this.getLength() - 1;
        }
        else {
            return startCol;
        }
    }
    
    public int getLength() {
        return correctValue.length();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    @Override
    public String toString() {
        return this.id + ". " + this.correctValue + " at (" + this.startRow + "," + this.startCol + "), in the " + this.direction
                + " direction, with the hint: " + this.hint;
    }
    
}
