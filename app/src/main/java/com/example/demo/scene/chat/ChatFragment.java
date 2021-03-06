package com.example.demo.scene.chat;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.effective.R;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.view.panel.IPanelView;
import com.effective.android.panel.view.panel.PanelView;
import com.effective.databinding.CommonChatWithTitlebarLayoutBinding;
import com.example.demo.Constants;
import com.example.demo.anno.ChatPageType;
import com.example.demo.scene.chat.adapter.ChatAdapter;
import com.example.demo.scene.chat.adapter.ChatInfo;
import com.example.demo.scene.chat.emotion.EmotionPagerView;
import com.example.demo.scene.chat.emotion.Emotions;
import com.example.demo.systemui.StatusbarHelper;
import com.example.demo.util.DisplayUtils;
import com.rd.PageIndicatorView;

public class ChatFragment extends Fragment {

    private CommonChatWithTitlebarLayoutBinding mBinding;
    private PanelSwitchHelper mHelper;
    private ChatAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private static final String TAG = "ChatFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.common_chat_with_titlebar_layout, container, false);

        int type = getArguments().getInt(Constants.KEY_PAGE_TYPE);

        switch (type) {
            case ChatPageType.COLOR_STATUS_BAR: {
                mBinding.getRoot().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.common_page_bg_color));
                mBinding.statusBar.setColor(R.color.colorPrimary);
                mBinding.titleBar.setVisibility(View.VISIBLE);
                mBinding.titleBar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                mBinding.title.setText("Fragment-??????????????????,???????????????");
                break;
            }
            case ChatPageType.TRANSPARENT_STATUS_BAR: {
                StatusbarHelper.setStatusBarColor(getActivity(), Color.TRANSPARENT);
                mBinding.titleBar.setVisibility(View.VISIBLE);
                mBinding.title.setText("Fragment-????????????????????????????????????");
                break;
            }
            default: {
                mBinding.getRoot().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.common_page_bg_color));
                mBinding.statusBar.setColor(R.color.colorPrimary);
                mBinding.titleBar.setVisibility(type == ChatPageType.DEFAULT ? View.GONE : View.VISIBLE);
                mBinding.titleBar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                mBinding.title.setText("Fragment-??????????????????");
            }
        }
        initView();
        return mBinding.getRoot();
    }


    private void initView() {
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mBinding.recyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new ChatAdapter(getContext(), 50);
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mBinding.editText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(getContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAdapter.insertInfo(ChatInfo.CREATE(content));
                mBinding.editText.setText(null);
                scrollToBottom();
            }
        });
    }

    private void scrollToBottom() {
        mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mHelper == null) {
            mHelper = new PanelSwitchHelper.Builder(this)
                    //??????
                    .addKeyboardStateListener((visible, height) -> Log.d(TAG, "???????????????????????? : " + visible + " ????????????" + height))
                    //??????
                    .addEditTextFocusChangeListener((view, hasFocus) -> {
                        Log.d(TAG, "??????????????????????????? : " + hasFocus);
                        if(hasFocus){
                            scrollToBottom();
                        }
                    })
                    //??????
                    .addViewClickListener(view -> {
                        switch (view.getId()){
                            case R.id.edit_text:
                            case R.id.add_btn:
                            case R.id.emotion_btn:{
                                scrollToBottom();
                            }
                        }
                        Log.d(TAG, "?????????View : " + view);
                    })
                    //??????
                    .addPanelChangeListener(new OnPanelChangeListener() {

                        @Override
                        public void onKeyboard() {
                            Log.d(TAG, "?????????????????????");
                            mBinding.emotionBtn.setSelected(false);
                        }

                        @Override
                        public void onNone() {
                            Log.d(TAG, "??????????????????");
                            mBinding.emotionBtn.setSelected(false);
                        }

                        @Override
                        public void onPanel(IPanelView view) {
                            Log.d(TAG, "???????????? : " + view);
                            if(view instanceof PanelView){
                                mBinding.emotionBtn.setSelected(((PanelView)view).getId() == R.id.panel_emotion ? true : false);
                            }
                        }

                        @Override
                        public void onPanelSizeChange(IPanelView panelView, boolean portrait, int oldWidth, int oldHeight, int width, int height) {
                            if(panelView instanceof PanelView){
                                switch (((PanelView)panelView).getId()) {
                                    case R.id.panel_emotion: {
                                        EmotionPagerView pagerView = mBinding.getRoot().findViewById(R.id.view_pager);
                                        int viewPagerSize = height - DisplayUtils.dip2px(getContext(), 30f);
                                        pagerView.buildEmotionViews(
                                                (PageIndicatorView) mBinding.getRoot().findViewById(R.id.pageIndicatorView),
                                                mBinding.editText,
                                                Emotions.getEmotions(), width, viewPagerSize);
                                        break;
                                    }
                                    case R.id.panel_addition: {
                                        //auto center,nothing to do
                                        break;
                                    }
                                }
                            }
                        }
                    })
                    .logTrack(true)             //output log
                    .build();
        }
        mBinding.recyclerView.setPanelSwitchHelper(mHelper);
    }

    public boolean hookOnBackPressed() {
        return mHelper != null && mHelper.hookSystemBackByPanelSwitcher();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
