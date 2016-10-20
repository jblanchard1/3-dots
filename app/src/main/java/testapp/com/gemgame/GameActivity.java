package testapp.com.gemgame;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    /**
     * The game object (in GemGame.class) does the functions of the game behind the scenes
     * This activity provides an interface to interact with it.
     */

    GemGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        /**
         * Collect all the information needed to create the game object.
         */

        //Number of squares the game is across
        int gameWidth = getResources().getInteger(R.integer.gameWidth);

        //Number of squares the game is tall
        int gameHeight = getResources().getInteger(R.integer.gameHeight);

        //Number of different types of objects in the game
        int gameComplexity = getResources().getInteger(R.integer.gameComplexity);

        //Random Seed:
        //First we need to get the extras from the intent:
        Bundle extras = getIntent().getExtras();
        //Now get the randomSeed from the extras.
        long randomSeed = extras.getLong("RANDOM_SEED");

        /**
         * Create the game object
         */
        game = new GemGame(gameWidth, gameHeight, gameComplexity, randomSeed);

        /**
         * Create the game interface
         */
        //Find the Table Layout that we are currently using for the game interface
        TableLayout gameTable = (TableLayout) findViewById(R.id.game_interface);

                //Give buttons array the correct number of elements. (other methods use this array to interact with the buttons)
        buttons = new Button[gameWidth][gameHeight];

        //Find size of the entire window
        Configuration configuration = this.getResources().getConfiguration();
        int widthDP = configuration.screenWidthDp;
        int heightDP = configuration.screenHeightDp; //not using until landscape mode is supported
        float density = getResources().getDisplayMetrics().density;

        //Calculate the Height and Width of individual buttons in pixels.
        int buttonHeight = (int) Math.floor(widthDP / gameHeight * density); //TODO: update for landscape. using width for portrait
        int buttonWidth = (int) Math.floor(widthDP / gameWidth * density);

        //Set the layout height of the gameTable
        gameTable.setMinimumHeight((int) Math.floor(widthDP * density));//TODO: need to fix for landscape mode

        //Loop this once for each row in the game
        for (int bRow = 0; bRow < gameHeight; bRow++){

            //Create a TableRow object
            TableRow currentRow = new TableRow(gameTable.getContext());

            //Put the TableRow we just created into the game table
            gameTable.addView(currentRow);

            //Loop this once for each column in the game
            for (int bCol = 0; bCol < gameWidth; bCol++){

                //Create a button object
                buttons[bCol][bRow] = new Button(this);
                //buttons[bCol][bRow] = (Button) getLayoutInflater().inflate(R.layout.game_button, (ViewGroup) currentRow.getRootView(), false);

                //Add the button object to the row we created in the outer loop
                currentRow.addView(buttons[bCol][bRow]);

                //Make the button the correct size
                buttons[bCol][bRow].setHeight(buttonHeight);
                buttons[bCol][bRow].setWidth(buttonWidth);
                buttons[bCol][bRow].setMinimumWidth(0);
                buttons[bCol][bRow].setMinimumHeight(0);

                //Create a final version of the current row and column values so we can set these values permanently in the button listener
                final int fCol = bCol;
                final int fRow = bRow;

                //run the buttonPress function for the current bCol and bRow when the button we just created is pressed
                buttons[bCol][bRow].setOnClickListener(
                    new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             buttonPress(fCol, fRow);
                         }
                     }
                );
            }
        }


        /**
         * Initialize other buttons (To Win, Undo, Start Over)
         */
        //Undo button runs undo() method and updates board
        Button undoButton = (Button) findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  //Run the undo function. If it returns false, give the error message.
                  if (!game.undo()) {
                      Toast cantUndoToast = Toast.makeText(getApplicationContext(), R.string.cant_undo, Toast.LENGTH_LONG);
                      cantUndoToast.show();
                  }
                  displayBoard();
              }
          }
        );

        //Start Over button returns the board to the initial state.
        Button startOverButton = (Button) findViewById(R.id.start_over_button);
        startOverButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      game.startOver();
                      displayBoard();
                  }
              }
        );

        //"How to win" button pops up a small window with the number of each type currently and the number remaining to win.
        Button winConditionPopup = (Button) findViewById(R.id.win_condition_button);
        winConditionPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Make this :D
            }
        }
        );

        /**
         *
         * Need to replace this with a popup from the "how to win button.

        //Type Count TextViews will display the amount of each type that is left on the board
        typeCountViews = new TextView[gameComplexity + 1]; //same as length of TypeCounts, but that hasn't been initialized yet.
        for (int type = 1; type < (gameComplexity + 1); type++) {
            String typeCountViewID = "type_count_" + type;
            int typeCountResID = getResources().getIdentifier(typeCountViewID, "id", this.getPackageName());
            typeCountViews[type] = (TextView) findViewById(typeCountResID);
        }

        //Win Condition TextViews will display the amount of each type that the user needs to win the game
        winConditionViews = new TextView[gameComplexity + 1]; //same as length of WinConditions, but that hasn't been initialized yet.
        for (int type = 1; type < (gameComplexity + 1); type++) {
            String winConditionViewID = "win_count_" + type;
            int winConditionResID = getResources().getIdentifier(winConditionViewID, "id", this.getPackageName());
            winConditionViews[type] = (TextView) findViewById(winConditionResID);
        }
         */


        //Update the Game Board to its initial state.
        displayBoard();

    }



    /**
     * The button array that users click to interact with the game.
     */
    private Button buttons[][];

    /**
     * The array of Views that show the type counts
     */
    private TextView[] typeCountViews;

    /**
     * The array of Views that show what the type count would be to win.
     */
    private TextView[] winConditionViews;

    /**
     * Display board.
     */
    private void displayBoard(){

        int[][] board = game.getBoard();

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
     *  Update the text for each type to show how many are left in typeCounts
     */
    private void displayTypeCount () {
        //get the type counts from the game object
        int[] typeCounts = game.getTypeCounts();

        //iterate through the type counts objects and set each to the proper integer based on the current count
        for (int i = 1; i < typeCounts.length; i++) {
            typeCountViews[i].setText(String.valueOf(typeCounts[i]));
        }
    }


    /**
     * Detect whether a button is currently selected in the game (true = button is selected)
     */
    private boolean buttonIsSelected = false;

    /**
     * The column where the selected button is located
     */
    private int selectedCol;

    /**
     * The row where the selected button is located
     */
    private int selectedRow;

    /**
     * This method runs every time a button is pressed and will receive the column and row of the button press.
     * col and row should correspond to buttons[col][row] and board[col][row]
     * @param col column of button
     * @param row row of button
     */
    private void buttonPress(int col, int row) {
        int[][] board = game.getBoard();

        int defaultButtonColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int selectedButtonColor = ContextCompat.getColor(this, R.color.buttonSelectedColor);
        int tradeButtonColor = ContextCompat.getColor(this, R.color.buttonTradeColor);


        if (!buttonIsSelected) {
            if (board[col][row] != 0) {
                //record the row and column of the selected button
                selectedCol = col;
                selectedRow = row;

                //Make selected button the selected color
                buttons[col][row].setTextColor(selectedButtonColor);

                //Make buttons next to selected button another color
                //(if statements are to prevent errors if the button is on the edge of the board)
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

            //if the buttons are next to each other, swap them
            if (
                    // the second button is directly to the left or right of the selected button
                    ( (col == selectedCol - 1 || col == selectedCol + 1) && row == selectedRow) ||
                    // OR the second button is directly above or below the selected button
                    ( (row == selectedRow - 1 || row == selectedRow + 1) && col == selectedCol)
                ) {
                game.exchange(selectedCol, selectedRow, col, row);

                //update the board (perform basic game functions like applying gravity and solving)
                updateBoard();

            } else {

                if (selectedCol != col && selectedRow != row) {//TODO: don't prompt if you click the same button a second time.
                    Toast invalidButtonToast = Toast.makeText(getApplicationContext(), R.string.invalid_button, Toast.LENGTH_SHORT);
                    invalidButtonToast.show();
                }
            }

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

    /**
     * All the methods to run after a move is made and the board needs to update to show what has changed
     */

    private void updateBoard() {
        game.updateBoard();

        //check if the game is won
        if (game.gameWonCheck()) {
            gameWon();
        }
        //check if the game is lost
        if (game.gameLostCheck()){
            gameLost();
        }
        //show the solved game.
        displayBoard();
    }




    private void displayWinCondition() {
        //get winCondition
        int[] winCondition = game.getWinCondition();

        for (int i = 1; i < winCondition.length; i++) {
            winConditionViews[i].setText(String.valueOf(winCondition[i]));
        }
    }



    /**
     *  run this method when the game has been won
     */
    private void gameWon(){
        //Pop up a toast if the player wins the game
        Toast wonGameToast = Toast.makeText(getApplicationContext(), R.string.game_won, Toast.LENGTH_LONG);
        wonGameToast.show();
    }



    private void gameLost() {
        Toast lostGameToast = Toast.makeText(getApplicationContext(), R.string.game_lost, Toast.LENGTH_LONG);
        lostGameToast.show();
    }








}
