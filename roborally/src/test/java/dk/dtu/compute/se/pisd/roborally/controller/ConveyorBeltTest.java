package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConveyorBeltTest {
    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = new Board(8, 8, "easyintro");
        gameController = new GameController(board);
        for (int i = 0; i < 2; i++) {
            Player player = new Player(board, null,"Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @Test
    void doActionTest() {
        Board board = gameController.board;
        ConveyorBelt belt=  new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        belt.setColor("GREEN");
        board.getSpace(2,1).addAction((FieldAction) belt);
        Player current = board.getPlayer(1);
        gameController.executeNextStep();


        Assertions.assertEquals( null, board.getSpace(2, 1).getPlayer(),"Space (2,1) should be empty!\"");
        Assertions.assertEquals(current.getSpace(), board.getSpace(3,1), "Players in both space should be same\"");
    }
}