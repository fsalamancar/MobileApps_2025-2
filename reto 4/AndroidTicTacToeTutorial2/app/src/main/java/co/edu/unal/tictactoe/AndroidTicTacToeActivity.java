package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import java.text.BreakIterator;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    // Modelo y vista
    private TicTacToeGame mGame;
    private BoardView mBoardView;

    // Estado
    private boolean mGameOver;
    private int mHumanWins = 0, mComputerWins = 0, mTies = 0;
    private char mTurn = TicTacToeGame.COMPUTER_PLAYER;

    // UI
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView, mComputerScoreTextView, mTiesTextView;
    private Button mNewGameButton;

    // Diálogos
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_ABOUT_ID = 2;

    private SharedPreferences mPrefs;
    public enum DifficultyLevel { Easy, Harder, Expert }



    private void runAfterDelay(long millis, Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(action, millis);
    }

    private void displayScores() {
        mHumanScoreTextView.setText("Human: " + mHumanWins);
        mComputerScoreTextView.setText("Android: " + mComputerWins);
        mTiesTextView.setText("Ties: " + mTies);
    }


    // Touch listener para el tablero
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
            if (mGameOver) return false;

            int col = (int) (event.getX() / mBoardView.getBoardCellWidth());
            int row = (int) (event.getY() / mBoardView.getBoardCellHeight());
            if (col < 0 || col > 2 || row < 0 || row > 2) return false;

            int pos = row * 3 + col;

            if (setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    runAfterDelay(500, () -> {
                        if (mGameOver) return;
                        int move = mGame.getComputerMove();
                        setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                        handleWinner(mGame.checkForWinner());
                    });
                } else {
                    handleWinner(winner);
                }
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_tic_tac_toe);

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        // Restore the scores
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        // UI
        mInfoTextView = findViewById(R.id.information);
        mHumanScoreTextView = findViewById(R.id.human_score);
        mComputerScoreTextView = findViewById(R.id.computer_score);
        mTiesTextView = findViewById(R.id.ties_score);
        mNewGameButton = findViewById(R.id.new_game_button);
        mNewGameButton.setOnClickListener(v -> startNewGame());

        // 3) Muestra puntajes restaurados de preferencias inmediatamente
        displayScores();


        // Modelo y vista
        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        if (savedInstanceState == null) {
            startNewGame();
        } else {
            // Restaurar estado del juego
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            mHumanWins = savedInstanceState.getInt("mHumanWins");
            mComputerWins = savedInstanceState.getInt("mComputerWins");
            mTies = savedInstanceState.getInt("mTies");
            mTurn = savedInstanceState.getChar("mTurn");

            // Asegúrate de que los TextViews del marcador se actualicen
            displayScores();

            mBoardView.invalidate();
        }
    }


    private void startNewGame() {
        mGame.clearBoard();
        mGameOver = false;
        mBoardView.invalidate();

        if (mTurn == TicTacToeGame.COMPUTER_PLAYER) {
            mTurn = TicTacToeGame.HUMAN_PLAYER;
            mInfoTextView.setText(R.string.first_human);
        } else {
            mTurn = TicTacToeGame.COMPUTER_PLAYER;
            mInfoTextView.setText(R.string.turn_computer);
            runAfterDelay(500, () -> {
                if (mGameOver) return;
                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                handleWinner(mGame.checkForWinner());
            });
        }
    }

    // Coloca la pieza si la casilla está libre; redibuja el tablero
    private boolean setMove(char player, int location) {
        if (mGame.getBoardOccupant(location) != TicTacToeGame.OPEN_SPOT) return false;

        mGame.setMove(player, location);
        mBoardView.invalidate();

        // Reproducir efecto de sonido según el jugador
        if (player == TicTacToeGame.HUMAN_PLAYER) {
            if (mHumanMediaPlayer != null) {
                mHumanMediaPlayer.start(); // Sonido para movimiento humano
            }
        } else if (player == TicTacToeGame.COMPUTER_PLAYER) {
            if (mComputerMediaPlayer != null) {
                mComputerMediaPlayer.start(); // Sonido para movimiento de computadora
            }
        }

        return true;
    }


    private void handleWinner(int winner) {
        if (winner == 0) {
            mInfoTextView.setText(R.string.turn_human);
            return;
        }
        if (winner == 1) {
            mInfoTextView.setText(R.string.result_tie);
            mTies++;
            mTiesTextView.setText("Ties: " + mTies);
        } else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            mHumanWins++;
            mHumanScoreTextView.setText("Human: " + mHumanWins);
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            mComputerWins++;
            mComputerScoreTextView.setText("Android: " + mComputerWins);
        }
        mGameOver = true;
    }

    // Menú
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
            startNewGame();
            return true;
        } else if (id == R.id.ai_difficulty) {
            showDialog(DIALOG_DIFFICULTY_ID);
            return true;
        } else if (id == R.id.Reset_Scores) {
            mHumanWins = 0;
            mComputerWins = 0;
            mTies = 0;
            displayScores();

            // También limpia SharedPreferences
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putInt("mHumanWins", 0);
            ed.putInt("mComputerWins", 0);
            ed.putInt("mTies", 0);
            ed.apply();

            Toast.makeText(this, "Scores reset", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.about) {
            showDialog(DIALOG_ABOUT_ID);
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
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        mHumanWins = savedInstanceState.getInt("mHumanWins");
        mComputerWins = savedInstanceState.getInt("mComputerWins");
        mTies = savedInstanceState.getInt("mTies");
        mTurn = savedInstanceState.getChar("mTurn");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Guarda el estado del tablero (debes tener el método en TicTacToeGame)
        outState.putCharArray("board", mGame.getBoardState());

        // Guarda el resto del estado relevante
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("mHumanWins", mHumanWins);
        outState.putInt("mComputerWins", mComputerWins);
        outState.putInt("mTies", mTies);
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putChar("mTurn", mTurn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mComputerWins", mComputerWins);
        ed.putInt("mTies", mTies);
        ed.commit();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_DIFFICULTY_ID: {
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getString(R.string.difficulty_easy),
                        getString(R.string.difficulty_harder),
                        getString(R.string.difficulty_expert)
                };
                int selected = mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Easy ? 0
                        : mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Harder ? 1 : 2;
                builder.setSingleChoiceItems(levels, selected, (d, item) -> {
                    d.dismiss();
                    if (item == 0) mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                    else if (item == 1) mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                    else mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                    Toast.makeText(getApplicationContext(), levels[item], Toast.LENGTH_SHORT).show();
                });
                dialog = builder.create();
                break;
            }
            case DIALOG_QUIT_ID: {
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (d, w) -> finish())
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
            }
            case DIALOG_ABOUT_ID: {
                View layout = getLayoutInflater().inflate(R.layout.about_dialog, null);
                builder.setView(layout).setPositiveButton("OK", null);
                dialog = builder.create();
                break;
            }
        }
        return dialog;
    }
}