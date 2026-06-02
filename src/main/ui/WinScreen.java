package main.ui;

import main.input.KeyHandler;
import java.awt.*;

public class WinScreen implements Screen {

    private final GameStateManager gsm;
    private boolean keyWasDown = false;
    private int     fadeAlpha  = 0;

    // Hiệu ứng hạt kim cương rơi
    private static final int SPARK_COUNT = 22;
    private final int[] sx = new int[SPARK_COUNT];
    private final int[] sy = new int[SPARK_COUNT];
    private final int[] sp = new int[SPARK_COUNT]; // tốc độ rơi

    public WinScreen(GameStateManager gsm) {
        this.gsm = gsm;
        for (int i = 0; i < SPARK_COUNT; i++) {
            sx[i] = (int)(Math.random() * 960);
            sy[i] = (int)(Math.random() * 960);
            sp[i] = 1 + (int)(Math.random() * 3);
        }
    }

    @Override
    public void update(KeyHandler key) {
        if (fadeAlpha < 255) fadeAlpha = Math.min(fadeAlpha + 3, 255);

        // Cập nhật hạt rơi
        int h = gsm.getGamePanel().getHeight();
        int w = gsm.getGamePanel().getWidth();
        for (int i = 0; i < SPARK_COUNT; i++) {
            sy[i] += sp[i];
            if (sy[i] > h) {
                sy[i] = 0;
                sx[i] = (int)(Math.random() * w);
            }
        }

        boolean keyDown = key.enterPressed;
        if (keyDown && !keyWasDown && fadeAlpha > 150) {
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

        // Nền gradient xanh lá fade-in
        GradientPaint bg = new GradientPaint(0, 0, new Color(0, 18, 5, a),
                                              0, h, new Color(0, 45, 18, a));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        if (a < 60) return;

        // Hạt kim cương rơi
        g2.setFont(new Font("Dialog", Font.BOLD, 20));
        g2.setColor(new Color(90, 210, 255, Math.min(a, 160)));
        for (int i = 0; i < SPARK_COUNT; i++) {
            g2.drawString("◆", sx[i], sy[i]);
        }

        // "CHIẾN THẮNG!" — shadow + chữ chính
        g2.setFont(new Font("Arial Black", Font.BOLD, 62));
        String title = "YOU WIN!";
        int tx = getCenterX(g2, title, w);
        g2.setColor(new Color(0, 70, 0, a));
        g2.drawString(title, tx + 4, h / 3 + 4);
        g2.setColor(new Color(70, 245, 100, a));
        g2.drawString(title, tx, h / 3);

        // Kim cương trang trí
        g2.setFont(new Font("Dialog", Font.BOLD, 52));
        g2.setColor(new Color(90, 210, 255, a));
        String gems = "◆  ◆  ◆";
        g2.drawString(gems, getCenterX(g2, gems, w), h / 2 - 5);

        // Thống kê
        g2.setFont(new Font("Arial", Font.BOLD, 23));
        g2.setColor(new Color(255, 255, 210, a));
        int col = gsm.getGamePanel().player.score;
        String stat  = "Collected " + col + " diamonds!";
        g2.drawString(stat, getCenterX(g2, stat, w), h / 2 + 62);

        if (a > 150) {
            g2.setFont(new Font("Arial", Font.PLAIN, 19));
            g2.setColor(new Color(160, 245, 180, a));
            String hint  = "Press Enter to return to Menu";
            g2.drawString(hint, getCenterX(g2, hint, w), h * 2 / 3 + 30);
        }
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}
