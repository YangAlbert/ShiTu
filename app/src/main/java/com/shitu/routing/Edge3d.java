package com.shitu.routing;

/**
 * Created by DongliangLyu on 2015/12/3.
 */

//topology edge for route minimizing
public class Edge3d {
    Point3d startPt;
    Point3d endPt;
    EdgeAttribute.EdgeType eType;
    double length;
    double time;
    //默认构造函数
    public Edge3d(){
        startPt = new Point3d();
        endPt = new Point3d();
        length = 0.0;
        time = 0.0;
        eType = EdgeAttribute.EdgeType.FLATEDGE;
    }

    //构造函数
    public Edge3d(Point3d startPt0, Point3d endPt0){
        startPt = startPt0;
        endPt = endPt0;
        length = Math.sqrt( Math.pow((startPt0.x - endPt0.x), 2) + Math.pow((startPt0.y - endPt0.y), 2)
                + Math.pow((startPt0.floor - endPt0.floor) * EdgeAttribute.floorHeight, 2) );
        switch(eType)
        {
            case FLATEDGE:
                time = length / EdgeAttribute.walkSpeed;
                break;
            case STAIREDGE:
                time = length / EdgeAttribute.stairSpeed;
                break;
            case LIFTEDGE:
                time = length / EdgeAttribute.liftSpeed;
        }
    }

    public void Initial()
    {
        length = Math.sqrt( Math.pow((startPt.x - endPt.x), 2) + Math.pow((startPt.y - endPt.y), 2)
                + Math.pow((startPt.floor - endPt.floor) * EdgeAttribute.floorHeight, 2) );

        switch(eType)
        {
            case FLATEDGE:
                time = length / EdgeAttribute.walkSpeed;
                break;
            case STAIREDGE:
                time = length / EdgeAttribute.stairSpeed;
                break;
            case LIFTEDGE:
                time = length / EdgeAttribute.liftSpeed;
        }
    }


    public Edge3d(SimpleEdge3d simpleEdge)
    {
        startPt = simpleEdge.startPt;
        endPt = simpleEdge.endPt;
        eType = simpleEdge.eType;

        Initial();
    }

}
