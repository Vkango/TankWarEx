package game.rules;

import game.engine.*;
import game.map.entities.*;
import java.util.ArrayList;
import java.util.List;

public class DefaultRuleProvider implements RuleProvider {

    @Override
    public boolean isGameOver(GameContext context) {
        GameState state = context.getState();
        int aliveTeamCount = 0;

        for (int teamIndex : state.getTeamEntities().keySet()) {
            Object base = state.getTeamBase(teamIndex);
            boolean baseAlive = base != null && base instanceof Entity entity && entity.isAlive();
            boolean hasAliveTanks = state.getTeamEntityList(teamIndex).stream()
                    .anyMatch(e -> e instanceof TankEntity tank && tank.isAlive());

            if (baseAlive || hasAliveTanks) {
                aliveTeamCount++;
            }
        }

        return aliveTeamCount <= 1;
    }

    @Override
    public int getWinner(GameContext context) {
        GameState state = context.getState();
        List<Integer> survivors = new ArrayList<>();

        for (int teamIndex : state.getTeamEntities().keySet()) {
            Object base = state.getTeamBase(teamIndex);
            boolean baseAlive = base != null && base instanceof Entity entity && entity.isAlive();

            boolean hasAliveTanks = state.getTeamEntityList(teamIndex).stream()
                    .filter(e -> e instanceof TankEntity)
                    .map(e -> (TankEntity) e)
                    .anyMatch(Entity::isAlive);

            if (baseAlive || hasAliveTanks) {
                survivors.add(teamIndex);
            }
        }

        return survivors.size() == 1 ? survivors.get(0) : -1;
    }

    @Override
    public String getGameOverMessage(GameContext context) {
        int winner = getWinner(context);
        if (winner >= 0) {
            return "Team " + (winner + 1) + " Wins!";
        }
        return "Game Over - Draw!";
    }
}
