package com.shitu.routing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

/**
 * Created by Huanggf on 2015/12/9.
 */
public class OsmWaysParser {
    private String mPath;
    private ArrayList<InnerEdge> mEdgeList;
    private ArrayList<Room> mRoomList;

    private class InnerEdge {
        public long startId;
        public long endId;
        public SimpleEdge3d edge;

        public InnerEdge(long start, long end, SimpleEdge3d eg) {
            startId = start;
            endId = end;
            edge = eg;
        }
    }
    private class Node {
        public long id;
        public Point2d pt;
        public boolean isDoor;// 是否为门
        public int angle;// 门的角度（正北为0°，顺时针）

        public Node() {
            id = 0;
            pt = new Point2d();
            isDoor = false;
            angle = 0;
        }

        public boolean equals(Object o) {
            if (o instanceof Node) {
                Node u = (Node)o;
                return id == u.id;
            }
            else if (o instanceof Integer) {
                Integer u = (Integer)o;
                return u == id;
            }
            return false;
        }
    }

    private class Way {
        public ArrayList nds;
        public String key;
        public String value;

        public Way() {
            nds = new ArrayList<>();
        }
    }

    public OsmWaysParser(String path) {
        mPath = path;

        File xmlFile = new File(mPath);
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            ReadOSM(inputStream);
            inputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 得到原始的路径信息
    public ArrayList<SimpleEdge3d> GetRawWays() {
        ArrayList<SimpleEdge3d> edgeList = new ArrayList<>();
        for (int i = 0; i < mEdgeList.size(); ++i) {
            edgeList.add(mEdgeList.get(i).edge);
        }
        return edgeList;
    }

    public ArrayList<Room> GetRawRooms() {
        if (mRoomList == null) {
            mRoomList = new ArrayList<>();
        }
        return mRoomList;
    }

    public boolean WriteXml(String path) {
        StringWriter xmlWriter = new StringWriter();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlSerializer xmlSerializer = factory.newSerializer();

            xmlSerializer.setOutput(xmlWriter);// 保存创建的xml
            xmlSerializer.startDocument("utf-8", null);// <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>

            xmlSerializer.startTag("", "root");
            xmlSerializer.attribute("", "author", "Flatland");

            ArrayList<Node> nodes = new ArrayList<>();// 还原所有的节点
            for (int i = 0; i < mEdgeList.size(); i++) {
                InnerEdge edge = mEdgeList.get(i);

                Node startNode = new Node();
                Point3d startPt = edge.edge.startPt;
                startNode.id = edge.startId;
                startNode.pt.x = startPt.x;
                startNode.pt.y = startPt.y;
                startNode.isDoor = false;
                startNode.angle = 0;
                if (!nodes.contains(startNode)) {
                    nodes.add(startNode);
                }

                Node endNode = new Node();
                Point3d endPt = edge.edge.endPt;
                endNode.id = edge.endId;
                endNode.pt.x = endPt.x;
                endNode.pt.y = endPt.y;
                endNode.isDoor = false;
                endNode.angle = 0;
                if (!nodes.contains(endNode)) {
                    nodes.add(endNode);
                }
            }
            for (int i = 0; i < mRoomList.size(); i++) {
                Room room = mRoomList.get(i);

                Node node = new Node();
                Point3d currentPt = room.pt;
                node.id = room.idDoor;
                node.pt.x = currentPt.x;
                node.pt.y = currentPt.y;
                node.isDoor = true;
                node.angle = room.angle;
                if (!nodes.contains(node)) {
                    nodes.add(node);
                }
            }

            // 写节点
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);

                xmlSerializer.startTag("", "node");// 创建节点
                xmlSerializer.attribute("", "id", Long.toString(node.id));
                xmlSerializer.attribute("", "lat", Double.toString(node.pt.x));
                xmlSerializer.attribute("", "lon", Double.toString(node.pt.y));

                if (node.isDoor) {
                    xmlSerializer.startTag("", "tag");
                    xmlSerializer.attribute("", "k", "direction");
                    xmlSerializer.attribute("", "v", Integer.toString(node.angle));
                    xmlSerializer.endTag("", "tag");

                    xmlSerializer.startTag("", "tag");
                    xmlSerializer.attribute("", "k", "type");
                    xmlSerializer.attribute("", "v", "door");
                    xmlSerializer.endTag("", "tag");
                }

                xmlSerializer.endTag("", "node");
            }

            // 写边对应的 way
            for (int i = 0; i < mEdgeList.size(); i++) {
                InnerEdge edge = mEdgeList.get(i);

                xmlSerializer.startTag("", "way");// 创建节点

                xmlSerializer.startTag("", "nd");
                xmlSerializer.attribute("", "ref", Long.toString(edge.startId));
                xmlSerializer.endTag("", "nd");

                xmlSerializer.startTag("", "nd");
                xmlSerializer.attribute("", "ref", Long.toString(edge.endId));
                xmlSerializer.endTag("", "nd");

                xmlSerializer.startTag("", "tag");
                xmlSerializer.attribute("", "k", "highway");
                xmlSerializer.attribute("", "v", "indoor");
                xmlSerializer.endTag("", "tag");

                xmlSerializer.endTag("", "way");
            }

            // 写房间对应的 way
            for (int i = 0; i < mRoomList.size(); i++) {
                Room room = mRoomList.get(i);

                xmlSerializer.startTag("", "way");// 创建节点

                xmlSerializer.startTag("", "nd");
                xmlSerializer.attribute("", "ref", Long.toString(room.idDoor));
                xmlSerializer.endTag("", "nd");

                xmlSerializer.startTag("", "tag");
                xmlSerializer.attribute("", "k", "office");
                xmlSerializer.attribute("", "v", Integer.toString(room.number));
                xmlSerializer.endTag("", "tag");

                xmlSerializer.endTag("", "way");
            }

            xmlSerializer.endTag("", "root");
            xmlSerializer.endDocument();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return SavedXML(path, xmlWriter.toString());
    }

    private boolean ReadOSM(InputStream inStream) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");// 设置数据源编码
            int eventType = parser.getEventType();// 获取事件类型
            Node currentNode = null;
            Way currentWay = null;
            ArrayList<Node> nodes = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:// 文档开始事件,可以进行数据初始化处理
                        mEdgeList = new ArrayList<>();// 实例化集合类
                        mRoomList = new ArrayList<>();// 实例化集合类
                        nodes = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG://开始读取某个标签
                        //通过getName判断读到哪个标签，然后通过nextText()获取文本节点值，或通过getAttributeValue(i)获取属性节点值
                        String name = parser.getName();
                        if (name.equalsIgnoreCase("node")) {
                            assert currentNode == null;
                            currentNode = new Node();
                            currentNode.id = Long.parseLong(parser.getAttributeValue(null, "id"));
                            String value = parser.getAttributeValue(null, "lat");
                            currentNode.pt.x = Double.parseDouble(value);
                            value = parser.getAttributeValue(null, "lon");
                            currentNode.pt.y = Double.parseDouble(value);
                        }
                        else if (name.equalsIgnoreCase("way")) {
                            currentWay = new Way();
                        }
                        else if (currentWay != null) {
                            if (name.equalsIgnoreCase("nd")) {
                                long ref_id = Long.parseLong(parser.getAttributeValue(null, "ref"));
                                currentWay.nds.add(ref_id);
                            }
                            else if (name.equalsIgnoreCase("tag")) {
                                String key = parser.getAttributeValue(null, "k");
                                // 只关心这两个 tag
                                if (key.equalsIgnoreCase("highway") || key.equalsIgnoreCase("office")) {
                                    currentWay.key = key;
                                    currentWay.value = parser.getAttributeValue(null, "v");
                                }
                            }
                        }
                        else if (currentNode != null) {
                            if (name.equalsIgnoreCase("tag")) {
                                String key = parser.getAttributeValue(null, "k");
                                if (key != null) {
                                    String value = parser.getAttributeValue(null, "v");

                                    if (key.equalsIgnoreCase("type")) {
                                        if (value != null && value.equalsIgnoreCase("door")) {
                                            currentNode.isDoor = true;
                                        }
                                    }
                                    else if (key.equalsIgnoreCase("direction")) {
                                        if (value != null) {
                                            currentNode.angle = Integer.parseInt(value);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:// 结束元素事件
                        if (parser.getName().equalsIgnoreCase("node") && currentNode != null) {
                            nodes.add(currentNode);
                            currentNode = null;
                        }
                        else if (parser.getName().equalsIgnoreCase("way") && currentWay != null) {
                            if (currentWay.key != null && currentWay.value != null) {
                                if (currentWay.key.equalsIgnoreCase("highway") &&
                                        currentWay.value.equalsIgnoreCase("indoor")) {
                                    for (int i = 1; i < currentWay.nds.size(); i++) {
                                        long start_node_id = Long.parseLong(currentWay.nds.get(i - 1).toString());
                                        long end_node_id = Long.parseLong(currentWay.nds.get(i).toString());

                                        Point2d start = null;
                                        Point2d end = null;
                                        for (Node node: nodes) {
                                            if (start == null && node.id == start_node_id) {
                                                start = node.pt;
                                            }
                                            else if (end == null && node.id == end_node_id) {
                                                end = node.pt;
                                            }

                                            if (start != null && end != null) {
                                                break;
                                            }
                                        }

                                        if (start != null && end != null) {
                                            Point3d startPt = new Point3d(start.x, start.y, 3);
                                            Point3d endPt = new Point3d(end.x, end.y, 3);
                                            SimpleEdge3d edge = new SimpleEdge3d(startPt, endPt);

                                            InnerEdge innerEdge = new InnerEdge(start_node_id, end_node_id, edge);
                                            mEdgeList.add(innerEdge);
                                        }
                                    }
                                }
                                else if (currentWay.key.equalsIgnoreCase("office")) {
                                    int size = currentWay.nds.size();
                                    if (size > 0) {
                                        Point3d door_pt = null;
                                        Point3d first_pt = null;// 第一个点，容错用
                                        int angle = 0;
                                        long door_id = 0;

                                        for (int i = 0; i < size; i++) {
                                            long node_id = Long.parseLong(currentWay.nds.get(i).toString());
                                            for (Node node: nodes) {
                                                if (node.id == node_id) {
                                                    if (first_pt == null) {
                                                        first_pt = new Point3d();
                                                        first_pt.x = node.pt.x;
                                                        first_pt.y = node.pt.y;
                                                        angle = node.angle;
                                                        door_id = node.id;
                                                    }

                                                    if (node.isDoor) {
                                                        door_pt = new Point3d();// 房间的门
                                                        door_pt.x = node.pt.x;
                                                        door_pt.y = node.pt.y;
                                                        angle = node.angle;
                                                        door_id = node.id;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (door_pt != null) {
                                                break;
                                            }
                                        }

                                        if (door_pt == null) {
                                            assert(first_pt != null);
                                            door_pt = first_pt;
                                        }

                                        int number = Integer.parseInt(currentWay.value);
                                        Room room = new Room(number, door_pt, angle, door_id);
                                        mRoomList.add(room);
                                    }
                                }
                            }
                            currentWay = null;
                        }
                        break;
                }
                eventType = parser.next();
            }

            inStream.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean SavedXML(String fileName, String xml) {
        File xmlFile = new File(fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(xmlFile);
            byte[] buffer = xml.getBytes();
            outputStream.write(buffer);
            outputStream.close();
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
