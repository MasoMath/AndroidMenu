
package cse340.menus;


import cse340.menus.ExperimentTrial;

public interface TrialListener {
    /** Callback function called when a trial is finished. */
    void onTrialCompleted(ExperimentTrial trial);
}
