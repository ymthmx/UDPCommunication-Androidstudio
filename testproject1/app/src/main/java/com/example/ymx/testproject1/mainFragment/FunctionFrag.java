package com.example.ymx.testproject1.mainFragment;


import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ymx.testproject1.MyLog;
import com.example.ymx.testproject1.R;
import com.example.ymx.testproject1.mainFragment.rcdAty.HistoryActivity;
import com.example.ymx.testproject1.mainFragment.rcdAty.IpMsg;

import java.util.List;
import java.util.Vector;


public class FunctionFrag extends Fragment {

    final String TAG = "FunctionFrag";
    TextView testText;
    Button history_but;


        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){


        View view = inflater.inflate(R.layout.main_fragment_function,
                container,false);


        MyLog.log(TAG,"in History onCreateView");
        testText = view.findViewById(R.id.func_test_text);
        history_but=view.findViewById(R.id.history_but);
        history_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),HistoryActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState){
//        super.onActivityCreated(savedInstanceState);
//
//        testText.setText("in funcFrag");
//    }
}