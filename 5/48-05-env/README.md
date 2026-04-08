# Project 05 — Env（环境与绑定）

## 背景

在上一个项目中，我们实现了一个简单的求值器，但所有符号都定义在一个全局环境中（如 `+`、`-`、`cons` 等内置函数）。用户无法自定义变量。

在本项目中，我们引入 **环境（Environment）** 的概念，通过 `let` 和 `let*` 绑定表达式，允许用户在局部作用域中定义变量。此外，你还需要实现两个扩展功能：`if` 条件求值和 `begin` 顺序求值。

## 文件结构

```
src/
├── main/java/edu/nju/software/
│   ├── token/
│   │   ├── Token.java
│   │   ├── IntegerToken.java
│   │   ├── StringToken.java
│   │   ├── SymbolToken.java
│   │   ├── LeftParen.java
│   │   ├── RightParen.java
│   │   └── Lexer.java
│   ├── stream/
│   │   ├── CharLookaheadStream.java
│   │   └── TokenReadStream.java
│   ├── ast/
│   │   ├── LObject.java
│   │   ├── LInteger.java
│   │   ├── LString.java
│   │   ├── LSymbol.java
│   │   ├── LList.java
│   │   ├── LFunction.java
│   │   └── Parser.java
│   └── eval/
│       ├── Environment.java
│       └── Evaluator.java        ← 你需要修改的文件
└── test/java/
    └── EnvTest.java
```

## 环境（Environment）

`Environment` 类实现了一个带有父环境指针的符号表。查找符号时，先在当前环境中查找，找不到则沿 parent 链向上查找。

```java
public class Environment {
    private final Environment parent;
    private final HashMap<LSymbol, LObject> symbols = new HashMap<>();

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(LSymbol symbol, LObject value) {
        symbols.put(symbol, value);
    }

    public LObject lookup(LSymbol symbol) {
        if (symbols.containsKey(symbol)) return symbols.get(symbol);
        if (parent != null) return parent.lookup(symbol);
        throw new IllegalArgumentException("Invalid symbol: " + symbol);
    }
}
```

**要点：**
- 每次 `let`/`let*` 都会创建一个以当前环境为 parent 的新子环境
- `define` 在当前环境中添加绑定
- `lookup` 沿 parent 链查找，实现了**遮蔽（shadowing）**——子环境中同名变量会遮蔽父环境中的变量

## 执行规则

在给定环境 `env` 下对 AST 节点 `obj` 求值：

| 情况 | 规则 |
|------|------|
| `LInteger` / `LString` | 返回自身（字面量） |
| `LSymbol` | 在环境中查找：`env.lookup(symbol)` |
| `LList`（空列表） | 返回自身 |
| `LList` 首元素为 `let` | 处理普通绑定 → `handleLet(list, env, false)` |
| `LList` 首元素为 `let*` | 处理特殊绑定 → `handleLet(list, env, true)` |
| `LList` 首元素为 `if` | 条件求值 → `handleIf(list, env)` |
| `LList` 首元素为 `begin` | 顺序求值 → `handleBegin(list, env)` |
| `LList`（其他） | 对每个元素求值，第一个作为函数调用 |

## let 与 let* 的区别

`let` 和 `let*` 的语法形式相同：

```
(let  ((var1 expr1) (var2 expr2) ...) body)
(let* ((var1 expr1) (var2 expr2) ...) body)
```

**关键区别在于绑定求值时使用的环境：**

- **`let`**：所有绑定表达式都在**旧环境**中求值。绑定之间互不可见。
- **`let*`**：绑定表达式在**新环境**中依次求值。后面的绑定可以引用前面的绑定。

示例：

```
(let ((x 2) (y 3))
  (let ((x 7) (z (+ x y)))
    (* z x)))
=> 35
```

解释：内层 `let` 中，`z = (+ x y)` 在旧环境中求值，此时 `x=2, y=3`，所以 `z=5`。`(* z x)` 在新环境中求值，`z=5, x=7`，所以结果为 `35`。

```
(let ((x 2) (y 3))
  (let* ((x 7) (z (+ x y)))
    (* z x)))
=> 70
```

解释：内层 `let*` 中，`x=7` 先绑定到新环境。`z = (+ x y)` 在新环境中求值，此时 `x=7, y=3`，所以 `z=10`。`(* z x)` 结果为 `70`。

## 任务一：实现 eval 方法（必须完成）

在 `Evaluator.java` 中实现 `eval(LObject obj, Environment env)` 方法。

伪代码：

```
if obj 是 LSymbol:
    return env.lookup(symbol)
else if obj 是 LList:
    if 列表为空: return obj
    else if 第一个元素是 let:  handleLet(list, env, false)
    else if 第一个元素是 let*: handleLet(list, env, true)
    else if 第一个元素是 if:   handleIf(list, env)
    else if 第一个元素是 begin: handleBegin(list, env)
    else:
        对每个元素求值，得到新列表
        第一个元素作为 LFunction，调用 call(新列表)
else:
    return obj  （LInteger / LString 返回自身）
```

## 任务二：实现 handleLet（必须完成）

`handleLet(LList list, Environment env, boolean inplace)` 方法处理 `let` 和 `let*` 绑定。

步骤：
1. 取出绑定列表：`LList bindings = (LList) list.content.get(1)`
2. 创建子环境：`Environment newEnv = new Environment(env)`
3. 遍历每个绑定 `(var expr)`：
   - 取出变量名：`LSymbol name = (LSymbol) binding.content.get(0)`
   - 求值表达式：`LObject value = eval(binding.content.get(1), inplace ? newEnv : env)`
     - `let`（`inplace=false`）用旧环境 `env` 求值
     - `let*`（`inplace=true`）用新环境 `newEnv` 求值
   - 绑定到新环境：`newEnv.define(name, value)`
4. 在新环境中执行 body：`return eval(list.content.get(2), newEnv)`

## 任务三：扩展1 — if 条件求值

语法：`(if cond then else)`

求值规则：
1. 在当前环境下求值 `cond`
2. 如果结果**不是空列表**（即不是 nil / `()`），则求值并返回 `then` 分支
3. 否则，求值并返回 `else` 分支

**判断"假"的标准：** 只有空列表（`nil` 或 `()`）为假，其他一切值（整数、字符串、非空列表）都为真。

示例：

```
(if (cons 1 nil) 10 20)   → 10  （非空列表 = 真）
(if nil 10 20)             → 20  （nil = 空列表 = 假）
(if () 10 20)              → 20  （空列表 = 假）
(if 1 10 20)               → 10  （整数 = 真）
```

## 任务四：扩展2 — begin 顺序求值

语法：`(begin expr1 expr2 ... exprN)`

求值规则：
1. 在当前环境下依次求值每个表达式
2. 返回**最后一个**表达式的结果

示例：

```
(begin 1 2 3)            → 3
(begin (+ 1 2) (* 3 4))  → 12
(begin 42)               → 42
```

## 运行测试

在项目根目录下执行：

```bash
mvn test
```

测试用例一览（共 16 个）：

| 测试方法 | 描述 |
|----------|------|
| `testLet` | let 基础绑定 |
| `testCalculated` | let 绑定表达式求值 |
| `testLetMultiBindings` | let 多重绑定 |
| `testLetStar` | let* 基础（后绑定引用前绑定） |
| `testCalculation` | let* 绑定后运算 |
| `testNested` | 嵌套 let*/let 环境 |
| `testList` | 绑定后构造列表 |
| `testShadowing` | 变量遮蔽 |
| `test_ext1_if_true` | if：非空列表为真 |
| `test_ext1_if_false` | if：nil 为假 |
| `test_ext1_if_empty_list` | if：空列表为假 |
| `test_ext1_if_integer` | if：整数为真 |
| `test_ext1_if_with_let` | if 与 let 结合 |
| `test_ext2_begin_simple` | begin 返回最后一个值 |
| `test_ext2_begin_exprs` | begin 中包含表达式 |
| `test_ext2_begin_single` | begin 单个表达式 |

全部测试通过即表示本次作业完成。
