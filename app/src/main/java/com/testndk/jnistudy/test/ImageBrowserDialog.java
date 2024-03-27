//package com.testndk.jnistudy.test;
//
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.DialogFragment;
//import androidx.viewpager2.widget.ViewPager2;
//
//import com.testndk.jnistudy.R;
//import com.testndk.jnistudy.utils.ScreenUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
//
//public class ImageBrowserDialog extends DialogFragment {
//    ViewPager2 vpBrowser;
//    ImageBrowserAdapter mBrowserAdapter = new ImageBrowserAdapter();
//    private List<BasePreBrowserBean> list = new ArrayList<>();
//
//    public static ImageBrowserDialog newInstance() {
//
//        Bundle args = new Bundle();
//
//        ImageBrowserDialog fragment = new ImageBrowserDialog();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Window window = getDialog().getWindow();
//        if (window == null) {
//            return;
//        }
//        WindowManager.LayoutParams windowParams = window.getAttributes();
//        //这里设置透明度
//        windowParams.dimAmount = 0.2f;
//        window.setGravity(Gravity.CENTER);
//        windowParams.width = ScreenUtil.getScreenWidth();
//        windowParams.height = MATCH_PARENT;
//        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        window.setAttributes(windowParams);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View mRootView = inflater.inflate(R.layout.dialog_fragment_browser, container, false);
//        vpBrowser = mRootView.findViewById(R.id.vpBrowser);
//        return mRootView;
//    }
//
//    String[] thumb = {"https://cdn.diandi.club/image/159056144500020_i.png?x-oss-process=image/resize,m_lfit,h_360,w_360/format,jpeg",
//            "https://cdn.diandi.club/image/159056144500096_i.png?x-oss-process=image/resize,m_lfit,h_360,w_360/format,jpeg"};
//    String[] url = {"https://cdn.diandi.club/image/159056144500020_i.png", "https://cdn.diandi.club/image/159056144500096_i.png"};
//    String[] video = {""};
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        list.add(new BasePreBrowserBean() {
//            @Override
//            String getBrowserUrl() {
//                return thumb[0];
//            }
//
//            @Override
//            String getBrowserThumbUrl() {
//                return url[0];
//            }
//
//            @Override
//            public int getItemType() {
//                return BasePreBrowserBean.TYPE_IMG;
//            }
//        });
//        list.add(new BasePreBrowserBean() {
//            @Override
//            String getBrowserUrl() {
//                return thumb[1];
//            }
//
//            @Override
//            String getBrowserThumbUrl() {
//                return url[1];
//            }
//
//            @Override
//            public int getItemType() {
//                return BasePreBrowserBean.TYPE_IMG_GIF;
//            }
//        });
//        list.add(new BasePreBrowserBean() {
//            @Override
//            String getBrowserUrl() {
//                return video[0];
//            }
//
//            @Override
//            String getBrowserThumbUrl() {
//                return "";
//            }
//
//            @Override
//            public int getItemType() {
//                return BasePreBrowserBean.TYPE_IMG_VIDEO;
//            }
//        });
//        vpBrowser.setAdapter(mBrowserAdapter);
//        mBrowserAdapter.setNewData(list);
//    }
//}
