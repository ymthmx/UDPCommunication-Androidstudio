package com.example.ymx.testproject1.mainFragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ymx.testproject1.MyLog;
import com.example.ymx.testproject1.R;
import com.example.ymx.testproject1.mainFragment.rcdAty.ChatActivity;
import com.example.ymx.testproject1.mainFragment.rcdAty.HistoryActivity;
import com.example.ymx.testproject1.mainFragment.rcdAty.IpMsg;


import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class RecordFrag extends Fragment implements View.OnClickListener{

    //1.布件
    private static RecyclerView ipRecyclerView;
    private static EditText ipEditView;
    //2.变量
    public static final int port = 8086;
    private static String myIp;
    //3.容器
    private List<IpMsg> ipList = new ArrayList<>();
    private IpAdapter adapter;
    //4.其他
    public static LocalBroadcastManager localBroadcastManager;
    public static SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TAG = "RecordFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.main_fragment_record,
                container,false);
        MyLog.log(TAG,"in RecordFrag onCreateView");
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        new Receiver().execute(port);
        initIps();
        ipRecyclerView = (RecyclerView)view.findViewById(R.id.ip_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        ipRecyclerView.setLayoutManager(layoutManager);
        adapter = new IpAdapter(ipList);
        ipRecyclerView.setAdapter(adapter);

        ipEditView = (EditText)view.findViewById(R.id.rc_ip_view);
        view.findViewById(R.id.rc_contact_btn).setOnClickListener(this);
        //view.findViewById(R.id.rc_start_his_activity).setOnClickListener(this);
//        view.findViewById(R.id.rc_start_soft_activity).setOnClickListener(this);
        LitePal.getDatabase();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatActivity.sendByUDP("255.255.255.255","我上线了");
            }
        }).start();
        return view;
    }

    @Override
    public void onDestroy(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatActivity.sendByUDP("255.255.255.255","我下线了");
            }
        }).start();
        super.onDestroy();
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getParentFragmentManager().setFragmentResultListener(myIp, this, new FragmentResultListener() {
//
//            @Override
//            public void onFragmentResult(@NonNull String key, @NonNull Bundle bundle) {
//                // We use a String here, but any type that can be put in a Bundle is supported
//                String result = bundle.getString(myIp);
//                // Do something with the result...
//            }
//        });
//    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
//            case R.id.rc_start_his_activity:
//                MyLog.log(TAG,"click the start_history_activity");
//                startActivity(new Intent(getContext(),
//                        HistoryActivity.class));
//                break;
//            case R.id.rc_start_soft_activity:
//                MyLog.log(TAG,"click the start_soft_activity");
//                startActivity(new Intent(getContext(),
//                        SoftwareActivity.class));
//                break;
            case R.id.rc_contact_btn:
                MyLog.log(TAG,"click the rc_contact_btn");
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                String ipAddr = ipEditView.getText().toString();
                if ("".equals(ipAddr)) Toast.makeText(getContext(),
                        "发送的ip尚未填写",Toast.LENGTH_SHORT).show();
                else {
                    intent.putExtra("ipAddress",ipAddr);
                    startActivity(intent);
                }
        }
    }

    /******初始化myIp和listView*****/
    private void initIps(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        myIp = inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        List<IpMsg>MsgList = DataSupport.select("iPAddress").
                order("iPAddress").find(IpMsg.class);
        if (MsgList.size()!= 0){
            String ipInList = MsgList.get(0).getIpAddress();
            //默认ip已经排序
            String ipCandidate;
            for (IpMsg ipMsg:MsgList){
                ipCandidate = ipMsg.getIpAddress();
                if (!ipInList.equals(ipCandidate)){
                    ipList.add(ipMsg);
                    ipInList = ipCandidate;
                }
            }
        }else{
            IpMsg ipMsg = new IpMsg("255.255.255");
            MsgList.add(ipMsg);
        }
    }

    /******用户展示listView*****/
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView ipAddress;
        View btn;
        public ViewHolder(View view){
            super(view);
            btn = view;
            ipAddress = (TextView)view.findViewById(R.id.ip_addr_item);
        }
    }

    public class IpAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<IpMsg> mIpList;

        public IpAdapter(List<IpMsg> ipList) {
            mIpList = ipList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.mainfrag_rcdaty_item_ip, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ipAddr = mIpList.get(holder.getAdapterPosition()).getIpAddress();
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.putExtra("ipAddress", ipAddr);
                    startActivity(intent);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            IpMsg ipMsg = mIpList.get(position);
            holder.ipAddress.setText(ipMsg.getIpAddress());
        }

        @Override
        public int getItemCount() {
            return mIpList.size();
        }
    }

    /*****数据库保存*******/
    public static void saveData(String ipAddr,String msgCtn,int type){
        //保存数据
        MyLog.log(TAG,"in saveData");
        IpMsg ipMsgData = new IpMsg(msgCtn,type);
        ipMsgData.setIpAddress(ipAddr);
        ipMsgData.setTime(formatter.format(new java.sql.Date
                (System.currentTimeMillis())));
        MyLog.log(TAG,"保存ipMsgData数据："+"Time:"+ipMsgData.getTime()+
                ",ip:"+ipMsgData.getIpAddress()+ ":content:"+ipMsgData.getContent()
                +",Type:"+ipMsgData.getType());
        ipMsgData.save();
    }

    /*****监听端口 ***********/
    private class Receiver extends AsyncTask<Integer,String,Boolean> {

        @Override
        protected Boolean doInBackground(Integer...receive){
            try {
                MyLog.log(TAG,"in doInBackground:监听端口:"+receive[0]);
                //1.创建一个2048字节的数组，用于接收数据
                byte[]buf = new byte[2048];
                //2.定义一个DatagramSocket对象，监听端口为receive[0]
                DatagramSocket ds = new DatagramSocket(receive[0]);
                //3.定义一个DatagramPacket对象，用于接收数据
                DatagramPacket dp = new DatagramPacket(buf,buf.length);
                while(true) {
                    //4.等待接受数据，没有数据则阻塞
                    ds.receive(dp);
                    String recvIp = dp.getAddress().getHostAddress();
                    MyLog.log(TAG,"myIp:"+myIp);
                    MyLog.log(TAG,"recvIp:"+recvIp);
                    if (!recvIp.equals(myIp)){
                        String str = new String(dp.getData(),0,dp.getLength());
                        MyLog.log(TAG,"in doInBackground:recContent："+str);
                        publishProgress(dp.getAddress().getHostAddress(),str);
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }


        @Override
        public void onProgressUpdate(String...str){
            MyLog.log(TAG,"in onProgressUpdate");

            //判断是否为界面已有ip
            boolean isNew = true;
            for (IpMsg ipMsg:ipList){
                MyLog.log(TAG,ipMsg.getIpAddress());
                if (ipMsg.getIpAddress().equals(str[0])){
                    isNew = false;
                    break;
                }
            }
            if (isNew){
                //新来一个用户更新用户展示ip界面
                MyLog.log(TAG,"isNew");
                IpMsg ip = new IpMsg(str[0]);
                ipList.add(ip);
            }
            adapter.notifyItemInserted(ipList.size());
            ipRecyclerView.scrollToPosition(ipList.size()-1);
            Intent intent = new Intent("com.enzosalvetore.circle.LOCAL_BROADCAST");
            //无论是否为新，都保存数据进数据库,并通知ChatActivity
            String chatAtyIpAddr = "";
            if (ChatActivity.ipAddr!=null){
                chatAtyIpAddr = ChatActivity.ipAddr;
            }
            if (!chatAtyIpAddr.equals("")){
                //用户已打开聊天活动
                if (!chatAtyIpAddr.equals("255.255.255.255")){
                    //此时为私聊，且接收到的消息不会是来自自己，publish之前已经过滤
                    MyLog.log(TAG,"私聊,收到："+str[1]);
                    //接收到的消息为已打开聊天活动的消息，则通知当前活动
                    if (chatAtyIpAddr.equals(str[0]))intent.putExtra("recv_msg",str[1]);
                    saveData(str[0],str[1], IpMsg.TYPE_RECEIVE);
                }else {
                    //此时为群聊，将发送者ip保存到content中
                    MyLog.log(TAG,"群聊");
                    intent.putExtra("recv_msg",str[0]+":"+str[1]);
                    saveData("255.255.255.255",str[0]+":"+str[1],IpMsg.TYPE_RECEIVE);
                }
            }else {
                //此时用户还没点开聊天活动，因此保存到私聊记录中
                MyLog.log(TAG,"私聊_用户没有点开群聊");
                saveData(str[0],str[1],IpMsg.TYPE_RECEIVE);
            }
            localBroadcastManager.sendBroadcast(intent);
            MyLog.log(TAG,"out onProgressUpdate");
        }
    }
}