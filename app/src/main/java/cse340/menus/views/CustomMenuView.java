package cse340.menus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cse340.menus.ExperimentTrial;
import cse340.menus.enums.State;

public class CustomMenuView extends MenuExperimentView {

    /** Class constant used to determine the size of the custom menu */
    private static final float CELL_WIDTH_RATIO = 0.175f;
    private static final float CELL_HEIGHT_RATIO = 0.1f;
    private static final float TEXT_OFFSET_RATIO = 0.055f;

    /** The length of each cell, the offset of the text from the cell
     * and the numbers of items in the current menu */
    private float CELL_HEIGHT, CELL_WIDTH, TEXT_OFFSET;
    // Number of items in menu, stored for code clarity
    private int menuSize;

    // Random object initialized and stored globally for efficiency
    private Random mRandom;
    // Arrays to keep track of where each item is being assigned its index and vertical position
    // with respect to their true indexing in the mItems List from parent class, and "inverse"
    // arrays to go in reverse.
    private int[] randomIndex, randomVertPos, inverseIndex, inverseVertPos;


    // Constructors
    public CustomMenuView(Context context, ExperimentTrial trial) { super(context, trial); }
    public CustomMenuView(Context context, List<String> items) { super(context, items); }

    /**
     * Method that will be called from the constructor to complete any set up for the view.
     * Calls the parent class setup method for initialization common to all menus
     */
    @Override
    protected void setup() {
        mState = State.START;

        CELL_HEIGHT = CELL_HEIGHT_RATIO * Math.min(
                mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
        CELL_WIDTH = CELL_WIDTH_RATIO * Math.min(
                mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
        TEXT_OFFSET = CELL_HEIGHT - TEXT_OFFSET_RATIO * Math.min(
                mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);

        menuSize = getItems().size();
        float strokeAdjust = getBorderPaint().getStrokeWidth();

        setLayoutParams(new ViewGroup.LayoutParams(
                (int) (CELL_HEIGHT * (menuSize - 1) + CELL_WIDTH + strokeAdjust),
                (int) (CELL_HEIGHT * menuSize + strokeAdjust)
        ));

        mRandom = new Random();
        randomIndex = new int[menuSize];
        inverseIndex = new int[menuSize];
        randomVertPos = new int[menuSize];
        inverseVertPos = new int[menuSize];
    }

    /**
     * Calculates the essential geometry for the custom menu.
     *
     * @param p the current location of the user's finger relative to the menu's (0,0).
     * @return the index - half the number of items in the menu modulo the size of the
     * menu of the menu item under the user's finger or -1 if none.
     */
    @Override
    protected int essentialGeometry(PointF p) {
        if (!invalidSelection(p.x, p.y)) {
            // Note that the index and vertPos arrays and their "inverses"
            // correspond with a permutation group, so if you treat them
            // as such, any call to those arrays is the code asking
            // either "where should this index go" or "where did this
            // index come from"
            int rawVertPos = (int) Math.floor(p.y / CELL_HEIGHT);
            int rawIndex = inverseVertPos[rawVertPos];
            int actualIndex = inverseIndex[rawIndex];
            if ((p.x > CELL_HEIGHT * rawIndex) &&
                    (p.x < CELL_HEIGHT * rawIndex + CELL_WIDTH) ) {
                // The following return calculation shifts the highlighted
                // menu option to what it is half the menuSize away from
                // the current selection modulo menuSize in the true sense
                return (((actualIndex - (menuSize / 2)) % menuSize) +
                        menuSize) % menuSize;
            }
        }
        return -1;
    }

    // Returns true if the given x,y coordinate is not within the bounding box
    // of this view or if the distance travelled by user is not more than the
    // minimum distance required for the app to register a selection is being
    // made. Returns false otherwise.
    private boolean invalidSelection(float x, float y) {
        float strokeAdjust = getBorderPaint().getStrokeWidth();
        return
                (x * x + y * y < MIN_DIST * MIN_DIST) ||
                ((x < 0) || (x > CELL_HEIGHT * (menuSize - 1) +
                            CELL_WIDTH + strokeAdjust)) ||
                ((y < 0) || (y > CELL_HEIGHT * menuSize))
        ;
    }


    /**
     * Start the menu selection by recording the starting point and starting
     * a trial (if in experiment mode).
     * @param point The current position of the mouse
     */
    @Override
    protected void startSelection(PointF point) {
        float adjustment = (CELL_HEIGHT * menuSize) / 2;
        setX(getX() - adjustment);
        setY(getY() - adjustment);
        // Assigns the random vertical positions of the menu items
        // Note that this method is only ever called once per a menu
        // being displayed, so this randomization is not changed until
        // the menu exits the state machine and re-enters it.
        randomVertPos = repeatPicker(menuSize, mRandom);
        randomIndex = repeatPicker(menuSize, mRandom);
        for (int i = 0; i < menuSize; i++) {
            inverseIndex[randomIndex[i]] = i;
            inverseVertPos[randomVertPos[i]] = i;
        }
        super.startSelection(point);
    }

    /**
     * This must be menu specific so override it in your menu class for Pie, Normal, & Custom menus
     * In either case, you can assume (0,0) is the place the user clicked when you are drawing.
     *
     * @param canvas Canvas to draw on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int trueIndex = 0;
        for (String item : getItems()) {
            int index = randomIndex[trueIndex];
            int vertPos = randomVertPos[index];
            menuBox(canvas, index, vertPos, getBorderPaint());
            canvas.drawText(
                    item, CELL_HEIGHT * index + TEXT_OFFSET / 4,
                    CELL_HEIGHT * (vertPos + 1) - TEXT_OFFSET, getTextPaint()
            );
            trueIndex++;
        }
        // Draws highlighted menu option, note that unless the menu is of size 1,
        // then this never draws the menu option being hovered over, but whatever
        // the option ((size of menu) / 2 ) modulo (size of menu) in mItems
        if (getCurrentIndex() != -1) {
            menuBox(
                    canvas, randomIndex[getCurrentIndex()],
                    randomVertPos[randomIndex[getCurrentIndex()]],
                    getHighlightPaint()
            );
        }
    }

    // Creates a menu box at the specified x - index and y - vertPos positions
    private void menuBox(Canvas canvas, int index, int vertPos, Paint brush) {
        float strokeAdjust = brush.getStrokeWidth() / 2;
        Path path = new Path();
        path.moveTo(
                CELL_HEIGHT * index + strokeAdjust,
                CELL_HEIGHT * vertPos + strokeAdjust
        );
        path.rLineTo(CELL_WIDTH - strokeAdjust, 0);
        path.rLineTo(0, CELL_HEIGHT - strokeAdjust);
        path.rLineTo(-CELL_WIDTH + strokeAdjust, 0);
        path.rLineTo(0, - CELL_HEIGHT + strokeAdjust);
        canvas.drawPath(path, brush);
    }

    // This method returns an int array of length n with each entry
    // randomly assigned a unique number (i.e. there are no repeats)
    // between 0 inclusive and n exclusive.
    private static int[] repeatPicker(int n, Random rng) {
        int[] repeatArray = new int[n];
        int[] probArray = new int[n];
        for (int i = 0; i < n; i++) {
            probArray[i] = i;
        }
        for (int i = 0; i < n; i++ ) {
            int loser = rng.nextInt(n - i);
            repeatArray[probArray[loser]] = i;
            probArray[loser] = probArray[n - 1 - i];
        }
        return repeatArray;
    }
}
