package game.map.entities.tanks;

import game.engine.*;
import javafx.scene.input.KeyCode;

public class PlayerTank extends BaseTankEntity {
    private static final double BASE_SPEED = 200;

    public PlayerTank(double x, double y, int teamIndex) {
        super(x, y, teamIndex);
    }

    @Override
    protected void handleInput(double deltaTime, GameContext context) {
        InputManager input = context.getInputManager();
        double speed = BASE_SPEED;

        boolean moving = false;
        if (input.isKeyPressed(KeyCode.UP)) {
            setVelocity(0, -speed);
            setRotation(-Math.PI / 2);
            moving = true;
        } else if (input.isKeyPressed(KeyCode.DOWN)) {
            setVelocity(0, speed);
            setRotation(Math.PI / 2);
            moving = true;
        } else if (input.isKeyPressed(KeyCode.LEFT)) {
            setVelocity(-speed, 0);
            setRotation(Math.PI);
            moving = true;
        } else if (input.isKeyPressed(KeyCode.RIGHT)) {
            setVelocity(speed, 0);
            setRotation(0);
            moving = true;
        } else {
            setVelocity(0, 0);
        }

        if (moving) {
            context.getSoundManager().playBGM("tank_move");
        } else {
            context.getSoundManager().stopSound("tank_move");
        }

        if (input.isKeyPressed(KeyCode.SPACE)) {
            fireShell(context);
        }
    }
}