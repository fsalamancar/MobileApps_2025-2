package co.edu.unal.tictactoe;

import java.util.Random;

public class TicTacToeGame {


    // Dificultad de la CPU
    public enum DifficultyLevel {Easy, Harder, Expert};

    // Dificultad seleccionada
    private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;

    public DifficultyLevel getDifficultyLevel() {
        return mDifficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
    }




    // Constantes públicas para la lógica del juego
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    // Variable privada para el tamaño del tablero
    private final int BOARD_SIZE = 9;

    // Variables privadas para el estado interno del juego
    private char mBoard[];
    private Random mRand;

    /**
     * Constructor
     */
    public TicTacToeGame() {
        mBoard = new char[BOARD_SIZE];
        mRand = new Random();
        clearBoard();
    }

    // --- MÉTODOS PÚBLICOS (LA "API" DE NUESTRO JUEGO) ---

    /**
     * Limpia el tablero, dejándolo listo para una nueva partida.
     */

    public void clearBoard() {
        // Rellenatodo el tablero con casillas vacías
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }

    /**
     * Coloca una ficha de un jugador en una casilla específica.
     * La casilla debe estar disponible, o el tablero no se modificará.
     *
     * @param player El jugador (HUMAN_PLAYER o COMPUTER_PLAYER)
     * @param location La casilla (0-8) donde colocar la ficha
     */
    public void setMove(char player, int location) {
        // Solo modifica el tablero si la casilla es válida y está vacía
        if (location >= 0 && location < BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player;
        }
    }

    /**
     * Revisa si hay un ganador.
     * @return 0 si no hay ganador ni empate.
     *         1 si es un empate.
     *         2 si el Humano ('X') ganó.
     *         3 si la Computadora ('O') ganó.
     */
    public int checkForWinner() {
        // Revisar victorias horizontales
        for (int i = 0; i <= 6; i += 3) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i+1] == HUMAN_PLAYER && mBoard[i+2] == HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i+1] == COMPUTER_PLAYER && mBoard[i+2] == COMPUTER_PLAYER)
                return 3;
        }

        // Revisar victorias verticales
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i+3] == HUMAN_PLAYER && mBoard[i+6] == HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i+3] == COMPUTER_PLAYER && mBoard[i+6] == COMPUTER_PLAYER)
                return 3;
        }

        // Revisar victorias diagonales
        if ((mBoard[0] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[6] == HUMAN_PLAYER))
            return 2;
        if ((mBoard[0] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[6] == COMPUTER_PLAYER))
            return 3;

        // Revisar si hay empate
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT)
                return 0; // Si hay una casilla vacía, el juego sigue
        }

        return 1; // Si no hay casillas vacías, es un empate
    }


    // ¿El tablero está lleno?
    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) return false;
        }
        return true;
    }

    // Movimiento aleatorio válido
    private int getRandomMove() {
        if (isBoardFull()) return -1; // no hay movimientos
        int move;
        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);
        return move;
    }


    // Intentar ganar en una jugada
    private int getWinningMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER;           // probar jugada de la CPU
                int winner = checkForWinner();         // 3 == gana la CPU en tu lógica
                mBoard[i] = OPEN_SPOT;                 // revertir siempre
                if (winner == 3) return i;
            }
        }
        return -1;
    }


    // Bloquear victoria inmediata del humano
    private int getBlockingMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER;              // simular jugada del humano
                int winner = checkForWinner();         // 2 == gana el humano en tu lógica
                mBoard[i] = OPEN_SPOT;                 // revertir siempre
                if (winner == 2) return i;             // bloquear aquí
            }
        }
        return -1;
    }



    public int getComputerMove() {
        int move = -1;

        if (mDifficultyLevel == DifficultyLevel.Easy) {
            move = getRandomMove();
        } else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = getWinningMove();
            if (move == -1) move = getRandomMove();
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {
            // Try to win, but if that's not possible, block.
            // If that's not possible, move anywhere.
            move = getWinningMove();
            if (move == -1) move = getBlockingMove();
            if (move == -1) move = getRandomMove();
        }

        return move;
    }
}
