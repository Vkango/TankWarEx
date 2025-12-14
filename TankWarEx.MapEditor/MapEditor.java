import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class MapEditor extends JPanel {
    private static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 20;
    private static final int MAP_HEIGHT = 20;

    private char[][] map;
    private int cursorX = 0;
    private int cursorY = 0;
    private Map<Character, TileType> tileTypes;

    public MapEditor() {
        setPreferredSize(new Dimension(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE));
        setFocusable(true);

        // 初始化地图
        map = new char[MAP_HEIGHT][MAP_WIDTH];
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = '0';
            }
        }

        tileTypes = new HashMap<>();
        tileTypes.put('1', new TileType('1', "Base", new Color(200, 200, 180)));
        tileTypes.put('2', new TileType('2', "BrickWall", new Color(180, 80, 60)));
        tileTypes.put('3', new TileType('3', "Grass", new Color(50, 180, 50)));
        tileTypes.put('4', new TileType('4', "Reflector", new Color(220, 220, 100)));
        tileTypes.put('5', new TileType('5', "SteelWall", new Color(120, 120, 140)));
        tileTypes.put('6', new TileType('6', "Water", new Color(50, 100, 200)));
        tileTypes.put('7', new TileType('7', "CReflector", new Color(250, 100, 200)));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char key = e.getKeyChar();
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_UP && cursorY > 0)
                    cursorY--;
                if (code == KeyEvent.VK_DOWN && cursorY < MAP_HEIGHT - 1)
                    cursorY++;
                if (code == KeyEvent.VK_LEFT && cursorX > 0)
                    cursorX--;
                if (code == KeyEvent.VK_RIGHT && cursorX < MAP_WIDTH - 1)
                    cursorX++;

                if (code == KeyEvent.VK_0) {
                    map[cursorY][cursorX] = '0';
                } else if (tileTypes.containsKey(key)) {
                    map[cursorY][cursorX] = key;
                }

                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                char tile = map[y][x];
                if (tile != '0') {
                    TileType type = tileTypes.get(tile);
                    if (type != null) {
                        g.setColor(type.getColor());
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    }
                }

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
            }
        }

        g.setColor(Color.YELLOW);
        g.drawRect(cursorX * TILE_SIZE, cursorY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        g.drawRect(cursorX * TILE_SIZE + 1, cursorY * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2);
    }

    public String exportToXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<map width=\"").append(MAP_WIDTH).append("\" height=\"").append(MAP_HEIGHT).append("\">\n");

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                char tile = map[y][x];
                if (tile != '0') {
                    TileType type = tileTypes.get(tile);
                    if (type != null) {
                        xml.append("  <tile x=\"").append(x)
                                .append("\" y=\"").append(y)
                                .append("\" type=\"").append(type.getName())
                                .append("\" id=\"").append(tile)
                                .append("\" />\n");
                    }
                }
            }
        }

        xml.append("</map>");
        return xml.toString();
    }

    private static class TileType {
        private char id;
        private String name;
        private Color color;

        public TileType(char id, String name, Color color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }

        public char getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TankWarEx - MapEditor");
        MapEditor editor = new MapEditor();

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JLabel infoLabel = new JLabel("<html>使用方向键移动光标<br>按0清空, 数字/字母放置方块</html>");
        controlPanel.add(infoLabel);

        JButton exportBtn = new JButton("导出XML");
        exportBtn.addActionListener(e -> {
            String xml = editor.exportToXML();
            JTextArea textArea = new JTextArea(xml, 15, 30);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(frame, scrollPane, "导出的XML", JOptionPane.INFORMATION_MESSAGE);
        });
        controlPanel.add(new JLabel(" "));
        controlPanel.add(exportBtn);

        frame.setLayout(new BorderLayout());
        frame.add(editor, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.EAST);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}