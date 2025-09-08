package co.edu.unal.tictactoe;

import java.util.Random;

public class TicTacToeGame {

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
        // Rellena todo el tablero con casillas vacías
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

    /**
     * Devuelve el mejor movimiento para la computadora.
     * Este método NO realiza el movimiento, solo lo calcula.
     * @return La casilla (0-8) del mejor movimiento.
     */
    public int getComputerMove() {
        // 1. Primero, ver si la computadora puede ganar en el siguiente movimiento
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER; // Hacer un movimiento de prueba
                if (checkForWinner() == 3) {
                    mBoard[i] = OPEN_SPOT; // Deshacer el movimiento
                    return i; // Devolver la posición ganadora
                }
                mBoard[i] = OPEN_SPOT; // Deshacer el movimiento
            }
        }

        // 2. Luego, ver si es necesario bloquear al jugador humano
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER; // Probar si el humano ganaría aquí
                if (checkForWinner() == 2) {
                    mBoard[i] = OPEN_SPOT; // Deshacer el movimiento
                    return i; // Devolver la posición para bloquear
                }
                mBoard[i] = OPEN_SPOT; // Deshacer el movimiento
            }
        }

        // 3. Si no hay movimientos críticos, elegir uno aleatorio
        int move;
        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);

        return move;
    }
}
