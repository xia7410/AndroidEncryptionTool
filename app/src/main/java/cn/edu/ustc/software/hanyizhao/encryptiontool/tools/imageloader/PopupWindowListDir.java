package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.edu.ustc.software.hanyizhao.encryptiontool.R;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.DisplayUtil;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.FolderBean;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by HanYizhao on 2015/12/11.
 */
public class PopupWindowListDir extends PopupWindow {
    int mWidth;
    int mHeight;
    private View mConvertView;
    private Context mContext;
    List<FolderBean> folders;
    private ListView mListView;
    private DirAdapter mDirAdapter;
    private WeakReference<OnPopupWindowListDirSelectListener> listener;
    private int nowPosition = 0;

    public PopupWindowListDir(Context context, List<FolderBean> folders) {
        mContext = context;
        this.folders = folders;
        getWidthAndHeight(context);
        mConvertView = LayoutInflater.from(context).inflate(R.layout.popup_window_image_loader, null);
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());


        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        initViews();
        initEvent();
    }

    private void initViews() {
        mListView = (ListView) mConvertView.findViewById(R.id.select_image_pop_up_listView);
        mDirAdapter = new DirAdapter(mContext, folders);
        mListView.setAdapter(mDirAdapter);
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                nowPosition = position;
                mDirAdapter.notifyDataSetChanged();
                OnPopupWindowListDirSelectListener l;
                if (listener != null && (l = listener.get()) != null) {
                    l.OnSelected(position);
                }
            }
        });
    }

    public void setOnPopupWindowListDirSelectListener(OnPopupWindowListDirSelectListener listener) {
        this.listener = new WeakReference<OnPopupWindowListDirSelectListener>(listener);
    }

    public interface OnPopupWindowListDirSelectListener {
        void OnSelected(int position);
    }

    private void getWidthAndHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        mWidth = metrics.widthPixels;
        mHeight = (int) (metrics.heightPixels * 0.73);
    }

    private class DirAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        List<FolderBean> mFolders;

        public DirAdapter(Context context, List<FolderBean> folders) {
            mInflater = LayoutInflater.from(context);
            mFolders = folders;
        }

        @Override
        public int getCount() {
            if (folders.get(0).files.size() > 0) {
                return folders.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return folders.get(position).folderName;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_view_image_loader, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.select_image_pop_up_imageView);
                viewHolder.imageViewIndicator = (ImageView) convertView.findViewById(R.id.select_image_pop_up_indicator);
                viewHolder.textViewDirName = (TextView) convertView.findViewById(R.id.select_image_pop_up_dir_name);
                viewHolder.textViewDirCount = (TextView) convertView.findViewById(R.id.select_image_pop_up_dir_count);
                viewHolder.imageViewStart = (ImageView) convertView.findViewById(R.id.select_image_pop_up_start);
                viewHolder.view = convertView.findViewById(R.id.temp);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.view.getLayoutParams().height = DisplayUtil.dip2px(mContext, 10);
            } else {
                viewHolder.view.getLayoutParams().height = 0;
            }
            if (position == 1 && !mFolders.get(1).briefMode) {
                viewHolder.imageViewStart.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imageViewStart.setVisibility(View.INVISIBLE);
            }
            String path;
            FolderBean bean = mFolders.get(position);
            if (bean.briefMode) {
                path = bean.folder.getAbsolutePath() + "/" + bean.files.get(0).path;
            } else {
                path = bean.files.get(0).path;
            }
            if (nowPosition != position) {
                viewHolder.imageViewIndicator.setImageBitmap(null);
            } else {
                viewHolder.imageViewIndicator.setImageResource(R.drawable.indicator);
            }
            ImageLoader.getInstance().loadImage(path, viewHolder.imageView, null, bean.files.get(0).type);
            viewHolder.textViewDirCount.setText(bean.files.size() + "张");
            viewHolder.textViewDirName.setText(bean.folderName);
            return convertView;
        }

        private class ViewHolder {
            public ImageView imageView;
            public TextView textViewDirName;
            public TextView textViewDirCount;
            public ImageView imageViewIndicator;
            public View view;
            public ImageView imageViewStart;
        }
    }
}
