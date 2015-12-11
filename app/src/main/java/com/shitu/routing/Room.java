package com.shitu.routing;

/**
 * Created by Huanggf on 2015/12/11.
 */
public class Room {
    public int number;
    public Point3d pt;

    public Room(int num, Point3d p) {
        number = num;
        pt = new Point3d(p.x, p.y, p.floor);
    }
    public Room(Room r) {
        number = r.number;
        pt = new Point3d(r.pt.x, r.pt.y, r.pt.floor);
    }
}
