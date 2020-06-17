package gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import ai.Agent;
import checkers.Board;

// Because we're putting the code to handle the bot thinking
// into a different thread, the GUI can remain responsive and
// continue updating while the bot is thinking.
public class BotMoveService extends Service<int[]> {

    Board gameboard;
    Agent bot;

    public BotMoveService(Board gameboard, Agent bot) {
        this.gameboard = gameboard;
        this.bot = bot;
    }

    @Override
    protected Task<int[]> createTask() {
        return new BotMoveTask(gameboard, bot);
    }

    private class BotMoveTask extends Task<int[]> {

        Board gameboard;
        Agent bot;

        public BotMoveTask(Board gameboard, Agent bot) {
            this.gameboard = gameboard;
            this.bot = bot;
        }

        @Override
        protected int[] call() throws Exception {

            int[] move = bot.chooseMove(gameboard);
            // This sleep statement is the only reason
            // why the RandomAI has to have its own Task.
            Thread.sleep(200);

            return move;

        }
    }
}
