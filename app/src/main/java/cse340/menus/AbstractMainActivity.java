package cse340.menus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import cse340.menus.enums.MenuType;
import cse340.menus.views.MenuExperimentView;
import cse340.menus.views.CustomMenuView;
import cse340.menus.views.NormalMenuView;
import cse340.menus.views.PieMenuView;


public abstract class AbstractMainActivity extends AppCompatActivity {

    /** The layout for the entire application */
    protected FrameLayout mMainLayout;

    /**
     * The view that will show the menu for the current trial. Because it is a generic
     * MenuExperimentView, this field can hold PieMenuViews, NormalMenuViews,
     * or CustomMenuViews. **/
    protected MenuExperimentView mMenuView;

    /** The data structure holding the current session (for the current user) **/
    protected ExperimentSession mSession;

    /** The current session participant */
    protected int mParticipantNum;

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mMainLayout = findViewById(R.id.mainLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMainLayout.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // convert event to child coordinates
                mMenuView.setX(event.getX());
                mMenuView.setY(event.getY());
            }
            event.offsetLocation(-mMenuView.getX(), -mMenuView.getY());
            return mMenuView.onTouchEvent(event);
        });
    }


    /**
     * Shows the menu given a trial.
     *
     * @param trial Current trial containing menu information.
     */
    protected void showMenuForTrial(ExperimentTrial trial) {
        mMainLayout.removeView(mMenuView);

        // Create Menu based on trial (need to check what menu the current trial requires).
        MenuType cur = trial.getMenu();

        // Create Menu based on ExperimentSession of current task.
        if (cur == MenuType.NORMAL) {
            mMenuView = new NormalMenuView(this, trial);
        } else if (cur == MenuType.PIE) {
            mMenuView = new PieMenuView(this, trial);
        } else if (cur == MenuType.CUSTOM) {
            mMenuView = new CustomMenuView(this, trial);
        } else {
            throw new IllegalStateException("trial menu type invalid");
        }

        mMainLayout.addView(mMenuView);
        mMenuView.setVisibility(View.INVISIBLE);
    }


    /**
     * This uses inflate to create a menu with options for the session.
     *
     * @param menu The menu resource to inflate for this app
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * This is the callback for the option menu (three dots in upper right).
     *
     * @param item Selected item.
     * @return true if selection successful.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.action_clear_result_CSV) {
            new AlertDialog.Builder(this)
                    .setTitle("Erase CSV?")
                    .setMessage("Do you want to erase your data?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes", (dialog, which) -> resetAndClearCSV())
                    .setNegativeButton("No", null)
                    .show();
            return true;
        } else if (item.getItemId() ==  R.id.switch_to_test) {
            new AlertDialog.Builder(this)
                    .setTitle("Switch to Test and Erase CSV?")
                    .setMessage("Do you want to erase your data?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes", (dialog, which) -> switchToTestMode())
                    .setNegativeButton("No", null)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.switch_to_experiment) {
            switchToExperimentMode();
            return true;
        } else if (itemID == R.id.action_next_session) {
            nextSession();
            return true;
        }

        return false;
    }

    /**
     * Switch the app into our experiment mode.
     */
    protected void switchToExperimentMode() {
        Intent intent = new Intent(this, ExperimentActivity.class);
        startActivity(intent);
    }

    /**
     * Switch the app into a mode so the menus can be tested
     */
    protected void switchToTestMode() {
        resetAndClearCSV();
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    /**
     * Erases the CSV file where the experimental data is being stored
     */
    protected void resetAndClearCSV() {
        mParticipantNum = -1;

        if (mSession != null) {
            mSession.deleteCSV();
            mMainLayout.removeView(mMenuView);
        }

        nextSession();
    }


    /**
     * Handles moving from session to session. When sessions switch,
     * a new participant should start the study.
     */
    private void nextSession() {
        mParticipantNum += 1;

        // when user selects "Next Session" in action bar menu:
        // create a floating Toast and create Menu for next session.
        Toast.makeText(this, "Moving to session " + (mParticipantNum + 1),
                Toast.LENGTH_LONG).show();

        mMainLayout.removeView(mMenuView);
        startExperimentSession();

    }

    /**
     * Start the new ExperimentSession with a given participant ID/Number. If there is a new
     * session ready, show the menu for that Trial.
     */
    protected void startExperimentSession() {

        // Pre 20sp - use the participantNum for the ID.
        //mSession = new ExperimentSession(getAssets(), mParticipantNum);

        // 20sp - create a random participant ID
        int participantID = (int)(Math.random() * 1000);
        mSession = new ExperimentSession(getAssets(), participantID);

        if (mSession.hasNext()) {
            showMenuForTrial(mSession.next());
        }
    }

    /**
     * Get permission to write data to the csv file
     * @param permission
     */
    protected void ensurePermission(String permission) {
        int res = ContextCompat.checkSelfPermission(this, permission);
        if (res != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { permission }, PackageManager.PERMISSION_GRANTED);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int res : grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Failed to grant permissions", Toast.LENGTH_LONG).show();
            }
        }
    }

}
