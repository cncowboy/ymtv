package com.lemon95.ymtv.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lemon95.androidtvwidget.bridge.EffectNoDrawBridge;
import com.lemon95.androidtvwidget.bridge.OpenEffectBridge;
import com.lemon95.androidtvwidget.view.MainUpView;
import com.lemon95.ymtv.R;
import com.lemon95.ymtv.adapter.FavoritesAdapter;
import com.lemon95.ymtv.bean.FavoritesBean;
import com.lemon95.ymtv.myview.ConfirmDialog;
import com.lemon95.ymtv.presenter.HistoryPresenter;
import com.lemon95.ymtv.presenter.MovieDetailsPresenter;
import com.lemon95.ymtv.utils.LogUtils;
import com.lemon95.ymtv.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity{

    private GridView lemon_gridview;
    private MainUpView mainUpView1;
    private TextView lemon_msg;
    private HistoryPresenter historyPresenter = new HistoryPresenter(this);
    private ProgressBar lemon_movie_details_pro;
    private FavoritesAdapter favoritesAdapter;
    private View mOldView;
    private List<FavoritesBean.Data> listData;
    private boolean isDelete = false;
    OpenEffectBridge mOpenEffectBridge;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_history;
    }

    @Override
    protected void setupViews() {
        lemon_gridview = (GridView)findViewById(R.id.lemon_gridview);
        mainUpView1 = (MainUpView) findViewById(R.id.mainUpView1);
        lemon_msg = (TextView) findViewById(R.id.lemon_msg);
        lemon_movie_details_pro = (ProgressBar) findViewById(R.id.lemon_movie_details_pro);
        // 建议使用 NoDraw.
        mainUpView1.setEffectBridge(new EffectNoDrawBridge());
        mOpenEffectBridge = (EffectNoDrawBridge) mainUpView1.getEffectBridge();
        mOpenEffectBridge.setTranDurAnimTime(100);
        // 设置移动边框的图片.
        mainUpView1.setUpRectResource(R.drawable.health_focus_border);
        // 移动方框缩小的距离.
        mainUpView1.setDrawUpRectPadding(new Rect(10, -10, 4, -43));
        lemon_gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        lemon_gridview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * 这里注意要加判断是否为NULL.
                 * 因为在重新加载数据以后会出问题.
                 */
                LogUtils.i(TAG, "焦点改变");
                if (view != null) {
                    view.bringToFront();
                    mainUpView1.setFocusView(view, mOldView, 1.1f);
                }
                mOldView = view;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        lemon_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FavoritesBean.Data video = listData.get(position);
                Bundle bundle = new Bundle();
                bundle.putString("videoId", video.getVideoId());
                bundle.putString("videoType", video.getVideoTypeId());
                startActivity(MovieDetailsActivity.class, bundle);
            }
        });
        lemon_gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final FavoritesBean.Data video = listData.get(position);
                ConfirmDialog.Builder dialog = new ConfirmDialog.Builder(HistoryActivity.this);
                dialog.setMessage(video.getVideoName());
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("删除该片", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        historyPresenter.deleteVideo(listData, video);
                        dialog.dismiss();
                    }
                });
                dialog.create().show();
                return true;
            }
        });
        lemon_gridview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                if (lemon_gridview.getChildCount() > 0) {
                    // int v1 = lemon_gridview.getSelectedItemPosition();
                    if (isDelete) {
                        lemon_gridview.setSelection(0);
                        View newView = lemon_gridview.getChildAt(0);
                        newView.bringToFront();
                        mainUpView1.setFocusView(newView, 1.1f);
                        mOldView = lemon_gridview.getChildAt(0);
                        isDelete = false;
                    }
                }
            }
        });
        lemon_gridview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (lemon_gridview.getChildCount() > 0) {
                        // int v1 = lemon_gridview.getSelectedItemPosition();
                        lemon_gridview.setSelection(0);
                        View newView = lemon_gridview.getChildAt(0);
                        newView.bringToFront();
                        mainUpView1.setFocusView(newView, 1.1f);
                        mOldView = lemon_gridview.getChildAt(0);
                    }
                    LogUtils.i(TAG,"gridView 获取焦点");
                }
            }
        });
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    protected void initialized() {
        //获取收藏数据
        showPro();
        historyPresenter.getFavorites();
    }

    public void showPro() {
        lemon_movie_details_pro.setVisibility(View.VISIBLE);
        lemon_msg.setVisibility(View.GONE);
        lemon_gridview.setVisibility(View.GONE);
    }

    public void hidePro() {
        lemon_movie_details_pro.setVisibility(View.GONE);
        lemon_msg.setVisibility(View.VISIBLE);
        lemon_gridview.setVisibility(View.VISIBLE);
    }

    public void showError(String msg) {
        mainUpView1.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        lemon_msg.setText(msg);
        lemon_msg.setVisibility(View.VISIBLE);
        lemon_gridview.setVisibility(View.GONE);
        lemon_movie_details_pro.setVisibility(View.GONE);
    }

    //初始化收藏数据
    public void showFavoriteData(List<FavoritesBean.Data> listData) {
        this.listData = listData;
        favoritesAdapter = new FavoritesAdapter(listData,context);
        lemon_gridview.setAdapter(favoritesAdapter);
        favoritesAdapter.notifyDataSetChanged();
        hidePro();
    }


}
