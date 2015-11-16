package com.minutes.swipelayoutdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.minutes.swipelayout.MaterialHeader;
import com.minutes.swipelayout.PhoenixHeader;
import com.minutes.swipelayout.PullToLoadMoreFooter;
import com.minutes.swipelayout.PullToRefreshHeader;
import com.minutes.swipelayout.SwipeToRefreshLayout;
import com.minutes.swipelayout.SwipeToRefreshListener;

import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements SwipeToRefreshListener {

    final static List<String> list = new Vector<>();

    static {
        for (int i = 0; i < 30; i++) {
            list.add("Test data " + i);
        }
    }

    private SwipeToRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new DemoAdapter());

        swipeLayout = (SwipeToRefreshLayout) findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(this);
    }

    @Override
    public void onLoadMore(final SwipeToRefreshLayout layout) {
        layout.postDelayed(new Runnable() {

            @Override
            public void run() {
                layout.setRefreshing(false);
            }

        }, 2000);
    }

    @Override
    public void onPull2Refresh(final SwipeToRefreshLayout layout) {
        layout.postDelayed(new Runnable() {

            @Override
            public void run() {
                layout.setRefreshing(false);
            }

        }, 2000);
    }

    class DemoAdapter extends RecyclerView.Adapter<DemoViewHolder> {

        @Override
        public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.test_list_item, parent, false);
            return new DemoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DemoViewHolder holder, int position) {
            holder.text1.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class DemoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView text1;

        public DemoViewHolder(View itemView) {
            super(itemView);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text1.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), text1.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_temp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.jump){
            startActivity(new Intent(this, TempTestActivity.class));
            return true;
        }
        if (id == R.id.styleMaterial) {
            MaterialHeader header = new MaterialHeader();
            swipeLayout.setHeader(header);
            swipeLayout.setMode(SwipeToRefreshLayout.MODE_PULL_DOWN_TO_REFRESH);
            return true;
        }
        if (id == R.id.stylePullToRefresh) {
            PullToRefreshHeader head = new PullToRefreshHeader();
            PullToLoadMoreFooter footer = new PullToLoadMoreFooter();
            swipeLayout.setHeader(head);
            swipeLayout.setFooter(footer);
            swipeLayout.setMode(SwipeToRefreshLayout.MODE_BOTH);
            return true;
        }
        if (id == R.id.stylePhoenix) {
            PhoenixHeader head = new PhoenixHeader();
            swipeLayout.setHeader(head);
            swipeLayout.setMode(SwipeToRefreshLayout.MODE_PULL_DOWN_TO_REFRESH);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
