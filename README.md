# VoiceRobot

<div align="center">

![Language](https://img.shields.io/badge/language-Kotlin-blue)
![Android](https://img.shields.io/badge/platform-Android%2024+-green)
![Gradle](https://img.shields.io/badge/build-Gradle%20KTS-yellow)

基于 **Volc Dialog SDK** 的AI语音对话应用，提供实时语音识别、智能对话、自定义动画驱动等功能。

[快速开始](#-快速开始) • [技术栈](#-技术栈) • [项目展示](#-项目展示) • [贡献](#-贡献指南)

</div>

---

## 📱 项目介绍

**VoiceRobot** 是一个 Kotlin 编写的模块化 Android 应用，集成字节跳动 Volc 语音对话引擎，实现实时语音交互、智能对话管理和丰富的自定义动画效果。项目采用清晰的模块化架构，支持离线/在线语音识别与合成。

### ✨ 核心特性

- 🎙️ **实时语音识别** - 基于 Volc Dialog SDK 的实时语音转文本  
- 🔊 **智能语音合成** - 高保真语音输出，支持多种语调  
- 💬 **多轮对话管理** - 完整的上下文维护与消息管理  
- 🎨 **自定义动画系统** - 动态渐变背景 + 三球波形实时驱动 + 机器人状态 Lottie 动画
- ⚡ **Kotlin Coroutines** - Flow 驱动的响应式编程  
- 🔐 **安全配置** - 支持 local.properties 动态加载敏感配置

---

## 📸 项目展示

### 机器人三种状态 Lottie 动画

机器人根据对话状态展示不同的 Lottie 动画，通过丰富的肢体语言增强交互感受：

| 待机状态 | 聆听思考 | 回复应答 |
|:---:|:---:|:---:|
| ![待机](https://github.com/user-attachments/assets/157496e9-9d91-4650-b575-e10f5d0b45c3) | ![聆听思考](https://github.com/user-attachments/assets/8316db80-ecd9-4210-aea3-788eb1a7ca10) | ![回复](https://github.com/user-attachments/assets/9e4bc7b8-213e-4917-80e0-3a533aac7024) |

**状态切换流程**：
```
待机 → 用户开始说话 → 聆听思考 → AI 生成回复 → 回复应答 → 语音合成完成 → 待机
```

**Lottie 动画特性**：
- ✅ 矢量动画，分辨率无关，适配所有屏幕
- ✅ 文件体积小，加载快速流畅
- ✅ 支持循环播放和状态转换
- ✅ 与 MainViewModel 联动，自动驱动状态变化

---

## 🏗️ 项目结构

```
VoiceRobot/
├── app/                    # 应用层（UI + 业务逻辑）
│   ├── src/main/java/com/voicerobot/
│   │   ├── MainActivity    # 主界面入口，权限申请、UI 绑定
│   │   ├── VoiceRobotApp   # Application，依赖注入容器
│   │   ├── di/AppContainer # 轻量级 DI（未引入 Hilt）
│   │   ├── ui/
│   │   │   ├── robot/      # Robot UI，MainViewModel，动画驱动
│   │   │   └── chat/       # 聊天气泡，ChatAdapter，ChatMessage
│   │   └── audio/          # 音频振幅读取器
│   └── src/main/res/       # 布局、资源、drawable
│       └── layout/activity_main.xml  # GradientBackgroundView + WaveformView
│
├── voiceengine/            # 语音引擎模块（SDK 封装层）
│   ├── api/                # 公开接口
│   │   └── VoiceEngineRepository  # 语音操作的抽象
│   ├── impl/               # Volc Dialog SDK 实现
│   │   └── VolcDialogVoiceEngineRepository
│   ├── model/              # 数据模型
│   │   └── VoiceConfig     # 认证与配置信息
│   └── build.gradle.kts    # 依赖：Volc SDK, Coroutines, OkHttp
│
├── core/                   # 核心库（通用功能）
│   ├── domain/
│   │   └── Result<T>       # 泛型结果包装类（Success/Error/Loading）
│   └── ... 其他工具类
│
├── lottie/                 # 动画与 UI 组件库
│   ├── widget/
│   │   ├── GradientBackgroundView    # 动态渐变背景视图
│   │   └── WaveformView              # 三球波形动画视图
│   ├── AgentAnimMapper     # 动画阶段映射
│   └── res/                # 矢量资源、Lottie JSON
│
├── build.gradle.kts        # 插件声明（无依赖）
├── settings.gradle.kts     # 模块配置 + Volc Maven 仓库
└── gradle/                 # Gradle wrapper
```

### 模块职责

| 模块 | 语言 | 职责 | 关键依赖 |
|------|------|------|--------|
| **app** | Kotlin | UI 布局、权限、MainViewModel、聊天列表 | Lottie 6.4.0, Lifecycle, AndroidX UI |
| **voiceengine** | Kotlin | Volc SDK 封装、Flow 驱动的语音管理 | Volc Dialog SDK 0.0.14.1, Coroutines, OkHttp |
| **core** | Kotlin | Result<T> 泛型、工具函数、通用数据结构 | AndroidX Core |
| **lottie** | Kotlin | 自定义动画视图、渐变背景、波形动画 | Lottie 6.4.0, ConstraintLayout |

---

## 🛠️ 技术栈

### 编译环境
- **Language**: Kotlin 2.x
- **Target SDK**: Android 36（基于最新 API）
- **Min SDK**: Android 24（API 24+）
- **JDK**: Java 11+
- **Gradle**: 8.x（KTS 脚本）
- **IDE**: Android Studio Otter (2025.2.1 Patch 1)

### 核心依赖

#### 语音引擎
- **Volc Dialog SDK** `0.0.14.1-bugfix` - 字节跳动语音识别/合成 SDK
  - Maven 仓库：`https://artifact.bytedance.com/repository/Volcengine/`

#### UI & 动画
- **Lottie** `6.4.0` - 矢量动画库，支持 JSON 动画资源加载
- **AndroidX AppCompat** - Material Design 支持
- **ConstraintLayout** - 现代布局框架

#### 异步 & 响应式
- **Kotlin Coroutines** `1.7.3`
  - `kotlinx-coroutines-core` - 核心库
  - `kotlinx-coroutines-android` - Android 集成
- **AndroidX Lifecycle** `2.7.0`
  - `lifecycle-viewmodel-ktx` - ViewModel
  - `lifecycle-runtime-ktx` - lifecycleScope

#### 网络
- **OkHttp** `4.9.1` - HTTP 客户端（Volc SDK 依赖）

### 配置管理
敏感信息（Volc App ID / Key / Token）通过 `local.properties` 注入，避免硬编码：

```properties
VOLC_APP_ID=your_app_id
VOLC_APP_KEY=your_app_key
VOLC_ACCESS_TOKEN=your_access_token
```

---

## 🚀 快速开始

### 前置要求

1. **Android Studio**: Otter (2025.2.1 Patch 1) 或更新版本
2. **JDK**: Java 11 或以上
3. **Gradle**: 8.0+（已包含 wrapper）
4. **Volc 账户**: 获取 App ID、App Key、Access Token
   - [字节跳动 Volcengine 平台](https://console.volcengine.com/)

### 编译步骤

#### 1️⃣ 克隆项目
```bash
git clone https://github.com/qingfeng19491001/VoiceRobot.git
cd VoiceRobot
```

#### 2️⃣ 配置 Volc 凭证
在项目根目录创建 `local.properties`：

```properties
VOLC_APP_ID=xxx
VOLC_APP_KEY=xxx
VOLC_ACCESS_TOKEN=xxx
```

#### 3️⃣ 编译项目
```bash
# 使用 Gradle wrapper
./gradlew build

# 或使用 Android Studio 内置编译
```

#### 4️⃣ 运行应用
```bash
# 真机调试（推荐）
./gradlew installDebug

# 或通过 Android Studio：Run → Run 'app'
```

### 权限声明
应用需要以下权限（已在 `AndroidManifest.xml` 中声明）：

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />         <!-- 麦克风 -->
<uses-permission android:name="android.permission.INTERNET" />             <!-- 网络 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- 网络状态 -->
```

应用启动时会动态申请 `RECORD_AUDIO` 权限，用户同意后才能启动语音识别。

---

## 🎨 动画系统详解

VoiceRobot 采用三层动画设计：Lottie 动画 + 动态渐变背景 + 波形实时驱动，实现沉浸式交互体验。

### 1. 机器人状态 Lottie 动画

**职责**：驱动机器人的情感表达，反映对话状态

**三种状态**：
- **待机**：用户未发言
- **聆听思考**：用户发言中
- **回复应答**：回复用户发言

**特性**：
- 矢量动画，秒级加载
- 与 MainViewModel 自动联动
- 平滑状态转换，无视觉割裂

---

### 2. GradientBackgroundView（动态渐变背景）

**职责**：提供全屏流动渐变背景，烘托语音交互的沉浸感

**实现原理**：
- **驱动方式**：ValueAnimator + Canvas 绘制 + LinearGradient + Matrix 平移
- **核心技术**：利用 Matrix 的 `setLocalMatrix()` 实现渐变色的连续平移效果
- **颜色方案**：粉色（#FF1493） ↔ 蓝色（#1E90FF）的流动渐变
- **动画特性**：
  - 颜色平滑过渡，无闪烁感
  - 帧回调驱动，60 FPS 高帧率
  - 可配置动画强度 (`gbvIntensity`) 和滑动速度 (`gbvSpeedPxPerSec`)

**使用方式**：
```xml
<!-- activity_main.xml -->
<com.voicerobot.lottie.widget.GradientBackgroundView
    android:id="@+id/gradientBg"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:gbvIntensity="1.0"
    app:gbvSpeedPxPerSec="100" />
```

**核心代码**：
```kotlin
// 初始化渐变
val gradient = LinearGradient(
    0f, 0f, width.toFloat(), 0f,
    intArrayOf(Color.parseColor("#FF1493"), Color.parseColor("#1E90FF")),
    floatArrayOf(0f, 1f),
    Shader.TileMode.CLAMP
)
paint.shader = gradient

// 帧回调实现平移
choreographer.postFrameCallback(object : Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
        val matrix = Matrix().apply {
            setTranslate(offsetX, 0f)
        }
        gradient.setLocalMatrix(matrix)
        invalidate()
        choreographer.postFrameCallback(this)
    }
})
```

---

### 3. WaveformView（三球波形动画）

**职责**：实时驱动三个球的跳动，可视化语音振幅，反映对话状态

**实现原理**：
- **驱动方式**：Choreographer 帧回调 + Canvas 绘制 + 三角函数波形
- **核心数学**：
  - **正弦波生成**：使用 `sin()` 函数生成平滑波形曲线，驱动球的上下运动
  - **Bézier 曲线**：计算球的弧形轨迹，增强动画的流畅感
  - **振幅映射**：实时振幅值（0..1）动态改变球的弹跳高度和半径

**视觉效果**：
- **待机状态**：三个小球轻微呼吸，节奏缓慢，表示空闲准备状态
- **讲话状态**：三个球依次上下跳动，振幅跟随语音实时变化，形成生动的波形可视化

**使用方式**：
```xml
<!-- activity_main.xml -->
<com.voicerobot.lottie.widget.WaveformView
    android:id="@+id/waveformView"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:layout_gravity="center" />
```

```kotlin
// 推送实时振幅（0..1）
waveformView.pushAmplitude01(amplitudeValue)
```

**球的运动公式**：
```kotlin
// 正弦波驱动
val baseY = centerY
val waveAmplitude = amplitude * maxBallRadius // 基础半径随振幅变化
val phase = (i * 2 * Math.PI / 3).toFloat() // 三球相位差 120°

val ballY = baseY - sin(elapsedTime + phase) * waveAmplitude
val ballRadius = minBallRadius + amplitude * (maxBallRadius - minBallRadius)

// 绘制球
canvas.drawCircle(ballX, ballY, ballRadius, paint)
```

**可配置参数**：
- `minBallRadius`：最小球半径（待机状态）
- `maxBallRadius`：最大球半径（高振幅状态）
- `waveFrequency`：波形频率（控制跳动速度）
- `phaseShift`：三球间的相位差，决定跳动节奏

---

## 💡 核心工作流

### 应用启动流程

```
VoiceRobotApp.onCreate()
    └─> AppContainer(app)
        └─> VolcDialogVoiceEngineRepository(VoiceConfig)

MainActivity.onCreate()
    └─> MainViewModel(voiceEngineRepository)
        ├─> 权限申请
        ├─> amplitudeReader.start()      // 音频振幅读取
        ├─> vm.startIfNeeded()           // 启动语音引擎
        └─> UI 流订阅
            ├─> uiState.collect()        // 驱动 Lottie + 自定义动画
            ├─> chatMessages.collect()   // 更新聊天列表
            └─> amplitude.onEach()       // 驱动 WaveformView
```

### 语音交互流程

```
用户说话
    ├─> AudioAmplitudeReader 读取振幅 (0..1)
    ├─> WaveformView 实时可视化波形
    ├─> GradientBackgroundView 背景色响应式流动
    ├─> Volc Dialog SDK 识别文本
    ├─> MainViewModel 处理识别结果
    ├─> ChatAdapter 展示气泡（用户 & 机器人）
    ├─> Volc SDK 合成语音回复
    └─> Lottie 动画驱动机器人表情（待机→聆听→回复→待机）
```

---

## 📂 关键类说明

### MainViewModel (`app/ui/robot/`)
**职责**：驱动 UI 状态、管理语音生命周期、协调动画与语音引擎

**核心状态**：
- `uiState: StateFlow<MainUiState>` - 动画阶段、运行状态、实时振幅
- `chatMessages: StateFlow<List<ChatMessage>>` - 聊天消息列表
- `amplitude: Flow<Float>` - 实时振幅流，驱动 WaveformView

**核心方法**：
- `startIfNeeded()` - 启动语音引擎
- `onUserMessage(text)` - 处理用户输入文本

---

### VoiceEngineRepository (`voiceengine/api/`)
**职责**：语音引擎的抽象接口，隔离 SDK 实现细节

**实现**：`VolcDialogVoiceEngineRepository` 基于 Volc SDK

**核心方法**：
- `start()` / `stop()` - 生命周期控制
- `sendText(text)` - 发送文本（启动对话）
- `amplitude: Flow<Float>` - 实时振幅流（0..1）

---

### GradientBackgroundView (`lottie/widget/`)
**职责**：动态渐变背景，全屏流动色彩效果

**驱动方式**：ValueAnimator + Canvas + LinearGradient + Matrix

**可配置属性**：
- `gbvIntensity: Float` - 动画强度（0..1）
- `gbvSpeedPxPerSec: Float` - 渐变滑动速度（像素/秒）

**特性**：自动驱动，无需外部推送数据

---

### WaveformView (`lottie/widget/`)
**职责**：三球波形动画，实时驱动振幅可视化

**驱动方式**：Choreographer 帧回调 + 三角函数波形

**API**：`pushAmplitude01(value: Float)` - 推送 0..1 的振幅值

---

### ChatAdapter (`app/ui/chat/`)
**职责**：RecyclerView 适配器，渲染聊天气泡

**特性**：
- USER 气泡靠右（蓝色背景）
- BOT 气泡靠左（灰色背景）
- DiffUtil 高效增量更新

---

## 📊 核心数据流

```
Volc SDK
  ├─> 实时振幅值
  │   └─> MainViewModel (amplitude Flow)
  │       ├─> WaveformView.pushAmplitude01()    // 三球波形驱动
  │       └─> uiState.collect()                 // UI 更新
  │
  └─> 识别结果 + 合成结果
      └─> MainViewModel (chatMessages Flow)
          └─> ChatAdapter (RecyclerView 更新)

GradientBackgroundView
  └─> 自驱动（ValueAnimator）
      └─> Canvas 绘制 + 背景平移
```

---

## 🎯 使用场景

VoiceRobot 是一款**AI语音对话应用**，提供自然流畅的语音交互体验。

主要应用场景包括：
- 💬 **日常聊天** - 与AI进行多轮对话，获取信息和建议
- 📝 **语音笔记** - 边说边记，AI自动整理内容
- 🎓 **知识问答** - 快速语音提问，即时语音反馈
- 🗣️ **语言学习** - 语音对话练习，纠正发音

---

## 🗺️ 开发路线图

- [x] 基础架构搭建（模块化分层）
- [x] Volc SDK 集成与封装
- [x] 语音识别/合成流程
- [x] MainViewModel 与 UI 绑定
- [x] GradientBackgroundView 动态渐变动画
- [x] WaveformView 三球波形实时驱动
- [x] 聊天列表 UI 与气泡样式
- [x] 机器人状态 Lottie 动画
- [ ] 上下文管理与多轮对话优化
- [ ] 离线语音识别（Volc 离线模型）
- [ ] 个性化语音包加载机制
- [ ] 单元测试与集成测试
- [ ] 性能优化（内存、功耗）
- [ ] 国际化（i18n）支持
- [ ] API 文档与使用示例

---

## 🤝 贡献指南

欢迎 Issue、PR 和讨论！

### 提交 Issue
- **Bug 报告**：描述现象、复现步骤、预期结果、实际结果、日志
- **功能建议**：说明使用场景、期望效果、参考方案

### 提交 PR
1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/YourFeature`
3. 提交更改：`git commit -m 'Add YourFeature'`
4. 推送：`git push origin feature/YourFeature`
5. 开启 Pull Request，描述改动内容

### 代码规范
- **Kotlin**：遵循 [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Android**：遵循 [Google Android Code Style](https://developer.android.com/kotlin/style-guide)
- **Commit Message**：英文，简洁清晰（e.g., `Fix: correct WaveformView amplitude calculation`）

---

## 📄 许可证

本项目采用 **MIT 许可证** - 详见 [LICENSE](./LICENSE) 文件

```
MIT License

Copyright (c) 2026 qingfeng19491001

Permission is hereby granted, free of charge, to any person obtaining a copy...
```

---

## 📞 联系方式

- 📧 **Issues**：[GitHub Issues](https://github.com/qingfeng19491001/VoiceRobot/issues)
- 💬 **Discussions**：[GitHub Discussions](https://github.com/qingfeng19491001/VoiceRobot/discussions)
- 👤 **作者**：[@qingfeng19491001](https://github.com/qingfeng19491001)

---

## 🙏 致谢

- [字节跳动 Volcengine](https://www.volcengine.com/) - 语音引擎支持
- [Airbnb Lottie](https://airbnb.design/lottie/) - 动画库支持
- [AndroidX 团队](https://developer.android.com/jetpack/androidx) - Jetpack 库支持
- [Kotlin 官方](https://kotlinlang.org/) - 语言与协程库

---

<div align="center">

[⬆ 回到顶部](#voicerobot)

**⭐ 如果觉得项目有帮助，请给个 Star！**

Made with ❤️ by [qingfeng19491001](https://github.com/qingfeng19491001)

</div>
