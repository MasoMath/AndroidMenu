package cse340.menus;

import android.graphics.PointF;
import android.text.TextUtils;

import java.util.List;

import cse340.menus.enums.MenuType;
import cse340.menus.enums.TaskType;

public class ExperimentTrial {
    /** All the contents of this trial's menu */
    private final List<String> mMenuContents;

    /** The repeat number for this trial */
    private final int mRepeatNum;

    /** The number for this trial */
    private final int mTrialNum;

    /** The menu type for this trial */
    private final MenuType mMenu;

    /** The task type for this trial */
    private final TaskType mTask;

    /** The participant number doing this trial */
    private final int mParticipantNum;

    /** The index of the option the participant should click on */
    private final int mPromptedOptionIndex;

    /** The index of the option the participant does click on */
    private int mSelectedOptionIndex;

    /** The start time for the trial */
    private long mStartTime;

    /** The total time it took for the trial to be completed (for the user to click on a menu item) */
    private long mTaskDurationMillSec;

    /** The starting position of the participant's finger */
    private PointF mStartPoint;

    /** The final position of the participant's finger */
    private PointF mEndPoint;

    /***
     * Creates a new single trial for the experiment with the specified parameters.
     *
     * @param menu The menu condition for this trial
     * @param task The task condition for this trial
     * @param item The specific menuItem the user must select for this trial
     * @param menuContents The contents of the menu for this trial
     * @param trialNum The trial number
     * @param participantNum The participant number
     */
    public ExperimentTrial(MenuType menu, TaskType task, String item, List<String> menuContents,
                           int repeatNum, int trialNum, int participantNum) {
        this.mMenu = menu;
        this.mTask = task;
        this.mMenuContents = menuContents;
        this.mRepeatNum = repeatNum;
        this.mTrialNum = trialNum;
        this.mPromptedOptionIndex = menuContents.indexOf(item);
        this.mParticipantNum = participantNum;
        this.mSelectedOptionIndex = -1;
    }

    /***
     * Record the trial start (including a timestamp and the finger position at start)
     * @param p The finger's position.
     */
    public void startTrial(PointF p) {
        mStartTime = java.lang.System.currentTimeMillis();
        mStartPoint = p;
    }

    /***
     * Record the trial end (including a timestamp, duration, and the final position of the finger)
     * @param p The final finger position
     * @param selectedOption The option that the finger was selecting
     */
    public void endTrial(PointF p, int selectedOption) {
        mTaskDurationMillSec = java.lang.System.currentTimeMillis() - this.getStartTime();
        mEndPoint = p;
        mSelectedOptionIndex = selectedOption;
    }

    //Getters and Setters
    public MenuType getMenu() {
        return mMenu;
    }

    public TaskType getTask() {
        return mTask;
    }

    public List<String> getMenuContents() {
        return mMenuContents;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public int getTrialNum() {
        return mTrialNum;
    }

    public String getItem() {
        return mMenuContents.get(mPromptedOptionIndex);
    }
    //end Getters and Setters

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(mParticipantNum);
        s.append(',');
        s.append(mTrialNum);
        s.append(',');
        s.append(mRepeatNum);
        s.append(',');
        s.append(mMenu);
        s.append(',');
        s.append(mTask);
        s.append(',');
        s.append(mStartTime);
        s.append(',');
        s.append(mTaskDurationMillSec);
        s.append(',');
        s.append(mStartPoint.x);
        s.append(',');
        s.append(mStartPoint.y);
        s.append(',');
        s.append(mEndPoint.x);
        s.append(',');
        s.append(mEndPoint.y);
        s.append(',');
        s.append(mSelectedOptionIndex);
        s.append(',');
        s.append(mPromptedOptionIndex);
        s.append(',');
        s.append(TextUtils.join("/", mMenuContents));
        return s.toString();
    }
}
