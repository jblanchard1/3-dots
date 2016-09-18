package testapp.com.gemgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * The core code to run and use the game itself will be here,
 * and other classes and activities will create an object of this type
 * rather than having to include the code in them.
 */
public class GemGame {

    /**
     * Constructor for GemGame class
     * @param width width of the game board
     * @param height height of the game board
     * @param types the number of different kinds of objects on the board (1s, 2s, and 3s would be "3")
     * @param seed the random seed
     */
    public GemGame(int width, int height, int types, long seed){

        //set size of board
        board = new int[width][height];

        //start up typeCounts with the proper number of types
        //put one more slot in the typeCounts than there are types. 0s are empty space so we need room for every type and an empty slot.
        typeCounts = new int[types + 1];

        //Set up the random generator
        Random randomizer = new Random(); // create new "Random" object
        randomizer.setSeed(seed); //set seed for "Random" object

        //This variable will be changed for each square and represents the type that goes in that square.
        int typeNumber;

        //Put a random type into each square on the board
        int wC; //width Counter
        int hC; //height Counter
        for (wC = 0; wC < width; wC++){
            for (hC = 0; hC < height; hC++) {
                //get a random number for this square
                typeNumber = (randomizer.nextInt(types)+1);
                //put the number on the board at the current square
                board[wC][hC] = typeNumber;
                //Increment the type that was just added by one so we start with typeCounts initialized also.
                typeCounts[typeNumber]++;
            }
        }

        solve();
        undoSave();
        createWinCondition();

    }


    /**
     *  The game board. It's an array of arrays, the columns are first and the rows are second
     *  Format: board[column][row]
     */
    private int[][] board;

    /**
     * An array to keep count of how many individual squares of each type are left.
     * This will help us check if the game is unsolveable.
     * The 0th place will be empty, so that each place will correspond to the type it is counting.
     * For example, typeCounts[3] = --the number of 3s left on the board--
     *
     */
    private int[] typeCounts;

    /**
     * A list that contains a copy of the board for each move.
     */
    private ArrayList<int[][]> gameHistory= new ArrayList<>();

    /**
     * An array to keep track of what the typeCounts array should look like when the game is won
     */
    private int[] winCondition;

    /**
     * Getter for the game board
     */
    public int[][] getBoard(){
        return board;
    }

    /**
     * Getter for typeCounts
     */
    public int[] getTypeCounts(){
        return typeCounts;
    }

    /**
     * Getter for winCondition
     */
    public int[] getWinCondition(){
        return winCondition;
    }

    /**
     * Return the board to the last saved state. Returns true if it can successfully undo and false if it cannot
     * TODO: BUG: If you undo all the way back to the beginning, the undo just before the final undo (start over) does nothing.
     */
    public boolean undo() {
        if (gameHistory.size() > 2) {
            int undoIndex = gameHistory.size() - 2;
            board = gameHistory.get(undoIndex);
            gameHistory.remove(undoIndex + 1); //remove the last instance, since we're now on the one before it.
            updateTypeCounts();
            return true;
        } else if (gameHistory.size() == 2){
            startOver();
            return true;
        } else {
            return false;
        }
    }

    /**
     * exchange the value in the board array at (firstX, firstY) with the value in the array at (secondX, secondY)
     * Swapping board[firstX][firstY] with board[secondX][secondY]
     * @param firstX the width coordinate of the first square
     * @param firstY the height coordinate of the first square
     * @param secondX the width coordinate of the second square
     * @param secondY the height coordinate of the second square
     */

    public void exchange(int firstX, int firstY, int secondX, int secondY){
        int firstValue = board[firstX][firstY];
        board[firstX][firstY] = board[secondX][secondY];
        board[secondX][secondY] = firstValue;
    }

    /**
     * All the methods to run after a move is made and the board needs to update to show what has changed
     */

    public void updateBoard() {

        //Don't let pieces defy gravity
        collapseZeros();
        //update the game based on the moved pieces
        solve();
        //save game state for Undo button
        undoSave();
    }

    /**
     * Update type counts so that gameLostCheck functions correctly and you don't get a repeating "game over" when you undo or start over on game over.
     */

    private void updateTypeCounts() {
        //set typeCounts array to 0.
        Arrays.fill(typeCounts, 0);

        //check each square on the board and add one to each type count for the type in the square, ignoring 0s
        int tcWidth; //counter to increment through the width of board array; tc is typeCounts
        int tcHeight; //counter to increment through the height of board array.
        for (tcWidth = 0; tcWidth < board.length; tcWidth++){
            for (tcHeight = 0; tcHeight < board[0].length; tcHeight++) {
                if (board[tcWidth][tcHeight] > 0) {
                    typeCounts[board[tcWidth][tcHeight]] += 1;
                }
            }
        }
    }

    /**
     * Return the board to the first saved state.
     */

    public void startOver() {
        board = gameHistory.get(0);
        gameHistory.clear();
        undoSave();
        updateTypeCounts();
    }

    /**
     *  Method to save a copy of the current board state into the "gameHistory" array.
     *
     *  TODO: limit the undo list to prevent using way too much memory. Maybe disable undo if you win.
     *
     */
    private void undoSave(){
        int[][] currentBoard = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            currentBoard[i] = board[i].clone();
        }
        gameHistory.add(currentBoard);
    }


    /**
     * The Solve function.
     * If 3 or more squares are aligned vertically or horizontally with the same value,
     * it replaces those squares with zero, runs the collapseZeros function, and checks again until it stops solving.
     * It also updates "typeCounts" for the game over function (gameLostCheck)
     */
    private void solve(){
        //Variable to say whether the board is completely solved or needs to be solved again.
        boolean restedState = false; //true = board is finished solving.
        int solveCounter = 0; //rested State Counter. We add to it if a solve was made, then count it down. There is a test after the solve code to make "restedState = true" and stop solving if it is zero.
        int solveHappened = 2; //The number of times to double check for additional solves before declaring the board solved

        while (!restedState) {
            //We'll store the results of checking for horizontal solves here:
            Boolean[][] horSolveMatrix = new Boolean[board.length][board[0].length];
            //We'll store the results of checking for vertical solves here:
            Boolean[][] verSolveMatrix = new Boolean[board.length][board[0].length];

            //Initialize both matrices to "false"
            int fillCounter;
            for (fillCounter = 0; fillCounter < horSolveMatrix.length; fillCounter++){
                Arrays.fill(horSolveMatrix[fillCounter], Boolean.FALSE);
            }
            for (fillCounter = 0; fillCounter < verSolveMatrix.length; fillCounter++){
                Arrays.fill(verSolveMatrix[fillCounter], Boolean.FALSE);
            }

            //Check for horizontal solves, marking places with a horizontal solve as "true" in "horSolveMatrix"
            int hWC; //horizontal Width Counter
            int hHC; //horizontal Height Counter
            for (hHC = 0; hHC < board[0].length; hHC++){ //only works on rectangular boards
                for (hWC = 1; hWC < (board.length-1); hWC++){ //the 1 and -1 make sure we don't overlap the edge of the board later
                    if (board[hWC][hHC] > 0) { //don't check the empty squares
                        if (board[hWC][hHC] == board[hWC-1][hHC]
                                && board[hWC][hHC] == board[hWC+1][hHC]) { //if this value is the same as the ones to the left and right
                            horSolveMatrix[hWC][hHC] = true;
                            solveCounter = solveHappened;
                        } else{
                            horSolveMatrix[hWC][hHC] = false;
                        }
                    }
                }
            }
            //check for vertical solves
            int vWC; //vertical Width Counter
            int vHC; //vertical Height Counter
            for (vHC = 1; vHC < (board[0].length-1); vHC++){//the 1 and -1 make sure we don't overlap the edge of the board later
                for (vWC = 0; vWC < board.length; vWC++){
                    if (board[vWC][vHC] > 0) { //don't check the empty squares
                        if (board[vWC][vHC] == board[vWC][vHC-1]
                                && board[vWC][vHC] == board[vWC][vHC+1]){ //if this value is the same as the ones above and below
                            verSolveMatrix[vWC][vHC] = true;
                            solveCounter = solveHappened;
                        } else{
                            verSolveMatrix[vWC][vHC] = false;
                        }
                    }
                }
            }

            //Use the two matrices to put zeros in solved places on the game board
            int cWC; //change Width Counter
            int cHC; //change Height Counter
            for (cHC = 0; cHC < board[0].length; cHC++){
                for (cWC = 0; cWC < board.length; cWC++){
                    if (horSolveMatrix[cWC][cHC]){

                        //update typeCounts for the game over function
                        //if that square hasn't been solved yet, reduce the count by one for the type in that square
                        if (board[cWC][cHC] != 0) {
                            typeCounts[board[cWC][cHC]] = typeCounts[board[cWC][cHC]] - 1;
                        }
                        //The same thing for the square to the right
                        if (board[cWC + 1][cHC] != 0) {
                            typeCounts[board[cWC + 1][cHC]] = typeCounts[board[cWC + 1][cHC]] - 1;
                        }
                        //The same thing for the square to the left
                        if (board[cWC - 1][cHC] != 0) {
                            typeCounts[board[cWC - 1][cHC]] = typeCounts[board[cWC - 1][cHC]] - 1;
                        }

                        //Make all the squares 0 (interpreted as empty by collapseZeros)
                        board[cWC][cHC] = 0;
                        board[cWC + 1][cHC] = 0; //these shouldn't break anything due to precautions earlier
                        board[cWC - 1][cHC] = 0;
                    }
                    if (verSolveMatrix[cWC][cHC]){

                        //update typeCounts for the game over function
                        //if that square hasn't been solved yet, reduce the count by one for the type in that square
                        if (board[cWC][cHC] != 0) {
                            typeCounts[board[cWC][cHC]] = typeCounts[board[cWC][cHC]] - 1;
                        }
                        //The same thing for the square below
                        if (board[cWC][cHC + 1] != 0) {
                            typeCounts[board[cWC][cHC + 1]] = typeCounts[board[cWC][cHC + 1]] - 1;
                        }
                        //The same thing for the square above
                        if (board[cWC][cHC - 1] != 0) {
                            typeCounts[board[cWC][cHC - 1]] = typeCounts[board[cWC][cHC - 1]] - 1;
                        }

                        //make all those squares 0 (interpreted as empty)
                        board[cWC][cHC] = 0;
                        board[cWC][cHC + 1] = 0;
                        board[cWC][cHC - 1] = 0;
                    }
                }
            }

            //If a solve occurred recently, collapse zeros, only subtract one so we check again for solves
            if (solveCounter > 0) {
                collapseZeros();
                solveCounter = solveCounter - 1;
            }
            //when the counter reaches zero, stop the solve loop.
            if (solveCounter <= 0) {
                restedState = true;
            }
        }
    }


    /**
     * The "gravity" function. Collapses the board downwards, moving the zeros to the top.
     * Zeros are considered empty space.
     */
    private void collapseZeros(){
        int cWC; //collapse width counter
        int cHC; //collapse height counter

        //Go through each square of the board one by one.
        for (cWC = 0; cWC < board.length; cWC++){
            for (cHC = 1; cHC < board[cWC].length; cHC++){
                //If a square is zero and the square above it is not, switch the two.
                if (board[cWC][cHC] == 0 && board[cWC][cHC-1] != 0){
                    board[cWC][cHC] = board[cWC][cHC - 1];
                    board[cWC][cHC - 1] = 0;
                    cHC = 0;
                }
            }
        }
    }


    /**
     * find what to solve for in cases where the initial setup gives the user less than 3 of a type
     * Note: you can test this with seed 3, which has only 2 of the number 2.
     */
    private void createWinCondition() {
        //set up winCondition array to be the same as typeCounts array
        winCondition = new int[typeCounts.length];

        //if any of the typeCounts are below 3, save them in the winCondition array
        for (int i = 0; i < typeCounts.length; i++) {
            if (typeCounts[i] < 3) {
                winCondition[i] = typeCounts[i];
            } else {
                winCondition[i] = 0;
            }
        }

    }

    /** check if the game is won
     *
     */
    public boolean gameWonCheck(){
        if (Arrays.equals(typeCounts, winCondition)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function checks the "typecounts" array to make sure that the game can still be solved.
     * */
    public boolean gameLostCheck() {
        boolean gameLost = false;
        for (int i = 0; i < typeCounts.length; i++) {
            if (typeCounts[i] < 3 && typeCounts[i] != winCondition[i]) {
                gameLost = true;
                break;
            }
        }
        return gameLost;
    }
}
