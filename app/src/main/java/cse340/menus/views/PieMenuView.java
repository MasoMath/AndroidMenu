package cse340.menus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.ViewGroup;

import java.util.List;

import cse340.menus.ExperimentTrial;
import cse340.menus.enums.State;

public class PieMenuView extends MenuExperimentView {

    /** Class constant used to determine the size of the pie menu */
    private static final float RADIUS_RATIO = 0.347f;

    /** Actual radius of the pie menu once determined by the display metrics */
    private int RADIUS;

    // Num of items in menu, created and stored for code clarity
    private int menuSize;

    public PieMenuView(Context context, List<String> items) {
        super(context, items);
    }
    // Call super constructor
    public PieMenuView(Context context, ExperimentTrial trial) {
        super(context, trial);
    }

    /**
     * Method that will be called from the constructor to complete any set up for the view.
     * Calls the parent class setup method for initialization common to all menus
     */
    @Override
    protected void setup() {
        mState = State.START;
        // Determine the radius of the pie menu
        RADIUS = (int) (RADIUS_RATIO * Math.min(mDisplayMetrics.widthPixels,
                mDisplayMetrics.heightPixels));

        int strokeWidth = (int) getBorderPaint().getStrokeWidth();
        setLayoutParams(new ViewGroup.LayoutParams(
                2 * (RADIUS + strokeWidth), 2 * (RADIUS + strokeWidth)
        ));

        menuSize = getItems().size();
    }

    /**
     * Start the menu selection by recording the starting point and starting
     * a trial (if in experiment mode).
     * @param point The current position of the mouse
     */
    @Override
    protected void startSelection(PointF point) {
        // Centers the view at the starting point
        setX(getX() - RADIUS);
        setY(getY() - RADIUS);
        // let the parent handle other standard stuff
        super.startSelection(point);
    }

    /**
     * Calculates the index of the menu item using the current finger position
     * If the finger has moved less than MIN_DIST, return -1.
     *
     * Pie Menus have infinite width, so you should not return -1 if the finger leaves the
     * confines of the menu.
     *
     * Angle for the Pie Menu is 0 degrees at North. It increases in the clockwise direction.
     *
     * @param p the current location of the user's finger relative to the menu's (0,0).
     * @return the index of the menu item under the user's finger or -1 if none.
     */
    @Override
    protected int essentialGeometry(PointF p) {
        if (!invalidSelection(p.x, p.y)) {
            double angle = Math.toDegrees(Math.atan2(p.y - RADIUS, p.x - RADIUS));
            // Shifts angle to correspond with our menu's visual representation
            angle = angle + 90 * (menuSize + 2) / ((float) menuSize);
            if (angle < 0) {
                angle = 360 + angle;
            }
            return (int) Math.floor(menuSize * angle / 360);
        }
        return -1;
    }

    // Returns true if the given x, y coordinates are outside of
    // the circle. False otherwise.
    // Made for code clarity
    private boolean invalidSelection(float x, float y) {
        x = x - RADIUS;
        y = y - RADIUS;
        return
                (x * x + y * y < MIN_DIST * MIN_DIST) ||
                (x * x + y * y > RADIUS * RADIUS)
        ;
    }

    /**
     * This must be menu specific so override it in your menu class for Pie, Normal, & Custom menus
     * In either case, you can assume (0,0) is the place the user clicked when you are drawing.
     *
     * @param canvas Canvas to draw on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int strokeWidth = (int) getBorderPaint().getStrokeWidth();
        canvas.drawCircle(
                RADIUS + strokeWidth, RADIUS + strokeWidth,
                RADIUS, getBorderPaint()
        );
        canvas.drawCircle(
                RADIUS + strokeWidth, RADIUS + strokeWidth,
                RADIUS - 2 * TEXT_SIZE, getBorderPaint()
        );
        int angle = 360 / menuSize;
        int[] edges = {
                TEXT_SIZE + strokeWidth, TEXT_SIZE + strokeWidth,
                2 * RADIUS - TEXT_SIZE + strokeWidth,
                2 * RADIUS - TEXT_SIZE + strokeWidth
        };
        for (int i = 0; i < menuSize; i++) {
            drawMenuText(canvas, getItems().get(i), i, edges, angle);
        }
        // Draws highlighted menu option
        if (getCurrentIndex() != -1) {
            canvas.drawArc(
                    edges[0], edges[1], edges[2], edges[3],
                    getCurrentIndex() * angle -
                            90 * (menuSize + 2) / ((float) menuSize),
                    angle,true, getHighlightPaint()
            );
        }
    }

    // Draws the passed text on the circle specified with edges
    // for the given menu index it has occupying an angle slice
    private void drawMenuText(Canvas canvas, String text,
                              int index, int[] edges, int angle) {
        Path path = new Path();
        path.addArc(
                edges[0], edges[1], edges[2], edges[3],
                index * angle - 90, angle
        );
        canvas.drawTextOnPath(text, path, 0, 0, getTextPaint());
    }

}
