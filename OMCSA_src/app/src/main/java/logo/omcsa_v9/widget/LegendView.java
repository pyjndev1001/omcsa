package logo.omcsa_v9.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import logo.omcsa_v9.model.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logo.omcsa_v9.R;
import logo.omcsa_v9.model.LegendLine;
import logo.omcsa_v9.model.LegendPoint;
import logo.omcsa_v9.model.LegendRegion;

public class LegendView extends View {
    HashMap<String, Integer> colorsMap = new HashMap<>();
    HashMap<String, Integer> fillColorsMap = new HashMap<>();
    List<LegendPoint> legendPointList = new ArrayList<>();

    List<LegendRegion> legendRegionList = new ArrayList<>();
    List<LegendLine> legendLineList = new ArrayList<>();

    public LegendView(Context context) {
        super(context);
        buildColorMap();
    }

    public LegendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        buildColorMap();
    }

    public LegendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildColorMap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        //Draw Canvas Point
        Paint blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));

        for(int i = 0; i < legendPointList.size(); i++)
        {
            Paint paint = new Paint();
            Integer color = colorsMap.get(legendPointList.get(i).color.toLowerCase());
            int colorRes = R.color.white;
            if(color != null)
            {
                colorRes = color.intValue();
            }
            else
            {
                Log.e("ErrorColor", legendPointList.get(i).color.toLowerCase());
            }
            paint.setColor(getResources().getColor(colorRes));
            canvas.drawCircle(legendPointList.get(i).x, legendPointList.get(i).y, 10, blackPaint);
            canvas.drawCircle(legendPointList.get(i).x, legendPointList.get(i).y, 8, paint);
        }

        //Draw Lines
        for(int i = 0; i < legendLineList.size(); i++)
        {
            Paint paint = new Paint();
            paint.setStrokeWidth(2.0f);
            Integer color = colorsMap.get(legendLineList.get(i).color.toLowerCase());
            int colorRes = R.color.white;
            if(color != null)
            {
                colorRes = color.intValue();
            }
            else
            {
                Log.e("ErrorColor", legendLineList.get(i).color.toLowerCase());
            }
            paint.setColor(getResources().getColor(colorRes));
            canvas.drawLine(legendLineList.get(i).startPoint.x, legendLineList.get(i).startPoint.y, legendLineList.get(i).endPoint.x, legendLineList.get(i).endPoint.y, paint);
        }

        //Draw Regions
        for(int i = 0; i < legendRegionList.size(); i++)
        {
            Paint borderPaint = new Paint();
            borderPaint.setStrokeWidth(2.0f);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setColor(getColor(legendRegionList.get(i).color.toLowerCase()));

            Paint fillPaint = new Paint();
            fillPaint.setColor(getFillColor(legendRegionList.get(i).color.toLowerCase()));
            fillPaint.setStyle(Paint.Style.FILL);

            List<Point> points = new ArrayList<>();
            for(LegendPoint pt : legendRegionList.get(i).pointList)
            {
                points.add(pt.getPoint());
            }
            points.add(legendRegionList.get(i).pointList.get(0).getPoint());
            Path path = getPolygonPath(points);

            canvas.drawPath(path, borderPaint);
            canvas.drawPath(path, fillPaint);
        }
        super.onDraw(canvas);
    }

    private Path getPolygonPath(List<Point> points)
    {
        Path path = new Path();
        if(points.size() > 1){
            for(int i = points.size() - 2; i < points.size(); i++){
                if(i >= 0){
                    Point point = points.get(i);

                    if(i == 0){
                        Point next = points.get(i + 1);
                        point.dx = ((next.x - point.x) / 3);
                        point.dy = ((next.y - point.y) / 3);
                    }
                    else if(i == points.size() - 1){
                        Point prev = points.get(i - 1);
                        point.dx = ((point.x - prev.x) / 3);
                        point.dy = ((point.y - prev.y) / 3);
                    }
                    else{
                        Point next = points.get(i + 1);
                        Point prev = points.get(i - 1);
                        point.dx = ((next.x - prev.x) / 3);
                        point.dy = ((next.y - prev.y) / 3);
                    }
                }
            }
        }

        boolean first = true;
        for(int i = 0; i < points.size(); i++){
            Point point = points.get(i);
            if(first){
                first = false;
                path.moveTo(point.x, point.y);
            }
            else{
                Point prev = points.get(i - 1);
                path.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
            }
        }
        return path;
    }

    public void resetValues()
    {
        legendPointList.clear();
        legendLineList.clear();
        legendRegionList.clear();
    }

    public LegendPoint addPoint(float x, float y, String color)
    {
        LegendPoint point = new LegendPoint(x, y, color);
        legendPointList.add(point);
        return point;
    }

    public void addLine(float x, float y, float x1, float y1, String color)
    {
        legendLineList.add(new LegendLine(x, y, x1, y1, color));
    }

    public void addRegion(LegendRegion region)
    {
        legendRegionList.add(region);
    }


    private void buildColorMap()
    {
        colorsMap.put("white", R.color.legend_white);
        colorsMap.put("red", R.color.legend_red);
        colorsMap.put("chartreuse", R.color.legend_chart_reuse);
        colorsMap.put("aqua", R.color.legend_aqua);
        colorsMap.put("gold", R.color.legend_gold);
        colorsMap.put("gray", R.color.legend_gray);
        colorsMap.put("magenta", R.color.legend_magenta);
        colorsMap.put("darkorange", R.color.legend_dark_orange);
        colorsMap.put("loyalblue", R.color.legend_loyal_blue);
        colorsMap.put("royalblue", R.color.legend_loyal_blue);
        colorsMap.put("lawngreen", R.color.legend_lawn_green);
        colorsMap.put("yellowgreen", R.color.legend_yellow_green);
        colorsMap.put("plum", R.color.legend_plum);
        colorsMap.put("tan", R.color.legend_tan);
        colorsMap.put("peachpuff", R.color.legend_peach);
        colorsMap.put("green", R.color.legend_green);
        colorsMap.put("yellow", R.color.legend_yellow);
        colorsMap.put("cornflowerblue", R.color.legend_corn_flower_blue);
        colorsMap.put("sandybrown", R.color.legend_standy_brown);

        colorsMap.put("rainbow_1", R.color.rainbow_1);
        colorsMap.put("rainbow_2", R.color.rainbow_2);
        colorsMap.put("rainbow_3", R.color.rainbow_3);
        colorsMap.put("rainbow_4", R.color.rainbow_4);
        colorsMap.put("rainbow_5", R.color.rainbow_5);
        colorsMap.put("rainbow_6", R.color.rainbow_6);
        colorsMap.put("rainbow_7", R.color.rainbow_7);
        colorsMap.put("rainbow_8", R.color.rainbow_8);
        colorsMap.put("rainbow_9", R.color.rainbow_9);
        colorsMap.put("rainbow_10", R.color.rainbow_10);
        colorsMap.put("rainbow_11", R.color.rainbow_11);

        //for Others
        colorsMap.put("otherwhite", R.color.other_legend_white);
        colorsMap.put("otherred", R.color.other_legend_red);
        colorsMap.put("otherchartreuse", R.color.other_legend_chart_reuse);
        colorsMap.put("otheraqua", R.color.other_legend_aqua);
        colorsMap.put("othergold", R.color.other_legend_gold);
        colorsMap.put("othergray", R.color.other_legend_gray);
        colorsMap.put("othermagenta", R.color.other_legend_magenta);
        colorsMap.put("otherdarkorange", R.color.other_legend_dark_orange);
        colorsMap.put("otherloyalblue", R.color.other_legend_loyal_blue);
        colorsMap.put("otherroyalblue", R.color.other_legend_loyal_blue);
        colorsMap.put("otherlawngreen", R.color.other_legend_lawn_green);
        colorsMap.put("otheryellowgreen", R.color.other_legend_yellow_green);
        colorsMap.put("otherplum", R.color.other_legend_plum);
        colorsMap.put("othertan", R.color.other_legend_tan);
        colorsMap.put("otherpeachpuff", R.color.other_legend_peach);
        colorsMap.put("othergreen", R.color.other_legend_green);
        colorsMap.put("otheryellow", R.color.other_legend_yellow);
        colorsMap.put("othercornflowerblue", R.color.other_legend_corn_flower_blue);
        colorsMap.put("othersandybrown", R.color.other_legend_standy_brown);

        colorsMap.put("otherrainbow_1", R.color.other_rainbow_1);
        colorsMap.put("otherrainbow_2", R.color.other_rainbow_2);
        colorsMap.put("otherrainbow_3", R.color.other_rainbow_3);
        colorsMap.put("otherrainbow_4", R.color.other_rainbow_4);
        colorsMap.put("otherrainbow_5", R.color.other_rainbow_5);
        colorsMap.put("otherrainbow_6", R.color.other_rainbow_6);
        colorsMap.put("otherrainbow_7", R.color.other_rainbow_7);
        colorsMap.put("otherrainbow_8", R.color.other_rainbow_8);
        colorsMap.put("otherrainbow_9", R.color.other_rainbow_9);
        colorsMap.put("otherrainbow_10", R.color.other_rainbow_10);
        colorsMap.put("otherrainbow_11", R.color.other_rainbow_11);



        fillColorsMap.put("white", R.color.legend_fill_white);
        fillColorsMap.put("red", R.color.legend_fill_red);
        fillColorsMap.put("chartreuse", R.color.legend_fill_chart_reuse);
        fillColorsMap.put("aqua", R.color.legend_fill_aqua);
        fillColorsMap.put("gold", R.color.legend_fill_gold);
        fillColorsMap.put("gray", R.color.legend_fill_gray);
        fillColorsMap.put("magenta", R.color.legend_fill_magenta);
        fillColorsMap.put("darkorange", R.color.legend_fill_dark_orange);
        fillColorsMap.put("loyalblue", R.color.legend_fill_loyal_blue);
        fillColorsMap.put("royalblue", R.color.legend_fill_loyal_blue);
        fillColorsMap.put("lawngreen", R.color.legend_fill_lawn_green);
        fillColorsMap.put("yellowgreen", R.color.legend_fill_yellow_green);
        fillColorsMap.put("plum", R.color.legend_fill_plum);
        fillColorsMap.put("tan", R.color.legend_fill_tan);
        fillColorsMap.put("peachpuff", R.color.legend_fill_peach);
        fillColorsMap.put("green", R.color.legend_fill_green);
        fillColorsMap.put("yellow", R.color.legend_fill_yellow);
        fillColorsMap.put("cornflowerblue", R.color.legend_fill_corn_flower_blue);
        fillColorsMap.put("sandybrown", R.color.legend_fill_standy_brown);

        fillColorsMap.put("rainbow_1", R.color.fill_rainbow_1);
        fillColorsMap.put("rainbow_2", R.color.fill_rainbow_2);
        fillColorsMap.put("rainbow_3", R.color.fill_rainbow_3);
        fillColorsMap.put("rainbow_4", R.color.fill_rainbow_4);
        fillColorsMap.put("rainbow_5", R.color.fill_rainbow_5);
        fillColorsMap.put("rainbow_6", R.color.fill_rainbow_6);
        fillColorsMap.put("rainbow_7", R.color.fill_rainbow_7);
        fillColorsMap.put("rainbow_8", R.color.fill_rainbow_8);
        fillColorsMap.put("rainbow_9", R.color.fill_rainbow_9);
        fillColorsMap.put("rainbow_10", R.color.fill_rainbow_10);
        fillColorsMap.put("rainbow_11", R.color.fill_rainbow_11);

        //for Others
        fillColorsMap.put("otherwhite", R.color.other_legend_white);
        fillColorsMap.put("otherred", R.color.other_legend_red);
        fillColorsMap.put("otherchartreuse", R.color.other_legend_chart_reuse);
        fillColorsMap.put("otheraqua", R.color.other_legend_aqua);
        fillColorsMap.put("othergold", R.color.other_legend_gold);
        fillColorsMap.put("othergray", R.color.other_legend_gray);
        fillColorsMap.put("othermagenta", R.color.other_legend_magenta);
        fillColorsMap.put("otherdarkorange", R.color.other_legend_dark_orange);
        fillColorsMap.put("otherloyalblue", R.color.other_legend_loyal_blue);
        fillColorsMap.put("otherroyalblue", R.color.other_legend_loyal_blue);
        fillColorsMap.put("otherlawngreen", R.color.other_legend_lawn_green);
        fillColorsMap.put("otheryellowgreen", R.color.other_legend_yellow_green);
        fillColorsMap.put("otherplum", R.color.other_legend_plum);
        fillColorsMap.put("othertan", R.color.other_legend_tan);
        fillColorsMap.put("otherpeachpuff", R.color.other_legend_peach);
        fillColorsMap.put("othergreen", R.color.other_legend_green);
        fillColorsMap.put("otheryellow", R.color.other_legend_yellow);
        fillColorsMap.put("othercornflowerblue", R.color.other_legend_corn_flower_blue);
        fillColorsMap.put("othersandybrown", R.color.other_legend_standy_brown);

        fillColorsMap.put("otherrainbow_1", R.color.other_rainbow_1);
        fillColorsMap.put("otherrainbow_2", R.color.other_rainbow_2);
        fillColorsMap.put("otherrainbow_3", R.color.other_rainbow_3);
        fillColorsMap.put("otherrainbow_4", R.color.other_rainbow_4);
        fillColorsMap.put("otherrainbow_5", R.color.other_rainbow_5);
        fillColorsMap.put("otherrainbow_6", R.color.other_rainbow_6);
        fillColorsMap.put("otherrainbow_7", R.color.other_rainbow_7);
        fillColorsMap.put("otherrainbow_8", R.color.other_rainbow_8);
        fillColorsMap.put("otherrainbow_9", R.color.other_rainbow_9);
        fillColorsMap.put("otherrainbow_10", R.color.other_rainbow_10);
        fillColorsMap.put("otherrainbow_11", R.color.other_rainbow_11);
    }

    public int getColor(String strColor)
    {
        Integer color = colorsMap.get(strColor.toLowerCase());
        int colorRes = R.color.white;
        if(color != null)
        {
            colorRes = color.intValue();
        }
        else
        {
            Log.e("ErrorColor", strColor.toLowerCase());
        }

        return getResources().getColor(colorRes);
    }

    public int getFillColor(String strColor)
    {
        Integer color = fillColorsMap.get(strColor.toLowerCase());
        int colorRes = R.color.legend_fill_white;
        if(color != null)
        {
            colorRes = color.intValue();
        }
        else
        {
            Log.e("ErrorColor", strColor.toLowerCase());
        }
        return getResources().getColor(colorRes);
    }

}
