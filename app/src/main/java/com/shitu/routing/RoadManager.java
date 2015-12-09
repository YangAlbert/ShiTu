package com.shitu.routing;

import java.util.ArrayList;

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

    public RoadManager(ArrayList<SimpleEdge3d> simpleEdges)
    {
        edgeList = GetEdgeList(simpleEdges);
        nodePts = GetNodePoints();
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

    public ArrayList<Point3d> GetRoad()
    {
        RefreshTime();
        CalcReachingTime();
        return CalcShortestPath();
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

    private ArrayList<NodePoint3d> GetNodePoints()
    {
        double distError = 1;
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

                NodePoint3d endNote = new NodePoint3d(endPt, nodePts.size() + 1);
                endNote.edgeIndex.add(i);
                endNote.dualNodeIndex.add(nodePts.size());
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

        //candidateNodePts.remove(minOrder);

        int nodePtSize = nodePts.size();
        int k = 1;
        while (candidateNodePts.size() >= 1 && k < nodePtSize)
        {
            ArrayList dualNodeIndex = candidateNodePts.get(minOrder).dualNodeIndex;
            ArrayList edgeIndex = candidateNodePts.get(minOrder).edgeIndex;
            for (int i = 0; i < dualNodeIndex.size(); ++i)
            {
                int tempNodeIndex = (int)dualNodeIndex.get(i);
                double timeOld = nodePts.get(tempNodeIndex).time;
                if (timeOld > minTimeOld)
                {
                    int tempEdgeIndex = (int)edgeIndex.get(i);
                    double timeNew = minTimeNew + edgeList.get(tempEdgeIndex).time;
                    if (timeNew < timeOld) nodePts.get(tempNodeIndex).time = timeNew;
                    candidateNodePts.add(nodePts.get(tempNodeIndex));
                }
            }

            candidateNodePts.remove(minOrder);
            minOrder = GetMinNodeOrder(candidateNodePts);
            minTimeOld = minTimeNew;
            minTimeNew = candidateNodePts.get(minOrder).time;

            if (candidateNodePts.get(minOrder).index == endNode.index) break;

            k++;
        }

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

        //返回最短路上的点序列
        return ptList;
    }

}
