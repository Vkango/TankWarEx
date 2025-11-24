package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 队伍的基地
 */
public class BaseStructure extends BaseEntity {
    private final int teamIndex;
    private int health = 100;

    public BaseStructure(double x, double y, int teamIndex) {
        super(x, y, 50, 50);
        this.teamIndex = teamIndex;
    }

    public int getTeamIndex() {
        return teamIndex;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            markDead();
        }
    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        // Color[] TEAM_COLORS = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
        // Color baseColor = TEAM_COLORS[teamIndex % TEAM_COLORS.length];

        // gc.setFill(baseColor.darker());
        // gc.fillRect(x, y, width, height);
        gc.drawImage(context.getImageManager().getImage("base"), x, y, width, height);
        gc.setFill(Color.WHITE);
        gc.fillText("BASE", x + 8, y + 30);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        // 基地阻挡所有实体（坦克和子弹）
        // 注意：子弹的handleCollision会处理对基地的伤害
        return true;
    }
}
