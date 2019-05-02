package crossword;

/**
 * Immutable data type to represent word class
 * @author christophercheung
 *
 */
public class WordTuple {

    private final String word;
    private final String hint;
    private final String direction;
    private final int startRow;
    private final int startCol;
    private final int id;
    
    public WordTuple(final int pRow, final int pCol, final String pHint, final int pID, final String pWord, final String pDirection) {
        this.word = pWord;
        this.hint = pHint;
        this.direction = pDirection;
        this.startRow = pRow;
        this.startCol = pCol;
        this.id = pID;
    }
    
    public String getWord() {
        return word;
    }
    
    public String getHint() {
        return hint;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public int getRow() {
        return startRow;
    }
    
    public int getCol() {
        return startCol;
    }
    
    public int getID() {
        return id;
    }
}
