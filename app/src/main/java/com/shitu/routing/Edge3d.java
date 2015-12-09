package com.shitu.routing;
//import com.shitu.routing.Point3d;

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
        length = startPt.SquareDistanceTo2(endPt);
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
        length = startPt.SquareDistanceTo2(endPt);

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
