package ru.rebelsouth.demo1.Analyzer;

import java.util.HashMap;
import java.util.Map;

public class PrefixTree {
    private static final PrefixTree instance = new PrefixTree();
    private final Map<Character, PrefixTree> children = new HashMap<>();
    private boolean isEnd = false;

    public static PrefixTree getInstance() {
        return instance;
    }

    public void clear() {
        children.clear();
        isEnd = false;
    }

    public boolean isEmpty() {
        return children.isEmpty() && !isEnd;
    }

    public void insert(String number) {
        PrefixTree current = this;
        for (char c : number.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new PrefixTree());
        }
        current.isEnd = true;
    }

    public boolean contains(String number) {
        PrefixTree current = this;
        for (char c : number.toCharArray()) {
            current = current.children.get(c);
            if (current == null) return false;
        }
        return current.isEnd;
    }

    @Override
    public String toString() {
        return toString("");
    }

    private String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (isEnd) {
            sb.append(prefix).append(" [КОНЕЦ]\n");
        }
        for (Map.Entry<Character, PrefixTree> entry : children.entrySet()) {
            sb.append(prefix).append("└── ").append(entry.getKey()).append("\n");
            sb.append(entry.getValue().toString(prefix + "    "));
        }
        return sb.toString();
    }
}


