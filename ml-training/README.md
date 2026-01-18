# AimAssist 机器学习训练框架

本框架用于收集、训练和部署基于机器学习的转头预测模型，以替代传统的硬编码瞄准算法。

## 目录结构

```
ml-training/
├── requirements.txt      # Python依赖
├── config.py            # 训练配置
├── dataset.py           # 数据集处理
├── model.py             # 神经网络模型
├── train.py             # 训练脚本
├── export_onnx.py       # ONNX导出
├── data/                # 放置训练数据
└── models/              # 保存训练好的模型
```

## 使用流程

### 1. 收集训练数据

1. 在游戏中启用 `AimDataCollector` 模块
2. 与其他玩家进行PVP战斗
3. 正常手动瞄准（不要使用AimAssist）
4. 数据会自动保存到 `.minecraft/foxsense/training_data/` 目录

**模块设置**:
- `Range`: 收集数据的范围（默认6格）
- `Only When Aiming`: 只在瞄准目标时收集
- `Auto Save`: 自动保存数据
- `Auto Save Interval`: 每多少样本自动保存

**建议**：收集至少10000个样本以获得较好的训练效果。

### 2. 准备训练环境

```powershell
cd ml-training
pip install -r requirements.txt
```

### 3. 复制训练数据

将 `.minecraft/foxsense/training_data/` 中的所有JSON文件复制到 `ml-training/data/` 目录。

### 4. 开始训练

```powershell
python train.py --model simple
```

**可选模型类型**:
- `simple`: 简单MLP模型（推荐）
- `residual`: 带残差连接的模型
- `lstm`: LSTM序列模型

### 5. 导出ONNX模型

```powershell
python export_onnx.py --model simple
```

导出的文件将保存在 `models/` 目录：
- `aim_model.onnx` - 模型文件
- `scaler_info.json` - 归一化参数
- `model_info.json` - 模型信息

### 6. 部署到游戏

1. 在 `.minecraft/foxsense/` 目录下创建 `ml/` 文件夹
2. 将以下文件复制到 `ml/` 文件夹：
   - `aim_model.onnx`
   - `scaler_info.json`

### 7. 在AimAssist中使用

在 `AimAssist.java` 中使用 `MLAimPredictor`：

```java
MLAimPredictor predictor = MLAimPredictor.getInstance();
if (predictor.initialize()) {
    AimFeatures features = new AimFeatures(...);
    AimOutput output = predictor.predict(features);
    // 使用 output.deltaYaw 和 output.deltaPitch
}
```

## 配置参数

编辑 `config.py` 可调整：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| HIDDEN_DIMS | [64, 32] | 隐藏层维度 |
| BATCH_SIZE | 64 | 批量大小 |
| LEARNING_RATE | 0.001 | 学习率 |
| EPOCHS | 100 | 训练轮数 |
| DROPOUT_RATE | 0.1 | Dropout比率 |

## 输入特征说明

| 特征 | 说明 |
|------|------|
| yaw | 当前玩家水平视角 |
| pitch | 当前玩家垂直视角 |
| deltaX/Y/Z | 目标相对位置 |
| distance | 与目标距离 |
| targetVelX/Y/Z | 目标速度 |
| playerVelX/Y/Z | 玩家速度 |
| deltaTime | 帧间隔时间 |

## 输出说明

| 输出 | 说明 |
|------|------|
| deltaYaw | 预测的水平视角变化 |
| deltaPitch | 预测的垂直视角变化 |

## 故障排除

**Q: 模型效果不好？**
- 收集更多训练数据
- 确保训练数据质量（真实PVP场景）
- 调整模型超参数

**Q: ONNX导出失败？**
- 确保PyTorch模型训练成功
- 检查 `models/` 目录是否存在 `.pt` 文件

**Q: 游戏中加载模型失败？**
- 确认 `.minecraft/foxsense/ml/` 目录存在
- 检查 `aim_model.onnx` 文件是否正确放置
