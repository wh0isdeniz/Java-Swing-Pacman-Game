import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MainMenu extends JFrame {
    private JButton newGameButton;
    private JButton highScoresButton;
    private JButton exitButton;
    private Image backgroundImage;
    private Image loadingBackgroundImage;
    private final HighScore highScore;
    private JLabel titleLabel;

    public MainMenu() {
        highScore = new HighScore();

        setTitle("Pacman Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Upload background image
        loadBackgroundImage();

        // Loading background image
        try {
            loadingBackgroundImage = ImageIO.read(getClass().getResource("/img/loading_bg.png"));
        } catch (IOException e) {
            loadingBackgroundImage = null;
        }

        // Initialize components
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(getClass().getResource("img/background.png"));
        } catch (IOException e) {
            System.out.println("Background image not loaded :(: " + e.getMessage());
            backgroundImage = null;
        }
    }

    private void initializeComponents() {
        // Title label
        titleLabel = new JLabel("PACMAN GAME");
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.DARK_GRAY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Buttons
        newGameButton = new JButton("New Game");
        highScoresButton = new JButton("High Scores");
        exitButton = new JButton("Exit");

        // Set button styles
        styleButton(newGameButton, new Color(86, 86, 38));
        styleButton(highScoresButton, new Color(30, 144, 255));
        styleButton(exitButton, new Color(220, 20, 60));
    }


    private ImageIcon loadImageIcon(String path, int width, int height) {
        try {
            Image img = ImageIO.read(getClass().getResource(path));
            return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            System.out.println("Icon failed to load: " + path);
            return null;
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(250, 60));
        button.setMaximumSize(new Dimension(250, 60));
    }

    private void setupLayout() {
        // Main panel (for background)
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // If background image is not loaded, use a black background
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 100, 0));

        // Align buttons
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScoresButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add buttons to the panel
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(highScoresButton);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());

        // Add to main panel
        backgroundPanel.add(titlePanel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add to main window
        setContentPane(backgroundPanel);
    }

    private void setupListeners() {
        setupButtonHoverEffects(newGameButton, new Color(255, 215, 0), new Color(255, 235, 100));
        setupButtonHoverEffects(highScoresButton, new Color(30, 144, 255), new Color(100, 180, 255));
        setupButtonHoverEffects(exitButton, new Color(220, 20, 60), new Color(255, 70, 100));

        newGameButton.addActionListener(e -> startNewGame());
        highScoresButton.addActionListener(e -> showHighScores());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void setupButtonHoverEffects(JButton button, Color defaultColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultColor);
            }
        });
    }

    private void startNewGame() {
        GameAnimations.resetInstance();
        Animation.resetInstance();
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField rowField = new JTextField();
        JTextField colField = new JTextField();
        inputPanel.add(new JLabel("Row count (10-100):"));
        inputPanel.add(rowField);
        inputPanel.add(new JLabel("Column count (10-100):"));
        inputPanel.add(colField);

        int result = JOptionPane.showConfirmDialog(
                this,
                inputPanel,
                "New Game",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int rowCount = Integer.parseInt(rowField.getText().trim());
                int colCount = Integer.parseInt(colField.getText().trim());
                if (rowCount >= 10 && rowCount <= 100 && colCount >= 10 && colCount <= 100) {
                    // Create loading panel
                    JPanel loadingPanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            if (loadingBackgroundImage != null) {
                                g.drawImage(loadingBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                            } else {
                                g.setColor(Color.BLACK);
                                g.fillRect(0, 0, getWidth(), getHeight());
                            }
                        }
                    };
                    loadingPanel.setOpaque(true);
                    loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));

                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(false);
                    progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
                    progressBar.setPreferredSize(new Dimension(400, 30));
                    progressBar.setMaximumSize(new Dimension(400, 30));
                    progressBar.setBackground(Color.BLACK);
                    progressBar.setForeground(new Color(255, 215, 0)); // Sarı

                    progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                        @Override
                        protected Color getSelectionBackground() { return Color.BLACK; }
                        @Override
                        protected Color getSelectionForeground() { return new Color(255, 215, 0); }
                        @Override
                        protected void paintDeterminate(Graphics g, JComponent c) {
                            Insets b = progressBar.getInsets();
                            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
                            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
                            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
                            g.setColor(progressBar.getBackground());
                            g.fillRect(b.left, b.top, barRectWidth, barRectHeight);
                            g.setColor(progressBar.getForeground());
                            g.fillRect(b.left, b.top, amountFull, barRectHeight);
                        }
                    });

                    loadingPanel.add(Box.createVerticalGlue());
                    loadingPanel.add(progressBar);
                    loadingPanel.add(Box.createVerticalGlue());

                    setGlassPane(loadingPanel);
                    loadingPanel.setVisible(true);

                    // For a slower progress bar:
                    SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            for (int i = 0; i <= 100; i += 2) {
                                publish(i);
                                Thread.sleep(30); // Wait for 0.03 seconds (total ~3 seconds)
                            }
                            return null;
                        }
                        @Override
                        protected void process(java.util.List<Integer> chunks) {
                            progressBar.setValue(chunks.get(chunks.size() - 1));
                        }
                        @Override
                        protected void done() {
                            loadingPanel.setVisible(false);
                            GameWindow gameWindow = new GameWindow(rowCount, colCount);
                            setVisible(false);
                            gameWindow.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    setVisible(true);
                                }
                            });
                        }
                    };
                    worker.execute();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter numbers between 10 and 100 for both row and column!",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter valid numbers for row and column!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void showHighScores() {
        highScore.showHighScores();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}