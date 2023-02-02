package cse340.menus.views;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.List;

import cse340.menus.ExperimentTrial;
import cse340.menus.enums.State;

public abstract class MenuExperimentView extends AbstractMenuExperimentView {

    /**
     * Constructor
     *
     * @param context
     * @param trial Experiment trial (contains a list of items)
     */
    public MenuExperimentView(Context context, ExperimentTrial trial) { super(context, trial); }

    /**
     * Constructor
     *
     * @param context
     * @param items Items to display in menu
     */
    public MenuExperimentView(Context context, List<String> items) { super(context, items); }

    /**
     * Calculates the index of the menu item using the current finger position
     * This is specific to your menu's geometry, so override it in your Pie and Normal and Custom menu classes.
     *
     * Note that you should not be altering your menu's state within essentialGeometry.
     * This function should return a value to your touch event handler, and nothing more.
     *
     * @param p the current location of the user's finger relative to the menu's (0,0).
     * @return the index of the menu item under the user's finger or -1 if none.
     */
    protected abstract int essentialGeometry(PointF p);

    /***
     * Handles user's touch input on the screen. It should follow the state machine specified
     * in the spec.
     *
     * @param event Event for touch.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int menuItem = essentialGeometry(event);

        PointF point = new PointF(event.getX(), event.getY());

        switch (mState) {
            case START:
                startSelection(point); // Note State changes in this call
                return true;
            case SELECTING:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    updateModel(menuItem);
                    endSelection(menuItem, point);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    updateModel(menuItem);
                    return true;
                }
                // Note there is no break as the return calls function as those
                // Also if somehow we entered the SELECTING case, but not one
                // of the two above conditionals, then that is cause to enter
                // the default case and return false.
            default:
                return false;
        }
    }

    //////////////////////////////////////////////////
    // These methods are taken directly from the spec's description of the PPS
    //////////////////////////////////////////////////

    /**
     * Start the menu selection by recording the starting point and starting
     * a trial (if in experiment mode).
     * @param point The current position of the mouse
     */
    protected void startSelection(PointF point) {
        mState = State.SELECTING;
        if (experimentMode()) {
            getTrial().startTrial(point);
        }
        setVisibility(VISIBLE);
    }

    /**
     * Complete the menu selection and record the trial data if necessary
     * @param menuItem the menu item that was selected by the user
     * @param point The current position of the mouse
     */
    protected void endSelection(int menuItem, PointF point) {
        if (getCurrentIndex() == -1) {
            Toast.makeText(getContext(), getItem(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Selected " + getItem(), Toast.LENGTH_SHORT).show();
        }
        if (experimentMode()) {
            getTrial().endTrial(point, menuItem);
            getTrialListener().onTrialCompleted(getTrial());
        }
        setCurrentIndex(-1);
        setVisibility(INVISIBLE);
        mState = State.START;
    }

    /**
     * Change the model of the menu and force a redraw, if the current selection has changed.
     * @param menuItem the menu item that is currently selected by the user
     */
    protected void updateModel(int menuItem) {
        if (menuItem != getCurrentIndex()) {
            setCurrentIndex(menuItem);
            invalidate();
        }
    }
}
