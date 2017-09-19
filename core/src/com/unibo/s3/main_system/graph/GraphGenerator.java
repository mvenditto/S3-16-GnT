package com.unibo.s3.main_system.graph;

import akka.actor.ActorRef;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory;
import com.unibo.s3.main_system.communication.GeneralActors;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.game.GameSettings;
import com.unibo.s3.main_system.game.Wall;
import com.unibo.s3.main_system.map.AbstractMapGenerator;
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
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class GraphGenerator {

    private static void log(String msg) {
        System.out.println("[GRAPH GENERATOR] " + msg);
    }

    private static void printGrid(Integer[][] grid) {
        log("Griglia: ");
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

    public static UndirectedGraph<Vector2, DefaultEdge> createGraph(int width, int height, String mapFilename) {
        int dimWall = Wall.WALL_THICKNESS();
        log("genero il grafo di dimensione: " + width + ", " + height);

        ActorRef worldActor = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR());
        RaycastCollisionDetector<Vector2> collisionDetector = new Box2dProxyDetectorsFactory(worldActor).newRaycastCollisionDetector();
        HashMap<Vector2, Vector2> walls = new HashMap<>();
        Integer[][] grid = new Integer[width+(dimWall*2)][height+(dimWall*2)];
        Cronometer cron = new Cronometer();

        cron.start();

        readMap(mapFilename, walls, grid);
        //concurrentReadMap(mapFilename, walls, grid);
        cron.stop();

        log("A leggere la mappa ci ha messo: " + cron.getTime());

        //printGrid(grid);

        //UndirectedGraph<Vector2, DefaultEdge> graph = null;
        UndirectedGraph<Vector2, DefaultEdge> graph = create(grid, walls, collisionDetector, dimWall);


        log("Grafo creato: " + graph.toString());
        return graph;
    }

    private static UndirectedGraph<Vector2, DefaultEdge> create(Integer[][] grid, HashMap<Vector2, Vector2> walls,
                                                                RaycastCollisionDetector<Vector2> collisionDetector, int dimWall) {
        UndirectedGraph<Vector2, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        addNodes(grid, graph, walls, dimWall);
        //log("Finiti i nodi, sono " + graph.vertexSet().size());
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

        //log("A controllare i nodi staccati ci ha messo: " + cron.getTime());

    }

    private static void concurrentCheckUnconnectedNodes(UndirectedGraph<Vector2, DefaultEdge> graph,
                                                        RaycastCollisionDetector<Vector2> collisionDetector) {
        Semaphore semGraph = new Semaphore(1);
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

        //log("Finito");
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
                            if(checkEdgeRayCast(collisionDetector, node, toCompare)) {
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
     * @return true se almeno un raggio da v1 raggiunge v2 (non il punto v2 preciso,
     * ma un punto sulla circonferenza di centro v2 e raggio vertexRadius)
     */
    private static boolean checkEdgeRayCast(RaycastCollisionDetector<Vector2> collisionDetector, Vector2 v1, Vector2 v2) {

        //raggio del vertice da considerare per castare il raggio
        float vertexRadius = 0.5f;
        //numero di raggi da utilizzare
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
                        && checkEdgeRayCast(collisionDetector, vertex, node)) {
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

        FileHandle file = Gdx.files.local(mapFilename);
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
        FileHandle file = Gdx.files.local(mapFilename);
        String text = file.readString();
        //log("File = " + text);
        String[] lines = text.split("\\n");
        boolean print = false;
        for(int l = 0; l < lines.length; l++) {
            if(print) System.out.print("Riga: ");
            String[] toks = lines[l].split(":");
            float x = Float.parseFloat(toks[0]);
            if(print) System.out.print("x = " + x);
            float y = Float.parseFloat(toks[1]);
            if(print) System.out.print(", y = " + y);
            float w = Float.parseFloat(toks[2]);
            if(print) System.out.print(", width = " + w);
            float h = Float.parseFloat(toks[3]);
            if(print) System.out.print(", height = " + h);

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

            if(print) log("=> metto a 1:");
            for(int i = startX; i <= (int) tr.x && i < grid.length; i++) {
                for(int j = startY; j <= tr.y && j < grid[0].length; j++ ) {
                    grid[i][j] = 1;
                    if(print) System.out.print(i + ", " + j + " - ");
                }
            }
            if(print) log("");
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

