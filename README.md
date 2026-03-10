# 📦 资源复制机（Resource Replicator）功能分类文档

[English](#english) | [中文](#中文)

---

# **English**

Place the items (or fluids, chemicals) to be duplicated into the block. As long as FE energy is supplied, the block will begin outputting the same resources. At the same time, the block's appearance will change in real-time to display what is currently being duplicated.

## 1.Item Replicator

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
- Right-click with an item to place it; sneak right-click with an empty hand to remove the placed item.
- Supports blacklist/whitelist. By default, mod items and admin items are added to the blacklist. Supports item ID/mod ID/tag.
- Configurable processing speed and output multiplier for each tier. Default output amounts: T1: 4/20t, T2: 16/15t, T3: 32/10t, T4: 64/5t, T5: 128/1t.
- Configurable destruction function. Items input via pipes will be destroyed, with destruction speed depending on the pipe's speed. Disabled by default.
- Configurable number of output slots, with one cache slot by default.
- Configurable automatic output. When enabled, items will be automatically output to containers on one adjacent face.
- Configurable energy consumption.

## 2.Fluid Replicator

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
- Right-click with a fluid bucket to place the fluid; right-click with an empty bucket to remove all fluid inside.
- Supports blacklist/whitelist. Supports item ID/mod ID/tag.
- Configurable processing speed and output multiplier for each tier. Default output amounts: T1: 1K/20t, T2: 2.5K/15t, T3: 5K/10t, T4: 10K/5t, T5: 25K/1t.
- Configurable special output for water, with unchanged tick rate and output amounts of 1K, 10K, 100K, 1M, 10M.
- Configurable special output for lava, with unchanged tick rate and output amounts of 10, 50, 100, 500, 1K.
- Configurable destruction function. Fluids input via pipes will be destroyed, with destruction speed depending on the pipe's speed. Disabled by default.
- Configurable output tank buffer size, default is 8000mB.
- Configurable automatic output. When enabled, fluids will be automatically output to containers on one adjacent face.
- Configurable energy consumption.

## 3.Chemical Replicator (Mekanism Integration)

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
- Right-click with a tank containing chemicals to place them; right-click with an empty tank to remove all chemicals inside.
- Supports blacklist/whitelist. Supports item ID/mod ID.
- Configurable processing speed and output multiplier for each tier. Default output amounts: T1: 10/20t, T2: 50/15t, T3: 100/10t, T4: 500/5t, T5: 1K/1t.
- Configurable destruction function. Chemicals input via pipes will be destroyed, with destruction speed depending on the pipe's speed. Disabled by default.
- Configurable output tank buffer size, default is 8000mB.
- Configurable energy consumption.

---

# **中文**

## 📖 模组简介

将需要复制的物品（或流体、化学品）放入方块。只要为其提供FE能量，方块便会开始向外输出相同的资源。同时，方块的外观会实时变化，以显示当前正在复制的内容。

## 1.物品复制机

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
- 手持物品右键放入，空手潜行右键清除放入的物品；
- 支持黑白名单，默认添加模组物品和管理员物品为黑名单，支持物品ID/模组ID/tag；
- 可配置每个等级的处理速度和产量倍率，默认输出数量：T1：4/20t，T2：16/15t，T3：32/10t，T4：64/5t，T5：128/1t。
- 可配置销毁功能，通过管道输入的物品会被销毁，销毁速度取决于管道的速度，默认关闭；
- 可配置输出槽数量，默认为一个缓存槽；
- 可配置是否自动输出，启用后向周围一个面的容器自动输出物品；
- 可配置能量消耗。

## 2.流体复制机

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
- 手持流体桶右键放入，手持空桶右键清除内部所有流体；
- 支持黑白名单，支持物品ID/模组ID/tag；
- 可配置每个等级的处理速度和产量倍率，默认输出数量：T1：1K/20t，T2：2.5K/15t，T3：5K/10t，T4：10K/5t，T5：25K/1t。
- 可配置水特殊输出，tick不变，输出量为1K、10K、100K、1M、10M；
- 可配置岩浆特殊输出，tick不变，输出量为10、50、100、500、1K；
- 可配置销毁功能，通过管道输入的流体会被销毁，销毁速度取决于管道的速度，默认关闭；
- 可配置输出罐缓存大小，默认为8000mB；
- 可配置是否自动输出，启用后向周围一个面的容器自动输出流体；
- 可配置能量消耗。

## 3.化学品复制机（Mekanism 联动）

**功能**：复制任意化学品（可通过黑白名单配置限制）  
**等级**：T1 - T5

| 等级 | 每次产出    | 耗时      |
|----|---------|---------|
| T1 | 10 mB   | 20 tick |
| T2 | 50 mB   | 15 tick |
| T3 | 100 mB  | 10 tick |
| T4 | 500 mB  | 5 tick  |
| T5 | 1000 mB | 1 tick  |

**特性**：
- 手持装有化学品的储罐右键放入，手持空储罐右键清除内部所有化学品；
- 支持黑白名单，支持物品ID/模组ID；
- 可配置每个等级的处理速度和产量倍率，默认输出数量：T1：10/20t，T2：50/15t，T3：100/10t，T4：500/5t，T5：1K/1t。
- 可配置销毁功能，通过管道输入的化学品会被销毁，销毁速度取决于管道的速度，默认关闭；
- 可配置输出罐缓存大小，默认为8000mB；
- 可配置能量消耗。

---