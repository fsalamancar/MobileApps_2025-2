package co.edu.unal.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Random;

public class MainMenuActivity extends AppCompatActivity {

    private static final int[] GIF_RESOURCES = {
            R.drawable.iniciocat,
            R.drawable.iniciocat2,
            R.drawable.iniciocat3,
            R.drawable.iniciocat4,
            R.drawable.lose_gif,
            R.drawable.win_gif,
            R.drawable.tie_gif
    };

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ImageView gifImageView = findViewById(R.id.image_logo);
        Button playOnlineButton = findViewById(R.id.button_play_online);
        Button playLocalButton = findViewById(R.id.button_play_local);

        int randomGif = GIF_RESOURCES[random.nextInt(GIF_RESOURCES.length)];
        Glide.with(this).asGif().load(randomGif).into(gifImageView);

        playOnlineButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, OnlineGameActivity.class);
            startActivity(intent);
        });

        playLocalButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, LocalGameActivity.class);
            startActivity(intent);
        });
    }
}
