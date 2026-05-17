import javax.swing.*;
import java.awt.*;

public class GameAnimations {
    private static GameAnimations instance;
    private final Animation animationInstance;
    private int cellSize;
    private String currentPlayerDirection = "right"; // Default direction
    private boolean isGhostFrightened = false;

    private GameAnimations(int cellSize) {
        this.cellSize = cellSize;
        this.animationInstance = Animation.getInstance(cellSize);
    }

    public static GameAnimations getInstance(int cellSize) {
        if (instance == null) {
            instance = new GameAnimations(cellSize);
        }
        return instance;
    }

    public void setPlayerDirection(String direction) {
        if (direction != null && !direction.equals(currentPlayerDirection)) {
            currentPlayerDirection = direction;
        }
    }

    public void setGhostFrightened(boolean frightened) {
        this.isGhostFrightened = frightened;
    }

    public ImageIcon getCurrentPlayerFrame(boolean isMoving) {
        String animationKey = "player_" + currentPlayerDirection;
        ImageIcon frame = animationInstance.getCurrentFrame(animationKey);
        if (frame != null) {
            return resizeImage(frame);
        }
        return null;
    }

    public ImageIcon getCurrentGhostFrame(String direction, boolean isMoving, boolean isChasing) {
        if (direction.equalsIgnoreCase("rıght")) {
            direction = "right";
        }

        if (isGhostFrightened) {
            return resizeImage(new ImageIcon(getClass().getResource("/img/ghost_fright1.png")));
        }

        String suffix = isChasing ? "_3.png" : "_1.png";
        return resizeImage(new ImageIcon(getClass().getResource("/img/ghost_" + direction + suffix)));
    }

    public ImageIcon getFrozenGhostFrame() {
        return new ImageIcon(getClass().getResource("/img/ghost_frozen.png"));
    }

    private ImageIcon resizeImage(ImageIcon originalIcon) {
        if (originalIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            System.out.println("[DEBUG] Failed to load image");
            return null;
        }

        int targetSize = (int)(cellSize * 0.8);
        Image scaledImage = originalIcon.getImage().getScaledInstance(
                targetSize, targetSize, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public void stopAllAnimations() {
        animationInstance.stop();
    }

    public void startAllAnimations() {
        // Animation class starts automatically
    }

    public static void resetInstance() {
        instance = null;
    }
}
