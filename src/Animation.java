import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Animation {
    private static Animation instance;
    private final Object lock = new Object();
    private Thread animationThread;
    private volatile boolean isRunning = true;
    private final Map<String, ImageIcon[]> animations;
    private final Map<String, Integer> currentFrames;
    private int cellSize;

    private Animation(int cellSize) {
        this.cellSize = cellSize;
        this.animations = new HashMap<>();
        this.currentFrames = new HashMap<>();
        loadAnimations();
        startAnimationThread();
    }

    public static synchronized Animation getInstance(int cellSize) {
        if (instance == null) {
            instance = new Animation(cellSize);
        }
        return instance;
    }

    private void loadAnimations() {
        // Loading player animations using getResource, so I can easily open the project without working with folders in different computer
        animations.put("player_right", new ImageIcon[]{
                new ImageIcon(getClass().getResource("/img/player_right_closed.png")),
                new ImageIcon(getClass().getResource("/img/player_right_open.png"))
        });
        animations.put("player_left", new ImageIcon[]{
                new ImageIcon(getClass().getResource("/img/player_left_closed.png")),
                new ImageIcon(getClass().getResource("/img/player_left_open.png"))
        });
        animations.put("player_up", new ImageIcon[]{
                new ImageIcon(getClass().getResource("/img/player_up_closed.png")),
                new ImageIcon(getClass().getResource("/img/player_up_open.png"))
        });
        animations.put("player_down", new ImageIcon[]{
                new ImageIcon(getClass().getResource("/img/player_down_closed.png")),
                new ImageIcon(getClass().getResource("/img/player_down_open.png"))
        });

        // Initialize current frames
        for (String key : animations.keySet()) {
            currentFrames.put(key, 0);
        }
    }

    private void startAnimationThread() {
        animationThread = new Thread(() -> {
            while (isRunning) {
                try {
                    synchronized(lock) {
                        for (String key : currentFrames.keySet()) {
                            int currentFrame = currentFrames.get(key);
                            ImageIcon[] frames = animations.get(key);
                            currentFrames.put(key, (currentFrame + 1) % frames.length);
                        }
                    }
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        animationThread.start();
    }

    public ImageIcon getCurrentFrame(String animationKey) {
        synchronized(lock) {
            if (!animations.containsKey(animationKey)) {
                return null;
            }
            int currentFrame = currentFrames.get(animationKey);
            return animations.get(animationKey)[currentFrame];
        }
    }

    public void setCellSize(int cellSize) {
        synchronized(lock) {
            this.cellSize = cellSize;
        }
    }

    public void stop() {
        isRunning = false;
        if (animationThread != null) {
            animationThread.interrupt();
            try {
                animationThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void resetInstance() {
        instance = null;
    }
}
