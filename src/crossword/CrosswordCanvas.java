/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * AF(state, request, currentBoard, currentPuzzleMathces, endString) = a GUI representing a CrosswordExtravaganza
     *                                                                     user interface in the gamestate given by state.
     *                                                                     If the player is in the PLAY state, then they
     *                                                                     are playing on a board referened by currentBoard
     *                                                                     and if they are in the CHOOSE state, then they view
     *                                                                     the list of matches referened by currentPuzzleMatches.
     *                                                                     Finally, if they are in the SHOW_SCORE state, then
     *                                                                     they hold the details to the results of the game in
     *                                                                     endString.                                                           
     *                                                                     
     * 
     * Rep Invariant:
     *  currentBoard only contains ?, #, alphanumerics, and newlines.
     *  in currentPuzzleMatches, integers precede the lines of matches.
     *      For the first set of numbers lines = int.
     *      For the second set of numbers lines = int*2.
     *  TODO endstring and request?
     *  
     * 
     * Safety from Rep Exposure:
     *  All variables are private
     *  None of the methods take in or return mutable objects
     *  
     * Thread safety argument:
     *  Threadsafe because Client is threadsafe and each crowssword canvas is unique to one Client.
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
        g.drawString(s, originX + 400, originY + line * fm.getAscent() * 6 / 5);
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
    
    // Centered bolded text
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
    
    // Centered regular text
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
     * @param state the state of the game the canvas should currently be representing
     * @param input another string representing the parameters for the given state
     */
    public void setRequest(ClientState state, String input) {
        switch (state) {
        case START:
            this.state = ClientState.START;
            break;
        case CHOOSE:
            this.state = ClientState.CHOOSE;
            break;
        case WAIT:
            this.state = ClientState.WAIT;
            break;
        case PLAY:
            this.state = ClientState.PLAY;
            break;
        case SHOW_SCORE:
            this.state = ClientState.SHOW_SCORE;
            break;
        default:
            throw new RuntimeException("Should never be here");
        }
        request = input;
    }
    
    /**
     * Sets the board string of the canvas 
     * @param input the string representation of a board to update the canvas with
     */
    public void setBoard(String input) {
        currentBoard = input;
    }
    
    /**
     * Sets the available puzzles to the string provided TODO bad spec?
     * @param puzzleMatchString the string of available puzzles and matches
     */
    public void setList(String puzzleMatchString) {
        currentPuzzleMatches = puzzleMatchString;
    }
    
    /**
     * Updates the endgame score
     * @param scoreString the string holding the score information of both players
     */
    public void setScore(String scoreString) {
        endString = scoreString;
    }
    
    /**
     * @return The state of the CrosswordCanvas game
     */
    public ClientState getState() {
        return state;
    }
    
    /**
     * @return a text representation of the client gameboard along with the necessary information TODO william
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
     * @return the parameters of the state
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
     */
    @Override
    public void paint(Graphics g) {
        
        // Clear all the stuff?
        line = 0;
        
        // This is for the START state
        if (state == ClientState.START) {
            if (request.equals("new game")) {
                printStartInstructions(g);
            }
            else if (request.equals("try again")) {     
                printStartInstructions(g);
                
                line += 10;
                printlnCenterBold("That was an invalid request or the ID already exists.", g);
                printlnCenterBold("Try again!", g);
            }
        }
        else if (state == ClientState.CHOOSE) {
            if (request.equals("new")) {
                printChooseInstructions(g);
                printMatchList(g);
            }
            else if (request.equals("try again")) {
                printChooseInstructions(g);
                printMatchList(g);
                printlnCenterBold("That was an invalid request. Try again!", g);
            }
        }
        else if (state == ClientState.WAIT) {
            printlnCenterBold("Waiting for other player to join...", g);
            ++line;
            printlnCenterBig("Enter EXIT to go back to the lobby.", g);
        }
        else if (state == ClientState.PLAY) {
            //... Conditionals based on PLAY stuff ...// 
            printBoard(g);
        }
        else if (state == ClientState.SHOW_SCORE) {
            printScores(g);
        }
    }
    
    // Method to help print necessary information at the start state
    private void printStartInstructions(Graphics g) {
        printlnCenterBig("Welcome to Crossword Extravaganza!", g);
        ++line;
        printlnCenterBig("Interact with the UI by entering text then clicking on the ENTER button.", g);
        printlnCenterBig("Words in ALLCAPS should be entered as-is.", g);
        printlnCenterBig("Otherwise, follow the instructions on the screen.", g);
        ++line;
        printlnCenterBold("Please enter into the textbox: START player_ID", g);
        printlnCenterBig("player_ID should only be composed of alphanumerics.", g);
    }
    
    // Method to help print necessary information at the choose state
    private void printChooseInstructions(Graphics g) {
        printlnCenterBold("Valid Commands:", g);
        ++line;
        printlnCenter("PLAY Match_ID: Match_ID Should be obtained from the valid puzzles to choose from.", g);
        ++line;
        printlnCenter("NEW Match_ID Puzzle_ID \"Description\": Match_ID should be a unique alphanumeric identifier", g);
        ++line;
        printlnCenter("EXIT", g);
        line -= 2;
    }
    
    // Method to print scores when the game is over
    private void printScores(Graphics g) {
        String[] lines = endString.split("\\n");
        int lineCounter = 1;
        
        Color oldColor = g.getColor();
        FontMetrics fm = g.getFontMetrics();
        
        printlnCenterBold("The game is over! Winner: " + request, g);
        
        ++line;
        // Print out my score
        g.setColor(new Color(100, 0, 0));
        g.setFont(textFont);
        
        line += 3;
        g.drawString("Your total score: " + lines[lineCounter], originX + 250, originY + line * fm.getAscent() * 6 / 5);
        g.drawString(lines[lineCounter+2] + "'s total score: " + lines[lineCounter+3], originX + 500, originY + line * fm.getAscent() * 6 / 5);
        ++line;
        g.drawString("Your challenge points: " + lines[lineCounter+1], originX + 250, originY + line * fm.getAscent() * 6 / 5);
        g.drawString(lines[lineCounter+2] + "'s challenge points: " + lines[lineCounter+4], originX + 500, originY + line * fm.getAscent() * 6 / 5);

        g.setColor(oldColor);
    }
    
    // Method to print the list of valid and available matches
    private void printMatchList(Graphics g) {
        String[] lines = currentPuzzleMatches.split("\\n");
        int lineCounter = 0;
        
        // Printing valid puzzles
        printlnCenterBold("Valid Puzzles To Choose From:", g);
        line += 6;
        int validPuzzleCount = Integer.valueOf(lines[lineCounter]);
        lineCounter++;
        for (int i = 0; i < validPuzzleCount; i++) {
            int listCounter = i + 1;
            printlnCenter(listCounter + ". " + lines[lineCounter], g);
            lineCounter++;
        }
        
        line -= 5;
        // Printing valid Matches
        printlnCenterBold("Valid Matches To Connect To:", g);
        line += 10; // To space out the title and the list
        int validMatchCount = Integer.valueOf(lines[lineCounter]);
        lineCounter++;
        for (int i = 0; i < validMatchCount; i++) {
            int listCounter = i + 1;
            printlnCenter(listCounter + ". " + lines[lineCounter] + " \"" + lines[lineCounter+1] + "\"", g);
            lineCounter += 2;
        }
    }
    
    // Method to help print the board
    private void printBoard(Graphics g) {
        
        Map<String, Set<String>> ownedMap = new HashMap<>();
        Map<String, Set<String>> confirmedMap = new HashMap<>();
        Map<String, Integer> scoreMap = new HashMap<>();
        Map<String, Integer> challengeMap = new HashMap<>();
        
        // First, split input string according to newlines
        String[] lines = currentBoard.split("\\n");
        int lineCounter = 0;
        
        // Get first player
        ownedMap.put(lines[0], new HashSet<String>());
        confirmedMap.put(lines[lineCounter], new HashSet<String>());
        String myScore = lines[1];
        String myChallengePoints = lines[2];
        lineCounter += 3;
        
        // Get second player
        ownedMap.put(lines[lineCounter], new HashSet<String>());
        confirmedMap.put(lines[lineCounter], new HashSet<String>());
        String theirScore = lines[lineCounter+1];
        String theirChallengePoints = lines[lineCounter+2];
        lineCounter += 3;
        // First line after is going to give us dimensions so split according to x
        String[] dimensions = lines[lineCounter].split("x");
        lineCounter++;

        // Create the board with values
        for (int i = 0; i < Integer.valueOf(dimensions[0]); i++) {
            for (int j = 0; j <Integer.valueOf(dimensions[1]); j++) {
                char thisChar = lines[lineCounter].charAt(j);
                if (thisChar == '?') {
                    drawCell(i, j, g);
                }
                else if (Character.isLetter(thisChar)) {
                    drawCell(i, j, g);
                    letterInCell(Character.toString(thisChar), i, j, g);
                }
            }
            lineCounter++;
        }

        // Put in IDs 
        int numCount = 2 * Integer.valueOf(lines[lineCounter]);
        lineCounter++;
        
        // There are 8 words. There are 2 lines for each word.
        for (int i = 0; i < numCount; i += 2) {
            int wordIndex = lineCounter + i;
            String wordString = "";
            for (int j = 0; j < 2; j++) {
                if (j == 0) {
                    String[] split = lines[wordIndex].split(" ");
                    if (split[2].equals("ACROSS")) {
                        horizontalId(split[3], Integer.valueOf(split[0]), Integer.valueOf(split[1]), g);
                    }
                    else if (split[2].equals("DOWN")) {
                        verticalId(split[3], Integer.valueOf(split[0]), Integer.valueOf(split[1]), g);
                    }
                    wordString += "ID: " + split[3];
                }
                else if (j == 1) {
                    wordString += "     Hint: " + lines[wordIndex+1];
                }
            }
            println(wordString, g);
        }
        
        // Get score + challenge points.
    }
}
