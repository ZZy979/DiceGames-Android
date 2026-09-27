package com.zzy.dicegames.ui.about;

import android.os.Bundle;

import com.zzy.dicegames.R;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 用于查看关于信息的{@code Activity}<br>
 * 传入数据：无<br>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
