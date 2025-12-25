package com.eric.GobangGameMC;

public class GobangBoard {

    private static final int BOARD_SIZE = 15;
    private int[][] board;

    public GobangBoard() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
    }

    public boolean makeMove(int row, int col, int pieceType) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }

        if (board[row][col] != 0) {
            return false;
        }

        board[row][col] = pieceType;
        return true;
    }

    public boolean checkWin(int row, int col, int pieceType) {
        // Check horizontal
        if (countConsecutive(row, col, 0, 1, pieceType) >= 5) return true;

        // Check vertical
        if (countConsecutive(row, col, 1, 0, pieceType) >= 5) return true;

        // Check diagonal (top-left to bottom-right)
        if (countConsecutive(row, col, 1, 1, pieceType) >= 5) return true;

        // Check diagonal (top-right to bottom-left)
        if (countConsecutive(row, col, 1, -1, pieceType) >= 5) return true;

        return false;
    }

    private int countConsecutive(int row, int col, int rowDir, int colDir, int pieceType) {
        int count = 1; // Count the current position

        // Check in positive direction
        count += countDirection(row, col, rowDir, colDir, pieceType);

        // Check in negative direction
        count += countDirection(row, col, -rowDir, -colDir, pieceType);

        return count;
    }

    private int countDirection(int startRow, int startCol, int rowDir, int colDir, int pieceType) {
        int count = 0;
        int row = startRow + rowDir;
        int col = startCol + colDir;

        while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            if (board[row][col] == pieceType) {
                count++;
                row += rowDir;
                col += colDir;
            } else {
                break;
            }
        }

        return count;
    }

    public boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getPiece(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return -1;
        }
        return board[row][col];
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public int[][] getBoardState() {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }
}