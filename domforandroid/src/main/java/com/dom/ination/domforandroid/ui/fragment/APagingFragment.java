package com.dom.ination.domforandroid.ui.fragment;

import android.view.ViewGroup;

import com.dom.ination.domforandroid.common.utils.Logger;
import com.dom.ination.domforandroid.network.biz.IResult;
import com.dom.ination.domforandroid.network.task.TaskException;
import com.dom.ination.domforandroid.support.paging.IPaging;
import com.dom.ination.domforandroid.ui.fragment.adpter.IPagingAdapter;
import com.dom.ination.domforandroid.ui.fragment.itemview.AFooterItemView;
import com.dom.ination.domforandroid.ui.fragment.itemview.AHeaderItemViewCreator;
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

            Logger.d(TAG, toString() + "-" + ABaseTaskState.falid + " - " + mode + "-" + exception.getMessage());
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
}
