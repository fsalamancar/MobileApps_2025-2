package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    // --- Variables Miembro ---
    private TicTacToeGame mGame;
    private boolean mGameOver;
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;
    private char mTurn = TicTacToeGame.COMPUTER_PLAYER;

    // --- Widgets ---
    private Button mBoardButtons[];
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTiesTextView;
    private Button mNewGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_tic_tac_toe);

        mBoardButtons = new Button[9];
        mBoardButtons[0] = (Button) findViewById(R.id.one);
        mBoardButtons[1] = (Button) findViewById(R.id.two);
        mBoardButtons[2] = (Button) findViewById(R.id.three);
        mBoardButtons[3] = (Button) findViewById(R.id.four);
        mBoardButtons[4] = (Button) findViewById(R.id.five);
        mBoardButtons[5] = (Button) findViewById(R.id.six);
        mBoardButtons[6] = (Button) findViewById(R.id.seven);
        mBoardButtons[7] = (Button) findViewById(R.id.eight);
        mBoardButtons[8] = (Button) findViewById(R.id.nine);

        mInfoTextView = (TextView) findViewById(R.id.information);

        // --- LÍNEAS QUE FALTABAN ---
        // Conectar las variables del marcador con los TextViews del layout
        mHumanScoreTextView = (TextView) findViewById(R.id.human_score);
        mComputerScoreTextView = (TextView) findViewById(R.id.computer_score);
        mTiesTextView = (TextView) findViewById(R.id.ties_score);

        // Conectar el nuevo botón y asignarle su listener
        mNewGameButton = (Button) findViewById(R.id.new_game_button);
        mNewGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });




        mGame = new TicTacToeGame();
        startNewGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add("New Game");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startNewGame();
        return true;
    }

    private void startNewGame() {
        mGame.clearBoard();
        mGameOver = false;

        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        if (mTurn == TicTacToeGame.COMPUTER_PLAYER) {
            mTurn = TicTacToeGame.HUMAN_PLAYER;
            mInfoTextView.setText(R.string.first_human);
        } else {
            mTurn = TicTacToeGame.COMPUTER_PLAYER;
            mInfoTextView.setText(R.string.turn_computer);
            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        }
    }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER) {
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        } else {
            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        @Override
        public void onClick(View view) {
            if (mGameOver) return;

            if (mBoardButtons[location].isEnabled()) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);
                int winner = mGame.checkForWinner();

                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human);
                } else if (winner == 1) {
                    mInfoTextView.setText(R.string.result_tie);
                    mTies++;
                    mTiesTextView.setText("Ties: " + mTies);
                    mGameOver = true;
                } else if (winner == 2) {
                    mInfoTextView.setText(R.string.result_human_wins);
                    mHumanWins++;
                    mHumanScoreTextView.setText("Human: " + mHumanWins);
                    mGameOver = true;
                } else {
                    mInfoTextView.setText(R.string.result_computer_wins);
                    mComputerWins++;
                    mComputerScoreTextView.setText("Android: " + mComputerWins);
                    mGameOver = true;
                }
            }
        }
    }
}

