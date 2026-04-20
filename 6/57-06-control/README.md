# Project 06 — Control（控制流）

## 背景

在 Project 05 实现环境（Environment）和变量绑定的基础上，本次作业引入**控制流**：`do` 顺序执行、`if` 条件分支、`fn*` 函数闭包。这三个构造是程序语言中最基本的控制机制，有了它们，我们的 Lambda 演算解释器就具备了图灵完备的表达能力。

---

## 文件结构

```
06-control/
├── pom.xml
├── README.md
└── src/
    ├── main/java/edu/nju/software/
    │   ├── stream/
    │   │   ├── CharLookaheadStream.java    ← 已实现
    │   │   └── TokenReadStream.java        ← 已实现
    │   ├── token/
    │   │   ├── Token.java                  ← 已实现
    │   │   ├── IntegerToken.java           ← 已实现
    │   │   ├── LeftParen.java              ← 已实现
    │   │   ├── RightParen.java             ← 已实现
    │   │   ├── StringToken.java            ← 已实现
    │   │   ├── SymbolToken.java            ← 已实现
    │   │   └── Lexer.java                  ← 已实现
    │   ├── ast/
    │   │   ├── LObject.java               ← 已实现
    │   │   ├── LInteger.java              ← 已实现
    │   │   ├── LList.java                 ← 已实现
    │   │   ├── LString.java               ← 已实现
    │   │   ├── LSymbol.java               ← 已实现
    │   │   ├── LFunction.java             ← 已实现
    │   │   └── Parser.java                ← 已实现
    │   └── eval/
    │       ├── Environment.java           ← 已实现（含 deepClone）
    │       └── Evaluator.java             ← 你需要补充代码
    └── test/java/
        └── ControlTest.java               ← 公开测试用例（18 个）
```

---

## Environment 新增功能

相比 Project 05，`Environment` 新增了两个重要能力：

### 三参数构造器

```java
public Environment(Environment parent, LList binds, LList args)
```

用于函数调用时的参数绑定。`binds` 是形参列表 `(a b c)`，`args` 是调用时的参数列表（含函数本身在 index 0，实参从 index 1 开始）。

### deepClone

```java
public Environment deepClone()
```

递归深拷贝整个环境链。用于 `fn*` 创建闭包时，确保闭包捕获的环境不会被后续操作修改。

---

## 新增执行规则

### do — 顺序执行

```
(do expr1 expr2 ... exprN)
```

依次求值所有表达式，返回最后一个表达式的结果。

```
(do (* 1 2) (- 1 2) (+ 1 2))  → 3
(do 42)                         → 42
```

### if — 条件分支

```
(if cond then else)
```

先求值 `cond`，若为"假"则求值并返回 `else`，否则求值并返回 `then`。

```
(if 0 1 2)   → 2
(if 1 10 20) → 10
(if nil 10 20) → 20
```

### fn* — 函数闭包

```
(fn* (params...) body)
```

创建一个函数对象，捕获当前环境作为闭包。调用时将实参绑定到形参，在闭包环境中求值函数体。

```
((fn* (a) a) 123)        → 123
((fn* (a b) (+ a b)) 3 4) → 7
```

---

## "假"值约定

在本解释器中，以下两个值被视为"假"：

- `LList()` — 空列表（即 `nil`）
- `LInteger(0)` — 整数零

其他所有值均为"真"。`if`、`cond`、`when` 都使用此约定判断条件。

---

## fn* 与闭包

闭包（Closure）是指函数在创建时"捕获"了所在环境的变量绑定。例如：

```
(let* ((x 10)
       (f (fn* (y) (+ x y))))
  (f 5))
→ 15
```

`f` 在创建时捕获了 `x = 10`，即使后续环境发生变化，`f` 内部仍然能访问到 `x`。

**为什么需要 deepClone？** 因为 `let*` 会逐步向同一个环境对象中添加绑定。如果闭包直接引用环境对象而不拷贝，后续绑定的变量可能"泄漏"进闭包，导致错误行为。`deepClone` 确保闭包拥有创建时刻的环境快照。

---

## 任务一：实现 eval

在 `Evaluator.java` 的 `eval(LObject, Environment)` 方法中实现核心求值逻辑：

1. `LSymbol` → 在环境中查找变量值 `env.lookup(symbol)`
2. `LList`（空）→ 直接返回
3. `LList`（非空）→ 根据首元素分发：
   - `let` → `handleLet(list, env, false)`
   - `let*` → `handleLet(list, env, true)`
   - `do` → `handleDo(list, env)`
   - `if` → `handleIF(list, env)`
   - `fn*` → `handleFnStar(list, env)`
   - `cond` → `handleCond(list, env)`
   - `when` → `handleWhen(list, env)`
   - 其他 → 求值所有元素，取第一个作为函数调用
4. 其他类型 → 直接返回

---

## 任务二：实现 do / if / fn*

### handleDo

从 `i=1` 到 `size-2` 依次求值（丢弃结果），返回最后一个表达式的求值结果。

### handleIF

求值条件表达式，根据"假"值约定选择执行 `then` 或 `else` 分支。

### handleFnStar

取出参数列表和函数体，对当前环境执行 `deepClone`，返回一个 `LFunction`，其调用逻辑为：在捕获的环境上创建新环境，绑定形参到实参，求值函数体。

---

## 任务三：扩展1 — cond

`cond` 是多分支条件表达式，类似其他语言中的 `switch/case`：

```
(cond (test1 expr1) (test2 expr2) ... (else exprN))
```

- 依次求值每个 `test`，遇到第一个为"真"的，求值并返回对应 `expr`
- 如果 `test` 是 `else` 符号，则无条件执行对应 `expr`
- 所有 `test` 都为假且无 `else` → 返回 `LList()`

示例：

```
(cond (1 10) (1 20) (else 30))   → 10
(cond (0 10) (1 20) (else 30))   → 20
(cond (0 10) (0 20) (else 30))   → 30
(cond (0 10) (0 20))             → ()
```

---

## 任务四：扩展2 — when

`when` 是条件执行表达式，相当于没有 `else` 分支的 `if` + `do`：

```
(when test expr1 expr2 ... exprN)
```

- 先求值 `test`
- 若为"真"，依次求值 `expr1` 到 `exprN`，返回最后一个结果
- 若为"假"，不执行任何表达式，返回 `LList()`

示例：

```
(when 1 (+ 1 2) (* 3 4))  → 12
(when 0 (+ 1 2) (* 3 4))  → ()
```

---

## 运行测试

```bash
mvn test
```

### 公开测试用例说明（18 个）

| 测试方法 | 测试内容 |
|---------|---------|
| `testDo` | do：多表达式顺序执行，返回最后一个 |
| `testDoSingle` | do：单表达式 |
| `testIf_zero` | if：条件为 0（假），走 else |
| `testIf_true` | if：条件为 1（真），走 then |
| `testIf_nil` | if：条件为 nil（假），走 else |
| `testFn_identity` | fn*：恒等函数 |
| `testFn_add` | fn*：两参数加法 |
| `testFn_closure` | fn*：闭包捕获外部变量 |
| `testComplex` | 综合：do + fn* + if 嵌套 |
| `testEnvClosure` | 闭包环境隔离 |
| `testLet` | let 回归测试 |
| `testLetStar` | let* 回归测试 |
| `test_ext1_cond_first_true` | cond：第一个条件为真 |
| `test_ext1_cond_else` | cond：所有条件为假，走 else |
| `test_ext1_cond_no_match` | cond：无匹配且无 else |
| `test_ext1_cond_middle` | cond：中间条件为真 |
| `test_ext2_when_true` | when：条件为真，执行并返回最后一个 |
| `test_ext2_when_false` | when：条件为假，返回空列表 |

**全部测试通过即表示本次作业完成。**