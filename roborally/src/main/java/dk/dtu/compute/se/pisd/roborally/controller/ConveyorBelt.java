package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

public class ConveyorBelt extends FieldAction {

    private Heading heading;
    private Color color;
    public void setColor(Color color){
        this.color=color;
    }

    public Color getColor(){
        return color;
    }

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        // TODO needs to be implemented
        FieldAction fa = space.getActions().stream().filter((FieldAction obj)-> obj.getClass().getSimpleName().equals("ConveyorBelt")).findAny().orElse(null);
        ConveyorBelt cb= (ConveyorBelt) fa;
        if(cb != null ){
            try {
                Space target= gameController.board.getNeighbour(space, cb.getHeading());
                if(cb.getColor()==Color.BLUE){
                    target=gameController.board.getNeighbour(target, cb.getHeading());
                }
                gameController.moveToSpace(space.getPlayer(), target, cb.getHeading());
                return true;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

}