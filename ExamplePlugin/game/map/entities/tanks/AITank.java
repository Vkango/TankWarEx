package game.map.entities.tanks;

import game.engine.*;
import java.util.Random;

public class AITank extends BaseTankEntity {
    private Random random;
    private double changeDirectionTimer = 0;
    private double fireTimer = 0;
    private static final double BASE_SPEED = 300;

    public AITank(double x, double y, int teamIndex) {
        super(x, y, teamIndex);
    }

    public AITank() {
        super(0, 0, -1);
    }

    @Override
    protected void handleInput(double deltaTime, GameContext context) {
        if (this.random == null)
            random = new Random();
        changeDirectionTimer -= deltaTime;
        fireTimer -= deltaTime;

        if (changeDirectionTimer <= 0) {
            changeRandomDirection();
            changeDirectionTimer = 2.0 + random.nextDouble() * 2.0;
        }

        if (fireTimer <= 0) {
            fireShell(context);
            fireTimer = 1.0 + random.nextDouble();
        }
    }

    private void changeRandomDirection() {
        int direction = random.nextInt(5);
        double speed = BASE_SPEED;

        switch (direction) {
            case 0 -> {
                setVelocity(0, -speed);
                setRotation(-Math.PI / 2);
            }
            case 1 -> {
                setRotation(Math.PI / 2);
                setVelocity(0, speed);
            }
            case 2 -> {
                setVelocity(-speed, 0);
                setRotation(Math.PI);
            }
            case 3 -> {
                setVelocity(speed, 0);
                setRotation(0);
            }
            case 4 -> {
                setVelocity(0, 0);
            }
        }
    }
}