package model.helpers;

import model.paintActions.PaintAction;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;

public class ActionTrackerDLLNode implements Iterable<ActionTrackerDLLNode>, Comparable<ActionTrackerDLLNode>{
    public boolean deleted = false;
    public PaintAction paintAction;
    public int creationNumber;

    public ActionTrackerDLLNode prev;

    public ActionTrackerDLLNode next;

    public ActionTrackerDLLNode(PaintAction paintAction, int creationNumber){
        this.paintAction = paintAction;
        this.creationNumber = creationNumber;
        prev = null; //TODO definitely not good code
        next = null;
    }

    public ActionTrackerDLLNode get(int index){
        if(index == 0) return this;
        return get(index-1);
    }

    @Override
    @NonNull
    public Iterator<ActionTrackerDLLNode> iterator() {
        return new Iterator<>() {
            private ActionTrackerDLLNode current = ActionTrackerDLLNode.this;
            @Override
            public boolean hasNext() {
                return (current != null);
            }

            @Override
            public ActionTrackerDLLNode next() {
                ActionTrackerDLLNode res = current;
                current = current.next;
                return res;
            }
        };
    }

    @Override
    public int hashCode(){
        return this.creationNumber;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof ActionTrackerDLLNode node){
            return creationNumber == node.creationNumber;
        }
        return false;
    }

    @Override
    public int compareTo(ActionTrackerDLLNode o) {
        return creationNumber - o.creationNumber;
    }

    public static ActionTrackerDLLNode emptyNode = new ActionTrackerDLLNode(null, -1);
}
