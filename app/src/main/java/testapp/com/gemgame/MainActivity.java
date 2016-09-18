package testapp.com.gemgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }




    /**
     * put a random number into the "magic number" field.
     */
    public void setRandom(View view) {

        //initialize the Random object
        Random randomGenerator = new Random();

        //the highest random number that the program will put into the "magic number" field
        int upperLimit = 25000;

        //generate a random number
        String randomNumber = Integer.toString(randomGenerator.nextInt(upperLimit));

        //get the TextView for the random seed for the game ("magic number")
        TextView magicNumberView = (TextView) findViewById(R.id.magicNumberField);

        //put the random number into the text field.
        magicNumberView.setText(randomNumber);
    }

    public void runGame(View view){
        //launch the game activity!
        Intent intent = new Intent(this, GameActivity.class);

        //get the TextView for the random seed for the game ("magic number")
        TextView magicNumberView = (TextView) findViewById(R.id.magicNumberField);

        //TODO: Make sure the field is an acceptable random seed.
        //get the random seed "magic number" as a string
        String randomSeedString = magicNumberView.getText().toString();
        //convert to a long value
        long randomSeed = Long.parseLong(randomSeedString);
        intent.putExtra("RANDOM_SEED", randomSeed);
        startActivity(intent);
    }

    public void helpPage(View view){
        //launch the game activity!
        Intent helpPageIntent = new Intent(this, HelpPage.class);
        startActivity(helpPageIntent);
    }
}
