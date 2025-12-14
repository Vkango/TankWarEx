package game.rules;

import game.engine.*;
import game.map.entities.BaseStructure;

import java.util.*;

public class DefaultRuleProvider implements RuleProvider {

    @Override
    public boolean isGameOver(GameContext context) {
        GameState state = context.getState();

        // 统计每个队伍的基地存活数量
        Map<Integer, Boolean> teamBaseAlive = new HashMap<>();

        for (Object obj : state.getEntities()) {
            if (obj instanceof BaseStructure base && base.isAlive()) {
                teamBaseAlive.put(base.getTeamIndex(), true);
            }
        }

        // 只要还有2个或以上基地存活，游戏继续
        return teamBaseAlive.size() <= 1;
    }

    @Override
    public int getWinner(GameContext context) {
        GameState state = context.getState();
        List<Integer> survivors = new ArrayList<>();

        // 获胜者是基地仍然存活的队伍
        for (Object obj : state.getEntities()) {
            if (obj instanceof BaseStructure base && base.isAlive()) {
                if (!survivors.contains(base.getTeamIndex())) {
                    survivors.add(base.getTeamIndex());
                }
            }
        }

        return survivors.size() == 1 ? survivors.get(0) : -1;
    }

    @Override
    public String getGameOverMessage(GameContext context) {
        try {
            context.getSoundManager().playSoundEffect("gameover");
        } finally {

        }

        int winner = getWinner(context);
        if (winner >= 0) {
            return "Team " + (winner + 1) + " Wins!";
        }
        return "Game Over - Draw!";
    }
}
