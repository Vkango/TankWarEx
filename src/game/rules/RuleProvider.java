package game.rules;

import game.engine.GameContext;

public interface RuleProvider {
    boolean isGameOver(GameContext context);

    int getWinner(GameContext context);

    String getGameOverMessage(GameContext context);
}
