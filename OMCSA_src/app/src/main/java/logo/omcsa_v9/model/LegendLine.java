package logo.omcsa_v9.model;

public class LegendLine {
    public String color;
    public LegendPoint startPoint;
    public LegendPoint endPoint;
    public LegendLine(float x, float y, float x1, float y1, String color)
    {
        this.color = color;
        startPoint = new LegendPoint(x, y, color);
        endPoint = new LegendPoint(x1, y1, color);
    }
}
