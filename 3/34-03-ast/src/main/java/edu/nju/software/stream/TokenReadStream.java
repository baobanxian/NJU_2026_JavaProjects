package edu.nju.software.stream;

import edu.nju.software.token.Token;

import java.util.ArrayList;

/**
 * Token 前瞻流：对 Token 列表提供顺序读取和预览功能。
 *
 * 需要实现的方法：
 *   - next()：消耗并返回当前位置的 Token，位置向后移动
 *   - peek()：预览当前位置的 Token，不移动位置；若已到末尾返回 null
 */
public class TokenReadStream {
    private final ArrayList<Token> value;
    private int position;

    public TokenReadStream(ArrayList<Token> val) {
        value = val;
        position = 0;
    }

    /**
     * 消耗并返回当前位置的 Token，位置向后移动一步。
     *
     * TODO: YOUR CODE HERE
     */
    public Token next() {
        if (position < value.size()) {
            return value.get(position++);
        }
        return null;
    }

    /**
     * 预览当前位置的 Token，不移动位置。
     * 若已到达流末尾，返回 null。
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：检查 position 是否超出 value.size()
     */
    public Token peek() {
        if (position < value.size()) {
            return value.get(position);
        }
        return null;
    }
}
