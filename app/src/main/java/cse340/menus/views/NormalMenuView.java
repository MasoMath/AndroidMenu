package cse340.menus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.ViewGroup;

import java.util.List;

import cse340.menus.ExperimentTrial;
import cse340.menus.enums.State;

public class NormalMenuView extends MenuExperimentView {

    /** Class constant used to determine the size of the normal menu */
    private static final float CELL_HEIGHT_RATIO = 0.104f;
    private static final float CELL_WIDTH_RATIO = 0.277f;
    private static final float TEXT_OFFSET_RATIO = 0.055f;

    /**
     * The height of each menu cell, in pixels. This is set to (CELL_HEIGHT_RATIO) * the device's
     * smaller dimension.
     */
    private float CELL_HEIGHT;

    /**
     * The width of each menu cell, in pixels. This is set to (CELL_WIDTH_RATIO) * the device's
     * smaller dimension.
     */
    private float CELL_WIDTH;

    // Num of items in menu, created and stored for code clarity
    private int menuSize;

    /**
     * When adding text to your menu cells, TEXT_OFFSET should be added to both the X and Y
     * coordinates of the menu cell. This will ensure that text is "contained" by the menu.
     * For experimentation, try leaving this property off when drawing your menus.
     */
    private float TEXT_OFFSET;

    // Constructors
    public NormalMenuView(Context context, List<String> items) {
        super(context, items);
    }

    public NormalMenuView(Context context, ExperimentTrial trial) {
        super(context, trial);
    }

    /**
     * Method that will be called from the constructor to complete any set up for the view.
     * Calls the parent class setup method for initialization common to all menus
     */
    @Override
    protected void setup() {
        mState = State.START;

        // Determine the dimensions of the normal menu
        CELL_HEIGHT = CELL_HEIGHT_RATIO * Math.min(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
        CELL_WIDTH = CELL_WIDTH_RATIO * Math.min(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
        TEXT_OFFSET = TEXT_OFFSET_RATIO * Math.min(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);

        menuSize = getItems().size();
        float strokeAdjust = getBorderPaint().getStrokeWidth();

        setLayoutParams(new ViewGroup.LayoutParams(
                (int) (CELL_WIDTH + strokeAdjust),
                (int) (CELL_HEIGHT * menuSize + strokeAdjust)
        ));

    }

    /**
     * Calculates the index of the menu item using the current finger position
     * This is specific to your menu's geometry, so override it in your Pie and Normal menu classes
     * If the finger has moved less than MIN_DIST, or is outside the bounds of the menu,
     * return -1.
     *
     * @param p the current location of the user's finger relative to the menu's (0,0).
     * @return the index of the menu item under the user's finger or -1 if none.
     */
    @Override
    protected int essentialGeometry(PointF p) {
        if (!invalidSelection(p.x, p.y)) {
            // Determines which cell we are in
            return (int) Math.floor(p.y / CELL_HEIGHT);
        }
        return -1;
    }

    // Returns true if the given x, y coordinates are outside of
    // the bounding box of the view. False otherwise.
    // Made for code clarity
    private boolean invalidSelection(float x, float y) {
        return
                (x * x + y * y < MIN_DIST * MIN_DIST) ||
                ((x < 0) || (x > CELL_WIDTH)) ||
                ((y < 0) || (y > CELL_HEIGHT * menuSize))
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
        Paint textBrush = getTextPaint();
        Paint borderBrush = getBorderPaint();
        int index = 0;
        for (String item : getItems()) {
            menuBox(canvas, index, borderBrush);
            canvas.drawText(
                    item, TEXT_OFFSET,
                    CELL_HEIGHT * index + TEXT_OFFSET, textBrush
            );
            index++;
        }
        // Highlights the selected box
        if (getCurrentIndex() != -1) {
            borderBrush = getHighlightPaint();
            menuBox(canvas, getCurrentIndex(), borderBrush);
        }

    }


    // Creates a menu box at the specified cell index with pass Paint object
    private void menuBox(Canvas canvas, int index, Paint brush) {
        float strokeAdjust = brush.getStrokeWidth() / 2;
        Path path = new Path();
        path.moveTo(
                strokeAdjust,
                CELL_HEIGHT * index + strokeAdjust
        );
        path.rLineTo(CELL_WIDTH - strokeAdjust, 0);
        path.rLineTo(0, CELL_HEIGHT - strokeAdjust);
        path.rLineTo(-CELL_WIDTH + strokeAdjust, 0);
        path.rLineTo(0, - CELL_HEIGHT + strokeAdjust);
        canvas.drawPath(path, brush);
    }
}
