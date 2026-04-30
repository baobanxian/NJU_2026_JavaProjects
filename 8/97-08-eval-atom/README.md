# Project 08 — EvalAndAtom（动态求值与可变引用）

## 背景

Lisp 语言的一个核心特性是**代码与数据同构（homoiconicity）**——代码本身就是数据结构（列表），可以在运行时动态构建并执行。通过 `read-string` 将字符串解析为 AST，再通过 `eval` 对 AST 求值，我们的解释器获得了元编程能力。

此外，本项目引入 `LAtom`——一种可变引用类型。此前我们的所有数据都是不可变的，`LAtom` 为解释器增加了受控的可变性，类似 Clojure 中的 `atom`。

---

## 文件结构

```
08-eval-atom/
├── pom.xml
├── README.md
└── src/
    ├── main/java/edu/nju/software/
    │   ├── token/
    │   │   ├── Token.java
    │   │   ├── IntegerToken.java
    │   │   ├── LeftParen.java
    │   │   ├── RightParen.java
    │   │   ├── StringToken.java
    │   │   ├── SymbolToken.java
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
    │   │   ├── LFunctionTco.java
    │   │   ├── LAtom.java              ← 新增：可变引用类型
    │   │   └── Parser.java
    │   └── eval/
    │       ├── Environment.java
    │       └── Evaluator.java           ← 你需要补充代码
    └── test/java/
        └── EvalAndAtomTest.java
```

---

## 代码与数据同构

在 Lisp 中，表达式 `(+ 1 2)` 既是一段代码（加法运算），也是一个列表数据结构。这意味着我们可以：

1. **`read-string`**：将字符串 `"(+ 1 2)"` 解析为 `LList(LSymbol("+"), LInteger(1), LInteger(2))`
2. **`eval`**：将上述 `LList` 作为代码在全局环境中执行，得到 `LInteger(3)`

```lisp
(eval (read-string "(+ 1 2)"))   ;; → 3
```

也可以用 `cons` 动态构建代码再执行：

```lisp
(eval (cons + (cons 1 (cons 2 nil))))   ;; → 3
```

---

## LAtom — 可变引用

`LAtom` 是一个包裹了可变引用的对象，提供以下操作：

| 函数 | 说明 | 示例 |
|------|------|------|
| `atom` | 创建 atom | `(atom 42)` |
| `atom?` | 判断是否为 atom | `(atom? (atom 1))` → `1` |
| `deref` | 获取当前值 | `(deref (atom 42))` → `42` |
| `reset!` | 设置新值 | `(reset! a 10)` → `10` |
| `swap!` | 用函数更新值 | `(swap! a + 1)` |

**计数器示例：**

```lisp
(let* ((counter (atom 0))
       (increment (fn* () (swap! counter + 1)))
       (get-count (fn* () (deref counter))))
  (do (increment) (increment) (increment)
      (get-count)))
;; → 3
```

---

## swap! 详解

`swap!` 是本项目中最复杂的函数。其语义为：

```
(swap! atom func arg1 arg2 ...)
```

等价于：

```
(reset! atom (func (deref atom) arg1 arg2 ...))
```

**实现步骤：**

1. 从参数列表中取出 `atom`（第 1 个参数）和 `func`（第 2 个参数）
2. 构建调用参数列表：`[func, atom.getRef(), arg3, arg4, ...]`
3. 根据 `func` 的类型分别调用：
   - 若 `func` 是 `LFunction`（内建函数），调用 `((LFunction) func).call(args)`
   - 若 `func` 是 `LFunctionTco`（用户定义函数），调用 `eval(f.getAst(), f.genEnv(args))`
4. 将结果设为 atom 的新引用，并返回结果

**示例：**

```lisp
(let* ((a (atom 5)))
  (swap! a * 2))       ;; 调用 (* 5 2) → 10，atom 变为 10
```

这里 `*` 是 `LFunction`，参数列表为 `[*, 5, 2]`。

```lisp
(let* ((a (atom 5))
       (double (fn* (x) (* x 2))))
  (swap! a double))    ;; 调用 (double 5) → 10，atom 变为 10
```

这里 `double` 是 `LFunctionTco`，需要通过 `eval(f.getAst(), f.genEnv(args))` 调用。

---

## 任务一：实现 read-string 和 eval

在 `Evaluator.java` 的 `static` 块中完成 TODO 1 和 TODO 2。

**read-string（TODO 1）：**
- 取第一个参数，强转为 `LString`，获取其 `value`
- 用 `Lexer.lex(new CharLookaheadStream(str))` 词法分析
- 用 `Parser.parse(...)` 语法分析
- 返回解析结果

**eval（TODO 2）：**
- 取第一个参数
- 调用 `eval(list.content.get(1))` 在全局环境下执行
- 返回执行结果

---

## 任务二：实现 atom / atom? / deref / reset!

在 `static` 块中完成 TODO 3 ~ TODO 6。

**atom（TODO 3）：** 取第一个参数，用 `new LAtom(...)` 包裹并返回。

**atom?（TODO 4）：** 取第一个参数，用 `instanceof LAtom` 判断。是返回 `new LInteger(1)`，否返回 `new LInteger(0)`。

**deref（TODO 5）：** 取第一个参数，强转为 `LAtom`，调用 `getRef()` 返回。

**reset!（TODO 6）：** 取两个参数——atom 和新值。调用 `atom.setRef(newRef)`，返回新值。

---

## 任务三：实现 swap!

在 `static` 块中完成 TODO 7。这是本项目的难点。

**步骤：**

1. 取出 atom（`list.content.get(1)`）和 func（`list.content.get(2)`）
2. 构建参数列表 `args`：
   ```java
   ArrayList<LObject> argsList = new ArrayList<>();
   argsList.add(func);                    // 位置 0：函数本身
   argsList.add(atom.getRef());           // 位置 1：atom 当前值
   for (int i = 3; i < list.content.size(); i++) {
       argsList.add(list.content.get(i)); // 位置 2+：额外参数
   }
   LList args = new LList(argsList);
   ```
3. 根据 func 类型调用：
   ```java
   LObject result;
   if (func instanceof LFunction) {
       result = ((LFunction) func).call(args);
   } else {
       LFunctionTco f = (LFunctionTco) func;
       result = eval(f.getAst(), f.genEnv(args));
   }
   ```
4. 更新 atom 并返回：
   ```java
   atom.setRef(result);
   return result;
   ```

---

## 任务四：扩展1 — list 函数

在 `static` 块中完成 TODO 8。

`list` 函数接受任意多个参数，返回由这些参数组成的 `LList`。

```lisp
(list 1 2 3)         ;; → LList(1, 2, 3)
(list)               ;; → LList()
(list (+ 1 2))       ;; → LList(3)
```

**提示：** 使用 `list.content.subList(1, list.content.size())` 获取所有参数（跳过位置 0 的函数自身）。

---

## 任务五：扩展2 — nth 函数

在 `static` 块中完成 TODO 9。

`nth` 函数接受一个 `LList` 和一个 `LInteger`（下标），返回列表中对应位置的元素。

```lisp
(nth (list 10 20 30) 0)   ;; → 10
(nth (list 10 20 30) 2)   ;; → 30
```

**提示：** 将第一个参数强转为 `LList`，第二个参数强转为 `LInteger`，用 `.content.get(index.value)` 取值。

---

## 运行测试

本项目包含 18 个公开测试用例，覆盖所有 9 个 TODO。

```bash
mvn test
```

测试分组：
- `testReadString` — read-string
- `testEval`, `testEval2` — eval
- `testAtomDeref`, `testAtomQ_true`, `testAtomQ_false` — atom 基本操作
- `testReset` — reset!
- `testSwapBuiltin`, `testSwapFn`, `testCounter`, `testToggleSwitch` — swap!
- `test_ext1_list_*` (4 个) — list 扩展
- `test_ext2_nth_*` (3 个) — nth 扩展
