package cse340.menus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.List;

import cse340.menus.ExperimentTrial;
import cse340.menus.TrialListener;
import cse340.menus.enums.State;

public abstract class AbstractMenuExperimentView extends View {

    /** The current trial. */
    private ExperimentTrial mTrial = null;

    /** The items displayed in this menu. */
    private List<String> mItems;

    /** The current state of this menu (selecting or not). */
    protected State mState;

    /** Standard paint objects for outlines, highlighted items, etc. */
    private Paint mTextPaint;
    private Paint mHighlightPaint;
    private Paint mBorderPaint;

    /** Display metrics used in to figure out scaling */
    protected final DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();

    /** Standard size for drawn text. */
    protected static final float TEXT_SIZE_RATIO = 0.028f;
    protected final int TEXT_SIZE;

    /**
     * Constant for threshold distance from starting point. Until this distance threshold
     * is crossed, the menu should night highlight an item as "selected"
     **/
    public static final float MIN_DIST_RATIO = 0.007f;
    protected final int MIN_DIST;

    /**
     * The currently selected index of your menu view. Your `onTouchEvent` function
     * should modify this (using the accessor/mutator) in order to record the menu's current state.
     */
    private int mCurrentIndex = -1;

    /**
     * You need to create a listener interface so the application can be notified when a selection
     * is made. This will trigger the move to the next trial.
     */
    private TrialListener mTrialListener = null;

    /**
     * Constructor
     *
     * @param context
     * @param items Items to display in menu
     */
    public AbstractMenuExperimentView(Context context, List<String> items) {
        super(context);

        this.mItems = items;

        TEXT_SIZE = (int) (TEXT_SIZE_RATIO * Math.min(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));
        MIN_DIST =  (int) (MIN_DIST_RATIO * Math.min(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));

        // default paint to draw/highlight menu.
        // you may change the paint styles if you'd like
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setStyle(Paint.Style.STROKE);
        mHighlightPaint.setColor(Color.RED);
        mHighlightPaint.setStrokeWidth(10);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.BLACK);
        mBorderPaint.setStrokeWidth(3);

        setup();
    }

    /**
     * Constructor
     *
     * @param context
     * @param trial Experiment trial (contains a list of items)
     */
    public AbstractMenuExperimentView(Context context, ExperimentTrial trial) {
        this(context, trial.getMenuContents());
        this.mTrial = trial;
    }

    /**
     * Method that will be called from the constructor to complete any set up for the view.
     * Should set the correct width and height for your menu and any other important parameters
     */
    protected abstract void setup();

    /**
     * Similar to onDraw, shifts the selection point so that the current finger position is
     * relative to to the menu's (0,0).
     *
     * Then asks essentialGeometry where (p) is. This should return the menu item that the finger
     * is in, or -1 if the finger has (a) moved less than MIN_DIST or (b) is not in any menu item.
     *
     * @param e The motion event being processed in onTouchEvent
     * @return the index of the menu item under the user's finger or -1 if none.
     */
    protected int essentialGeometry(MotionEvent e) {
        PointF currentPoint = new PointF(e.getX(), e.getY());

        // Call essentialGeometry and return the result
        return essentialGeometry(currentPoint);
    }

    /**
     * Calculates the index of the menu item using the current finger position
     * This is specific to your menu's geometry, so override it in your Pie and Normal menu classes.
     *
     * Note that you should not be altering your menu's state within essentialGeometry. This function
     * should return a value to your touch event handler, and nothing more.
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
    public abstract boolean onTouchEvent(MotionEvent event);

    /**
     * Start the menu selection by recording the starting point and starting
     * a trial (if in experiment mode).
     * @param point The current position of the mouse
     */
    protected abstract void startSelection(PointF point);


    /**
     * Complete the menu selection and record the trial data if necessary
     * @param menuItem the menu item that was selected by the user
     * @param point The current position of the mouse
     */
    protected abstract void endSelection(int menuItem, PointF point);


    /**
     * Change the model of the menu and force a redraw, if the current selection has changed.
     * @param menuItem the menu item that is currently selected by the user
     */
    protected abstract void updateModel(int menuItem);

    /**
     * Translate the canvas so that it is easier to draw relative to 0,0
     * Also only draw when the user has pressed (we are in the selecting state)
     *
     * @param canvas Canvas to draw on.
     */
    protected abstract void onDraw(Canvas canvas);


    /**
     * Method to announce a string using a Toast.
     * @param s The string to announce to the user in a toast
     */
    public void announce(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get the currently selected item from the item list based on the current index.
     * @return the currently selected item from the menu list.
     */
    protected String getItem() {
        if (getCurrentIndex() < 0) {
            return "Nothing Selected";
        }

        try {
            return getItems().get(getCurrentIndex());
        } catch (Exception e){
            return "Invalid Selection";
        }
    }

    /**
     * Determine if we are in experiment mode or not.
     * @return true if we are in the middle of an experiment, false if not
     */
    public boolean experimentMode() {
        return mTrial != null;
    }

    /**
     * Get the distance of this point from the origin.
     * @param p the point in question
     * @return the distance from the origin to this point.
     */
    protected double getDistance(PointF p) { return Math.sqrt(p.x * p.x + p.y * p.y); }
    /**
     * Get the distance between two points
     * @return the distance between two points
     */
    protected double getDistance(PointF p, PointF p2) { return Math.sqrt((p.x-p2.x)*(p.x-p2.x) + (p.y - p2.y)*(p.y-p2.y)); }


    /* ********************************************************************************************** *
     *                              Getters and Setters
     * ********************************************************************************************** */

    /**
     * Get the list of items in the menu.
     * @return a list of items that are being used in the menu.
     */
    public List<String> getItems() {
        return mItems;
    }

    /**
     * Get the currently selected index of your menu view.
     * @return The currently selected index of the menu view.
     */
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    /**
     * Set the currently selected index of your menu view (done in your menu's `onTouchEvent`)
     * @param newIndex The new index that should be stored as the current index for this view.
     */
    public void setCurrentIndex(int newIndex) {
        this.mCurrentIndex = newIndex;
    }

    /**
     * Get the current trial for this experiment. Note: this can possibly be null if there
     * is no current experiment.
     * @return The current trial that is being run in the experiment, or null if there is no
     * experiment running.
     */
    public ExperimentTrial getTrial() {
        return mTrial;
    }

    /**
     * Set the current trial for this experiment.
     * @param trial The current experiment trial to store for this menu
     */
    public void setTrial(ExperimentTrial trial) {
        this.mTrial = trial;
    }


    /**
     * Get the brush that handles painting the border of the menu. This is set in the
     * constructor but may be changed using setBorderPaint.
     * @return The brush that can be used to paint the border of the menu
     */
    public Paint getBorderPaint() {
        return mBorderPaint;
    }


    /**
     * Reset or change the brush that handles painting the border of the menu.
     * @param borderPaint The new brush that will be used to paint the border of the menu
     */
    public void setBorderPaint(Paint borderPaint) {
        this.mBorderPaint = borderPaint;
    }


    /**
     * Get the brush that handles painting the text of the menu. This is set in the
     * constructor but may be changed using setTextPaint.
     * @return The brush that can be used to paint the text of the menu
     */
    public Paint getTextPaint() {
        return mTextPaint;
    }

    /**
     * Reset or change the brush that handles painting the text of the menu.
     * @param textPaint The new brush that will be used to paint the text of the menu
     */
    public void setTextPaint(Paint textPaint) {
        this.mTextPaint = textPaint;
    }


    /**
     * Get the brush that handles painting the highlighting of the menu. This is set in the
     * constructor but may be changed using setHighlightPaint.
     * @return The brush that can be used to paint the hightlighting the menu
     */
    public Paint getHighlightPaint() {
        return mHighlightPaint;
    }

    /**
     * Reset or change the brush that handles painting the highlighting of the menu.
     * @param highlightPaint The new brush that will be used to paint the highlighting of the menu
     */
    public void setHighlightPaint(Paint highlightPaint) {
        this.mHighlightPaint = highlightPaint;
    }

    /* ********************************************************************************************** *
     *                              Listener region
     * ********************************************************************************************** */
    /**
     * Register the trial listener with the menu. This will be notified when the trial is complete
     * @param listener the listener to register with the view
     */
    public void setTrialListener(TrialListener listener) {
        mTrialListener = listener;
    }

    /**
     * Get the trial listener that is registered with this menu
     * @return the listener to register with the menu
     */
    public TrialListener getTrialListener() {
        return mTrialListener;
    }
}
