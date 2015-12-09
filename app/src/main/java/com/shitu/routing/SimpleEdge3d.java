package com.shitu.routing;
import com.shitu.routing.EdgeAttribute;

import com.shitu.routing.EdgeAttribute;
/**
 * Created by DongliangLyu on 2015/12/3.
 */
public class SimpleEdge3d {
    Point3d startPt;
    Point3d endPt;
    EdgeAttribute.EdgeType eType;

    public SimpleEdge3d(Point3d start, Point3d end) {
        startPt = start;
        endPt = end;
        eType = EdgeAttribute.EdgeType.FLATEDGE;
    }

    public SimpleEdge3d(Point3d start, Point3d end, EdgeAttribute.EdgeType type) {
        startPt = start;
        endPt = end;
        eType = type;
    }
}
