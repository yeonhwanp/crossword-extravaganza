/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;

import crossword.Client.ClientState;

/**
 * This component allows you to draw a crossword puzzle. Right now it just has
 * some helper methods to draw cells and add text in them, and some demo code
 * to show you how they are used. You can use this code as a starting point when
 * you develop your own UI.
 * @author asolar
 */
class CrosswordCanvas extends JComponent {

    private ClientState state;
    private String request;
    private String currentBoard;
    private String currentPuzzleMatches;
    private String endString;
    
    /*
     * Abstraction Function
     * AF(playerID, matchID, userInput, sendString, canvas) = a canvas represented by a state and the currentBoard if the 
     *                                                        state is PLAY and a list of matches currentPuzzleMathces if the
     *                                                        state is CHOOSE. The request is in line with the latest
     *                                                        text that the user has entered into the text field.
     * 
     * Rep Invariant:
     *  currentBoard only contains ?, #, and letters.
     * 
     * Safety from Rep Exposure:
     *  All variables are private
     *  None of the methods take in or return mutable objects
     *  
     * Thread safety argument:
     *  Threadsafe because client is threadsafe.
     * 
     */

    /**
     * Horizontal offset from corner for first cell.
     */
    private final int originX = 100;
    /**
     * Vertical offset from corner for first cell.
     */
    private final int originY = 60;
    /**
     * Size of each cell in crossword. Use this to rescale your crossword to have
     * larger or smaller cells.
     */
    private final int delta = 30;

    /**
     * Font for letters in the crossword.
     */
    private final Font mainFont = new Font("Arial", Font.PLAIN, delta * 4 / 5);

    /**
     * Font for small indices used to indicate an ID in the crossword.
     */
    private final Font indexFont = new Font("Arial", Font.PLAIN, delta / 3);

    /**
     * Font for small indices used to indicate an ID in the crossword.
     */
    private final Font textFont = new Font("Arial", Font.PLAIN, 16);
    
    /**
     * Font for bold things
     */
    private final Font boldFont = new Font("Arial", Font.BOLD, 32);
    
    /**
     * Generally big font
     */
    private final Font bigFont = new Font("Arial", Font.PLAIN, 32); 

    /**
     * Draw a cell at position (row, col) in a crossword.
     * @param row Row where the cell is to be placed.
     * @param col Column where the cell is to be placed.
     * @param g Graphics environment used to draw the cell.
     */
    private void drawCell(int row, int col, Graphics g) {
        Color oldColor = g.getColor();
        g.drawRect(originX + col * delta,
                   originY + row * delta, delta, delta);
        g.setColor(Color.WHITE);
        g.fillRect(originX + col * delta,
                originY + row * delta, delta, delta);
        g.setColor(oldColor);
    }

    /**
     * Place a letter inside the cell at position (row, col) in a crossword.
     * @param letter Letter to add to the cell.
     * @param row Row position of the cell.
     * @param col Column position of the cell.
     * @param g Graphics environment to use.
     */
    private void letterInCell(String letter, int row, int col, Graphics g) {
        g.setFont(mainFont);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(letter, originX + col * delta + delta / 6,
                             originY + row * delta + fm.getAscent() + delta / 10);
    }

    /**
     * Add a vertical ID for the cell at position (row, col).
     * @param id ID to add to the position.
     * @param row Row position of the cell.
     * @param col Column position of the cell.
     * @param g Graphics environment to use.
     */
    private void verticalId(String id, int row, int col, Graphics g) {
        g.setFont(indexFont);
        g.drawString(id, originX + col * delta + delta / 8,
                         originY + row * delta - delta / 15);
    }

    /**
     * Add a horizontal ID for the cell at position (row, col).
     * @param id ID to add to the position.
     * @param row Row position of the cell.
     * @param col Column position of the cell.
     * @param g Graphics environment to use.
     */
    private void horizontalId(String id, int row, int col, Graphics g) {
        g.setFont(indexFont);
        FontMetrics fm = g.getFontMetrics();
        int maxwidth = fm.charWidth('0') * id.length();
        g.drawString(id, originX + col * delta - maxwidth - delta / 8,
                         originY + row * delta + fm.getAscent() + delta / 15);
    }

    // The three methods that follow are meant to show you one approach to writing
    // in your canvas. They are meant to give you a good idea of how text output and
    // formatting work, but you are encouraged to develop your own approach to using
    // style and placement to convey information about the state of the game.

    private int line = 0;
    
    // The Graphics interface allows you to place text anywhere in the component,
    // but it is useful to have a line-based abstraction to be able to just print
    // consecutive lines of text.
    // We use a line counter to compute the position where the next line of code is
    // written, but the line needs to be reset every time you paint, otherwise the
    // text will keep moving down.
    private void resetLine() {
        line = 0;
    }

    // This code illustrates how to write a single line of text with a particular
    // color.
    private void println(String s, Graphics g) {
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();
        g.setColor(new Color(100, 0, 0));
        g.drawString(s, originX + 500, originY + line * fm.getAscent() * 6 / 5);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }

    // This code shows one approach for fancier formatting by changing the
    // background color of the line of text.
    private void printlnFancy(String s, Graphics g) {

        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getAscent() * 6 / 5;
        int xpos = originX + 500;
        int ypos = originY + line * lineHeight;

        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();

        g.setColor(new Color(0, 0, 0));
        g.fillRect(xpos, ypos - fm.getAscent(), fm.stringWidth(s), lineHeight);
        g.setColor(new Color(200, 200, 0));
        g.drawString(s, xpos, ypos);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }
    
    // Centered text
    private void printlnCenterBig(String s, Graphics g) {
        g.setFont(bigFont);
        FontMetrics fm = g.getFontMetrics();
        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();
        g.setColor(new Color(100, 0, 0));
        int centerX = (1200 - fm.stringWidth(s)) / 2;
        int placeY = 30 + originY + line * fm.getAscent() * 6 / 5;
        // Set the font
        g.drawString(s, centerX, placeY);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }
    
    private void printlnCenterBold(String s, Graphics g) {
        g.setFont(boldFont);
        FontMetrics fm = g.getFontMetrics();
        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();
        g.setColor(new Color(100, 0, 0));
        int centerX = (1200 - fm.stringWidth(s)) / 2;
        int placeY = 30 + originY + line * fm.getAscent() * 6 / 5;
        // Set the font
        g.drawString(s, centerX, placeY);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }
    
    private void printlnCenter(String s, Graphics g) {
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();
        g.setColor(new Color(100, 0, 0));
        int centerX = (1200 - fm.stringWidth(s)) / 2;
        int placeY = 30 + originY + line * fm.getAscent() * 6 / 5;
        // Set the font
        g.drawString(s, centerX, placeY);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }

    private int x = 1;
    
    // =============== MY METHODS =============== /
    
    /**
     * Sets the state of the canvas as well as the parameters for the state.
     * @param state the string representing the state that the match should be in
     * @param input another string representing the parameters for the match.
     */
    public void setRequest(String state, String input) {
        switch (state) {
        case "start":
            this.state = ClientState.START;
            break;
        case "choose":
            this.state = ClientState.CHOOSE;
            break;
        case "wait":
            this.state = ClientState.WAIT;
            break;
        case "play":
            this.state = ClientState.PLAY;
            break;
        case "show_score":
            this.state = ClientState.SHOW_SCORE;
            break;
        default:
            throw new RuntimeException("uh oh");
        }
        request = input;
    }
    
    /**
     * Sets what the canvas should look like
     * @param input the inputed string
     */
    public void setBoard(String input) {
        currentBoard = input;
    }
    
    /**
     * Sets the available puzzles to the string provided
     * @param puzzleMatchString the string of available puzzles and matches
     */
    public void setList(String puzzleMatchString) {
        currentPuzzleMatches = puzzleMatchString;
    }
    
    /**
     * Sets the score to the last updated
     * @param scoreString
     */
    public void setScore(String scoreString) {
        endString = scoreString;
    }
    
    /**
     * @return The state of the client gameside
     */
    public ClientState getState() {
        return state;
    }
    
    /**
     * @return a text representation of the client gameboard along with necessary information
     */
    public String getCurrentBoard() {
        return currentBoard;
    }
    
    /**
     * @return a text representation of the list of valid/available matches.
     */
    public String getListOfMatches() {
        return currentPuzzleMatches;
    }
    
    /**
     * @return To be used for testing
     */
    public String getRequestState() {
        return request;
    }
    
    // =============== MY METHODS =============== //

    /**
     * Simple demo code just to illustrate how to paint cells in a crossword puzzle.
     * The paint method is called every time the JComponent is refreshed, or every
     * time the repaint method of this class is called.
     * We added some state just to allow you to see when the class gets repainted,
     * although in general you wouldn't want to be mutating state inside the paint
     * method.
     * 
     * TODO different UI updates for different game states
     */
    @Override
    public void paint(Graphics g) {
        
        // Clear all the stuff?
        line = 0;
        
        // This is for the START state
        if (state == ClientState.START) {
            if (request.equals("new game")) {
                printlnCenterBig("Welcome to Crossword Extravaganza!", g);
                printlnCenterBig("Please enter a user ID with only: ALPHANUMERICS", g);
            }
            else if (request.equals("try again")) {
                printlnCenterBig("That was an invalid request or the ID already exists.", g);
                printlnCenterBig("Try again!", g);
            }
        }
        else if (state == ClientState.CHOOSE) {
            if (request.equals("new")) {
                printMatchList(g);
            }
            else if (request.equals("try again")) {
                printlnCenterBig("That was an invalid request. Try again!", g);
                printMatchList(g);
            }
        }
        else if (state == ClientState.WAIT) {
            printlnCenterBig("Waiting for other player to join...", g);
        }
        else if (state == ClientState.PLAY) {
            //... Conditionals based on PLAY stuff ...// 
            printBoard(g);
        }
    }
    
    private void printMatchList(Graphics g) {
        String[] lines = currentPuzzleMatches.split("\\n");
        int lineCounter = 0;
        
        // Printing valid puzzles
        printlnCenterBold("Valid Puzzles To Choose From:", g);
        line += 1;
        int validPuzzleCount = Integer.valueOf(lines[lineCounter]);
        lineCounter++;
        for (int i = 0; i < validPuzzleCount; i++) {
            int listCounter = i + 1;
            printlnCenter(listCounter + ". " + lines[lineCounter], g);
            lineCounter++;
        }
        
        // Printing valid Matches
        printlnCenterBold("Valid Matches To Connect To:", g);
        line += 5; // To space out the title and the list
        int validMatchCount = Integer.valueOf(lines[lineCounter]);
        lineCounter++;
        for (int i = 0; i < validMatchCount; i++) {
            int listCounter = i + 1;
            printlnCenter(listCounter + ". " + lines[lineCounter] + " \"" + lines[lineCounter+1] + "\"", g);
            lineCounter += 2;
        }
    }
    
    private void printBoard(Graphics g) {
        
        // First, split input string according to newlines
        String[] lines = currentBoard.split("\\n");
        int lineCounter = 1;
        
        // First line is going to give us dimensions so split according to x
        String[] dimensions = lines[0].split("x");
        
        // Create the board with values
        for (; lineCounter < Integer.valueOf(dimensions[0]) + 1; lineCounter++) {
            for (int j = 0; j <Integer.valueOf(dimensions[1]); j++) {
                
                char thisChar = lines[lineCounter].charAt(j);
                if (thisChar == '?') {
                    drawCell(lineCounter-1, j, g);
                }
                else if (Character.isLetter(thisChar)) {
                    drawCell(lineCounter-1, j, g);
                    letterInCell(Character.toString(thisChar), lineCounter-1, j, g);
                }
            }
        }
        
        // Put in IDs 
        int numCount = 2 * Integer.valueOf(lines[lineCounter]);
        lineCounter++;
        for (; lineCounter < Integer.valueOf(dimensions[0]) + numCount + 1;) {
            String wordString = "";
            
            // Each word is formatted with two lines. The first line has details and second has hints.
            // Add ID and Hint from the two lines then print onto the board
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    String[] split = lines[lineCounter].split(" ");
                    if (split[2].equals("ACROSS")) {
                        horizontalId(split[3], Integer.valueOf(split[0]), Integer.valueOf(split[1]), g);
                    }
                    else if (split[2].equals("DOWN")) {
                        verticalId(split[3], Integer.valueOf(split[0]), Integer.valueOf(split[1]), g);
                    }
                    wordString += split[3] + ". ";
                }
                else if (i == 1) {
                    wordString += " " + lines[lineCounter].substring(1, lines[lineCounter].length()-1);
                }
                lineCounter++;
            }
            println(wordString, g);
        }
        
        // Get score + challenge points.
        System.out.println(currentBoard);
    }
}
