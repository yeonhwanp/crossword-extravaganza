package crossword;

import java.util.List;
import java.util.Optional;

public class Word {

    private final List<Cell> involvedCells;
    private Optional<Player> owner;
    private boolean confirmed;
    private final String hint;
    private final int id;
    private final String correctValue;
    
    public Word(int pRow, int pCol, String inputHint, int pID, String pValue) {
        
    }
    
    public String getCorrectValue() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public boolean isVertical() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public boolean isHorizontal() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public int getRowLowerBound() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public int getRowUpperBound() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public int getColumnLowerBound() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public int getColumnUpperBound() {
        throw new RuntimeException("not yet implemented!");
    }
    
    public int getLength() {
        throw new RuntimeException("not yet implemented!");
    }
}
