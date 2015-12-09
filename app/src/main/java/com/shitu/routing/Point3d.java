package com.shitu.routing;

import com.shitu.routing.EdgeAttribute;
/**
 * Created by DongliangLyu on 2015/12/3.
 */
public class Point3d {
    double x;
    double y;
    int floor;

    public double Lat() {
        return x;
    }

    public double Lon() {
        return y;
    }

    public int Floor() {
        return floor;
    }

    //默认构造函数
    public Point3d(){
        double x = 0.0;
        double y = 0.0;
        floor = 0;
    }

    //构造函数
    public Point3d(double x0, double y0, int floor0){
        x = x0;
        y = y0;
        floor = floor0;
    }

    //判断两个点是否相等
    public boolean IsEqual(Point3d pt, double distError)
    {
        //double squareLength = this.SquareDistanceTo(pt);
        double squareLength = this.SquareDistanceTo2(pt);
        return (squareLength < distError * distError);
    }


    public double SquareDistanceTo(Point3d pt)
    {
        //Euclidean square distance
        return Math.pow((x - pt.x), 2) + Math.pow((y - pt.y), 2) + Math.pow((floor - pt.floor) * EdgeAttribute.floorHeight, 2);
    }

    public double SquareDistanceTo2(Point3d pt)
    {
        //spherical square distance
        double earthRadius = 6.4E9;
        double tempSum = Math.cos(y) * Math.cos(pt.y) * Math.cos(x - pt.x) + Math.sin(y) * Math.sin(pt.y);
        double flatDis = earthRadius * Math.acos(tempSum);
        return Math.pow(flatDis, 2) + Math.pow((floor - pt.floor) * EdgeAttribute.floorHeight, 2);
    }


}
