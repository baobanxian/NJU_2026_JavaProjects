package edu.nju.software.ast;

import java.util.*;
import java.util.stream.Collectors;

public class LList extends LObject {
    private final List<LObject> content;

    public LList(LObject[] content) {
        this.content = Arrays.stream(content).collect(Collectors.toList());
    }

    public LList(ArrayList<LObject> content) {
        this.content = content;
    }

    public LList() {
        this.content = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LList)) return false;
        LList lList = (LList) o;
        return Objects.deepEquals(content, lList.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    public List<LObject> getContent() {
        return Collections.unmodifiableList(content);
    }

    public int size() {
        return content.size();
    }

    public LObject get(int index) {
        return content.get(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < content.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(content.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
}