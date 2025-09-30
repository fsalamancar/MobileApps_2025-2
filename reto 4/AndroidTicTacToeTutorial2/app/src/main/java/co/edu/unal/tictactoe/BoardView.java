package co.edu.unal.tictactoe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BoardView extends View {



    // Anchura de las líneas del triqui
    public static final int GRID_WIDTH = 6;

    // Bitmaps para las imágenes X (humano) y O (computadora)
    private Bitmap mHumanBitmap;
    private Bitmap mComputerBitmap;

    // Pincel para dibujar
    private Paint mPaint;

    // Referencia al juego
    private TicTacToeGame mGame;

    // Constructores
    public BoardView(Context context) {
        super(context);
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    // Inicialización común
    private void initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.x_img);
        mComputerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.o_img);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStrokeWidth(GRID_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    // Inyectar la instancia del juego
    public void setGame(TicTacToeGame game) {
        mGame = game;
        invalidate();
    }

    // Tamaño de celda útil si se necesita externamente
    public int getBoardCellWidth() { return getWidth() / 3; }
    public int getBoardCellHeight() { return getHeight() / 3; }

    // Helper dp a px
    private int dp(int dps) {
        return (int) (dps * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int boardWidth  = getWidth();
        int boardHeight = getHeight();

        int cellW = boardWidth / 3;
        int cellH = boardHeight / 3;

        // Reconfigurar por si cambió (opcional)
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStrokeWidth(GRID_WIDTH);
        mPaint.setAntiAlias(true);

        // Líneas verticales
        canvas.drawLine(cellW, 0, cellW, boardHeight, mPaint);
        canvas.drawLine(cellW * 2, 0, cellW * 2, boardHeight, mPaint);

        // Líneas horizontales
        canvas.drawLine(0, cellH, boardWidth, cellH, mPaint);
        canvas.drawLine(0, cellH * 2, boardWidth, cellH * 2, mPaint);

        // Padding interno para separar piezas de las líneas
        int pad = Math.max(GRID_WIDTH, dp(8));

        // Dibujar piezas según el estado del juego
        if (mGame != null) {
            for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {
                int col = i % 3;
                int row = i / 3;

                int left   = col * cellW + pad;
                int top    = row * cellH + pad;
                int right  = (col + 1) * cellW - pad;
                int bottom = (row + 1) * cellH - pad;

                char occ = mGame.getBoardOccupant(i);
                if (occ == TicTacToeGame.HUMAN_PLAYER) {
                    canvas.drawBitmap(mHumanBitmap, null, new Rect(left, top, right, bottom), null);
                } else if (occ == TicTacToeGame.COMPUTER_PLAYER) {
                    canvas.drawBitmap(mComputerBitmap, null, new Rect(left, top, right, bottom), null);
                }
            }
        }
    }
}
