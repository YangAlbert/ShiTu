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
        public Node() {
            id = 0;
        }
        public int id;
        public Point2d pt;

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
        public Way() {
            id = 0;
        }
        public int id;
        public ArrayList nds;
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
                        currentNode = new Node();
                        break;
                    case XmlPullParser.START_TAG://开始读取某个标签
                        //通过getName判断读到哪个标签，然后通过nextText()获取文本节点值，或通过getAttributeValue(i)获取属性节点值
                        String name = parser.getName();
                        if (name.equalsIgnoreCase("node")) {
                            currentNode.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
                            currentNode.pt.x = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                            currentNode.pt.y = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                        }
                        else if (name.equalsIgnoreCase("way")) {
                            currentWay = new Way();
                            currentWay.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
                        }
                        else if (currentWay != null) {
                            if (name.equalsIgnoreCase("nd")) {
                                int ref_id = Integer.parseInt(parser.getAttributeValue(null, "ref"));
                                currentWay.nds.add(ref_id);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:// 结束元素事件
                        if (parser.getName().equalsIgnoreCase("node") && currentNode != null) {
                            nodes.add(currentNode);
                            currentNode = null;
                        }
                        else if (parser.getName().equalsIgnoreCase("way") && currentWay != null) {
                            for (int i = 1; i < currentWay.nds.size(); i++) {
                                int start_node_id = Integer.parseInt(currentWay.nds.get(i-1).toString());
                                int end_node_id = Integer.parseInt(currentWay.nds.get(i).toString());

                                Point2d start = null;
                                Point2d end = null;
                                for (Node node: nodes) {
                                    if (node.id == start_node_id) {
                                        start = node.pt;
                                    }
                                    else if (node.id == end_node_id) {
                                        end = node.pt;
                                    }
                                }

                                if (start != null && end != null) {
                                    SimpleEdge3d edge = new SimpleEdge3d();
                                    edge.startPt.x = start.x;
                                    edge.startPt.y = start.y;
                                    edge.endPt.x = end.x;
                                    edge.endPt.y = end.y;
                                    edges.add(edge);
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
