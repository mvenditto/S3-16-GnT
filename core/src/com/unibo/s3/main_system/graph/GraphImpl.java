package com.unibo.s3.main_system.graph;

import akka.actor.ActorRef;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.world.actors.Box2dRayCastCollisionDetectorProxy;
import com.unibo.s3.main_system.world.actors.WorldActor;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GraphImpl implements Graph, GraphAdapter<Vector2> {
    private Integer[][] grid;
    private String mapFilename;

    private HashMap<Vector2, Vector2> walls = new HashMap<>();
    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private RaycastCollisionDetector<Vector2> collisionDetector;

    public GraphImpl() {

    }

    private void setWorldActor() {
        ActorRef worldActor = SystemManager.getInstance().getLocalActor("worldActor");
        this.collisionDetector = new Box2dProxyDetectorsFactory(worldActor).newRaycastCollisionDetector();
    }

    @Override
    public void receivedDimension(int width, int height) {
        this.grid = new Integer[width][height];
    }

    @Override
    public Integer[] getGridDimension() {
        return new Integer[]{grid.length, grid[0].length};
    }

    @Override
    public void receivedMapfile(String name) {
        this.mapFilename = name;
        this.setWorldActor();
        try {
            this.readMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getMapFilename() {return this.mapFilename; }

    @Override
    public Integer[][] getGrid() {
        return this.grid;
    }

    @Override
    public UndirectedGraph<Vector2, DefaultEdge> createGraph() throws Exception {
        if(this.mapFilename == null) {
            throw new Exception("File with map not received");
        } else {
            this.graph = new SimpleGraph<>(DefaultEdge.class);
            addNodes();
            addEdges();
        }
        System.out.println("Grafo creato: " + this.graph.toString());
        return this.graph;
    }

    private void addEdges() {
        addFirstsEdges();
        checkUnconnectedNodes();
    }

    private void checkUnconnectedNode2() {
        KShortestPaths<Vector2, DefaultEdge> ksp = new KShortestPaths<>(this.graph, 1);
        this.graph.vertexSet().forEach(node -> {
            float dist = 6f;
            for(float x = node.x - dist; x <= node.x + dist; x++) {
                for(float y = node.y - dist; x <= node.y + dist; y++) {
                    Vector2 toCompare = createVector(x,y);
                    if(this.graph.containsVertex(toCompare) && !toCompare.equals(node) &&
                            ksp.getPaths(node, toCompare).size() == 0 && checkEdgeRayCast(node, toCompare, 0.5f, 16))
                        this.graph.addEdge(node, toCompare);
                }
            }
        });
    }

    private void checkUnconnectedNodes() {
        KShortestPaths<Vector2, DefaultEdge> ksp = new KShortestPaths<Vector2, DefaultEdge>(this.graph, 1);
        List<Vector2> nodes = new ArrayList<>(this.graph.vertexSet());
        this.graph.vertexSet().forEach(vertex -> {
            if(this.graph.degreeOf(vertex) <= 1) {
                Vector2 nearest = null;
                float xDist = 10, yDist = 10;
                float xNode, yNode;
                for(Vector2 node : nodes) {
                    if(vertex != node && this.graph.getEdge(vertex, node) == null) {
                        xNode = Math.abs(vertex.x - node.x);
                        yNode = Math.abs(vertex.y - vertex.y);
                        if((xNode + yNode) <= (xDist + yDist)) {
                            List<GraphPath<Vector2, DefaultEdge>> paths = new ArrayList<>();
                            try {
                                paths = ksp.getPaths(vertex, node);
                            } catch (Exception e){}
                            if((paths.size() == 0 || paths.size() > 3) && checkEdgeRayCast(vertex, node, 0.5f, 16)) {
                                nearest = node;
                                 xDist = xNode;
                                 yDist = yNode;
                            }
                        }
                    }

                }
                if(nearest != null)
                    this.graph.addEdge(vertex, nearest);
                nodes.remove(vertex);
            }
        });
    }

    private void addFirstsEdges() {
        List<Vector2> nodes = new ArrayList<>(this.graph.vertexSet());
        this.graph.vertexSet().forEach(vertex -> {
            nodes.forEach(node -> {
                if(vertex != node && checkNodeProximity(vertex, node) && checkEdgeRayCast(vertex, node, 0.5f, 16))
                    this.graph.addEdge(vertex, node);
            });
            nodes.remove(vertex);
        });
    }

    /**
     *
     * @param v1 vertice 1
     * @param v2 vertice 2
     * @param vertexRadius raggio del vertice da considerare per castare il raggio
     * @param numRays numero di raggi da utilizzare
     * @return true se almeno un raggio da v1 raggiunge v2 (non il punto v2 preciso,
     * ma un punto sulla circonferenza di centro v2 e raggio vertexRadius)
     */
    private boolean checkEdgeRayCast(Vector2 v1, Vector2 v2, float vertexRadius, int numRays) {

        final Vector2 tmp = new Vector2();
        final Vector2 tmp2 = new Vector2();
        final float step = 360.0f / numRays;

        for (float i = 0; i <= 360; i += step) {
            final double iRad = Math.toRadians(i);

            tmp.x = (float)(vertexRadius * Math.cos(iRad) + v2.x);
            tmp.y = (float)(vertexRadius * Math.sin(iRad) + v2.y);

            tmp2.x = (float)(vertexRadius * Math.cos(iRad) + v1.x);
            tmp2.y = (float)(vertexRadius * Math.sin(iRad) + v1.y);

            if (!collisionDetector.collides(new Ray<>(tmp2, tmp))) {
                return true;
            }
        }

        return false;
    }

    private boolean checkNodeProximity(Vector2 first, Vector2 second) {
        int val = 3;
        return ((Float.compare(first.x, second.x) == 0 && checkCoord(first.y, second.y,val)) ||
                (Float.compare(first.y, second.y) == 0 && checkCoord(first.x, second.x, val)) ||
                (checkCoord(first.x, second.x, 5) || checkCoord(first.y, second.y, 2)) ||
                (checkCoord(first.x, second.x, 2) && checkCoord(first.y, second.y, 5)));
    }

    private boolean checkCoord(float coFirst, float coSecond, int val) {
        for(float i = coFirst - val; i <= coFirst + val; i++) {
            if(Float.compare(coSecond, i) == 0)
                return  true;
        }
        return false;
    }

    private boolean checkX(int x) {
        return x > 0 && x < grid.length;
    }

    private boolean checkY(int y) {
        return y > 0 && y < grid[0].length;
    }

    private void addNodes() {
        addFirstNodes();
        addWallsNode();
    }

    private void addWallsNode() {
        this.walls.forEach((pos, size) -> {
            Vector2[] vertex = new Vector2[4];
            float halfWidth = size.x / 2;
            float halfHeight = size.y / 2;
            vertex[0] = createVector((pos.x - halfWidth - 2f), pos.y);
            vertex[1] = createVector((pos.x - halfWidth + 2f), pos.y);
            vertex[2] = createVector(pos.x, (pos.y - halfHeight + 2f));
            vertex[3] = createVector(pos.x, (pos.y - halfHeight - 2f));

            for(int i = 0; i < 4; i++) {
                int x = (int) vertex[i].x;
                int y = (int) vertex[i].y;

                if(checkForAddingNode(x, y))
                    this.graph.addVertex(createVector(x,y));

                boolean modified = false;
                int x1 = x, y1 = y;
                if(x != vertex[i].x) {
                    x1 = x + 1;
                    modified = true;
                }
                if(y != vertex[i].y) {
                    y1 = y + 1;
                    modified = true;
                }
                if(modified && checkForAddingNode(x1, y1))
                    this.graph.addVertex(createVector(x1, y1));

            }
        });
    }

    private boolean checkForAddingNode(int x, int y) {
        return checkX(x) && checkY(y) && checkGrid(x,y) && checkContains(x, y);
    }

    private boolean checkContains(int x, int y) {
        int dist = 2;
        for(int i = x - dist; i <= x + dist; i++) {
            for (int j = y - dist; j <= y + dist; j++) {
                if(this.graph.containsVertex(createVector(i, j)))
                    return false;
            }
        }
        return true;
    }

    /**
     *
     * @param row index of row
     * @param col index of coloumn
     * @return true if cell of grid is empty
     * */
    private boolean checkGrid(int row, int col) {
        return grid[row][col] == null;
    }

    private Vector2 createVector(float x, float y) {
        return new Vector2(x, y);
    }

    private void addFirstNodes() {
        for(int row = 2; row < grid.length; row+=3) {
            for(int col = 2; col < grid[0].length; col+=3) {
                if(checkGrid(row, col))
                    this.graph.addVertex(createVector(row, col));
            }
        }
    }

    @Override
    public UndirectedGraph<Vector2, DefaultEdge> getGraph() {
        return this.graph;
    }

    private void readMap() throws IOException {
        Files.lines(Paths.get(this.mapFilename + ".txt")).forEach(l -> {
            //System.out.println("Linea: " + l);
            String[] toks = l.split(":");
            float x = Float.parseFloat(toks[0]);
            float y = Float.parseFloat(toks[1]);
            float w = Float.parseFloat(toks[2]);
            float h = Float.parseFloat(toks[3]);

            float halfw = w / 2;
            float halfh = h / 2;
            Vector2 bl = createVector((x - halfw), (y - halfh));
            Vector2 tr = createVector((x + halfw), (y + halfh));

            new Thread(new SetWall(bl, tr)).start();
            this.walls.put(createVector(x,y), createVector(w,h));

            /*for(int i = (int) bl.x; i <= (int) tr.x; i++) {
                for(int j = (int) bl.y; j <= tr.y; j++ ) {
                    grid[i][j] = 1;
                }
            }*/
        });
    }

    @Override
    public Iterator<Vector2> getVertices() {
        return this.graph.vertexSet().iterator();
    }

    @Override
    public Iterator<Vector2> getNeighbors(Vector2 vertex) {
        NeighborIndex<Vector2, DefaultEdge> neighborIndex = new NeighborIndex<>(this.graph);
        return neighborIndex.neighborsOf(vertex).iterator();
    }

    private class SetWall implements Runnable {
        Vector2 bl, tr;

        public SetWall(Vector2 bottomLeft, Vector2 topRight) {
            this.bl = bottomLeft;
            this.tr = topRight;
        }

        @Override
        public void run() {
            for(int i = (int) bl.x; i <= (int) tr.x && i < grid.length; i++) {
                for(int j = (int) bl.y; j <= tr.y && j < grid[0].length; j++ ) {
                    grid[i][j] = 1;
                }
            }
        }
    }

}
