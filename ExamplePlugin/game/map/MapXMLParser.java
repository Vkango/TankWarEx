package game.map;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import game.engine.GameState;
import game.map.entities.*;

public class MapXMLParser {

    private int gridSize = 40;
    private Document doc;
    private Map<String, TileCreator> tileCreators;

    public MapXMLParser() {
        tileCreators = new HashMap<>();
        tileCreators.put("1", (x, y, state) -> state.addEntity(new BaseStructure(x * gridSize, y * gridSize, 0)));
        tileCreators.put("2", (x, y, state) -> state.addEntity(new BrickWallTile(x * gridSize, y * gridSize)));
        tileCreators.put("3", (x, y, state) -> state.addEntity(new GrassEntity(x * gridSize, y * gridSize)));
        tileCreators.put("4", (x, y, state) -> state.addEntity(new Reflector(x * gridSize, y * gridSize)));
        tileCreators.put("5", (x, y, state) -> state.addEntity(new SteelWallTile(x * gridSize, y * gridSize)));
        tileCreators.put("6", (x, y, state) -> state.addEntity(new WaterTile(x * gridSize, y * gridSize)));
        tileCreators.put("7", (x, y, state) -> state.addEntity(new CReflector(x * gridSize, y * gridSize)));
    }

    public void placeNodes(GameState state) {
        try {
            NodeList tileList = doc.getElementsByTagName("tile");
            for (int i = 0; i < tileList.getLength(); i++) {
                Element tileElement = (Element) tileList.item(i);
                int gridX = Integer.parseInt(tileElement.getAttribute("x"));
                int gridY = Integer.parseInt(tileElement.getAttribute("y"));
                String tileId = tileElement.getAttribute("id");
                TileCreator creator = tileCreators.get(tileId);
                if (creator != null) {
                    creator.create(gridX, gridY, state);
                } else {
                    System.out.println("Warning: Unknown tile type '" + tileId + "'");
                }
            }
            System.out.println("Map loaded successfully: " + tileList.getLength() + " tiles");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public int[] loadMapFromXML(String xmlFile, int gridSize) {
        try {
            this.gridSize = gridSize;
            File file = new File(xmlFile);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList mapList = doc.getElementsByTagName("map");
            Element mapNode = (Element) mapList.item(0);
            if (mapNode == null) {
                throw new Exception("Invalid map file: Missing <map> node");
            }
            int[] dimensions = new int[2];
            dimensions[0] = Integer.parseInt(mapNode.getAttribute("width"));
            dimensions[1] = Integer.parseInt(mapNode.getAttribute("height"));
            return dimensions;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while loading map: " + e.getMessage());
        }
        return null;
    }

    @FunctionalInterface
    public interface TileCreator {
        void create(int gridX, int gridY, GameState state);
    }

}