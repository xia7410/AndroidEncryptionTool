package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.List;

import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.ImageLoader;
import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Image;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Video;

public class MainActivity extends HandleSavedDataActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * VideoFragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public NavigationDrawerFragment mNavigationDrawerFragment;

    private final static String S_PASSWORD_FRAGMENT = PasswordFragment.class.getName();
    private final static String S_VIDEO_FRAGMENT = VideoFragment.class.getName();
    private final static String S_IMAGE_FRAGMENT = ImageFragment.class.getName();
    private final static String S_MODIFY_PASSWORD_FRAGMENT = ModifyPasswordFragment.class.getName();


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * 当前页面 0 密码 1视频 2图片 3修改密码
     */
    private int nowPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.e("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Logger.e("MainActivity", "onNavigationDrawerItemSelected：" + position);
        // update the main content by replacing fragments
        if (position != nowPosition) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            hideFragment(transaction, fragmentManager);
            nowPosition = position;
            onSectionAttached(nowPosition);
            Fragment temp = null;
            switch (position) {
                case 0: {
                    temp = fragmentManager.findFragmentByTag(S_PASSWORD_FRAGMENT);
                    if (temp == null) {
                        transaction.add(R.id.container, PasswordFragment.newInstance(), S_PASSWORD_FRAGMENT);
                    } else {
                        transaction.show(temp);
                    }
                    break;
                }
                case 1: {
                    temp = fragmentManager.findFragmentByTag(S_VIDEO_FRAGMENT);
                    if (temp == null) {
                        transaction.add(R.id.container, VideoFragment.newInstance(), S_VIDEO_FRAGMENT);
                    } else {
                        transaction.show(temp);
                    }
                    break;
                }
                case 2: {
                    temp = fragmentManager.findFragmentByTag(S_IMAGE_FRAGMENT);
                    if (temp == null) {
                        transaction.add(R.id.container, ImageFragment.newInstance(), S_IMAGE_FRAGMENT);
                    } else {
                        transaction.show(temp);
                    }
                    break;
                }
                case 3: {
                    temp = fragmentManager.findFragmentByTag(S_MODIFY_PASSWORD_FRAGMENT);
                    if (temp == null) {
                        transaction.add(R.id.container, ModifyPasswordFragment.newInstance(), S_MODIFY_PASSWORD_FRAGMENT);
                    } else {
                        transaction.show(temp);
                    }
                    break;
                }
            }
            transaction.commit();
        }
    }

    private void hideFragment(FragmentTransaction transaction, FragmentManager fragmentManager) {
        Fragment fragment = null;
        fragment = fragmentManager.findFragmentByTag(S_PASSWORD_FRAGMENT);
        if (fragment != null) {
            transaction.hide(fragment);
        }
        fragment = fragmentManager.findFragmentByTag(S_VIDEO_FRAGMENT);
        if (fragment != null) {
            transaction.hide(fragment);
        }
        fragment = fragmentManager.findFragmentByTag(S_IMAGE_FRAGMENT);
        if (fragment != null) {
            transaction.hide(fragment);
        }
        fragment = fragmentManager.findFragmentByTag(S_MODIFY_PASSWORD_FRAGMENT);
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }

    private void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
            case 3:
                mTitle = getString(R.string.modify_login_password);
        }

    }

    public void restoreActionBar() {
        Logger.e("es", "restoreActionBar:" + mTitle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logger.e("es", "MainActivity_onCreateOptionsMenu");
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
        return super.onCreateOptionsMenu(menu);
    }

    long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fragment;
                    if (nowPosition == 0) {
                        fragment = fragmentManager.findFragmentByTag(S_PASSWORD_FRAGMENT);
                        if (fragment != null) {
                            if (((PasswordFragment) fragment).onKeyBackDown()) {
                                return true;
                            }
                        }
                    } else if (nowPosition == 1) {
                        fragment = fragmentManager.findFragmentByTag(S_VIDEO_FRAGMENT);
                        if (fragment != null) {
                            if (((VideoFragment) fragment).onKeyBackDown()) {
                                return true;
                            }
                        }
                    } else if (nowPosition == 2) {
                        fragment = fragmentManager.findFragmentByTag(S_IMAGE_FRAGMENT);
                        if (fragment != null) {
                            if (((ImageFragment) fragment).onKeyBackDown()) {
                                return true;
                            }
                        }
                    }
                    long now = System.currentTimeMillis();
                    if (now - exitTime < 2000) {
                        StaticData.getInstance().stopMessage();
                        this.finish();
                    } else {
                        exitTime = now;
                        StaticData.getInstance().showMessage(
                                getString(R.string.PressAgainToEncodeAndToExitTheProgram), this);
                    }
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.e("MainActivity", "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Logger.e("MainActivity", "onDestory");
        ImageLoader.getInstance().setShouldStop();
        //加密视频
        List<Video> list = StaticData.getInstance().getAllVideoData();
        for (Video i : list) {
            try {
                if (i.hasDecrypted) {
                    if (FileTools.encrypt(new File(StaticData.fromVideoIdToSavePath(i.id)), StaticData.getInstance().getRealPassword())) {
                        i.hasDecrypted = false;
                    } else {
                        Logger.e("ERROR", "在退出程序的时候加密视频失败！" + i.path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //加密图片
        List<Image> list2 = StaticData.getInstance().getAllImageData();
        for (Image i : list2) {
            try {
                if (i.hasDecrypted) {
                    if (FileTools.encrypt(new File(StaticData.fromImageIdToSavePath(i.id)), StaticData.getInstance().getRealPassword())) {
                        i.hasDecrypted = false;
                    } else {
                        Logger.e("ERROR", "在退出程序的时候加密图片失败！" + i.path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Logger.e("MainActivity", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.e("MainActivity", "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Logger.e("MainActivity", "onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Logger.e("MainActivity", "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Logger.e("MainActivity", "onRestart");
        super.onRestart();
    }

    //    private void getOverflowMenu() {
//        try {
//            ViewConfiguration config = ViewConfiguration.get(this);
//            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
//            if (menuKeyField != null) {
//                menuKeyField.setAccessible(true);
//                menuKeyField.setBoolean(config, false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static final View.OnTouchListener TouchLight = new View.OnTouchListener() {
//
//        public final float[] BT_SELECTED = new float[]{1, 0, 0, 0, 50, 0, 1, 0, 0, 50, 0, 0, 1, 0, 50, 0, 0, 0, 1, 0};
//        public final float[] BT_NOT_SELECTED = new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0};
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                v.getBackground().setColorFilter(
//                        new ColorMatrixColorFilter(BT_SELECTED));
//                v.setBackgroundDrawable(v.getBackground());
//            } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                v.getBackground().setColorFilter(
//                        new ColorMatrixColorFilter(BT_NOT_SELECTED));
//                v.setBackgroundDrawable(v.getBackground());
//            }
//            return false;
//        }
//    };
//
//    public static final View.OnTouchListener TouchDark = new View.OnTouchListener() {
//
//        public final float[] BT_SELECTED = new float[]{1, 0, 0, 0, -50, 0, 1, 0, 0, -50, 0, 0, 1, 0, -50, 0, 0, 0, 1, 0};
//        public final float[] BT_NOT_SELECTED = new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0};
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                v.getBackground().setColorFilter(
//                        new ColorMatrixColorFilter(BT_SELECTED));
//                v.setBackgroundDrawable(v.getBackground());
//            } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                v.getBackground().setColorFilter(
//                        new ColorMatrixColorFilter(BT_NOT_SELECTED));
//                v.setBackgroundDrawable(v.getBackground());
//            }
//            return false;
//        }
//    };
}
