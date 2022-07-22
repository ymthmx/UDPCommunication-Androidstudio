package com.example.ymx.testproject1.mainFragment.rcdAty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ymx.testproject1.BasicActivity;
import com.example.ymx.testproject1.MyLog;
import com.example.ymx.testproject1.R;
import com.example.ymx.testproject1.mainFragment.RecordFrag;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BasicActivity {
    public List<IpMsg> msgList;
    private static EditText inputText;
    private static RecyclerView msgRecyclerView;
    private static TextView ipView;
    private static RadioButton chatInLANrBtn;
    public static MsgAdapter adapter;
    public static String ipAddr;
    private static String ipAddrForLAN;
    private static boolean isRadioCkd;
    private static IntentFilter intentFilter;
    private static LocalReceiver localReceiver;
    public String inputCtn;
    private static final String TAG = "ChatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainfrag_rcdaty_chat);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        /***初始化***/
        ipView = (TextView)findViewById(R.id.chat_ip_view);
        inputText = (EditText)findViewById(R.id.input_text);
        chatInLANrBtn = (RadioButton)findViewById(R.id.chat_LAN_radio);
        msgRecyclerView = (RecyclerView)findViewById(R.id.msg_recycler_view);

        isRadioCkd = false;
        msgList = new ArrayList<>();
        initMsgs();      //查询数据库，初始化msgList
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        msgRecyclerView.scrollToPosition(msgList.size()-1); //定位到记录的最后一行
        intentFilter = new IntentFilter();
        localReceiver = new LocalReceiver();
        intentFilter.addAction("com.enzosalvetore.circle.LOCAL_BROADCAST");
        RecordFrag.localBroadcastManager.registerReceiver
                (localReceiver,intentFilter);

        ((Button)findViewById(R.id.chat_call_back_btn)).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });

        chatInLANrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRadioCkd){
                    MyLog.log(TAG,"点击Radio前已选中");
                    chatInLANrBtn.setChecked(false);
                    ipAddr = ipAddrForLAN;  //还原原通信ip
                }else {
                    MyLog.log(TAG,"点击Radio前没有选中");
                    ipAddrForLAN = ipAddr;   //保留当前通信ip
                    chatInLANrBtn.setChecked(true);
                    ipAddr = "255.255.255.255";
                }
                isRadioCkd = !isRadioCkd;
            }
        });

        ((Button)findViewById(R.id.send_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String content = inputText.getText().toString();
                if(!"".equals(content)){
                    final IpMsg msg = new IpMsg(content,IpMsg.TYPE_SENT);
                    msgList.add(msg);
                    //有新消息时刷新RecyclerView的显示
                    adapter.notifyItemInserted(msgList.size()-1);
                    //将RecyclerView定位到最后一行
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
                    inputCtn = inputText.getText().toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendByUDP(ipAddr,inputCtn);
                            RecordFrag.saveData(ipView.getText().toString(),
                                    content,IpMsg.TYPE_SENT);
                        }
                    }).start();
                    inputText.setText("");
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        ipAddr = "";
        RecordFrag.localBroadcastManager.unregisterReceiver(localReceiver);
        super.onDestroy();
    }

    private void getData(String ipAddress) {
        MyLog.log(TAG, "in getData");
        if (!ipAddress.isEmpty()) {
            MyLog.log(TAG, "getData,ipAddress:" + ipAddress);
            msgList.clear();
            msgList = DataSupport.where("iPAddress = ?",ipAddress)
                    .select("time","content","type")
                    .order("time")
                    .find(IpMsg.class);
            if (msgList.isEmpty()){
                MyLog.log(TAG, "数据库为空");
                Toast.makeText(ChatActivity.this,
                        "数据库为空", Toast.LENGTH_SHORT).show();
            }else {
                MyLog.log(TAG,"查询数据库结果：");
                for (IpMsg ipMsg:msgList){
                    MyLog.log(TAG,ipMsg.getTime()+"ip:"+
                            ipMsg.getIpAddress()+":"+ipMsg.getContent());
                }
            }
        } else {
            MyLog.log(TAG, "ip地址为空");
            Toast.makeText(ChatActivity.this,
                    "ip地址为空", Toast.LENGTH_SHORT).show();
        }
        MyLog.log(TAG, "out getData");
    }

    private void initMsgs(){
        Intent intent = getIntent();
        ipAddr = intent.getStringExtra("ipAddress");
        if ("".equals(ipAddr)) onBackPressed();
        ipView.setText(ipAddr);
        getData(ipAddr);   //初始化List
    }

    public static void sendByUDP(String ipAddr,String inputCtn){
        if (!inputCtn.equals("")){
            try{
                DatagramSocket ds = new DatagramSocket();
                byte buf[] = inputCtn.getBytes();
                int size = buf.length;
                MyLog.log(TAG,ipAddr+","+RecordFrag.port);
                MyLog.log(TAG,"Send>>content:"+inputCtn+"size:"+size);
                DatagramPacket dp = new DatagramPacket(buf,size,
                        InetAddress.getByName(ipAddr),RecordFrag.port);
                ds.send(dp);
                MyLog.log(TAG,"已发送");
                ds.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            final String ctn = intent.getStringExtra("recv_msg");
           if (!ctn.equals("")){
                final IpMsg ipMsg = new IpMsg(ctn,IpMsg.TYPE_RECEIVE);
                MyLog.log(TAG,"收到广播信息:"+ipMsg.getContent());
                msgList.add(ipMsg);
                adapter.notifyItemInserted(msgList.size()-1);
                msgRecyclerView.scrollToPosition(msgList.size()-1);
            }
        }
    }
}