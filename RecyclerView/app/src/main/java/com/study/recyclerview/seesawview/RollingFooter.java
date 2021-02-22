package com.study.recyclerview.seesawview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.study.recyclerview.R;
import com.study.recyclerview.databinding.LayoutSeesawViewFooterBinding;

public class RollingFooter extends FrameLayout implements IRvFooter {
    private LayoutSeesawViewFooterBinding binding;

    public RollingFooter(@NonNull Context context) {
        super(context);
        binding = LayoutSeesawViewFooterBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public int getMinLoadingTime() {
        return 2000;
    }

    @Override
    public void onStartLoading() {
        changeChildVisibility(true, false, false);
        binding.rbLoading.ignoreMoving();
        binding.rbLoading.startAnimator();
    }

    @Override
    public void onFinish(boolean isSuccess, boolean noMoreData) {
        binding.rbLoading.stopAnimator();
        changeChildVisibility(false, isSuccess, noMoreData);
    }

    public void changeChildVisibility(boolean isLoading, boolean isSuccess, boolean noMoreData) {
        if (isLoading) {
            binding.tvTips.setVisibility(GONE);
            binding.rbLoading.setVisibility(VISIBLE);
            return;
        }
        binding.rbLoading.setVisibility(GONE);
        if (isSuccess) {
            if (noMoreData) {
                // 没有更多
                binding.tvTips.setText(R.string.load_more_no_more_tips);
                binding.tvTips.setVisibility(VISIBLE);
            } else {
                // 加载成功，显示新加载的item
                binding.tvTips.setVisibility(GONE);
            }
            return;
        }
        // 加载失败
        binding.tvTips.setText(R.string.load_more_failure_tips);
        binding.tvTips.setVisibility(VISIBLE);
    }
}
