package model.helpers;

import model.constants.CanvasConstants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

//successfully handles undo/redo tracking and overwrites
public class ActionPointTracker<E> {

    private ArrayList<E> undoPoints = new ArrayList<>();
    private ArrayList<E> redoPoints = new ArrayList<>();

    //for deques, last is the top of the stack

    //undo points  in the future
    private Deque<E> unavailableUndos = new ArrayDeque<>();
    //redo points in the past
    private Deque<E> unavailableRedos = new ArrayDeque<>();
    //undo points in the past
    private Deque<E> availableUndos = new ArrayDeque<>();
    //redo points in the future
    private Deque<E> availableRedos = new ArrayDeque<>();

    public E earliestUndo(){
        if(availableUndos.isEmpty()) return unavailableUndos.getLast();
        return availableUndos.getFirst();
    }

    public boolean unavailableUndosEmpty(){
        return unavailableUndos.isEmpty();
    }
    public boolean availableUndosEmpty(){
        return availableUndos.isEmpty();
    }
    public boolean unavailableRedosEmpty(){
        return unavailableRedos.isEmpty();
    }
    public boolean availableRedosEmpty(){
        return availableRedos.isEmpty();
    }

    public E getLatestUndoPoint() throws Exception {
        if(availableUndos.isEmpty()){
            throw new Exception("no available undos"); //TODO better exceptions?
        }
        return availableUndos.peekLast();
    }

    public E getEarliestUnavailableUndoPoint() throws Exception {
        if(unavailableUndos.isEmpty()){
            throw new Exception("no unavailable undos");
        }
        return unavailableUndos.peekLast();
    }


    public E getEarliestRedoPoint() throws Exception {
        if(availableRedos.isEmpty()){
            throw new Exception("no available redos");
        }
        return availableRedos.peekLast();
    }

    public E getLatestUnavailableRedoPoint() throws Exception {
        if(unavailableRedos.isEmpty()){
            throw new Exception("no unavailable redos");
        }
        return unavailableRedos.peekLast();
    }


    //every new stroke "resets" unavailable undos and available redos,

    public void addUndo(E point){
        //context: user just did an undoable action, so clear unavailable undos and available redos because those are overwritten now
        unavailableUndos.clear();
        availableUndos.addLast(point);
        while(availableUndos.size()> CanvasConstants.UNDO_LIMIT){
            availableUndos.removeFirst();
        }
        //clear available redos
        availableRedos.clear();
    }

    public void addRedo(E point){
        //must be called AFTER addUndo
        assert(unavailableUndos.isEmpty());
        assert(availableRedos.isEmpty());
        //context: user just finished an undoable action
        unavailableRedos.addLast(point);
        while(unavailableRedos.size() > CanvasConstants.UNDO_LIMIT){
            unavailableRedos.removeFirst();
        }
    }

    //user undid something
    public void undoUpdate(){
        //there exist available undos and unavailable redos
        assert(!availableUndos.isEmpty());
        assert(!unavailableRedos.isEmpty());
        //move last undo to unavailable cause its in the future now
        unavailableUndos.addLast(availableUndos.removeLast());
        //move last redo to available cause its in the future now
        availableRedos.addLast(unavailableRedos.removeLast());
    }

    public void redoUpdate(){
        //there exist available redos and unavailable undos
        assert(!availableRedos.isEmpty());
        assert(!unavailableUndos.isEmpty());
        //move last redo to unavailable cause its in the past now
        unavailableRedos.addLast(availableRedos.removeLast());
        //move last undo to available cause its in the past now
        availableUndos.addLast(unavailableUndos.removeLast());
    }

    public void remove(E point){ //inefficient, but # of operations so low it doesnt matter
        unavailableUndos.remove(point);
        unavailableRedos.remove(point);
        availableUndos.remove(point);
        availableRedos.remove(point);
    }

}
