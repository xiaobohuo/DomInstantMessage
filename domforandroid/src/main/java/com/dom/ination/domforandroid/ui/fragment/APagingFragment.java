package com.dom.ination.domforandroid.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.dom.ination.domforandroid.R;
import com.dom.ination.domforandroid.common.context.GlobalContext;
import com.dom.ination.domforandroid.common.utils.ActivityHelper;
import com.dom.ination.domforandroid.common.utils.Logger;
import com.dom.ination.domforandroid.common.utils.ViewUtils;
import com.dom.ination.domforandroid.network.biz.IResult;
import com.dom.ination.domforandroid.network.task.TaskException;
import com.dom.ination.domforandroid.support.paging.IPaging;
import com.dom.ination.domforandroid.ui.fragment.adpter.IPagingAdapter;
import com.dom.ination.domforandroid.ui.fragment.itemview.AFooterItemView;
import com.dom.ination.domforandroid.ui.fragment.itemview.AHeaderItemViewCreator;
import com.dom.ination.domforandroid.ui.fragment.itemview.BasicFooterView;
import com.dom.ination.domforandroid.ui.fragment.itemview.IItemView;
import com.dom.ination.domforandroid.ui.fragment.itemview.IItemViewCreator;
import com.dom.ination.domforandroid.ui.fragment.itemview.OnFooterViewListener;
import com.dom.ination.domforandroid.ui.widget.DomToolBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10174987 on 2016/8/29.
 */

public abstract class APagingFragment<T extends Serializable, Ts extends Serializable, V extends ViewGroup> extends ABaseFragment implements DomToolBar.OnToolbarDoubleClick, OnFooterViewListener, AFooterItemView.OnFooterViewCallback {
    private static final String TAG = "APagingFragment";

    public static final String PAGING_TASK_ID = "com.dom.ination.domforandroid.PAGING_TASK";

    private static final String SAVED_DATAS = "com.dom.ination.domforandroid.ui.Datas";
    private static final String SAVED_PAGING = "com.dom.ination.domforandroid.ui.Paging";
    private static final String SAVED_CONFIG = "com.dom.ination.domforandroid.Config";

    private IPaging mPaging;// 分页器

    private IPagingAdapter<T> mAdapter;

    private APagingTask pagingTask;// 分页线程

    RefreshConfig refreshConfig;// 刷新方面的配置

    IItemViewCreator<T> mFooterItemViewCreator;
    AFooterItemView<T> mFooterItemView;// FooterView，滑动到底部时，自动加载更多数据

    AHeaderItemViewCreator<T> mHeaderItemViewCreator;

    public enum RefreshMode {
        // 重设数据
        reset,
        //上拉，加载更多
        update,
        // 下拉，刷新最新
        refresh
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            refreshConfig = new RefreshConfig();
        } else {
            refreshConfig = (RefreshConfig) savedInstanceState.getSerializable(SAVED_CONFIG);
        }

        ArrayList<T> datas = savedInstanceState == null ? new ArrayList<T>() : (ArrayList<T>) savedInstanceState.getSerializable(SAVED_DATAS);
        mAdapter = newAdapter(datas);

        if (savedInstanceState != null && savedInstanceState.getSerializable(SAVED_PAGING) != null) {
            mPaging = (IPaging) savedInstanceState.getSerializable(SAVED_PAGING);
        } else {
            mPaging = newPaging();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 将分页信息保存起来
        if (mPaging != null)
            outState.putSerializable(SAVED_PAGING, mPaging);
        if (refreshConfig != null)
            outState.putSerializable(SAVED_CONFIG, refreshConfig);

        onSaveDatas(outState);

        super.onSaveInstanceState(outState);
    }

    protected void onSaveDatas(Bundle outState) {
        // 将数据保存起来
        if (getAdapter() != null && getAdapter().getDatas().size() != 0)
            outState.putSerializable(SAVED_DATAS, getAdapter().getDatas());
    }

    @Override
    void _layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        super._layoutInit(inflater, savedInstanceState);
        setupRefreshConfig(refreshConfig);
        setupRefreshView(savedInstanceState);
        setupRefreshViewWithConfig(refreshConfig);
        bindAdapter(getAdapter());
    }

    public IPagingAdapter getAdapter() {
        return mAdapter;
    }

    public ArrayList<T> getAdapterItems() {
        return mAdapter.getDatas();
    }

    public void onPullDownToRefresh() {
        requestData(RefreshMode.refresh);
    }

    public void onPullUpToRefresh() {
        requestData(RefreshMode.update);
    }

    @Override
    public boolean isContentEmpty() {
        return getAdapter() == null || getAdapter().getDatas().size() == 0;
    }

    // 子类不再允许重写这个类
    @Override
    final protected void onTaskStateChanged(ABaseTaskState state, TaskException exception) {
        // super.onTaskStateChanged(state, tag);
    }

    protected void onTaskStateChanged(ABaseTaskState state, TaskException exception, RefreshMode mode) {
        super.onTaskStateChanged(state, exception);

        onTaskStateChanged(mFooterItemView, state, exception, mode);

        if (state == ABaseTaskState.success) {
            if (isContentEmpty()) {
                if (emptyLayout != null && !TextUtils.isEmpty(refreshConfig.emptyHint))
                    ViewUtils.setTextViewValue(emptyLayout, R.id.txtLoadEmpty, refreshConfig.emptyHint);
            }
        } else if (state == ABaseTaskState.failed) {
            if (isContentEmpty()) {
                if (loadFailedLayout != null && !TextUtils.isEmpty(exception.getMessage()))
                    ViewUtils.setTextViewValue(loadFailedLayout, R.id.txtLoadFailed, exception.getMessage());
            }
        } else if (state == ABaseTaskState.finished) {
            onRefreshViewFinished(mode);
        }
    }

    public void setAdapterItems(ArrayList<T> items) {
        mAdapter.getDatas().clear();
        mAdapter.getDatas().addAll(items);
    }

    protected IPaging<T, Ts> newPaging() {
        return null;
    }

    abstract public IItemViewCreator<T> configItemViewCreator();

    abstract public void requestData(RefreshMode mode);

    abstract public V getRefreshView();

    abstract protected IPagingAdapter<T> newAdapter(ArrayList<T> datas);

    abstract protected void bindAdapter(IPagingAdapter adapter);

    //某些控件，设置它的刷新状态，它会自己自动回调Callback去刷新数据，true即这种情况
    public boolean setRefreshViewToLoading() {
        return false;
    }

    protected void setupRefreshViewWithConfig(RefreshConfig config) {

    }

    public void onRefreshViewFinished(RefreshMode mode) {

    }

    //初始化RefreshView
    protected void setupRefreshView(Bundle savedInstanceSate) {
        if (refreshConfig != null && refreshConfig.footerMoreEnable) {
            mFooterItemViewCreator = configFooterViewCreator();
            View convertView = mFooterItemViewCreator.newContentView(getActivity().getLayoutInflater(), null, IPagingAdapter.TYPE_FOOTER);
            mFooterItemView = (AFooterItemView<T>) mFooterItemViewCreator.newItemView(convertView, IPagingAdapter.TYPE_FOOTER);
        }

        mHeaderItemViewCreator = configHeaderViewCreator();

        if (mFooterItemView != null) {
            addFooterViewToRefreshView(mFooterItemView);
        }
        if (mHeaderItemViewCreator != null) {
            addHeaderViewToRefreshView(mHeaderItemViewCreator);
        }
    }

    public boolean isRefreshing() {
        return pagingTask != null;
    }


    //子类配置
    protected void setupRefreshConfig(RefreshConfig config) {

    }

    @Override
    public boolean onToolbarDoubleClick() {
        return false;
    }


    public static class RefreshConfig implements Serializable {
        private static final long serialVersionUID = 6244426943442129360L;
        public boolean pagingEnd = false;// 分页是否结束
        public String positionKey = null;// 最后阅读坐标的Key，null-不保存，针对缓存数据有效
        public boolean displayWhenScrolling = true;// 滚动的时候加载图片
        public int releaseDelay = 5 * 1000;// 当配置了releaseItemIds参数时，离开页面后自动释放资源
        public int[] releaseItemIds = null;// 离开页面时，释放图片的控件，针对ItemView
        public String emptyHint = "数据为空";// 如果EmptyLayout中有R.id.txtLoadEmpty这个控件，将这个提示绑定显示
        public boolean footerMoreEnable = true;// FooterView加载更多
    }

    /*********************************************
     * 开始数据刷新方法
     ************************************************/
    @Override
    public void requestData() {
        // 如果没有Loading视图，且数据为空，就显示FootView加载状态
        RefreshMode mode = RefreshMode.reset;
        if (getAdapter().getDatas().size() == 0 && loadingLayout == null)
            mode = RefreshMode.update;

        requestData(mode);
    }

    @Override
    public void requestDataOutofdate() {
        putLastReadPosition(0);
        putLastReadTop(0);

        requestDataSetRefreshing();
    }

    /**
     * 设置刷新控件为刷新状态且刷新数据
     */
    public void requestDataSetRefreshing() {
        // 如果没有正在刷新，设置刷新控件，且子类没有自动刷新
        if (!isRefreshing() && !setRefreshViewToLoading())
            requestData(RefreshMode.reset);
    }

    public void requestDataDelaySetRefreshing(long delay) {
        Runnable requestDelayRunnable = new Runnable() {

            @Override
            public void run() {
                Logger.d(TAG, "延迟刷新，开始刷新, " + toString());

                requestDataSetRefreshing();
            }

        };

        runUIRunnable(requestDelayRunnable, delay);
    }

    public abstract class APagingTask<Params, Progress, Result extends Serializable> extends ABaseTask<Params, Progress, Result> {

        final protected RefreshMode mode;

        public APagingTask(RefreshMode mode) {
            super(PAGING_TASK_ID);
            this.mode = mode;
            pagingTask = this;

            if (mode == RefreshMode.reset && mPaging != null)
                mPaging = newPaging();
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();

            Logger.d(TAG, toString() + "-" + ABaseTaskState.prepare + " - " + mode);
            onTaskStateChanged(ABaseTaskState.prepare, null, mode);
        }

        @Override
        public Result workInBackground(Params... params) throws TaskException {
            String previousPage = null;
            String nextPage = null;

            if (mPaging != null) {
                previousPage = mPaging.getPreviousPage();
                nextPage = mPaging.getNextPage();
            }

            return workInBackground(mode, previousPage, nextPage, params);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onSuccess(Result result) {
            if (result == null || getActivity() == null) {
                super.onSuccess(result);
                return;
            }

            bindAdapter(getAdapter());

            List<T> resultList;
            if (result instanceof List)
                resultList = (List<T>) result;
            else {
                resultList = parseResult(result);
                if (resultList == null)
                    resultList = new ArrayList<T>();
            }

            // 如果子类没有处理新获取的数据刷新UI，默认替换所有数据
            if (!handleResult(mode, resultList))
                if (mode == RefreshMode.reset)
                    setAdapterItems(new ArrayList<T>());

            // append数据
            if (mode == RefreshMode.reset || mode == RefreshMode.refresh)
                IPagingAdapter.Utils.addItemsAtFrontAndRefresh(getAdapter(), resultList);
            else if (mode == RefreshMode.update)
                IPagingAdapter.Utils.addItemsAndRefresh(getAdapter(), resultList);

            // 处理分页数据
            if (mPaging != null) {
                if (getAdapter() != null && getAdapter().getDatas().size() != 0)
                    mPaging.processData(result, (T) getAdapter().getDatas().get(0), (T) getAdapter().getDatas().get(getAdapter().getDatas().size() - 1));
                else
                    mPaging.processData(result, null, null);
            }

            if (mode == RefreshMode.reset)
                refreshConfig.pagingEnd = false;
            if (mode == RefreshMode.update || mode == RefreshMode.reset)
                refreshConfig.pagingEnd = resultList.size() == 0;

            // 如果是缓存数据，且已经过期
            if (result instanceof IResult) {
                // 这里增加一个自动刷新设置功能
                IResult iResult = (IResult) result;

                if (iResult.fromCache() && !iResult.outofdate())
                    toLastReadPosition();

                if (iResult.endPaging())
                    refreshConfig.pagingEnd = true;
            }

            if (mode == RefreshMode.reset && getTaskCount(getTaskId()) > 1)
                getAdapter().notifyDataSetChanged();

            setupRefreshViewWithConfig(refreshConfig);

            Logger.d(TAG, toString() + "-" + ABaseTaskState.success + " - " + mode);
            onTaskStateChanged(ABaseTaskState.success, null, mode);

            super.onSuccess(result);
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            Logger.d(TAG, toString() + "-" + ABaseTaskState.failed + " - " + mode + "-" + exception.getMessage());
            onTaskStateChanged(ABaseTaskState.failed, exception, mode);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            Logger.d(TAG, toString() + "-" + ABaseTaskState.canceled + " - " + mode);
            onTaskStateChanged(ABaseTaskState.canceled, null, mode);
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            Logger.d(TAG, toString() + "-" + ABaseTaskState.finished + " - " + mode);
            onTaskStateChanged(ABaseTaskState.finished, null, mode);

            pagingTask = null;
        }

        /**
         * 每次调用接口，获取新的数据时调用这个方法
         *
         * @param mode  当次拉取数据的类型
         * @param datas 当次拉取的数据
         * @return <tt>false</tt> 如果mode={@link RefreshMode#reset}
         * 默认清空adapter中的数据
         */
        protected boolean handleResult(RefreshMode mode, List<T> datas) {
            return false;
        }

        /**
         * 将Ts转换成List(T)
         *
         * @param result List(T)
         * @return
         */
        abstract protected List<T> parseResult(Result result);

        /**
         * 异步执行方法
         *
         * @param mode         刷新模式
         * @param previousPage 上一页页码
         * @param nextPage     下一页页码
         * @param params       task参数
         * @return
         * @throws TaskException
         */
        abstract protected Result workInBackground(RefreshMode mode, String previousPage, String nextPage, Params... params) throws TaskException;

    }
    /*********************************************结束数据刷新方法************************************************/

    /*********************************************** 开始FooterView************************************************/

    protected AHeaderItemViewCreator<T> configHeaderViewCreator() {
        return null;
    }

    protected IItemViewCreator<T> configFooterViewCreator() {
        return new IItemViewCreator<T>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(BasicFooterView.LAYOUT_RES, parent, false);
            }

            @Override
            public IItemView<T> newItemView(View convertView, int viewType) {
                return new BasicFooterView<>(getActivity(), convertView, APagingFragment.this);
            }

        };
    }

    abstract protected void addFooterViewToRefreshView(AFooterItemView<?> footerItemView);

    abstract protected void addHeaderViewToRefreshView(AHeaderItemViewCreator<?> headerItemViewCreator);

    @Override
    public void setFooterViewToRefreshing() {
        if (mFooterItemView != null) {
            mFooterItemView.setFooterViewToRefreshing();
        }
    }

    @Override
    public void onTaskStateChanged(AFooterItemView<?> footerItemView, ABaseTaskState state, TaskException exception, RefreshMode mode) {
        if (refreshConfig == null || !refreshConfig.footerMoreEnable || mFooterItemView == null)
            return;

        if (mFooterItemView != null) {
            mFooterItemView.onTaskStateChanged(footerItemView, state, exception, mode);
        }
    }

    private boolean refreshViewScrolling = false;// 正在滚动

    protected void onScrollStateChanged(int scrollState) {
        // 滑动的时候，不加载图片
        if (!refreshConfig.displayWhenScrolling) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                refreshViewScrolling = true;
            } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                refreshViewScrolling = true;
            } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                refreshViewScrolling = false;
            }
        }

        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && // 停止滚动
                !refreshConfig.pagingEnd && // 分页未加载完
                refreshConfig.footerMoreEnable && // 自动加载更多
                mFooterItemView != null // 配置了FooterView
                ) {
            int childCount = getRefreshView().getChildCount();
            if (childCount > 0 && getRefreshView().getChildAt(childCount - 1) == mFooterItemView.getConvertView()) {
                setFooterViewToRefreshing();
            }
        }

        // 保存最后浏览位置
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!TextUtils.isEmpty(refreshConfig.positionKey) && getRefreshView() != null) {
                putLastReadPosition(getFirstVisiblePosition());

                putLastReadTop(getRefreshView().getChildAt(0).getTop());
            }
        }
    }

    @Override
    public boolean canLoadMore() {
        return !refreshConfig.pagingEnd;
    }

    @Override
    public void onLoadMore() {
        onPullUpToRefresh();
    }

    protected void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public boolean canDisplay() {
        if (refreshConfig.displayWhenScrolling)
            return true;

        return !refreshViewScrolling;
    }

    /*********************************************结束FooterView************************************************/

    /*********************************************
     * 开始阅读位置历史
     ************************************************/
    protected void toLastReadPosition() {
        if (getRefreshView() == null || TextUtils.isEmpty(refreshConfig.positionKey))
            return;

        if (getRefreshView() instanceof ListView) {
            runUIRunnable(new Runnable() {

                @Override
                public void run() {
                    ListView listView = (ListView) getRefreshView();
                    listView.setSelectionFromTop(getLastReadPosition(), getLastReadTop() + listView.getPaddingTop());
                }
            });
        }
    }

    protected int getLastReadPosition() {
        return ActivityHelper.getIntShareData(GlobalContext.getInstance(), refreshConfig.positionKey + "Position", 0);
    }

    protected void putLastReadPosition(int position) {
        if (!TextUtils.isEmpty(refreshConfig.positionKey))
            ActivityHelper.putIntShareData(GlobalContext.getInstance(), refreshConfig.positionKey + "Position", position);
    }

    protected int getLastReadTop() {
        return ActivityHelper.getIntShareData(GlobalContext.getInstance(), refreshConfig.positionKey + "Top", 0);
    }

    protected void putLastReadTop(int top) {
        if (!TextUtils.isEmpty(refreshConfig.positionKey))
            ActivityHelper.putIntShareData(GlobalContext.getInstance(), refreshConfig.positionKey + "Top", top);
    }

    protected int getFirstVisiblePosition() {
        return 0;
    }

    /*********************************************
     * 结束阅读位置历史
     ************************************************/


    // 重构需要实现的方法
    public void refreshUI() {

    }

    public void releaseImageViewByIds() {

    }

}
