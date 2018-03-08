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
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.DefaultImageThumbnailGetter;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.DefaultVideoThumbnailGetter;
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
    private List<FolderBean> folders;
    private ListView mListView;
    private DirAdapter mDirAdapter;
    private WeakReference<OnPopupWindowListDirSelectListener> listener;

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

    public void refreshFolderBean(List<FolderBean> folders) {
        this.folders = folders;
        mDirAdapter.notifyDataSetChanged();
    }

    private void initViews() {
        mListView = (ListView) mConvertView.findViewById(R.id.select_image_pop_up_listView);
        mDirAdapter = new DirAdapter(mContext);
        mListView.setAdapter(mDirAdapter);
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < folders.size(); i++) {
                    folders.get(i).isSelected = i == position;
                }
                mDirAdapter.notifyDataSetChanged();
                OnPopupWindowListDirSelectListener l;
                if (listener != null && (l = listener.get()) != null) {
                    l.OnSelected(folders.get(position));
                }
            }
        });
    }

    public void setOnPopupWindowListDirSelectListener(OnPopupWindowListDirSelectListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    public interface OnPopupWindowListDirSelectListener {
        void OnSelected(FolderBean selected);
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

        public DirAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return PopupWindowListDir.this.folders.size();
        }

        @Override
        public Object getItem(int position) {
            return PopupWindowListDir.this.folders.get(position).folderName;
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
            if (folders.get(position).isVideoFolder) {
                viewHolder.imageViewStart.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imageViewStart.setVisibility(View.INVISIBLE);
            }
            String path = null;
            FolderBean bean = folders.get(position);
            if (bean.files.size() > 0) {
                if (bean.briefMode) {
                    path = bean.folder.getAbsolutePath() + "/" + bean.files.get(0).path;
                } else {
                    path = bean.files.get(0).path;
                }
            }
            if (!PopupWindowListDir.this.folders.get(position).isSelected) {
                viewHolder.imageViewIndicator.setImageBitmap(null);
            } else {
                viewHolder.imageViewIndicator.setImageResource(R.drawable.indicator);
            }
            if (path != null) {
                ImageLoader.getInstance().loadImage(path, viewHolder.imageView, null,
                        bean.files.get(0).isVideo ? DefaultVideoThumbnailGetter.getInstance() : DefaultImageThumbnailGetter.getInstance());
            }
            viewHolder.textViewDirCount.setText(bean.files.size() + "张");
            viewHolder.textViewDirName.setText(bean.folderName);
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView textViewDirName;
            TextView textViewDirCount;
            ImageView imageViewIndicator;
            View view;
            ImageView imageViewStart;
        }
    }
}
