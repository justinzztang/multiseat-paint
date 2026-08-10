package model;

import model.paintActions.Action;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tracks and handles actions and canvas states, allowing undoing/redoing functionality
 */
public class StateTracker {

    /** HashMap containing an action list for each user based on their ID */
    private HashMap<Integer, ArrayList<Action>> actionList = new HashMap<>();
}
