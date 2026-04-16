# Budget Diary Android 项目说明（增强版）

这是一个原生 Android 项目，技术栈为 **Kotlin + Jetpack Compose**。

目前已经实现：

- 月预算设置
- 饮食预算范围、活动资金范围
- 一键为整个月随机生成每日预算
- 新增消费记录（金额、标签、日期、时间、备注）
- 标签自定义
- 月历可视化显示每日预算与总消费
- 颜色区分是否超支
- 点击某一天查看当天明细，并删除单条记录
- 统计页：预算使用率、超支天数、分类消费、消费最高日
- 本地持久化存储（SharedPreferences + JSON）

---

## 一、推荐环境

- Android Studio 最新稳定版
- JDK 17
- Android SDK Platform 36
- Android SDK Build-Tools 35 或更高
- Gradle 8.13
- Android Gradle Plugin 8.13.2
- Kotlin 2.3.10

---

## 二、导入项目

### 方式 A：Android Studio 直接打开

1. 打开 Android Studio
2. 选择 **Open**
3. 选中 `BudgetDiary` 项目根目录
4. 等待 Gradle Sync
5. 如果 Android Studio 提示缺少 SDK，按提示安装：
   - Android SDK Platform 36
   - Android SDK Build-Tools
   - Android Emulator（需要模拟器时）
6. 如果没有自动生成 `local.properties`，把 `local.properties.example` 复制为 `local.properties`，并填入本机 `sdk.dir`
7. 点击运行按钮，选择模拟器或真机

### 方式 B：新建空项目后覆盖

如果你的 Android Studio 对这个项目的 Gradle 初始化不顺利，可以这样做：

1. 在 Android Studio 新建一个 **Empty Activity / Empty Compose Activity** 项目
2. 包名改成 `com.example.budgetdiary`
3. `minSdk` 设为 26 或更高
4. 用本压缩包中的这些文件覆盖新项目对应文件：
   - 根目录 `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
   - `app/build.gradle.kts`
   - `app/src/main/...`
5. 再执行 Gradle Sync

---

## 三、主要目录结构

- `app/src/main/java/com/example/budgetdiary/MainActivity.kt`
  - 主界面与主要逻辑
- `app/src/main/java/com/example/budgetdiary/ui/theme/`
  - Compose 主题文件
- `app/src/main/AndroidManifest.xml`
  - 应用清单
- `app/build.gradle.kts`
  - App 模块配置
- `build.gradle.kts`
  - 项目级 Gradle 配置

---

## 四、当前功能说明

### 1. 预算页
- 设置饮食预算最小值/最大值
- 设置活动资金最小值/最大值
- 点击“保存预算”保存范围
- 点击“生成日预算”后，会给本月每天生成一个随机预算

### 2. 标签管理
- 在预算页下半部分可以新增自定义标签
- 例如：电影、社团、学习、旅游、恋爱、礼物
- 新增后的标签会自动出现在记账页的标签下拉框中
- 可删除自定义标签

### 3. 记账页
- 输入金额
- 选择标签
- 输入日期和时间
- 可填写备注
- 点击“填入当前时间”可快速使用当前时间
- 点击“保存记录”完成记账

### 4. 日历页
- 灰色：尚未生成预算
- 绿色：未超支
- 红色：已超支
- 点击某天可以查看：
  - 饮食预算
  - 活动预算
  - 总预算
  - 当日消费
  - 当日剩余 / 超出金额
  - 明细记录

### 5. 统计页
- 总预算
- 总消费
- 预算使用率
- 超支天数
- 本月消费最高日
- 各标签消费柱状进度条

---

## 五、建议你接下来继续扩展的功能

如果你准备把它当成长期自用 APP，建议下一步按这个顺序升级：

### 第一优先级
- 真正的日期选择器 / 时间选择器
- 编辑记录
- 搜索和筛选记录
- 首页显示“今天预算”和“今天已花”

### 第二优先级
- Room 数据库替代 SharedPreferences
- DataStore 保存设置项
- 导出 CSV / Excel
- 月度 / 年度趋势图

### 第三优先级
- 开机提醒 / 每晚预算提醒通知
- 深色模式优化
- Material 3 动效与图标完善
- 应用图标和启动页
- 签名打包发布 APK

---

## 六、常见问题

### 1. Sync 失败，提示 JDK 版本不对
把 Android Studio 的 Gradle JDK 改成 17。

### 2. 提示找不到 SDK
在 Android Studio 的 SDK Manager 中安装对应 SDK，并检查 `local.properties`。

### 3. 模拟器很卡
优先使用真机调试；或者减少模拟器分辨率并确认虚拟化已开启。

### 4. 命令行没有 `gradlew`
这个压缩包主要面向 Android Studio 导入运行。若你需要完整命令行包装器，可在本机装好 Gradle 后执行：

```bash
gradle wrapper --gradle-version 8.13
```

然后再使用：

```bash
./gradlew assembleDebug
```

### 5. 为什么现在还没用数据库？
当前版本优先保证“导入即运行、逻辑清楚、便于你继续改”。
如果直接上 Room，会更正式，但项目复杂度也会明显上升。

---

## 七、下一步我建议怎么做

你现在最适合继续做的是下面两条之一：

1. **升级成更正式的本地存储版**：我帮你改成 `Room + ViewModel + Repository` 架构。
2. **直接生成可安装 APK 的完整流程版**：我继续给你补签名配置、debug/release 打包步骤、真机安装步骤。

