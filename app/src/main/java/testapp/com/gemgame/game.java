package testapp.com.gemgame;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Number of squares the game is across
        int gameWidth = 5;
        //Number of squares the game is tall
        int gameHeight = 5;
        //Number of different types of objects in the game
        int gameComplexity = 4;
        //Random Seed for game board
        long randomSeed = 24;

        //Create array to store references to the grid of buttons
        Button buttons[][] = new Button[gameWidth][gameHeight];

        //Put references into the array for each button, based on the ids of the buttons.
        for (int col = 0; col < gameWidth; col++) {
            for (int row = 0; row < gameHeight; row++) {
                //button ids must be in the form of button##, like "button04"
                String buttonID = "button" + col + row;
                int resID = getResources().getIdentifier(buttonID, "id", this.getPackageName());
                buttons[col][row] = (Button) findViewById(resID);

                //create final versions of col, row, buttons for OnClickListener
                final int finalCol = col;
                final int finalRow = row;
                final Button[][] finalButtons = buttons;

                //On button press, run "buttonPress" method with the column and row as parameters.
                buttons[col][row].setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             buttonPress(finalCol, finalRow, finalButtons);
                         }
                     }
                );

            }
        }

        //Create the game board array
        createBoard(gameWidth, gameHeight, gameComplexity, randomSeed);

        //Display the initial game state
        solve();
        displayBoard(buttons);

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
     * Method to put random values into "board" matrix
     * @param width width of the game board
     * @param height height of the game board
     * @param types the number of different kinds of objects on the board (1s, 2s, and 3s would be "3")
     * @param seed the random seed
     */
    private void createBoard(int width, int height, int types, long seed){

        Random randomizer = new Random(); // create new "Random" object
        randomizer.setSeed(seed); //set seed for "Random" object

        //set size of board
        board = new int[width][height];
        typeCounts = new int[types + 1]; //put one more slot in the typeCounts than there are types, since 0s are empty space
        int typeNumber; //the random integer representing the type of object in a particular square

        int wC; //width Counter
        int hC; //height Counter
        for (wC = 0; wC < width; wC++){
            for (hC = 0; hC < height; hC++) {
                //get a random number for this square
                typeNumber = (randomizer.nextInt(types)+1);
                //put the number on the board at the current square
                board[wC][hC] = typeNumber;
                //increment the count, so we know how many squares have that number initially.
                typeCounts[typeNumber]++;
            }
        }

    }

    /**
     * Display board.
     * @param buttons the grid of buttons which we will update to display the board.
     */
    private void displayBoard(Button[][] buttons){
        int dWC; //display Width Counter
        int dHC; //display Height Counter

        //count through each column one at a time
        for (dHC = 0; dHC < board[0].length; dHC++){
            //count through each row one at a time
            for (dWC = 0; dWC < board.length; dWC++){
                //display a " " in place of a 0
                if (board[dWC][dHC] == 0){
                    buttons[dWC][dHC].setText(" ");
                } else {
                    //set the text of the button to the number value of that place on the board
                    buttons[dWC][dHC].setText(String.valueOf(board[dWC][dHC]));
                }
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
     * The Solve function.
     * If 3 or more squares are aligned vertically or horizontally with the same value,
     * it replaces those squares with zero, runs the collapseZeros function, and checks again until it stops solving.
     * It also updates "typeCounts" for the game over function (gameLostCheck)
     */
    private void solve(){
        //Variable to say whether the board is completely solved or needs to be solved again.
        boolean restedState = false; //true = board is finished solving.
        //rested State Counter. We add to it if a solve was made, then count it down. There is a test after the solve code to make "restedState = true" and stop solving if it is zero.
        int solveCounter = 0;
        int solveHappened = 2; //Can make a number larger than one if multiple solve checks are needed.

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
                                && board[hWC][hHC] == board[hWC+1][hHC]){
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
                                && board[vWC][vHC] == board[vWC][vHC+1]){
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

    private boolean buttonIsSelected = false;
    private int selectedCol;
    private int selectedRow;

    /**
     * This method runs every time a button is pressed and will receive the column and row of the button press.
     * col and row should correspond to buttons[col][row] and board[col][row]
     * @param col column of button
     * @param row row of button
     * @param buttons the object array of buttons
     */
    private void buttonPress(int col, int row, Button[][] buttons) {
        if (!buttonIsSelected) {
            //record the row and column of the selected button
            selectedCol = col;
            selectedRow = row;

            //Make selected button blue
            buttons[col][row].setTextColor(Color.BLUE);

            //Make buttons next to selected button red
            //left
            if (col >= 1) {
                buttons[col - 1][row].setTextColor(Color.RED);
            }
            //right
            if (col < buttons.length - 1) {
                buttons[col + 1][row].setTextColor(Color.RED);
            }
            //above
            if (row >= 1) {
                buttons[col][row-1].setTextColor(Color.RED);
            }
            //below
            if (row < buttons[0].length - 1) {
                buttons[col][row+1].setTextColor(Color.RED);
            }

            // tell system that a button is selected
            buttonIsSelected = true;

        }
        //If a button is already selected:
        else {
            //if the buttons are next to each other, swap them
            if (
                // the button is directly to the left or right of the selected button
                (col == selectedCol - 1 || col == selectedCol + 1 && row == selectedRow) ||
                // the button is directly above or below the selected button
                (row == selectedRow - 1 || row == selectedRow + 1 && col == selectedCol)
                    ) {
                exchange(selectedCol, selectedRow, col, row);
            } else {
                Toast invalidButtonToast = Toast.makeText(getApplicationContext(), R.string.invalid_button, Toast.LENGTH_SHORT);
                invalidButtonToast.show();
            }

            //Make all the buttons black again
            for (int i = 0; i < buttons.length; i++) {
                for (int j = 0; j < buttons[0].length; j++) {
                    buttons[i][j].setTextColor(Color.BLACK);
                }
            }

            //a button is no longer selected.
            buttonIsSelected = false;

            //Don't let pieces defy gravity
            collapseZeros();
            //update the game based on the moved pieces
            solve();
            //check if the game is won
            gameWonCheck();
            //check if the game is lost
            gameLostCheck();
            //show the solved game.
            displayBoard(buttons);
        }

    }

    private void exchange(int firstX, int firstY, int secondX, int secondY){
        int firstValue = board[firstX][firstY];
        board[firstX][firstY] = board[secondX][secondY];
        board[secondX][secondY] = firstValue;
    }

    /** check if the game is won
     *
     */
    private void gameWonCheck(){
        int sumBottomRow = 0;
        int sBRC; //sumBottomRowCounter

        for (sBRC = 0; sBRC < board.length; sBRC++){
            sumBottomRow = sumBottomRow + board[sBRC][board[0].length-1];
        }

        if (sumBottomRow == 0) {
            //TODO: Implement game timer
            //double gameTime = elapsedTime();

            gameWon();
        }
    }

    /**
     *  run this method when the game has been won
     *
     */
    private void gameWon(){
        //Pop up a toast if the player wins the game
        Toast wonGameToast = Toast.makeText(getApplicationContext(), R.string.game_won, Toast.LENGTH_LONG);
        wonGameToast.show();
    }

    private void gameLostCheck() {
        for (int type = 1; type < typeCounts.length; type++) {
            if (typeCounts[type] > 0 && typeCounts[type] < 3) {
                Toast lostGameToast = Toast.makeText(getApplicationContext(), R.string.game_lost, Toast.LENGTH_LONG);
                lostGameToast.show();
                break;
            }
        }
    }


}
