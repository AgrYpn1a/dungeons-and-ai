package com.dai.ai;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import com.badlogic.gdx.math.Vector2;
import com.dai.world.World;

/**
 * TODO: Write Grid class and refactor accordingly.
 *
 * We will work with World class directly for now.
 * Further abstraction to a separate Grid class is desirable.
 *  */
public final class AStar implements ISearch {

    private final class Node implements Comparable<Node> {
        private ITraversable t;
        public Node parent = null;

        public float cost = 0.0f;
        public float heuristic = 0.0f;

        public Node(ITraversable t) {
            this.t = t;
        }

        // public float getTotalCost() { return (cost + heuristic) * this.t.getCostModifier(); }

        public float getTotalCost() { return (cost + heuristic); }

        public ITraversable getTraversable() { return t; }

        public boolean isTraversable() { return t.isTraversable(); }

		@Override
		public int compareTo(Node o) {
            if(o.getTotalCost() < this.getTotalCost()) {
                return 1;
            } else if(o.getTotalCost() > this.getTotalCost()) {
                return -1;
            }

            return 0;
		}

        @Override
        public boolean equals(Object obj) {
            Node n = (Node)obj;
            if(n == null) {
                return false;
            }

            return this.getTraversable().getPosition().equals(n.getTraversable().getPosition());
        }
    }

    private Node[][] grid;

    public AStar() {
        ITraversable[][] grid = World.getInstance().getTiles();
        this.grid = new Node[grid.length][grid[0].length];

        for(int y=0; y<grid.length; y++) {
            for (int x=0; x<grid[0].length; x++) {
                this.grid[y][x] = new Node(grid[y][x]);
            }
        }
    }

    public AStar(ITraversable[][] grid) {
        this.grid = new Node[grid.length][grid[0].length];

        for(int y=0; y<grid.length; y++) {
            for (int x=0; x<grid[0].length; x++) {
                this.grid[y][x] = new Node(grid[y][x]);
            }
        }
    }

	@Override
	public Queue<Vector2> findPath(ITraversable start, ITraversable target) {
        Node startNode = new Node(start);
        Node targetNode = new Node(target);

        Queue<Node> frontier = new PriorityQueue<>();
        List<Node> visited = new LinkedList<>();

        // Init start node
        startNode.cost = 0;
        startNode.heuristic = computeManhattanDistance(startNode, targetNode);
        startNode.parent = null;

        frontier.add(startNode);

        System.out.println("findPath - from " + start + " to " + target + "startNode = " + startNode);

        while(!frontier.isEmpty() && !frontier.peek().equals(targetNode)) {
            Node currNode = frontier.poll();
            visited.add(currNode);

            List<Node> neighbours = getNeighbours(currNode);
            for(Node neighbour : neighbours) {
                float nextNodeCost = currNode.cost + computeManhattanDistance(currNode, neighbour);

                // The node has not been visited so far, nor has been added
                // for further inspection in the frontier, we need to consider it
                if(currNode.isTraversable() && !frontier.contains(neighbour) && !visited.contains(neighbour)) {

                    // Expand the frontier
                    neighbour.cost = nextNodeCost;
                    neighbour.heuristic = computeManhattanDistance(neighbour, targetNode);
                    neighbour.parent = currNode;

                    // Push to frontier
                    frontier.add(neighbour);
                }
            }
        }

        // Reconstruct path
        Node reachedTargetNode = frontier.poll();
        Stack<Node> pathStack = new Stack<>();

        Node curr = reachedTargetNode;
        while(curr.parent != null) {
            pathStack.push(curr);
            curr = curr.parent;
        }

        Queue<Vector2> path = new LinkedList<>();
        while(!pathStack.isEmpty()) {
            path.add(pathStack.pop().getTraversable().getPosition());
        }

        return path;
	}

    private int computeManhattanDistance(Node a, Node b) {
        return Math.abs((int) a.getTraversable().getPosition().x - (int) b.getTraversable().getPosition().x)
            + Math.abs((int) a.getTraversable().getPosition().y - (int) b.getTraversable().getPosition().y);
    }

    private List<Node> getNeighbours(Node n) {
        return World.getInstance().getNeighbours(n.getTraversable())
            .stream()
            .map(Node::new)
            .toList();
    }

}
