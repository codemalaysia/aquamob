package com.polluxlab.aquamob;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.polluxlab.aquamob.helpers.DBHelper;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.models.Form;
import com.polluxlab.aquamob.models.Header;
import com.polluxlab.aquamob.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Author: ARGHA K ROY
 * Date: 3/12/2016.
 */
public class FormFieldsPagerActivity extends AppCompatActivity {

    Form mForm;
    int currentPosition = 0;
    ArrayList<FormFieldsFragment> formFieldsFragmentList;
    private JSONArray commonFieldsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_fields_pager_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context mContext = this;
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.formPagerTabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.formViewPager);
        DBHelper mDBHelper = new DBHelper(mContext);
        PrefHelper mPrefHelper = new PrefHelper(mContext);
        try {
            commonFieldsData=new JSONArray(getIntent().getStringExtra("common-fields-data"));
        } catch (JSONException e) {
            e.printStackTrace();
            Util.printDebug("Common field get exception",e.getMessage());
        }
        formFieldsFragmentList = new ArrayList<>();

        mForm = (Form) getIntent().getSerializableExtra("form");
        try {
            JSONArray commonDataArray = new JSONArray(getIntent().getStringExtra("common-fields-data"));
            Util.printDebug("Common datas", commonDataArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Util.printDebug("Error passing common fields data", e.getMessage());
        }
        getSupportActionBar().setTitle(mForm.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ArrayList<Form> savedData= mDBHelper.getAllSavedForms(mForm.getId(), mPrefHelper.getCurrentUser().getUserName());
        final FormViewPagerAdapter pagerAdapter = new FormViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                formFieldsFragmentList.get(currentPosition).saveFragment(FormDataListActivity.mainHeaderList.get(currentPosition),commonFieldsData);
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class FormViewPagerAdapter extends FragmentStatePagerAdapter {
        // private ArrayList<Form> mFormArrayList;
        public FormViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FormFieldsFragment fragment = FormFieldsFragment.newInstance(mForm, position);
            formFieldsFragmentList.add(fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return FormDataListActivity.mainHeaderList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            ArrayList<Header> headers = FormDataListActivity.mainHeaderList.get(position);
            StringBuilder headerText = new StringBuilder();
            for (Header header : headers) {
                if (headerText.length() != 0) headerText.append("-");
                headerText.append(header.getValue());
            }
            return headerText;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        formFieldsFragmentList.get(currentPosition).saveFragment(FormDataListActivity.mainHeaderList.get(currentPosition),commonFieldsData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pager, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
