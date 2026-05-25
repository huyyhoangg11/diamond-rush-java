package main.ui;

import main.input.KeyHandler;
import java.awt.*;

public class PauseScreen implements Screen {

    private final GameStateManager gsm;

    public PauseScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        // Toggle đã xử lý trong GameStateManager.update() — không làm gì thêm
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();

        // Lớp phủ tối lên trên gameplay
        g2.setColor(new Color(0, 0, 0, 155));
        g2.fillRect(0, 0, w, h);

        // Hộp trung tâm
        int bw = 360, bh = 185;
        int bx = (w - bw) / 2, by = (h - bh) / 2;

        g2.setColor(new Color(18, 8, 42, 235));
        g2.fillRoundRect(bx, by, bw, bh, 22, 22);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 200, 0));
        g2.drawRoundRect(bx, by, bw, bh, 22, 22);

        // Tiêu đề
        g2.setFont(new Font("Arial Black", Font.BOLD, 38));
        g2.setColor(Color.WHITE);
        String title = "PAUSED";
        g2.drawString(title, getCenterX(g2, title, w), by + 68);

        // Gợi ý
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(new Color(195, 195, 195));
        String hint = "Press P or ESC to resume";
        g2.drawString(hint, getCenterX(g2, hint, w), by + 112);

        // Thông tin hiện tại
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(130, 210, 255));
        int col  = gsm.getGamePanel().player.score;
        int tot  = gsm.getGamePanel().totalDiamonds;
        int live = gsm.getGamePanel().player.lives;
        String info  = "Diamonds: " + col + "/" + tot + "      Lives: " + live;
        g2.drawString(info, getCenterX(g2, info, w), by + 152);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}