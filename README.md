# TankBattle (from AeroplaneChess<span style="color: red">Ex</span>)

Java Course Project: 坦克大战游戏 (基于飞行棋框架重构)

[![Version](https://img.shields.io/badge/Version-1.0.0.re-blue.svg)](https://github.com/Vkango/TankWarEx)  [![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)  [![JavaFX](https://img.shields.io/badge/JavaFX-21.0.8-green.svg)](https://openjfx.io/)  [![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## 核心特性

- 🎮 **实时游戏循环**: 60FPS游戏逻辑，流畅的操作体验
- 🧵 **双线程架构**: UI线程与游戏线程分离，状态快照同步
- 🚀 **高性能渲染**: JavaFX Canvas GPU加速，全屏重绘
- ⚡ **空间哈希优化**: O(n)碰撞检测
- 🔄 **对象池管理**: 炮弹和爆炸效果复用，减少GC压力
- � **事件驱动**: 保留事件总线架构

## 游戏玩法

**操作方式:**
- **WASD** - 控制坦克移动
- **Space** - 发射炮弹

**游戏规则:**
- 2名玩家同时游戏 (一名可控，一名AI/静止)
- 炮弹击中敌方坦克造成25点伤害
- 坦克血量归零后被摧毁
- 最后存活的玩家获胜

## 架构设计

### 实体系统
```
GameEntity (抽象基类)
├── Tank (坦克)
├── Shell (炮弹)
└── Explosion (爆炸效果)
```

### 线程模型
```
UI线程:  键盘输入 → BlockingQueue
           ↓
游戏线程: 处理输入 → 更新实体 → 物理模拟 → 生成快照
           ↓
UI线程:  读取快照 → Canvas渲染 (AnimationTimer)
```

### 核心组件
- `GameEngine` - 实时游戏循环 (60FPS)
- `GameWorld` - 实体管理器
- `PhysicsEngine` - 碰撞检测引擎
- `GameRenderer` - Canvas渲染器
- `EntityPool` - 对象池

## 快速开始

### 环境要求

- **Java**: JDK 17 或更高版本
- **JavaFX**: 21.0.8, 请[手动下载](https://gluonhq.com/products/javafx/)对应平台库取代占位文件.

### 编译项目

```bash
compile.bat
```

### 运行游戏

```bash
run.bat
```

## 技术亮点

1. **状态快照模式** - 不可变快照实现无锁跨线程通信
2. **空间哈希** - 将世界划分为格子，加速碰撞检测
3. **对象池** - Shell/Explosion实体复用
4. **组件化设计** - InputComponent处理玩家输入
5. **GPU加速** - Canvas全重绘利用硬件加速
import game.engine.EventBus;
import java.util.HashMap;

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
