package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.edu.ustc.software.hanyizhao.encryptiontool.R;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.DisplayUtil;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.FolderBean;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MediaPath;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.TaskType;

import java.lang.ref.WeakReference;
import java.util.Set;

public class GridViewAdapter extends BaseAdapter {

    private Set<MediaPath> mSelected;

    private FolderBean folderBean;
    private LayoutInflater inflater;
    private Context context;

    private WeakReference<OnSelectedListener> selectedListener;

    public void setOnSelectedListener(OnSelectedListener listener) {
        selectedListener = new WeakReference<OnSelectedListener>(listener);
    }

    public GridViewAdapter(Context context, FolderBean folderBean, Set<MediaPath> mSelected) {
        this.mSelected = mSelected;
        this.folderBean = folderBean;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        //计算宽高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        widthPixels = (displayMetrics.widthPixels - DisplayUtil.dip2px(context, 4)) / 3;
        buttonWidthPixels = widthPixels / 6;
        buttonPaddingPixels = buttonWidthPixels / 4;
    }

    @Override
    public int getCount() {
        return folderBean.files.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int widthPixels = 0;
    private int buttonWidthPixels = 0;
    private int buttonPaddingPixels = 0;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_view_image_loader, null);
            viewHolder = new ViewHolder();
            viewHolder.imageButton = (ImageButton) convertView.findViewById(R.id.select_image_image_button);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.select_image_grid_view_image);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.select_image_textView_duration);
            viewHolder.camera = (ImageView) convertView.findViewById(R.id.select_image_imageView_camera);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final MediaPath mediaPath = folderBean.files.get(position);
        final String path = folderBean.briefMode ? folderBean.folder.getAbsolutePath() + "/" + mediaPath.path : mediaPath.path;
        final MediaPath temp = new MediaPath(mediaPath.type, path, mediaPath.duration);
        final ViewHolder finalViewHolder = viewHolder;
        ImageView imageView = viewHolder.imageView;
        ImageButton imageButton = viewHolder.imageButton;
        TextView textView = viewHolder.textView;
        ImageView camera = viewHolder.camera;
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        if (layoutParams.width <= 0) {
            layoutParams.width = widthPixels;
            layoutParams.height = widthPixels;
            layoutParams = imageButton.getLayoutParams();
            layoutParams.width = buttonWidthPixels;
            layoutParams.height = buttonWidthPixels;
            imageButton.setPadding(0, buttonPaddingPixels, buttonPaddingPixels, 0);
        }
        if (mediaPath.type == TaskType.VIDEO) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(mediaPath.duration);
            camera.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            camera.setVisibility(View.INVISIBLE);
        }
        ImageLoader.getInstance().loadImage(path, imageView, R.drawable.no_photo, mediaPath.type);
        imageView.setOnClickListener(null);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected.contains(temp)) {
                    mSelected.remove(temp);
                    if (selectedListener != null) {
                        OnSelectedListener onSelectedListener = selectedListener.get();
                        if (onSelectedListener != null) {
                            onSelectedListener.onRemove(new MediaPath(mediaPath.type, path, mediaPath.duration));
                        }
                    }
                    finalViewHolder.imageView.setColorFilter(null);
                    finalViewHolder.imageButton.setImageResource(R.drawable.photo_no_selected);
                } else {
                    mSelected.add(temp);
                    if (selectedListener != null) {
                        OnSelectedListener onSelectedListener = selectedListener.get();
                        if (onSelectedListener != null) {
                            onSelectedListener.onSelected(new MediaPath(mediaPath.type, path, mediaPath.duration));
                        }
                    }
                    finalViewHolder.imageButton.setImageResource(R.drawable.photo_selected);
                    finalViewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
                }
            }
        });
        if (mSelected.contains(temp)) {
            finalViewHolder.imageButton.setImageResource(R.drawable.photo_selected);
            finalViewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
        } else {
            finalViewHolder.imageView.setColorFilter(null);
            finalViewHolder.imageButton.setImageResource(R.drawable.photo_no_selected);
        }
        return convertView;
    }

    private class ViewHolder {
        public ImageView imageView;
        public ImageButton imageButton;
        public TextView textView;
        public ImageView camera;
    }

    interface OnSelectedListener {
        void onSelected(MediaPath mediaPath);

        void onRemove(MediaPath mediaPath);
    }
}