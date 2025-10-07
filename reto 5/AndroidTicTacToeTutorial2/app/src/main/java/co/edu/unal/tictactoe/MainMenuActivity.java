package co.edu.unal.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Random;

public class MainMenuActivity extends AppCompatActivity {

    private final Random random = new Random();
    private ImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        gifImageView = findViewById(R.id.image_logo);

        Button playOnlineButton = findViewById(R.id.button_play_online);
        Button playLocalButton = findViewById(R.id.button_play_local);

        playOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, OnlineGameActivity.class);
                startActivity(intent);
            }
        });

        playLocalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, LocalGameActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int[] gifResources = {
                R.drawable.iniciocat,
                R.drawable.iniciocat2,
                R.drawable.iniciocat3,
                R.drawable.iniciocat4
        };

        int randomGif = gifResources[random.nextInt(gifResources.length)];
        Glide.with(this).asGif().load(randomGif).into(gifImageView);
    }
}
