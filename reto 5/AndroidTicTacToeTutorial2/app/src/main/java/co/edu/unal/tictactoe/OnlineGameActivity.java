package co.edu.unal.tictactoe;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OnlineGameActivity extends AppCompatActivity {

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    private TicTacToeGame mGame;
    private BoardView mBoardView;

    private boolean mGameOver;
    private int mHumanWins = 0, mComputerWins = 0, mTies = 0;

    private TextView mInfoTextView;
    private TextView mHumanScoreTextView, mComputerScoreTextView, mTiesTextView;
    private Button mPlayAgainButton;

    private SharedPreferences mPrefs;

    // Firebase
    private FirebaseDatabase mDatabase;
    private DatabaseReference mGameRef;
    private ValueEventListener mGameListener;
    private String mGameId;
    private char mPlayer;
    private char mCurrentTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_tic_tac_toe);

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        mInfoTextView = findViewById(R.id.information);
        mHumanScoreTextView = findViewById(R.id.human_score);
        mComputerScoreTextView = findViewById(R.id.computer_score);
        mTiesTextView = findViewById(R.id.ties_score);
        mPlayAgainButton = findViewById(R.id.new_game_button); // Repurposing this button

        displayScores();

        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        mDatabase = FirebaseDatabase.getInstance();

        promptCreateOrJoin();
    }

    private void promptCreateOrJoin() {
        new AlertDialog.Builder(this)
                .setTitle("Play Online")
                .setMessage("Create a new game or join an existing one.")
                .setPositiveButton("Create Game", (dialog, which) -> createGame())
                .setNegativeButton("Join Game", (dialog, which) -> promptJoinGame())
                .setCancelable(false)
                .show();
    }

    private void createGame() {
        mPlayer = TicTacToeGame.HUMAN_PLAYER;
        mGameId = String.format("%06d", new Random().nextInt(999999));
        mGameRef = mDatabase.getReference("games/").child(mGameId);
        mGameRef.onDisconnect().removeValue(); // Friend disconnects handling

        mGame.clearBoard();
        mGameOver = false;
        Game newGame = new Game("         ", "X", false, "", "waiting", "player1", "");
        mGameRef.setValue(newGame);

        addGameListener();

        new AlertDialog.Builder(this)
                .setTitle("Game Code")
                .setMessage("Share this code with your friend: " + mGameId)
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show();
    }

    private void promptJoinGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Game");
        builder.setMessage("Enter the 6-digit game code:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Join", (dialog, which) -> {
            String gameId = input.getText().toString();
            if (gameId.length() == 6) {
                joinGame(gameId);
            } else {
                Toast.makeText(getApplicationContext(), "Invalid code. Please enter a 6-digit code.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void joinGame(String gameId) {
        mGameId = gameId;
        mGameRef = mDatabase.getReference("games/").child(mGameId);
        mGameRef.onDisconnect().removeValue(); // Handle disconnect

        mGameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                if (game != null && "waiting".equals(game.getGameStatus())) {
                    mPlayer = TicTacToeGame.COMPUTER_PLAYER;
                    mGameRef.child("player2Id").setValue("player2");
                    mGameRef.child("gameStatus").setValue("ongoing");
                    addGameListener();
                    Toast.makeText(getApplicationContext(), "Joined game!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Game not found or is full.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to join game.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addGameListener() {
        if (mGameListener != null && mGameRef != null) {
            mGameRef.removeEventListener(mGameListener);
        }
        mGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(OnlineGameActivity.this, "Your friend left the game.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Game game = dataSnapshot.getValue(Game.class);
                if (game == null || game.getBoard() == null) return;

                // Rematch logic
                if (game.isPlayer1WantsRematch() && game.isPlayer2WantsRematch()) {
                    if (mPlayer == TicTacToeGame.HUMAN_PLAYER) { // Only player 1 resets the game
                        resetGame();
                    }
                    return; // Avoid processing the rest of the logic on this update
                }

                char[] oldBoard = mGame.getBoardState().clone();
                char[] newBoard = game.getBoard().toCharArray();

                if (!Arrays.equals(newBoard, oldBoard) && game.getTurn().charAt(0) == mPlayer) {
                    if (mComputerMediaPlayer != null) mComputerMediaPlayer.start();
                }

                mGame.setBoardState(newBoard);
                mGameOver = game.isGameOver();
                mCurrentTurn = game.getTurn().charAt(0);

                mBoardView.invalidate();
                updateInfoText(game);

                if (mGameOver) {
                    handleWinner(game);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OnlineGameActivity.this, "Failed to read game state.", Toast.LENGTH_SHORT).show();
            }
        };
        mGameRef.addValueEventListener(mGameListener);
    }

    private void updateInfoText(Game game) {
        if ("waiting".equals(game.getGameStatus())) {
            mInfoTextView.setText("Waiting for friend to join...");
            return;
        }

        if (mGameOver) {
            mPlayAgainButton.setVisibility(View.VISIBLE);
            String winner = game.getWinner();
            if (winner.equals("Tie")) {
                mInfoTextView.setText(R.string.result_tie);
            } else if (winner.equals(String.valueOf(mPlayer))) {
                mInfoTextView.setText(R.string.result_human_wins);
            } else {
                mInfoTextView.setText(R.string.result_friend_wins);
            }

            // Update Play Again button text based on rematch status
            if (mPlayer == TicTacToeGame.HUMAN_PLAYER && game.isPlayer2WantsRematch()) {
                mPlayAgainButton.setText("Friend wants a rematch! Play Again?");
            } else if (mPlayer == TicTacToeGame.COMPUTER_PLAYER && game.isPlayer1WantsRematch()) {
                mPlayAgainButton.setText("Friend wants a rematch! Play Again?");
            }

        } else {
            mPlayAgainButton.setVisibility(View.GONE);
            if (mCurrentTurn == mPlayer) {
                mInfoTextView.setText(R.string.turn_human);
            } else {
                mInfoTextView.setText(R.string.turn_friend);
            }
        }
    }

    private void handleWinner(Game game) {
        String winner = game.getWinner();
        if (winner.equals("Tie")) {
            mTies++;
            mTiesTextView.setText("Ties: " + mTies);
        } else if (winner.equals(String.valueOf(mPlayer))) {
            mHumanWins++;
            mHumanScoreTextView.setText("Human: " + mHumanWins);
        } else {
            mComputerWins++;
            mComputerScoreTextView.setText("Android: " + mComputerWins);
        }

        mPlayAgainButton.setText("Play Again");
        mPlayAgainButton.setEnabled(true);
        mPlayAgainButton.setOnClickListener(v -> {
            String rematchPath = (mPlayer == TicTacToeGame.HUMAN_PLAYER) ? "player1WantsRematch" : "player2WantsRematch";
            mGameRef.child(rematchPath).setValue(true);
            mPlayAgainButton.setText("Waiting for friend...");
            mPlayAgainButton.setEnabled(false);
        });
    }

    private void resetGame() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("board", "         ");
        updates.put("gameOver", false);
        updates.put("winner", "");
        updates.put("turn", "X");
        updates.put("player1WantsRematch", false);
        updates.put("player2WantsRematch", false);
        mGameRef.updateChildren(updates);
    }

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
            if (mGame == null || mGameOver || mPlayer != mCurrentTurn) return false;

            int col = (int) (event.getX() / mBoardView.getBoardCellWidth());
            int row = (int) (event.getY() / mBoardView.getBoardCellHeight());
            int pos = row * 3 + col;

            if (mGame.getBoardOccupant(pos) == TicTacToeGame.OPEN_SPOT) {
                mGame.setMove(mPlayer, pos);
                if (mHumanMediaPlayer != null) mHumanMediaPlayer.start();

                int winner = mGame.checkForWinner();
                char nextTurn = (mPlayer == TicTacToeGame.HUMAN_PLAYER) ? TicTacToeGame.COMPUTER_PLAYER : TicTacToeGame.HUMAN_PLAYER;

                mGameRef.child("board").setValue(new String(mGame.getBoardState()));
                mGameRef.child("turn").setValue(String.valueOf(nextTurn));

                if (winner != 0) {
                    mGameRef.child("gameOver").setValue(true);
                    mGameRef.child("winner").setValue(winner == 1 ? "Tie" : String.valueOf(mPlayer));
                }
            }
            return false;
        }
    };

    private void displayScores() {
        mHumanScoreTextView.setText("Human: " + mHumanWins);
        mComputerScoreTextView.setText("Android: " + mComputerWins);
        mTiesTextView.setText("Ties: " + mTies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_game) {
            promptCreateOrJoin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.android);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHumanMediaPlayer != null) mHumanMediaPlayer.release();
        if (mComputerMediaPlayer != null) mComputerMediaPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mComputerWins", mComputerWins);
        ed.putInt("mTies", mTies);
        ed.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGameRef != null) {
            mGameRef.removeValue(); // Remove game on graceful exit
            if (mGameListener != null) {
                mGameRef.removeEventListener(mGameListener);
            }
        }
    }
}
