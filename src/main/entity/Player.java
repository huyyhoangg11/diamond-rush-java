// Huy Hoàng làm

package main.entity;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.input.KeyHandler;
import main.util.AssetManager;
import main.util.Collision;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    public int score;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
        direction = "down";
        score = 0;
    }

    public void getPlayerImage() {
        down1 = AssetManager.playerDown;
        down2 = AssetManager.playerDown;
        up1 = AssetManager.playerDown;
        up2 = AssetManager.playerDown;
        left1 = AssetManager.playerDown;
        left2 = AssetManager.playerDown;
        right1 = AssetManager.playerDown;
        right2 = AssetManager.playerDown;
    }

    public void update() {

        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

            if (keyH.upPressed) {
                direction = "up";
            } else if (keyH.downPressed) {
                direction = "down";
            } else if (keyH.leftPressed) {
                direction = "left";
            } else if (keyH.rightPressed) {
                direction = "right";
            }

            collisionOn = false;
            Collision.checkTile(this, gp);

            if (!collisionOn) {
                switch (direction) {
                    case "up":
                        y -= speed;
                        break;
                    case "down":
                        y += speed;
                        break;
                    case "left":
                        x -= speed;
                        break;
                    case "right":
                        x += speed;
                        break;
                    default:
                        break;
                }
            }

            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(AssetManager.playerDown, x, y, gp.tileSize, gp.tileSize, null);
    }
}
