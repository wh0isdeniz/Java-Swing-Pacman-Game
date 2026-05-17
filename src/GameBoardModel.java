import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.awt.Point;

public class GameBoardModel extends AbstractTableModel {
    private final int rowCount;
    private final int colCount;
    private final Cell[][] board;
    private final Random random;
    private int totalFoodCount = 0;
    private int remainingFoodCount = 0;

    public enum Cell {
        EMPTY,
        WALL,
        PLAYER,
        ENEMY,
        FOOD,
        POWER_UP
    }

    public GameBoardModel(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.board = new Cell[rowCount][colCount];
        this.random = new Random();
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize all cells as walls
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                board[i][j] = Cell.WALL;
            }
        }

        // Generate maze using "Prim's" Algorithm
        generateMaze();

        // Place player in the center
        int centerRow = rowCount / 2;
        int centerCol = colCount / 2;
        board[centerRow][centerCol] = Cell.PLAYER;

        // Finding all the reachable empty cells from player start
        Set<Point> reachable = getReachableCells(centerRow, centerCol);

        // Place initial enemies (only on reachable cells)  
        placeEnemies(3, reachable);

        // Place food dots (approximately 40% of reachable empty cells)
        int foodCount = (int)(reachable.size() * 0.4); // 40% of reachable cells
        placeFood(foodCount, reachable);

        // Count total food
        countTotalFood();
    }

    private void generateMaze() {
        // Initialize all cells as walls
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                board[i][j] = Cell.WALL;
            }
        }

        // Start from a random cell
        int startRow = random.nextInt(rowCount - 2) + 1;
        int startCol = random.nextInt(colCount - 2) + 1;
        board[startRow][startCol] = Cell.EMPTY;

        // Create paths using Prim's algorithm
        List<Wall> walls = new ArrayList<>();
        addWalls(startRow, startCol, walls);

        while (!walls.isEmpty()) {
            int wallIndex = random.nextInt(walls.size());
            Wall wall = walls.remove(wallIndex);

            int row = wall.row;
            int col = wall.col;
            Direction dir = wall.direction;

            int newRow = row + dir.row;
            int newCol = col + dir.col;

            if (isValidCell(newRow, newCol) && board[newRow][newCol] == Cell.WALL) {
                // 70% chance to remove the wall
                if (random.nextDouble() < 0.7) {
                    board[newRow][newCol] = Cell.EMPTY;
                    board[row][col] = Cell.EMPTY;
                    addWalls(newRow, newCol, walls);
                }
            }
        }

        // Add some random walls to create more interesting paths
        for (int i = 1; i < rowCount - 1; i++) {
            for (int j = 1; j < colCount - 1; j++) {
                if (board[i][j] == Cell.EMPTY && random.nextDouble() < 0.2) {
                    board[i][j] = Cell.WALL;
                }
            }
        }
    }

    private static class Wall {
        int row, col;
        Direction direction;

        Wall(int row, int col, Direction direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
    }

    private static class Direction {
        int row, col;

        Direction(int row, int col) {
            this.row = row;
            this.col = col;
        }

        static final Direction[] DIRECTIONS = {
                new Direction(-1, 0), // UP
                new Direction(1, 0),  // DOWN
                new Direction(0, -1), // LEFT
                new Direction(0, 1)   // RIGHT
        };
    }

    private void addWalls(int row, int col, List<Wall> walls) {
        for (Direction dir : Direction.DIRECTIONS) {
            int newRow = row + dir.row;
            int newCol = col + dir.col;

            if (isValidCell(newRow, newCol) && board[newRow][newCol] == Cell.WALL) {
                walls.add(new Wall(row, col, dir));
            }
        }
    }

    private boolean isValidCell(int row, int col) {
        return row > 0 && row < rowCount - 1 && col > 0 && col < colCount - 1;
    }

    private int countEmptyCells() {
        int count = 0;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (board[i][j] == Cell.EMPTY) {
                    count++;
                }
            }
        }
        return count;
    }

    private void placeEnemies(int count, Set<Point> reachable) {
        List<Point> emptyList = new ArrayList<>();
        for (Point p : reachable) {
            if (board[p.x][p.y] == Cell.EMPTY) {
                emptyList.add(p);
            }
        }
        Collections.shuffle(emptyList, random);
        for (int i = 0; i < count && i < emptyList.size(); i++) {
            Point p = emptyList.get(i);
            board[p.x][p.y] = Cell.ENEMY;
        }
    }

    private void placeFood(int count, Set<Point> reachable) {
        // First, clear any existing food
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (board[i][j] == Cell.FOOD) {
                    board[i][j] = Cell.EMPTY;
                }
            }
        }
        // Only place food on reachable and empty cells
        List<Point> emptyList = new ArrayList<>();
        for (Point p : reachable) {
            if (board[p.x][p.y] == Cell.EMPTY) {
                emptyList.add(p);
            }
        }
        Collections.shuffle(emptyList, random);
        int placed = 0;
        for (int i = 0; i < count && i < emptyList.size(); i++) {
            Point p = emptyList.get(i);
            board[p.x][p.y] = Cell.FOOD;
            placed++;
        }
        System.out.println("[DEBUG] Food placed (reachable): " + placed + "/" + count);
        // Count total food after placement
        countTotalFood();
        System.out.println("[DEBUG] Final food placement complete. Total food: " + totalFoodCount);
    }

    private void countTotalFood() {
        totalFoodCount = 0;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (board[i][j] == Cell.FOOD) {
                    totalFoodCount++;
                }
            }
        }
        remainingFoodCount = totalFoodCount;
        System.out.println("[DEBUG] Total food count: " + totalFoodCount);
    }

    public void decrementFoodCount() {
        if (remainingFoodCount > 0) {
            remainingFoodCount--;
            System.out.println("[DEBUG] Food collected! Remaining: " + remainingFoodCount + " / " + totalFoodCount);
        } else {
            System.out.println("[DEBUG] Warning: Trying to decrement food count when it's already 0!");
        }
    }

    public int getRemainingFoodCount() {
        // Double check the actual count on the board
        int actualCount = 0;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (board[i][j] == Cell.FOOD) {
                    actualCount++;
                }
            }
        }

        if (actualCount != remainingFoodCount) {
            System.out.println("[DEBUG] Warning: Food count mismatch! Expected: " + remainingFoodCount + ", Actual: " + actualCount);
            remainingFoodCount = actualCount;
        }

        return remainingFoodCount;
    }

    public int getTotalFoodCount() {
        return totalFoodCount;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return board[rowIndex][columnIndex];
    }

    public void setValueAt(int row, int col, Cell value) {
        board[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    public int getRowCountValue() {
        return rowCount;
    }

    public int getColCountValue() {
        return colCount;
    }

    // Find all empty cells reachable by BFS
    private Set<Point> getReachableCells(int startRow, int startCol) {
        Set<Point> visited = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startRow, startCol));
        visited.add(new Point(startRow, startCol));
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            for (Direction dir : Direction.DIRECTIONS) {
                int newRow = p.x + dir.row;
                int newCol = p.y + dir.col;
                Point np = new Point(newRow, newCol);
                if (isValidCell(newRow, newCol) && !visited.contains(np) && (board[newRow][newCol] == Cell.EMPTY || board[newRow][newCol] == Cell.PLAYER)) {
                    visited.add(np);
                    queue.add(np);
                }
            }
        }
        return visited;
    }

    private boolean wouldCreateDeadEnd(int row, int col) {
        int emptyNeighbors = 0;
        for (Direction dir : Direction.DIRECTIONS) {
            int newRow = row + dir.row;
            int newCol = col + dir.col;
            if (isValidCell(newRow, newCol) && board[newRow][newCol] == Cell.EMPTY) {
                emptyNeighbors++;
            }
        }
        // If there's only one empty neighbor, this would create a dead end
        return emptyNeighbors <= 1;
    }

    private boolean wouldCreateNarrowPath(int row, int col) {
        // Check if adding a wall here would create a very narrow path
        int emptyNeighbors = 0;
        for (Direction dir : Direction.DIRECTIONS) {
            int newRow = row + dir.row;
            int newCol = col + dir.col;
            if (isValidCell(newRow, newCol) && board[newRow][newCol] == Cell.EMPTY) {
                emptyNeighbors++;
                // Check if this neighbor would become a dead end
                int neighborEmptyNeighbors = 0;
                for (Direction neighborDir : Direction.DIRECTIONS) {
                    int neighborRow = newRow + neighborDir.row;
                    int neighborCol = newCol + neighborDir.col;
                    if (isValidCell(neighborRow, neighborCol) && board[neighborRow][neighborCol] == Cell.EMPTY) {
                        neighborEmptyNeighbors++;
                    }
                }
                if (neighborEmptyNeighbors <= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPathAccessible(int startRow, int startCol) {
        boolean[][] visited = new boolean[rowCount][colCount];
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startRow, startCol));
        visited[startRow][startCol] = true;
        int reachableCount = 1;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Direction dir : Direction.DIRECTIONS) {
                int newRow = current.x + dir.row;
                int newCol = current.y + dir.col;

                if (isValidCell(newRow, newCol) && !visited[newRow][newCol] && board[newRow][newCol] == Cell.EMPTY) {
                    visited[newRow][newCol] = true;
                    queue.add(new Point(newRow, newCol));
                    reachableCount++;
                }
            }
        }

        // Check if we can reach at least 90% of the empty cells
        int totalEmpty = countEmptyCells();
        return reachableCount >= totalEmpty * 0.9;
    }
} 