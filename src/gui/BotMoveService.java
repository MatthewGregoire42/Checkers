package gui;

import checkers.Move;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import ai.Agent;
import checkers.Board;

// Because we're putting the code to handle the bot thinking
// into a different thread, the GUI can remain responsive and
// continue updating while the bot is thinking.
public class BotMoveService extends Service<Move> {

    Board gameboard;
    Agent bot;

    public BotMoveService(Board gameboard, Agent bot) {
        this.gameboard = gameboard;
        this.bot = bot;
    }

    @Override
    protected Task<Move> createTask() {
        return new BotMoveTask(gameboard, bot);
    }

    private class BotMoveTask extends Task<Move> {

        Board gameboard;
        Agent bot;

        public BotMoveTask(Board gameboard, Agent bot) {
            this.gameboard = gameboard;
            this.bot = bot;
        }

        @Override
        protected Move call() throws Exception {

            Move move = bot.chooseMove(gameboard);
            // This sleep statement is the only reason
            // why the RandomAI has to have its own Task.
            Thread.sleep(200);

            return move;

        }
    }
}
