package statetracker;

import model.helpers.ActionTrackerDLLNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IndexTrackerDLLNodeTest {
    /*
    @Test
    public void spliceTests() throws Exception {
        ActionTrackerDLLNode fst = new ActionTrackerDLLNode(false, 0,0);
        ActionTrackerDLLNode lst = new ActionTrackerDLLNode(false, 1,fst,1);
        lst = new ActionTrackerDLLNode(true, 2, lst,2);
        lst = new ActionTrackerDLLNode(false, 3, lst,3);
        lst = new ActionTrackerDLLNode(true, 4, lst,4);

        ActionTrackerDLLNode curNode = fst;
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
    */
}
