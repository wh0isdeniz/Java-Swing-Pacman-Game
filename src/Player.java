import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Player implements KeyListener {
    private int row;
    private int col;
    private double speed = 1.0;
    private boolean invincible = false;
    private long invincibilityEndTime = 0;
    private Direction currentDirection = Direction.RIGHT;
    private boolean isMoving = false;
    private int cellSize;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Player(int row, int col) {
        this.row = row;
        this.col = col;
        this.cellSize = 48; // Default
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        isMoving = true;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                currentDirection = Direction.UP;
                GameAnimations.getInstance(cellSize).setPlayerDirection("up");
                break;
            case KeyEvent.VK_DOWN:
                currentDirection = Direction.DOWN;
                GameAnimations.getInstance(cellSize).setPlayerDirection("down");
                break;
            case KeyEvent.VK_LEFT:
                currentDirection = Direction.LEFT;
                GameAnimations.getInstance(cellSize).setPlayerDirection("left");
                break;
            case KeyEvent.VK_RIGHT:
                currentDirection = Direction.RIGHT;
                GameAnimations.getInstance(cellSize).setPlayerDirection("right");
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        isMoving = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    public void move(GameBoardModel gameModel) {
        int newRow = row;
        int newCol = col;

        switch (currentDirection) {
            case UP:
                newRow--;
                break;
            case DOWN:
                newRow++;
                break;
            case LEFT:
                newCol--;
                break;
            case RIGHT:
                newCol++;
                break;
        }

        // Check if the new position is valid
        if (newRow >= 0 && newRow < gameModel.getRowCount() &&
                newCol >= 0 && newCol < gameModel.getColumnCount() &&
                gameModel.getCell(newRow, newCol) != GameBoardModel.Cell.WALL) {
            row = newRow;
            col = newCol;
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isInvincible() {
        return invincible && System.currentTimeMillis() < invincibilityEndTime;
    }

    public void setInvincible(boolean invincible, long duration) {
        this.invincible = invincible;
        if (invincible) {
            this.invincibilityEndTime = System.currentTimeMillis() + duration;
        }
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public boolean isMoving() {
        return isMoving;
    }
} 