package model.paintActions;

public class EndErase extends EndStroke{
    public EndErase(int x, int y, int t, int id) {
        super(x, y, t, id);
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof EndErase);
    }

}
