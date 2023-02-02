package cse340.menus;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ExperimentActivity extends AbstractMainActivity {

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParticipantNum = 0;

        // programmatically asks for permissions to write to file storage
        // this is for saving the CSV file to disk
        ensurePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ensurePermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        startExperimentSession();
    }

    /**
     * Shows the menu given a trial.
     *
     * @param trial Current trial containing menu information.
     */
    @Override
    protected void showMenuForTrial(ExperimentTrial trial) {
        // Creates Menu based on trial (need to check what menu the current trial requires).
        // Sets the layout parameters for the menu and make it visible on screen
        super.showMenuForTrial(trial);

        // Indicate menu type in the task.
        final TextView instructionTextView = findViewById(R.id.instructionTextView);
        instructionTextView.setText(getString(R.string.trial_message, trial.getMenu(), trial.getItem()));

        mMenuView.setTrialListener(new TrialListener() {
            @Override
            public void onTrialCompleted(ExperimentTrial trial) {
                trialCompleted();
            }
        });
    }

    // Ends current session and advances to next session if available
    private void trialCompleted() {
        if (mSession != null) {
            mSession.recordResult();
            if (mSession.hasNext()) {
                mSession.next();
                showMenuForTrial(mSession.getCurrentTrial());
            } else {
                TextView instructionTextView = findViewById(
                        R.id.instructionTextView
                );
                instructionTextView.setText(R.string.session_completed);
                mMenuView.announce(getString(R.string.session_completed));
                mSession = null;
                mMainLayout.removeView(mMenuView);
            }
        } else {
            Toast.makeText(
                    getBaseContext(),
                    R.string.session_completed,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
