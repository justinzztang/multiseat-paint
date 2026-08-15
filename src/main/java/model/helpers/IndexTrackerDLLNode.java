package model.helpers;

import org.jspecify.annotations.NonNull;

import java.util.Iterator;

public class IndexTrackerDLLNode implements Iterable<IndexTrackerDLLNode>, Comparable<IndexTrackerDLLNode>{
    public boolean deleted = false;
    public boolean isIndex;
    public int indexNumber;
    public int id;

    public IndexTrackerDLLNode prev;

    public IndexTrackerDLLNode next;

    public IndexTrackerDLLNode(boolean isIndex, int indexNumber, int id){
        this.isIndex = isIndex;
        this.indexNumber = indexNumber;
        this.id = id;
        prev = null; //TODO definitely not good code
        next = null;
    }

    public IndexTrackerDLLNode(boolean isIndex, int indexNumber, IndexTrackerDLLNode prev, int id){
        this.isIndex = isIndex;
        this.indexNumber = indexNumber;
        this.prev = prev;
        this.id = id;
        if(prev!=null) prev.next = this;
        next = null;
    }

    public void spliceOut(){
        if(next == null){
            if(prev == null){
                return; //nothing happens
            }
            prev.next = null;
            return;
        }
        if(prev == null){
            if(next == null){
                return; //nothing
            }
            next.prev = null;
            return;
        }
        next.prev = prev;
        prev.next = next;
        deleted = true;
    }

    @Override
    @NonNull
    public Iterator<IndexTrackerDLLNode> iterator() {
        return new Iterator<>() {
            private IndexTrackerDLLNode current = IndexTrackerDLLNode.this;
            @Override
            public boolean hasNext() {
                return (current != null);
            }

            @Override
            public IndexTrackerDLLNode next() {
                IndexTrackerDLLNode res = current;
                current = current.next;
                return res;
            }
        };
    }

    @Override
    public int hashCode(){
        return this.id;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof IndexTrackerDLLNode node){
            return id == node.id;
        }
        return false;
    }

    @Override
    public int compareTo(IndexTrackerDLLNode o) {
        return indexNumber - o.indexNumber;
    }
}
