package edu.nju.software.ast;

import java.util.*;
import java.util.stream.Collectors;

public class LList extends LObject {
    public final List<LObject> content;

    public LList(LObject... content) {

        this.content = Collections.unmodifiableList(Arrays.asList(content));
    }

    public LList(List<LObject> content) {
        this.content = Collections.unmodifiableList(new ArrayList<>(content));
    }

    public LList() {
        this.content = Collections.emptyList();
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
}
