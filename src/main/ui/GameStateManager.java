package main.ui;

import main.core.GamePanel;
import main.input.KeyHandler;
import java.awt.Graphics2D;

public class GameStateManager {

    private GameState currentState;
    private final GamePanel gp;

    private final MenuScreen      menuScreen;
    private final HowToPlayScreen howToPlayScreen;
    private final PauseScreen     pauseScreen;
    private final GameOverScreen  gameOverScreen;
    private final WinScreen       winScreen;

    // Debounce phím Pause để tránh toggle quá nhanh
    private boolean pauseKeyWasDown = false;

    public GameStateManager(GamePanel gp) {
        this.gp              = gp;
        this.menuScreen      = new MenuScreen(this);
        this.howToPlayScreen = new HowToPlayScreen(this);
        this.pauseScreen     = new PauseScreen(this);
        this.gameOverScreen  = new GameOverScreen(this);
        this.winScreen       = new WinScreen(this);
        this.currentState    = GameState.MENU;
    }

    public void setState(GameState newState) {
        this.currentState = newState;
    }

    public GameState getState() {
        return currentState;
    }

    public GamePanel getGamePanel() {
        return gp;
    }

    /**
     * Gọi từ đầu GamePanel.update() mỗi frame.
     *
     * Lưu ý: GamePanel.update() đã có guard:
     *   if (gameOver || levelComplete) return;
     * Vì vậy khi gameOver/levelComplete = true, GamePanel ngừng cập nhật gameplay,
     * nhưng GSM vẫn cần chạy để xử lý input trên các màn hình kết thúc.
     * => GamePanel phải gọi gsm.update(keyH) TRƯỚC guard đó (xem hướng dẫn tích hợp).
     */
    public void update(KeyHandler key) {
        // Phát hiện chuyển trạng thái tự động từ gameplay
        if (currentState == GameState.PLAYING) {
            if (gp.gameOver) {
                setState(GameState.GAME_OVER);
                return;
            }
            if (gp.levelComplete) {
                setState(GameState.WIN);
                return;
            }
        }

        // Toggle Pause (chỉ hoạt động khi PLAYING hoặc PAUSED)
        boolean pauseKeyDown = key.escPressed || key.pPressed;
        if (pauseKeyDown && !pauseKeyWasDown) {
            if (currentState == GameState.PLAYING) {
                setState(GameState.PAUSED);
            } else if (currentState == GameState.PAUSED) {
                setState(GameState.PLAYING);
            }
        }
        pauseKeyWasDown = pauseKeyDown;

        // Delegate update cho màn hình hiện tại
        switch (currentState) {
            case MENU        -> menuScreen.update(key);
            case HOW_TO_PLAY -> howToPlayScreen.update(key);
            case PAUSED      -> pauseScreen.update(key);
            case GAME_OVER   -> gameOverScreen.update(key);
            case WIN         -> winScreen.update(key);
            case PLAYING     -> {} // GamePanel tự xử lý gameplay
        }
    }

    /**
     * Gọi từ UI.draw() — vẽ màn hình overlay đè lên trên gameplay.
     * paintComponent() đã vẽ map/entity trước, UI.draw() gọi sau cùng.
     */
    public void draw(Graphics2D g2) {
        switch (currentState) {
            case MENU        -> menuScreen.draw(g2);
            case HOW_TO_PLAY -> howToPlayScreen.draw(g2);
            case PAUSED      -> pauseScreen.draw(g2);
            case GAME_OVER   -> gameOverScreen.draw(g2);
            case WIN         -> winScreen.draw(g2);
            case PLAYING     -> {} // Chỉ HUD — đã được UI.drawHUD() xử lý
        }
    }
}