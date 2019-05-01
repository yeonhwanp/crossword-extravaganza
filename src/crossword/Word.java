package crossword;

import java.util.Optional;

public class Word {
    
    public enum Direction {ACROSS, DOWN}

//    private final List<Cell> involvedCells; //can fix this later after warmup
    private final int startRow;
    private final int startCol;
    private Optional<Player> owner;
    private boolean confirmed;
    private final String hint;
    private final int id;
    private final String correctValue;
    private final Direction direction;
    
    public Word(int pRow, int pCol, String inputHint, int pID, String pValue, String pDirectionStr) {
        this.startRow = pRow;
        this.startCol = pCol;
        this.hint = inputHint;
        this.id = pID;
        this.correctValue = pValue;
        this.direction = pDirectionStr.equals("ACROSS") ? Direction.ACROSS : Direction.DOWN;
        
        this.confirmed = false;
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
