package testapp.com.gemgame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String[] testArray = {"1", "3", "5"};

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.game_square_layout, testArray);

        ListView gameGrid = (ListView) findViewById(R.id.gameGrid);
        gameGrid.setAdapter(adapter);

    }

}
