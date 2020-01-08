package com.testndk.jnistudy.ui.weight.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class MapFlashCardEditTextView extends AppCompatEditText {
    MapFlashInputConnection mInputConnection;

    public MapFlashCardEditTextView(Context context) {
        this(context, null);
    }

    public MapFlashCardEditTextView(Context context, AttributeSet attrs) {
        //解决软键盘无法弹出问题
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MapFlashCardEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        // 禁止编辑框横屏时弹出另外一个编辑界面
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//        setRawInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
//        setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        setLongClickable(false);
        mInputConnection = new MapFlashInputConnection(null, true);
        mInputConnection.setEditTextView(this);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        mInputConnection.setTarget(super.onCreateInputConnection(outAttrs));
        return mInputConnection;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
