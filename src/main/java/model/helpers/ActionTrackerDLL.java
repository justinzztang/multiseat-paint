package model.helpers;

import org.jspecify.annotations.NonNull;

import java.util.Iterator;

public class ActionTrackerDLL implements Iterable<ActionTrackerDLLNode>{

    public ActionTrackerDLLNode head;

    public ActionTrackerDLLNode end;

    public int size;

    public int size(){
        return size;
    }
    public boolean isEmpty(){
        return size==0;
    }
    public void addLast(ActionTrackerDLLNode node){

        if(isEmpty()){
            head = node;
            end = node;
        }
        else{
            this.end.next = node;
            node.prev = this.end.next;
            this.end = node;
        }

        size++;

    }

    public ActionTrackerDLLNode getFirst(){
        return head;
    }

    public ActionTrackerDLLNode getLast(){
        return end;
    }

    public ActionTrackerDLLNode get(int index){
        return head.get(index);
    }

    public void removeUsingNodeReference(ActionTrackerDLLNode node){
        node.deleted = true;
        if(node.next == null){ //its the end
            if(node.prev == null){ //its the only one
                //remove the node from the list and make everything null
                this.head = null;
                this.end = null;
                this.size = 0;
                return;
            }
            //otherwise its just the end and remove one
            this.end = node.prev; //new end
            node.prev.next = null;
            size--;
            return;
        }
        if(node. prev == null){ //its the head
            if(node.next == null){
                //remove the node from the list and make everything null
                this.head = null;
                this.end = null;
                this.size = 0;
                return;
            }
            this.head = node.next;
            node.next.prev = null;
            size--;
            return;
        }
        node.next.prev = node.prev;
        node.prev.next = node.next;
        size--;
    }


    @Override
    @NonNull
    public Iterator<ActionTrackerDLLNode> iterator() {
        return new Iterator<>() {
            private ActionTrackerDLLNode current = ActionTrackerDLL.this.head;
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
}
