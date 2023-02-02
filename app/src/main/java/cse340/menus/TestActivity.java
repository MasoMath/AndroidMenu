package cse340.menus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cse340.menus.views.*;


public class TestActivity extends AbstractMainActivity {

    /** List of items in the menu that will be displayed in the menu for this activity */
    protected List<String> mMenuItems;

    /** The normal menu that will be tested */
    private MenuExperimentView mNormalMenu;

    /** The pie menu that will be tested */
    private MenuExperimentView mPieMenu;

    /** The student's custom menu that will be tested */
    private MenuExperimentView mCustomMenu;

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> testItems = new ArrayList<>();
        Collections.addAll(testItems, "1", "2", "4", "8", "16");
        mMenuItems = testItems;

        mNormalMenu = new NormalMenuView(this, mMenuItems);
        mNormalMenu.setVisibility(View.INVISIBLE);
        mMainLayout.addView(mNormalMenu);
        mPieMenu = new PieMenuView(this, mMenuItems);
        mPieMenu.setVisibility(View.INVISIBLE);
        mMainLayout.addView(mPieMenu);
        mCustomMenu = new CustomMenuView(this, mMenuItems);
        mCustomMenu.setVisibility(View.INVISIBLE);
        mMainLayout.addView(mCustomMenu);

        mMenuView = mNormalMenu;
    }

    /**
     * This uses inflate to create a menu with options for the session.
     *
     * @param menu The menu resource to inflate for this app
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    /**
     * Do an action based on the item that was selected from the menu
     *
     * @param item The item selected from the m enu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        mMenuView.setCurrentIndex(-1);
        mMenuView.setVisibility(View.INVISIBLE);

        // Inspect the item to determine the menu type
        switch (item.getItemId()) {
            case R.id.action_open_normal: mMenuView = mNormalMenu; break;
            case R.id.action_open_pie: mMenuView = mPieMenu;  break;
            case R.id.action_open_custom: mMenuView = mCustomMenu;  break;
            default: return super.onOptionsItemSelected(item);
        }

        return true;
    }

}
