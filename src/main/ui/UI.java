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
        int x = 18;
        int y = 16;
        int panelW = gp.hasHammer() ? 430 : 370;
        int panelH = 74;

        g2.setColor(new Color(30, 18, 8, 205));
        g2.fillRoundRect(x + 4, y + 5, panelW, panelH, 20, 20);
        GradientPaint panelPaint = new GradientPaint(x, y,
                new Color(102, 65, 26, 230),
                x, y + panelH,
                new Color(34, 21, 10, 230));
        g2.setPaint(panelPaint);
        g2.fillRoundRect(x, y, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(new Color(255, 214, 102, 235));
        g2.drawRoundRect(x, y, panelW, panelH, 20, 20);
        g2.setColor(new Color(255, 244, 186, 80));
        g2.drawLine(x + 18, y + 8, x + panelW - 18, y + 8);

        drawLevelBadge(g2, x + 18, y + 16);
        drawDiamondCounter(g2, x + 146, y + 20, 96);
        drawLives(g2, x + 262, y + 45);
        if (gp.hasHammer()) {
            drawHammerStatus(g2, x + 356, y + 20);
        }
    }

    private void drawDiamondCounter(Graphics2D g2, int x, int y, int width) {
        int collected = gp.player.score;
        String text = "x " + collected;

        g2.drawImage(AssetManager.uiDiamond, x, y + 3, 32, 32, null);
        g2.setFont(fontHudValue);
        drawTextShadow(g2, text, x + 40, y + 30, new Color(245, 250, 255));
    }

    private void drawLives(Graphics2D g2, int x, int baselineY) {
        int lives = gp.player.lives;

        g2.setFont(fontHeart);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            hearts.append(i < lives ? "♥" : "♡");
        }
        drawTextShadow(g2, hearts.toString(), x, baselineY,
                lives > 1 ? new Color(255, 90, 85) : new Color(235, 35, 35));
    }

    private void drawHammerStatus(Graphics2D g2, int x, int y) {
        g2.drawImage(AssetManager.hammer, x, y + 1, 32, 32, null);
        g2.setFont(fontHudValue);
        drawTextShadow(g2, "F", x + 40, y + 30, new Color(255, 230, 120));
    }

    private void drawLevelBadge(Graphics2D g2, int x, int y) {
        String level = gp.getLevelName(gp.getCurrentLevelIndex()).toUpperCase();
        g2.drawImage(AssetManager.uiPlayer, x, y, 34, 34, null);
        g2.setFont(new Font("Georgia", Font.BOLD, 20));
        drawTextShadow(g2, level, x + 44, y + 27, new Color(255, 235, 170));
    }

    private void drawTextShadow(Graphics2D g2, String text, int x, int y, Color fill) {
        g2.setColor(new Color(35, 16, 3, 210));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }
}
