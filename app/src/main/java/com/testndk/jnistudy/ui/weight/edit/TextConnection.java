package com.testndk.jnistudy.ui.weight.edit;

import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

import com.testndk.jnistudy.utils.ScreenUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextConnection extends BaseInputConnection {
    private static CopyOnWriteArrayList<TextBean> beans = new CopyOnWriteArrayList<>();

    private MapFlashCardEditTextView editTextView;
    //多少个字进行换行
    public static int LINE_MAX_LENGTH = 11;
    //换行时,光标辅助值(不要改动,否则后果严重)
    public int LINE_CURSOR = LINE_MAX_LENGTH + 1;
    //限制文字输入个数
    public static int MAX_LENGTH = 50;

    //匹配是否为全英文
    public String matchRule = "^[A-Za-z]+$";
    //正则匹配
    Pattern p = Pattern.compile(matchRule);

    public TextConnection(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
        if (targetView instanceof MapFlashCardEditTextView) {
            editTextView = (MapFlashCardEditTextView) targetView;
        }
    }

    public void log(Object... s) {
        StringBuilder builder = new StringBuilder();
        for (Object o : s) {
            builder.append(o).append("   ");
        }
        Log.e("testMap: ", builder.toString());
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        int selectionStart = editTextView.getSelectionStart();
        log("selectionStart", selectionStart);
        log("text", text);
        if (TextUtils.isEmpty(text) && newCursorPosition == -1) {
            log("del", "删除操作");
            //根据游标位置获取操作bean
            TextBean targetBean = toGetOptionBean(selectionStart);
            if (targetBean == null) {
                return true;
            }
            //当前游标位置
            int cursorPosition = targetBean.getCursorPosition();
            log("del  cursorPosition", cursorPosition);
            //前面的字符
            CharSequence frontText;
            if (cursorPosition > 0) {
                frontText = targetBean.getContent().subSequence(0, cursorPosition - 1);
            } else {
                frontText = targetBean.getContent().subSequence(0, 0);
            }
            //后面的字符
            CharSequence backText = targetBean.getContent().subSequence(cursorPosition, targetBean.content.length());
            if (TextUtils.isEmpty(frontText) && TextUtils.isEmpty(backText)) {
                beans.remove(targetBean);
            } else if (cursorPosition == 0) {
                int currentPosition = beans.indexOf(targetBean);
                if (currentPosition > 1) {
                    currentPosition--;
                    TextBean bean = beans.get(currentPosition);
                    bean.setContent(bean.getContent().subSequence(1, bean.getContent().length()));
                }
            } else {
                targetBean.setContent(frontText + backText.toString());
            }
            SpannableString allData = getAllData();
            editTextView.setText(allData);
            if (selectionStart > 0) {
                editTextView.setSelection(selectionStart - 1);
            }
        } else {
            doAddText(text, selectionStart);
        }
        return true;
    }

    /**
     * 添加操作
     *
     * @param text
     * @param selectionStart
     */
    private void doAddText(CharSequence text, int selectionStart) {
        if (isEmpty(beans)) {
            addNewLine(0, text);
            SpannableString allData = getAllData();
            editTextView.setText(allData);
            editTextView.setSelection(allData.length());
        } else {
            //根据游标位置获取操作bean
            TextBean targetBean = toGetOptionBean(selectionStart);
            //当前游标位置
            int cursorPosition = targetBean.getCursorPosition();
            int cursorAddTextPosition;
            if (targetBean.isNewLine()) {
                cursorAddTextPosition = cursorPosition + text.length() + 1;
            } else {
                cursorAddTextPosition = cursorPosition + text.length();
            }
            //如果游标位置+文字长度 大于 一行最大显示,那么就需要换行辅助
            int cursorOffsetLength = cursorAddTextPosition / LINE_MAX_LENGTH;
            if (cursorPosition == LINE_MAX_LENGTH) {
                cursorOffsetLength--;
            }
            //前面的字符
            CharSequence frontText = targetBean.getContent().subSequence(0, cursorPosition);
            //后面的字符
            CharSequence backText = targetBean.getContent().subSequence(cursorPosition, targetBean.content.length());
            //获取拼接后的文字
            CharSequence groupText = frontText + text.toString() + backText.toString();
            int targetPosition = beans.indexOf(targetBean);
            doAutoSub(targetPosition, groupText);
            SpannableString allData = getAllData();
            editTextView.setText(allData);
            editTextView.setSelection(selectionStart + text.length() + cursorOffsetLength);

        }
    }


    private void doAutoSub(int position, CharSequence data) {
        //首先判断该行是否存在
        if (beans.size() > position) {
            //存在
            TextConnection.TextBean bean = beans.get(position);
            //判断data长度是否超过一行最大长度
            if (data.length() >= LINE_MAX_LENGTH) {
                //超过,做截断处理,并且换行
                bean.setNewLine(true);
                bean.setContent(data.subSequence(0, LINE_MAX_LENGTH));
                //塞入本行后剩下的数据
                CharSequence lastStr = data.subSequence(LINE_MAX_LENGTH, data.length());
                //判断是否刚好为空
                if (lastStr.length() <= 0) {
                    return;
                }
                position++;
                if (beans.size() > position) {
                    doAutoInsert(position, lastStr);
                } else {
                    addNewLine(position, lastStr);
                }
            } else {
                //判断是否刚好为空
                if (TextUtils.isEmpty(data)) {
                    return;
                }
                //不超过,直接塞入进原疏忽,并且结束循环
                bean.setContent(data);
            }
        } else {
            addNewLine(position, data);
        }
    }

    /**
     * 自动向选中行的下一行插入数据
     *
     * @param position
     * @param lastStr
     */
    private void doAutoInsert(int position, CharSequence lastStr) {
        //获取本行
        TextConnection.TextBean bean = beans.get(position);
        //判断本行是否是手动换行
        if (bean.isNewLineSelf()) {
            //如果是手动换行,作为新行添加
            addNewLine(position, lastStr);
        } else {
            //如果不是手动换行,
            CharSequence groupText = lastStr + bean.getContent().toString();
            doAutoSub(position, groupText);
        }
    }

    private void addNewLine(int inDataPosition, CharSequence text) {
        if (text.length() >= LINE_MAX_LENGTH) {
            if (beans.size() > inDataPosition) {
                doSubStr(inDataPosition, text);
            } else {
                TextConnection.TextBean bean = new TextConnection.TextBean();
                CharSequence subText = text.subSequence(0, 11);
                CharSequence last = text.subSequence(11, text.length());
                bean.setContent(subText);
                bean.setNewLine(true);
                beans.add(bean);
                if (last.length() <= 0) {
                    return;
                }
                inDataPosition++;
                addNewLine(inDataPosition, last);
            }
        } else {
            TextConnection.TextBean textBean = new TextConnection.TextBean();
            textBean.setContent(text);
            beans.add(textBean);
        }
    }

    /**
     * 数据截断处理
     *
     * @param inDataPosition
     * @param text
     * @return
     */
    private void doSubStr(int inDataPosition, CharSequence text) {
        if (text.length() >= LINE_MAX_LENGTH) {
            CharSequence sequence = text.subSequence(0, LINE_MAX_LENGTH);
            TextConnection.TextBean textBean = new TextConnection.TextBean();
            textBean.setContent(sequence);
            textBean.setNewLine(true);
            beans.add(inDataPosition, textBean);
            inDataPosition++;
            doSubStr(inDataPosition, text.subSequence(LINE_MAX_LENGTH, text.length()));
        } else {
            addNewLine(inDataPosition, text);
        }
    }

    private boolean isEmpty(List<TextBean> data) {
        return (data == null || data.isEmpty());
    }

    private TextConnection.TextBean toGetOptionBean(int selection) {
        TextConnection.TextBean targetBean = null;
        boolean isZero = selection == 0;
        for (TextConnection.TextBean bean : beans) {
            targetBean = bean;
            if (isZero) {
                targetBean.setCursorPosition(0);
                break;
            }
            if (bean.isNewLine()) {
                selection -= (bean.getContent().length() + 1);
            } else {
                selection -= bean.getContent().length();
            }
            if (selection <= 0) {
                targetBean.setCursorPosition(bean.getContent().length() + selection);
                break;
            }
        }
        return targetBean;
    }

    /**
     * 获取当前游标位置
     *
     * @return
     */
    private TextBean getCurrentCursorPosition(String dataStr) {
        Pattern p = Pattern.compile("\n", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(dataStr);
        int count = 0;
        while (m.find()) {
            count++;
        }
        TextBean textBean = beans.get(count);
        int i = dataStr.lastIndexOf("\n");
        if (i == -1) {
            textBean.setCursorPosition(dataStr.length());
        } else {
            textBean.setCursorPosition(dataStr.substring(i + 1).length());
        }
        return textBean;
    }

    private void addToNextLine(int inDataPosition, CharSequence text) {
        //如果当前总数据的条目 大于 将要插入数据的条目.说明该位置数据不为空
        if (beans.size() > inDataPosition) {
            //获取该位置数据
            TextBean textBean = beans.get(inDataPosition);
            //首先判断是否是手动换行
            if (textBean.isNewLineSelf()) {
                //如果是手动换行,不往该行添加数据,直接作为新行添加
                addNewLine(inDataPosition, text);
            } else {
                //如果不是手动换行,判断该数据长度是否超过换行限制
                //首先判断text+已有数据的长度 是否会超过换行限制(注意此刻应该往该行前面添加,及text 在 textBean.getContent前面)
                if ((textBean.getContent().length() + text.length()) >= LINE_MAX_LENGTH) {
                    //如果超过,需要做截断处理
                    //获取全部数据,并且拼接
                    CharSequence data = text + textBean.getContent().toString();
                    //截断数据到换行限制
                    CharSequence newContent = data.subSequence(0, LINE_MAX_LENGTH);
                    //截断剩余数据
                    CharSequence surplusContext = data.subSequence(LINE_MAX_LENGTH, data.length());
                    //将最新数据赋值给原有行
                    textBean.setContent(newContent);
                    //并且设置换行
                    textBean.setNewLine(true);
                    //重新塞入到数据中
                    beans.set(inDataPosition, textBean);
                    inDataPosition++;
                    //其他数据做添加新行处理(还是添加到未满行数?)
                    addToNextLine(inDataPosition, surplusContext);
                } else {
                    //如果没有超过,直接往该行添加
                    CharSequence data = text + textBean.getContent().toString();
                    textBean.setContent(data);
                    //重新塞入到数据中
                    beans.set(inDataPosition, textBean);
                }
            }
        } else {
            //如果数据总行数小于插入位置,说明该位置数据为空,那么直接作为添加新行处理
            addNewLine(inDataPosition, text);
        }

    }

    private SpannableString getAllData() {
        StringBuilder data = new StringBuilder();
        int totalHeight = 0;
        for (TextBean value : beans) {
            totalHeight += getFontHeight(toCalculateFontWidth(value.getCalculateLength()));
            if (value.isNewLine) {
                data.append(value.content).append("\n");
            } else {
                data.append(value.content);
            }

        }
        float heightScale = 1f;
        float textMaxHeight = ScreenUtil.getScreenHeight() * 0.8f;
        if (totalHeight > textMaxHeight) {
            heightScale = totalHeight / textMaxHeight;
        }
        SpannableString contentData = new SpannableString(data);
        int start = 0;
        for (TextBean bean : beans) {
//            int i = 0;
            int i = (int) (toCalculateFontWidth(bean.getCalculateLength()) / heightScale);
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(i);
            if (bean.isNewLine()) {
                contentData.setSpan(absoluteSizeSpan, start, start + bean.getContent().length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += (bean.getContent().length() + 1);
            } else {
                contentData.setSpan(absoluteSizeSpan, start, start + bean.getContent().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += bean.getContent().length();
            }
        }
        return contentData;
    }

    private int toCalculateFontWidth(int length) {
        return ScreenUtil.sp2px(218 * 100f / length);
    }

    Paint paint = new Paint();

    public int getFontHeight(float fontSize) {
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        commitText("", -1);
        return false;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        log("event", event.getAction());
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean performEditorAction(int actionCode) {
        log("performEditorAction", actionCode);
        return super.performEditorAction(actionCode);
    }

    public static class TextBean {
        CharSequence content;
        boolean isNewLine;
        boolean isNewLineSelf;
        int cursorPosition;
        boolean isDeleteLineTemp;

        public boolean isDeleteLineTemp() {
            return isDeleteLineTemp;
        }

        public void setDeleteLineTemp(boolean deleteLineTemp) {
            isDeleteLineTemp = deleteLineTemp;
        }

        public int getCursorPosition() {
            return cursorPosition;
        }

        public boolean isNewLineSelf() {
            return isNewLineSelf;
        }

        public void setNewLineSelf(boolean newLineSelf) {
            isNewLineSelf = newLineSelf;
        }

        public void setCursorPosition(int cursorPosition) {
            this.cursorPosition = cursorPosition;
        }


        public CharSequence getContent() {
            return content;
        }

        public void setContent(CharSequence content) {
//            if (TextUtils.isEmpty(content)) {
//                this.content = "";
//            } else {
//                this.content = content;
//            }
            this.content = content;
        }


        public int getCalculateLength() {
            if (!TextUtils.isEmpty(content)) {
                int length = calculateStrLength(content.toString());
                return length == 0 ? 800 : length;
            } else {
                return 800;
            }
        }

        public boolean isNewLine() {
            return isNewLine;
        }

        public void setNewLine(boolean newLine) {
            isNewLine = newLine;
        }

        private int calculateStrLength(String etstring) {
            char[] ch = etstring.toCharArray();

            double varlength = 0;
            for (int i = 0; i < ch.length; i++) {
                // changed by zyf 0825 , bug 6918，加入中文标点范围 ， TODO 标点范围有待具体化
                if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb
                    //中文
                    varlength += 1;
                } else {
                    varlength += 0.53;
                }
            }
            return (int) (varlength * 100);
        }
    }
}
