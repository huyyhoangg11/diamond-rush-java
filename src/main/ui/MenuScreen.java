package main.ui;

import main.input.KeyHandler;
import java.awt.*;
import main.core.GamePanel;

public class MenuScreen implements Screen {

    private final GameStateManager gsm;
    private int selectedOption = 0;
    private final String[] options = {"Start Game", "How To Play", "Quit"};

    private boolean keyWasDown = false;
    private int blinkCounter   = 0;
    private boolean blinkOn    = true;

    public MenuScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        boolean anyKey = key.upPressed || key.downPressed || key.enterPressed;

        if (anyKey && !keyWasDown) {
            if (key.upPressed) {
                selectedOption = (selectedOption - 1 + options.length) % options.length;
            } else if (key.downPressed) {
                selectedOption = (selectedOption + 1) % options.length;
            } else if (key.enterPressed) {
                handleSelection();
            }
        }
        keyWasDown = anyKey;

        // Nhấp nháy mũi tên mỗi 30 frame (~0.5 giây ở 60fps)
        if (++blinkCounter >= 30) {
            blinkOn      = !blinkOn;
            blinkCounter = 0;
        }
    }

    private void handleSelection() {
        switch (selectedOption) {
            case 0 -> {
                gsm.getGamePanel().resetGame();
                gsm.setState(GameState.PLAYING);
            }
            case 1 -> gsm.setState(GameState.HOW_TO_PLAY);
            case 2 -> System.exit(0);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        GamePanel gp = gsm.getGamePanel();
        int w = gp.getWidth();
        int h = gp.getHeight();

        // --- Nền gradient tím đậm ---
        GradientPaint bg = new GradientPaint(0, 0, new Color(8, 4, 22),
                                              0, h, new Color(25, 12, 55));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // --- Viền trang trí ---
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 190, 0, 70));
        g2.drawRect(14, 14, w - 28, h - 28);

        // --- Tiêu đề với shadow ---
        g2.setFont(new Font("Arial Black", Font.BOLD, 64));
        String title = "DIAMOND RUSH";
        int titleX   = getCenterX(g2, title, w);
        // Shadow
        g2.setColor(new Color(140, 70, 0));
        g2.drawString(title, titleX + 4, h / 5 + 4);
        // Chữ chính
        g2.setColor(new Color(255, 200, 0));
        g2.drawString(title, titleX, h / 5);

        // --- Subtitle ---
        g2.setFont(new Font("Arial", Font.ITALIC, 19));
        g2.setColor(new Color(170, 170, 220));
        String sub = "Collect diamonds — escape the cave!";
        g2.drawString(sub, getCenterX(g2, sub, w), h / 5 + 48);

        // --- Các lựa chọn ---
        g2.setFont(new Font("Arial Black", Font.BOLD, 30));
        int baseY = h / 2;
        for (int i = 0; i < options.length; i++) {
            int oy = baseY + i * 58;
            if (i == selectedOption) {
                // Highlight background
                int tw = g2.getFontMetrics().stringWidth(options[i]);
                g2.setColor(new Color(255, 200, 0, 35));
                g2.fillRoundRect(w / 2 - tw / 2 - 24, oy - 32, tw + 48, 42, 10, 10);
                // Mũi tên nhấp nháy
                if (blinkOn) {
                    g2.setColor(new Color(255, 200, 0));
                    g2.drawString("►", w / 2 - tw / 2 - 48, oy);
                }
                g2.setColor(new Color(255, 215, 0));
            } else {
                g2.setColor(new Color(155, 155, 195));
            }
            g2.drawString(options[i], getCenterX(g2, options[i], w), oy);
        }

        // --- Gợi ý phím ---
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(new Color(100, 100, 135));
        String hint = "Arrow Keys  Move       Enter  Select";
        g2.drawString(hint, getCenterX(g2, hint, w), h - 28);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}