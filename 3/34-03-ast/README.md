# Project 03 — AST（抽象语法树）

## 背景

词法分析将字符串切分为 Token 序列，下一步是**语法分析（Parsing）**：将 Token 序列组织为具有层次结构的**抽象语法树（AST）**。

例如，`(+ 1 2)` 的 Token 序列经过语法分析后得到：

```
LList
├── LSymbol("+")
├── LInteger(1)
└── LInteger(2)
```

本次作业在 Project 02 Lexer 的基础上，实现 `TokenReadStream` 和 `Parser`，以及两个扩展功能。

---

## 文件结构

```
03-ast/
├── pom.xml
├── README.md
└── src/
    ├── main/java/edu/nju/software/
    │   ├── stream/
    │   │   ├── CharLookaheadStream.java    ← 已实现
    │   │   └── TokenReadStream.java        ← 你需要补充代码
    │   ├── token/
    │   │   └── ...（已实现）
    │   └── ast/
    │       ├── LObject.java               ← 已实现，抽象基类
    │       ├── LInteger.java              ← 已实现
    │       ├── LString.java               ← 已实现
    │       ├── LSymbol.java               ← 已实现
    │       ├── LList.java                 ← 已实现
    │       └── Parser.java                ← 你需要补充代码
    └── test/java/
        └── AstTest.java                   ← 公开测试用例
```

---

## AST 节点类型

| 类 | 字段 | 含义 |
|----|------|------|
| `LInteger` | `public final int value` | 整数字面量 |
| `LString` | `public final String value` | 字符串字面量 |
| `LSymbol` | `public final String name` | 符号（变量名、运算符等）|
| `LList` | 通过 `getContent()` 访问 `List<LObject>` | 列表/S-表达式 |

---

## 任务零：实现 `TokenReadStream`（必须完成）

打开 `TokenReadStream.java`，实现两个方法：

| 方法 | 说明 |
|------|------|
| `next()` | 消耗并返回当前位置的 Token，位置向后移动 |
| `peek()` | 预览当前位置的 Token，不移动位置；到末尾时返回 `null` |

---

## 任务一：实现 `Parser`（必须完成）

打开 `Parser.java`，在 `// TODO: YOUR CODE HERE` 处补充代码。

### 方法说明

| 方法 | 说明 |
|------|------|
| `parse(tokens)` | 入口：创建 `TokenReadStream`，调用 `parseForm` |
| `parseForm(rdr)` | 决策：若下一个 Token 是 `LeftParen` → `parseList`，否则 → `parseAtom` |
| `parseList(rdr)` | 消耗 `(`，循环解析子元素，消耗 `)`，返回 `LList` |
| `parseAtom(rdr)` | 消耗一个 Token，按类型创建对应的 `LObject` |

### 实现逻辑

```
parseForm(rdr):
    if rdr.peek() 是 LeftParen → parseList(rdr)
    else → parseAtom(rdr)

parseList(rdr):
    rdr.next()              // 消耗 '('
    lst = []
    while rdr.peek() != null && rdr.peek() 不是 RightParen:
        lst.add(parseForm(rdr))
    rdr.next()              // 消耗 ')'
    return new LList(lst)

parseAtom(rdr):
    token = rdr.next()
    if token 是 IntegerToken → return new LInteger(token.value)
    if token 是 StringToken  → return new LString(token.value)
    if token 是 SymbolToken  → return new LSymbol(token.name)
```

### 示例

```
输入: "(+ 1 2)"
Token: [LeftParen, SymbolToken(+), IntegerToken(1), IntegerToken(2), RightParen]
AST:  LList[ LSymbol("+"), LInteger(1), LInteger(2) ]
```

---

## 任务二：扩展 1 — AST 转字符串 `astToString()`

实现 `Parser.astToString(LObject ast)` 方法，将 AST 节点转换回 Lisp 表达式字符串。

### 转换规则

| AST 节点 | 输出 |
|---------|------|
| `LInteger(n)` | `"n"`，如 `"42"`、`"-1"` |
| `LString(s)` | `"\"s\""`，含双引号，如 `"\"hello\""` |
| `LSymbol(name)` | 符号名，如 `"+"` |
| `LList([])` | `"()"` |
| `LList([...])` | `"(el1 el2 ...)"` 元素间空格分隔 |

### 示例

```java
astToString(parse(lex("(+ 1 2)")))    // → "(+ 1 2)"
astToString(new LString("hi"))         // → "\"hi\""
astToString(new LList())               // → "()"
```

> 提示：用 `instanceof` 判断类型；访问 `((LList) ast).getContent()` 遍历子节点。

---

## 任务三：扩展 2 — 统计原子节点数 `countAtoms()`

实现 `Parser.countAtoms(LObject ast)` 方法，递归统计 AST 中原子（非列表）节点的总数。

### 规则

- `LInteger`、`LString`、`LSymbol` → 计 1
- `LList([])` → 计 0
- `LList([...])` → 各子节点 `countAtoms` 之和

### 示例

```java
countAtoms(parse(lex("(+ 1 2)")))          // → 3  ("+", 1, 2)
countAtoms(parse(lex("(+ (- 1 2) 3)")))    // → 5  ("+", "-", 1, 2, 3)
countAtoms(new LList())                    // → 0
```

> 提示：用 `instanceof` 判断是否为 `LList`；使用递归调用 `countAtoms(child)`。

---

## 运行测试

```bash
mvn test
```

### 公开测试用例说明

| 测试方法 | 测试内容 |
|---------|---------|
| `testAtom` | 基础：整数原子解析 |
| `testAdd` | 基础：`(+ 1 2)` 列表解析 |
| `testNeg` | 基础：含负整数的列表解析 |
| `test_ext1_integer` | 扩展1：整数节点转字符串 |
| `test_ext1_neg_integer` | 扩展1：负整数节点转字符串 |
| `test_ext1_symbol` | 扩展1：符号节点转字符串 |
| `test_ext1_string` | 扩展1：字符串节点转字符串（含双引号）|
| `test_ext1_empty_list` | 扩展1：空列表转字符串 |
| `test_ext1_simple_list` | 扩展1：列表转字符串 |
| `test_ext2_integer` | 扩展2：整数原子计数 |
| `test_ext2_symbol` | 扩展2：符号原子计数 |
| `test_ext2_string` | 扩展2：字符串原子计数 |
| `test_ext2_empty_list` | 扩展2：空列表原子计数 |
| `test_ext2_simple_list` | 扩展2：列表原子计数 |

**全部测试通过即表示本次作业完成。**
