package com.remmoo997.igtvsaver.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.adapter.RecyclerViewAdapter;

import java.util.ArrayList;

public class GridActivity extends AppCompatActivity {

    private ArrayList<String> multi_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        multi_data = getIntent().getStringArrayListExtra("multi");
        String url = getIntent().getStringExtra("URL");
        String title = getIntent().getStringExtra("title");

        RecyclerView recyclerView = findViewById(R.id.recycler_view1);
        //Change 2 to your choice because here 2 is the number of Grid layout Columns in each row.
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(this, 2);
        recyclerViewLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (multi_data.size() % 2 != 0) {
                    return (position == multi_data.size() - 1) ? 2 : 1;
                } else {
                    return 1;
                }
            }
        });
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        RecyclerView.Adapter recyclerView_Adapter = new RecyclerViewAdapter(getApplicationContext(), multi_data, url, title);
        recyclerView.setAdapter(recyclerView_Adapter);
    }
}
