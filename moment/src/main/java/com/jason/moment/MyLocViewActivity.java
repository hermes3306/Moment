package com.jason.moment;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jason.moment.util.db.MyLoc;
import com.jason.moment.util.MyActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.jason.moment.util.db.MyLocAdapter;


public class MyLocViewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MyLocAdapter adapter;
    private MyLoc myLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myloc_view);

        myLoc = MyLoc.getInstance(this);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyLocAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        loadData();
    }

    private void loadData() {
        List<MyActivity> activities = myLoc.getToodayActivities();
        Collections.reverse(activities);
        adapter.updateData(activities);
        adapter.updateData(activities);
    }

    private void refreshData() {
        loadData();
        swipeRefreshLayout.setRefreshing(false);
    }
}
