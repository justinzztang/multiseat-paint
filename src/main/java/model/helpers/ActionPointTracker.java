package model.helpers;

import java.util.ArrayList;
import java.util.Stack;


//successfully handles undo/redo tracking and overwrites
public class ActionPointTracker<E> {

    private ArrayList<E> undoPoints = new ArrayList<>();
    private ArrayList<E> redoPoints = new ArrayList<>();

    //undo points  in the future
    private Stack<E> unavailableUndos = new Stack<>();
    //redo points in the past
    private Stack<E> unavailableRedos = new Stack<>();
    //undo points in the past
    private Stack<E> availableUndos = new Stack<>();
    //redo points in the future
    private Stack<E> availableRedos = new Stack<>();

    public E getLatestUndoPoint() throws Exception {
        if(availableUndos.isEmpty()){
            throw new Exception("no available undos");
        }
        return availableUndos.peek();
    }

    public E getEarliestRedoPoint() throws Exception {
        if(availableRedos.isEmpty()){
            throw new Exception("no available redos");
        }
        return availableRedos.peek();
    }


    //every new stroke "resets" unavailable undos and available redos,

    public void addUndo(E point){
        //context: user just did an undoable action, so clear unavailable undos and available redos because those are overwritten now
        unavailableUndos.clear();
        availableUndos.push(point);
        //clear available redos
        availableRedos.clear();
    }

    public void addRedo(E point){
        //must be called AFTER addUndo
        assert(unavailableUndos.isEmpty());
        assert(availableRedos.isEmpty());
        //context: user just finished an undoable action
        unavailableRedos.push(point);
    }

    //user undid something
    public void undoUpdate(){
        //there exist available undos and unavailable redos
        assert(!availableUndos.isEmpty());
        assert(!unavailableRedos.isEmpty());
        //move last undo to unavailable cause its in the future now
        unavailableUndos.push(availableUndos.pop());
        //move last redo to available cause its in the future now
        availableRedos.push(unavailableRedos.pop());
    }

    public void redoUpdate(){
        //there exist available redos and unavailable undos
        assert(!availableRedos.isEmpty());
        assert(!unavailableUndos.isEmpty());
        //move last redo to unavailable cause its in the past now
        unavailableRedos.push(availableRedos.pop());
        //move last undo to available cause its in the past now
        availableUndos.push(unavailableUndos.pop());
    }



}
