package testapp.com.gemgame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Button buttons[][] = new Button[5][5];

        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                String buttonID = "button" + col + row;
                int resID = getResources().getIdentifier(buttonID, "id", this.getPackageName());
                buttons[col][row] = (Button) findViewById(resID);
            }
        }

        buttons[0][0].setText("1");
        buttons[2][3].setText("4");

/* this is now old code
        String[] testArray = {"1", "3", "5"};

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.game_square_layout, testArray);

        ListView gameGrid = (ListView) findViewById(R.id.gameGrid);
        gameGrid.setAdapter(adapter);
*/

    }

}
