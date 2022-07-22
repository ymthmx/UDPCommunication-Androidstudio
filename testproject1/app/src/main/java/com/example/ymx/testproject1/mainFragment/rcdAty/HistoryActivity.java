package com.example.ymx.testproject1.mainFragment.rcdAty;
import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.appcompat.app.ActionBar;

import com.example.ymx.testproject1.BasicActivity;
import com.example.ymx.testproject1.MyLog;
import com.example.ymx.testproject1.R;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class HistoryActivity extends BasicActivity {

    private static List<IpMsg> MsgList;
    private static ListView listView;
    private static ArrayAdapter<String> adapter;
    private static Vector<String> vecString;
    private static RadioGroup ipRadioGroup;
    private static String ipAddress;
    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainfrag_rcdaty_his);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ipRadioGroup = (RadioGroup) findViewById(R.id.his_ip_group);
        ((RadioButton)findViewById(R.id.radio_for_size)).setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.uav_info_list_view);
        MsgList = new ArrayList<>();
        vecString = new Vector<>();
        adapter = new ArrayAdapter<String>(HistoryActivity.this,
                android.R.layout.simple_list_item_1, vecString);
        listView.setAdapter(adapter);

        //初始化RadioGroup
        initRadioG();
        //返回键
        ((Button)findViewById(R.id.his_call_back_btn)).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
        //删除当前ip数据键
        ((Button)findViewById(R.id.delete_current_data_btn)).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int id = ipRadioGroup.getCheckedRadioButtonId();
                        if (id == -1) {
                            MyLog.log(TAG,"尚未选择Ip");
                            Toast.makeText(HistoryActivity.this,"请选择Ip",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            final String ipAddr = ipAddress;
                            if (!ipAddr.equals("")){
                                DataSupport.deleteAll
                                        (IpMsg.class,"iPAddress = ?", ipAddr);
                                vecString.clear();
                                adapter.notifyDataSetChanged();
                                ipAddress="";
                            }else {
                                Toast.makeText(HistoryActivity.this,"已删除",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        //删除所有ip数据键
        ((Button)findViewById(R.id.delete_all_data_btn)).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DataSupport.deleteAll(IpMsg.class);
                        vecString.clear();
                        adapter.notifyDataSetChanged();
                        ipRadioGroup.clearAnimation();
                        ipAddress = "";
                        Toast.makeText(HistoryActivity.this,"已删除",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initRadioG(){
        MsgList = DataSupport.select("ipAddress")
                .order("ipAddress")
                .find(IpMsg.class);
        if (MsgList.isEmpty()){
            MyLog.log(TAG,"The list is empty when initialize the RadioGroup");
        }else {
            String ipInVec = MsgList.get(0).getIpAddress();
            String ipCandidate;
            Vector<String>ipVec = new Vector<>();
            ipVec.add(ipInVec);
            for (IpMsg ipMsg:MsgList){
                MyLog.log(TAG,ipMsg.getIpAddress());
                ipCandidate = ipMsg.getIpAddress();
                if (!ipInVec.equals(ipCandidate)){
                    ipVec.add(ipCandidate);
                    ipInVec = ipCandidate;
                }
            }

            for (final String ipAddr : ipVec){
                final RadioButton radioButton = new RadioButton(this);
                final String ipAddress = ipAddr;
                radioButton.setText(ipAddress);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HistoryActivity.ipAddress = ipAddress;
                        getData(ipAddress);
                    }
                });
                ipRadioGroup.addView(radioButton);
            }
        }
    }

    private void getData(String ipAddress) {
        MyLog.log(TAG, "in getData,ipAddress:");
        if (!ipAddress.isEmpty()) {
            MyLog.log(TAG, "getData,ipAddress:" + ipAddress);
            //查询数据
            MsgList.clear();
            MsgList = DataSupport.where("iPAddress = ?",ipAddress)
                    .select("time","content","type")
                    .order("time")
                    .find(IpMsg.class);
            //更新ListView
            vecString.clear();
            if (!MsgList.isEmpty()){
                for (IpMsg msg : MsgList) {
                    String ctn;
                    if (msg.getType()==IpMsg.TYPE_RECEIVE)
                        ctn = msg.getTime()+",Recv:"+""+msg.getContent();
                    else
                        ctn = msg.getTime()+",Send:"+""+msg.getContent();
                    vecString.add(ctn);
                }
            }else {
                MyLog.log(TAG, "数据库为空");
                Toast.makeText(HistoryActivity.this,
                        "数据库为空", Toast.LENGTH_SHORT).show();
            }
            adapter.notifyDataSetChanged();
        } else {
            MyLog.log(TAG, "ip地址为空");
            Toast.makeText(HistoryActivity.this,
                    "ip地址为空", Toast.LENGTH_SHORT).show();
        }
        MyLog.log(TAG, "out getData");
    }
}