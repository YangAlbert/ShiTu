package com.shitu.routing;

/**
 * Created by DongliangLyu on 2015/12/3.
 */
public class Point2d {
    double x;
    double y;

    //默认构造函数
    public Point2d(){
        x = 0.0;
        y = 0.0;
    }

    //构造函数
    public  Point2d(double x0, double y0) {
        x = x0;
        y = y0;
    }

    //判断两个点是否相等
    public boolean IsEqual(Point2d pt, double distError)
    {
        //double squareLength = this.SquareDistanceTo(pt);
        double squareLength = this.SquareDistanceTo(pt);
        return (squareLength < distError * distError);
    }

    public double SquareDistanceTo(Point2d pt)
    {
        //Euclidean square distance
        return Math.pow((x - pt.x), 2) + Math.pow((y - pt.y), 2);
    }

    //欧氏坐标下单位化
    public void Normalize()
    {
        if (Math.abs(x) > 0.01 || Math.abs(y) > 0.01)
        {
            double squareLength = Math.pow(x, 2) + Math.pow(y, 2);
            double length = Math.sqrt(squareLength);
            x = x / length;
            y = y / length;
        }
        else
        {
            x = 1.0;
            y = 0.0;
        }
    }
}