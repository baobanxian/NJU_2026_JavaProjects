import edu.nju.software.ast.*;
import edu.nju.software.eval.Evaluator;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;
import org.junit.Assert;
import org.junit.Test;

public class TcoTest {
    private void testFor(String input, LObject output) {
        Assert.assertEquals(output, Evaluator.eval(Parser.parse(Lexer.lex(new CharLookaheadStream(input)))));
    }

    // ── def + 基础 TCO ─────────────────────

    @Test
    public void testDef() {
        testFor("((def f (fn* (a) (if a (+ a (f (- a 1))) 0))) 5)", new LInteger(15));
    }

    @Test
    public void testTco_500() {
        testFor("((def f (fn* (a) (do 1 2 (if a (+ a (f (- a 1))) 0)))) 500)", new LInteger(125250));
    }

    @Test
    public void testTco_700() {
        testFor("((def f (fn* (a) (do 1 2 (if a (+ a (f (- a 1))) 0)))) 700)", new LInteger(245350));
    }

    // ── do/if/let* 回归 ────────────────────

    @Test
    public void testDo() {
        testFor("(do (* 1 2) (- 1 2) (+ 1 2))", new LInteger(3));
    }

    @Test
    public void testIf() {
        testFor("(if 0 1 (+ 1 -1))", new LInteger(0));
    }

    @Test
    public void testFnClosure() {
        testFor("(let* ((x 10) (f (fn* (y) (+ x y)))) (f 5))", new LInteger(15));
    }

    @Test
    public void testLet() {
        testFor("(let ((x 1)) x)", new LInteger(1));
    }

    @Test
    public void testLetStar() {
        testFor("(let* ((x (+ 1 2)) (y (* 2 x))) y)", new LInteger(6));
    }

    // ── 扩展1：= 和 < 比较函数 ─────────────

    @Test
    public void test_ext1_eq_true() {
        testFor("(= 3 3)", new LInteger(1));
    }

    @Test
    public void test_ext1_eq_false() {
        testFor("(= 3 4)", new LInteger(0));
    }

    @Test
    public void test_ext1_lt_true() {
        testFor("(< 3 5)", new LInteger(1));
    }

    @Test
    public void test_ext1_lt_false() {
        testFor("(< 5 3)", new LInteger(0));
    }

    @Test
    public void test_ext1_compare_in_if() {
        testFor("(if (= 1 1) 10 20)", new LInteger(10));
    }

    // ── 扩展2：defn 语法糖 ─────────────────

    @Test
    public void test_ext2_defn_basic() {
        testFor("((defn add (a b) (+ a b)) 3 4)", new LInteger(7));
    }

    @Test
    public void test_ext2_defn_recursive() {
        testFor("(do (defn sum (n) (if n (+ n (sum (- n 1))) 0)) (sum 10))", new LInteger(55));
    }

    @Test
    public void test_ext2_defn_tco() {
        testFor("(do (defn sum (n) (if n (+ n (sum (- n 1))) 0)) (sum 500))", new LInteger(125250));
    }
}