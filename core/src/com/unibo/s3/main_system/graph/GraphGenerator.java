package com.unibo.s3.main_system.graph;

import akka.actor.ActorRef;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory;
import com.unibo.s3.main_system.communication.SystemManager;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import scala.Int;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;

public class GraphGenerator {

    private static void log(String msg) {
        System.out.println("[GRAPH GENERATOR] " + msg);
    }

    private static void printGrid(Integer[][] grid) {
        log("Griglia: ");
        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[0].length; j++) {
                System.out.print(grid[i][j] + "  ");
            }
            log("");
        }
    }

    private static Vector2 createVector(float x, float y) {
        return new Vector2(x, y);
    }

    public static UndirectedGraph<Vector2, DefaultEdge> createGraph(int width, int height, String mapFilename) {
        ActorRef worldActor = SystemManager.getInstance().getLocalActor("worldActor");
        RaycastCollisionDetector<Vector2> collisionDetector = new Box2dProxyDetectorsFactory(worldActor).newRaycastCollisionDetector();
        HashMap<Vector2, Vector2> walls = new HashMap<>();
        Integer[][] grid = new Integer[width+6][height+6];
        log("genero il grafo di dimensione: " + width + ", " + height);
        Cronometer cron = new Cronometer();

        cron.start();

        readMap(mapFilename, walls, grid);
        //concurrentReadMap(mapFilename, walls, grid);
        cron.stop();

        log("A leggere la mappa ci ha messo: " + cron.getTime());

        //printGrid(grid);

        //UndirectedGraph<Vector2, DefaultEdge> graph = null;
        UndirectedGraph<Vector2, DefaultEdge> graph = create(grid, walls, collisionDetector);

        log("Grafo creato: " + graph.toString());
        return graph;
    }

    private static UndirectedGraph<Vector2, DefaultEdge> create(Integer[][] grid, HashMap<Vector2, Vector2> walls, RaycastCollisionDetector<Vector2> collisionDetector) {
        UndirectedGraph<Vector2, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        addNodes(grid, graph, walls);
        log("Finiti i nodi, sono " + graph.vertexSet().size());
        addEdges(graph, collisionDetector);
        return graph;
    }

    private static void addEdges(UndirectedGraph<Vector2, DefaultEdge> graph,
                                 RaycastCollisionDetector<Vector2> collisionDetector) {
        addFirstsEdges(graph, collisionDetector);
        Cronometer cron = new Cronometer();
        cron.start();

        //checkUnconnectedNodes(graph, collisionDetector);
        concurrentCheckUnconnectedNodes(graph, collisionDetector);
        cron.stop();

        log("A controllare i nodi staccati ci ha messo: " + cron.getTime());

    }

    private static void concurrentCheckUnconnectedNodes(UndirectedGraph<Vector2, DefaultEdge> graph,
                                                        RaycastCollisionDetector<Vector2> collisionDetector) {
        int nProc = Runtime.getRuntime().availableProcessors()+1;
        ExecutorService executor = Executors.newFixedThreadPool(nProc);
        Set<Future<Void>> resultSet = new HashSet<>();
        int nTask = graph.vertexSet().size();
        int vectToThread = graph.vertexSet().size() / nTask;
        /*if(graph.vertexSet().size() % (float) nTask > 60)
            vectToThread++;*/
        //log("Resto modulo = " + graph.vertexSet().size() % (float) nTask);
        HashMap<Integer, List<Vector2>> ntc = new HashMap<>();
        int count = 0;
        ntc.put(count, new ArrayList<>());
        for(Vector2 node : graph.vertexSet()) {
            List<Vector2> nodesList = ntc.get(count);
            nodesList.add(node);
            if(nodesList.size() == vectToThread && count != (nTask-1)) {
                Future<Void> res = executor.submit(new ConcurrentAddEdges(nodesList, graph, collisionDetector, (count+1)));
                resultSet.add(res);
                count++;
                ntc.put(count, new ArrayList<>());
            }
        }
        Future<Void> res = executor.submit(new ConcurrentAddEdges(ntc.get(count), graph, collisionDetector, (count+1)));
        resultSet.add(res);

        for (Future<Void> future : resultSet) {
            try {
                future.get();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        log("Finito");
        executor.shutdown();
    }

    private static void checkUnconnectedNodes(UndirectedGraph<Vector2, DefaultEdge> graph,
                                              RaycastCollisionDetector<Vector2> collisionDetector) {
        KShortestPaths<Vector2, DefaultEdge> ksp = new KShortestPaths<>(graph, 1);
        float maxDist = 7f;
        graph.vertexSet().forEach(node ->{
            for(float x = node.x - maxDist; x <= node.x + maxDist; x++) {
                for(float y = node.y - maxDist; y <= node.y + maxDist; y++) {
                    Vector2 toCompare = createVector(x, y);
                    //log("Sto comparando " + node.toString() + " con " + toCompare.toString());
                    if(graph.containsVertex(toCompare)) {
                        if (!toCompare.equals(node) && ksp.getPaths(node, toCompare).size() == 0) {
                            //log(node.toString() + " non arriva a " + toCompare.toString());
                            if(checkEdgeRayCast(collisionDetector, node, toCompare, 0.5f, 16)) {
                                DefaultEdge edge = graph.addEdge(node, toCompare);
                                log("Secondi archi: aggiunto " + edge.toString());
                            }
                        }

                    }
                }
            }
        });
        log("Finiti secondi archi");
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
    private static boolean checkEdgeRayCast(RaycastCollisionDetector<Vector2> collisionDetector, Vector2 v1, Vector2 v2, float vertexRadius, int numRays) {

        final Vector2 tmp = new Vector2();
        final Vector2 tmp2 = new Vector2();
        final float step = 360.0f / numRays;

        for (float i = 0; i <= 360; i += step) {
            final double iRad = Math.toRadians(i);

            tmp.x = (float)(vertexRadius * Math.cos(iRad) + v2.x);
            tmp.y = (float)(vertexRadius * Math.sin(iRad) + v2.y);

            tmp2.x = (float)(vertexRadius * Math.cos(iRad) + v1.x);
            tmp2.y = (float)(vertexRadius * Math.sin(iRad) + v1.y);

            //log("Analisi: " + tmp2.toString() + " con " + tmp.toString());
            if (!collisionDetector.collides(new Ray<>(tmp2, tmp))) {
                //log(tmp2.toString() + " e " + tmp.toString() + " collidono");
                return true;
            }
        }

        return false;
    }

    private static void addFirstsEdges(UndirectedGraph<Vector2, DefaultEdge> graph, RaycastCollisionDetector<Vector2> collisionDetector) {
        List<Vector2> nodes = new ArrayList<>(graph.vertexSet());
        graph.vertexSet().forEach(vertex -> {
            nodes.forEach(node -> {
                //log("Controllo " + vertex.toString() + " - " + node.toString());
                if(vertex != node && checkNodeProximity(vertex, node)
                        && checkEdgeRayCast(collisionDetector, vertex, node, 0.5f, 16)) {
                    DefaultEdge edge = graph.addEdge(vertex, node);
                    //log("Primi archi: aggiunto " + edge.toString());
                }
            });
            nodes.remove(vertex);
        });
        log("Finiti primi archi");
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

    private static void concurrentReadMap(String mapFilename,
                                          HashMap<Vector2, Vector2> walls,
                                          Integer[][] grid) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
        Set<Future<List<Vector2>>> resultSet = new HashSet<>();
        int nTasks = 3600;

        FileHandle file = Gdx.files.internal(mapFilename);
        String text = file.readString();
        String[] lines = text.split("\\n");
        int linesForTask = lines.length / (nTasks+1);
        for(int l = 0; l < lines.length; l++) {
            List<String> tasksLines = new ArrayList<>();
            for (int i = 0; i < linesForTask; i++) {
                //String[] toks = lines[l].split(":");
                tasksLines.add(lines[i]);
                String[] toks = lines[l].split(":");
                float x = Float.parseFloat(toks[0]);
                float y = Float.parseFloat(toks[1]);
                float w = Float.parseFloat(toks[2]);
                float h = Float.parseFloat(toks[3]);
                walls.put(createVector(x, y), createVector(w, h));
            }
            Future<List<Vector2>> res = executor.submit(new ConcurrentSetWall(tasksLines, grid.length, grid[0].length));
            resultSet.add(res);
        }

        for(Future<List<Vector2>> future : resultSet) {
            try {
                for(Vector2 coord : future.get()) {
                    grid[(int) coord.x][(int) coord.y] = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void readMap(String mapFilename, HashMap<Vector2, Vector2> walls, Integer[][] grid) {
        FileHandle file = Gdx.files.internal(mapFilename);
        String text = file.readString();
        String[] lines = text.split("\\n");
        for(int l = 0; l < lines.length; l++) {
            String[] toks = lines[l].split(":");
            float x = Float.parseFloat(toks[0]);
            float y = Float.parseFloat(toks[1]);
            float w = Float.parseFloat(toks[2]);
            float h = Float.parseFloat(toks[3]);

            float halfw = w / 2;
            float halfh = h / 2;
            Vector2 bl = createVector((x - halfw), (y - halfh));
            Vector2 tr = createVector((x + halfw), (y + halfh));

            //new Thread(new SetWall(bl, tr, grid)).start();
            for(int i = (int) bl.x; i <= (int) tr.x && i < grid.length; i++) {
                for(int j = (int) bl.y; j <= tr.y && j < grid[0].length; j++ ) {
                    grid[i][j] = 1;
                }
            }
            walls.put(createVector(x, y), createVector(w, h));
        }
    }

    private static void addNodes(Integer[][] grid,
                          UndirectedGraph<Vector2, DefaultEdge> graph,
                          HashMap<Vector2, Vector2> walls) {
        addFirstNodes(grid, graph);
        addWallsNode(walls, grid, graph);
    }

    private static void addFirstNodes(Integer[][] grid, UndirectedGraph<Vector2, DefaultEdge> graph) {
        int step = 3;
        for(int row = (3); row < grid.length; row+=step) {
            for(int col = (3); col < grid[0].length; col+=step) {
                if(checkGrid(row, col, grid)) {
                    Vector2 v = createVector(row, col);
                    graph.addVertex(v);
                    //log("Primi nodi: " + v.toString());
                }
            }
        }
        log("Finiti primi nodi");
    }

    /**
     *
     * @param row index of row
     * @param col index of coloumn
     * @param grid the grid
     * @return true if cell of grid is empty
     * */
    private static boolean checkGrid(int row, int col, Integer[][] grid) {
        return grid[row][col] == null;
    }

    private static void addWallsNode(HashMap<Vector2, Vector2> walls, Integer[][] grid, UndirectedGraph<Vector2, DefaultEdge> graph) {
        walls.forEach((pos, size) -> {
            float dist = 1f;
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
                    //log("Secondi nodi: " + v.toString());
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
                    //log("Secondi nodi: " + v);
                }


            }
        });
        log("Finiti i nodi dei muri");
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

