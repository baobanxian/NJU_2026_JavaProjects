# Project 04 — Eval（求值器）

## 背景

词法分析将字符串切分为 Token 序列，语法分析将 Token 序列组织为 AST。下一步是**求值（Evaluation）**：遍历 AST，执行计算，得到最终结果。

例如，`(+ 1 2)` 经过词法分析和语法分析后得到 AST：

```
LList
├── LSymbol("+")
├── LInteger(1)
└── LInteger(2)
```

求值过程：
1. 这是一个非空列表，对每个元素求值
2. `LSymbol("+")` → 在全局变量中查找，得到加法函数 `LFunction`
3. `LInteger(1)` → 原子，返回自身
4. `LInteger(2)` → 原子，返回自身
5. 以加法函数调用 `call`，参数为 `(+ 1 2)` 的求值列表 → 返回 `LInteger(3)`

本次作业在 Project 03 AST 的基础上，实现 `Evaluator` 类中的求值逻辑和内建函数。

---

## 文件结构

```
04-eval/
├── pom.xml
├── README.md
└── src/
    ├── main/java/edu/nju/software/
    │   ├── stream/
    │   │   ├── CharLookaheadStream.java    ← 已实现
    │   │   └── TokenReadStream.java        ← 已实现
    │   ├── token/
    │   │   └── ...（已实现）
    │   ├── ast/
    │   │   ├── LObject.java               ← 已实现，抽象基类
    │   │   ├── LInteger.java              ← 已实现
    │   │   ├── LString.java               ← 已实现
    │   │   ├── LSymbol.java               ← 已实现
    │   │   ├── LList.java                 ← 已实现
    │   │   ├── LFunction.java             ← 已实现，函数对象
    │   │   └── Parser.java                ← 已实现
    │   └── eval/
    │       └── Evaluator.java             ← 你需要补充代码
    └── test/java/
        └── EvalTest.java                  ← 公开测试用例
```

---

## 执行规则

求值器 `eval(LObject obj)` 遵循以下四条规则：

| 规则 | 输入类型 | 行为 |
|------|---------|------|
| 1 | `LInteger` / `LString` | 原子：直接返回自身 |
| 2 | `LSymbol` | 符号查找：在 `globals` 哈希表中查找对应的值 |
| 3 | `LList`（空） | 空列表：直接返回自身 |
| 4 | `LList`（非空） | 函数调用：对每个元素求值，第一个元素作为函数，调用 `call` |

**规则 4 详解**（最关键）：

```
eval( LList[LSymbol("+"), LInteger(1), LInteger(2)] )
  ↓ 对每个子元素调用 eval
  LList[LFunction(+), LInteger(1), LInteger(2)]
  ↓ 取第一个元素作为函数，调用 call
  LFunction(+).call( LList[LFunction(+), LInteger(1), LInteger(2)] )
  ↓
  LInteger(3)
```

注意：`call` 的参数是**整个求值后的列表**（包含函数本身在 index 0），因此参数从 index 1 开始。

---

## 函数作为对象

`LFunction` 类将函数包装为 AST 节点：

```java
public class LFunction extends LObject {
    public final int ary;                          // 参数个数
    public final Function<LList, LObject> function; // 实际执行逻辑

    public LObject call(LList l) {
        if (l.content.size() != ary + 1)           // +1 因为 index 0 是函数本身
            throw new IllegalArgumentException();
        return function.apply(l);
    }
}
```

例如，加法函数 `+` 的 `ary = 2`，`call` 时列表长度应为 3（函数 + 两个操作数）。

---

## 内建全局量

| 名称 | 类型 | 说明 | 状态 |
|------|------|------|------|
| `+` | `LFunction(2, ...)` | 两个整数相加 | 已实现 |
| `-` | `LFunction(2, ...)` | 两个整数相减 | **TODO 1** |
| `*` | `LFunction(2, ...)` | 两个整数相乘 | **TODO 2** |
| `/` | `LFunction(2, ...)` | 两个整数相除（整数除法） | **TODO 3** |
| `nil` | `LList()` | 空列表常量 | 已实现 |
| `cons` | `LFunction(2, ...)` | 在列表前端插入元素 | **TODO 4** |
| `car` | `LFunction(1, ...)` | 取列表第一个元素 | **TODO 5**（扩展2）|
| `cdr` | `LFunction(1, ...)` | 取列表除第一个外的剩余部分 | **TODO 6**（扩展2）|

---

## 任务一：实现 `eval` 方法（必须完成）

打开 `Evaluator.java`，找到 `TODO 7`，实现 `eval` 方法。

### 伪代码

```
eval(obj):
    if obj 是 LSymbol:
        result = globals.get(obj)
        if result == null:
            throw RuntimeException("Unknown symbol: " + name)
        return result

    else if obj 是 LList:
        list = (LList) obj
        if list.content 为空:
            return obj
        对 list.content 中每个元素调用 eval，收集结果为新列表 evaluated
        func = (LFunction) evaluated.content.get(0)
        return func.call(evaluated)

    else:
        return obj   // LInteger, LString 直接返回
```

### 示例

```
eval("15")          → LInteger(15)     // 规则 1
eval("()")          → LList()          // 规则 3
eval("nil")         → LList()          // 规则 2：在 globals 中找到 nil
eval("(+ 1 2)")     → LInteger(3)      // 规则 4：函数调用
```

---

## 任务二：实现内建函数（必须完成）

在 `Evaluator.java` 的 `static { }` 块中，找到 TODO 1–4，实现四个内建函数。

### TODO 1：减法 `-`

参照已给出的 `+`，将运算符改为 `-`：

```java
globals.put(new LSymbol("-"), new LFunction(2,
        list -> new LInteger(
                ((LInteger) list.content.get(1)).value
                        - ((LInteger) list.content.get(2)).value)));
```

### TODO 2：乘法 `*`

同理，将运算符改为 `*`。

### TODO 3：除法 `/`

同理，将运算符改为 `/`（Java 整数除法自动向零取整）。

### TODO 4：`cons`

`cons` 接受两个参数：一个元素和一个列表，返回新列表（元素在前）。

```java
globals.put(new LSymbol("cons"), new LFunction(2, list -> {
    LObject toAdd = list.content.get(1);
    LList oldList = (LList) list.content.get(2);
    ArrayList<LObject> newList = new ArrayList<>();
    newList.add(toAdd);
    newList.addAll(oldList.content);
    return new LList(newList);
}));
```

示例：`(cons 3 nil)` → `LList(LInteger(3))`

---

## 任务三：扩展1 — `evalToString()`

找到 `TODO 8`，实现 `evalToString` 方法，将 `LObject` 转换为可读字符串。

### 转换规则

| AST 节点 | 输出 |
|---------|------|
| `LInteger(42)` | `"42"` |
| `LString("hello")` | `"\"hello\""` |
| `LSymbol("+")` | `"+"` |
| `LList()` | `"()"` |
| `LList(LInteger(1), LInteger(2))` | `"(1 2)"` |

### 提示

```java
if (obj instanceof LInteger) {
    return String.valueOf(((LInteger) obj).value);
} else if (obj instanceof LString) {
    return "\"" + ((LString) obj).value + "\"";
} else if (obj instanceof LSymbol) {
    return ((LSymbol) obj).name;
} else if (obj instanceof LList) {
    LList list = (LList) obj;
    if (list.content.isEmpty()) return "()";
    // 对每个子元素递归调用 evalToString，用空格连接，加括号
}
```

---

## 任务四：扩展2 — `car` 和 `cdr`

在 `static { }` 块中找到 TODO 5 和 TODO 6，实现两个列表操作函数。

### `car`（TODO 5）

取列表的第一个元素。`ary = 1`。

```
(car (cons 1 (cons 2 nil)))  → 1
```

提示：`((LList) list.content.get(1)).content.get(0)`

### `cdr`（TODO 6）

取列表去掉第一个元素后的剩余部分。`ary = 1`。

```
(cdr (cons 1 (cons 2 nil)))  → (2)
```

提示：用 `subList(1, size)` 获取子列表，再包装为新 `LList`。

---

## 运行测试

```bash
mvn test
```

### 公开测试用例说明

| 测试方法 | 测试内容 |
|---------|---------|
| `testAtom` | eval：整数原子返回自身 |
| `testEmpty` | eval：空列表返回自身 |
| `testAdd` | eval：`(+ 1 2)` → 3 |
| `testSub` | eval：`(- 5 3)` → 2 |
| `testMul` | eval：`(* 4 3)` → 12 |
| `testDiv` | eval：`(/ 7 2)` → 3 |
| `testNil` | eval：`nil` → 空列表 |
| `testCons` | eval：`(cons 3 nil)` → (3) |
| `testNested` | eval：`(cons 5 (cons 3 nil))` → (5 3) |
| `test_ext1_integer` | 扩展1：整数转字符串 |
| `test_ext1_string` | 扩展1：字符串转字符串（含双引号）|
| `test_ext1_empty_list` | 扩展1：空列表转字符串 |
| `test_ext1_simple_list` | 扩展1：列表转字符串 |
| `test_ext2_car` | 扩展2：car 取列表头 |
| `test_ext2_cdr` | 扩展2：cdr 取列表尾 |

**全部测试通过即表示本次作业完成。**
