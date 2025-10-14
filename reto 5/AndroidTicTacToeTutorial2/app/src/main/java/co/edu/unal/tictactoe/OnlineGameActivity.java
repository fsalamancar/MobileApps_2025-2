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
    private TextView mHumanScoreTextView, mComputerScoreTextView, mTiesTextView, mRoomIdTextView;
    private Button mPlayAgainButton;
    private Button mBackButton;

    private SharedPreferences mPrefs;

    // Firebase
    private FirebaseDatabase mDatabase;
    private DatabaseReference mGameRef;
    private ValueEventListener mGameListener;
    private String mGameId;
    private char mPlayer; // This player's symbol ('X' or 'O')
    private char mCurrentTurn; // The symbol of the player whose turn it currently is

    // New: Variable to track who started the *previous* game, to alternate turns for *rematches*
    private char mLastStartingPlayer = TicTacToeGame.HUMAN_PLAYER; // 'X' starts the very first game of a session/room

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_tic_tac_toe);

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        mHumanWins = 0;
        mComputerWins = 0;
        mTies = 0;
        // Load the last starting player state for persistent alternation across sessions
        mLastStartingPlayer = mPrefs.getString("mLastStartingPlayer", String.valueOf(TicTacToeGame.HUMAN_PLAYER)).charAt(0);


        mInfoTextView = findViewById(R.id.information);
        mHumanScoreTextView = findViewById(R.id.human_score);
        mComputerScoreTextView = findViewById(R.id.computer_score);
        mTiesTextView = findViewById(R.id.ties_score);
        mPlayAgainButton = findViewById(R.id.new_game_button);
        mRoomIdTextView = findViewById(R.id.room_id_text);
        mBackButton = findViewById(R.id.back_button);

        // Set the listener once. Its behavior is controlled in updateInfoText.
        mPlayAgainButton.setOnClickListener(v -> {
            if (mGameRef != null) {
                // Set this player's rematch flag in Firebase
                String rematchPath = (mPlayer == TicTacToeGame.HUMAN_PLAYER) ? "player1WantsRematch" : "player2WantsRematch";
                mGameRef.child(rematchPath).setValue(true);
            }
        });

        mBackButton.setOnClickListener(v -> finish());

        displayScores();

        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        mDatabase = FirebaseDatabase.getInstance();
        mGameOver = false; // Initialize game over state

        promptCreateOrJoin();
    }

    private void promptCreateOrJoin() {
        new AlertDialog.Builder(this)
                .setTitle("Play Online")
                .setMessage("Create a new game or join an existing one.")
                .setPositiveButton("Create Game", (dialog, which) -> createGame())
                .setNegativeButton("Join Game", (dialog, which) -> promptJoinGame())
                .setNeutralButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void createGame() {
        mPlayer = TicTacToeGame.HUMAN_PLAYER; // Player 1 is 'X'
        mGameId = String.format("%06d", new Random().nextInt(999999));
        mGameRef = mDatabase.getReference("games/").child(mGameId);
        mGameRef.onDisconnect().removeValue(); // Handle friend disconnect by removing game

        mGame.clearBoard();
        mGameOver = false;
        // The very first game created in a room ALWAYS starts with 'X'
        // mLastStartingPlayer will be updated for subsequent rematches.
        Game newGame = new Game("         ", String.valueOf(TicTacToeGame.HUMAN_PLAYER), false, "", "waiting", "player1", "");
        mGameRef.setValue(newGame);

        addGameListener();

        mRoomIdTextView.setText("Room ID: " + mGameId);
        mRoomIdTextView.setVisibility(View.VISIBLE);
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
                finish();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
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
                    mPlayer = TicTacToeGame.COMPUTER_PLAYER; // Player 2 is 'O'
                    mGameRef.child("player2Id").setValue("player2");
                    mGameRef.child("gameStatus").setValue("ongoing");
                    addGameListener();
                    mRoomIdTextView.setText("Room ID: " + mGameId);
                    mRoomIdTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Joined game!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Game not found or is full.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to join game.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void addGameListener() {
        // Remove any previous listener to avoid multiple callbacks
        if (mGameListener != null && mGameRef != null) {
            mGameRef.removeEventListener(mGameListener);
        }
        mGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // If the game node is removed from Firebase (e.g., opponent left)
                if (dataSnapshot.getValue() == null) {
                    if (!isFinishing()) { // Prevent toast/finish if activity is already finishing
                        Toast.makeText(OnlineGameActivity.this, "Your friend left the game.", Toast.LENGTH_LONG).show();
                        finish(); // Close this activity
                    }
                    return;
                }

                Game game = dataSnapshot.getValue(Game.class);
                if (game == null || game.getBoard() == null) {
                    return; // Should not happen with proper Firebase setup, but for safety
                }

                // Rematch logic: if both players have requested a rematch
                if (game.isPlayer1WantsRematch() && game.isPlayer2WantsRematch()) {
                    // Only player 1 ('X') handles the actual reset of the game state in Firebase
                    if (mPlayer == TicTacToeGame.HUMAN_PLAYER) {
                        resetGame();
                    }
                    // Crucial: Return here. The reset operation will trigger a new onDataChange
                    // with the fresh game state, preventing double processing.
                    return;
                }

                // --- Game State Update and Sound Logic ---
                char[] oldBoard = mGame.getBoardState().clone(); // Clone current board state
                mGame.setBoardState(game.getBoard().toCharArray()); // Update game model with new board
                char[] newBoard = mGame.getBoardState(); // Get the newly updated board state

                // Play opponent's move sound only if the board visibly changed
                // AND it's now *this* player's turn (meaning the opponent just completed their move)
                boolean boardChanged = !Arrays.equals(newBoard, oldBoard);
                mCurrentTurn = game.getTurn().charAt(0); // Update current turn from Firebase
                if (boardChanged && mCurrentTurn == mPlayer && mComputerMediaPlayer != null) {
                    mComputerMediaPlayer.start();
                }

                // --- Game Over State Change Detection ---
                boolean wasGameOver = mGameOver; // Store previous game over state
                mGameOver = game.isGameOver(); // Update game over state from Firebase

                if (!wasGameOver && mGameOver) {
                    handleWinner(game); // Process winner, update scores
                    // After a game ends, update mLastStartingPlayer based on who *just* started this game
                    // This is important for alternating the *next* game.
                    mLastStartingPlayer = game.getTurn().charAt(0); // Turn *before* this game ended was the starter of this game.
                }

                // Invalidate board view to redraw with new moves
                mBoardView.invalidate();
                // Update UI text and button visibility/state
                updateInfoText(game);
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
            mPlayAgainButton.setVisibility(View.GONE);
            mBackButton.setVisibility(View.VISIBLE);
            mBoardView.setAlpha(0.5f);
            mBoardView.setEnabled(false);
            return;
        } else {
            mBackButton.setVisibility(View.GONE);
            mBoardView.setAlpha(1.0f);
            mBoardView.setEnabled(true);
        }

        if (mGameOver) {
            mPlayAgainButton.setVisibility(View.VISIBLE);

            // Set winner/tie message
            String winner = game.getWinner();
            if (winner.equals("Tie")) {
                mInfoTextView.setText(R.string.result_tie);
            } else if (winner.equals(String.valueOf(mPlayer))) { // This player won
                mInfoTextView.setText(R.string.result_human_wins);
            } else { // Opponent won
                mInfoTextView.setText(R.string.result_friend_wins);
            }

            // Determine button state and text based on rematch flags
            boolean iWantRematch = (mPlayer == TicTacToeGame.HUMAN_PLAYER) ? game.isPlayer1WantsRematch() : game.isPlayer2WantsRematch();
            boolean opponentWantsRematch = (mPlayer == TicTacToeGame.HUMAN_PLAYER) ? game.isPlayer2WantsRematch() : game.isPlayer1WantsRematch();

            if (iWantRematch) {
                // If this player already requested a rematch
                mPlayAgainButton.setText("Waiting for friend...");
                mPlayAgainButton.setEnabled(false); // Disable until opponent responds
            } else if (opponentWantsRematch) {
                // If opponent requested a rematch, this player can accept
                mPlayAgainButton.setText("Friend wants a rematch! Play Again?");
                mPlayAgainButton.setEnabled(true);
            } else {
                // Neither requested, show default "Play Again"
                mPlayAgainButton.setText("Play Again");
                mPlayAgainButton.setEnabled(true);
            }

        } else { // Game is ongoing (not over)
            mPlayAgainButton.setVisibility(View.GONE); // Hide button during active game
            if (mCurrentTurn == mPlayer) { // It's this player's turn
                mInfoTextView.setText(R.string.turn_human);
            } else { // It's opponent's turn
                mInfoTextView.setText(R.string.turn_friend);
            }
        }
    }

    // Handles score updates when a game concludes
    private void handleWinner(Game game) {
        String winner = game.getWinner();
        if (winner.equals("Tie")) {
            mTies++;
        } else if (winner.equals(String.valueOf(mPlayer))) { // This player won
            mHumanWins++;
        } else { // Opponent won
            mComputerWins++; // 'Android' score tracks opponent's wins in this online context
        }
        displayScores(); // Update TextViews with new scores
    }

    // Resets the game state in Firebase for a new round
    private void resetGame() {
        // New logic: Alternate the starting player for the next game
        // If 'X' started last, 'O' starts next, and vice versa.
        char nextStartingPlayer = (mLastStartingPlayer == TicTacToeGame.HUMAN_PLAYER) ? TicTacToeGame.COMPUTER_PLAYER : TicTacToeGame.HUMAN_PLAYER;
        mLastStartingPlayer = nextStartingPlayer; // Update for the *next* next game

        Map<String, Object> updates = new HashMap<>();
        updates.put("board", "         "); // Clear the board
        updates.put("gameOver", false);     // Game is no longer over
        updates.put("winner", "");          // No winner yet
        updates.put("turn", String.valueOf(nextStartingPlayer)); // Set the new starting player
        updates.put("player1WantsRematch", false); // Reset rematch flags
        updates.put("player2WantsRematch", false);
        mGameRef.updateChildren(updates);
    }

    // Touch listener for the BoardView to handle player moves
    private final View.OnTouchListener mTouchListener = (v, event) -> {
        // Only process ACTION_DOWN events
        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
        // Don't allow moves if game is over, game object is null, or it's not this player's turn
        if (mGame == null || mGameOver || mPlayer != mCurrentTurn) return false;

        // Calculate cell position from touch coordinates
        int col = (int) (event.getX() / mBoardView.getBoardCellWidth());
        int row = (int) (event.getY() / mBoardView.getBoardCellHeight());
        int pos = row * 3 + col;

        // If the touched spot is open
        if (mGame.getBoardOccupant(pos) == TicTacToeGame.OPEN_SPOT) {
            if (mHumanMediaPlayer != null) {
                mHumanMediaPlayer.start(); // Play human move sound
            }

            Map<String, Object> updates = new HashMap<>();
            mGame.setMove(mPlayer, pos); // Make the move in the local game model
            updates.put("board", new String(mGame.getBoardState())); // Update board in Firebase

            int winner = mGame.checkForWinner(); // Check for winner after the move
            if (winner != 0) { // If game is over (winner or tie)
                updates.put("gameOver", true);
                // Set winner based on TicTacToeGame's return value
                updates.put("winner", (winner == 1) ? "Tie" : String.valueOf(mPlayer));
            } else { // Game is not over, switch turn
                char nextTurn = (mPlayer == TicTacToeGame.HUMAN_PLAYER) ? TicTacToeGame.COMPUTER_PLAYER : TicTacToeGame.HUMAN_PLAYER;
                updates.put("turn", String.valueOf(nextTurn));
            }
            mGameRef.updateChildren(updates);
        }
        return false; // Indicate that touch event was not fully consumed
    };

    // Updates the score TextViews
    private void displayScores() {
        mHumanScoreTextView.setText("Me: " + mHumanWins);
        mComputerScoreTextView.setText("Your Friend: " + mComputerWins);
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
            // Prompt to create or join a new game.
            // Note: This will effectively abandon the current game by creating a new reference.
            promptCreateOrJoin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize MediaPlayers
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.android);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Release MediaPlayers to free resources
        if (mHumanMediaPlayer != null) {
            mHumanMediaPlayer.release();
            mHumanMediaPlayer = null; // Set to null to avoid using a released player
        }
        if (mComputerMediaPlayer != null) {
            mComputerMediaPlayer.release();
            mComputerMediaPlayer = null; // Set to null
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save scores and last starting player to SharedPreferences asynchronously
        mPrefs.edit()
                .putString("mLastStartingPlayer", String.valueOf(mLastStartingPlayer)) // Save last starting player
                .apply(); // Use apply() for non-blocking write
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGameRef != null) {
            // Remove the game from Firebase when this activity is destroyed.
            // This is a simple way to clean up; in a more complex app, you might
            // differentiate between creator and joiner, or add a 'leave game' button.
            mGameRef.removeValue();
            // Remove the listener to prevent memory leaks and unnecessary callbacks
            if (mGameListener != null) {
                mGameRef.removeEventListener(mGameListener);
            }
        }
    }
}
