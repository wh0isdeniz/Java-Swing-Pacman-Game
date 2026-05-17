import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HighScore implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SCORES_FILE = "highscores.dat";
    private static final int MAX_SCORES = 15;

    private static class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
        private static final long serialVersionUID = 1L;
        private final String playerName;
        private final int score;
        private final long date;

        public ScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
            this.date = System.currentTimeMillis();
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // Descending order
        }

        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(new Date(date));
        }

        @Override
        public String toString() {
            return String.format("%s - %d points - %s", playerName, score, getFormattedDate());
        }
    }

    private List<ScoreEntry> scores;

    public HighScore() {
        scores = new ArrayList<>();
        loadScores();
    }

    public void addScore(String playerName, int score) {
        scores.add(new ScoreEntry(playerName, score));
        Collections.sort(scores);

        // Keep only top MAX_SCORES
        if (scores.size() > MAX_SCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
        }

        saveScores();
    }

    public void showHighScores() {
        loadScores();
        JFrame frame = new JFrame("High Scores");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 0, 102));
        JLabel titleLabel = new JLabel("HIGH SCORES");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        headerPanel.add(titleLabel);

        // Score model and custom renderer
        DefaultListModel<ScoreEntry> listModel = new DefaultListModel<>();
        for (ScoreEntry entry : scores) {
            listModel.addElement(entry);
        }

        JList<ScoreEntry> scoreList = new JList<>(listModel);
        scoreList.setCellRenderer(new ScoreCellRenderer());
        scoreList.setFont(new Font("Arial", Font.PLAIN, 16));
        scoreList.setBackground(Color.BLACK);
        scoreList.setForeground(Color.WHITE);
        scoreList.setFixedCellHeight(50);

        JScrollPane scrollPane = new JScrollPane(scoreList);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.setBackground(Color.BLACK);

        // If there are no scores, show an information message
        if (scores.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.BLACK);
            emptyPanel.setLayout(new BorderLayout());

            JLabel emptyLabel = new JLabel("No High Scores Yet!");
            emptyLabel.setForeground(Color.YELLOW);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 20));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            frame.add(emptyPanel, BorderLayout.CENTER);
        } else {
            frame.add(scrollPane, BorderLayout.CENTER);
        }

        // Bottom panel (for the close button)
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(0, 0, 102));
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 40));
        closeButton.setBackground(new Color(255, 215, 0));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> frame.dispose());
        footerPanel.add(closeButton);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(footerPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // Custom cell renderer
    private class ScoreCellRenderer extends JPanel implements ListCellRenderer<ScoreEntry> {
        private JLabel rankLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel scoreLabel = new JLabel();
        private JLabel dateLabel = new JLabel();

        public ScoreCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 0));
            infoPanel.setOpaque(false);

            rankLabel.setFont(new Font("Arial", Font.BOLD, 16));
            rankLabel.setForeground(Color.YELLOW);
            rankLabel.setPreferredSize(new Dimension(30, 40));

            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setForeground(Color.WHITE);

            scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
            scoreLabel.setForeground(new Color(255, 215, 0));
            scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            dateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            dateLabel.setForeground(Color.LIGHT_GRAY);
            dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel nameScorePanel = new JPanel(new BorderLayout());
            nameScorePanel.setOpaque(false);
            nameScorePanel.add(nameLabel, BorderLayout.WEST);
            nameScorePanel.add(scoreLabel, BorderLayout.EAST);

            infoPanel.add(nameScorePanel);
            infoPanel.add(dateLabel);

            add(rankLabel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ScoreEntry> list,
                                                      ScoreEntry entry, int index, boolean isSelected, boolean cellHasFocus) {

            rankLabel.setText((index + 1) + ".");
            nameLabel.setText(entry.playerName);
            scoreLabel.setText(entry.score + " points");
            dateLabel.setText(entry.getFormattedDate());

            if (isSelected) {
                setBackground(new Color(30, 30, 70));
            } else if (index % 2 == 0) {
                setBackground(new Color(20, 20, 40));
            } else {
                setBackground(new Color(10, 10, 30));
            }

            // Highlight the first 3 rows with special colors
            if (index == 0) {
                rankLabel.setForeground(new Color(255, 215, 0)); // Gold
            } else if (index == 1) {
                rankLabel.setForeground(new Color(192, 192, 192)); // Silver
            } else if (index == 2) {
                rankLabel.setForeground(new Color(205, 127, 50)); // Bronze
            } else {
                rankLabel.setForeground(Color.WHITE);
            }

            return this;
        }
    }

    private String[] getScoreStrings() {
        String[] scoreStrings = new String[scores.size()];
        for (int i = 0; i < scores.size(); i++) {
            scoreStrings[i] = String.format("%d. %s", i + 1, scores.get(i));
        }
        return scoreStrings;
    }

    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error saving high scores: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScores() {
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SCORES_FILE))) {
            scores = (List<ScoreEntry>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading high scores: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            scores = new ArrayList<>();
        }
    }

    public boolean isHighScore(int score) {
        if (scores.size() < MAX_SCORES) {
            return true;
        }
        return score > scores.get(scores.size() - 1).score;
    }
}