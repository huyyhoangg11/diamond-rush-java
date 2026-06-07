package main.ui;

import main.core.GamePanel;
import main.util.AssetManager;
import java.awt.*;

/**
 * Được gọi từ GamePanel.paintComponent() sau g2.translate(cameraX, cameraY).
 * => Tọa độ đã về màn hình, vẽ trực tiếp lên góc màn hình là đúng.
 *
 * UI đảm nhiệm 2 việc:
 *   1. Vẽ HUD khi đang chơi (kim cương, mạng)
 *   2. Gọi GameStateManager.draw() để vẽ các màn hình overlay (Menu, Pause, v.v.)
 */
public class UI {

    private final GamePanel gp;
    public GameStateManager gsm;

    private final Font fontHudValue = new Font("Arial Black", Font.BOLD, 24);
    private final Font fontHeart = new Font("Dialog", Font.BOLD, 28);

    public UI(GamePanel gp) {
        this.gp = gp;
        // gsm sẽ được gán từ GamePanel sau khi khởi tạo
    }

    /**
     * Điểm vào duy nhất từ GamePanel.paintComponent().
     * g2 lúc này đã ở tọa độ màn hình (sau translate ngược camera).
     */
    public void draw(Graphics2D g2) {
        if (gsm == null) return;

        GameState state = gsm.getState();

        // Khi đang chơi hoặc pause: vẽ HUD trước, rồi overlay đè lên
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            drawHUD(g2);
        }

        // Vẽ màn hình tương ứng (Menu, Pause overlay, GameOver, Win)
        gsm.draw(g2);
    }

    // =========================================================
    // HUD — hiển thị kim cương và mạng khi đang chơi
    // =========================================================

    private void drawHUD(Graphics2D g2) {
        int panelW = 178;
        int panelH = gp.hasHammer() ? 132 : 92;
        int x = gp.getWidth() - panelW - 16;
        int y = gp.getHeight() - panelH - 16;

        g2.setColor(new Color(0, 0, 0, 175));
        g2.fillRoundRect(x, y, panelW, panelH, 14, 14);

        g2.drawImage(AssetManager.uiPlayer, x + 14, y + 13, 34, 34, null);
        drawLives(g2, x + 58, y + 39);
        drawDiamondCounter(g2, x + 15, y + 52, panelW - 30);
        if (gp.hasHammer()) {
            drawHammerStatus(g2, x + 15, y + 92, panelW - 30);
        }
    }

    private void drawDiamondCounter(Graphics2D g2, int x, int y, int width) {
        int collected = gp.player.score;
        String text = "x " + collected;

        g2.drawImage(AssetManager.uiDiamond, x, y + 4, 30, 30, null);
        g2.setFont(fontHudValue);
        g2.setColor(Color.WHITE);
        int textX = x + width - g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, textX, y + 30);
    }

    private void drawLives(Graphics2D g2, int x, int baselineY) {
        int lives = gp.player.lives;

        g2.setFont(fontHeart);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            hearts.append(i < lives ? "♥" : "♡");
        }
        g2.setColor(lives > 1 ? new Color(255, 80, 80) : new Color(220, 20, 20));
        g2.drawString(hearts.toString(), x, baselineY);
    }

    private void drawHammerStatus(Graphics2D g2, int x, int y, int width) {
        String text = "F";

        g2.drawImage(AssetManager.hammer, x, y, 30, 30, null);
        g2.setFont(fontHudValue);
        g2.setColor(Color.WHITE);
        int textX = x + width - g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, textX, y + 27);
    }
}
