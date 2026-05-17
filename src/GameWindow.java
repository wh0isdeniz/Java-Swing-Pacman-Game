import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameWindow extends JFrame {
    private final Object gameStateLock = new Object();
    private final GameBoardModel gameModel;
    private final JTable gameBoard;
    private JLabel scoreLabel;
    private JLabel timeLabel;
    private JLabel livesLabel;
    private volatile int score = 0;
    private volatile int lives = 3;
    private volatile long startTime;
    private Thread gameThread;
    private volatile boolean isRunning = true;
    private int cellSize;

    private Player player;
    private List<Enemy> enemies;
    private List<PowerUp> powerUps;
    private Random random;
    private volatile boolean doublePoints = false;
    private volatile long lastPowerUpCheck = 0;
    private static final long POWER_UP_CHECK_INTERVAL = 5000; // 5 seconds
    private static final ImageIcon PLAYER_ICON =
            new ImageIcon(GameWindow.class.getResource("/img/player1.png"));
    private int enemyMoveCounter = 0;
    private volatile boolean ghostsFrightened = false;
    private volatile boolean ghostsSlowed = false;
    private volatile long ghostsFrozenUntil = 0;
    private JPanel gamePanel;

    public GameWindow(int rowCount, int colCount) {
        gameModel = new GameBoardModel(rowCount, colCount);
        random = new Random();

        setTitle("Pacman Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Calculate cell size based on screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 0.95);  // 95% of screen width
        int maxHeight = (int) (screenSize.height * 0.85); // 85% of screen height (leaving space for status panel)

        // Calculate cell size that fits the screen
        cellSize = Math.min(
                maxWidth / colCount,
                maxHeight / rowCount
        );

        // Create game board
        gameBoard = new JTable(gameModel);
        setupGameBoard(cellSize);

        gamePanel = new JPanel(new GridBagLayout());
        gamePanel.add(gameBoard);

        // Create status panel
        JPanel statusPanel = createStatusPanel();

        // Layout
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Initialize game objects
        initializeGameObjects();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Start game thread
        startGameThread();

        // Set frame size based on board dimensions
        int boardWidthPx = colCount * cellSize;
        int boardHeightPx = rowCount * cellSize;
        int statusPanelHeight = 80;
        int totalHeight = boardHeightPx + statusPanelHeight;
        int totalWidth = boardWidthPx;

        setPreferredSize(new Dimension(totalWidth, totalHeight));
        setMinimumSize(new Dimension(400, 400));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        if (PLAYER_ICON.getImageLoadStatus() != MediaTracker.COMPLETE) {
            System.out.println("PLAYER_ICON yüklenemedi!");
        }

        // resize grid when window resizes
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeGameBoard();
            }
        });

        gamePanel.setBackground(Color.BLACK);
        getContentPane().setBackground(Color.BLACK);
    }

    private void setupGameBoard(int cellSize) {
        // Set cell size
        gameBoard.setRowHeight(cellSize);
        for (int i = 0; i < gameModel.getColumnCount(); i++) {
            TableColumn column = gameBoard.getColumnModel().getColumn(i);
            column.setPreferredWidth(cellSize);
            column.setMaxWidth(cellSize);
        }

        // Set custom renderer
        GameCellRenderer renderer = new GameCellRenderer(cellSize);
        renderer.setPowerUpList(powerUps);
        renderer.setEnemyList(enemies);
        gameBoard.setDefaultRenderer(Object.class, renderer);

        gameBoard.getTableHeader().setReorderingAllowed(false);
        gameBoard.getTableHeader().setResizingAllowed(false);

        gameBoard.setCellSelectionEnabled(false);
        gameBoard.setRowSelectionAllowed(false);
        gameBoard.setColumnSelectionAllowed(false);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));

        ImageIcon scoreIcon = new ImageIcon(getClass().getResource("/img/coin.png"));
        ImageIcon timeIcon = new ImageIcon(getClass().getResource("/img/clock.png"));
        ImageIcon livesIcon = new ImageIcon(getClass().getResource("/img/heart.png"));

        // Score
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 22));
        scoreLabel.setForeground(new Color(255, 215, 0));
        scoreLabel.setIcon(scoreIcon);
        scoreLabel.setIconTextGap(10);
        panel.add(scoreLabel);
        panel.add(Box.createHorizontalStrut(30));

        // Time
        timeLabel = new JLabel("Time: 0");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        timeLabel.setForeground(new Color(135, 206, 250));
        timeLabel.setIcon(timeIcon);
        timeLabel.setIconTextGap(10);
        panel.add(timeLabel);
        panel.add(Box.createHorizontalStrut(30));

        // Lives
        livesLabel = new JLabel("Lives: 3");
        livesLabel.setFont(new Font("Arial", Font.BOLD, 22));
        livesLabel.setForeground(new Color(255, 99, 71));
        livesLabel.setIcon(livesIcon);
        livesLabel.setIconTextGap(10);
        panel.add(livesLabel);

        return panel;
    }

    private void initializeGameObjects() {
        // Find player starting position
        int playerRow = -1, playerCol = -1;
        for (int i = 0; i < gameModel.getRowCount(); i++) {
            for (int j = 0; j < gameModel.getColumnCount(); j++) {
                if (gameModel.getCell(i, j) == GameBoardModel.Cell.PLAYER) {
                    playerRow = i;
                    playerCol = j;
                    break;
                }
            }
            if (playerRow != -1) break;
        }

        player = new Player(playerRow, playerCol);
        player.setCellSize(cellSize);
        gameBoard.addKeyListener(player);

        // Initialize enemies
        enemies = new ArrayList<>();
        for (int i = 0; i < gameModel.getRowCount(); i++) {
            for (int j = 0; j < gameModel.getColumnCount(); j++) {
                if (gameModel.getCell(i, j) == GameBoardModel.Cell.ENEMY) {
                    enemies.add(new Enemy(i, j));
                }
            }
        }

        // Initialize power-ups
        powerUps = new ArrayList<>();
    }

    private void setupKeyboardShortcuts() {
        KeyStroke ctrlShiftQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        getRootPane().registerKeyboardAction(e -> {
            isRunning = false;
            dispose();
            new MainMenu().setVisible(true);
        }, ctrlShiftQ, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void startGameThread() {
        startTime = System.currentTimeMillis();

        gameThread = new Thread(() -> {
            while (isRunning) {
                try {
                    synchronized(gameStateLock) {
                        updateGame();
                    }

                    SwingUtilities.invokeLater(this::updateUI);

                    // Sleep for game speed
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        gameThread.start();
    }

    private void updateGame() {
        player.move(gameModel);

        checkCollisions();

        updatePlayerPosition();

        enemyMoveCounter++;
        if (enemyMoveCounter >= 3) {
            for (Enemy enemy : enemies) {
                enemy.updateFrozenState();
                if (!enemy.isFrozen()) {
                    int oldRow = enemy.getRow();
                    int oldCol = enemy.getCol();
                    enemy.move(gameModel, player);
                    updateEnemyPosition(enemy, oldRow, oldCol);
                }
            }
            enemyMoveCounter = 0;
        }

        // Check for power-ups
        checkPowerUps();

        // Update renderer
        GameCellRenderer renderer = (GameCellRenderer) gameBoard.getDefaultRenderer(Object.class);
        renderer.setPowerUpList(powerUps);
        renderer.setEnemyList(enemies);

        // Check for game over
        checkGameOver();
        SwingUtilities.invokeLater(() -> gameBoard.repaint());
    }

    private void updatePlayerPosition() {
        // Clear old position
        for (int i = 0; i < gameModel.getRowCount(); i++) {
            for (int j = 0; j < gameModel.getColumnCount(); j++) {
                if (gameModel.getCell(i, j) == GameBoardModel.Cell.PLAYER) {
                    gameModel.setValueAt(i, j, GameBoardModel.Cell.EMPTY);
                }
            }
        }

        // Set new position
        gameModel.setValueAt(player.getRow(), player.getCol(), GameBoardModel.Cell.PLAYER);

        // Update renderer
        GameCellRenderer renderer = (GameCellRenderer) gameBoard.getDefaultRenderer(Object.class);
        renderer.setPowerUpList(powerUps);
    }

    private void updateEnemyPosition(Enemy enemy, int oldRow, int oldCol) {
        // When leaving the old position, check if there is a PowerUp there
        boolean hadPowerUp = false;
        for (PowerUp pu : powerUps) {
            if (pu.getRow() == oldRow && pu.getCol() == oldCol) {
                hadPowerUp = true;
                break;
            }
        }
        if (hadPowerUp) {
            gameModel.setValueAt(oldRow, oldCol, GameBoardModel.Cell.POWER_UP);
            System.out.println("[DEBUG] Enemy left a PowerUp at: " + oldRow + "," + oldCol);
        } else {
            gameModel.setValueAt(oldRow, oldCol, GameBoardModel.Cell.EMPTY);
            System.out.println("[DEBUG] Cleared enemy's old position at: " + oldRow + "," + oldCol);
        }


        boolean hasPowerUpHere = false;
        for (PowerUp pu : powerUps) {
            if (pu.getRow() == enemy.getRow() && pu.getCol() == enemy.getCol()) {
                hasPowerUpHere = true;
                break;
            }
        }
        gameModel.setValueAt(enemy.getRow(), enemy.getCol(), GameBoardModel.Cell.ENEMY);
        if (hasPowerUpHere) {
            System.out.println("[DEBUG] Enemy is on a PowerUp at: " + enemy.getRow() + "," + enemy.getCol());
        } else {
            System.out.println("[DEBUG] Moved enemy to: " + enemy.getRow() + "," + enemy.getCol());
        }

        GameCellRenderer renderer = (GameCellRenderer) gameBoard.getDefaultRenderer(Object.class);
        renderer.setPowerUpList(powerUps);
        renderer.setEnemyList(enemies);
    }

    private void checkCollisions() {
        // Check enemy collisions
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == player.getRow() && enemy.getCol() == player.getCol()) {
                if (player.isInvincible()) {
                    // Player is invincible, enemy should be respawned
                    System.out.println("[DEBUG] Player is invincible, enemy respawned");
                    respawnEnemy(enemy);
                    updateScore(200); // Bonus points for hitting enemy while invincible
                } else {
                    // Player is not invincible, lose a life
                    System.out.println("[DEBUG] Player hit by enemy, losing a life");
                    updateLives(-1);
                    respawnPlayer();
                    checkGameOver();
                }
                break;
            }
        }

        // Checking power-up collisions
        PowerUp powerUp = findPowerUpAt(player.getRow(), player.getCol());
        if (powerUp != null) {
            System.out.println("[DEBUG] PowerUp collected: " + powerUp.getType());
            powerUp.activate();
            powerUp.applyEffect(player);

            // Handleing special power-up effects
            switch (powerUp.getType()) {
                case FREEZE_GHOSTS:
                    ghostsFrozenUntil = System.currentTimeMillis() + powerUp.getType().getDuration();
                    for (Enemy enemy : enemies) {
                        enemy.freeze(powerUp.getType().getDuration());
                    }
                    System.out.println("[DEBUG] Ghosts frozen for " + powerUp.getType().getDuration() + "ms");
                    break;
                case GHOST_FRIGHT:
                    ghostsFrightened = true;
                    for (Enemy enemy : enemies) {
                        enemy.setFrightened(true, powerUp.getType().getDuration());
                    }
                    GameAnimations.getInstance(cellSize).setGhostFrightened(true);
                    System.out.println("[DEBUG] Ghost fright activated!");
                    break;
                case SLOW_GHOSTS:
                    ghostsSlowed = true;
                    for (Enemy enemy : enemies) {
                        enemy.setSlowed(true, powerUp.getType().getDuration());
                        enemy.setSpeed(0.5);
                    }
                    System.out.println("[DEBUG] Ghosts slowed down!");
                    break;
                case DOUBLE_POINTS:
                    doublePoints = true;
                    System.out.println("[DEBUG] Double points activated!");
                    break;
            }

            powerUps.remove(powerUp);
            updateScore(100);
        }

        // Check food collisions
        if (gameModel.getCell(player.getRow(), player.getCol()) == GameBoardModel.Cell.FOOD) {
            gameModel.setValueAt(player.getRow(), player.getCol(), GameBoardModel.Cell.EMPTY);
            gameModel.decrementFoodCount();
            updateScore(doublePoints ? 20 : 10);

            // Check if all food is collected
            if (gameModel.getRemainingFoodCount() == 0) {
                victory();
            }
        }
    }

    private void checkPowerUps() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpCheck >= POWER_UP_CHECK_INTERVAL) {
            lastPowerUpCheck = currentTime;

            // Debug: Double Points checking
            System.out.println("[DEBUG] Double Points Check - Current state: " + doublePoints);
            System.out.println("[DEBUG] Active PowerUps: " + powerUps.size());

            boolean anyDoublePointsActive = false;
            for (PowerUp pu : powerUps) {
                if (pu.getType() == PowerUp.Type.DOUBLE_POINTS && pu.isActive()) {
                    System.out.println("[DEBUG] Found DOUBLE_POINTS PowerUp - Active: " + pu.isActive() +
                            ", Expired: " + pu.isExpired() +
                            ", Duration: " + pu.getType().getDuration());
                    if (!pu.isExpired()) {
                        anyDoublePointsActive = true;
                    }
                }
            }

            // If there is no Double Points PowerUp active and doublePoints is true, set it to false
            if (!anyDoublePointsActive && doublePoints) {
                System.out.println("[DEBUG] No active DOUBLE_POINTS PowerUp found, setting doublePoints to false");
                doublePoints = false;
            }

            // 25% chance for each enemy to create a power-up
            for (Enemy enemy : enemies) {
                if (random.nextDouble() < 0.25) {
                    Point behindCell = getCellBehindEnemy(enemy);
                    if (behindCell != null) {
                        if (gameModel.getCell(behindCell.x, behindCell.y) == GameBoardModel.Cell.EMPTY) {
                            System.out.println("[DEBUG] Creating PowerUp at: " + behindCell.x + "," + behindCell.y);
                            createPowerUp(behindCell.x, behindCell.y);

                            if (gameModel.getCell(behindCell.x, behindCell.y) != GameBoardModel.Cell.POWER_UP) {
                                System.out.println("[DEBUG] WARNING: PowerUp creation failed at " + behindCell.x + "," + behindCell.y);
                            } else {
                                System.out.println("[DEBUG] PowerUp successfully created at " + behindCell.x + "," + behindCell.y);
                            }
                        } else {
                            System.out.println("[DEBUG] Cell not empty at: " + behindCell.x + "," + behindCell.y +
                                    ", type: " + gameModel.getCell(behindCell.x, behindCell.y));
                        }
                    } else {
                        System.out.println("[DEBUG] No valid cell behind enemy at: " + enemy.getRow() + "," + enemy.getCol());
                    }
                }
            }
        }

        // Check for expired power-ups
        List<PowerUp> expiredPowerUps = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive() && powerUp.isExpired()) {
                System.out.println("[DEBUG] PowerUp expired: " + powerUp.getType());
                System.out.println("[DEBUG] Before removeEffect - Double Points: " + doublePoints);
                powerUp.removeEffect(player);
                System.out.println("[DEBUG] After removeEffect - Double Points: " + doublePoints);
                expiredPowerUps.add(powerUp);
                gameModel.setValueAt(powerUp.getRow(), powerUp.getCol(), GameBoardModel.Cell.EMPTY);
                System.out.println("[DEBUG] Removed PowerUp at: " + powerUp.getRow() + "," + powerUp.getCol());

                // Check if this was a double points power-up
                if (powerUp.getType() == PowerUp.Type.DOUBLE_POINTS) {
                    System.out.println("[DEBUG] DOUBLE_POINTS PowerUp expired!");
                    System.out.println("[DEBUG] Before setting doublePoints = false");
                    doublePoints = false;
                    System.out.println("[DEBUG] After setting doublePoints = false");
                }
            }
        }

        // Remove expired power-ups from list
        powerUps.removeAll(expiredPowerUps);

        boolean anyGhostFrightActive = false;
        boolean anySlowGhostsActive = false;
        for (PowerUp pu : powerUps) {
            if (pu.isActive()) {
                if (pu.getType() == PowerUp.Type.GHOST_FRIGHT) {
                    anyGhostFrightActive = true;
                } else if (pu.getType() == PowerUp.Type.SLOW_GHOSTS) {
                    anySlowGhostsActive = true;
                }
            }
        }
        if (!anyGhostFrightActive && ghostsFrightened) {
            ghostsFrightened = false;
            GameAnimations.getInstance(cellSize).setGhostFrightened(false);
            System.out.println("[DEBUG] All Ghost Fright effects ended!");
        }
        if (!anySlowGhostsActive && ghostsSlowed) {
            ghostsSlowed = false;
            for (Enemy enemy : enemies) {
                enemy.setSlowed(false, 0);
                enemy.setSpeed(1.0);
            }
            System.out.println("[DEBUG] All Slow Ghosts effects ended!");
        }

        // Update renderer
        GameCellRenderer renderer = (GameCellRenderer) gameBoard.getDefaultRenderer(Object.class);
        renderer.setPowerUpList(powerUps);

        // For the Debug: List current power-ups and their effects 
        System.out.println("[DEBUG] Current PowerUps: " + powerUps.size());
        System.out.println("[DEBUG] Active effects: " +
                "Speed Boost: " + (player.getSpeed() > 1.0) + ", " +
                "Invincibility: " + player.isInvincible() + ", " +
                "Ghost Fright: " + ghostsFrightened + ", " +
                "Double Points: " + doublePoints + ", " +
                "Slow Ghosts: " + ghostsSlowed);
    }

    private Point getCellBehindEnemy(Enemy enemy) {
        // Enemy's current direction
        Enemy.Direction direction = enemy.getCurrentDirection();
        int row = enemy.getRow();
        int col = enemy.getCol();

        // Calculate the coordinates of the cell behind the enemy
        int behindRow = row;
        int behindCol = col;

        switch (direction) {
            case UP:
                behindRow = row + 1; // The cell below the enemy
                break;
            case DOWN:
                behindRow = row - 1; // The cell above the enemy
                break;
            case LEFT:
                behindCol = col + 1; // The cell to the right of the enemy
                break;
            case RIGHT:
                behindCol = col - 1; // The cell to the left of the enemy
                break;
        }

        // Check if the cell is within the bounds and if it's empty
        if (behindRow >= 0 && behindRow < gameModel.getRowCount() &&
                behindCol >= 0 && behindCol < gameModel.getColumnCount() &&
                gameModel.getCell(behindRow, behindCol) == GameBoardModel.Cell.EMPTY) {
            System.out.println("[DEBUG] Found empty cell behind enemy at: " + behindRow + "," + behindCol);
            return new Point(behindRow, behindCol);
        }

        System.out.println("[DEBUG] No valid cell behind enemy at: " + row + "," + col +
                " (direction: " + direction + ", behind: " + behindRow + "," + behindCol + ")");
        return null;
    }

    private void createPowerUp(int row, int col) {
        // First check if the cell is empty
        if (gameModel.getCell(row, col) != GameBoardModel.Cell.EMPTY) {
            System.out.println("[DEBUG] Cannot create PowerUp at " + row + "," + col +
                    " - cell is not empty (type: " + gameModel.getCell(row, col) + ")");
            return;
        }

        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type randomType = types[random.nextInt(types.length)];
        PowerUp powerUp = new PowerUp(randomType, row, col);

        // First add the PowerUp to the list
        powerUps.add(powerUp);
        System.out.println("[DEBUG] Added PowerUp to list. Current size: " + powerUps.size());

        // Then mark the cell as POWER_UP
        gameModel.setValueAt(row, col, GameBoardModel.Cell.POWER_UP);
        System.out.println("[DEBUG] Marked cell as POWER_UP at: " + row + "," + col);

        GameCellRenderer renderer = (GameCellRenderer) gameBoard.getDefaultRenderer(Object.class);
        renderer.setPowerUpList(powerUps);

        System.out.println("[DEBUG] Created " + randomType.getName() + " PowerUp at: " + row + "," + col);
        System.out.println("[DEBUG] PowerUp list size: " + powerUps.size());
        System.out.println("[DEBUG] Cell type at " + row + "," + col + ": " + gameModel.getCell(row, col));

        if (gameModel.getCell(row, col) != GameBoardModel.Cell.POWER_UP) {
            System.out.println("[DEBUG] WARNING: Cell not marked as POWER_UP! Retrying...");
            gameModel.setValueAt(row, col, GameBoardModel.Cell.POWER_UP);
            System.out.println("[DEBUG] Cell type after retry: " + gameModel.getCell(row, col));

            // If still unsuccessful, remove the PowerUp from the list
            if (gameModel.getCell(row, col) != GameBoardModel.Cell.POWER_UP) {
                System.out.println("[DEBUG] ERROR: Failed to mark cell as POWER_UP after retry. Removing PowerUp from list.");
                powerUps.remove(powerUp);
            }
        }
    }

    private PowerUp findPowerUpAt(int row, int col) {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.getRow() == row && powerUp.getCol() == col) {
                return powerUp;
            }
        }
        return null;
    }

    private void respawnEnemy(Enemy enemy) {
        int row, col;
        do {
            row = random.nextInt(gameModel.getRowCount() - 2) + 1;
            col = random.nextInt(gameModel.getColumnCount() - 2) + 1;
        } while (gameModel.getCell(row, col) != GameBoardModel.Cell.EMPTY);

        enemy.setPosition(row, col);
        gameModel.setValueAt(row, col, GameBoardModel.Cell.ENEMY);
    }

    private void respawnPlayer() {
        int centerRow = gameModel.getRowCountValue() / 2;
        int centerCol = gameModel.getColCountValue() / 2;
        player.setPosition(centerRow, centerCol);
        gameModel.setValueAt(centerRow, centerCol, GameBoardModel.Cell.PLAYER);
    }

    private void checkGameOver() {
        if (lives <= 0) {
            isRunning = false;
            gameOver();
        }
    }

    private void gameOver() {
        HighScore highScore = new HighScore();

        SwingUtilities.invokeLater(() -> {
            String playerName = JOptionPane.showInputDialog(
                    this,
                    "Game Over! Enter your name:",
                    "Game Over",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (playerName != null && !playerName.trim().isEmpty()) {
                highScore.addScore(playerName, score);
            }

            dispose(); // to make it safer :)
            new MainMenu().setVisible(true);
        });
    }


    private void victory() {
        System.out.println("[DEBUG] Victory method called!");
        isRunning = false;
        HighScore highScore = new HighScore();
        if (highScore.isHighScore(score)) {
            String playerName = JOptionPane.showInputDialog(
                    this,
                    "Victory! New High Score! Enter your name:",
                    "Game Won",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (playerName != null && !playerName.trim().isEmpty()) {
                highScore.addScore(playerName, score);
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Victory! You collected all the food!\nFinal Score: " + score,
                    "Game Won",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        dispose();
        new MainMenu().setVisible(true);
    }

    private void updateUI() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - startTime) / 1000;
        timeLabel.setText("Time: " + elapsedTime);

        scoreLabel.setText("Score: " + score);

        // Update lives
        livesLabel.setText("Lives: " + lives);
    }

    private void updateScore(int points) {
        synchronized(gameStateLock) {
            score += points;
            SwingUtilities.invokeLater(() -> scoreLabel.setText("Score: " + score));
        }
    }

    private void updateLives(int change) {
        synchronized(gameStateLock) {
            lives += change;
            if (lives <= 0) {
                isRunning = false;
                gameOver();
            }
            SwingUtilities.invokeLater(() -> livesLabel.setText("Lives: " + lives));
        }
    }

    @Override
    public void dispose() {
        isRunning = false;
        if (gameThread != null) {
            gameThread.interrupt();
            try {
                gameThread.join(1000); // Wait for 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Stop all animations when window is closed
        GameAnimations.getInstance(cellSize).stopAllAnimations();
        super.dispose();
    }

    private void resizeGameBoard() {
        int width = gamePanel.getWidth();
        int height = gamePanel.getHeight();
        int newCellSize = Math.min(width / gameModel.getColumnCount(), height / gameModel.getRowCount());
        cellSize = newCellSize;
        gameBoard.setRowHeight(cellSize);
        for (int i = 0; i < gameModel.getColumnCount(); i++) {
            TableColumn column = gameBoard.getColumnModel().getColumn(i);
            column.setPreferredWidth(cellSize);
            column.setMaxWidth(cellSize);
        }
        gameBoard.repaint();
    }
} 