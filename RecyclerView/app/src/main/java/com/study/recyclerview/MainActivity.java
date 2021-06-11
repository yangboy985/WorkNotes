package com.study.recyclerview;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.lepu.recure.util.RvGridDivider;
import com.study.recyclerview.databinding.ActivityMainBinding;
import com.study.recyclerview.databinding.LayoutRvItemTestBinding;
import com.study.recyclerview.recyclerview.BaseRvAdapter;
import com.study.recyclerview.recyclerview.RvDataCtrl;
import com.study.recyclerview.recyclerview.RvHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mViewBinder;

    private BaseRvAdapter<String> adapter = new BaseRvAdapter<String>() {
        @NonNull
        @Override
        public RvHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TestRvHolder(LayoutRvItemTestBinding.inflate(getLayoutInflater(), parent, false));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinder = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mViewBinder.getRoot());

        initView();
    }

    private void initView() {
        mViewBinder.rv.setAdapter(adapter);
        mViewBinder.rv.addItemDecoration(new RvGridDivider(mViewBinder.rv, 30, 60));
        RvDataCtrl<String> ctrl = adapter.getDataCtrl();
        ctrl.addAll(getInitData());
        GridLayoutManager gm = (GridLayoutManager) mViewBinder.rv.getLayoutManager();
        gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 8 || position == 9) {
                    return 2;
                }
                if (position == 4) {
                    return 3;
                }
                if (position == 20) {
                    return 3;
                }
                return 1;
            }
        });
    }

    private List<String> getInitData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            data.add(String.valueOf(i + 1));
        }
        return data;
    }

    static class TestRvHolder extends RvHolder<String> {
        private LayoutRvItemTestBinding binding = null;

        public TestRvHolder(@NonNull LayoutRvItemTestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bindData(String data) {
            binding.tv.setText(data);
        }
    }
}