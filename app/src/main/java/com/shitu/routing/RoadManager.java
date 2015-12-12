package com.shitu.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by DongliangLyu on 2015/12/3.
 */
public class RoadManager  {
    Point3d startPt;
    Point3d endPt;

    private NodePoint3d startNode;
    private NodePoint3d endNode;

    private ArrayList<Edge3d> edgeList;
    //topological relation
    private ArrayList<NodePoint3d> nodePts;
    private ArrayList<Room> mRoomList;

    public RoadManager(ArrayList<SimpleEdge3d> simpleEdges, ArrayList<Room> roomList)
    {
        edgeList = GetEdgeList(simpleEdges);
        mRoomList = GetRoomList(roomList);
        nodePts = GetNodePoints();
    }

    public ArrayList<Edge3d> getEdgeList() {
        return edgeList;
    }

    public void SetStartPoint(Point3d startPt0)
    {
        startPt = startPt0;
        startNode = GetNearestNode(startPt);
    }

    public void SetEndPoint(Point3d endPt0)
    {
        endPt = endPt0;
        endNode = GetNearestNode(endPt);
    }

    public Point3dList GetRoad()
    {
        RefreshTime();
        CalcReachingTime();
        return new Point3dList(CalcShortestPath());
    }

    public Point3d GetRoomPosition(int roomNumber)
    {
        for (int i = 0; i < mRoomList.size(); ++i)
        {
            if (mRoomList.get(i).number == roomNumber) {
                return mRoomList.get(i).pt;
            }
        }
        return null;
    }

    public int GetRoomAngle(int roomNumber)
    {
        for (int i = 0; i < mRoomList.size(); ++i)
        {
            if (mRoomList.get(i).number == roomNumber) {
                return mRoomList.get(i).angle;
            }
        }
        return 0;
    }

    public ArrayList GetRoomNumbers()
    {
        ArrayList numbers = new ArrayList<>();
        for (int i = 0; i < mRoomList.size(); ++i)
        {
            numbers.add(mRoomList.get(i).number);
        }
        return numbers;
    }

    //Refresh node time
    private void RefreshTime()
    {
        for (int i = 0; i < nodePts.size(); ++i)
        {
            nodePts.get(i).time = 1.0E10;
        }
    }

    private ArrayList<Edge3d> GetEdgeList(ArrayList<SimpleEdge3d> sEdgeList)
    {
        ArrayList<Edge3d> edgeList = new ArrayList<Edge3d>();
        for (int i = 0; i < sEdgeList.size(); ++i)
        {
            Edge3d edge = new Edge3d(sEdgeList.get(i));
            edgeList.add(edge);
        }
        return edgeList;
    }

    private ArrayList<Room> GetRoomList(ArrayList<Room> roomList)
    {
        ArrayList<Room> tempList = new ArrayList<Room>();
        for (int i = 0; i < roomList.size(); ++i)
        {
            Room room = new Room(roomList.get(i));
            tempList.add(room);
        }
        return tempList;
    }

    private ArrayList<NodePoint3d> GetNodePoints()
    {
        double distError = 0.2;
        ArrayList<NodePoint3d> nodePts = new ArrayList<NodePoint3d>(edgeList.size());

        //第0条边的起始, 终止点分别作为第0个和第1个节点
        NodePoint3d nodePt0 = new NodePoint3d(edgeList.get(0).startPt, 0);
        NodePoint3d nodePt1 = new NodePoint3d(edgeList.get(0).endPt, 1);
        nodePt0.edgeIndex.add(0);
        nodePt0.dualNodeIndex.add(1);

        nodePt1.edgeIndex.add(0);
        nodePt1.dualNodeIndex.add(0);

        nodePts.add(nodePt0);
        nodePts.add(nodePt1);

        for (int i = 1; i < edgeList.size(); ++i)
        {
            Point3d startPt = edgeList.get(i).startPt;
            Point3d endPt = edgeList.get(i).endPt;

            boolean hasStartPt = false;
            boolean hasEndPt = false;
            int startPtIndex = -1;
            int endPtIndex = -1;
            for (int j = 0; j < nodePts.size(); ++j)
            {
                Point3d prevNodePt = nodePts.get(j).pt;
                if (!hasStartPt && startPt.IsEqual(prevNodePt, distError)) {
                    hasStartPt = true;
                    startPtIndex = j;
                }

                if(!hasEndPt && endPt.IsEqual(prevNodePt, distError)) {
                    hasEndPt = true;
                    endPtIndex = j;
                }

                if (hasStartPt && hasEndPt) break;
            }

            if (hasStartPt && hasEndPt) {
                nodePts.get(startPtIndex).edgeIndex.add(i);
                nodePts.get(startPtIndex).dualNodeIndex.add(endPtIndex);
                nodePts.get(endPtIndex).edgeIndex.add(i);
                nodePts.get(endPtIndex).dualNodeIndex.add(startPtIndex);
            }
            else if (hasStartPt && !hasEndPt) {
                nodePts.get(startPtIndex).edgeIndex.add(i);
                nodePts.get(startPtIndex).dualNodeIndex.add(nodePts.size());

                NodePoint3d endNote = new NodePoint3d(endPt, nodePts.size());
                endNote.edgeIndex.add(i);
                endNote.dualNodeIndex.add(startPtIndex);
                nodePts.add(endNote);
            }
            else if (!hasStartPt && hasEndPt) {
                NodePoint3d startNote = new NodePoint3d(startPt, nodePts.size());
                startNote.edgeIndex.add(i);
                startNote.dualNodeIndex.add(endPtIndex);
                nodePts.add(startNote);

                nodePts.get(endPtIndex).edgeIndex.add(i);
                nodePts.get(endPtIndex).dualNodeIndex.add(nodePts.size());
            }
            else {
                NodePoint3d startNote = new NodePoint3d(startPt, nodePts.size());
                startNote.edgeIndex.add(i);
                startNote.dualNodeIndex.add(nodePts.size() + 1);
                nodePts.add(startNote);

                NodePoint3d endNote = new NodePoint3d(endPt, nodePts.size());
                endNote.edgeIndex.add(i);
                endNote.dualNodeIndex.add(nodePts.size() - 1);
                nodePts.add(endNote);
            }
        }

        return nodePts;
    }

    private NodePoint3d GetNearestNode(Point3d pt)
    {
        NodePoint3d nodePt = nodePts.get(0);
        double distance = pt.SquareDistanceTo2(nodePt.pt);
        for (int i = 1; i < nodePts.size(); ++i)
        {
            double tempDist = pt.SquareDistanceTo2(nodePts.get(i).pt);
            if (tempDist < distance)
            {
                nodePt = nodePts.get(i);
                distance = tempDist;
            }
        }
        return nodePt;
    }

    private void InitCandidateNodePoints(ArrayList<NodePoint3d> candidateNodePts, NodePoint3d startNode)
    {
        int iSize = startNode.dualNodeIndex.size();
        for (int i = 0; i < iSize; ++i)
        {
            int dualNodeIndex = (int)startNode.dualNodeIndex.get(i);
            int edgeIndex = (int)startNode.edgeIndex.get(i);

            NodePoint3d dualNode = nodePts.get(dualNodeIndex);
            dualNode.time = edgeList.get(edgeIndex).time;
            candidateNodePts.add(dualNode);
        }
    }

    //计算时间最短的节点序号
    private int GetMinNodeOrder(ArrayList<NodePoint3d> localNodePts)
    {
        int order = 0;
        double localMinT = localNodePts.get(0).time;
        for (int i = 1; i < localNodePts.size(); i++)
        {
            if (localNodePts.get(i).time < localMinT) {
                order = i;
                localMinT = localNodePts.get(i).time;
            }
        }

        return order;
    }

    private void CalcReachingTime()
    {
        startNode.time = 0.0;
        double minTimeOld = 0.0;

        ArrayList<NodePoint3d> candidateNodePts = new ArrayList<NodePoint3d>();
        InitCandidateNodePoints(candidateNodePts, startNode);
        int minOrder = GetMinNodeOrder(candidateNodePts);
        double minTimeNew = candidateNodePts.get(minOrder).time;

        ArrayList dualNodeIndex = candidateNodePts.get(minOrder).dualNodeIndex;
        ArrayList edgeIndex = candidateNodePts.get(minOrder).edgeIndex;
        candidateNodePts.remove(minOrder);

        int nodePtSize = nodePts.size();
        int k = 0;
        while (candidateNodePts.size() >= 1 && k < nodePtSize)
        {

            for (int i = 0; i < dualNodeIndex.size(); ++i)
            {
                int tempNodeIndex = (int)dualNodeIndex.get(i);
                double timeOld = nodePts.get(tempNodeIndex).time;
                if (timeOld > minTimeOld + 0.01)
                {
                    int tempEdgeIndex = (int)edgeIndex.get(i);
                    double timeNew = minTimeNew + edgeList.get(tempEdgeIndex).time;
                    if (timeNew + 0.01 < timeOld) nodePts.get(tempNodeIndex).time = timeNew;
                    candidateNodePts.add(nodePts.get(tempNodeIndex));
                }
            }

            candidateNodePts = removeDuplicates(candidateNodePts);
            minTimeOld = minTimeNew;
            minOrder = GetMinNodeOrder(candidateNodePts);
            minTimeNew = candidateNodePts.get(minOrder).time;

            dualNodeIndex = candidateNodePts.get(minOrder).dualNodeIndex;
            edgeIndex = candidateNodePts.get(minOrder).edgeIndex;

            if (candidateNodePts.get(minOrder).index == endNode.index) {
                break;
            }

            if (candidateNodePts.size() > 1) {
                candidateNodePts.remove(minOrder);
            }
            else if (Math.abs(minTimeNew - minTimeOld) < 0.01) {
                endNode = candidateNodePts.get(minOrder);
                break;
            }

//            while (Math.abs(minTimeNew - minTimeOld) < 0.01)
//            {
//
//                minOrder = GetMinNodeOrder(candidateNodePts);
//                minTimeNew = candidateNodePts.get(minOrder).time;
//            }

            k++;
        }

    }

    private ArrayList<NodePoint3d> removeDuplicates(ArrayList<NodePoint3d> ptArray) {
        HashSet<NodePoint3d> ptHashSet = new HashSet<>();
        for (NodePoint3d pt : ptArray) {
            ptHashSet.add(pt);
        }

        ArrayList<NodePoint3d> identicalPtArray = new ArrayList<>();
        identicalPtArray.addAll(ptHashSet);
        return identicalPtArray;
    }

    private ArrayList<Point3d> CalcShortestPath()
    {
        NodePoint3d currentNode = endNode;
        ArrayList<Point3d> ptList = new ArrayList<Point3d>(100);
        ptList.add(currentNode.pt);

        int k = 0;
        while (currentNode.time > 0.1 && k < nodePts.size())
        {
            for (int i = 0; i < currentNode.edgeIndex.size(); ++i)
            {
                int dualNodeIndex = (int)currentNode.dualNodeIndex.get(i);
                int edgeIndex = (int)currentNode.edgeIndex.get(i);
                NodePoint3d nextNode = nodePts.get(dualNodeIndex);
                double nextNodeT = currentNode.time - edgeList.get(edgeIndex).time;
                if (Math.abs(nextNodeT - nextNode.time) < 1.0)
                {
                    ptList.add(nextNode.pt);
                    currentNode = nextNode;
                    break;
                }
            }

        }

        Collections.reverse(ptList);
        //返回最短路上的点序列
        return ptList;
    }
}
