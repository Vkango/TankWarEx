# TankWar<span style="color: red">Ex</span>

Java Course Project: 坦克大战重构版

[![Version](https://img.shields.io/badge/Version-1.0.0.re-blue.svg)](https://github.com/Vkango/TankWarEx)  [![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)  [![JavaFX](https://img.shields.io/badge/JavaFX-21.0.8-green.svg)](https://openjfx.io/)  [![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## 核心特性

- 🎮 **实时游戏循环**: 60FPS游戏逻辑，流畅的操作体验
- 🧵 **双线程架构**: UI线程与游戏线程分离，状态快照同步
- 🚀 **高性能渲染**: JavaFX Canvas GPU加速，全屏重绘
- 🔄 **对象池管理**: 炮弹和爆炸效果复用，减少GC压力

## 架构设计

游戏核心实际上仅为实体管理器. 真正的游戏内容全部在插件中.

### 实体系统 (插件实现)

```
GameEntity (抽象基类)
├── Tank (坦克)
├── Shell (炮弹)
└── Explosion (爆炸效果)
```

### 线程模型

```
UI线程:  键盘输入
           ↓
游戏线程: 处理输入 → 更新实体 → 物理模拟
           ↓
UI线程:  读取快照 → Canvas渲染 (AnimationTimer)
```

### 核心组件

- `GameEngine` - 实时游戏循环 (60FPS)
- `GameRenderer` - Canvas渲染器

## 快速开始

### 环境要求

- **Java**: JDK 17 或更高版本
- **JavaFX**: 21.0.8, 请[手动下载](https://gluonhq.com/products/javafx/)对应平台库取代占位文件.

### 编译项目

```bash
compile.bat
```

### 生成开发API

```bash
build_api.bat
```

### 运行游戏

```bash
run.bat
```

```java
// 创建事件数据
Map<String, Object> data = new HashMap<>();
data.put("key", "value");
// 发布事件
GameEvent event = new GameEvent("CustomEvent", data, "自定义事件描述");
EventBus.getInstance().publish(event);
```

## ⚖️ LICENSE

本项目采用 MIT 许可证.

---

**⭐ 如果这个项目对你有帮助, 请给它一个 Star！**
