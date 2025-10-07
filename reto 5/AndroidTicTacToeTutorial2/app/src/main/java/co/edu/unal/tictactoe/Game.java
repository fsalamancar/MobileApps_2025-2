package co.edu.unal.tictactoe;

public class Game {
    private String board;
    private String turn;
    private boolean gameOver;
    private String winner;
    private String gameStatus; // waiting, ongoing, finished
    private String player1Id;
    private String player2Id;
    private boolean player1WantsRematch;
    private boolean player2WantsRematch;

    public Game() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    // Constructor for creating a new game
    public Game(String board, String turn, boolean gameOver, String winner, String gameStatus, String player1Id, String player2Id) {
        this.board = board;
        this.turn = turn;
        this.gameOver = gameOver;
        this.winner = winner;
        this.gameStatus = gameStatus;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1WantsRematch = false;
        this.player2WantsRematch = false;
    }

    public String getBoard() { return board; }
    public String getTurn() { return turn; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
    public String getGameStatus() { return gameStatus; }
    public String getPlayer1Id() { return player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public boolean isPlayer1WantsRematch() { return player1WantsRematch; }
    public boolean isPlayer2WantsRematch() { return player2WantsRematch; }
}
