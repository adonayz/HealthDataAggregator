package com.fitbit.sampleandroidoauth2;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fitbit.authentication.AuthenticationManager;

import edu.wpi.healthdataaggregator.R;

public class UserDataActivity extends AppCompatActivity{
    private UserDataPagerAdapter userDataPagerAdapter;
    private RelativeLayout relativeLayout;
    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private android.support.v7.app.ActionBar actionBar;

    public static Intent newIntent(Context context) {
        return new Intent(context, UserDataActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        actionBar = getSupportActionBar();

        relativeLayout = (RelativeLayout) findViewById(R.id.userDataProgressRelativeLayout) ;
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        linearLayout = (LinearLayout) findViewById(R.id.userDataLinearLayout);

        relativeLayout.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

        userDataPagerAdapter = new UserDataPagerAdapter(getFragmentManager());
        viewPager.setAdapter(userDataPagerAdapter);

        viewPager.addOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        addTabs();
    }

    private void addTabs() {
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        int numberOfTabs = userDataPagerAdapter.getCount();
        for (int i = 0; i < numberOfTabs; i++) {
            final int index = i;
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(getString(userDataPagerAdapter.getTitleResourceId(i)))
                            .setTabListener(new android.support.v7.app.ActionBar.TabListener() {
                                @Override
                                public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                                    viewPager.setCurrentItem(index);

                                }

                                @Override
                                public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

                                }

                                @Override
                                public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

                                }
                            }));
        }
    }


    public void onLogoutClick(View view) {
        relativeLayout.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
        AuthenticationManager.logout(this);
    }
}
