package com.shitu.routing;

/**
 * Created by LvDongliang on 2015/12/10.
 */
public class ProjectPoint {
    Point2d originPt = new Point2d();

    public ProjectPoint(Point2d originPt0)
    {
        //Set coordinate origin.
        originPt.x = originPt0.x * Math.PI / 180;
        originPt.y = originPt0.y * Math.PI / 180;
    }

    public ProjectPoint(Point3d originPt0)
    {
        //Set coordinate origin.
        originPt.x = originPt0.x * Math.PI / 180;
        originPt.y = originPt0.y * Math.PI / 180;
    }

    //Get projective point with Euclidean coordinate.
    public Point2d GetProjectivePoint(Point2d spherePt)
    {
        double earthRadius = 6.4E6;

        Point2d tempSpherePt = new Point2d(spherePt.x * Math.PI / 180, spherePt.y * Math.PI / 180);
        double deltaTheta = tempSpherePt.x - originPt.x;
        double deltaPhi = tempSpherePt.y - originPt.y;

        double deltaX = earthRadius * Math.cos(originPt.y) * deltaTheta;
        double deltaY = earthRadius * deltaPhi;

        return new Point2d(deltaX, deltaY);
    }

    public Point3d GetProjectivePoint(Point3d spherePt)
    {
        double earthRadius = 6.4E6;

        Point2d tempSpherePt = new Point2d(spherePt.x * Math.PI / 180, spherePt.y * Math.PI / 180);
        double deltaTheta = tempSpherePt.x - originPt.x;
        double deltaPhi = tempSpherePt.y - originPt.y;

        double deltaX = earthRadius * Math.cos(originPt.y) * deltaTheta;
        double deltaY = earthRadius * deltaPhi;

        return new Point3d(deltaX, deltaY, spherePt.floor);
    }
}
