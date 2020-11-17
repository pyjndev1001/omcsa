package logo.omcsa_v9.model;

public class LegendPoint {
    public float x;
    public float y;
    public String color;

    public LegendPoint(float x, float y, String color)
    {
        this.x = x;
        this.y = y;
        this.color = color;
    }
    public Point getPoint()
    {
        Point point = new Point();
        point.x = (int)x;
        point.y = (int)y;
        return  point;
    }
}
