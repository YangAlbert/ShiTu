package com.shitu.routing;

import java.util.ArrayList;

import com.shitu.routing.Point3d;
/**
 * Created by DongliangLyu on 2015/12/3.
 */
public class NodePoint3d {
    Point3d pt;
    int index;
    double time = 1.0E10;
    //当前节点的其他邻域节点索引
    ArrayList dualNodeIndex;
    //所属的边索引
    ArrayList edgeIndex;

    public NodePoint3d() {
        pt = new Point3d();
        index = -1;
    }

    public NodePoint3d(Point3d pt0, int index0) {
        pt = pt0;
        index = index0;
    }
}
