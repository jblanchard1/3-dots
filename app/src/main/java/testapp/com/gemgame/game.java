package testapp.com.gemgame;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //TODO: Get these values from an external resource file in the "values" folder instead of setting them here.
        //Number of squares the game is across
        int gameWidth = 5;
        //Number of squares the game is tall
        int gameHeight = 5;
        //Number of different types of objects in the game
        int gameComplexity = 4;


        //Random Seed:
        //First we need to get the extras from the intent:
        Bundle extras = getIntent().getExtras();
        //Now get the randomSeed from the extras.
        long randomSeed;
        if (extras != null) {
            randomSeed = extras.getLong("RANDOM_SEED");
        } else {
            // a fallback random Seed
            randomSeed = 25;
            Toast badRandomSeedToast = Toast.makeText(getApplicationContext(), R.string.bad_random_seed, Toast.LENGTH_SHORT);
            badRandomSeedToast.show();

        }

        //Create array to store references to the grid of buttons TODO: learn more about final
        final Button buttons[][] = new Button[gameWidth][gameHeight];

        //Put references into the array for each button, based on the ids of the buttons.
        for (int col = 0; col < gameWidth; col++) {
            for (int row = 0; row < gameHeight; row++) {
                //button ids must be in the form of button##, like "button04"
                String buttonID = "button" + col + row;
                int resID = getResources().getIdentifier(buttonID, "id", this.getPackageName());
                buttons[col][row] = (Button) findViewById(resID);

                //create final versions of col, row for OnClickListener
                final int finalCol = col;
                final int finalRow = row;

                //On button press, run "buttonPress" method with the column and row as parameters.
                buttons[col][row].setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             buttonPress(finalCol, finalRow, buttons);
                         }
                     }
                );

            }
        }

        //Undo button runs undo() method and updates board
        Button undoButton = (Button) findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  undo();
                  displayBoard(buttons);
              }
          }
        );

        //Start Over button returns the board to the initial state.
        Button startOverButton = (Button) findViewById(R.id.start_over_button);
        startOverButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      startOver();
                      displayBoard(buttons);
                  }
              }
        );

        //Create the game board array
        createBoard(gameWidth, gameHeight, gameComplexity, randomSeed);
        solve();
        undoSave();
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
            int hWC; //horizontal Width Counter TODO: can we make these more clear by just reausing variables called "width" and "height"?
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
        int defaultButtonColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int selectedButtonColor = ContextCompat.getColor(this, R.color.buttonSelectedColor);
        int tradeButtonColor = ContextCompat.getColor(this, R.color.buttonTradeColor);


        if (!buttonIsSelected) { //TODO: can no longer select empty squares. Is this desirable?
            if (board[col][row] != 0) {
                //record the row and column of the selected button
                selectedCol = col;
                selectedRow = row;

                //Make selected button the selected color
                buttons[col][row].setTextColor(selectedButtonColor);

                //Make buttons next to selected button another color
                //left
                if (col >= 1) {
                    buttons[col - 1][row].setTextColor(tradeButtonColor);
                }
                //right
                if (col < buttons.length - 1) {
                    buttons[col + 1][row].setTextColor(tradeButtonColor);
                }
                //above
                if (row >= 1) {
                    buttons[col][row - 1].setTextColor(tradeButtonColor);
                }
                //below
                if (row < buttons[0].length - 1) {
                    buttons[col][row + 1].setTextColor(tradeButtonColor);
                }

                // tell system that a button is selected
                buttonIsSelected = true;
            }

            //Do nothing if it's 0. That means that's an empty space.

        }
        //If a button is already selected:
        else {
            //Make sure the button isn't blank
            //if (board[col][row] != 0) {
                //if the buttons are next to each other, swap them
                if (
                        // the second button is directly to the left or right of the selected button
                        ( (col == selectedCol - 1 || col == selectedCol + 1) && row == selectedRow) ||
                        // OR the second button is directly above or below the selected button
                        ( (row == selectedRow - 1 || row == selectedRow + 1) && col == selectedCol)
                    ) {
                    exchange(selectedCol, selectedRow, col, row);

                    //update the board (perform basic game functions)
                    updateBoard(buttons);

                } else { //TODO: make this less annoying somehow. Maybe give it a repeat count after the first time you mess up
                    Toast invalidButtonToast = Toast.makeText(getApplicationContext(), R.string.invalid_button, Toast.LENGTH_SHORT);
                    invalidButtonToast.show();
                }
            //}

            //Make all the buttons default color again
            for (int i = 0; i < buttons.length; i++) {
                for (int j = 0; j < buttons[0].length; j++) {
                    buttons[i][j].setTextColor(defaultButtonColor);
                }
            }

            //a button is no longer selected.
            buttonIsSelected = false;


        }

    }

    private void updateBoard(Button[][] buttons) {
        //Don't let pieces defy gravity
        collapseZeros();
        //update the game based on the moved pieces
        solve();
        //check if the game is won
        gameWonCheck();
        //check if the game is lost
        gameLostCheck();
        //save game state for Undo button
        undoSave();
        //show the solved game.
        displayBoard(buttons);
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

    /**
     * This function checks the "typecounts" array to make sure that the game can still be solved.
     * TODO: Fix this check so that games that start with 3 or fewer of a type are still winnable.
     * TODO: add an end-state indicator for when this occurs.
     * TODO: reset when the "start over" button is pressed. BUG: if you undo back to the beginning and then replay, you can't start over.
     */
    private void gameLostCheck() {
        for (int type = 1; type < typeCounts.length; type++) {
            if (typeCounts[type] > 0 && typeCounts[type] < 3) {
                Toast lostGameToast = Toast.makeText(getApplicationContext(), R.string.game_lost, Toast.LENGTH_LONG);
                lostGameToast.show();
                break;
            }
        }
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
     * A list that contains a copy of the board for each move.
     */
    private ArrayList<int[][]> gameHistory= new ArrayList<>();

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
     * Return the board to the last saved state.
     */
    private void undo() {
        if (gameHistory.size() >= 2) {
            int undoIndex = gameHistory.size() - 2;
            board = gameHistory.get(undoIndex);
            gameHistory.remove(undoIndex + 1); //remove the last instance, since we're now on the one before it.
            updateTypeCounts();
        } else {
            Toast cantUndoToast = Toast.makeText(getApplicationContext(), R.string.cant_undo, Toast.LENGTH_LONG);
            cantUndoToast.show();
        }
    }

    /**
     * Return the board to the first saved state.
     * TODO: BUG: it stops working if you undo to the beginning and play over again.
     */

    private void startOver() {
        board = gameHistory.get(0);
        gameHistory.clear();
        undoSave();
        updateTypeCounts();
    }


}
