package main.ui;

import main.input.KeyHandler;
import java.awt.Graphics2D;

public interface Screen {
    void update(KeyHandler key);
    void draw(Graphics2D g2);
}