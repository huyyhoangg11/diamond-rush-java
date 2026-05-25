package main.ui;

import main.input.KeyHandler;
import java.awt.*;

public class HowToPlayScreen implements Screen {

    private final GameStateManager gsm;
    private boolean keyWasDown = false;

    public HowToPlayScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        boolean anyKey = key.escPressed || key.enterPressed;
        if (anyKey && !keyWasDown) {
            gsm.setState(GameState.MENU);
        }
        keyWasDown = anyKey;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();

        // Nền
        g2.setColor(new Color(8, 4, 22));
        g2.fillRect(0, 0, w, h);

        // Tiêu đề
        g2.setFont(new Font("Arial Black", Font.BOLD, 38));
        g2.setColor(new Color(255, 200, 0));
        String title = "HOW TO PLAY";
        g2.drawString(title, getCenterX(g2, title, w), 80);

        // Kẻ ngang
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 190, 0, 80));
        g2.drawLine(60, 95, w - 60, 95);

        // Nội dung: [phím] — [mô tả]
        String[][] rows = {
        	    {"W / UP",       "Move up"},
        	    {"S / DOWN",     "Move down"},
        	    {"A / LEFT",     "Move left"},
        	    {"D / RIGHT",    "Move right"},
        	    {"P / ESC",      "Pause / Resume"},
        	    {"Enter",        "Confirm in menu"},
        	    {"Diamond",      "Collect all to unlock the door"},
        	    {"Door",         "Exit when diamonds are complete"},
        	    {"Snake",        "Enemy — avoid! Lose 1 life on touch"},
        	    {"Rock",         "Can be pushed left or right"},
        	};

        int startY = 130;
        int rowH   = 42;
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));

        for (int i = 0; i < rows.length; i++) {
            int oy = startY + i * rowH;
            // Nền xen kẽ
            if (i % 2 == 0) {
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRect(55, oy - 26, w - 110, rowH - 2);
            }
            // Phím
            g2.setColor(new Color(255, 200, 0));
            g2.drawString(rows[i][0], 70, oy);
            // Mô tả
            g2.setColor(Color.WHITE);
            g2.drawString("—  " + rows[i][1], 270, oy);
        }

        // Kẻ ngang dưới
        int lineY = startY + rows.length * rowH + 5;
        g2.setColor(new Color(255, 190, 0, 80));
        g2.drawLine(60, lineY, w - 60, lineY);

        // Mục tiêu
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.setColor(new Color(150, 255, 170));
        String goal = "GOAL: Collect all diamonds, then reach the exit door";
        g2.drawString(goal, getCenterX(g2, goal, w), lineY + 35);

        // Quay về
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(new Color(100, 100, 135));
        String back = "ESC or Enter to go back";
        g2.drawString(back, getCenterX(g2, back, w), h - 28);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}