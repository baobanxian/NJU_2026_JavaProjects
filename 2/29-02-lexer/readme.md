# Project 02 — Lexer（词法分析器）

## 背景

在解析程序的终极目的上，我们想要得到的是程序的语法结构。但在最开始时，我们所拥有的只是一个字符串，如 `(+ 1 2)`。想要分析这个表达式的结构，首先需要把字符串切分成有意义的词法单元（Token），这就是**词法分析**。

例如，`(+ 1 2)` 经词法分析得到：
```
左括号  符号(+)  整数(1)  整数(2)  右括号
```

本次作业在 Project 01 REPL 的基础上，实现词法分析器的核心逻辑，以及两个扩展功能。

---

## 文件结构

```
02-lexer/
├── pom.xml
├── README.md
└── src/
    ├── main/java/edu/nju/software/
    │   ├── stream/
    │   │   └── CharLookaheadStream.java   ← 已实现，字符前瞻流
    │   └── token/
    │       ├── Token.java                ← 已实现，抽象基类
    │       ├── IntegerToken.java         ← 已实现
    │       ├── StringToken.java          ← 已实现
    │       ├── SymbolToken.java          ← 已实现
    │       ├── LeftParen.java            ← 已实现
    │       ├── RightParen.java           ← 已实现
    │       └── Lexer.java                ← 你需要在此补充代码
    └── test/java/
        └── LexerTest.java               ← 公开测试用例
```

---

## Token 类型规范

| Token 类型 | 示例 | 正则描述 |
|-----------|------|---------|
| **整数** | `5`、`-42`、`007` | `^-?[0-9]+$` |
| **字符串** | `"hello"`、`"a\"b"` | `^"(?:[^"\\]|\\[\\"])*"$` |
| **左括号** | `(` | — |
| **右括号** | `)` | — |
| **符号** | `+`、`foo`、`let*`、`default-queue-size` | `^(?!-?[0-9]+)[^"() ]+$` |

解析规则均为**贪心匹配**，即尽可能长地匹配每个 Token。

---

## CharLookaheadStream 说明

`CharLookaheadStream` 提供三个方法：

| 方法 | 说明 |
|------|------|
| `input.eof()` | 流是否已到达结尾 |
| `input.consume()` | 读取并消耗下一个字符（位置向后移动）|
| `input.lookahead(1)` | 预览下一个字符，不消耗（不移动位置）|

---

## 任务一：基础版 `lex()`（必须完成）

打开 `Lexer.java`，在 `lex()` 方法的 `// TODO: YOUR CODE HERE` 处补充代码，将字符流解析为 Token 列表。

### 实现逻辑

```
while 流未结束:
    读取下一个字符 c
    if c 是空白 → 跳过
    if c == '(' → 加入 LeftParen
    if c == ')' → 加入 RightParen
    if c == '"' → 读取字符串（处理转义），加入 StringToken
    if c 是数字，或 c=='-' 且下一个字符是数字 → 读取整数，加入 IntegerToken
    else → 贪心读取直到特殊字符，加入 SymbolToken
```

### 字符串转义处理

字符串内部支持两种转义：
- `\\` → 反斜杠
- `\"` → 引号

使用 `StringEscapeUtils.unescapeJava(str)` 处理转义（已在 pom.xml 中引入 commons-text 依赖）。

### 示例

```
输入: "(+ 1 -2)"
输出: [LeftParen, SymbolToken(+), IntegerToken(1), IntegerToken(-2), RightParen]
```

---

## 任务二：扩展 1 — Token 计数 `countTokens()`

实现 `countTokens(ArrayList<Token> tokens)` 方法，统计列表中各类型 Token 的数量。

### 返回格式（严格匹配，含中文冒号）

```
整数:N 符号:N 字符串:N 左括号:N 右括号:N
```

### 示例

```java
countTokens(lex("(+ 1 2)"))
// → "整数:2 符号:1 字符串:0 左括号:1 右括号:1"

countTokens(new ArrayList<>())
// → "整数:0 符号:0 字符串:0 左括号:0 右括号:0"
```

> 提示：用 `instanceof` 判断类型，维护五个计数器，格式严格按照示例。

---

## 任务三：扩展 2 — Token 转字符串 `tokensToString()`

实现 `tokensToString(ArrayList<Token> tokens)` 方法，将 Token 列表转换回字符串，Token 之间用单个空格分隔。

### 各类型转换规则

| Token 类型 | 输出 |
|-----------|------|
| `IntegerToken(n)` | `n` 的数字字符串，如 `"1"`、`"-2"` |
| `StringToken(s)` | `"s"`（含双引号，不重新转义）|
| `SymbolToken(name)` | 符号名，如 `"+"` |
| `LeftParen` | `"("` |
| `RightParen` | `")"` |

### 示例

```java
tokensToString(lex("(+ 1 2)"))
// → "( + 1 2 )"

tokensToString(lex("nil"))
// → "nil"

tokensToString(new ArrayList<>())
// → ""
```

> 提示：使用 StringBuilder，第一个 token 前不加空格，其余每个 token 前加一个空格。

---

## 运行测试

```bash
mvn test
```

### 公开测试用例说明

| 测试方法 | 测试内容 |
|---------|---------|
| `testAdd` | 基础：`(+ 1 2)` 的词法分析 |
| `testNeg` | 基础：负整数 `-2` 的处理 |
| `testString` | 基础：字符串与转义序列 |
| `testNested` | 基础：多层嵌套表达式 |
| `test_ext1_count_basic` | 扩展1：`(+ 1 2)` 的 Token 计数 |
| `test_ext1_count_empty` | 扩展1：空列表计数 |
| `test_ext1_count_with_string` | 扩展1：含字符串的计数 |
| `test_ext1_count_only_symbol` | 扩展1：单符号计数 |
| `test_ext2_toString_simple` | 扩展2：`(+ 1 2)` 转字符串 |
| `test_ext2_toString_symbol_only` | 扩展2：单符号转字符串 |
| `test_ext2_toString_with_neg` | 扩展2：含负整数转字符串 |
| `test_ext2_toString_with_string` | 扩展2：含字符串 Token 转字符串 |
| `test_ext2_toString_empty` | 扩展2：空列表转字符串 |

**全部测试通过即表示本次作业完成。**
