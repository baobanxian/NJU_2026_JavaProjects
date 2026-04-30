# Project 07 — TCO（尾递归优化）

## 背景

在之前的项目中，`eval` 方法通过递归调用自身来求值嵌套表达式。当递归深度过大时（如 `(f 500)`），Java 调用栈会溢出，抛出 `StackOverflowError`。

**尾递归优化（Tail Call Optimization, TCO）** 的核心思想是：将递归转化为迭代。在 `eval` 内部使用 `while(true)` 循环，对于尾位置的表达式（如 `if` 的分支、`do` 的最后一项、`let*` 的 body），不递归调用 `eval`，而是将 AST 赋给循环变量，由下一轮循环继续处理。这样函数调用不会增加 Java 栈帧，从而避免栈溢出。

---

## 文件结构

```
07-tco/
├── pom.xml
├── README.md
└── src/
    ├── main/java/edu/nju/software/
    │   ├── stream/
    │   │   ├── CharLookaheadStream.java    ← 已实现
    │   │   └── TokenReadStream.java        ← 已实现
    │   ├── token/
    │   │   └── ...（已实现，7 个文件）
    │   ├── ast/
    │   │   ├── LObject.java               ← 已实现
    │   │   ├── LInteger.java              ← 已实现
    │   │   ├── LString.java               ← 已实现
    │   │   ├── LSymbol.java               ← 已实现
    │   │   ├── LList.java                 ← 已实现
    │   │   ├── Parser.java                ← 已实现
    │   │   ├── LFunction.java             ← 已实现（内置函数）
    │   │   └── LFunctionTco.java          ← 已实现（TCO 闭包，新增）
    │   └── eval/
    │       ├── Environment.java           ← 已实现
    │       └── Evaluator.java             ← 你需要补充代码
    └── test/java/
        └── TcoTest.java                   ← 公开测试用例（16 个）
```

---

## LFunctionTco 类

本项目新增 `LFunctionTco`，用于表示用户定义的闭包（替代之前用 `LFunction` + lambda 的方式）。

| 字段/方法 | 说明 |
|-----------|------|
| `params` | 参数列表（`LList`，如 `(a b)`） |
| `ast` | 函数体的 AST（不求值，延迟到调用时） |
| `env` | 闭包捕获的环境 |
| `getAst()` | 获取函数体 AST |
| `getEnv()` | 获取捕获的环境 |
| `genEnv(args)` | 创建绑定环境：以 `env` 为 parent，将 `params` 与 `args` 一一绑定 |

与 `LFunction` 的区别：`LFunction` 是一个 Java lambda，调用时直接执行；`LFunctionTco` 存储的是 AST，调用时由 `eval` 的 `while(true)` 循环迭代求值，从而实现 TCO。

---

## 新增规则：def

`def` 用于在全局环境中定义具名函数：

```lisp
(def f (fn* (a) (+ a 1)))
(f 5)  ;; → 6
```

实现要点：
1. 用 `handleFnStar` 创建 `LFunctionTco`，传入 `GLOBALS` 作为环境
2. 在当前 `env` 中定义函数名
3. **自引用**：在函数自身的环境中也定义函数名，使递归调用成为可能

```java
LFunctionTco f = handleFnStar((LList) list.content.get(2), GLOBALS);
env.define(name, f);
f.getEnv().define(name, f);  // 自引用
```

---

## TCO 修改规则

TCO 的核心修改：**尾位置的表达式不调用 `eval`，而是返回 AST，由外层循环继续处理。**

### 哪些位置是尾位置？

| 特殊形式 | 尾位置 |
|---------|--------|
| `(let* bindings body)` | `body` 是尾位置 |
| `(do e1 e2 ... en)` | `en`（最后一个）是尾位置 |
| `(if cond then else)` | `then` 和 `else` 都是尾位置 |
| `(fn* params body)` | `body` 是尾位置（在调用时） |
| 函数调用 `(f args...)` | 如果 `f` 是 `LFunctionTco`，其 body 是尾位置 |

### eval 的 while(true) 循环

```
while (true) {
    if obj 是 LSymbol → return env.lookup(obj)
    else if obj 是 LList → {
        空列表 → return obj
        let   → return handleLet(...)       // 不做 TCO
        let*  → obj = handleLetStar(...); continue  // TCO
        do    → obj = handleDo(...); continue       // TCO
        if    → obj = handleIF(...); continue       // TCO
        fn*   → return handleFnStar(...)
        def   → handleDef, return
        defn  → 转为 def, continue                  // 扩展2
        其他  → {
            eval 第一个元素：
            LFunctionTco → eval 参数, obj=f.getAst(), env=f.genEnv(args), continue
            LFunction    → eval 所有元素, return f.call(...)
        }
    }
    else → return obj  // 字面量
}
```

---

## TCO 前后对比

以 `handleIF` 为例：

**06-control（递归版）：**
```java
private static LObject handleIF(LList list, Environment env) {
    LObject cond = eval(list.content.get(1), env);
    if (isFalse(cond)) return eval(list.content.get(3), env);  // 递归 eval
    return eval(list.content.get(2), env);                     // 递归 eval
}
```

**07-tco（TCO 版）：**
```java
private static LObject handleIF(LList list, Environment env) {
    LObject cond = eval(list.content.get(1), env);
    if (isFalse(cond)) return list.content.get(3);  // 返回 AST
    return list.content.get(2);                      // 返回 AST
}
```

返回的 AST 会被 `while(true)` 循环中的 `obj = handleIF(...); continue;` 接住，在下一轮迭代中求值。

以函数调用为例：

**06-control（递归版）：**
```java
// 调用 LFunction 的 lambda
return f.call(evaluatedList);
```

**07-tco（TCO 版，LFunctionTco）：**
```java
// 不调用，而是设置循环变量
obj = f.getAst();
env = f.genEnv(args);
continue;  // 下一轮循环处理函数体
```

---

## 任务一：实现 TCO 版 handleLetStar / handleDo / handleIF

打开 `Evaluator.java`，完成 TODO 1、TODO 2、TODO 3。

### handleLetStar（TODO 1）

- 遍历 `bindings`，对每个绑定求值并在 `env` 中 `define`
- **不 eval body**，直接 `return list.content.get(2)`

### handleDo（TODO 2）

- 从 index 1 到 size-2，依次 `eval` 并丢弃结果
- **不 eval 最后一项**，直接 `return list.content.get(size-1)`

### handleIF（TODO 3）

- `eval` 条件表达式
- 假值（`LInteger(0)` 或空列表）→ `return list.content.get(3)`
- 真值 → `return list.content.get(2)`
- **不 eval 分支**，返回 AST

---

## 任务二：实现 handleFnStar（返回 LFunctionTco）

完成 TODO 4。

- 取出参数列表 `params` 和函数体 `body`
- 用 `env.deepClone()` 捕获当前环境
- 返回 `new LFunctionTco(params, body, captured)`

---

## 任务三：实现 eval（while(true) 循环 + def）

完成 TODO 5。这是本项目最核心的部分。

### eval 结构

```java
while (true) {
    if (obj instanceof LSymbol) {
        return env.lookup((LSymbol) obj);
    } else if (obj instanceof LList) {
        LList list = (LList) obj;
        if (list.content.isEmpty()) return obj;

        LObject head = list.content.get(0);

        if (SYMBOL_DEF.equals(head)) {
            // def: 定义具名函数
            ...
            return f;
        } else if (SYMBOL_LET.equals(head)) {
            return handleLet(list, env);
        } else if (SYMBOL_LET_STAR.equals(head)) {
            obj = handleLetStar(list, env);
            continue;
        } else if (SYMBOL_DO.equals(head)) {
            obj = handleDo(list, env);
            continue;
        } else if (SYMBOL_IF.equals(head)) {
            obj = handleIF(list, env);
            continue;
        } else if (SYMBOL_FN_STAR.equals(head)) {
            return handleFnStar(list, env);
        } else {
            // 函数调用
            LObject first = eval(head, env);
            if (first instanceof LFunctionTco) {
                // TCO: eval 参数，设置 obj 和 env，continue
                ...
            } else {
                // LFunction: eval 所有元素，call，return
                ...
            }
        }
    } else {
        return obj;
    }
}
```

### def 的实现

```java
LSymbol name = (LSymbol) list.content.get(1);
LFunctionTco f = handleFnStar((LList) list.content.get(2), GLOBALS);
env.define(name, f);
f.getEnv().define(name, f);  // 自引用，支持递归
return f;
```

### LFunctionTco 调用

```java
LFunctionTco f = (LFunctionTco) first;
// eval 所有参数
ArrayList<LObject> argsList = new ArrayList<>();
argsList.add(first);  // 占位，genEnv 从 index 1 开始取
for (int i = 1; i < list.content.size(); i++) {
    argsList.add(eval(list.content.get(i), env));
}
LList args = new LList(argsList);
obj = f.getAst();
env = f.genEnv(args);
continue;
```

### LFunction 调用

```java
LFunction f = (LFunction) first;
ArrayList<LObject> evaluated = new ArrayList<>();
evaluated.add(first);
for (int i = 1; i < list.content.size(); i++) {
    evaluated.add(eval(list.content.get(i), env));
}
return f.call(new LList(evaluated));
```

---

## 任务四：扩展1 — = 和 < 比较函数

完成 TODO 7（在 `static` 块中添加）。

### = 比较

```java
GLOBALS.define(new LSymbol("="), new LFunction(2,
        list -> new LInteger(
            ((LInteger) list.content.get(1)).value == ((LInteger) list.content.get(2)).value ? 1 : 0)));
```

### < 比较

```java
GLOBALS.define(new LSymbol("<"), new LFunction(2,
        list -> new LInteger(
            ((LInteger) list.content.get(1)).value < ((LInteger) list.content.get(2)).value ? 1 : 0)));
```

---

## 任务五：扩展2 — defn 语法糖

在 `eval` 的 `while(true)` 循环中处理 `SYMBOL_DEFN`。

`(defn name (params) body)` 等价于 `(def name (fn* (params) body))`。

实现方式：构造等价的 `def` 表达式，赋给 `obj`，`continue` 让下一轮循环处理。

```java
if (SYMBOL_DEFN.equals(head)) {
    LSymbol name = (LSymbol) list.content.get(1);
    LList params = (LList) list.content.get(2);
    LObject body = list.content.get(3);
    LList fnList = new LList(SYMBOL_FN_STAR, params, body);
    LList defList = new LList(SYMBOL_DEF, name, fnList);
    obj = defList;
    continue;
}
```

---

## 运行测试

```bash
mvn test
```

### 公开测试用例说明（16 个）

| 测试方法 | 测试内容 |
|---------|---------|
| `testDef` | def 定义递归函数 |
| `testTco_500` | TCO：递归深度 500 |
| `testTco_700` | TCO：递归深度 700 |
| `testDo` | do 顺序执行 |
| `testIf` | if 条件分支 |
| `testFnClosure` | fn* 闭包捕获 |
| `testLet` | let 绑定 |
| `testLetStar` | let* 顺序绑定 |
| `test_ext1_eq_true` | 扩展1：= 相等为真 |
| `test_ext1_eq_false` | 扩展1：= 不等为假 |
| `test_ext1_lt_true` | 扩展1：< 小于为真 |
| `test_ext1_lt_false` | 扩展1：< 不小于为假 |
| `test_ext1_compare_in_if` | 扩展1：= 配合 if 使用 |
| `test_ext2_defn_basic` | 扩展2：defn 基础调用 |
| `test_ext2_defn_recursive` | 扩展2：defn 递归 |
| `test_ext2_defn_tco` | 扩展2：defn TCO 深度 500 |

**全部测试通过即表示本次作业完成。**