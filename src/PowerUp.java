import java.io.Serializable;

public class PowerUp implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Object lock = new Object();

    public enum Type {
        SPEED_BOOST("Speed Boost", 5000),
        INVINCIBILITY("Invincibility", 6000),
        GHOST_FRIGHT("Ghost Fright", 4000),
        DOUBLE_POINTS("Double Points", 10000),
        SLOW_GHOSTS("Slow Ghosts", 6000),
        FREEZE_GHOSTS("Freeze Ghosts", 5000);

        private final String name;
        private final long duration;

        Type(String name, long duration) {
            this.name = name;
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public long getDuration() {
            return duration;
        }
    }

    private final Type type;
    private final int row;
    private final int col;
    private volatile boolean active = false;
    private volatile long activationTime = 0;
    private Thread durationThread;

    public PowerUp(Type type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public Type getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void activate() {
        synchronized(lock) {
            if (active) return;

            System.out.println("[DEBUG] PowerUp.activate() called for " + type);
            active = true;
            activationTime = System.currentTimeMillis();
            System.out.println("[DEBUG] After activation - Active: " + active + ", Expired: " + isExpired());

            // If the previous thread still working, finish it
            if (durationThread != null && durationThread.isAlive()) {
                durationThread.interrupt();
            }

            durationThread = new Thread(() -> {
                try {
                    Thread.sleep(type.getDuration());
                    synchronized(lock) {
                        active = false;
                        System.out.println("[DEBUG] PowerUp expired: " + type.getName());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[DEBUG] PowerUp thread interrupted: " + type.getName());
                }
            });
            durationThread.start();
        }
    }

    public boolean isActive() {
        synchronized(lock) {
            return active;
        }
    }

    public boolean isExpired() {
        synchronized(lock) {
            if (!active) {
                return false;
            }
            boolean expired = System.currentTimeMillis() - activationTime >= type.getDuration();
            if (expired) {
                active = false;
            }
            return expired;
        }
    }

    public void applyEffect(Player player) {
        synchronized(lock) {
            if (!isActive()) return;

            switch (type) {
                case SPEED_BOOST:
                    player.setSpeed(1.5);
                    break;
                case INVINCIBILITY:
                    player.setInvincible(true, type.getDuration());
                    break;
                case GHOST_FRIGHT:
                    break;
                case DOUBLE_POINTS:
                    break;
                case SLOW_GHOSTS:
                    break;
            }
        }
    }

    public void removeEffect(Player player) {
        synchronized(lock) {
            System.out.println("[DEBUG] PowerUp.removeEffect() called for " + type);
            active = false;

            switch (type) {
                case SPEED_BOOST:
                    player.setSpeed(1.0);
                    System.out.println("[DEBUG] Speed reset to normal");
                    break;
                case INVINCIBILITY:
                    player.setInvincible(false, 0);
                    System.out.println("[DEBUG] Invincibility removed");
                    break;
                case GHOST_FRIGHT:
                    System.out.println("[DEBUG] Ghost fright effect will be removed in GameWindow");
                    break;
                case DOUBLE_POINTS:
                    System.out.println("[DEBUG] Double points effect will be removed in GameWindow");
                    break;
                case SLOW_GHOSTS:
                    System.out.println("[DEBUG] Slow ghosts effect will be removed in GameWindow");
                    break;
            }

            // finishing thread
            if (durationThread != null && durationThread.isAlive()) {
                durationThread.interrupt();
            }
        }
    }

    @Override
    public String toString() {
        return type.getName();
    }
} 