package main.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean enterPressed;
    public boolean escPressed;      // thêm mới — dùng cho Pause/Back
    public boolean pPressed;        // thêm mới — phím P cũng pause

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed    = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed  = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed  = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_ENTER                -> enterPressed = true;
            case KeyEvent.VK_ESCAPE               -> escPressed   = true;
            case KeyEvent.VK_P                    -> pPressed     = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed    = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed  = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed  = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_ENTER                -> enterPressed = false;
            case KeyEvent.VK_ESCAPE               -> escPressed   = false;
            case KeyEvent.VK_P                    -> pPressed     = false;
        }
    }
}