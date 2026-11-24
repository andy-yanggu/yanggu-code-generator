# tree.vue v1 功能点实录（模板组「单体java项目模板_复制」）

> 本文是设计文档 `2026-11-24-template-tree-split-design.md` 功能基线清单（F01–F30）的 **v1 实际行为快照**，作为 T2 阶段 v1/v2 对照的基准表。
> 对照开发时，v2 逐条复现本表行为；任何与本表不符的 v2 表现，除「预期差异」列外，一律判 v2 缺陷。

## 一、取证范围与前提

- **被测对象**：v1 `src/views/gen/template/tree.vue`（1508 行，全程冻结未改动）。
- **模板组**：`单体java项目模板_复制`（项目模板类型），变量目录 **288 项**。
- **环境**：前端 `http://localhost:5000`，后端 `8888`，已登录「张三」；路由 `#/gen/template-group`。
- **取证工具**：browser-use MCP（13 工具）。**能力边界**：无右键、无拖拽、无 `evaluate_script`；`click` 支持 `dblClick`。
- **数据完整性声明**：取证过程在 pom.xml 头部产生过破坏性未保存编辑（`${projectName}` 前缀），收尾严格按 F02「否(N)」分支丢弃（测试子抽屉一次、主抽屉一次），**全程未点「是(Y)」或「保存」，数据库零变更**。最终浏览器状态回到列表页、抽屉关闭。

### 取证方式图例

| 标记 | 含义 |
|------|------|
| ① 实测 | browser-use 可自动化触发并截图/snapshot 取证 |
| ② 源码推导 | 右键/拖拽/键盘/特定数据态，browser-use 无法触发，从 v1 冻结源码提取预期行为，**待手动核对** |

---

## 二、F01–F30 逐项实录

### A. 抽屉生命周期

#### F01 打开入口 — ① 实测（2026-11-24 v1/v2 双跑 S0）
- **观察**：列表行「模板」按钮打开全屏抽屉，标题为「模板配置（单体java项目模板_复制）」，带组名。
- **锚点**：`template-group/index.vue` L183-188。
- **v2 对照要点**：标题拼接、`init(row.id)` 时机一致。
- **双跑结论**：`F01_title=2BD8AAE8_19`、`F01_hasGroupName=Y` 两版逐字相同；同段顺带取证的初始态（`tabCount0=1`、`active0=2891`、`expanded0=9`、`treeDots0=0`、`save0=dis`、`search0=[]`、`varEdge=变量288`）也全等 → S0 行为串 `fpBehav=FE999DBB`/len=425 两版一致。

#### F02 关闭确认（三态） — ① 实测（2026-11-24 v1/v2 双跑 S2/S3/S4）
- **观察**：存在未保存编辑时，关闭动作触发 `MessageBox` 三态确认（`distinguishCancelAndClose`）。实测在 **两处** 均出现：
  - **测试子抽屉**关闭：提示「pom.xml已修改未保存，是否保存后再关闭？」（**不带**「模板文件」）。
  - **主抽屉**关闭（tree.vue `handleClose`）：提示「pom.xml模板文件已修改未保存，是否保存后再关闭？」（**带**「模板文件」，由 `joinFileName` 产生）。
  - 按钮：`是(Y)`=批量 `updateContentList` 后关；`否(N)`=丢弃直接关；`关闭(X)`=留在抽屉。
- **关键陷阱**：`take_screenshot` 会捕捉 MessageBox 淡出动画，确认框看似仍在但实际已处理——须用 `take_snapshot` 核实真实状态。
- **锚点**：L683-733。
- **v2 对照要点**：两处文案差异（带/不带「模板文件」）须原样保留；三态分支行为一致。
- **双跑结论**：原列为**预期差异项**的「两处文案」实测**未发生差异**，v2 原样保留了这一不对称：
  - 主抽屉·单文件 `59C5D1A4_29`（`hasTplWord=Y`）、测试子抽屉·单文件 `23893E55_25`（`hasTplWord=N`）——两串长度差恰为 4，即「模板文件」四字，成对断言 `pairCheck=Y / pairLenDelta=4` 两版相同；
  - 主抽屉·多脏折叠 `2BD72644_61`（`hasEtc=Y` + `hasTplWord=Y`）、主抽屉·双文件名拼接 `D26BF258_45`（`names=Y`）、批量关闭 `52832574_45`、单 tab 释放 `49583903_35`（与 F06 R suite 的确认文案哈希**同一个值**，交叉印证读的是同一条 `joinFileName` 链路）；
  - 三态按钮恒为 `x,否(N),是(Y)`，`distinguishCancelAndClose` 语义两版一致；F02 全段并入 S4 串 `fpBehav=91013E7D`/len=1665，两版逐字相同。
- **结论修订**：F02 从「预期差异项」清单移出，预期差异只保留 F10/F15/F29（ContextMenu 三项）。

#### F03 重复打开复位 — ① 实测（2026-11-24 v1/v2 双跑 S4）
- **实测（原推导逐条兑现）**：4 个文件全部制造脏态（`dirtySet=4`）后关抽屉丢弃，重开得到 `tabs=95053EDA_15 / n=1 / active=2891 / exp=9 / search=[] / dots=0 / tabDots=0 / edge=变量288` —— tabList 与激活位回落初始、展开态恢复 9 项、搜索词清空、变量面板回落收起、红点全部归零。
- **内容侧复位**：`F03_contentBack=2891:Y,2142:Y,2865:Y,2136:Y`，四个文件的编辑器内容与基线哈希逐一相符（丢弃未污染、重开从库重取）。
- **锚点**：L576-614。
- **双跑结论**：上述两项两版逐字相同（同在 S4 串 `91013E7D` 内）。红点归零同时也把 K6 的脏标记残留一并冲掉——丢弃后残留的 `isEdited` 只在**本次会话内**有效，重开抽屉即由 `init` 重新拉数据覆盖。

### B. 树区

#### F04 树搜索 — ① 实测（v1/v2 双跑，2026-11-24）
- **观察**：搜索框输入实时 `filter`，占位文案「请输入目录/文件名称」；清空恢复全树。
- **实测细化（EP el-tree 过滤语义，两版逐字段一致）**：
  - 命中节点 + 其**全部祖先**可见，非命中的兄弟整棵隐藏；祖先目录被 EP **自动展开**（组 80 实测 DOM 节点 23 → 34，`is-expanded` 追加 `2141(vo)`/`2149(mapstruct)`），但该展开只改 EP 内部状态、**不写回 `expandedKeys`**，故点「刷新」后回到原展开态（子孙回到 DOM 却 `display:none`）。
  - 命中**目录名**时只显示目录本身，其子文件因不匹配被隐藏（搜 `entity` → vis=9，`1111.java`/`BaseUserVO.java` 隐藏），要再看内容须手动展开。
  - `node.fileName.includes(value)` **大小写敏感**：`BaseUserVO` 命中 10 个节点，`baseuser` 命中 0 个。
  - 只有空串等于「不过滤」（`if (!value) return true`）；单个空格是合法子串 → 0 命中，此时 `el-tree` 的 `#empty` 插槽可见（实测 empty=1）。
  - **过滤不影响勾选**：先勾 `BaseUserVO.java(2142)` 再搜 `PageVO` 把它过滤掉，隐藏节点仍保持 checked、「删除」按钮仍 `disabled=false` → `getCheckedKeys()` 里带着看不见的 id。已登记为共有缺陷 **K5**（设计文档「对照期已知共有缺陷」），与 K1/K2 同属删除链路的风险叠加项。
- **取证方式**：同一份 Playwright 探针脚本分别跑 v1/v2（选择器统一 `.el-drawer:visible`，不依赖 `.tree-panel`），9 场景 × 8 字段（search / dom+vis 计数 / 可见序列含文件名 / 隐藏 key 序列 / expanded / checked / 删除按钮 disabled / empty）拼成 17 行指纹串：`len=5940`、FNV-1a=`18efdf94`，**两版相同**；全程仅 1 条 `GET /gen/template/tree?templateGroupId=80`，零写入、零新增 console error。
- **锚点**：v1 L735-750；v2 `tree-panel.vue` L148-159。搜索词复位：v1 在 `init` L581 与 `refreshTree` L635 各清一次（后者冗余）；v2 收敛为 `resetSearch()` L384-386，由容器 `tree-v2.vue` 的 `init()` L157 调用，`refreshTree()` L192 经 `init()` 覆盖 —— 路径等价，实测 q9 两版均「搜索框清空 + 展开态回落 + 勾选清空」。
- **v2 对照要点**：无 `render-after-expand`/`default-expand-all`，两版 el-tree props 逐条对应（v1 L29-48 / v2 L17-37），过滤行为由 EP 内部实现决定，v2 未引入任何过滤侧改动。

#### F05 单击节点 — ① 实测（2026-11-24 v1/v2 双跑 N1）
- **实测**：单击文件节点（type≠0）→ `activeItemId` 赋值 + `tabPush`，tab 数 1→2、激活位为该文件、节点挂 `is-current`、编辑器 `scrollIntoView` 到位（`inView=Y / scrollTop=0`，内容 `cmLen=927 / cmHash=78D5E7DE_400`）；已在 tab 的文件再点只切激活（`tabs 2>2 / activeSwitch=Y`）。
- **旧记纠正**：原写「目录节点仅切勾选」不成立——实测单击目录 `checkedFlip=N / checkedTotal 0>0`（勾选完全不动）、`expand Y>N`（只翻展开）、tab 与激活位不变。源码根因：L36 只设了 `:check-on-click-leaf="false"`，**未配 `check-on-click-node`**（EP 默认 false），所以点目录不存在任何勾选通路；两版同构。
- **零写入**：`writeReq=NONE`；行为串 `3125E167`/len=312 两版逐字相同。
- **已知不足**：当时选中的目录 2117 已处于展开态，只覆盖到折叠方向。
- **锚点**：L753-759、L330-354、L36。

#### F06 红点/编辑状态 — ① 实测（2026-11-24 v1/v2 双跑专项 R suite）
- **v1 行为**：编辑激活文件后 tab 红点与树节点红点**同时**出现、改回原文后**同时**消失；仅 `templateType===1` 参与。机制上不是「两处状态互相同步」，而是**同一份数据的两处渲染**：`buildFileList` 把 `treeList` 里的节点对象原样收进文件清单、`tabPush` 直接 push 不 clone，`activeTabItem` 取 `findByIdFromTree(activeItemId, treeList)`、`getElementFromTabList` 取 `tabList.find(...)`，两者命中同一对象，所以 watch 里 `tab.isEdited` 与 `activeTabItem.value.isEdited` 实际是对同一字段写两次。
- **锚点**：L654-680（watch）、L391-406、L59-68（v2 对应 `use-template-tree-state.ts` L102-128、L50-68）。
- **v2 对照要点**：v2 watch 与 v1 L654-680 **逐字符等价**（注释编号、早退顺序、三分支都一致）；`buildFileList`/`tabPush`/`activeTabItem` 要么走共用件要么原地 mutate，无 spread/clone——风险表「对象引用同一性在搬运中被破坏」的验证点即在此。
- **双跑结论**（21 项行为指纹两版逐字一致；串长 534/507，差值 27 全落在版本身份项）：
  - `R3_identity=SAME_REF` 两版相同——运行时直接比较 `findByIdFromTree(2142, treeList) === tabList.find(id===2142)`，证明共享引用未被搬运破坏；
  - 基线 `tree=off/tab=off/save=dis` → 编辑（插入 7 字符）后**同一快照**内 `tree=on/tab=on/save=ena`、`dotRaw=true`；
  - 逐字退格改回原文 `restored=Y` 后同一快照内 `tree=off/tab=off/save=dis`、`dotRaw=false`——亮与灭都是同帧，不存在一侧先或一侧后；
  - 反向通道：编辑后点抽屉 X，确认弹窗文案由 `treeList` 递归 `findAndClose` 得出，仍点名 `BaseUserVO.java`（`R7_isTreeRead=Y`，文案 FNV=`49583903`/len=35 两版相同）——tab 写入的编辑态能被树侧读到；
  - 丢弃后重开抽屉 `treeDots=0/tabDots=0`，`originalTemplateContent` 哈希回到 `89EA3AEA`（未被污染）。

#### F07 刷新 — ① 实测（2026-11-24 v1/v2 双跑 S4，两分支）
- **实测**：脏文件 + 搜索词 `VO` 并存时点刷新，确认框为**确定/取消**类（`btns=x,取消,确定`、`hasYesNo=N`，与 F02/F16 的是/否类按钮族不同），文案哈希 `CC8AD9D7_44`。
  - **取消分支零副作用**：`F07_cancelled` 与 `F07_before` 全字段 `same=Y`（tab 序、激活 2142、展开 12、搜索 `[VO]`、红点、内容哈希 `7181B7FA_922` 全部原地不动），仅回一条 5 字 info toast（`E125569E_5`）。
  - **确定分支**：回落 `n=1 / active=2891 / exp=9 / search=[] / dots=0`，未保存编辑被丢弃（`dirtyDropped=Y`）、`edited=N`、保存按钮回灰；**无 toast**（`okToast=811C9DC5_0` = 空串的 FNV 基值）。
- **锚点**：L627-651。
- **双跑结论**：两分支全部字段两版逐字相同；原「已实测片段」（确认文案一致、搜索词清空、重发 `GET /tree`）已并入本专项，F07 不再有未覆盖片段。

#### F08 勾选批量删除 — ① 实测（前序轮次 v1/v2 双跑，靶点为自建 fixture）
- **实测**：未勾选告警；确认 → `DELETE /deleteList` 落库（目录级联含未渲染子孙，父在前）→「删除成功」→ 树移除、按钮回灰；取消分支零请求零副作用；脏 tab 连环确认同屏叠加。
- **原「破坏性删除、实测禁止落库」的阻塞已解除**：改打在自建 fixture（id 900001-900016）上跑，跑完物理清理并回到基线指纹。
- **命中共有缺陷**：K1（行已删完才弹确认 + `是(Y)` 对 `is_delete=1` 行发更新并谎称保存成功）、K3（多脏连环确认同屏叠加）。
- **锚点**：L762-793。
- **双跑结论**：9 场景逐项一致（明细见第四节勾平表 F08 行）。

#### F09 拖拽移动 — ① 实测（前序轮次 v1/v2 双跑）
- **实测**：`treeAllowDrag/treeAllowDrop/handleNodeDrop` 三段两版逐字符等价；同父 inner/prev、自身与自身子孙均零请求；跨父 inner/prev 发 `PUT /updateParent` 并落库 + 激活 + tabPush + `initData`；拖到顶级根 `newParentId=0` 被后端拒但本地不回滚。
- **原「browser-use 无拖拽能力」的阻塞已解除**：Playwright MCP 可造真 drag，靶点为自建 fixture。
- **命中共有缺陷**：K4（顶级根拖拽后端拒但本地已改，无 `.catch()`/回滚）。
- **锚点**：L796-863。
- **双跑结论**：8 用例一致（明细见第四节勾平表 F09 行）。

#### F10 节点右键菜单 — ① 实测（前序轮次 v1/v2 双跑，6 用例）**【T0 专项 · 预期差异】**
- **行为**：节点上 = 修改 +（目录时）新建目录/模板文件/二进制文件；空白处 = 三个新建（根级 parentId=0、路径 `/`）；越界夹取定位；点外/Esc 关；项点击后自动关。
- **取证方式**：Playwright MCP 已具备右键能力，原「browser-use 无右键」的阻塞已解除；v1/v2 逐项走查一致。**证据粒度＝用例逐项对照，未留本轮那种指纹串**（若需可复核硬证据可补一次同脚本双跑）。
- **锚点**：L86-104、L956-1013。
- **v2 对照要点**：v2 经 ContextMenu 后**翻转定位、监听器随实例卸载、无菜单串扰**——此为 F10/F15/F29 允许的预期差异。

### C. 模板表单弹窗（form.vue 通道）

#### F11 字段随类型显隐 — ② 源码推导（本次不动）
- **预期**：目录/文件名称 label 互斥；条件表达式 type∈{0,1}；模板名称仅 1；上传仅 2；必填文案随类型。
- **锚点**：`form.vue` L68-97。
- **说明**：form.vue 对照期不动，行为以现状为准。

#### F12 入口上下文注入 — ② 源码推导（待手动核对）**【T0/T1】**
- **预期**：新增/修改的 parentId/templateType/templatePath 由右键入口决定（不信详情回传）；路径去尾段计算。
- **受阻原因**：依赖 F10 右键入口。
- **锚点**：L1026-1037、`form.vue` L129-136。

#### F13 保存后链路 — ② 源码推导（待手动核对）
- **预期**：`emit('refreshDataList', data)` → `tabPush` + `initData` + 文件类节点激活。
- **受阻原因**：依赖 F10/F12 表单保存落库。
- **锚点**：L1072-1082。

### D. Tab 区

#### F14 切换/关闭单 tab — ① 实测（2026-11-24 v1/v2 双跑 S2）
- **实测**：点击 tab 切换激活（`clickC=2865/CC97B10C_754/754/isActive=Y`）；无脏时关闭**不弹确认**（`closeClean=no_box`）；关闭末尾 tab 后回落左侧最后一个（3 tab `active=2865` → 关 2865 后 `active=2142`），与推导的「优先右侧、无则左侧末尾」一致。
- **脏 tab 释放三态**：`dirtyBox=49583903_35/hasTplWord=Y/btns=x,否(N),是(Y)`；点右上角 X → 弹窗消失但 **tab 仍在**、红点仍亮（`xBranch=box_gone/tabStill=true/dot=on,on`）即 K2 复现；点 `否(N)` → tab 关闭、激活回落 2891，但节点 `edited=Y` 且树红点仍亮（`dot=on`）即新登记共有缺陷 **K6**。
- **锚点**：L1145-1188。
- **双跑结论**：以上全部字段含两处共有缺陷的两版行为逐字相同，S2 行为串 `fpBehav=662F9F06`/len=1031。

#### F15 tab 右键菜单 6 项 — ① 实测（前序轮次 v1/v2 双跑，含「是(Y)=保存后关闭」分支））**【T0 专项 · 预期差异】**
- **行为**：关闭当前/其他/左/右/未改/全部，各项显隐条件逐项对应现状；经 `tab-{id}` DOM 反查定位目标。
- **取证方式**：6 项关闭操作两版逐项一致；其中带未保存内容的释放确认（是/否/X 三态）已单独补跑过「是(Y)」分支（靶点文件已改回原文）。同样**未留指纹串**。
- **锚点**：L189-216、L1104-1142。

#### F16 批量关闭 + 未保存释放 — ① 实测（2026-11-24 v1/v2 双跑 S2/S3）
- **实测**：tab 右键菜单 6 项在 3 tab 场景下全量出现（`关闭当前,关闭其他,关闭左侧,关闭右侧,关闭未改,关闭全部`）；1 tab 仅 2 项（`关闭当前,关闭未改`）。
- **关闭未改**：**无确认直接过滤**（`unedited=no_box`），全部脏时 tab 数不变（`allEdited=true`），激活位重算后为 2865。
- **关闭全部（含脏）**：弹三态框（`allBox=52832574_45/hasTplWord=Y`）；`否(N)` → `n=0 / active=-1` 并显示空态；`是(Y)` → `PUT /updateContentBatch` 一次批量落库 + toast `044332ED_4`（「保存成功」4 字）→ `n=0 / treeDots=0`，且 `origSync=Y`（保存后 `originalTemplateContent` 与内容同步）。
- **锚点**：L1281-1325。
- **双跑结论**：零写入部分（S2）与落库部分（S3）行为串两版均逐字相同：S2 `662F9F06`/len=1031、S3 `F59C86C6`/len=792（S3 靶点 2891/2142 落库后已逐条改回基线并自证 `toBase=Y`）。

#### F17 tab 拖拽排序 — ① 实测（2026-11-24 v1/v2 双跑 N2a + 真 drag + N2b）
- **实测**：`H.DRAG` 把 `#tab-2891 .tab-label` 真拖到 `#tab-2136 .tab-label` 后，DOM 序由 `2891,2142,2865,2136` 变（变后为 `2142,2136,2865,2891`）、**激活项不跟随**（`activeIsMoved=N`）、tab 数不变（`count=4>4`）；再点其它 tab 后 DOM 序又变（`stable=N`），即 Vue 数据序与 DOM 序不同步。
- **旧预期纠正**：原写「oldIndex/newIndex 各 -1（高亮项偏移）；拖后激活被拖 tab」两项均不成立。机理探针：`hasActiveBar=false`——本页 tabs 是 `type="card"`，**不渲染 `.el-tabs__active-bar`**，`.el-tabs__nav` 子节点全是 `.el-tabs__item`，所以下标本来就对齐，`-1` 反而使映射整体错位一格：按 `-1` 公式机械预测的列表与激活位与实测**逐位吻合** → **真正被移动的是被拖项的前一项**（命中 **K7**）。
- **原「browser-use 无拖拽」的阻塞已解除**：Playwright MCP 可用 `locator.dragTo` 发真 drag（`browser_drag` 会在本场景 5s 超时且只触发拖动中的 DOM 实时位移、不触发 `onEnd`，不能用作证据）。
- **锚点**：L359-389（v2 对应 `editor-panel.vue` L180-192，逐字同构）。

#### F30 tab 标签构成与红点 — ① 实测（2026-11-24 v1/v2 双跑专项 R suite）
- **观察**：tab = 文件图标 + 文件名 + `el-badge is-dot` 红点；红点与 F06 共用同一个 `isEdited` 字段。`tree.vue` L177 与 `editor-panel.vue` L49 是同一段模板结构，所以两版可用同一族选择器取证。
- **双跑结论**（与 F06 同一份 R suite）：基线 `tab=off` → 编辑后 `tab=on` 且与树红点同帧 → 改回原文 `tab=off`；重开抽屉后树与 tab 徽标同为 0（`R9_reopen=0/0` 两版相同）；跳 tab 不串红点由前序 D3 用例覆盖（`dot1111=Y` + `dot2142=n`）。
- **锚点**：L172-186、L545→L1419-1431。

### E. 编辑区

#### F18 路径显示 — ① 实测（2026-11-24 v1/v2 双跑 N3）
- **实测**：`.text-tooltip-title` 文本带前缀「路径：」，完整值例 `路径：${backendPath}/src/main/java/${projectPackageSlash}/${projectNameSlash}/common/domain/entity/1111.java`（len=105，FNV=`55A67E30`）；过长省略真实命中（`sw=721 > cw=655`，computed `text-overflow:ellipsis / display:inline-block / white-space:nowrap`）；合成 hover 出 tooltip，内容为**无前缀纯路径**（`FACEA4BD_102`）；切到另一文件后路径跟随（p2 len=118、`sw=813`）。
- **交叉验证**：用 SQL 逐级反查 `parent_id` 装配链 `2117→2120→2121→2127→2130→2134→2135→2891` 与页面全路径逐段吻合；p2 对应 2190（parent 2130）。
- **零写入**：`writeReq=NONE`；行为串 `B56547D8`/len=270 两版逐字相同。
- **锚点**：L547-552、L132。

#### F19 保存 — ① 实测（2026-11-24 v1/v2 双跑 S1，含落库链路）
- **观察**：`isEdited` 时保存按钮启用（蓝色）；无改动时禁用带 tooltip。本轮补齐了上一版欠的**点击落库链路**（靶点文件 2891 `1111.java`，基线内容哈希 `FD3E6C13_160`）：
  - 插入 `@@SAVE@@` → 内容 `6B63B642_168`、`dot=on,on`、`save=ena`、`edited=Y`；
  - 点保存 → toast `044332ED_4` → `dot=off,off`、`save=dis`、`edited=N`，且 **`origAdvanced=Y`**（`originalTemplateContent` 跟上新内容，不会把已存内容当脏）；
  - 再插再退 → `reverted=Y` 回到基线哈希但 `dot=on,on`（与 F06 「改回原文同帧灭」不同帧），需再点一次保存才能把红点与库同时归零 → 收尾 `final=FD3E6C13_160 / dot=off,off / restoredToBase=Y`。
- **锚点**：L142-146、L925-948、L225。
- **双跑结论**：S1 行为串 `fpBehav=FAEE0F60`/len=441 两版逐字相同；DB 侧固定指纹 SQL 复核 `d07878b5db8bea76510d99946695ae6f` 未变。

#### F20 测试试渲染 — ① 实测（2026-11-24 v1/v2 双跑 N4）
- **实测**：激活文件（type≠0）显示「测试」按钮；点击弹子抽屉，标题「模板测试（测试111.java）」（`15454FC1_16`）、tabs = `原始模板*,渲染结果`（`A04E74C9_10`）、未选数据时**渲染与保存均 disabled**、刷新 enabled、仅 1 个 cascader select（placeholder `|1111.java`）；本轮共 6 个请求全 GET，并弹一条「请先关联项目」toast。
- **关闭不影响主抽屉**：点子抽屉 X 后主抽屉内容 `hashSame=Y`、tab 序 `seqSame=Y`、`treeDots=0`、无确认框（`closeBox=NO_BOX`，因 `isEdit=false` 直接走 `done()`）；实测关闭耗时 382～1516ms，固定 `sleep(900)` 会误报「关不掉」，已改轮询。
- **两个假告警已推翻**（均为探针缺陷，非行为差异）：① `test.vue` 的 `el-drawer` **未开 `append-to-body`**，子抽屉整棵 DOM 嵌在主抽屉内，全子树查询把子抽屉的 tab / cm-editor 混进主抽屉读数；② 上述关闭延迟。子抽屉的关闭按钮选择器是 `button[aria-label="关闭此对话框"]` / `.el-drawer__close-btn`。
- **旧观察补充纠正**：真 ESC **关不掉子抽屉**（上一轮实测），而主抽屉会被 ESC 直接关掉（见 F21）。
- **锚点**：L136-138、L897-922；行为串 `075DEC26`/len=292 两版逐字相同。

#### F21 伪全屏 — ① 实测（2026-11-24 v1/v2 双跑 N5a + 真 ESC + N5b）
- **实测**：点「全屏」后 `.editor-area` 挂 `full-screen-mode`，computed `position:fixed / z-index:3000`，尺寸从 977×690 铺满到 1441×770（= 视口），按钮文案变「退出」；**未进入原生全屏**（`document.fullscreenElement=NULL`、`fullscreenchange` 计数增量 0），属纯 CSS 伪全屏。
- **真 ESC**：`afterEsc=EXITED` 看起来像 ESC 退出了全屏，但 `escClosedMain=Y` 说明**ESC 被 `el-drawer` 的 `close-on-press-escape` 吃掉、直接把整个主抽屉关了**，而 `treeFullScreen` 随组件销毁/`init` 复位，重开后不残留全屏态——所以上一轮的「ESC 对伪全屏无效」需修正为「ESC 不会退出伪全屏，而是直接关掉宿主抽屉」。
- **补充探针（双版同码）**：不开 ESC，直接 全屏 → 退出：两版均 `977×690 → 1441×770 → 977×690`，`restored=Y`、`cls` 回 N、零请求。
- **量测口径教训**：本环境下 `requestAnimationFrame` 被饥饿，`position:fixed` 元素的 `left/top` 会因祖先 transform 停在过渡起始值而偏到 1441/1885，因此断言只用宽高 + 类名 + computed style（绝对坐标降为身份项）。
- **锚点**：L147-153、L409-423、L1497-1507；行为串 N5a `B51CBD46`/108、N5b `D558C5F3`/148 两版逐字相同。

### F. 变量面板

#### F22 双入口显隐 — ① 实测（2026-11-24 v1/v2 双跑）
- **v1 行为**：默认收起。头部「变量」按钮与右侧竖柄 `.variable-edge`（宽 28px，文案「变量」+ 计数徽标，tooltip「展开变量面板」）切同一个 `variablePanelVisible`；展开态竖柄 `v-if` 卸载，收起态面板不渲染（非 display:none）。
- **锚点**：L154-159、L257-264、L462-464。
- **v2 对照要点**：竖柄抽成共享件 `variable-panel/variable-edge.vue`（tooltip 文案、28px、徽标条件渲染、CSS 逐条对齐内联版）。
- **双跑结论**：B1/B2/B3 + D1 的 7 项指纹全等 — `edgeTxt=变量288`、`edgeW=28`、收起态 `panelCollapsed=0`、点竖柄 `panelW=266`+`edgeStillVisible=false`、头部按钮收起 `panelVis=0`/`edgeVis=1`、再展开 266、`D1_edgeTip=展开变量面板`。

#### F23 徽标计数 — ① 实测（2026-11-24 v1/v2 双跑）
- **v1 行为**：收起态竖柄角标取 `countVariables(catalog)`，与面板头部「共 N 项」同源。
- **锚点**：L441-450。
- **双跑结论**：竖柄 `288` 与面板 `共 288 项` 两版逐字一致；`D2_panelTitle=Freemarker变量`、`D2_count=共 288 项`、`D2_usageTip` 说明文案 FNV=`19e14067` 两版相同。

#### F24 目录懒加载 — ① 实测（2026-11-24 v1/v2 双跑）
- **v1 行为**：`useVariableCatalog(toRef(props,'templateGroupId'), { immediate: false })`，抽屉打开才 `ensureLoaded`，命中模块级 `catalogCache` 则不再发请求。
- **锚点**：L428-460。
- **双跑结论**：两宿主调用签名完全一致（`tree.vue` L435 / `tree-v2.vue` L116）。本轮 5 次开抽屉，网络侧只新增 `GET /tree`，**无一条 `variableCatalog`**（组 80 目录在会话早期已入缓存），两版同为缓存命中；`rowCnt=22`（顶层 4 组展开）与 `rowHash=16f734cf` 两版相同。

#### F25 宽度 px 体系 — ① 实测（2026-11-24 v1/v2 双跑 N0a-N0d，干净会话）
- **v1 行为**：px 自持体系，区间 `[240,560]`、编辑区下限 320、首次展开取 `editorSplitterSize.width × 28%`；`init()` 里 `panelSizeInitialized=false` 复位。
- **锚点**：L466-508。
- **v2 对照要点**：v1 内联 L494-508 与 `use-variable-panel-size.ts` L58-68 逐式等价（同一 `0.28`/`Math.round`/首次标记），复位改由 `resetInit()` 承担；抽屉重复打开时两版都会重新取首次值。
- **双跑结论**：首次展开值已在干净会话取得并**推翻上一轮的存疑结论**：`SE0=949`（分栏容器 clientWidth）→ `round(949×0.28)=266` 与实测面板宽完全相等（`hit=Y`）。上一轮写「266 不是首次展开值、是拖拽后的 px」是**量错了对象**（量的是编辑面板 711）。
- **窄容器退让与首次值已压测完（N0b/N0c/N0d）**：SE=599 时 `retain=266` 与 `recompute=240` 分岔，实测守 266 → **RETAIN 分支**（`panelSizeInitialized` 已置后不重算）；SE=473 时面板守 240、编辑区掉到 233 < 320 → **EDITOR_YIELDS**（编辑区下限退让，`floor=233/upper=240`，`panelMin=auto`）；视口复原后 `seBack=Y / pw=266 / hit=Y`。
- **一个新增口径发现**：`.variable-edge` 是**占流元素（28px）**，收起时挤窄分栏容器（949）、展开后容器释放为 977，所以 28% 取的是含竖柄的窄容器（展开后实际占比 27.2%）；`epw`/`sum` 会随该 28px 释放时机在 233/261 之间跳（同版本内噪声），F25 的真断言量是 `pw`。
- **锚点**：L466-508。

#### F26 变量插入 — ① 实测（2026-11-24 v1/v2 双跑）
- **v1 行为**：行 **双击** 触发（`panel-body.vue` L170 `handleRowDblclick` → L149 `handleInsert` → `variable-utils.ts` L175 `buildInsertText` → `props.insert(text,caret)`）；单击不插入。口径：SCALAR 插 `${表达式}`（caret=末尾）；LIST 插 `<#list expr as loopVar>\n  \n</#list>`（caret=循环体缩进后）；OBJECT/MAP/分组/元素行返回 null 静默不插；`props.disabled` 在 `handleInsert` 最前面早退；宿主返回 false 才 toast「编辑器尚未就绪，请稍后重试」。
- **锚点**：L510-521、`variable-panel/panel-body.vue`、`variable-utils.ts` L175-192。
- **v2 对照要点**：`canInsertVariable` 两版同为 `activeTabItem.templateType === 1`（v1 L515 / `use-template-tree-state.ts` L45）；插入转发 v1 直取 `codeMirrorRef`，v2 多一跳 `tree-v2 → editor-panel L246-248 → codeMirrorRef`，末端同为 `code-mirror.insertAtCursor`（共用件）。
- **双跑结论**（28 项业务指纹两版逐项一致，串长 594/621，差值 27 恰为 v1 脚本里多出的自证行）：
  - 双击 `projectName` 927→941（+14）、再双击 955、`${projectName}` 计数 2 ✓；单击 941 不变 ✓
  - 双击 MAP 行 `projectTemplatePropertyData` 长度不变且无 toast ✓（静默兜住）
  - 双击 LIST 行 `moduleList` 955→995，落地 `<#list moduleList as module>` ✓
  - 双击可空变量 `databaseUrl` 995→1009 ✓
  - 插入后 `dotAfterEdit=Y`、保存按钮转可点；全程 `toast=none` 证明 v2 转发链就绪 ✓
  - 面板关键词 `matchKeyword` 与树过滤口径不同：`PROJECTNAME`(大写) 命中 16、`项目`(中文，走 desc) 命中 29、清空 102（含被自动展开的组），两版全等
  - D3/E：切到 `1111.java` 后双击插入只让该文件亮红点（`dot1111=Y`），`BaseUserVO` 内容与红点均不受影响（`808_untouched`）——插入落点跟随激活 tab，两版一致
- **不可达分支**：`disabled=true`（非模板文件）在模板树内无法构造——组 80 无 `template_type=2` 行，且点目录不改变激活项（`handleTreeNodeClick` 仅 `templateType!=0` 激活）。该分支由共用件同一份代码承担，登记为死分支而非差异项。
- **遗留观察（非 v2 回归，共用件既有）**：`panel-body.vue` L171 的 `closest('.insert-button')` 守卫与 L287/L292 的 `.insert-button` CSS 均无对应 DOM（实测 `D2_hasInsertButtonEl=false`）——插入按钮在后续迭代中被双击方案取代，守卫与样式残留。归入 T4 收尾清理清单。

### G. 其他

#### F27 二进制展示 — ① 实测（2026-11-24 v1/v2 双跑 Suite P）
- **预期**：图片扩展名 inline 预览，其余「暂不支持预览」；图标按扩展名区分（image/unknown）。
- **实测（两条分支都用真实数据跑通，X=空集）**：
  - **图片分支**（组 79 存量 `404.png`，id 2112，父链 2015→2027→2111）：`P2_imageCount=1,wrapCls=el-image,imgCls=el-image__inner`；`P2_src=data:image/png;base64,iVBO,len=68926`，与 SQL `CHAR_LENGTH(template_content)` 完全相等，即 `templateContent` 原样作 data URI 直填 `src`、无接口二次拉取；`P2_natural=1100x731,objFit=fill`（对应模板里的 `fit="fill"`）；`P2_noSupport=N,mainText=811C9DC5_0`（`el-main` 文本长度为 0，该分支确实不渲染任何文字节点）；节点图标 `#icon-file-image`。
  - **非图片分支**（夹具组内新建 `T2P-NOTIMAGE`，上传文件名 `fixture.jar`）：`P1_previewText=6E37EBCE_17,noSupportWord=Y`（「文件暂不支持预览（目前只支持图片）」逐字命中）；`P1_imageCount=0,cm=0`（既不起 `el-image` 也不起 CodeMirror，符合 `templateType===2` 两分支互斥）；`P1_headerBtns=保存=true|全屏=false|变量=false`——**「测试」按钮缺席**，与 L136 `v-if="![0,2].includes(...)"` 一致；节点图标 `#icon-file-unknown`。
  - **提交通道**：`P1_bodyKeys` 为 12 键纯表单（与 Suite G 的新建请求体同构），`P1_bodyContent=data:application/octet-str,len=49`，证明上传走 `form.vue` 的 `FileReader.readAsDataURL` → `templateContent`，前端不落文件路径。
- **自动激活态单独落档**（本段取证口径的关键前提）：抽屉 init 会自动开 DFS 首个文件 `favicon.ico`（id 2116），它**也是图片分支**（`P2_autoTab=2116/favicon.ico/ACT`、`P2_autoImg=1,data:image/x-icon;base64,A,len=360557,256x256`）。不先记这一条，后面的 `imageCount=1` 就说不清属于 404.png 还是 favicon.ico。
- **观察（两版共有，非 v2 回归）**：`imageTypeList` = `['png','jpg','jpeg','gif','svg','bmp','git','ico']`，其中 **`'git'` 疑似 `'gif'` 的笔误**（`gif` 已在其之前的位置）；v1 `tree.vue` L545 与 v2 `use-template-tree-state.ts` L35 **逐字相同**，故不构成差异。但 `endsWith` 是字面比较，**大写扩展名（如 `LOGO.PNG`）会落到「暂不支持预览」分支**；另外共用件单测 `tree-utils.test.ts` L115 用的是**不含 `'git'`** 的列表，与生产数组不一致（不影响本次结论）。归入 T4 清理清单。
- **锚点**：L228-237（v2 `editor-panel.vue` L71-80 逐字同构）、L545（v2 state L35）、L136。

#### F28 空态引导 — ① 实测（2026-11-24 v1/v2 双跑 Suite P）
- **预期**：无数据时提示空白处右键新建。
- **靶点建立方式**：库里**原本没有任何 0 模板的模板组**（`GROUP BY ... HAVING COUNT(*)=0` 返回 0 行），故用自建夹具组 `id=93 / group_name='__T2FIXTURE_EMPTY__'`（`type=0`）造出真实空态，批次结束后物理 `DELETE` 回收。
  > 更正：更早一轮曾据列表页「**0个**」列推断「存在多个空组」，实为误读——该列是 `groupPropertyCount`（组属性数量）而非模板数（见 `template-group/index.vue` L18/L138）。登记为探针教训：**结论必须回到 SQL 与源码证据，列名与展示值不能互证**。
- **实测**：`P0_treeNodes=0,emptyBlock=1,emptySeen=YES`；`P0_emptyCls=el-empty,emptyHostCls=el-tree__empty-block`（EP 原生空态，非自绘）；`P0_emptyDescRows=2,emptyImage=1`；文案 `P0_emptyText=ECCFA594_30`，两行分别为「暂无数据」与「请在空白处点击右键新建目录、模板文件或者二进制文件」（`line1=Y,line2=Y`）；右侧同时保持空编辑器态 `P0_editorHint=Y,tabs=0,cm=0,headerBtns=`（无按钮可点）。
- **引导语可执行性**（不只验文案，还验它指的路径走得通）：空树空白处发真实右键 `pointerdown → mousedown → contextmenu` → `P0_blankMenu=OPEN,新建目录|新建模板文件|新建二进制文件`（三项齐全），随后 `P0_menuClose=0`；P1 正是**从这条右键菜单进去**新建出非图片二进制节点的，等于把 F28 的引导语端到端验证。本段 `P0_writeReq=NONE,count=0`（零写入）。
- **锚点**：L68-81（v2 `tree-panel.vue` L56-69 逐字同构）。

#### F29 暗色与层级 — ① 实测（2026-11-24 v1/v2 双跑 Suite H）**【T0 专项 · 预期差异】**
- **预期**：context-menu 全走 EP 语义变量；菜单 z-index 高于抽屉 popup（2000+）。
- **实测（亮色 + 暗色各一轮，全程零写入）**：四段行为串两版逐字全等（H0 `E066C103`/499、H1b `E7E21F11`/74、H2 `9D4A93A1`/543、H3 `1F44806A`/49）。
  亮色：`bg=rgb(255,255,255)`、`bd=0.571429px/rgb(220,223,230)`、`sh=rgba(0,0,0,.15) 0 2 8`、`z=9999`、`miw=85/maw=200/pad=5px/ls=none`；
  暗色（翻 `html.dark`）：`bg=rgb(29,30,31)`、`bd/rgb(76,77,79)`、`clr=rgb(255,255,255)`，且 `H2_darkHoverVar=rgb(57,57,58)/rgb(229,234,243)` 两版同值；
  hover（真实鼠标）：`bg=rgb(235,237,240)`、`clr=rgb(48,49,51)`，且 `H1_varEq=bg:Y,clr:Y` —— 这一条是 F29 的正面证据：hover 底色/文字色与同主题下 `--el-fill-color-dark`/`--el-text-color-primary` 的计算值**相等**（用同变量建探针元素比 computed 值，不比字面量）。
  层级：两版 `z=9999`，`hitTest=IN_MENU,clip=OK` 两版同——v1 菜单在抽屉 DOM 内 9 层深、v2 Teleport 到 body 直下，**两者都压得过抽屉**；`drawerZ=auto,overlayZ=NA,testZ=auto` 两版同。
- **预期差异实测值（单列 X 串，不参与 `fpBehav` 判等）**：
  x1 限高滚动 v2=`mah=754.286px|ovf=auto` / v1=`mah=none|ovf=hidden`（T0 新增的最后防线）；
  x2 Esc 关闭 v2=全部 `Y`（afterEsc 计数 0）/ v1=全部 `N`（计数 1）——**实测确认 v1 全文无 Esc 处理器**，Esc 是 T0 共享件新增的关闭通道，F10 基线里「点外/Esc 关」的 Esc 部分对 v1 不成立；
  x3/x5 菜单串扰见下方「口径修正」。
- **预期差异项修正（本批最重要的一条）**：原列为预期差异的「**无菜单串扰**」实测不成立：
  x5 用纯 `contextmenu`（不经任何外部指针事件）构造同开态时，**v1=2、v2=2 —— 两版都能两菜单同开**（v2 的 `useContextMenu` 每实例独立 `visible`，同样无互斥）；
  而真实右键必带 pointerdown/mousedown，此时两版都会先关掉上一个菜单（x3 两版均 `visible=1`、`H0_closeAll=0` 两版同）。
  故「串扰」在行为面**两版均不可观测为差异**，v2 的改进仅在于定位/外点判定不再依赖首个 `.context-menu` 匹配（结构层面，同时消除监听器泄漏）。
- **锚点**：L1377-1418；脚本 `2026-11-24-t2-suite-H-dark-zindex.js`。

---

## 三、取证分类统计（v1 行为取证 + T2 双跑进度）

| 分类 | 功能点 | 数量 |
|------|--------|------|
| ① 实测（v1 行为已取证） | F01、F02、F03、F04、F05、F06、F07、F08、F09、F10、F11、F12、F13、F14、F15、F16、F17、F18、F19、F20、F21、F22、F23、F24、F25、F26、F27、F28、F29、F30 | 30 |
| ② 源码推导（v1 行为待手动核对） | —（已清零） | 0 |

> 取证方式口径升级：不再区分「仅 v1 实测」与「v1/v2 双跑」，① 均要求至少一轮可复核证据；F03/F07/F14/F16 四项已于 2026-11-24 从 ② 转 ①（Suite S 双跑），F19 的保存落库链路也已补齐；F17/F21 于同日经 Suite N 双跑转 ①；F11/F12/F13 于同日经 Suite G 双跑转 ①；F29 于同日经 Suite H 双跑转 ①；F27/F28 于同日经 Suite P 双跑（自建空态夹具组 + 存量真实二进制节点）转 ①，**② 类已清零**。

**T2 双跑进度（30 项）**

| 状态 | 项 |
|------|----|
| ☑ 已勾平（30） | F01、F02、F03、F04、F05、F06、F07、F08、F09、F10、F11、F12、F13、F14、F15、F16、F17、F18、F19、F20、F21、F22、F23、F24、F25、F26、F27、F28、F29、F30 |
| ☐ 未跑（0） | 无。原以「数据受阻」拟移交 T4 的 F27/F28，已改用夹具空组（自建后物理回收）+ 组 79 存量二进制节点实跑闭合 |

**T2 双跑进度：30 / 30 = 100%**（F01-F30 全部为 v1/v2 双跑实测，无口径外遗留项；数据受阻项的处理方式见文末 Suite P 脚注）。

- **预期差异项（v2 允许与 v1 不同）**：仅剩 F10、F15、F29（ContextMenu 翻转定位、监听器随实例卸载、无菜单串扰），其中 F10/F15 已实测一致。**F02 已于本轮从预期差异项移出**：两处文案（带/不带「模板文件」）双跑实测逐字相同，v2 原样保留了这一不对称，未发生差异。**Suite H 再移出「无菜单串扰」一条**：实测两版均可同开、且真实右键路径下两版都会先关上一个，行为面不可观测为差异；F29 剩下的真实差异只有 x1（限高滚动）与 x2（Esc 关闭通道），均为 T0 共享件的既定新增。
- **已关闭的最高危对照项**：F06 / F30 红点双向同步（对象引用同一性）——R suite 勾平，两版 `SAME_REF`；上一轮列为「剩余高危关注点」的 F02 三态文案也已关闭；F17 tab 拖拽排序原疑为「v2 新组件回归」，本轮经源码逐字核对 + 双跑同形已降级为**共有缺陷 K7**。
- **状态机批次（Suite S）已跑完**：F01/F02/F03/F07/F14/F16/F19 七项五段行为指纹两版逐字全等（数值见文末脚注与勾平表）；本轮新增共有缺陷 **K6**（`否(N)` 丢弃分支不清 `isEdited`、不回滚内容），设计文档「对照期已知共有缺陷」已登记。
- **布局交互批次（Suite N）已跑完**：F05/F17/F18/F20/F21/F25 六项 11 段行为指纹两版逐字全等（零写入），新增共有缺陷 **K7**（card 型 tabs 下 Sortable 索引 `-1` 整体错位一格），并纠正两条历史误记文案（详见文末 Suite N 脚注）。

---

## 四、T2 对照勾平表（v2 复现时逐条填写）

| 编号 | 功能点 | 取证方式 | v1 行为摘要 | v2 是否一致 | 勾平 |
|------|--------|---------|-------------|-------------|------|
| F01 | 打开入口 | **实测**（v1/v2 双跑 S0） | 标题带组名，init(row.id)；初始 tab/展开/红点/保存态全为基线值 | 是（`FE999DBB` / len=425 两版逐字相同） | ☑ |
| F02 | 关闭确认 | **实测**（v1/v2 双跑 S2/S3/S4） | 三态；两处文案带/不带「模板文件」，共 6 种弹窗变体 | 是（单文件成对 `59C5D1A4_29` vs `23893E55_25`，`pairCheck=Y/pairLenDelta=4`；`49583903_35` 与 R suite 同值） | ☑ |
| F03 | 重复打开复位 | **实测**（v1/v2 双跑 S4） | init 清全部态，恢复展开（4 脏丢弃后重开 → `n=1/active=2891/exp=9/search=[]/dots=0`，四文件内容回基线） | 是（并入 S4 串 `91013E7D` / len=1665） | ☑ |
| F04 | 树搜索 | **实测**（v1/v2 双跑，同脚本同选择器） | 实时 filter；命中节点+祖先可见并自动展开（不写回 expandedKeys）；命中目录不含子文件；大小写敏感；空串才等于不过滤；过滤不掉勾选 → 命中 **K5**；无匹配显 empty | 是（17 行指纹串 FNV-1a=`18efdf94` / len=5940 相同） | ☑ |
| F05 | 单击节点 | **实测**（v1/v2 双跑 N1） | 文件：进 tab + 激活 + `is-current` + scrollIntoView 定位（`inView=Y/scrollTop=0`）；已开文件再点只切激活（tab 数不变）；目录：**只翻展开、不改勾选**（`checkedFlip=N / checkedTotal 0>0`，因 `:check-on-click-leaf="false"` 且未配 `check-on-click-node`）——**纠正旧记「目录仅勾选」** | 是（`3125E167` / len=312 两版逐字相同） | ☑ |
| F06 | 红点/编辑状态 | **实测**（v1/v2 双跑专项 R suite） | 同一 `isEdited` 字段的两处渲染；编辑同帧亮、改回原文同帧灭 | 是（21 项行为指纹逐字一致；`identity=SAME_REF`、确认弹窗文案 `49583903_35` 两版相同） | ☑ |
| F07 | 刷新 | **实测**（v1/v2 双跑 S4，两分支） | 确认框为确定/取消类（`CC8AD9D7_44`）；取消零副作用（`same=Y` + 5 字 info toast），确定重 init 并丢弃未保存、无 toast | 是（两分支全字段逐项一致） | ☑ |
| F08 | 勾选批量删除 | **实测**（v1/v2 双跑，组 80 fixture 靶点） | 取消分支零请求零副作用；确定分支 `DELETE /deleteList`（目录级联含未渲染子孙，父在前）→「删除成功」→ 树移除、按钮回灰；脏 tab 连环确认 → 命中 **K1/K2/K3** | 是（9 场景逐项一致） | ☑ |
| F09 | 拖拽移动 | **实测**（v1/v2 双跑） | `treeAllowDrag/treeAllowDrop/handleNodeDrop` 三段逐字符等价；同父 inner/prev、自身与自身子孙均零请求；跨父 inner/prev 发 `updateParent` 并落库 + 激活 + tabPush + `initData`；拖到顶级根 `newParentId=0` 被后端拒但本地不回滚 → 命中 **K4** | 是（8 用例一致） | ☑ |
| F10 | 节点右键菜单 | **实测**（v1/v2 双跑，6 用例） | 树节点/空白处右键菜单项与回调逐项对应 | 是（6 用例逐项一致） | ☑ |
| F11 | 字段随类型显隐 | **实测**（v1/v2 双跑 G0 三新建项矩阵 + G5 必填拦截） | form.vue 未动；模板类型恒渲染 3 个 radio（r=3）且按入口预置选中项，条件表达式仅 type∈{0,1}、模板名称仅 1、文件上传仅 2；二进制缺文件上传时被前端校验拦下 | 是（dump 哈希 B2F585E6_149 / 1E508800_181 / 593FB31A_151 两版同值；G5 `5DD215C0`/174 逐字相同） | ☑ |
| F12 | 入口上下文注入 | **实测**（v1/v2 双跑 G0/G1/G2/G3/G5） | 节点右键 parentId=节点 id、路径=节点全路径；空白右键 parentId=0 且路径为 '/'（`pathIsRoot=Y`）；「修改」取节点全路径**去尾段**（`pathNoTail=Y`）；不信详情回传——`GET /detail` 后表单值仍由入口 context 覆盖 | 是（`88E57805`/381、`D7F1E192`/300、`5DD215C0`/174 三段两版逐字相同） | ☑ |
| F13 | 保存后链路 | **实测**（v1/v2 双跑 G2/G3/G4/G6，含写库） | 目录分支 `POST /add` 后仅 initData 回树（不进 tab、不换激活、不改路径）；文件分支 `POST /add` 回传 VO → tabPush + 激活新 tab + 路径显示 + 编辑器挂载；`PUT /update` 后 tab 数/激活/路径/编辑器文本零变化且 tab 与树描述同值 | 是（`9C2ACDAA`/446、`ED183723`/915、`F2A507BD`/683、`00CABAB4`/126 四段两版逐字相同） | ☑ |
| F14 | 切换/关闭单 tab | **实测**（v1/v2 双跑 S2） | 点击切换激活；无脏关闭不弹框；关末尾 tab 回左则左侧末尾；脏释放三态（X 不关 = K2、否(N) 不清脏 = K6） | 是（`662F9F06` / len=1031 两版逐字相同） | ☑ |
| F15 | tab 右键菜单 6 项 | **实测**（v1/v2 双跑） | 关闭当前/其他/左/右/未改/全部 | 是（6 项关闭操作逐项一致） | ☑ |
| F16 | 批量关闭+未保存释放 | **实测**（v1/v2 双跑 S2/S3） | 6 项菜单显隐条件、「关闭未改」无确认、「关闭全部」三态；`是(Y)` 走 `updateContentBatch` 并清红点 | 是（S2 `662F9F06`/1031、S3 `F59C86C6`/792 两版逐字相同；`origSync=Y`） | ☑ |
| F17 | tab 拖拽排序 | **实测**（v1/v2 双跑 N2a+真 drag+N2b） | Sortable 挂在 `.el-tabs__nav`，拖完 **DOM 序变、激活项不跟随、再点 tab 后序又变**（`seqChanged=Y / activeIsMoved=N / stable=N`）；机理已验证：card 型 tabs **不渲染 `.el-tabs__active-bar`**，所以下标多了一个，`oldIndex!-1 / newIndex!-1` 整体错位一格 → 命中 **K7** | 是（N2a `2992E4FE`/172、N2b `AC7F1D23`/246 两版逐字相同，拖后新序均为 `2142,2136,2865,2891`） | ☑ |
| F18 | 路径显示 | **实测**（v1/v2 双跑 N3） | `fullFilePath = getFullPathById(...)` 实算，前缀「路径：」；过长省略（sw=721 > cw=655，`text-overflow:ellipsis`）；合成 hover 出 tooltip 且内容为**无前缀纯路径**；切 tab 跟随更新（p2 sw=813） | 是（`B56547D8` / len=270 两版逐字相同；另用 SQL 交叉验证装配链 2117→2120→2121→2127→2130→2134→2135→2891 与 2190 全吻合） | ☑ |
| F19 | 保存 | **实测**（v1/v2 双跑 S1，含落库） | isEdited 启用；点击保存 → `updateContent` → toast → 红点双灭 + `origAdvanced=Y` | 是（`FAEE0F60` / len=441 两版逐字相同；落库后已复原并 SQL 复核） | ☑ |
| F20 | 测试试渲染 | **实测**（v1/v2 双跑 N4） | 子抽屉标题「模板测试（…）」、tabs `原始模板*,渲染结果`、未选数据时**渲染/保存均 disabled**（刷新 enabled）、仅 1 个 select；关闭后主抽屉内容/tab 序/红点零变化（`hashSame=Y / seqSame=Y / treeDots=0`）；全程只 GET | 是（`075DEC26` / len=292 两版行为串逐字相同） | ☑ |
| F21 | 伪全屏 | **实测**（v1/v2 双跑 N5a+真 ESC+N5b） | `full-screen-mode` → `position:fixed; z-index:3000` 铺满 1441×770；**未进原生全屏**（`nativeFs=NULL / fschDelta=0`）；真 ESC **不退出伪全屏而是直接关掉整个主抽屉**（`escClosedMain=Y`），重开后全屏态不残留；点「退出」尺寸回 977×690 | 是（N5a `B51CBD46`/108、N5b `D558C5F3`/148 两版逐字相同；补充探针两版均为 977×690→1441×770→977×690 `restored=Y`） | ☑ |
| F22 | 双入口显隐 | **实测**（v1/v2 双跑） | 按钮+竖柄同状态，互斥显隐 | 是（7 项指纹一致，含竖柄 tooltip 文案） | ☑ |
| F23 | 徽标计数 | **实测**（v1/v2 双跑） | countVariables 总数 288 | 是（288 / 共 288 项 / 说明文案哈希相同） | ☑ |
| F24 | 目录懒加载 | **实测**（v1/v2 双跑） | ensureLoaded 按组缓存 | 是（顶层 22 行 rowHash 相同；重开走缓存零请求） | ☑ |
| F25 | 宽度 px 体系 | **实测**（v1/v2 双跑 N0a-N0d，干净会话） | 首次展开值就是 28%：`round(949×0.28)=266` 与实测宽完全相等（**纠正旧记「266 不是首次值」**，上一轮量错了对象）；中度窄（SE=599）走 **RETAIN** 分支守 266 而非重算的 240；极窄（SE=473）**编辑区退让**（面板保 240、`floor=233/upper=240`）；复原 `seBack=Y / hit=Y` | 是（四段 `7CBDF116`/`11AEB455`/`4E0A6EFD`/`698E30C6` 两版逐字相同） | ☑ |
| F26 | 变量插入 | **实测**（v1/v2 双跑） | 双击插入光标处 | 是（28 项业务指纹逐项一致；disabled 分支不可达；`.insert-button` 守卫为共用件死分支） | ☑ |
| F27 | 二进制展示 | **实测**（v1/v2 双跑 Suite P1+P2，每版恰 1 条写库） | 图片分支：`el-image` data URI 直填（`404.png` len=68926、1100×731、`objFit=fill`）；非图片分支：提示文案 + `imageCount=0/cm=0` + 「测试」按钮缺席；图标 `#icon-file-image` vs `#icon-file-unknown` | 是（P1 `B7EF1754`/894、P2 `2312EB5A`/572 两版逐字相同；X=空集且实测亦等） | ☑ |
| F28 | 空态引导 | **实测**（v1/v2 双跑 Suite P0，自建夹具空组，零写入） | `el-empty` 两行文案（「暂无数据」+ 右键新建引导）+ 空编辑器态；空白处真实右键开三项菜单，且引导路径可端到端新建成功 | 是（P0 `C4A4FDA8`/346 两版逐字相同；文案哈希 `ECCFA594_30`、菜单文本串两版同值） | ☑ |
| F29 | 暗色与层级 | **实测**（v1/v2 双跑 Suite H，亮+暗） | 取色全走 EP 语义变量；z-index 9999 压过抽屉 | 是（四段行为串逐字全等；真实差异仅 x1 限高滚动、x2 Esc 通道；「无串扰」经 x5 实测为两版同形，已从差异项移出） | ☑ |
| F30 | tab 标签构成与红点 | **实测**（v1/v2 双跑专项 R suite） | 图标+名+红点，红点与 F06 同字段 | 是（off/on/off 同帧一致；重开后树与 tab 徽标同为 0） | ☑ |

> F08/F09 已转为实测（2026-11-24）：靶点为组 80 自建 fixture（id 900001-900016），跑完已物理清理并回到基线指纹 `f7114ea90fe03539c389580d3163e6a9`（cnt=78 / sum_id=170845 / sum_parent=165301 / is_delete=1 计 6 行）；存量 78 行模板未动。实测过程中新发现的四条共有缺陷登记在设计文档「对照期已知共有缺陷」K1-K4。
>
> F04 已转为实测（2026-11-24）：零写入（唯一请求为 `GET /gen/template/tree`），数据侧复核 cnt=78 / sum_id=170845 / sum_parent=165301 / is_delete=1 计 6 行，与 F08/F09 收尾基线一致。**注意**：历史 fp `f7114ea9…` 的计算式在更早的会话里，当前会话记录（jsonl）中已检索不到 `MD5(`/`GROUP_CONCAT` 原文，无法逐式复现。自本条起固定指纹 SQL，后续一律用它比对：
>
> ```sql
> SELECT COUNT(*) AS cnt, IFNULL(SUM(id),0) AS sum_id, IFNULL(SUM(parent_id),0) AS sum_parent, SUM(is_delete = 1) AS deleted,
>        MD5(GROUP_CONCAT(CONCAT(id,'|',file_name,'|',parent_id,'|',is_delete,'|',IFNULL(template_content,'')) ORDER BY id SEPARATOR '##')) AS fp
> FROM gen_template WHERE template_group_id = 80;
> ```
>
> 该式当前值：`fp=d07878b5db8bea76510d99946695ae6f`（cnt=78 / sum_id=170845 / sum_parent=165301 / deleted=6）。
>
> F22-F26 已转为 v1/v2 双跑实测（2026-11-24）：取证方式为同一份探针脚本分别在两版抽屉各跑一轮（B 插入 suite 28 项指纹、D 面板结构 13 项、E 关抽屉复位 11 项），逐项比对指纹串；两版身份由 `.tree-panel` 可见数自证（v2=1 / v1=0）。全程零写入：本轮网络仅 `GET /gen/template/tree`，无 `updateContent*`/`deleteList`/`updateParent`；结束后固定指纹 SQL 复核 `fp=d07878b5db8bea76510d99946695ae6f` 与 F04 收尾值逐字相同，现场 0 抽屉 / 0 弹窗 / 0 遮罩。
>
> F06/F30 已转为 v1/v2 双跑实测（2026-11-24 R suite，同一份脚本只换入口按钮文案）：靶点节点 `BaseUserVO.java`（id 2142）。编辑手法 `cm.focus()` + `document.execCommand('insertText', false, '@@RED@@')`，回退为等次 `execCommand('delete')` ×7——CM6 走 beforeinput，内容与哈希可完全复原（`origHash` 前后同为 `89EA3AEA`）。v2 `fpHash=A45AAC0F`/len=534，v1 `fpHash=14A215EC`/len=507，差值 27 全部来自版本身份项 `R0_holder`（v2=`tree-v2+tree-panel+editor-panel`，v1=`tree`），其余 21 项行为指纹逐字相同。零写入：本轮请求号 906-939 全为 `GET /gen/template/tree`，最后一条写请求仍是上一轮的 905；固定指纹 SQL 复核 `fp=d07878b5db8bea76510d99946695ae6f` 未变；结束现场 0 抽屉 / 0 弹窗 / 0 遮罩。
>
> **探针口径提醒（本轮新增，后续双跑必须沿用）**：`template-group/index.vue` 是**同时挂载** `<template-tree>` 与 `<template-tree-v2>` 两套组件（各自持有一个 `el-drawer`），入口按钮只决定谁被打开。因此读取「当前 state」必须从根实例 BFS 全组件树、再按 `state.visible === true` 过滤候选：首版探针用 `el.__vueParentComponent` 遍历 DOM，只能命中 EP 组件与 slot owner，v1 的 `tree.vue` 实例压根不在其中（holder=null 直接报错）；改为 BFS 后又出现 v2 实例排在 v1 之前的风险。`R0_visCand` 即这条校验的落档：v2=3（tree-v2 / tree-panel / editor-panel 命中同一 state 对象，本身也是共享引用的正面证据）、v1=1。
>
> 踩坑记录：关抽屉的保存确认按钮是「是(Y)/否(N)」而非「确定/取消」，早先用 `/确定|取消/` 兜底的清理逻辑对它无效，导致 D4 的复位断言路径不可信，已作废并重跑为 E 系列（显式点「否(N)」丢弃）。本轮所有丢弃路径均显式匹配「否(N)」，未触碰「是(Y)」。

> **F02/F03/F07/F14/F16/F19 已转为 v1/v2 双跑实测（2026-11-24 Suite S，状态机批次）**：同一份五段脚本只换入口按钮文案，靶点为组 80 前四个 DFS 文件 `2891:1111.java / 2142:BaseUserVO.java / 2865:2222.java / 2136:BaseEntity2.java`。**注意入口文案是 `模板v2`（中间无空格）**，S0 的按钮定位走 `innerText.trim() === text` 精确匹配，写成「模板 v2」会直接 `ABORT_BTN_MISSING`。
>
> - **脚本一致性**：套件磁盘态 `45522` 字节 / `41842` 字符、源文本 FNV-1a=`6E454F2D`；页内读取同一文件算得 `41842 / 6E454F2D`，**两边逐字符全等**后才重装（无 BOM，首字符 `/`），因此下列指纹值可脱离会话按该文件复现。
> - **五段行为串两版逐字全等**（`fpBehav` = 只含业务断言项，版本身份项不参与）：
>
> | 段 | 覆盖 | 行为串 `fpBehav` | len | v2 | v1 |
> |----|------|-----------------|-----|----|----|
> | S0 | 预检 + F01 + 基线快照（零写入） | `FE999DBB` | 425 | ✓ | ✓ |
> | S1 | F19 保存落库链路 | `FAEE0F60` | 441 | ✓ | ✓ |
> | S2 | F14 单 tab + F16 批量（零写入） | `662F9F06` | 1031 | ✓ | ✓ |
> | S3 | F16 `是(Y)` 批量落库 | `F59C86C6` | 792 | ✓ | ✓ |
> | S4 | F02 文案 + F03 复位 + F07 两分支 | `91013E7D` | 1665 | ✓ | ✓ |
>
> - **身份串差异均可逐项解释**（不属行为差异）：S0 `fpId` v2=`8331E505` / v1=`ACF1A2DE`，差异字段为 `P_ver_treePanel`（1/0，v2 有 `tree-panel.vue`）、`P_holder`（v2=`tree-v2+tree-panel+editor-panel\|tree` / v1=`tree-v2\|tree`）、`P_refShare`（v2=`3/objCount=2` / v1=`1/objCount=2`）——v2 三个子组件命中**同一个 state 对象**而 v1 只有一个，这既是拆分正确性的正面证据也是共享引用的证据；`P_visCand=1`、`P_rowIdx=4/P_rowCount=10`、`P_kind` 两版同形。S2 `fpId` v2=`A8146BA2` / v1=`912F90C4`，唯一差异是 `F16_menuDom`：v2=`context-menu|2|2`、v1=`context-menu|0|2`，即 v1 菜单项是裸 `li`、v2 经 ContextMenu 后菜单项带 `.context-menu__item` 类（属 F15 预期差异族）；中段菜单文本序列与弹窗行为全等。S4 无身份项（`Istr=""`），两版 `fpId` 同为空串基值 `811C9DC5`。
> - **写库与复原**：仅 S1（单条 `updateContent` ×2）与 S3（`updateContentBatch` ×1 + 逐条 `updateContent` 复原 ×2）发生写入；S0/S2/S4 零写入。固定指纹 SQL 在 **S1 后、S3 后、v1 S1 后、v1 S3 后、收尾** 五次复核，均为 `cnt=78 / sum_id=170845 / sum_parent=165301 / deleted=6 / fp=d07878b5db8bea76510d99946695ae6f`，与 F04/F22-F26/F06-F30 各轮收尾值逐字相同。
> - **现场清零**：收尾 `drawerVisible=0 / overlayVisible=0 / msgBoxVisible=0 / msgVisible=0`。`document.querySelectorAll('.el-drawer')` 总数为 3 但均为 `destroy-on-close` 后的隐藏壳；`.el-tabs__item` 可见 5 个已定性为顶部多页签栏（项目管理/表管理/枚举管理/模板组管理/首页，全部 `out-of-drawer`），非抽屉残留。
> - **本轮新增共有缺陷 K6**（两版行为一致，非 v2 回归）：S2 点 `否(N)` 丢弃后 `F14_noBranch=.../dot=on,NO_BADGE/edited=Y`、尾部 `treeDots=2`，即 **tab 已闭但节点 `isEdited` 仍为 true 且树红点残留**；源码定位 `tree.vue` L1315-1322（`handleCloseTabs` 的 `action === 'cancel'` 分支）只调 `closeTab()`，**既不清 `isEdited`、也不回滚 `templateContent`/`originalTemplateContent`**，而 `是(Y)` 分支 L1305-1308 两都做了（v2 已逐字核对同构：`use-template-tree-state.ts` L246-253 vs L236-239）。由于 tab 与树节点是同一对象引用（F06 `SAME_REF` 已证），红点会一直挂在树上；重开抽屉会被 `init` 重拉数据掩盖（S4 `F03_reopen` 的 `dots=0`）。已登记到设计文档「对照期已知共有缺陷」K6。

> **F05/F17/F18/F20/F21/F25 已转为 v1/v2 双跑实测（2026-11-24 Suite N，布局与交互批次）**：脚本 `2026-11-24-t2-suite-N-layout-interaction.js` 磁盘态 `40269` 字节 / `37668` 字符 / 源文本 FNV-1a=`667DBDA2`（11 段，页内 FileReader 读回算得同值、逐字符全等后才开跑），两版只换 `window.__BTN`，文件一字不改；外层动作仅三类：`setViewportSize`（1441→940→760→1441）、`locator.dragTo`、`keyboard.press('Escape')`，页内合成事件不作为证据。
>
> | 段 | 覆盖 | 行为串 `fpBehav` | len | v2 | v1 |
> |----|------|-----------------|-----|----|----|
> | N0a | F25 首次展开值 + 结构 recon（零写入） | `7CBDF116` | 206 | ✓ | ✓ |
> | N0b | F25 SE=599 保留 vs 重算 | `11AEB455` | 122 | ✓ | ✓ |
> | N0c | F25 SE=473 极窄退让 | `4E0A6EFD` | 131 | ✓ | ✓ |
> | N0d | F25 复原收口 | `698E30C6` | 61 | ✓ | ✓ |
> | N1 | F05 单击目录/未开文件/已开文件 | `3125E167` | 312 | ✓ | ✓ |
> | N2a | F17 前置（凑 4 tab） | `2992E4FE` | 172 | ✓ | ✓ |
> | N2b | F17 后置（真 drag 后读序） | `AC7F1D23` | 246 | ✓ | ✓ |
> | N3 | F18 路径前缀/省略/tooltip/跟随 | `B56547D8` | 270 | ✓ | ✓ |
> | N4 | F20 测试子抽屉结构与关闭后不变 | `075DEC26` | 292 | ✓ | ✓ |
> | N5a | F21 伪全屏铺满 + 非原生全屏 | `B51CBD46` | 108 | ✓ | ✓ |
> | N5b | F21 真 ESC 后状态 | `D558C5F3` | 148 | ✓ | ✓ |
>
> - **身份串差异均可逐项解释**：N0a `fpId` v2=`5C3DD64D` / v1=`0173286E`，差异字段只有 `btn=模板v2/模板` 与 `treePanel=Y/N`（v2 有 `tree-panel.vue`，v1 没有）；N4 `fpId` 差异来自 `N4_reqs` 里的请求时间戳与 `N4_goneMs`（v2=1516 / v1=382，同一关闭动作的耗时浮动，故已从行为串移到身份串）；其余段 `fpId` 两版全等。
> - **本轮新增共有缺陷 K7**（两版行为一致，非 v2 回归）：`initTabSortable` 的 `onEnd` 用 `evt.oldIndex! - 1` / `newIndex! - 1` 回写 `itemIds`，这套下标只在 tabs 渲染 `.el-tabs__active-bar`（`type="border-card"`）时才对齐；本页是 `type="card"`，实测 `hasActiveBar=false`，`.el-tabs__nav` 子节点全是 `.el-tabs__item`，于是 **-1 使整个映射错位一格**：把第 1 个 tab 拖到最后，预测序（按 `-1` 公式机械推算）与实测逐位吻合（`2891,2142,2865,2136` → `2142,2136,2865,2891`），**真正被移动的是被拖项的前一项**，且激活项不跟随（`activeIsMoved=N`）、Vue 数据序与 DOM 序不同步（再点 tab 后 DOM 序又变，`stable=N`）。源码同构证据：v2 `editor-panel.vue` L180-192 与 v1 `tree.vue` L369-382 逐字一致。未修（对照期只登记不修）。
> - **两条历史误记已纠正**（均为上一轮量测对象选错，不是行为变更）：① F05「目录仅勾选」→ 实测目录单击**不改变勾选状态**（`checkedFlip=N / checkedTotal 0>0`），只翻展开；② F25「266 不是首次展开值、是拖拽残留」→ 实测 `round(SE0×0.28)=round(949×0.28)=266` 正是首次展开值，上一轮量的是**编辑面板宽（711）**而非变量面板宽。
> - **环温噪声轴与探针教训（后续双跑必须沿用）**：
>   1. **合成帧饥饿**：本页面在 MCP 浏览器里 `requestAnimationFrame` 500ms 只跳 1 次，导致带 `transition` 的 transform 停在起始值（实测 `.el-drawer.rtl.open` 的 `translateX` 一度为 1441px，元素整体在视口外但 `checkVisibility()=true`）。因此 **布局断言一律不得用 `left/top`，只能用宽高 + 类名 + computed style**，且每段前需 `page.bringToFront()`。 Suite N 已按此改造（`N5a_fit` → `N5a_fitWH`，绝对坐标降为身份项）。
>   2. **嵌套子抽屉污染**：`test.vue` 的 `el-drawer` 未开 `append-to-body`，测试子抽屉整棵 DOM 嵌在主抽屉内部，首版 N4 的 `H.tabs / H.navDump / H.cm` 全子树查询把子抽屉的 `原始模板*/渲染结果` tab 和 cm-editor 混进了主抽屉读数，造成 `mainUnchanged=N / testDrawerGone=STILL` 两个**假告警**。受控实验证明内容 hash 三次相同（`7599F843_181`），行为无差异；已给三个 helper 加 `!closest('.template-test-drawer')` 排除。
>   3. **关闭延迟需轮询**：子抽屉从点 X 到 overlay 置隐实测 382～1516ms，固定 `sleep(900)` 会误报「关不掉」；已改为 `waitTestDrawerHidden(4000)` 并将耗时归入身份串。
>   4. **`epw` 的 28px 双稳**：`.variable-edge` 是占流元素，展开面板时分栏容器会释放这 28px，但释放时机受 `el-splitter` 过渡影响，实测 `N0c` 的 `epw` 在 233 / 261 之间跳（对应 `fpBehav` `7131EEEE` / `4E0A6EFD`），**属同一版本内噪声**；F25 真正断言的 `pw`（240/266）、`yield`、`floor/upper` 三项在两版、各次重跑中均稳定。若后续比对只在此字段出差异，先按噪声处理、重跑取同版本双值带。
> - **零写库自证**：11 段每段 `writeReq=NONE`；会话级（最后一次 reload 后）累计 15 个请求全部 GET，去重后仅 `/gen/template/tree`、`/gen/template/detail`、`/gen/template/variableCatalog`、`/gen/templateGroup/cascaderData` 四条读路径；固定指纹 SQL 跑批前/跑批后两次复核均为 `cnt=78 / sum_id=170845 / sum_parent=165301 / deleted=6 / fp=d07878b5db8bea76510d99946695ae6f`，与 F04/F06-F30/Suite S 各轮收尾值逐字相同；现场清零 0 抽屉 / 0 弹窗 / 0 toast / 视口回到 1441×770。
> - **两个已知不足**（不阻塞勾平）：① N1 选中的目录当时已展开，只覆盖到“展开→折叠”一个方向（展开方向另由 F04 命中自动展开旁证）；② N2b 只验证了一组 drag 路径（首→末），但拖前/拖中两种插入位置都走同一 `onEnd` 公式，K7 的机理探针已覆盖。

> **F11/F12/F13 已转为 v1/v2 双跑实测（2026-11-24 Suite G，表单通道批次，T2 唯一必须写库的批次）**：脚本 `2026-11-24-t2-suite-G-form-channel.js` 定版磁盘态 `37886` 字节 / `35533` 字符 / 源文本 FNV-1a=`BF4AFC07`（7 段，页内 FileReader 读回同值、逐字符全等后才开跑），两版只换 `window.__BTN`，文件一字不改。
>
> | 段 | 覆盖 | 行为串 `fpBehav` | len | v2 | v1 |
> |----|------|-----------------|-----|----|----|
> | G0 | F11/F12 节点右键三新建项矩阵（零写入） | `88E57805` | 381 | ✓ | ✓ |
> | G1 | F12 空白右键 + 文件「修改」回填（零写入） | `D7F1E192` | 300 | ✓ | ✓ |
> | G2 | F13 目录分支 `POST /add` | `9C2ACDAA` | 446 | ✓ | ✓ |
> | G3 | F12 路径注入 + F13 tabPush/激活/红点同步 | `ED183723` | 915 | ✓ | ✓ |
> | G4 | F13 修改分支 `PUT /update` | `F2A507BD` | 683 | ✓ | ✓ |
> | G5 | F11 二进制必填校验拦截（零写入） | `5DD215C0` | 174 | ✓ | ✓ |
> | G6 | 收口：关 tab / 关抽屉无确认（零写入） | `00CABAB4` | 126 | ✓ | ✓ |
>
> - **七段行为串共 62 行指纹、合计 3025 字符，两版逐字全等**。要点：入口三态的字段 visible 矩阵与 radio 预置项与 `form.vue` 声明一致（模板类型恒渲染 3 个 radio）；「修改」路径去尾段 `pathNoTail=Y`；空白右键 `pathIsRoot=Y`；G4 证明**描述更新不触碰编辑器文本**（`cmTextUnchanged=Y`）且 tab 与树的 `templateDesc` 同值（`same=Y`）；G3 证明新建文件的 tab 对象与 `initData` 后的树节点是**同一对象引用**（`refSame=Y`），故红点双向同步（`editOn tree=on/tab=on` → `editOff tree=off/tab=off`）。
> - **写库与复原**：每版恰 3 次写（`POST /add` ×2 + `PUT /update` ×1），只写自建 fixture `T2GDIR` / `T2GF.java`；存量 78 行未动（G1 的「修改」只读回填后取消，G3 的编辑探测逐字还原且不点保存）。v2、v1 各一轮，逐轮由外层 `DELETE FROM gen_template WHERE id IN (...)` 物理清理。固定指纹 SQL 在 **v2 跑批前、v2 清理后、v1 跑批前、v1 清理后** 四次复核均为 `cnt=78 / sum_id=170845 / sum_parent=165301 / deleted=6 / fp=d07878b5db8bea76510d99946695ae6f`，收尾 `fixture_left=0`；会话自证 v1 侧 10 请求中恰 3 条写、读路径仅 tree/variableCatalog/detail；现场清零 0 抽屉 / 0 弹窗 / 0 对话框 / 0 菜单 / 视口 1441×770。
> - **请求体两版逐字同构**（本批最有分量的等价证据）：新建走 **12 键纯表单**（`id:""`，不含 children/时间戳），两版 `bodyRaw` 除父 id 外全等；修改走 **18 键整份详情 VO 回传**，`G4_bodyFullLen` 两版同为 432，`templateContent` 以详情原文携带（实测 `EMPTY`，是带空串而非省略）、`hasId=true`。这条同时解释了「描述更新不会覆盖编辑器内容」，并证明 v2 的 CrudApi/useCrudForm 改造没有收窄或放宽字段集。
> - **身份串差异均可逐项解释**：G0/G1 的 `menuDom` v2=`context-menu|4|4` / v1=`context-menu|0|4`、`blankMenuDom` v2=`context-menu|3|3` / v1=`context-menu|0|3`（v1 菜单项是裸 `li`，属 F15 预期差异族）；`treePanel=Y/N`；`editorPanel` 两版同为 `N`（`editor-panel.vue` 根节点是 `el-container`，本就没有 `.editor-panel` 类名，不构成差异）；G3 `stateCand=2,kinds=v1,v2` / `1,kinds=v1`（页面同时挂载的 state 实例数，属环境量）；其余为新行自增 id 与请求时间戳。
> - **两条探针纪律（本批新增，后续必须沿用）**：
>   1. **请求体 spy 不得「先截断再解析」**：`PUT /update` 正文 432 字符，首版 spy 取 `slice(0,400)` 使 `bodyKeys`/`bodyContent` 双双 `UNPARSEABLE`，恰好废掉本批最关键的一条取证；改为「截断副本进 I 串、完整原文另存 `LASTFULL` 供 JSON 解析」。
>   2. **「挂载了几套组件实例」不是行为**：首版把 `cand=probe.all` 拼进行为串，造成 G3 `fpBehav` v2=`7D051FEE` / v1=`40E7A559` 的**假差异**（len 同为 922，逐行比对唯一差异就是 `cand=2` 与 `cand=1` 一位）；移入身份串后两版 `ED183723`/915 全等。
> - **本批未新增共有缺陷**；F11 原定的静态 props/emits 比对已由 G0 字段矩阵 + G5 校验拦截双证代替，可直接关闭。

> **F29 已转为 v1/v2 双跑实测（2026-11-24 Suite H，暗色与层级批次，全程零写入）**：脚本 `2026-11-24-t2-suite-H-dark-zindex.js` 定版磁盘态 `23574` 字节 / `20246` 字符 / 源文本 FNV-1a=`587FEF81`（5 段，页内 `f.text()` 读回同值、逐字符全等后才开跑）；两版只换 `window.__BTN`，文件与外层执行序列一字不改（外层只在 H1a→H1b 之间做真实 `mouse.move`，其余动作零）。
>
> | 段 | 覆盖 | 行为串 `fpBehav` | len | v2 | v1 |
> |----|------|-----------------|-----|----|----|
> | H0 | 亮色层级/取色/命中 + 点外关收口 | `E066C103` | 499 | ✓ | ✓ |
> | H1b | 真实 hover 与 EP 变量等值 | `E7E21F11` | 74 | ✓ | ✓ |
> | H2 | 暗色（翻 `html.dark`）同一套量测 + 自还原 | `9D4A93A1` | 543 | ✓ | ✓ |
> | H3 | 收口：关抽屉 + 暗色残留/现场自检 | `1F44806A` | 49 | ✓ | ✓ |
>
> - **四段行为串两版逐字全等**，且每一行的取色值都能与 EP 主题变量对得上：亮 `#fff / #dcdfe6 / #ebedf0 / #303133`，暗 `rgb(29,30,31) / rgb(76,77,79) / rgb(57,57,58) / rgb(229,234,243)`（与 v1 样式注释里自述的「明暗两套分别落到 #ebedf0 / #39393A」完全命中）。`H2_restored=LIGHT` + `H3_dark=N` 自证暗色探针不留环境残渣。
> - **身份串差异均可逐项解释**：`chain` v1=`ul.context-menu < … < el-drawer__body < el-drawer < …`（9 层、inDrawer=Y）/ v2=`div.context-menu < BODY`（directChildOfBody=Y）；`rootTag` `ul.context-menu` vs `div.context-menu`；`x5_menus` `ul/ul` vs `div/div`；`hoverPoint` v1=`141,131`/w=121 vs v2=`140,136`/w=119 —— 坐标差来自 v1 直接用光标坐标、v2 走 `computeMenuPosition`（F15 预期差异族）；**li 宽 121 vs 119 的 2px 已定位到源码**：v1 树菜单的 `<svg-icon icon="icon-edit">` 未写 size、走 svg-icon 默认 `1em`（= li 的 12px），而 v2 ContextMenu 统一渲染 `size="10px"`；v1 自己的 tab 菜单却已经是 `size="10px"`/`<el-icon size="10">`，即 **v1 两个菜单的图标尺寸本来就不一致，v2 将其归一**。
> - **本批新增两条探针纪律（后续双跑必须沿用）**：
>   1. **合成右键必须忠实于真实事件序列 `pointerdown → mousedown → contextmenu`**：v1 的外点关闭挂 `document mousedown`，v2 挂 `document pointerdown`（捕获），只发其中一种就会把「关闭通道实现不同」误读成「行为不同」（本批首版只发 mousedown，导致 v2 的外点关一次也没被验到）。
>   2. **指针停靠点不得由「本版本算出的坐标」带下一段**：H1 的 hover 坐标来自各自版本的 li 中心（两版差 5px），若指针原地不动直接跑 H2，菜单重开后一版命中 hover、一版没命中，item 的 bg/clr 就产生了 1 字符的**伪差异**（H2 `D9273DC2`/491 vs `D7053679`/492）；修正为“H2 前先把指针移到与版本无关的固定点 (5,400)”，并另补不依赖 hover 的 `H2_darkHoverVar` 作为暗色取色证据。
>   3. **变量等值不能比字面量**：`cs.backgroundColor`（`rgb(235,237,240)`）与 `getPropertyValue('--el-fill-color-dark')`（`#ebedf0`）直接字符串比较恒为 N，首版 `H1_fillDarkVar=...,match=N` 是废断言；改为用同一变量建一个探针元素、比两个 computed 值（`H.varAsRgb`）。
> - **本批未新增共有缺陷**；唯一新登记的结论是一条口径修正（「无菜单串扰」不是 v2 的行为优势，见正文 F29），已同步到设计文档的预期差异清单与风险表。

> **F27/F28 已转为 v1/v2 双跑实测（2026-11-24 Suite P，数据受阻项的实跑批次，每版恰 1 条写库）**：脚本 `2026-11-24-t2-suite-P-binary-empty.js` 定版磁盘态 `30548` 字节 / `27201` 字符 / 源文本 FNV-1a=`7E96AE8C`（4 段，页内 `f.text()` 读回同值、两版装载各核对一次后才开跑）；两版只换 `window.__BTN`，文件与外层执行序列一字不改，两版之间外层只做一件事：`DELETE FROM gen_template WHERE template_group_id=93` 把夹具组复位成空态。
>
> | 段 | 覆盖 | 行为串 `fpBehav` | len | v2 | v1 |
> |----|------|-----------------|-----|----|----|
> | P0 | F28 空态引导 + 空白右键三项（零写入） | `C4A4FDA8` | 346 | ✓ | ✓ |
> | P1 | F27 非图片分支（夹具 `fixture.jar`，本批唯一写） | `B7EF1754` | 894 | ✓ | ✓ |
> | P2 | F27 图片分支（组 79 存量 `404.png`，零写入） | `2312EB5A` | 572 | ✓ | ✓ |
> | P3 | 收口：现场清零 + 会话写账 | `E5E41DAA` | 321 | ✓ | ✓ |
>
> - **X 串（预期差异）跑批前即定为空集，实测也逐字相同**：`D6E0CC58`/36、`53E5B8CB`/38、`CBC46CE4`/25、`811C9DC5`/0。定空的依据是源码同构而不是「先跑跑看」：v1 `tree.vue` L68-81 ≡ v2 `tree-panel.vue` L56-69（`#empty` 插槽）、v1 L228-237 ≡ v2 `editor-panel.vue` L71-80（两分支），且新建表单是两版共用的同一个 `form.vue`。结构同源就不预先豁免任何一项，本批任何 F 差异都会被当作真回归或探针缺陷（实测未发生）。
> - **身份串差异四项，均可解释**：① `btn`（`模板v2` / `模板`，入口文案已从行为串移入 I）；② `blankMenuDom` v2=`div.context-menu` / v1=`ul.context-menu`（F15 预期差异族，同 Suite H）；③ `treePanel=Y/N`；④ P1 新建行的自增 id v2=`900124` / v1=`900125`（**只进 I 串**，见探针纪律②）。`P3_reqSeq` 两版逐字相同（9 个请求）。
> - **写库与复原（本批可控写入量：每版恰 1 条）**：唯一写请求 `POST /code-generator/gen/template/add`，只写自建夹具组 93 下的 `T2P-NOTIMAGE`。收尾 `DELETE FROM gen_template WHERE template_group_id=93` → `DELETE FROM gen_template_group WHERE id=93 AND group_name='__T2FIXTURE_EMPTY__'` 物理回收夹具。复核值：`gen_template_group` `cnt=54 / sum_id=3396 / max_id=92 / deleted=44`、`gen_template` 全表 `1596` 行、`fx_templates=0 / fx_group=0 / stray_fixtures=0`；组 80 固定指纹仍为 `cnt=78 / sum_id=170845 / sum_parent=165301 / deleted=6 / fp=d07878b5db8bea76510d99946695ae6f`，与 F04/F06-F30/Suite S/N/G 各轮收尾值逐字相同，即本批对既有数据零污染。
> - **三条探针纪律（本批新增，后续双跑必须沿用）**：
>   1. **写账不能按「非 GET 即写」判**：模板组分页查询是 `POST /gen/templateGroup/page`，语义上只读。首版据此把列表搜索记成写请求（`P2_writeReq count=1`、`P3_sessionWrites=4`），会让「本批只写了 1 条」这条取证失真。现按端点语义集判定：`/(page|list|detail|tree|cascaderData|variableCatalog|info|get|export)$/i` 都算读。修后 `P2_writeReq=NONE,count=0`、`P3_sessionWrites=1`。**这条口径同样适用于历史批次的写账复核**。
>   2. **本轮新建行的自增 id 绝不进行为串**：v2 试点新建节点拿到 900123，v1 轮必为 900124，首版把它拼进 `P1_tabDump` 就是自造假差异；改为 `P.tabNames()`（只名字+ACT），id 只进 I 串。
>   3. **`el-image` 的 `is-loading` 是时序双稳态**：data URI 的 `load` 可能早于 EP 监听器挂上，试点采到 `imgCls=el-image__inner is-loading` 而 `complete=true/natural=1100x731`。取类名前先 `waitImgSettled`，并把 `SETTLED/TIMEOUT` 本身落档，避免把「什么时候采」写进「采到了什么」。
> - **另两条环境事实**：① el-tree 的非虚拟渲染在此页表现为「**祖先未展开则子节点不进 DOM**」（组 79 抽屉首屏只有 15 个 `.el-tree-node`，而该组有 99 行），故点目标节点前需按 SQL 查出的父链 2015→2027→2111 自顶向下逐级点开，且**已展开的层绝不重复点**（点一下会折叠）；② 上传文件用页内 `new DataTransfer()` + `new File([固定 8 字节], 'fixture.jar', {type:'application/octet-stream'})` 赋给 `input.files` 再派发 `change` 即可复现 base64，无需外部文件与上传通道。
> - **本批未新增共有缺陷**；F27/F28 两项关闭同时意味着：原「拟移交 T4」的**数据受阻口径外项已清零**，T2 不再存在未跑项。`imageTypeList` 的 `'git'` 笔误与大写扩展名失配是两版共有的既有事实，登记为 T4 清理项而非回归。
