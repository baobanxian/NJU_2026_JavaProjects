import edu.nju.software.ast.*;
import edu.nju.software.eval.Evaluator;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;
import org.junit.Assert;
import org.junit.Test;

public class EvalAndAtomTest {
    private void testFor(String input, LObject output) {
        Assert.assertEquals(output, Evaluator.eval(Parser.parse(Lexer.lex(new CharLookaheadStream(input)))));
    }

    // ── read-string ─────────────────────────

    @Test
    public void testReadString() {
        testFor("(read-string \"(+ 1 2)\")", new LList(new LSymbol("+"), new LInteger(1), new LInteger(2)));
    }

    // ── eval ────────────────────────────────

    @Test
    public void testEval() {
        testFor("(eval (read-string \"(+ 1 2)\"))", new LInteger(3));
    }

    @Test
    public void testEval2() {
        testFor("(eval (cons + (cons 1 (cons 2 nil))))", new LInteger(3));
    }

    // ── atom / deref ────────────────────────

    @Test
    public void testAtomDeref() {
        testFor("(deref (atom 42))", new LInteger(42));
    }

    @Test
    public void testAtomQ_true() {
        testFor("(atom? (atom 1))", new LInteger(1));
    }

    @Test
    public void testAtomQ_false() {
        testFor("(atom? 42)", new LInteger(0));
    }

    // ── reset! ──────────────────────────────

    @Test
    public void testReset() {
        testFor("(let* ((x (atom 1)) (y (reset! x 2)) (z (reset! x 1))) (+ (deref x) (+ y z)))", new LInteger(4));
    }

    // ── swap! ───────────────────────────────

    @Test
    public void testSwapBuiltin() {
        testFor("(let* ((a (atom 5))) (swap! a * 2))", new LInteger(10));
    }

    @Test
    public void testSwapFn() {
        testFor("(let* ((a (atom 5)) (double (fn* (x) (* x 2)))) (swap! a double))", new LInteger(10));
    }

    @Test
    public void testCounter() {
        testFor(
                "(let* ((counter (atom 0)) " +
                "       (increment (fn* () (swap! counter + 1))) " +
                "       (get-count (fn* () (deref counter)))) " +
                "  (do (increment) (increment) (increment) (get-count)))",
                new LInteger(3));
    }

    @Test
    public void testToggleSwitch() {
        testFor(
                "(let* ((switch (atom 0)) " +
                "       (toggle (fn* () (swap! switch (fn* (s) (if s 0 1)))))) " +
                "  (do (toggle) (toggle) (toggle) (deref switch)))",
                new LInteger(1));
    }

    // ── 扩展1：list ────────────────────────

    @Test
    public void test_ext1_list_basic() {
        testFor("(list 1 2 3)", new LList(new LInteger(1), new LInteger(2), new LInteger(3)));
    }

    @Test
    public void test_ext1_list_empty() {
        testFor("(list)", new LList());
    }

    @Test
    public void test_ext1_list_single() {
        testFor("(list 42)", new LList(new LInteger(42)));
    }

    @Test
    public void test_ext1_list_nested() {
        testFor("(list (+ 1 2) (* 3 4))", new LList(new LInteger(3), new LInteger(12)));
    }

    // ── 扩展2：nth ─────────────────────────

    @Test
    public void test_ext2_nth_first() {
        testFor("(nth (list 10 20 30) 0)", new LInteger(10));
    }

    @Test
    public void test_ext2_nth_last() {
        testFor("(nth (list 10 20 30) 2)", new LInteger(30));
    }

    @Test
    public void test_ext2_nth_with_cons() {
        testFor("(nth (cons 1 (cons 2 nil)) 1)", new LInteger(2));
    }
}
