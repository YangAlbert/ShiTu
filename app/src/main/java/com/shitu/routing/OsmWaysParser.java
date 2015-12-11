package com.shitu.routing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

/**
 * Created by Huanggf on 2015/12/9.
 */
public class OsmWaysParser {
    private String mPath;
    private ArrayList<SimpleEdge3d> mEdgeList;

    private class Node{
        public long id;
        public Point2d pt;

        public Node() {
            id = 0;
            pt = new Point2d();
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

    private class Way{
        public long id;
        public ArrayList nds;
        public String tag;

        public Way() {
            id = 0;
            nds = new ArrayList<>();
        }
    }

    public OsmWaysParser(String path){
        mPath = path;
    }

    // 得到原始的路径信息
    public ArrayList<SimpleEdge3d> GetRawWays(){
        File xmlFile = new File(mPath);
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            mEdgeList = ReadOSM(inputStream);
            inputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mEdgeList;
    }

    private ArrayList<SimpleEdge3d> ReadOSM(InputStream inStream) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");// 设置数据源编码
            int eventType = parser.getEventType();// 获取事件类型
            Node currentNode = null;
            Way currentWay = null;
            ArrayList<SimpleEdge3d> edges = null;
            ArrayList<Node> nodes = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:// 文档开始事件,可以进行数据初始化处理
                        edges = new ArrayList<>();// 实例化集合类
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
                            currentWay.id = Long.parseLong(parser.getAttributeValue(null, "id"));
                        }
                        else if (currentWay != null) {
                            if (name.equalsIgnoreCase("nd")) {
                                long ref_id = Long.parseLong(parser.getAttributeValue(null, "ref"));
                                currentWay.nds.add(ref_id);
                            }
                            else if (name.equalsIgnoreCase("tag")) {
                                currentWay.tag = parser.getAttributeValue(null, "v");
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:// 结束元素事件
                        if (parser.getName().equalsIgnoreCase("node") && currentNode != null) {
                            nodes.add(currentNode);
                            currentNode = null;
                        }
                        else if (parser.getName().equalsIgnoreCase("way") && currentWay != null) {
                            if (currentWay.tag != null && currentWay.tag.equalsIgnoreCase("footway")) {
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
                                        edges.add(edge);
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
            return edges;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
