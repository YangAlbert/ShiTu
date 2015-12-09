package com.shitu.routing;

import com.shitu.routing.Point2d;
import com.shitu.routing.Point3d;

import java.util.ArrayList;

/**
 * Created by DongliangLyu on 2015/12/8.
 */
public class Road {
    ArrayList<Point3d> pts;

    public Road(ArrayList<Point3d> inputPts)
    {
        pts = inputPts;
    }

    //在欧氏坐标下计算路的长度
    public double GetLength()
    {
        double length = 0.0;
        for (int i = 0; i < pts.size() - 1; ++i)
        {
            double squareLength = pts.get(i).SquareDistanceTo(pts.get(i + 1));
            length += Math.sqrt(squareLength);
        }

        return length;
    }

    //在经纬坐标下计算路的长度
    public double GetLength2()
    {
        double length = 0.0;
        for (int i = 0; i < pts.size() - 1; ++i)
        {
            double squareLength = pts.get(i).SquareDistanceTo2(pts.get(i + 1));
            length += Math.sqrt(squareLength);
        }

        return length;
    }

    //在欧氏坐标下计算路的起点方向
    public Point2d GetDirection()
    {
        Point2d pt2d = new Point2d();
        if (pts.size() >= 2)
        {
            if (pts.size() == 2){
                Point3d startPt = pts.get(0);
                Point3d endPt = pts.get(1);
                pt2d = new Point2d(endPt.x - startPt.x, endPt.y - startPt.y);
            }
            else {
                Point3d startPt = pts.get(0);
                Point3d middlePt = pts.get(1);
                Point3d endPt = pts.get(2);
                Point2d firstDir = new Point2d(middlePt.x - startPt.x, middlePt.y - startPt.y);
                Point2d nextDir = new Point2d(endPt.x - middlePt.x, endPt.y - middlePt.y);
                firstDir.Normalize();
                nextDir.Normalize();
                if (firstDir.IsEqual(nextDir, 0.1)){
                    pt2d = firstDir;
                }
                else {
                    pt2d = nextDir;
                }
            }
        }

        pt2d.Normalize();
        return pt2d;
    }

}
