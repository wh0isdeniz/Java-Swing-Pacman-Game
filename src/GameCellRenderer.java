import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class GameCellRenderer extends JLabel implements TableCellRenderer {
    private static final int DEFAULT_CELL_SIZE = 48;
    private int cellSize;
    private List<PowerUp> powerUps;
    private GameAnimations animations;
    private List<Enemy> enemies;

    // Static icons for non-animation elements
    private static final ImageIcon WALL_ICON = new ImageIcon(GameCellRenderer.class.getResource("/img/wall.png"));

    private static final ImageIcon FOOD_ICON = new ImageIcon(GameCellRenderer.class.getResource("/img/food.png"));


    // PowerUp icons
    private static final ImageIcon SPEED_BOOST_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/powerup_speed.png"));

    private static final ImageIcon INVINCIBILITY_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/powerup_invincibility.png"));

    private static final ImageIcon GHOST_FRIGHT_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/powerup_fright.png"));

    private static final ImageIcon DOUBLE_POINTS_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/powerup_double.png"));

    private static final ImageIcon SLOW_GHOSTS_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/powerup_slow.png"));

    private static final ImageIcon FREEZE_GHOSTS_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/powerup_freeze.png"));

    private static final ImageIcon GHOST_FROZEN_ICON =
            new ImageIcon(GameCellRenderer.class.getResource("/img/ghost_frozen.png"));


    public GameCellRenderer(int cellSize) {
        this.cellSize = cellSize;
        this.animations = GameAnimations.getInstance(cellSize);
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setPreferredSize(new Dimension(cellSize, cellSize));

        // Check static icons
        checkIconLoadStatus("WALL_ICON", WALL_ICON);
        checkIconLoadStatus("FOOD_ICON", FOOD_ICON);
        checkIconLoadStatus("SPEED_BOOST_ICON", SPEED_BOOST_ICON);
        checkIconLoadStatus("INVINCIBILITY_ICON", INVINCIBILITY_ICON);
        checkIconLoadStatus("GHOST_FRIGHT_ICON", GHOST_FRIGHT_ICON);
        checkIconLoadStatus("DOUBLE_POINTS_ICON", DOUBLE_POINTS_ICON);
        checkIconLoadStatus("SLOW_GHOSTS_ICON", SLOW_GHOSTS_ICON);
        checkIconLoadStatus("FREEZE_GHOSTS_ICON", FREEZE_GHOSTS_ICON);
        checkIconLoadStatus("GHOST_FROZEN_ICON", GHOST_FROZEN_ICON);
    }

    private void checkIconLoadStatus(String iconName, ImageIcon icon) {
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            System.out.println("[DEBUG] " + iconName + " yüklenemedi! Durum: " + icon.getImageLoadStatus());
            System.out.println("[DEBUG] " + iconName + " dosya yolu: " + icon.getDescription());
        } else {
            System.out.println("[DEBUG] " + iconName + " başarıyla yüklendi.");
        }
    }

    public void setPowerUpList(List<PowerUp> powerUps) {
        this.powerUps = powerUps;
    }

    public void setEnemyList(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    private ImageIcon resizeIcon(ImageIcon icon) {
        if (icon == null) return null;

        // Get the original size of the icon
        int originalWidth = icon.getIconWidth();
        int originalHeight = icon.getIconHeight();

        // Using 80% of cell replacement (to leave space on the edges)
        int targetSize = (int)(cellSize * 0.8);

        // Maintain aspect ratio
        int newWidth = targetSize;
        int newHeight = targetSize;

        if (originalWidth > originalHeight) {
            newHeight = (int)((float)targetSize * originalHeight / originalWidth);
        } else {
            newWidth = (int)((float)targetSize * originalWidth / originalHeight);
        }

        // Create new size icon
        Image scaledImage = icon.getImage().getScaledInstance(
                newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        setIcon(null);
        setText("");

        if (value instanceof GameBoardModel.Cell) {
            GameBoardModel.Cell cell = (GameBoardModel.Cell) value;

            switch (cell) {
                case WALL:
                    setIcon(resizeIcon(WALL_ICON));
                    setBackground(Color.BLACK);
                    break;
                case PLAYER:
                    // Use animated player sprite with direction
                    ImageIcon playerFrame = animations.getCurrentPlayerFrame(true); // true for moving animation
                    if (playerFrame != null) {
                        setIcon(playerFrame);
                    }
                    setBackground(Color.BLACK);
                    break;
                case ENEMY:
                    Enemy enemy = findEnemyAt(row, column);
                    System.out.println("[DEBUG] Renderer ENEMY at (" + row + "," + column + "): " + (enemy != null ? "found" : "not found"));
                    if (enemy != null) {
                        if (enemy.isFrozen()) {
                            setIcon(GHOST_FROZEN_ICON);
                        } else {
                            String direction = enemy.getCurrentDirection().toString().toLowerCase();
                            boolean isChasing = enemy.isChasingPlayer();
                            ImageIcon ghostFrame = animations.getCurrentGhostFrame(direction, true, isChasing);
                            System.out.println("[DEBUG] Ghost frame for direction " + direction + " (chasing=" + isChasing + "): " + (ghostFrame != null ? "OK" : "NULL"));
                            if (ghostFrame != null) {
                                setIcon(ghostFrame);
                            }
                        }
                    }
                    setBackground(Color.BLACK);
                    break;
                case FOOD:
                    setIcon(resizeIcon(FOOD_ICON));
                    setBackground(Color.BLACK);
                    break;
                case POWER_UP:
                    PowerUp powerUp = findPowerUpAt(row, column);
                    if (powerUp != null) {
                        ImageIcon icon = null;
                        switch (powerUp.getType()) {
                            case SPEED_BOOST:
                                icon = SPEED_BOOST_ICON;
                                break;
                            case INVINCIBILITY:
                                icon = INVINCIBILITY_ICON;
                                break;
                            case GHOST_FRIGHT:
                                icon = GHOST_FRIGHT_ICON;
                                break;
                            case DOUBLE_POINTS:
                                icon = DOUBLE_POINTS_ICON;
                                break;
                            case SLOW_GHOSTS:
                                icon = SLOW_GHOSTS_ICON;
                                break;
                            case FREEZE_GHOSTS:
                                icon = FREEZE_GHOSTS_ICON;
                                break;
                        }
                        if (icon != null) {
                            ImageIcon resizedIcon = resizeIcon(icon);
                            setIcon(resizedIcon);
                        }
                    } else {
                        if (table.getModel() instanceof GameBoardModel) {
                            ((GameBoardModel) table.getModel()).setValueAt(row, column, GameBoardModel.Cell.EMPTY);
                        }
                    }
                    setBackground(Color.BLACK);
                    break;
                default:
                    setBackground(Color.BLACK);
                    break;
            }
        }
        return this;
    }

    private PowerUp findPowerUpAt(int row, int col) {
        if (powerUps != null) {
            for (PowerUp powerUp : powerUps) {
                if (powerUp.getRow() == row && powerUp.getCol() == col) {
                    return powerUp;
                }
            }
        }
        return null;
    }

    private Enemy findEnemyAt(int row, int col) {
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.getRow() == row && enemy.getCol() == col) {
                    return enemy;
                }
            }
        }
        return null;
    }
}