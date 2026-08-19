package model.paintActions;

import model.Canvas;

/** ending class to complement fill, which is a starting undo point */
public class EndFill extends Fill{
    public EndFill(int x, int y, int r, int g, int b, int a, int id) {
        super(x, y, r, g, b, a, id);
    }
    @Override
    public void apply(Canvas canvas) {

    }
    @Override
    public PointType getPointType() {
        return PointType.REDOPOINT;
    }

    public boolean canEqual(Object o){
        return (o instanceof EndFill);
    }

}
