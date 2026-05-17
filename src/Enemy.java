import java.util.Random;

public class Enemy {
    private int row;
    private int col;
    private Direction currentDirection;
    private double speed = 1.0;
    private boolean isFrightened = false;
    private long frightenedEndTime = 0;
    private final Random random;
    private boolean isChasingPlayer = false;
    private long frightenedDuration = 0;
    private boolean slowed = false;
    private long slowedEndTime = 0;
    private boolean frozen = false;
    private long frozenUntil = 0;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Enemy(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.random = new Random();
        this.currentDirection = getRandomDirection();
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

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setFrightened(boolean frightened, long duration) {
        this.isFrightened = frightened;
        this.frightenedDuration = duration;
        System.out.println("[DEBUG] setFrightened(" + frightened + ", " + duration + ") for " + this);
        if (frightened) {
            this.frightenedEndTime = System.currentTimeMillis() + duration;
        }
    }

    public boolean isFrightened() {
        return isFrightened && System.currentTimeMillis() < frightenedEndTime;
    }

    public void setSlowed(boolean slowed, long duration) {
        this.slowed = slowed;
        if (slowed) {
            this.slowedEndTime = System.currentTimeMillis() + duration;
        }
        System.out.println("[DEBUG] setSlowed(" + slowed + ", " + duration + ") for " + this);
    }

    public boolean isSlowed() {
        return slowed && System.currentTimeMillis() < slowedEndTime;
    }

    public void move(GameBoardModel board, Player player) {
        if (isFrightened()) {
            isChasingPlayer = false;
            moveAwayFromPlayer(board, player);
        } else {
            double distance = calculateDistance(row, col, player.getRow(), player.getCol());
            if (distance <= 5) { // Chased by if smaller than 5 blocks
                isChasingPlayer = true;
                moveTowardsPlayer(board, player);
            } else {
                isChasingPlayer = false;
                moveRandomly(board);
            }
        }
    }

    private void moveRandomly(GameBoardModel board) {
        Direction[] possibleDirections = getPossibleDirections(board);
        if (possibleDirections.length > 0) {
            currentDirection = possibleDirections[random.nextInt(possibleDirections.length)];
            moveInCurrentDirection(board);
        }
    }

    private void moveTowardsPlayer(GameBoardModel board, Player player) {
        Direction[] possibleDirections = getPossibleDirections(board);
        if (possibleDirections.length > 0) {
            Direction bestDirection = findBestDirection(possibleDirections, player);
            currentDirection = bestDirection;
            moveInCurrentDirection(board);
        }
    }

    private void moveAwayFromPlayer(GameBoardModel board, Player player) {
        Direction[] possibleDirections = getPossibleDirections(board);
        if (possibleDirections.length > 0) {
            Direction bestDirection = findWorstDirection(possibleDirections, player);
            currentDirection = bestDirection;
            moveInCurrentDirection(board);
        }
    }

    private Direction[] getPossibleDirections(GameBoardModel board) {
        // Check all four directions and return those that are valid moves
        Direction[] allDirections = Direction.values();
        int validCount = 0;

        for (Direction dir : allDirections) {
            if (isValidMove(row + getRowOffset(dir), col + getColOffset(dir), board)) {
                validCount++;
            }
        }

        Direction[] validDirections = new Direction[validCount];
        int index = 0;

        for (Direction dir : allDirections) {
            if (isValidMove(row + getRowOffset(dir), col + getColOffset(dir), board)) {
                validDirections[index++] = dir;
            }
        }

        return validDirections;
    }

    private Direction findBestDirection(Direction[] possibleDirections, Player player) {
        Direction bestDirection = possibleDirections[0];
        double minDistance = Double.MAX_VALUE;

        for (Direction dir : possibleDirections) {
            int newRow = row + getRowOffset(dir);
            int newCol = col + getColOffset(dir);
            double distance = calculateDistance(newRow, newCol, player.getRow(), player.getCol());

            if (distance < minDistance) {
                minDistance = distance;
                bestDirection = dir;
            }
        }

        return bestDirection;
    }

    private Direction findWorstDirection(Direction[] possibleDirections, Player player) {
        Direction worstDirection = possibleDirections[0];
        double maxDistance = 0;

        for (Direction dir : possibleDirections) {
            int newRow = row + getRowOffset(dir);
            int newCol = col + getColOffset(dir);
            double distance = calculateDistance(newRow, newCol, player.getRow(), player.getCol());

            if (distance > maxDistance) {
                maxDistance = distance;
                worstDirection = dir;
            }
        }

        return worstDirection;
    }

    private void moveInCurrentDirection(GameBoardModel board) {
        int newRow = row + getRowOffset(currentDirection);
        int newCol = col + getColOffset(currentDirection);

        if (isValidMove(newRow, newCol, board)) {
            row = newRow;
            col = newCol;
        }
    }

    private int getRowOffset(Direction dir) {
        switch (dir) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }

    private int getColOffset(Direction dir) {
        switch (dir) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    private boolean isValidMove(int newRow, int newCol, GameBoardModel board) {
        if (newRow < 0 || newRow >= board.getRowCount() ||
                newCol < 0 || newCol >= board.getColumnCount()) {
            return false;
        }

        return board.getCell(newRow, newCol) != GameBoardModel.Cell.WALL;
    }

    private double calculateDistance(int row1, int col1, int row2, int col2) {
        return Math.sqrt(Math.pow(row2 - row1, 2) + Math.pow(col2 - col1, 2));
    }

    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public boolean isChasingPlayer() {
        return isChasingPlayer;
    }

    public void freeze(long durationMillis) {
        this.frozen = true;
        this.frozenUntil = System.currentTimeMillis() + durationMillis;
    }

    public void updateFrozenState() {
        if (frozen && System.currentTimeMillis() > frozenUntil) {
            frozen = false;
        }
    }

    public boolean isFrozen() {
        return frozen;
    }
}