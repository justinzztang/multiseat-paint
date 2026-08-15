package statetracker;

import model.helpers.IndexTrackerDLLNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IndexTrackerDLLNodeTest {
    @Test
    public void spliceTests() throws Exception {
        IndexTrackerDLLNode fst = new IndexTrackerDLLNode(false, 0,0);
        IndexTrackerDLLNode lst = new IndexTrackerDLLNode(false, 1,fst,1);
        lst = new IndexTrackerDLLNode(true, 2, lst,2);
        lst = new IndexTrackerDLLNode(false, 3, lst,3);
        lst = new IndexTrackerDLLNode(true, 4, lst,4);

        IndexTrackerDLLNode curNode = fst;
        curNode = curNode.next;
        curNode = curNode.next;
        curNode.spliceOut();
        assertEquals(0, fst.indexNumber);
        assertEquals(1, fst.next.indexNumber);
        assertEquals(3, fst.next.next.indexNumber);
        assertEquals(4, fst.next.next.next.indexNumber);
        assertNull(fst.next.next.next.next);

        System.out.println("debug");





    }
}
