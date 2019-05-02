package crossword;


public class WordTuple {

    private final String word;
    private final String hint;
    private final String direction;
    private final int startRow;
    private final int startCol;
    
    public WordTuple(final String pWord, final String pHint, final String pDirection, final int pRow, final int pCol) {
        this.word = pWord;
        this.hint = pHint;
        this.direction = pDirection;
        this.startRow = pRow;
        this.startCol = pCol;
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
}
