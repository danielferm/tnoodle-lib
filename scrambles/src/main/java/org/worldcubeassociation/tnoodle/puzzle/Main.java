package org.worldcubeassociation.tnoodle.puzzle;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/GetScramble", new RequestHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");
    }

    static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestURI = t.getRequestURI();
            String query = requestURI.getQuery();
            Map<String, String> parameters = parseQuery(query);
            String puzzleType = parameters.getOrDefault("cubetype", "Unknown");

            Puzzle puzzle;
            switch(puzzleType) {
                case "2x2": puzzle = new TwoByTwoCubePuzzle(); break;
                case "3x3": puzzle = new ThreeByThreeCubePuzzle(); break;
                case "4x4": puzzle = new FourByFourCubePuzzle(); break;
                case "5x5": puzzle = new CubePuzzle(5); break;
                case "6x6": puzzle = new CubePuzzle(6); break;
                case "7x7": puzzle = new CubePuzzle(7); break;
                case "Pyraminx": puzzle = new PyraminxPuzzle(); break;
                case "Megaminx": puzzle = new MegaminxPuzzle(); break;
                case "Skewb": puzzle = new SkewbPuzzle(); break;
                case "Square-1": puzzle = new SquareOnePuzzle(); break;
                case "Clock": puzzle = new ClockPuzzle(); break;
                default: throw new RuntimeException("Unknown puzzle type: " + puzzleType);
            }

            String scramble = puzzle.generateScramble();
            String svg = "";
            try {
                svg = puzzle.drawScramble(scramble, null).toString();
            } catch (InvalidScrambleException e) {
                e.printStackTrace();
            }
            String response = scramble + "|" + svg;

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1) {
                        result.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return result;
        }
    }
}
