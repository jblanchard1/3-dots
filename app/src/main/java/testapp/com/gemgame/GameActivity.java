package testapp.com.gemgame;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

    /**
     * The button array that users click to interact with the game.
     */
    private Button buttons[][];

    /**
     * Variables that will store size and configuration information to help with proper layout and display
     */
    private Configuration configuration;
    private int widthDP;
    private int heightDP;
    private float density;

    /**
     * The array of Views that show what the type count would be to win.
     */
    private TextView[] winConditionViews;

    /**
     * How many different options there will be in the game.
     */
    int gameComplexity;

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
        gameComplexity = getResources().getInteger(R.integer.gameComplexity);

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

        //Get the configuration, height, width, and density of the screen for the layout
        Configuration configuration = this.getResources().getConfiguration();
        widthDP = configuration.screenWidthDp;
        heightDP = configuration.screenHeightDp; //not using until landscape mode is supported
        density = getResources().getDisplayMetrics().density;

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

        //"hint" button pops up a small window with the number of each type currently and the number remaining to win.
        Button hintPopup = (Button) findViewById(R.id.hint_button);
        hintPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintPopup();
            }
        }

        );


        //Update the Game Board to its initial state.
        displayBoard();

    }


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
     * Method that runs when the "Hint" button is clicked. Will popup a Toast showing the number of
     * each type that is remaining, to enable easier solving of the puzzle.
     * The toast should say something like "There are 1: 5, 2: 3, 3: 8, 4:7, of each type remaining
     * to solve the puzzle." and if there are already too few of a certain type, it should say "you
     * don't need to worry about type 3 to win" or whatever type(s) don't need to be solved.
     */

    private void hintPopup() {
        //Initialize string that will eventually be displayed in a toast
        String hintString = "There are ";

        //get the type counts from the game object
        int[] typeCounts = game.getTypeCounts();

        //iterate through the type counts and write "[type]:[number remaining], " for each
        for (int i = 1; i < typeCounts.length; i++) {
            hintString = hintString + String.valueOf(i) + ":" + String.valueOf(typeCounts[i]) + ", ";
        }

        //finish first sentence.
        hintString = hintString + "of each type remaining.";

        //Now add an additional sentence saying to ignore one of the types if it is not necessary.

        //Get win condition array
        int[] winCondition = game.getWinCondition();

        //check each type to see if there will be some remaining when the player wins
        for (int i = 1; i < winCondition.length; i++) {
            if (winCondition[i] > 0) {
                //Add a sentence saying to ignore whichever value should be ignored
                hintString = hintString + " Don't worry about the " + String.valueOf(i) + "s.";
            }
        }

        Toast hintToast = Toast.makeText(getApplicationContext(), hintString, Toast.LENGTH_LONG);
        hintToast.show();
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
     * TODO: Possibly make this a swipe.
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

                if (selectedCol != col && selectedRow != row) {
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
