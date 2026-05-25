package main.ui;

import main.input.KeyHandler;
import java.awt.*;

public class GameOverScreen implements Screen {

    private final GameStateManager gsm;
    private boolean keyWasDown = false;
    private int     fadeAlpha  = 0; // Hiệu ứng fade-in

    public GameOverScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        // Tăng fade-in mỗi frame
        if (fadeAlpha < 255) fadeAlpha = Math.min(fadeAlpha + 3, 255);

        boolean keyDown = key.enterPressed;
        if (keyDown && !keyWasDown && fadeAlpha > 150) {
            // Reset fade cho lần sau rồi về menu
            fadeAlpha = 0;
            gsm.setState(GameState.MENU);
        }
        keyWasDown = keyDown;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();
        int a = fadeAlpha;

        // Nền đỏ tối fade-in
        g2.setColor(new Color(22, 0, 0, a));
        g2.fillRect(0, 0, w, h);

        if (a < 60) return; // Chờ nền hiện ra rồi mới vẽ chữ

        // "GAME OVER" lớn — shadow + chữ chính
        g2.setFont(new Font("Arial Black", Font.BOLD, 74));
        String title = "GAME OVER";
        int tx = getCenterX(g2, title, w);
        g2.setColor(new Color(90, 0, 0, a));
        g2.drawString(title, tx + 5, h / 3 + 5);
        g2.setColor(new Color(215, 30, 30, a));
        g2.drawString(title, tx, h / 3);

        // Thống kê
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(255, 255, 255, a));
        int col = gsm.getGamePanel().player.score;
        int tot = gsm.getGamePanel().totalDiamonds;
        String stat  = "Diamonds collected: " + col + " / " + tot;
        g2.drawString(stat, getCenterX(g2, stat, w), h / 2 + 10);

        // Gợi ý — chỉ hiện sau khi fade xong
        if (a > 150) {
            g2.setFont(new Font("Arial", Font.PLAIN, 19));
            g2.setColor(new Color(170, 170, 170, a));
            String hint  = "Press Enter to return to Menu";
            g2.drawString(hint, getCenterX(g2, hint, w), h * 2 / 3);
        }
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}