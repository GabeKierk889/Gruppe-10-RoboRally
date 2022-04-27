/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

/**
 * The GameController is responsible for handling the all
 * game request ,logic ,update the model and returns the view that should be changed.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;

    /**
     * This constructor takes a board as input.
     *
     * @param board the board which game should be played on.
     */
    public GameController(@NotNull Board board) {
        this.board = board;
    }

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space) {
//        Commented out as it is not a part of the game rules
//        if (space != null && space.board == board) {
//            Player currentPlayer = board.getCurrentPlayer();
//            if (currentPlayer != null && space.getPlayer() == null) {
//                currentPlayer.setSpace(space);
//                int playerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();
//                board.setCurrentPlayer(board.getPlayer(playerNumber));
//            }
//        }

    }

    /**
     * This methode allows the player to get some random cammand cards where players can program their robot with.
     */
    // XXX: V2
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    // XXX: V2
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    /**
     * This method allows the players to finish the programing phase, and
     * activates the activation phase, "Execute Program" and "Execute Current Register" buttons
     */
    // XXX: V2
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        // update player priority based on distance from priority antenna
        // this line is only in case priority was not updated at the end of the last register - i.e. at start of game
        board.sortPlayersAccordingToPriority();
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    // XXX: V2
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    /**
     * This methodes  executes the all programs card of all robots.
     */
    // XXX: V2
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * This methode executes the current map of the current robot,
     * so in this way the player click step by step throughout the program
     */
    // XXX: V2
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    // XXX: V2
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    // XXX: V2
    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    if (command.isInteractive()) {
                        board.setPhase(Phase.PLAYER_INTERACTION);
                        return;
                    } else {
                        executeCommand(currentPlayer, command);
                    }
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    // this is the end of a register
                    // at the end of a register, check if any player gets a checkpoint token
                    for (int i = 0; i < board.getPlayersNumber(); i++)
                        board.getPlayer(i).getSpace().giveTokenIfOnEndOnCheckpoint();
                    // check if any player has won the game
                    checkForWinner();
                    step++;
                    // update player priority based on distance from priority antenna
                    board.sortPlayersAccordingToPriority();
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    public void executeCommandOptionAndContinue(Command command) {
        Player currentPlayer = board.getCurrentPlayer();
        board.setPhase(Phase.ACTIVATION);
        executeCommand(currentPlayer, command);
        int step = board.getStep();
        int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
        if (nextPlayerNumber < board.getPlayersNumber()) {
            board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
        } else {
            // this is the end of a register
            // at the end of a register, check if any player gets a checkpoint token
            for (int i = 0; i < board.getPlayersNumber(); i++)
                board.getPlayer(i).getSpace().giveTokenIfOnEndOnCheckpoint();
            // check if any player has won the game
            checkForWinner();
            step++;
            // update player priority based on distance from priority antenna
            board.sortPlayersAccordingToPriority();
            if (step < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(step);
                board.setStep(step);
                board.setCurrentPlayer(board.getPlayer(0));
            } else {
                startProgrammingPhase();
            }
        }
        while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode()) {
            executeNextStep();
        }
    }

    // XXX: V2
    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;
                case U_TURN:
                    this.uTurn(player);
                    break;
                case SPEED_ROUTINE:
                    this.speedRoutine(player);
                    break;
                case BACKWARD:
                    this.moveBackward(player);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }

    void moveToSpace(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) throws ImpossibleMoveException {
        assert board.getNeighbour(player.getSpace(), heading) == space; // make sure the move to here is possible in principle
        Player other = space.getPlayer();
        if (other != null){
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                // XXX Note that there might be additional problems with
                //     infinite recursion here (in some special cases)!
                //     We will come back to that!
                moveToSpace(other, target, heading);

                // Note that we do NOT embed the above statement in a try catch block, since
                // the thrown exception is supposed to be passed on to the caller

                assert target.getPlayer() == null : target; // make sure target is free now
            } else {
                throw new ImpossibleMoveException(player, space, heading);
            }
        }
        player.setSpace(space);
    }

    class ImpossibleMoveException extends Exception {

        private Player player;
        private Space space;
        private Heading heading;

        public ImpossibleMoveException(Player player, Space space, Heading heading) {
            super("Move impossible");
            this.player = player;
            this.space = space;
            this.heading = heading;
        }
    }

    /**
     * This methode moves the player's robot 1 cell to the forwarded in the headed direction
     * @param player the player whose robots should be moved forward.
     */
    // TODO: V2
    public void moveForward(@NotNull Player player) {
        Space space = player.getSpace();
        if (player != null && player.board == board && space != null) {
            Heading heading = player.getHeading();
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                try {
                    moveToSpace(player, target, heading);
                } catch (ImpossibleMoveException e) {
                    // we don't do anything here  for now; we just catch the
                    // exception so that we do no pass it on to the caller
                    // (which would be very bad style).
                }
                //target.setPlayer(player);
            }
        }
    }

    public void moveBackward(@NotNull Player player) {
        uTurn(player);
        moveForward(player);
        uTurn(player);
    }

    /**
     * This methode moves the player's robot 2 cell to the forwarded in the headed direction
     * @param player the player whose robots should be moved forward.
     */
    // TODO: V2
    public void fastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
    }

    /**
     * This methode turns the player's robot 90 degree to the right
     * @param player the player whose robots should be turned.
     */
    // TODO: V2
    public void turnRight(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
        }
    }

    /**
     * This methode turns the player's robot 90 degree to the left
     * @param player the player whose robots should be turned.
     */
    // TODO: V2
    public void turnLeft(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    public void uTurn(@NotNull Player player) {
        if(player != null && player.board == board) {
            turnRight(player);
            turnRight(player);
        }
    }

    public void speedRoutine(Player player) {
        if(player != null && player.board == board) {
            moveForward(player);
            moveForward(player);
            moveForward(player);
        }
    }


    /**
     * This method allows moves the card between the commandCards fields
     * @param source the source where the card should be moved from.
     * @param target the target where the card should be moved to
     * @return return true if card can be moved successfully, otherwise return false.
     */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }

    public void checkForWinner() {
        // if a player has collected the last token, they have won
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            if (board.getPlayer(i).getCheckPointToken() != null
            && board.getPlayer(i).getCheckPointToken().getCheckpointNumber() == board.getMaxTokenNumber()) {
                // TODO - update the 2 lines below, display the relevant message and end the game
                board.setPhase(Phase.INITIALISATION);
                System.out.println("Player "+ (i+1) + " has won the game!");
            }
        }
    }
}
