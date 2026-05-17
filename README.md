# 🎮 Pacman Game — Java Swing

A classic Pacman-inspired arcade game built with **Java Swing**, featuring animated sprites, multiple power-ups, ghost AI, and a high score system.

---

## 📸 Gameplay

> <img width="1024" height="1024" alt="background 2" src="https://github.com/user-attachments/assets/6005ea5a-f541-4a63-8f76-2eee4f2cee86" />
<img width="1536" height="1024" alt="Your paragraph text" src="https://github.com/user-attachments/assets/3993f8b8-54c1-47c2-825c-5a733c2a7425" />
<img width="1050" height="1091" alt="SCR-20260517-qbir" src="https://github.com/user-attachments/assets/3a13f7ea-46df-492b-8892-ad0d8ee28aa9" />



---

## ✨ Features

- 🟡 **Animated Pacman** — Smooth directional movement animations
- 👻 **Ghost AI** — Enemies with chase, frightened, slowed, and frozen states
- ⚡ **6 Power-Up Types:**
  | Power-Up | Duration | Effect |
  |---|---|---|
  | Speed Boost | 5 sec | Player moves faster |
  | Invincibility | 6 sec | Immune to ghosts |
  | Ghost Fright | 4 sec | Ghosts enter frightened mode — Pacman can eat them! |
  | Double Points | 10 sec | All points doubled |
  | Slow Ghosts | 6 sec | Ghosts move slower |
  | Freeze Ghosts | 5 sec | Ghosts are fully frozen |
- 🏆 **High Score System** — Persistent leaderboard saved to file
- ❤️ **Lives System** — Start with 3 lives
- ⏱️ **Timer** — Track elapsed time per game
- 🎨 **Custom Sprites** — PNG assets for all entities
- 📐 **Responsive Board** — Cell size adapts to screen resolution

---

## 🛠️ Requirements

- **Java 11** or higher
- **IntelliJ IDEA** (recommended) or any Java IDE

---

## 🚀 How to Run

### Option 1 — IntelliJ IDEA

1. Clone the repository:
   ```bash
   git clone https://github.com/wh0isdeniz/Java-Swing-Pacman-Game.git
   ```
2. Open the project folder in IntelliJ IDEA.
3. Mark `src/` as the **Sources Root** (right-click → Mark Directory as → Sources Root).
4. Run `MainMenu.java` as the main class.

### Option 2 — Command Line

```bash
# Compile
javac -d out src/*.java

# Run
java -cp out MainMenu
```

> ⚠️ Make sure the `src/img/` folder is on the classpath so images load correctly.

---

## 📁 Project Structure

```
Java-Swing-Pacman-Game/
├── src/
│   ├── MainMenu.java          # Main menu screen
│   ├── GameWindow.java        # Main game window & loop
│   ├── GameBoardModel.java    # Game board data model (MVC)
│   ├── GameCellRenderer.java  # Custom cell renderer
│   ├── Player.java            # Player logic
│   ├── Enemy.java             # Ghost/enemy AI
│   ├── PowerUp.java           # Power-up types & logic
│   ├── Animation.java         # Sprite animation
│   ├── GameAnimations.java    # Animation manager
│   ├── HighScore.java         # High score persistence
│   └── img/                   # All game sprites (PNG)
└── .gitignore
```

---

## 🎮 Controls

| Key | Action |
|---|---|
| ← → ↑ ↓ | Move Pacman |

---

## 🏗️ Architecture

The game follows an **MVC-inspired** structure:

- **Model** — `GameBoardModel` manages board state (walls, food, empty cells)
- **View** — `GameCellRenderer` + `JTable` renders each cell with custom sprites
- **Controller** — `GameWindow` drives the game loop via a background `Thread`

---

## 📄 License

This project was developed as a university GUI programming assignment.  
Feel free to fork and build upon it.
