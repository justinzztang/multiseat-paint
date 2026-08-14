package statetracker;

import model.helpers.IndexTrackerDLLNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IndexTrackerDLLNodeTest {
    @Test
    public void spliceTests() throws Exception {
        IndexTrackerDLLNode fst = new IndexTrackerDLLNode(false, 0);
        IndexTrackerDLLNode lst = new IndexTrackerDLLNode(false, 1,fst);
        lst = new IndexTrackerDLLNode(true, 2, lst);
        lst = new IndexTrackerDLLNode(false, 3, lst);
        lst = new IndexTrackerDLLNode(true, 4, lst);

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
