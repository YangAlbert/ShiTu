package com.shitu.routing;

/**
 * Created by Huanggf on 2015/12/11.
 */
public class Room {
    public int number;// 房间门牌号
    public Point3d pt;
    public int angle;// 门的角度（正北为0°，顺时针）
    public long idDoor;// 门的Id

    public Room(int num, Point3d p, int ang, long doorId) {
        number = num;
        pt = new Point3d(p.x, p.y, p.floor);
        angle = ang;
        idDoor = doorId;
    }
    public Room(Room r) {
        number = r.number;
        pt = new Point3d(r.pt.x, r.pt.y, r.pt.floor);
        angle = r.angle;
        idDoor = r.idDoor;
    }
}
