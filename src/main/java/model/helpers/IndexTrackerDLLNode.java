package model.helpers;

import org.jspecify.annotations.NonNull;

import java.util.Iterator;

public class IndexTrackerDLLNode implements Iterable<IndexTrackerDLLNode>{
    public boolean isIndex;
    public int indexNumber;

    public IndexTrackerDLLNode prev;

    public IndexTrackerDLLNode next;

    public IndexTrackerDLLNode(boolean isIndex, int indexNumber){
        this.isIndex = isIndex;
        this.indexNumber = indexNumber;
        prev = null; //TODO definitely not good code
        next = null;
    }

    public IndexTrackerDLLNode(boolean isIndex, int indexNumber, IndexTrackerDLLNode prev){
        this.isIndex = isIndex;
        this.indexNumber = indexNumber;
        this.prev = prev;
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
    }

    @Override
    @NonNull
    public Iterator<IndexTrackerDLLNode> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public IndexTrackerDLLNode next() {
                return next;
            }
        };
    }
}
