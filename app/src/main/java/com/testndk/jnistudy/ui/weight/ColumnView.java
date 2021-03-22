package com.testndk.jnistudy.ui.weight;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.utils.ScreenUtil;

public class ColumnView extends LinearLayout {
    Drawable leftDraw;

    int leftDrawVisible;
    int leftDrawSize;

    String leftText;
    float leftTextSize;
    int leftTextColor;
    int leftTextVisible;

    Drawable rightDraw;
    int rightDrawVisible;
    int rightDrawSize;
    String rightText;
    float rightTextSize;
    int rightTextColor;
    int rightTextVisible;
    int topPadding;
    int bottomPadding;
    int bottomLineVisibility;
    int bottomLineMargin;
    int bottomLineMarginSize;
    int bottomLineHeight;
    int bottomLineBG;
    private TextView mTvTitle;
    private ImageView mIvLeft;
    private TextView mTvRight;
    private ImageView mIvRight;
    private ConstraintLayout mClParent;
    private TextView mTvLine;

    public ColumnView(Context context) {
        this(context, null);
    }

    public ColumnView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColumnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColumnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (attrs != null) {
            float density = getResources().getDisplayMetrics().density;
            final Resources.Theme theme = context.getTheme();
            rightDraw = getResources().getDrawable(R.drawable.ic_right_arrow);
            TypedArray typedArray = theme.obtainStyledAttributes(attrs, R.styleable.ColumnView, defStyleAttr, defStyleRes);
            for (int i = 0; i < typedArray.getIndexCount(); i++) {
                int attr = typedArray.getIndex(i);
                switch (attr) {
                    case R.styleable.ColumnView_left_icon:
                        leftDraw = typedArray.getDrawable(attr);
                        break;
                    case R.styleable.ColumnView_left_icon_visibility:
                        leftDrawVisible = typedArray.getInt(attr, 0);
                        break;
                    case R.styleable.ColumnView_left_icon_size:
                        leftDrawSize = typedArray.getDimensionPixelOffset(attr, 0);
                        break;
                    case R.styleable.ColumnView_left_text:
                        leftText = typedArray.getString(attr);
                        break;
                    case R.styleable.ColumnView_left_text_color:
                        leftTextColor = typedArray.getColor(attr, 0);
                        break;
                    case R.styleable.ColumnView_left_text_size:
                        leftTextSize = typedArray.getDimensionPixelOffset(attr, 0) / density;
                        break;
                    case R.styleable.ColumnView_left_text_visibility:
                        leftTextVisible = typedArray.getInt(attr, 0);
                        break;
                    case R.styleable.ColumnView_right_icon:
                        rightDraw = typedArray.getDrawable(attr);
                        break;
                    case R.styleable.ColumnView_right_icon_size:
                        rightDrawSize = typedArray.getDimensionPixelSize(attr, 0);
                        break;
                    case R.styleable.ColumnView_right_icon_visibility:
                        rightDrawVisible = typedArray.getInt(attr, 0);
                        break;
                    case R.styleable.ColumnView_right_text:
                        rightText = typedArray.getString(attr);
                        break;
                    case R.styleable.ColumnView_right_text_color:
                        rightTextColor = typedArray.getColor(attr, 0);
                        break;
                    case R.styleable.ColumnView_right_text_size:
                        rightTextSize = typedArray.getDimensionPixelOffset(attr, 0) / density;
                        break;
                    case R.styleable.ColumnView_right_text_visibility:
                        rightTextVisible = typedArray.getInt(attr, 0);
                        break;
                    case R.styleable.ColumnView_top_padding:
                        topPadding = typedArray.getDimensionPixelOffset(attr, 0);
                        break;
                    case R.styleable.ColumnView_bottom_padding:
                        bottomPadding = typedArray.getDimensionPixelOffset(attr, 0);
                        break;
                    case R.styleable.ColumnView_bottom_line_bg:
                        bottomLineBG = typedArray.getColor(attr, Color.parseColor("#EAF0F3"));
                        break;
                    case R.styleable.ColumnView_bottom_line_height:
                        bottomLineHeight = typedArray.getDimensionPixelOffset(attr, 0);
                        break;
                    case R.styleable.ColumnView_bottom_line_margin:
                        bottomLineMargin = typedArray.getInt(attr, 0);
                        break;
                    case R.styleable.ColumnView_bottom_line_margin_size:
                        bottomLineMarginSize = typedArray.getDimensionPixelOffset(attr, 0);
                        break;
                    case R.styleable.ColumnView_bottom_line_visibility:
                        bottomLineVisibility = typedArray.getInt(attr, 0);
                        break;
                }
            }
        }
        initView(context);
    }

    private void initView(Context context) {
        View mView = View.inflate(context, R.layout.base_column_view, this);
        mTvTitle = mView.findViewById(R.id.tvTitle);
        mIvLeft = mView.findViewById(R.id.ivLeft);
        mTvRight = mView.findViewById(R.id.tvRight);
        mIvRight = mView.findViewById(R.id.ivRight);
        mClParent = mView.findViewById(R.id.clParent);
        mTvLine = mView.findViewById(R.id.tvLine);
        //设置左边文字
        if (mTvTitle != null) {
            mTvTitle.setText(leftText);
        }
        if (leftTextColor != 0) {
            mTvTitle.setTextColor(leftTextColor);
        }
        if (leftTextSize != 0) {
            mTvTitle.setTextSize(leftTextSize);
        }
        if (leftTextVisible == 1) {
            mTvTitle.setVisibility(VISIBLE);
        } else if (leftTextVisible == 2) {
            mTvTitle.setVisibility(GONE);
        }
        //设置左边图片
        if (leftDrawSize != 0) {
            ViewGroup.LayoutParams layoutParams = mIvLeft.getLayoutParams();
            layoutParams.height = leftDrawSize;
            layoutParams.width = leftDrawSize;
            mIvLeft.setLayoutParams(layoutParams);
        }
        if (null == leftDraw) {
            if (mIvLeft!=null) {
                mIvLeft.setVisibility(GONE);
            }
        } else {
            mIvLeft.setImageDrawable(leftDraw);
        }
        if (leftDrawVisible == 1) {
            mIvLeft.setVisibility(VISIBLE);
        } else if (leftDrawVisible == 2) {
            mIvLeft.setVisibility(GONE);
        }
        //设置右边文字
        mTvRight.setText(rightText);
        if (rightTextColor != 0) {
            mTvRight.setTextColor(rightTextColor);
        }
        if (rightTextSize != 0) {
            mTvRight.setTextSize(rightTextSize);
        }
        if (rightTextVisible == 1) {
            mTvRight.setVisibility(VISIBLE);
        } else if (rightTextVisible == 2) {
            mTvRight.setVisibility(GONE);
        }
        //设置右边图片
        if (rightDrawSize != 0) {
            ViewGroup.LayoutParams layoutParams = mIvRight.getLayoutParams();
            layoutParams.height = rightDrawSize;
            layoutParams.width = rightDrawSize;
            mIvRight.setLayoutParams(layoutParams);
        }
        if (rightDraw == null) {
            mIvRight.setVisibility(GONE);
        } else {
            mIvRight.setImageDrawable(rightDraw);
        }

        if (rightDrawVisible == 1) {
            mIvRight.setVisibility(VISIBLE);
        } else if (rightDrawVisible == 2) {
            mIvRight.setVisibility(GONE);
        }
        //设置父布局margin
        LayoutParams layoutParams = (LayoutParams) mClParent.getLayoutParams();
        if (topPadding != 0 && bottomPadding != 0) {
            layoutParams.topMargin = topPadding;
            layoutParams.bottomMargin = bottomPadding;
        } else if (topPadding != 0) {
            layoutParams.topMargin = topPadding;
        } else if (bottomPadding != 0) {
            layoutParams.bottomMargin = bottomPadding;
        }
        mClParent.setLayoutParams(layoutParams);
        //设置底部线条

        if (bottomLineMarginSize != 0) {
            LayoutParams lineParam = (LayoutParams) mTvLine.getLayoutParams();
            if (bottomLineMargin == 0 || bottomLineMargin == 3) {
                lineParam.leftMargin = bottomLineMarginSize;
                lineParam.rightMargin = bottomLineMarginSize;
            } else if (bottomLineMargin == 1) {
                lineParam.leftMargin = bottomLineMarginSize;
            } else if (bottomLineMargin == 2) {
                lineParam.rightMargin = bottomLineMarginSize;
            }
            mTvLine.setLayoutParams(lineParam);
        }

        if (bottomLineVisibility == 1) {
            mTvLine.setVisibility(VISIBLE);
        } else if (bottomLineVisibility == 2) {
            mTvLine.setVisibility(GONE);
        }

        if (bottomLineHeight != 0) {
            LayoutParams lineParam = (LayoutParams) mTvLine.getLayoutParams();
            lineParam.height = bottomLineHeight;
            mTvLine.setLayoutParams(lineParam);
        }
    }

    public ImageView getLeftImageView() {
        return mIvLeft;
    }

    public ColumnView setLeftText(CharSequence string) {
        mTvTitle.setText(string);
        return this;
    }

    public ColumnView setRightText(CharSequence string) {
        mTvRight.setText(string);
        return this;
    }

    public ColumnView setRightTextVisible(boolean visible) {
        if (true) {
            mTvRight.setVisibility(VISIBLE);
        } else {
            mTvRight.setVisibility(GONE);
        }
        return this;
    }

    public ColumnView setRightTextClick(OnClickListener textClick) {
        mTvRight.setOnClickListener(textClick);
        return this;
    }

    public TextView getRightTextView() {
        return mTvRight;
    }

    public ColumnView setRightTextBackground(@DrawableRes int drawable) {
        mTvRight.setBackground(getResources().getDrawable(drawable));
        return this;
    }

    public ColumnView setRightPadding(int left, int top, int right, int bottom) {
        mTvRight.setPadding(ScreenUtil.dp2px(left), ScreenUtil.dp2px(top), ScreenUtil.dp2px(right), ScreenUtil.dp2px(bottom));
        return this;
    }

    public ColumnView setRightTextColor(@ColorRes int color) {
        mTvRight.setTextColor(getResources().getColor(color));
        return this;
    }

    public ColumnView setRightDrawVisible(boolean need) {
        if (need) {
            mIvRight.setVisibility(VISIBLE);
        } else {
            mIvRight.setVisibility(GONE);
        }
        return this;
    }

    public void setRightDraw(Drawable draw) {
        rightDraw = draw;
        mIvRight.setImageDrawable(draw);
    }
}
