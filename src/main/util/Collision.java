package main.util;

import main.core.GamePanel;
import main.entity.Player;
import main.object.Diamond;
import main.object.GameObject;

public class Collision {

    public static void checkTile(Player player, GamePanel gp) {
        int nextRow = player.getRow();
        int nextCol = player.getCol();

        switch (player.direction) {
            case "up":
                nextRow--;
                break;
            case "down":
                nextRow++;
                break;
            case "left":
                nextCol--;
                break;
            case "right":
                nextCol++;
                break;
            default:
                break;
        }

        if (gp.mapLoader.isWall(nextRow, nextCol)) {
            player.collisionOn = true;
            return;
        }

        GameObject object = gp.getObjectAt(nextRow, nextCol);
        if (object == null) {
            return;
        }

        if (object.collision) {
            player.collisionOn = true;
            return;
        }

        if (object instanceof Diamond) {
            object.setActive(false);
            player.score++;
        }
    }
}
