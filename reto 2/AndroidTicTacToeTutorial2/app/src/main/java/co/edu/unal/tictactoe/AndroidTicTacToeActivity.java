package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;




public class AndroidTicTacToeActivity extends AppCompatActivity {

    // --- Variables Miembro ---
    private TicTacToeGame mGame;
    private boolean mGameOver;
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;
    private char mTurn = TicTacToeGame.COMPUTER_PLAYER;

    static final int DIALOG_ABOUT_ID = 2;


    // --- Widgets ---
    private Button mBoardButtons[];
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTiesTextView;
    private Button mNewGameButton;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;

    private void runAfterDelay(long millis, Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(action, millis);
    }


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
            showDialog(DIALOG_DIFFICULTY_ID); // deprecado, pero sirve para el tutorial
            return true;
        } else if (id == R.id.quit) {
            showDialog(DIALOG_QUIT_ID);
            return true;
        } else if (id == R.id.about) {
            showDialog(DIALOG_ABOUT_ID); // mismo patrón del tutorial
            return true;
        }

        return false;
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);

                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)
                };

                // selected: 0 = Easy, 1 = Harder, 2 = Expert
                int selected;
                TicTacToeGame.DifficultyLevel current = mGame.getDifficultyLevel();
                if (current == TicTacToeGame.DifficultyLevel.Easy) {
                    selected = 0;
                } else if (current == TicTacToeGame.DifficultyLevel.Harder) {
                    selected = 1;
                } else {
                    selected = 2;
                }

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Cerrar diálogo

                                // Fijar el nivel según el índice elegido
                                switch (item) {
                                    case 0:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                        break;
                                    case 1:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                        break;
                                    case 2:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                                        break;
                                }

                                // Toast con el nivel elegido
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                dialog = builder.create();
                break;

            case DIALOG_QUIT_ID:
                // Diálogo de confirmación para salir
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AndroidTicTacToeActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;

            case DIALOG_ABOUT_ID:
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;
        }

        return dialog;
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
            // Si antes jugó la CPU, ahora inicia el humano
            mTurn = TicTacToeGame.HUMAN_PLAYER;
            mInfoTextView.setText(R.string.first_human);
        } else {
            // Le toca iniciar a la CPU
            mTurn = TicTacToeGame.COMPUTER_PLAYER;
            mInfoTextView.setText(R.string.turn_computer);

            // Esperar 1 segundo antes de mover la CPU
            runAfterDelay(500, new Runnable() {
                @Override
                public void run() {
                    if (mGameOver) return;  // por si se reinició durante el retardo
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                }
            });
        }
    }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER) {
            mBoardButtons[location].setTextColor(Color.rgb(255, 209, 102));
        } else {
            mBoardButtons[location].setTextColor(Color.rgb(6, 214, 160));
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
                    // Turno de la CPU: mostrar estado y esperar 1 segundo
                    mInfoTextView.setText(R.string.turn_computer);

                    runAfterDelay(500, new Runnable() {
                        @Override
                        public void run() {
                            if (mGameOver) return; // por si se reinició la partida

                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);

                            int w = mGame.checkForWinner();
                            if (w == 0) {
                                mInfoTextView.setText(R.string.turn_human);
                            } else if (w == 1) {
                                mInfoTextView.setText(R.string.result_tie);
                                mTies++;
                                mTiesTextView.setText("Ties: " + mTies);
                                mGameOver = true;
                            } else if (w == 2) {
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
                    });

                    // Salir para no continuar el flujo hasta que la CPU haya jugado
                    return;
                }

                // Si ya hubo resultado tras la jugada humana
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


