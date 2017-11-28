package com.example.liuchun.tv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.menuListView)
    ListView menuListView;
    private String[] names;
    private String[] urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        names = new String[]{"CCTV-1高清 ",
                "CCTV-1高清 ",
                "CCTV-2高清 ",
                "CCTV-2高清 ",
                "CCTV-3高清 ",
                "CCTV-3高清 ",
                "CCTV-4高清 ",
                "CCTV-5高清 ",
                "CCTV-5高清 ",
                "CCTV-5高清 ",
                "CCTV-5+高清 ",
                "CCTV-5+高清 ",
                "CCTV-5+高清 ",
                "CCTV-6高清 ",
                "CCTV-6高清 ",
                "CCTV-7高清 ",
                "CCTV-7高清 ",
                "CCTV-8高清 ",
                "CCTV-9高清 ",
                "CCTV-9高清 ",
                "CCTV-10高清 ",
                "CCTV-10高清 ",
                "CCTV-12高清 ",
                "CCTV-12高清 ",
                "CCTV-14高清 ",
                "凤凰中文高清 ",
                "凤凰资讯高清 ",
                "凤凰香港高清 ",
                "翡翠台高清 ",
                "河南卫视高清 ",
                "河南卫视高清 ",
                "天津卫视高清 ",
                "天津卫视高清 ",
                "北京卫视高清 ",
                "纪实频道高清 ",
                "湖南卫视高清 ",
                "湖北卫视高清 ",
                "浙江卫视高清 ",
                "东方卫视高清 ",
                "安徽卫视高清 ",
                "山东卫视高清 ",
                "广东卫视高清 ",
                "深圳卫视高清 ",
                "重庆卫视高清 ",
                "江苏卫视高清 ",
                "江西卫视高清 ",
                "辽宁卫视高清 ",
                "黑龙江卫视高清"};
        urls = new String[]{"http://183.251.61.207/PLTV/88888888/224/3221225922/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225922/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225923/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225923/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225924/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225924/index.m3u8",
                "http://61.166.153.32:1180/play/p ... 199/live100001.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225925/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225925/index.m3u8 ",
                "http://183.251.61.207/PLTV/88888888/224/3221225912/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225915/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225939/index.m3u8 ",
                "http://183.252.176.10/PLTV/88888888/224/3221225939/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225926/index.m3u8 ",
                "http://183.252.176.10/PLTV/88888888/224/3221225926/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225927/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225927/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225928/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225929/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225929/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225931/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225931/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225932/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225932/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225933/index.m3u8",
                "http://223.110.245.139:80/PLTV/3/224/3221226977/index.m3u8",
                "http://223.110.245.139:80/PLTV/3/224/3221226980/index.m3u8",
                "http://223.110.245.139:80/PLTV/3/224/3221226975/index.m3u8",
                "http://acm.gg/jade.m3u8",
                "http://live.hntv.tv:9602/live/li ... 0&KEY2=5a0aa17b",
                "http://live.hntv.tv:9601/live/li ... 0&KEY2=5a0aa17b",
                "http://183.251.61.207/PLTV/88888888/224/3221225941/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225941/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225937/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225946/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225935/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225948/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225934/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225936/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225945/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225943/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225942/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225938/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225949/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225930/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225834/index.m3u8",
                "http://183.252.176.10/PLTV/88888888/224/3221225947/index.m3u8",
                "http://183.251.61.207/PLTV/88888888/224/3221225940/index.m3u8",
        };
        menuListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, names));
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, InternetVideoDemo.class);
                intent.putExtra("url",urls[position]);
                startActivity(intent);
            }
        });
    }


}
