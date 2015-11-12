package com.minutes.swipelayoutdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.minutes.swipelayout.temp.MaterialHeader;
import com.minutes.swipelayout.temp.PhoenixHeader;
import com.minutes.swipelayout.temp.PullToRefreshHeader;
import com.minutes.swipelayout.temp.ScrollMode;
import com.minutes.swipelayout.temp.SwipeLayout;

import java.util.List;
import java.util.Vector;

/**
 * <p>Description  : TempTestActivity.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/12.</p>
 * <p>Time         : 上午11:06.</p>
 */
public class TempTestActivity extends AppCompatActivity {

    final static List<String> list = new Vector<>();
    static {
        for (int i = 0; i < 30; i++){
            list.add("Test data " + i);
        }
    }

    SwipeLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new DemoAdapter());

        swipeLayout = (SwipeLayout) findViewById(R.id.swipeLayout);

    }

    class DemoAdapter extends RecyclerView.Adapter<DemoViewHolder>{

        @Override
        public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(TempTestActivity.this).inflate(android.R.layout.test_list_item, parent, false);
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

    class DemoViewHolder extends RecyclerView.ViewHolder{
        final TextView text1;
        public DemoViewHolder(View itemView) {
            super(itemView);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.styleMaterial) {
            MaterialHeader header = new MaterialHeader(this);
            swipeLayout.setHeaderView(header);

            MaterialHeader.MaterialFooter footer = new MaterialHeader.MaterialFooter(this);
            swipeLayout.setFooterView(footer);

            swipeLayout.setScrollMode(ScrollMode.SCROLL_FONT);
            return true;
        }

        if (id == R.id.stylePullToRefresh) {
            PullToRefreshHeader head = new PullToRefreshHeader(this);
            swipeLayout.setHeaderView(head);

            PullToRefreshHeader.PullToRefreshFooter footer = new PullToRefreshHeader.PullToRefreshFooter(this);
            swipeLayout.setFooterView(footer);

            swipeLayout.setScrollMode(ScrollMode.SCROLL_FOLLOW);
            return true;
        }

        if (id == R.id.stylePhoenix) {
            PhoenixHeader head = new PhoenixHeader(this);
            swipeLayout.setHeaderView(head);
            swipeLayout.setScrollMode(ScrollMode.SCROLL_BACK);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
