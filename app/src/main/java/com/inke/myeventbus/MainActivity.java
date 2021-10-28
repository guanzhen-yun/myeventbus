package com.inke.myeventbus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        startActivity(new Intent(this, SecondActivity.class));
    }

    // 需要对方法做一个标识，告诉Eventbus只将带有该标识的方法放到Eventbus
    @Subscrible(threadMode = ThreadMode.MAIN)
    public void getMessage(User user) {
        Log.e("Netease", user.toString());
    }
}