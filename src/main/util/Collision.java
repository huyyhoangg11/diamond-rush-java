package main.util;

import main.core.GamePanel;
import main.entity.Player;
import main.object.Diamond;
import main.object.GameObject;

public class Collision {

    public static void checkTile(Player player, GamePanel gp) {
        int nextX = player.x;
        int nextY = player.y;

        switch (player.direction) {
            case "up":
                nextY -= player.speed;
                break;
            case "down":
                nextY += player.speed;
                break;
            case "left":
                nextX -= player.speed;
                break;
            case "right":
                nextX += player.speed;
                break;
            default:
                break;
        }

        int leftCol = nextX / gp.tileSize;
        int rightCol = (nextX + gp.tileSize - 1) / gp.tileSize;
        int topRow = nextY / gp.tileSize;
        int bottomRow = (nextY + gp.tileSize - 1) / gp.tileSize;

        if (gp.mapLoader.getTileAt(topRow, leftCol) == 1
                || gp.mapLoader.getTileAt(topRow, rightCol) == 1
                || gp.mapLoader.getTileAt(bottomRow, leftCol) == 1
                || gp.mapLoader.getTileAt(bottomRow, rightCol) == 1) {
            player.collisionOn = true;
            return;
        }

        for (GameObject object : gp.objects) {
            if (object == null || !object.isActive()) {
                continue;
            }

            if (nextX < object.worldX + gp.tileSize
                    && nextX + gp.tileSize > object.worldX
                    && nextY < object.worldY + gp.tileSize
                    && nextY + gp.tileSize > object.worldY) {

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
    }
}
