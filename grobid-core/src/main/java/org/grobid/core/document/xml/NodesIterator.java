package org.grobid.core.document.xml;

import nu.xom.Node;
import nu.xom.Nodes;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * User: zholudev
 */
public class NodesIterator implements Iterable<Node>, Iterator<Node> {
    private Nodes nodes;
    private int cur;

    public NodesIterator(Nodes nodes) {
        this.nodes = nodes;
        cur = 0;
    }

    @Override
    public Iterator<Node> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return cur < nodes.size();
    }

    @Override
    public Node next() {
        if (cur == nodes.size()) {
            throw new NoSuchElementException();
        }

        return nodes.get(cur++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
