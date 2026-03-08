# 📦 资源复制机（Resource Replicator）功能分类文档

[English](#english) | [中文](#中文)

---

# **English**

## 1️⃣ Item Replicator

**Function**: Replicates any item (can be restricted via blacklist/whitelist configuration)  
**Tiers**: T1 - T5

| Tier | Output per Operation | Duration                |
|------|----------------------|-------------------------|
| T1   | 4 items              | 20 ticks (1 second)     |
| T2   | 16 items             | 15 ticks (0.75 seconds) |
| T3   | 32 items             | 10 ticks (0.5 seconds)  |
| T4   | 64 items             | 5 ticks (0.25 seconds)  |
| T5   | 128 items            | 1 tick (0.05 seconds)   |

**Features**:
- Configurable output slot count (default: 1)
- Blacklist/Whitelist filtering support (by item ID, mod ID, item tags)
- Admin item protection (command blocks, bedrock, etc. cannot be replicated)
- Destruction function (optional): When enabled, items input via pipes/hoppers are instantly destroyed, but items manually placed by players are preserved normally

---

## 2️⃣ Fluid Replicator

**Function**: Replicates any fluid (can be restricted via blacklist/whitelist configuration)  
**Tiers**: T1 - T5

| Tier | Output per Operation | Duration | Special Fluid Output              |
|------|----------------------|----------|-----------------------------------|
| T1   | 1000 mB              | 20 ticks | Water: 1000 mB, Lava: 10 mB       |
| T2   | 2500 mB              | 15 ticks | Water: 10000 mB, Lava: 50 mB      |
| T3   | 5000 mB              | 10 ticks | Water: 100000 mB, Lava: 100 mB    |
| T4   | 10000 mB             | 5 ticks  | Water: 1000000 mB, Lava: 500 mB   |
| T5   | 25000 mB             | 1 tick   | Water: 10000000 mB, Lava: 1000 mB |

**Features**:
- Dual-tank system (input tank + output tank)
- Configurable output tank capacity (default: 100000 mB)
- Blacklist/Whitelist filtering support (by fluid ID, mod ID, fluid tags)
- Destruction function (optional): When enabled, fluids input via pipes are instantly destroyed, but fluids inserted by players using buckets (right-click) are preserved normally

---

## 3️⃣ Chemical Replicator (Mekanism Integration)

**Function**: Replicates Mekanism mod chemicals  
**Tiers**: T1 - T5

| Tier | Output per Operation | Duration |
|------|----------------------|----------|
| T1   | 10 mB                | 20 ticks |
| T2   | 50 mB                | 15 ticks |
| T3   | 100 mB               | 10 ticks |
| T4   | 500 mB               | 5 ticks  |
| T5   | 1000 mB              | 1 tick   |

**Features**:
- Dual-tank system (input tank + output tank)
- Configurable output tank capacity (default: 8000 mB)
- Blacklist/Whitelist filtering support (by chemical ID, mod ID, chemical tags)
- Destruction function (optional): When enabled, chemicals input via pipes are instantly destroyed, but chemicals inserted by players using tanks (right-click) are preserved normally
- Requires Mekanism mod to be installed

---

## ⚙️ Configuration System

The mod provides a comprehensive configuration file (`resource_replicator-server.toml`) with all parameters customizable:

### Item Replicator Configuration
- Output slot count (1-9)
- Destruction function toggle
- Blacklist/Whitelist mode switch
- Blacklist/Whitelist item list
- Output quantities and durations for each tier

### Fluid Replicator Configuration
- Output tank capacity
- Destruction function toggle
- Blacklist/Whitelist mode switch
- Blacklist/Whitelist fluid list
- Output quantities, special fluid (water/lava) outputs, and durations for each tier

### Chemical Replicator Configuration
- Output tank capacity
- Destruction function toggle
- Blacklist/Whitelist mode switch
- Blacklist/Whitelist chemical list
- Output quantities and durations for each tier

---

## 🔧 Technical Features

### Intelligent Recognition System
- Player vs Automation: Distinguishes between player right-click operations and pipe/hopper automation through stack trace technology
- Selective Destruction: Only destroys when destruction function is enabled and input comes from pipes, protecting manually inserted resources

### Data Synchronization
- Real-time client-server data synchronization
- Automatic notification to nearby players when chunks update
- Complete NBT data preservation (data persists after world restart)

### API Compatibility
- Supports NeoForge Capabilities system
- Compatible with Mekanism's IChemicalHandler interface
- Supports item/fluid tag filtering

---

## 🔐 Security Mechanisms

- Admin Protection: Items that disrupt game balance such as command blocks, structure blocks, and bedrock cannot be replicated
- Mod Item Protection: Resource Replicator's own machines and items cannot be replicated (prevents infinite loops)
- Dual Blacklist/Whitelist Protection: Precise control over which resources can be replicated through configuration

---

# **中文**

## 1️⃣ 物品复制机

**功能**：复制任意物品（可通过黑白名单配置限制）  
**等级**：T1 - T5

| 等级 | 每次产出  | 耗时              |
|----|-------|-----------------|
| T1 | 4 个   | 20 tick（1 秒）    |
| T2 | 16 个  | 15 tick（0.75 秒） |
| T3 | 32 个  | 10 tick（0.5 秒）  |
| T4 | 64 个  | 5 tick（0.25 秒）  |
| T5 | 128 个 | 1 tick（0.05 秒）  |

**特性**：
- 支持配置输出槽数量（默认 1 个）
- 支持黑白名单过滤（可指定物品 ID、模组 ID、物品标签）
- 管理员物品保护（命令方块、基岩等无法复制）
- 销毁功能（可选）：启用后管道/漏斗输入的物品会被瞬间销毁，但玩家手动放入的物品正常保存

---

## 2️⃣ 流体复制机

**功能**：复制任意流体（可通过黑白名单配置限制）  
**等级**：T1 - T5

| 等级 | 每次产出     | 耗时      | 特殊流体产出                   |
|----|----------|---------|--------------------------|
| T1 | 1000 mB  | 20 tick | 水：1000 mB，岩浆：10 mB       |
| T2 | 2500 mB  | 15 tick | 水：10000 mB，岩浆：50 mB      |
| T3 | 5000 mB  | 10 tick | 水：100000 mB，岩浆：100 mB    |
| T4 | 10000 mB | 5 tick  | 水：1000000 mB，岩浆：500 mB   |
| T5 | 25000 mB | 1 tick  | 水：10000000 mB，岩浆：1000 mB |

**特性**：
- 双罐系统（输入罐 + 输出罐）
- 输出罐容量可配置（默认 100000 mB）
- 支持黑白名单过滤（流体 ID、模组 ID、流体标签）
- 销毁功能（可选）：启用后管道输入的流体会被瞬间销毁，但玩家手持桶右键放入的流体正常保存

---

## 3️⃣ 化学品复制机（Mekanism 联动）

**功能**：复制 Mekanism 模组的化学品  
**等级**：T1 - T5

| 等级 | 每次产出    | 耗时      |
|----|---------|---------|
| T1 | 10 mB   | 20 tick |
| T2 | 50 mB   | 15 tick |
| T3 | 100 mB  | 10 tick |
| T4 | 500 mB  | 5 tick  |
| T5 | 1000 mB | 1 tick  |

**特性**：
- 双罐系统（输入罐 + 输出罐）
- 输出罐容量可配置（默认 8000 mB）
- 支持黑白名单过滤（化学品 ID、模组 ID、化学品标签）
- 销毁功能（可选）：启用后管道输入的化学品会被瞬间销毁，但玩家手持储罐右键放入的化学品正常保存
- 需要安装 Mekanism 模组才能使用

---

## ⚙️ 配置系统

模组提供完善的配置文件（`resource_replicator-server.toml`），所有参数均可自定义：

### 物品复制机配置
- 输出槽数量（1-9）
- 销毁功能开关
- 黑名单/白名单模式切换
- 黑名单/白名单物品列表
- 各等级的输出数量和耗时

### 流体复制机配置
- 输出罐容量
- 销毁功能开关
- 黑名单/白名单模式切换
- 黑名单/白名单流体列表
- 各等级的输出数量、特殊流体（水/岩浆）输出量和耗时

### 化学品复制机配置
- 输出罐容量
- 销毁功能开关
- 黑名单/白名单模式切换
- 黑名单/白名单化学品列表
- 各等级的输出数量和耗时

---

## 🔧 技术特性

### 智能识别系统
- 玩家操作 vs 自动化：通过栈追踪技术区分玩家右键操作和管道/漏斗自动化操作
- 选择性销毁：只在启用销毁功能且是管道输入时才销毁，保护玩家手动操作的资源

### 数据同步
- 客户端 - 服务端数据实时同步
- 区块更新时自动通知附近玩家
- NBT 数据完整保存（重启世界后数据不丢失）

### API 兼容
- 支持 NeoForge 的 Capabilities 系统
- 兼容 Mekanism 的 IChemicalHandler 接口
- 支持物品/流体的 Tag 过滤

---

## 🔐 安全机制

- 管理员保护：命令方块、结构方块、基岩等破坏游戏平衡的物品无法复制
- 本模组物品保护：资源复制机 自身的机器和物品无法被复制（防止无限套娃）
- 黑白名单双重保障：可以通过配置精确控制哪些资源可以被复制

---