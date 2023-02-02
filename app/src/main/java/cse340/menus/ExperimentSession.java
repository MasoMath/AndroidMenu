package cse340.menus;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cse340.menus.enums.MenuType;
import cse340.menus.enums.TaskType;

public class ExperimentSession implements Iterator {
    /** Tag for using with Log.i */
    public static final String TAG = "Menus";

    /** How many times should each trial repeat? */
    private final int NUM_REPEATS = 3;

    /** Maximum number of menu items to go through in each condition. */
    private final int ITEM_MAX = 4;

    /** This is the file where experiment results will be recorded */
    private File mExperimentResultCSV;

    /** This is the current participant number **/
    private int mParticipantNum;

    /** This is the set of tasks for this session **/
    private Map<TaskType, List<String>> mTasks;
    private Iterator<ExperimentTrial> mTrials;
    private ExperimentTrial mCurrentTrial;

    /**
     * Constructor generates a list of trials based on the available types of menus
     * (normal and pie) and tasks (linear, relative and unclassed) and the number of
     * times each test should be repeated
     *
     * @param assets Gives this class access to the csv files needed to set things up.
     * @param participantNum The participant number for the current participant
     */
    public ExperimentSession(AssetManager assets, int participantNum) {
        setParticipantNum(participantNum);

        // Create CSV file to log test results.
        // Download the result CSV by "adb pull /storage/emulated/0/CSE340_PieMenu/TestResult.csv"
        // or use Android File Transfer (may not work with emulator)
        createCSV();

        try {
            Log.i(TAG,"Loading tasks");
            mTasks = loadTasks("menuContents.csv", assets);
            Log.i(TAG, "Loaded tasks");

            // Create experiment trials
            List<ExperimentTrial> allTrials = createTrials(mTasks);
            Log.i(TAG, "Created Trials");

            // Store them in the trials variable
            mTrials = allTrials.iterator();
            Log.i(TAG, "Created Iterator");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load CSV", e);
        }

    }

    //region Getters and Setters
    public int getParticipantNum() {
        return mParticipantNum;
    }

    public void setParticipantNum(int mParticipantNum) {
        this.mParticipantNum = mParticipantNum;
    }

    public ExperimentTrial getCurrentTrial() {
        return mCurrentTrial;
    }
    //endregion


    //region Experiment Setup
    /**
     * Creates CSV file that experiment results are stored in.
     */
    private void createCSV() {
        // Make sure you grant storage permission for this app in Android settings!
        try {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "CSE340_Menus");
            if (!path.isFile()) path.mkdirs();
            mExperimentResultCSV = new File(path, "TestResult.csv");

            if (!mExperimentResultCSV.isFile()) {
                mExperimentResultCSV.createNewFile();
                FileOutputStream fOut = new FileOutputStream(mExperimentResultCSV, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append("participant, trialNum, repeatNum, menu, task, startTime, taskDuration (millis), Start x, Start y, End x, End y, selected option, prompted option, all options");
                myOutWriter.append('\n');
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            }
        } catch (IOException fnfe) {
            Log.e("createCSV", "Couldn't find or create TestResult.csv");
        }
    }

    /***
     * This deletes all the data collected so far. Be careful with it!
     */
    public void deleteCSV() {
        if (mExperimentResultCSV != null && mExperimentResultCSV.isFile()) {
            mExperimentResultCSV.delete();
        }
    }

    /**
     * Load item lists for each task from the CSV file
     *
     * @param filename The name of the csv file containing the list of item lists
     * @param assets Gives us a way to access that file.
     * @return Mapping from task type to a list of menu items.
     */
    private Map<TaskType, List<String>> loadTasks(String filename, AssetManager assets) {
        Map<TaskType, List<String>> tasks = new HashMap<>();

        // scan through the CSV file one line at a time
        try {
            Scanner scan = new Scanner(new InputStreamReader(assets.open(filename)));
            scan.useDelimiter("[,\n]");

            while (scan.hasNextLine()) {
                String taskType = scan.next();
                String optionList = scan.next();
                List<String> items = Arrays.asList(optionList.split("/"));
                tasks.put(TaskType.valueOf(taskType), items);
            }
        } catch (IOException e) {
            throw new IllegalStateException(filename + " not found in assets");
        } catch (InputMismatchException e) {
            throw new IllegalStateException(filename + "menuContents.csv is malformed");
        }

        return tasks;
    }

    /***
     * Creates all the experimental trials and returns them as a list.
     * @param tasks the list of task conditions to support (with menu items for each task)
     * @return a list of all of the trials for this experiment
     */
    private List<ExperimentTrial> createTrials(Map<TaskType, List<String>> tasks) {
        // this is the number of different trials the user will need to go through.
        List<ExperimentTrial> trials = new ArrayList<>();
        int trialNum = 0;


        // create a shuffled list of menus and tasks (randomize order)
        List<MenuType> shuffledMenus = Arrays.asList(MenuType.values());
        List<TaskType> shuffledTasks = Arrays.asList(TaskType.values());
        Collections.shuffle(shuffledMenus);

        // iterate through all the conditions (there are six: Two MenuTypes x Three TaskTypes)
        for (MenuType menu : shuffledMenus) {
            Collections.shuffle(shuffledTasks);

            for (TaskType task : shuffledTasks) {
                // The goal of this list is to instantiate all the trials for this condition
                // (menu type + task type)

                // First we need find the list of menuitems for this condition
                List<String> items = tasks.get(task);

                // Next we want to randomize the order of the menu items
                // To do this, first we clone the item list so we don't shuffle what's displayed
                List<String> shuffledItems = new ArrayList<>(items);

                // Next we randomize the order and prepare to iterate through the menu items
                Collections.shuffle(shuffledItems);

                // Next we add NUM_REPEATS trials for each item ITEM_MAX menu items
                for (int i = 0; i < Math.min(ITEM_MAX, items.size()); i++) {
                    for (int j = 0; j < NUM_REPEATS; j++) {
                        //create a trial for it
                        trials.add(new ExperimentTrial(menu, task, shuffledItems.get(i), items,
                                j, trialNum, getParticipantNum()));
                        trialNum++;
                    }
                }
            }
        }

        return trials;
    }
    //endregion


    //region Iterating through Trials and Recording Results
    /**
     * Records the result of the current trial to a CSV file setup by createCSV.
     */
    public void recordResult() {
        // Convert trial to a line, and append the line to CSV file.

        try {
            FileOutputStream fOut = new FileOutputStream(mExperimentResultCSV, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(getCurrentTrial().toString());
            myOutWriter.append('\n');
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed", e);
        }
    }

    /***
     * @return boolean true if any trials remaining, false otherwise.
     */
    public boolean hasNext() {
        return mTrials.hasNext();
    }

    /***
     * Implements Iterator functionality, allows this to be used in enhanced for loops, etc.
     *
     * @return Next trial and updates a variable so we have access to it
     */
    public ExperimentTrial next() {
        mCurrentTrial = mTrials.next();
        return mCurrentTrial;
    }

    /***
     * Creates a summary of the current experiment. Useful for debugging.
     *
     * @return A summary ready to print
     */
    public String toString() {
        String result;
        if (mCurrentTrial == null) {
            result = "Currently not in trial. ";
        } else {
            result = "Currently in trial " + mCurrentTrial.getTrialNum();
        }
        result += " and numRepeats set to: " + NUM_REPEATS;
        result += " and max menu items set to: " + ITEM_MAX;
        result += " and there are " + mTasks.size() + " tasks";
        result += " and the participant number is " + getParticipantNum();

        return result;
    }
    //endregion
}
