package game.engine;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import game.map.MapProvider;

public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE_PREFIX = "save_";
    private static final String SAVE_FILE_EXT = ".dat";

    public static String calculateHash(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String getJarKey() {
        try {
            GameContext context = GameContext.getInstance();
            MapProvider mapProvider = context.getMapProvider();

            if (mapProvider == null) {
                return "dev_ver";
            }

            File jarFile = plugin.api.PluginManager.getInstance().getJarFileForMapProvider(mapProvider);

            if (jarFile.exists()) {
                return calculateHash(jarFile.getAbsolutePath());
            }

            return "TANKWAREX";
        } catch (Exception e) {
            System.err.println("[SaveManager] Failed to generate jar key: " + e.getMessage());
            return "TANKWAREX";
        }
    }

    private static String getSaveFilePath(String mapName) {
        String jarMD5 = getJarKey();
        String sanitizedMapName = mapName.replaceAll("[^a-zA-Z0-9_-]", "_");
        return SAVE_DIR + File.separator + SAVE_FILE_PREFIX + jarMD5 + "_" + sanitizedMapName + SAVE_FILE_EXT;
    }

    public static boolean hasSaveGame(String mapName) {
        if (mapName == null || mapName.isEmpty())
            return false;
        File saveFile = new File(getSaveFilePath(mapName));
        return saveFile.exists();
    }

    public static boolean hasSaveGame(GameContext context) {
        if (context.getMapProvider() == null)
            return false;
        return hasSaveGame(context.getMapProvider().getMapName());
    }

    public static void saveGame(GameContext context) throws Exception {
        if (context.getMapProvider() == null)
            throw new Exception("Cannot save game: MapProvider is null");

        String mapName = context.getMapProvider().getMapName();
        if (mapName == null || mapName.isEmpty())
            throw new Exception("Cannot save game: Map name is empty");

        File saveDir = new File(SAVE_DIR);
        if (!saveDir.exists())
            saveDir.mkdirs();

        String saveFilePath = getSaveFilePath(mapName);
        GameState state = context.getState();
        List<Object> entities = state.getEntities();

        // 只保存存活的实体
        List<Object> aliveEntities = new ArrayList<>();
        for (Object entity : entities) {
            if (entity instanceof Entity e) {
                if (e.isAlive()) {
                    aliveEntities.add(entity);
                }
            } else {
                aliveEntities.add(entity); // 非Entity对象也保存
            }
        }

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(saveFilePath)))) {
            dos.writeUTF("TANK");
            dos.writeInt(1);
            dos.writeUTF(getJarKey());
            dos.writeUTF(mapName);
            dos.writeLong(System.currentTimeMillis());

            dos.writeInt(aliveEntities.size());
            for (Object entity : aliveEntities) {
                writeEntity(dos, entity);
            }
        }
    }

    private static void writeEntity(DataOutputStream dos, Object entity) throws IOException {
        dos.writeUTF(entity.getClass().getName());

        Map<String, Object> fields = new HashMap<>();
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()))
                    continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value != null && isSupportedType(value)) {
                        fields.put(field.getName(), value);
                    }
                } catch (Exception e) {
                }
            }
            clazz = clazz.getSuperclass();
        }

        dos.writeInt(fields.size());
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            dos.writeUTF(entry.getKey());
            writeFieldValue(dos, entry.getValue());
        }
    }

    private static boolean isSupportedType(Object value) {
        return value instanceof Integer || value instanceof Double || value instanceof Long ||
                value instanceof Float || value instanceof Boolean || value instanceof String ||
                value instanceof UUID || value instanceof Enum;
    }

    private static void writeFieldValue(DataOutputStream dos, Object value) throws IOException {
        if (value instanceof Integer) {
            dos.writeByte(1);
            dos.writeInt((Integer) value);
        } else if (value instanceof Double) {
            dos.writeByte(2);
            dos.writeDouble((Double) value);
        } else if (value instanceof Long) {
            dos.writeByte(3);
            dos.writeLong((Long) value);
        } else if (value instanceof Float) {
            dos.writeByte(4);
            dos.writeFloat((Float) value);
        } else if (value instanceof Boolean) {
            dos.writeByte(5);
            dos.writeBoolean((Boolean) value);
        } else if (value instanceof String) {
            dos.writeByte(6);
            dos.writeUTF((String) value);
        } else if (value instanceof UUID) {
            dos.writeByte(7);
            dos.writeUTF(value.toString());
        } else if (value instanceof Enum) {
            dos.writeByte(8);
            dos.writeUTF(((Enum<?>) value).name());
        }
    }

    public static void loadGame(GameContext context) throws Exception {
        if (context.getMapProvider() == null)
            throw new Exception("Cannot load game: MapProvider is null");

        String mapName = context.getMapProvider().getMapName();
        String saveFilePath = getSaveFilePath(mapName);
        File saveFile = new File(saveFilePath);
        if (!saveFile.exists())
            throw new FileNotFoundException("Save file not found: " + saveFilePath);

        if (context.getEngine() != null) {
            context.getEngine().stop();
            context.getEngine().reloadContext();
        }

        context.reset();

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(saveFile)))) {
            String magic = dis.readUTF();
            if (!"TANK".equals(magic))
                throw new Exception("Invalid save file format");
            int version = dis.readInt();
            System.out.println("[SaveManager] Loading save file version " + version);
            String jarMD5 = dis.readUTF();
            if (!jarMD5.equals(getJarKey()))
                throw new Exception("Save file is from a different version");
            String savedMapName = dis.readUTF();
            if (!savedMapName.equals(mapName))
                throw new Exception("Save file is for a different map");
            long timestamp = dis.readLong();

            int entityCount = dis.readInt();
            List<Object> entities = new ArrayList<>();

            ClassLoader mapClassLoader = context.getMapProvider().getClass().getClassLoader();

            for (int i = 0; i < entityCount; i++) {
                Object entity = readEntity(dis, mapClassLoader);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            GameState state = context.getState();
            int shellCount = 0;
            int aliveShellCount = 0;
            for (Object entity : entities) {
                state.addEntity(entity);
            }

            System.out.println("[SaveManager] Game loaded: " + entities.size() + " entities restored (including " +
                    shellCount + " shells, " + aliveShellCount + " alive)");
            context.showToast("存档已加载: " + entities.size() + " 个实体, 时间: " + new Date(timestamp));
        }

        if (context.getMapProvider() != null) {
            context.getMapProvider().initResources(context);
        }

        game.engine.EventBus.getInstance().publish(new GameEvent("GameInitialized", null, "Game loaded"));
    }

    private static Object readEntity(DataInputStream dis, ClassLoader currentClassLoader) throws IOException {
        String className = dis.readUTF();
        Object entity = null;
        try {
            Class<?> clazz = loadClassFromPlugins(className, currentClassLoader);
            if (clazz != null) {
                entity = createInstance(clazz);
            }
        } catch (Exception e) {
        }

        int fieldCount = dis.readInt();
        Map<String, Object> fieldValues = new HashMap<>();

        for (int i = 0; i < fieldCount; i++) {
            String fieldName = dis.readUTF();
            Object value = readFieldValue(dis);
            fieldValues.put(fieldName, value);
        }

        if (entity != null) {
            setEntityFields(entity, fieldValues);
        }

        return entity;
    }

    private static Object readFieldValue(DataInputStream dis) throws IOException {
        byte type = dis.readByte();
        switch (type) {
            case 1:
                return dis.readInt();
            case 2:
                return dis.readDouble();
            case 3:
                return dis.readLong();
            case 4:
                return dis.readFloat();
            case 5:
                return dis.readBoolean();
            case 6:
                return dis.readUTF();
            case 7:
                return UUID.fromString(dis.readUTF());
            case 8:
                return dis.readUTF();
            default:
                return null;
        }
    }

    private static void setEntityFields(Object entity, Map<String, Object> fieldValues) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()))
                    continue;
                String name = field.getName();
                if (fieldValues.containsKey(name)) {
                    try {
                        field.setAccessible(true);
                        Object value = fieldValues.get(name);

                        if (field.getType().isEnum() && value instanceof String) {
                            @SuppressWarnings({ "unchecked", "rawtypes" })
                            Object enumValue = Enum.valueOf((Class<Enum>) field.getType(), (String) value);
                            field.set(entity, enumValue);
                        } else {
                            field.set(entity, value);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to set field " + name + " on " + entity.getClass().getName() + ": "
                                + e.getMessage());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static Class<?> loadClassFromPlugins(String className, ClassLoader currentClassLoader) {
        assert currentClassLoader != null;
        try {
            Class<?> clazz = Class.forName(className, true, currentClassLoader);
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static void deleteSaveGame(String mapName) {
        File saveFile = new File(getSaveFilePath(mapName));
        if (saveFile.exists()) {
            saveFile.delete();
            System.out.println("[SaveManager] Save file deleted: " + mapName);
        }
    }

    public static void deleteSaveGame(GameContext context) {
        if (context.getMapProvider() != null) {
            deleteSaveGame(context.getMapProvider().getMapName());
        }
    }

    public static String getSaveInfo(String mapName) {
        if (!hasSaveGame(mapName))
            return null;

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(getSaveFilePath(mapName))))) {
            String magic = dis.readUTF();
            if (!"TANK".equals(magic))
                return "Invalid Format";
            int version = dis.readInt();
            String jarMD5 = dis.readUTF();
            String savedMapName = dis.readUTF();
            long timestamp = dis.readLong();
            return "地图: " + savedMapName + "\n存档时间: " + new Date(timestamp) + "\n版本: "
                    + jarMD5.substring(0, Math.min(8, jarMD5.length())) + " (v" + version + ")";
        } catch (Exception e) {
            return "存档信息读取失败";
        }
    }

    public static String getSaveInfo(GameContext context) {
        if (context.getMapProvider() == null)
            return null;
        return getSaveInfo(context.getMapProvider().getMapName());
    }
}