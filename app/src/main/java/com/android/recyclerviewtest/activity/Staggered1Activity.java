package com.android.recyclerviewtest.activity;


import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.android.recyclerviewtest.R;
import com.android.recyclerviewtest.adapter.SingleTypeAdapter;
import com.android.recyclerviewtest.adapter.Staggered1Adapter;
import com.android.recyclerviewtest.data.DataUtil;
import com.android.recyclerviewtest.utils.RLog;
import com.android.recyclerviewtest.utils.ToastUtil;

import java.util.List;

/**
 * ======================================================================
 * <p/>
 * 作者：Renj
 * <p/>
 * 创建时间：2017-04-12    17:30
 * <p/>
 * 描述：垂直方向瀑布流<br/><br/>
 * <pre>
 * <b>一、使用瀑布流显示图片可能出现的问题以及解决办法：
 *
 * <b>问题：
 *      <b>1.item 的位置不断发生变化</b>
 *      <b>2.当解决第一个问题时，会产生另外一个问题，顶部会留下空白</b>
 *      <b>3.因为 item 布局的复用，但是每张图片的高度又不同，所以导致 item 图片闪烁问题</b>
 *
 * <b>解决办法：</b>
 *      <b>1.解决第一个问题：调用 StaggeredGridLayoutManager 的 setGapStrategy(int gapStrategy) 方法</b>
 *      <code>staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);</code>
 *      <b>2.解决第二个问题：给 RecyclerView 控件增加监听，并且在监听中调用调用 StaggeredGridLayoutManager 的 invalidateSpanAssignments() 方法</b>
 *      <code>recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {</code>
 *          <code>@Override</code>
 *          <code>public void onScrollStateChanged(RecyclerView recyclerView, int newState) {</code>
 *              <code>super.onScrollStateChanged(recyclerView, newState);</code>
 *              <code>layoutManager.invalidateSpanAssignments();</code>
 *          <code>}</code>
 *      <code>});</code>
 *      <b>3.解决第三个问题：
 *      使用一个集合保存每一个 item 的高度，然后在显示的时候对每一个 item 的高度重新设置</b>
 * </pre>
 * <p/>
 * <pre>
 * <b>二、使用 Adapter 的</b> <code>notifyItemRemoved(position)</code> <b>和</b> <code>notifyItemInserted(position)</code> <b>方法产生的问题：</b>
 *
 * <b>问题：</b>
 *      <b>使用上面两个方法之后不会使 position 及其之后位置的 itemView 重新 onBindViewHolder，会导致下标错乱，
 *     如果一直调用 <code>notifyItemRemoved(position)</code> 来移除的话，那么就会发现真正移除的并不是想要移出的，
 *     而且还非常有可能出现 {@link IndexOutOfBoundsException} 异常。</b>
 * <b>解决：</b>
 *      <b>在调用上面两个方法(其中一个)之后继续调用 Adapter 的</b> <code>notifyItenRangeChanged(int positionStart, int itemCount)</code> <b>方法，
 *     使下面的 itemView 重新 onBind，就可以了。</b>
 * </pre>
 * 修订历史：
 * <p/>
 * ======================================================================
 */

public class Staggered1Activity extends BaseActivity {
    private TextView title;
    private RecyclerView recyclerView;
    private List<String> datas;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        title = (TextView) findViewById(R.id.title);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        title.setText("垂直方向瀑布流(点击 item 删除图片)");

        datas = DataUtil.getImageList();
        setRecyclerView();
    }

    private void setRecyclerView() {
        final Staggered1Adapter adapter = new Staggered1Adapter(this, datas, R.layout.item_staggred_1);
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // 增加 item 动画，使用默认实现，
        // 当使用adapter.notifyItemRemoved(position) 和 adapter.notifyItemInserted(position) 时有效果
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 解决 item 位置跳动问题，会导致顶部留下空白问题
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        // 解决顶部留下空白问题
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                layoutManager.invalidateSpanAssignments();
            }
        });

        // 设置点击事件
        adapter.setOnItemClickListener(new SingleTypeAdapter.OnItemClickListener<String>() {
            @Override
            public void onItemClick(View itemView, int position, List<String> datas, String itemData) {
                RLog.i("剩余数据个数：" + datas.size() + "; 删除位置：" + position);
                ToastUtil.showSingleToast(Staggered1Activity.this, "删除位置：" + position);
                datas.remove(position);

                // 刷新全部，图片会发生跳动
                // adapter.notifyDataSetChanged();

                // 只刷新指定位置
                adapter.notifyItemRemoved(position);  // 指定位置删除数据
                adapter.notifyItemRangeChanged(position, datas.size() - position); // 一定要重新排列位置
                // adapter.notifyItemChanged(position);  // 指定位置改变数据
                // adapter.notifyItemInserted(position); // 指定位置新增数据
            }
        });
    }
}
