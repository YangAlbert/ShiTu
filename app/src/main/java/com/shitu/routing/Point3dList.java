package com.shitu.routing;

import com.shitu.routing.Point2d;
import com.shitu.routing.Point3d;

import java.util.ArrayList;

/**
 * Created by DongliangLyu on 2015/12/8.
 */
public class Point3dList {
    ArrayList<Point3d> pts;

    public Point3dList(ArrayList<Point3d> inputPts)
    {
        pts = inputPts;
    }

    public ArrayList<Point3d> getPointList() {
        return pts;
    }

    //计算路的欧氏长度, 输入为欧氏坐标
    public double GetLength_Euler()
    {
        double length = 0.0;
        for (int i = 0; i < pts.size() - 1; ++i)
        {
            double squareLength = pts.get(i).SquareDistanceTo(pts.get(i + 1));
            length += Math.sqrt(squareLength);
        }

        return length;
    }

    //计算路的长度，输入为经纬坐标
    public double GetLength_GeoCoord()
    {
        double length = 0.0;
        for (int i = 0; i < pts.size() - 1; ++i)
        {
            double squareLength = pts.get(i).SquareDistanceTo2(pts.get(i + 1));
            length += Math.sqrt(squareLength);
        }

        return length;
    }

    //传入欧氏坐标表示的路， 计算路的起点方向
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

    //传入经纬坐标表示的路, 计算路的起点方向
    public Point2d GetDirection2()
    {
        Point2d pt2d = new Point2d();
        if (pts.size() >= 2)
        {
            Point3d startPt = pts.get(0);
            Point3d endPt = pts.get(1);

            ProjectPoint projecter = new ProjectPoint(startPt);
            Point3d projectStartPt = projecter.GetProjectivePoint(startPt);
            Point3d projectEndPt = projecter.GetProjectivePoint(endPt);

            pt2d = new Point2d(projectEndPt.x - projectStartPt.x, projectEndPt.y - projectStartPt.y);
//            if (pts.size() == 2){
//                Point3d endPt = pts.get(1);
//                Point3d projectEndPt = projecter.GetProjectivePoint(endPt);
//                pt2d = new Point2d(projectEndPt.x - projectStartPt.x, projectEndPt.y - projectStartPt.y);
//            }
//            else {
//                Point3d middlePt = pts.get(1);
//                Point3d projectMiddlePt = projecter.GetProjectivePoint(middlePt);
//                Point3d endPt = pts.get(2);
//                Point3d projectEndPt = projecter.GetProjectivePoint(endPt);
//                Point2d firstDir = new Point2d(projectMiddlePt.x - projectStartPt.x, projectMiddlePt.y - projectStartPt.y);
//                Point2d nextDir = new Point2d(projectEndPt.x - projectMiddlePt.x, projectEndPt.y - projectMiddlePt.y);
//                firstDir.Normalize();
//                nextDir.Normalize();
//                if (firstDir.IsEqual(nextDir, 0.1)){
//                    pt2d = firstDir;
//                }
//                else {
//                    pt2d = nextDir;
//                }
//            }
        }

        pt2d.Normalize();
        return pt2d;
    }

}
