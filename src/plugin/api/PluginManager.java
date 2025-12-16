package plugin.api;

import game.map.MapProvider;
import game.rules.RuleProvider;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginManager {
    private static PluginManager instance;
    private final List<MapProvider> mapProviders = new ArrayList<>();
    private final List<RuleProvider> ruleProviders = new ArrayList<>();
    private final List<ClassLoader> pluginClassLoaders = new ArrayList<>();
    private final java.util.Map<ClassLoader, File> classLoaderToJarMap = new java.util.HashMap<>();

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void clear() {
        for (ClassLoader loader : pluginClassLoaders) {
            // 跳过系统加载器，它不是 URLClassLoader，也不需要关闭
            if (loader instanceof URLClassLoader urlLoader && loader != ClassLoader.getSystemClassLoader()) {
                try {
                    urlLoader.close();
                } catch (IOException e) {
                    System.err.println("Failed to close ClassLoader: " + e.getMessage());
                }
            }
        }
        mapProviders.clear();
        ruleProviders.clear();
        pluginClassLoaders.clear();
        classLoaderToJarMap.clear();

        pluginClassLoaders.add(ClassLoader.getSystemClassLoader());
        classLoaderToJarMap.put(ClassLoader.getSystemClassLoader(), null); // System loader, no JAR file
        loadServices(ClassLoader.getSystemClassLoader(), false);

    }

    public void loadPlugins() {

        File pluginDir = new File("plugins");
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles != null) {
            for (File file : jarFiles) {
                try {
                    URL url = file.toURI().toURL();
                    System.out.println("Loading plugin jar: " + file.getName());
                    URLClassLoader pluginLoader = new URLClassLoader(new URL[] { url },
                            ClassLoader.getSystemClassLoader());
                    pluginClassLoaders.add(pluginLoader);
                    classLoaderToJarMap.put(pluginLoader, file);
                    loadServices(pluginLoader, true);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Loaded " + mapProviders.size() + " MapProviders.");
        System.out.println("Loaded " + ruleProviders.size() + " RuleProviders.");
    }

    private void loadServices(ClassLoader classLoader, boolean checkClassLoader) {
        ServiceLoader<MapProvider> maps = ServiceLoader.load(MapProvider.class, classLoader);
        for (MapProvider map : maps) {
            if (checkClassLoader && map.getClass().getClassLoader() != classLoader) {
                continue;
            }
            mapProviders.add(map);
            System.out.println("Loaded MapProvider: " + map.getMapName() + " (" + map.getClass().getName() + ")");
        }

        ServiceLoader<RuleProvider> rules = ServiceLoader.load(RuleProvider.class, classLoader);
        for (RuleProvider rule : rules) {
            if (checkClassLoader && rule.getClass().getClassLoader() != classLoader) {
                continue;
            }
            ruleProviders.add(rule);
            System.out.println("Loaded RuleProvider: " + rule.getClass().getName());
        }
    }

    public List<MapProvider> getMapProviders() {
        return mapProviders;
    }

    public List<RuleProvider> getRuleProviders() {
        return ruleProviders;
    }

    public MapProvider getMapProvider(String name) {
        for (MapProvider mp : mapProviders) {
            if (mp.getMapName().equals(name)) {
                return mp;
            }
        }
        return null;
    }

    public RuleProvider getRuleProviderForMap(MapProvider mapProvider) {
        if (mapProvider == null) {
            return ruleProviders.isEmpty() ? null : ruleProviders.get(0);
        }
        ClassLoader mapClassLoader = mapProvider.getClass().getClassLoader();

        for (RuleProvider rp : ruleProviders) {
            if (rp.getClass().getClassLoader() == mapClassLoader) {
                return rp;
            }
        }

        return null;
    }

    public List<ClassLoader> getPluginClassLoaders() {
        return new ArrayList<>(pluginClassLoaders);
    }

    public File getJarFileForMapProvider(MapProvider mapProvider) {
        if (mapProvider == null) {
            return null;
        }
        ClassLoader classLoader = mapProvider.getClass().getClassLoader();
        return classLoaderToJarMap.getOrDefault(classLoader, null);
    }

}
