package main.ui;

import main.core.GamePanel;
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

    private final Font fontHudLabel = new Font("Arial", Font.BOLD, 14);
    private final Font fontHudValue = new Font("Arial Black", Font.BOLD, 22);

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
        drawDiamondCounter(g2);
        drawLives(g2);
    }

    private void drawDiamondCounter(Graphics2D g2) {
        // Nền pill mờ
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(8, 8, 190, 48, 14, 14);

        // Nhãn nhỏ
        g2.setFont(fontHudLabel);
        g2.setColor(new Color(140, 210, 255));
        g2.drawString("DIAMONDS", 46, 22);

        // Icon
        g2.setFont(new Font("Dialog", Font.BOLD, 26));
        g2.setColor(new Color(80, 200, 255));
        g2.drawString("◆", 14, 46);

        // Số đếm — vàng nếu đủ, trắng nếu chưa đủ
        int collected = gp.player.score;
        int total     = gp.totalDiamonds;
        boolean done  = collected >= total;

        g2.setFont(fontHudValue);
        g2.setColor(done ? new Color(255, 215, 0) : Color.WHITE);
        g2.drawString(collected + " / " + total, 46, 46);
    }

    private void drawLives(Graphics2D g2) {
        int w = gp.getWidth();
        int lives = gp.player.lives;

        // Nền pill mờ bên phải
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(w - 128, 8, 120, 48, 14, 14);

        // Nhãn
        g2.setFont(fontHudLabel);
        g2.setColor(new Color(255, 180, 180));
        g2.drawString("MẠNG", w - 100, 22);

        // Trái tim theo số mạng (tối đa 3)
        g2.setFont(new Font("Dialog", Font.BOLD, 26));
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            hearts.append(i < lives ? "♥" : "♡");
        }
        // Đỏ tươi khi còn nhiều mạng, đỏ đậm khi còn 1
        g2.setColor(lives > 1 ? new Color(255, 80, 80) : new Color(220, 20, 20));
        g2.drawString(hearts.toString(), w - 118, 46);
    }
}