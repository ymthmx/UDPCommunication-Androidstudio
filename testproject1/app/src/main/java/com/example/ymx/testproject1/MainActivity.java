package com.example.ymx.testproject1;

import androidx.appcompat.app.ActionBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.ymx.testproject1.mainFragment.FriendFrag;
import com.example.ymx.testproject1.mainFragment.FunctionFrag;
import com.example.ymx.testproject1.mainFragment.RecordFrag;
import com.example.ymx.testproject1.mainFragment.rcdAty.HistoryActivity;

public class MainActivity extends BasicActivity {
    final String TAG = "MainActivity";
    int currentTabIndex;
    Fragment[] fragments;
    ImageView[] imageButtons;
    TextView[] unreadNumTexts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        initTabView();
        //设置软键盘不自动弹出
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void initTabView() {
        RecordFrag rcFrag = new RecordFrag();
        FriendFrag frdFrag = new FriendFrag();
        FunctionFrag funcFrag = new FunctionFrag();
        HistoryActivity historyActivity=new HistoryActivity();
        //CallFrag callFrag = new CallFrag();
       //fragments = new Fragment[] {rcFrag,frdFrag,funcFrag,callFrag};
        fragments = new Fragment[] {rcFrag,frdFrag,funcFrag};
        //三个title_bottom小红点
        unreadNumTexts = new TextView[3];
        unreadNumTexts[0] = (TextView) findViewById(R.id.title_rc_unread_num);
        unreadNumTexts[1] = (TextView) findViewById(R.id.title_frd_unread_num);
        unreadNumTexts[2] = (TextView) findViewById(R.id.title_func_unread_num);
        //获取title_bottom四个img
        imageButtons = new ImageView[4];
        imageButtons[0] = (ImageView) findViewById(R.id.title_rc_img);
        imageButtons[1] = (ImageView) findViewById(R.id.title_frd_img);
        imageButtons[2] = (ImageView) findViewById(R.id.title_func_img);
        //imageButtons[3] = (ImageView) findViewById(R.id.title_call_img);
        imageButtons[0].setSelected(true);
        // 添加显示fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frameLayout, rcFrag)
                .add(R.id.main_frameLayout, frdFrag)
                .add(R.id.main_frameLayout, funcFrag)
                //.add(R.id.main_frameLayout, callFrag)
                .hide(frdFrag).hide(funcFrag)
                //.hide(callFrag)
                .show(rcFrag).commit();
        currentTabIndex = 0;
    }

    public void onTabChange(View v){
        int index = Integer.MAX_VALUE;
        switch (v.getId()){
            case R.id.title_rc_btn:
                if(currentTabIndex != 0){
                    index = 0;
                }
                break;
            case R.id.title_frd_btn:
                if(currentTabIndex != 1){
                    index = 1;
                }
                break;
            case R.id.title_func_btn:
                if(currentTabIndex != 2){
                    index = 2;
                }
                break;
//            case R.id.title_call_btn:
//                if(currentTabIndex != 3){
//                    index = 3;
//                }
//                break;
        }
        if (index != Integer.MAX_VALUE){
            if(!fragments[index].isAdded()){
                getSupportFragmentManager().beginTransaction().add
                        (R.id.main_frameLayout, fragments[currentTabIndex]);
            }
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            if(index != currentTabIndex){
                trx.hide(fragments[currentTabIndex]);
                trx.show(fragments[index]).commit();
                imageButtons[currentTabIndex].setSelected(false);
                imageButtons[index].setSelected(true);
            }
            currentTabIndex = index;
        }
    }
}