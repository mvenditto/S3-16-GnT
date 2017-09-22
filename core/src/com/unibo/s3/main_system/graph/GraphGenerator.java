package com.unibo.s3.main_system.graph;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory;
import com.unibo.s3.main_system.game.Wall;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import java.util.*;
import java.util.concurrent.*;

/**
 * GraphGenerator is class used for generate graph starting from a map
 *
 * @author Sara
 * */
public class GraphGenerator {

    private static void log(String msg) {
        System.out.println("[GRAPH GENERATOR] " + msg);
    }

    private static void printGrid(Integer[][] grid) {
        log("Grid: ");
        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[0].length; j++) {
                if(grid[i][j] == null)
                    System.out.print("x  ");
                else
                    System.out.print(grid[i][j] + "  ");
            }
            System.out.println("");
        }
    }

    private static Vector2 createVector(float x, float y) {
        return new Vector2(x, y);
    }

    /**
     * @param width: width of map
     * @param height: height of map
     * @param mapFilename : filename of file where map is saved
     * @param worldActorRef: ActorRef od world actor,
     *                     to use when worldActor is in the same system of this class
     *
     * @return the genrated graph of type {@link UndirectedGraph<Vector2, DefaultEdge}
     */
    public static UndirectedGraph<Vector2, DefaultEdge> createGraphLocal(int width, int height, String mapFilename, ActorRef worldActorRef) {
        RaycastCollisionDetector<Vector2> collisionDetector =
                Box2dProxyDetectorsFactory.of(worldActorRef).newRaycastCollisionDetector();
        return init(width, height, mapFilename, collisionDetector);
    }

    /**
     * @param width: width of map
     * @param height: height of map
     * @param mapFilename : filename of file where map is saved
     * @param worldActorRef: ActorSelection od world actor,
     *                     to use when worldActor is in the opposite actor system compared to this class
     *
     * @return the genrated graph of type {@link UndirectedGraph<Vector2, DefaultEdge}
     */
    public static UndirectedGraph<Vector2, DefaultEdge> createGraphDistributed(int width, int height, String mapFilename, ActorSelection worldActorRef) {
        RaycastCollisionDetector<Vector2> collisionDetector =
                Box2dProxyDetectorsFactory.of(worldActorRef).newRaycastCollisionDetector();
        return init(width, height, mapFilename, collisionDetector);
    }

    private static UndirectedGraph<Vector2, DefaultEdge> init(int width, int height, String mapFilename, RaycastCollisionDetector<Vector2> collisionDetector) {
        int dimWall = Wall.WALL_THICKNESS();
        HashMap<Vector2, Vector2> walls = new HashMap<>();
        Integer[][] grid = new Integer[width+(dimWall*2)][height+(dimWall*2)];
        readMap(mapFilename, walls, grid);
        UndirectedGraph<Vector2, DefaultEdge> graph = create(grid, walls, collisionDetector, dimWall);
        return graph;
    }

    private static UndirectedGraph<Vector2, DefaultEdge> create(Integer[][] grid, HashMap<Vector2, Vector2> walls,
                                                                RaycastCollisionDetector<Vector2> collisionDetector, int dimWall) {
        UndirectedGraph<Vector2, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        addNodes(grid, graph, walls, dimWall);
        addEdges(graph, collisionDetector);
        return graph;
    }

    private static void addEdges(UndirectedGraph<Vector2, DefaultEdge> graph,
                                 RaycastCollisionDetector<Vector2> collisionDetector) {
        addFirstsEdges(graph, collisionDetector);
        concurrentCheckUnconnectedNodes(graph, collisionDetector);
    }

    private static void concurrentCheckUnconnectedNodes(UndirectedGraph<Vector2, DefaultEdge> graph,
                                                        RaycastCollisionDetector<Vector2> collisionDetector) {
        Semaphore semGraph = new Semaphore(1);
        int nProc = Runtime.getRuntime().availableProcessors()+1;
        ExecutorService executor = Executors.newFixedThreadPool(nProc);
        Set<Future<Void>> resultSet = new HashSet<>();
        int nTask = graph.vertexSet().size();
        int vectToThread = graph.vertexSet().size() / nTask;
        HashMap<Integer, List<Vector2>> ntc = new HashMap<>();
        int count = 0;
        ntc.put(count, new ArrayList<>());
        for(Vector2 node : graph.vertexSet()) {
            List<Vector2> nodesList = ntc.get(count);
            nodesList.add(node);
            if(nodesList.size() == vectToThread && count != (nTask-1)) {
                Future<Void> res = executor.submit(new ConcurrentAddEdges(nodesList, graph, collisionDetector, (count+1), semGraph));
                resultSet.add(res);
                count++;
                ntc.put(count, new ArrayList<>());
            }
        }
        Future<Void> res = executor.submit(new ConcurrentAddEdges(ntc.get(count), graph, collisionDetector, (count+1), semGraph));
        resultSet.add(res);

        for (Future<Void> future : resultSet) {
            try {
                future.get();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        executor.shutdown();
    }

    private static boolean checkEdgeRayCast(RaycastCollisionDetector<Vector2> collisionDetector, Vector2 v1, Vector2 v2) {

        float vertexRadius = 0.5f;
        int numRays = 16;

        final Vector2 tmp = new Vector2();
        final Vector2 tmp2 = new Vector2();
        final float step = 360.0f / numRays;

        for (float i = 0; i <= 360; i += step) {
            final double iRad = Math.toRadians(i);

            tmp.x = (float)(vertexRadius * Math.cos(iRad) + v2.x);
            tmp.y = (float)(vertexRadius * Math.sin(iRad) + v2.y);

            tmp2.x = (float)(vertexRadius * Math.cos(iRad) + v1.x);
            tmp2.y = (float)(vertexRadius * Math.sin(iRad) + v1.y);

            if (!collisionDetector.collides(new Ray<>(tmp2, tmp))) return true;
        }

        return false;
    }

    private static void addFirstsEdges(UndirectedGraph<Vector2, DefaultEdge> graph, RaycastCollisionDetector<Vector2> collisionDetector) {
        List<Vector2> nodes = new ArrayList<>(graph.vertexSet());
        graph.vertexSet().forEach(vertex -> {
            nodes.forEach(node -> {
                if(vertex != node && checkNodeProximity(vertex, node)
                        && checkEdgeRayCast(collisionDetector, vertex, node)) {
                    graph.addEdge(vertex, node);
                }
            });
            nodes.remove(vertex);
        });
    }

    private static boolean checkNodeProximity(Vector2 first, Vector2 second) {
        int val = 3;
        return ((Float.compare(first.x, second.x) == 0 && checkCoord(first.y, second.y, val)) ||
                (Float.compare(first.y, second.y) == 0 && checkCoord(first.x, second.x, val)) ||
                (checkCoord(first.x, second.x, 5) && checkCoord(first.y, second.y, 2)) ||
                (checkCoord(first.x, second.x, 2) && checkCoord(first.y, second.y, 5)));
    }

    private static boolean checkCoord(float coFirst, float coSecond, int val) {
        for(float i = coFirst - val; i <= coFirst + val; i++) {
            if(Float.compare(coSecond, i) == 0)
                return  true;
        }
        return false;
    }

    private static void readMap(String mapFilename, HashMap<Vector2, Vector2> walls, Integer[][] grid) {
        FileHandle file = Gdx.files.local(mapFilename);
        String text = file.readString();
        String[] lines = text.split("\\n");
        for(String line : lines) {
            String[] toks = line.split(":");
            float x = Float.parseFloat(toks[0]);
            float y = Float.parseFloat(toks[1]);
            float w = Float.parseFloat(toks[2]);
            float h = Float.parseFloat(toks[3]);

            float halfw = w / 2;
            float halfh = h / 2;
            Vector2 bl = createVector((x - halfw), (y - halfh));
            if(Float.compare(w, 2f) == 0) halfw = 0;
            if(Float.compare(h, 2f) == 0) halfh = 0;
            Vector2 tr = createVector((x + halfw), (y + halfh));

            int startX = 0, startY = 0;
            if(bl.x >= 0)
                startX = (int) bl.x;
            if(bl.y >= 0)
                startY = (int) bl.y;

            for(int i = startX; i <= (int) tr.x && i < grid.length; i++) {
                for(int j = startY; j <= tr.y && j < grid[0].length; j++ ) {
                    grid[i][j] = 1;
                }
            }
            walls.put(createVector(x, y), createVector(w, h));
        }
    }

    private static void addNodes(Integer[][] grid,
                          UndirectedGraph<Vector2, DefaultEdge> graph,
                          HashMap<Vector2, Vector2> walls, int dimWall) {
        addFirstNodes(grid, graph, dimWall);
        addWallsNode(walls, grid, graph, dimWall);
    }

    private static void addFirstNodes(Integer[][] grid, UndirectedGraph<Vector2, DefaultEdge> graph, int dimWall) {
        int step = 0, start = 0;
        if(dimWall == 3) {
            step = 4;
            start = 5;
        } else if(dimWall == 2) {
            step = 3;
            start = 3;
        }
        for(int row = (start); row < grid.length; row+=step) {
            for(int col = (start); col < grid[0].length; col+=step) {
                if(checkGrid(row, col, grid)) {
                    Vector2 v = createVector(row, col);
                    graph.addVertex(v);
                }
            }
        }
    }

    /**
     * @return true if cell of grid is empty
     * */
    private static boolean checkGrid(int row, int col, Integer[][] grid) {
        return grid[row][col] == null && grid[row-1][col] == null && grid[row-1][col-1] == null && grid[row][col -1] == null;
    }

    private static void addWallsNode(HashMap<Vector2, Vector2> walls, Integer[][] grid, UndirectedGraph<Vector2, DefaultEdge> graph, int dimWall) {
        walls.forEach((pos, size) -> {
            float dist = 2f;
            if(dimWall == 2) dist = 1f;
            Vector2[] vertex = new Vector2[4];
            float halfWidth = size.x / 2;
            float halfHeight = size.y / 2;
            vertex[0] = createVector((pos.x - halfWidth - dist), pos.y);
            vertex[1] = createVector((pos.x + halfWidth + dist), pos.y);
            vertex[2] = createVector(pos.x, (pos.y + halfHeight + dist));
            vertex[3] = createVector(pos.x, (pos.y - halfHeight - dist));

            for(int i = 0; i < 4; i++) {
                int x = (int) vertex[i].x;
                int y = (int) vertex[i].y;

                if(checkForAddingNode(x, y, grid, graph)) {
                    Vector2 v = createVector(x, y);
                    graph.addVertex(v);
                }

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

                if (modified && checkForAddingNode(x1, y1, grid, graph)) {
                    Vector2 v = createVector(x1, y1);
                    graph.addVertex(v);
                }


            }
        });
    }

    private static boolean checkForAddingNode(int x, int y, Integer[][] grid, UndirectedGraph<Vector2, DefaultEdge> graph) {
        return checkX(x, grid) && checkY(y, grid) && checkGrid(x,y, grid) && checkContains(x, y, graph);
    }

    private static boolean checkX(int x, Integer[][] grid) {
        return x > 0 && x < grid.length;
    }

    private static boolean checkY(int y, Integer[][] grid) {
        return y > 0 && y < grid[0].length;
    }

    private static boolean checkContains(int x, int y, UndirectedGraph<Vector2, DefaultEdge> graph) {
        int dist = 2;
        for(int i = x - dist; i <= x + dist; i++) {
            for (int j = y - dist; j <= y + dist; j++) {
                if(graph.containsVertex(createVector(i, j)))
                    return false;
            }
        }
        return true;
    }
}

