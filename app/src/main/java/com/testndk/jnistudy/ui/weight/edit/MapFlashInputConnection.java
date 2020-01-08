package com.testndk.jnistudy.ui.weight.edit;

import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import com.testndk.jnistudy.ui.utils.ScreenUtil;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapFlashInputConnection extends InputConnectionWrapper {
    private CopyOnWriteArrayList<TextBean> beans = new CopyOnWriteArrayList<>();
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

    public MapFlashInputConnection(InputConnection target, boolean mutable) {
        super(target, mutable);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        return super.commitCompletion(text);
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        //获取的是游标前面所有的数据(游标返回\n)
        try {
            CharSequence textBeforeCursor = getTextBeforeCursor(Integer.MAX_VALUE / 2, 0);
            String data = textBeforeCursor.toString().replace("\n", "");
            if (data.length() > MAX_LENGTH && !TextUtils.isEmpty(text)) {
                return true;
            }
            //删除操作
            if (TextUtils.isEmpty(text.toString()) && newCursorPosition == -1) {
                if (beans.isEmpty()) {
                    return true;
                } else {
                    //目标将要操作的bean
                    TextBean targetOptionBean = getCurrentCursorPosition(textBeforeCursor.toString());
                    //目标操作bean,在列表中的位置
                    int inDataPosition = beans.indexOf(targetOptionBean);
                    //获取游标前面数据(当前已经是该行的数据)
                    CharSequence frontStr = targetOptionBean.getContent().subSequence(0, targetOptionBean.getCursorPosition());
                    //获取游标后面数据(当前已经是该行的数据)
                    CharSequence backStr = targetOptionBean.getContent().subSequence(targetOptionBean.getCursorPosition(), targetOptionBean.getContent().length());
                    //获取完光标位置前后数据后,清空edittext内容
                    deleteSurroundingText(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
                    //判断游标位置是否是最开始的位置
                    if (frontStr.length() == 0) {
                        //如果游标位置是最开始的位置,删除上一行,首先需要判断上一行是否为空
                        if (inDataPosition > 0) {
                            //判断本行是否还有数据
                            if (TextUtils.isEmpty(backStr)) {
                                //如果没有数据,移除本行
                                beans.remove(inDataPosition);
                            }
                            //上一行不为空
                            inDataPosition--;
                            TextBean textBean = beans.get(inDataPosition);
                            CharSequence content = textBean.getContent();
                            textBean.setNewLineSelf(false);
                            textBean.setNewLine(false);
                            textBean.setContent(content.subSequence(0, content.length()));
                        } else {
                            //上一行为空拦截删除
                            return true;
                        }
                    } else {
                        //如果游标位置不在最开始位置
                        //首先判断游标前面是否
                        frontStr = frontStr.subSequence(0, frontStr.length() - 1);
                        //删除一个后判断前面是否还有数据
                        if (TextUtils.isEmpty(frontStr)) {
                            //如果没有,判断backStr是否还有数据
                            if (TextUtils.isEmpty(backStr)) {
                                //如果后面也没有数据
                                //那么就删除该行数据
                                beans.remove(inDataPosition);
                                //此时该行后面没有数据,但是后面的下一行可能有数据,又因为本行已经删除,数据自动往前移动一位
                                //首先判断beans获取当前数据是否还能往前移动
                                if (beans.size() > inDataPosition) {
                                    //如果能,说明删除行后面一行还有数据
                                    //不需要删除前一行的空格
                                } else {
                                    //并且去除前一行的空格
                                    inDataPosition--;
                                    if (inDataPosition >= 0) {
                                        TextBean textBean = beans.get(inDataPosition);
                                        textBean.setNewLine(false);
                                        textBean.setNewLineSelf(false);
                                        textBean.setDeleteLineTemp(true);
                                    }
                                }
                            } else {
                                //如果该行还有数据,设置剩余数据
                                targetOptionBean.setContent(backStr);
                                beans.set(inDataPosition, targetOptionBean);
                            }
                        } else {
                            //如果该行还有数据,那么就设置数据为 删除后的frontStr + backStr
                            targetOptionBean.setContent(frontStr.toString() + backStr);
                        }
                    }
                    boolean b = super.commitText(getAllData(), 0);
//                if (!TextUtils.isEmpty(textBeforeCursor)) {
//                    if (textBeforeCursor.length() != 1 && (textBeforeCursor.length() - getSelfNewLineAndTextCount()) % LINE_CURSOR == 1) {
//                        editTextView.setSelection(textBeforeCursor.length() - 2);
//                    } else {
//                        editTextView.setSelection(textBeforeCursor.length() - 1);
//                    }
//                }
                    return b;
                }
            } else if (text.equals("\n") && newCursorPosition == -1) {
                //当数据为空,不让换行
                if (beans.isEmpty()) {
                    return true;
                } else {
                    //目标将要操作的bean
                    TextBean targetOptionBean = getCurrentCursorPosition(textBeforeCursor.toString());
                    //目标操作bean,在列表中的位置
                    int inDataPosition = beans.indexOf(targetOptionBean);
                    //获取游标前面数据(当前已经是该行的数据)
                    CharSequence frontStr = targetOptionBean.getContent().subSequence(0, targetOptionBean.getCursorPosition());
                    //获取游标后面数据(当前已经是该行的数据)
                    CharSequence backStr = targetOptionBean.getContent().subSequence(targetOptionBean.getCursorPosition(), targetOptionBean.getContent().length());
                    //获取完光标位置前后数据后,清空edittext内容
                    deleteSurroundingText(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
                    if (TextUtils.isEmpty(frontStr)) {
                        TextBean textBean = new TextBean();
                        textBean.setContent("");
                        textBean.setNewLine(true);
                        textBean.setNewLineSelf(true);
                        beans.add(inDataPosition, textBean);
                    } else {
                        TextBean frontTextBean = new TextBean();
                        frontTextBean.setContent(frontStr);
                        frontTextBean.setNewLine(true);
                        frontTextBean.setNewLineSelf(true);
                        beans.set(inDataPosition, frontTextBean);
                        inDataPosition++;
                        targetOptionBean.setContent(backStr);
                        beans.add(inDataPosition, targetOptionBean);
                    }
                    boolean b = super.commitText(getAllData(), 0);
                    if (!TextUtils.isEmpty(textBeforeCursor)) {
                        editTextView.setSelection(textBeforeCursor.length() + 1);
                    }
                    return b;
                }
            } else {
                //当没有内容
                if (beans.isEmpty()) {
                    addNewLine(0, text);
                } else {
                    //目标将要操作的bean
                    TextBean targetOptionBean = getCurrentCursorPosition(textBeforeCursor.toString());
                    //目标操作bean,在列表中的位置
                    int inDataPosition = beans.indexOf(targetOptionBean);

                    //获取游标前面数据(当前已经是该行的数据)
                    CharSequence frontStr = targetOptionBean.getContent().subSequence(0, targetOptionBean.getCursorPosition());
                    //获取游标后面数据(当前已经是该行的数据)
                    CharSequence backStr = targetOptionBean.getContent().subSequence(targetOptionBean.getCursorPosition(), targetOptionBean.getContent().length());
                    //获取完光标位置前后数据后,清空edittext内容
                    deleteSurroundingText(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
                    //首先判断target长度是否添加后是否会超过11个,超过需要换行
                    if (targetOptionBean.getContent().length() < LINE_MAX_LENGTH) {
                        //判断光标位置添加多少个字将要换行
                        int needCountToNewLine = LINE_MAX_LENGTH - frontStr.length();
                        //后面还剩余的全部文字
                        String lastTotalText = text.toString() + backStr;
                        //如果剩余文字长度 > 换行缺少文字
                        if (lastTotalText.length() > needCountToNewLine) {
                            //首先判断
                            Matcher matcher = p.matcher(text);
                            boolean isMatch = matcher.find();
                            if (!isMatch) {
                                //从剩余文字中截取需要文字
                                String targetAddText = lastTotalText.substring(0, needCountToNewLine);
                                //剩余文字中剩余暂未添加的文字
                                String surplusText = lastTotalText.substring(needCountToNewLine, lastTotalText.length());
                                //设置给原数据
                                targetOptionBean.setContent(frontStr.toString() + targetAddText);
                                //并且设置换行符
                                targetOptionBean.setNewLine(true);
                                //重新塞入原始位置
                                beans.set(inDataPosition, targetOptionBean);
                                //将剩余文字进行添加
                                inDataPosition++;
                                addNewLine(inDataPosition, surplusText);
                            } else {
                                //并且设置换行符
                                targetOptionBean.setNewLine(true);
                                //重新塞入原始位置
                                beans.set(inDataPosition, targetOptionBean);
                                //将剩余文字进行添加
                                inDataPosition++;
                                addNewLine(inDataPosition, text);
                            }
                            //如果剩余文字刚好等于换行文字
                        } else if (lastTotalText.length() == needCountToNewLine) {
                            //设置给原数据
                            targetOptionBean.setContent(frontStr.toString() + lastTotalText);
                            //并且设置换行符
                            targetOptionBean.setNewLine(true);
                            //重新塞入原始位置
                            beans.set(inDataPosition, targetOptionBean);
                        } else {
                            //设置给原数据
                            targetOptionBean.setContent(frontStr.toString() + lastTotalText);
//                        //判断是否是主动换行,如果是主动换行不判断是否达到换行条件
//                        if (!targetOptionBean.isNewLineSelf()) {
//                            //并且设置改行未达到换行条件不进行换行
//                            targetOptionBean.setNewLine(false);
//                        }
                            //重新塞入原始位置
                            beans.set(inDataPosition, targetOptionBean);
                        }
                    } else if (targetOptionBean.getContent().length() == LINE_MAX_LENGTH) {
                        //判断光标位置添加多少个字将要换行
                        int needCountToNewLine = LINE_MAX_LENGTH - frontStr.length();
                        //后面还剩余的全部文字
                        String lastTotalText = text.toString() + backStr;
                        //其实此时不用判断多少个字换行,此刻必定换行,永远为true
                        if (lastTotalText.length() > needCountToNewLine) {
                            //从剩余文字中截取需要文字
                            String targetAddText = lastTotalText.substring(0, needCountToNewLine);
                            //剩余文字中剩余暂未添加的文字
                            String surplusText = lastTotalText.substring(needCountToNewLine);
                            //设置给原数据
                            targetOptionBean.setContent(frontStr.toString() + targetAddText);
                            //并且设置换行符
                            targetOptionBean.setNewLine(true);
                            //重新塞入原始位置
                            beans.set(inDataPosition, targetOptionBean);
                            //将剩余文字进行添加
                            inDataPosition++;
                            addToNextLine(inDataPosition, surplusText);
                        }
                    }
                }
                SpannableString allData = getAllData();
                boolean isCommit = super.commitText(allData, newCursorPosition);
                if (newCursorPosition != -1) {
                    if (((textBeforeCursor.length() - getSelfNewLineAndTextCount()) % LINE_CURSOR) + text.length() > LINE_MAX_LENGTH) {
                        try {
                            editTextView.setSelection(textBeforeCursor.length() + text.length() + getAddNewLineCount(0, ((textBeforeCursor.length() - getSelfNewLineAndTextCount()) % LINE_CURSOR) + text.length()));
                        } catch (Exception e) {
                        }
                    } else {
                        try {
                            editTextView.setSelection(textBeforeCursor.length() + text.length());
                        } catch (Exception e) {
                            editTextView.setSelection(textBeforeCursor.length());
                        }
                    }
                }
                return isCommit;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            getAllData();
            beans.clear();
            deleteSurroundingText(Integer.MAX_VALUE / 2, 0);
            return super.commitText("", newCursorPosition);
        }
    }

    /**
     * 添加文本时,因为换行符光标所需后移个数
     *
     * @return
     */
    public int getAddNewLineCount(int count, int textLength) {
        int length = textLength - LINE_MAX_LENGTH;
        if (length > 0) {
            count++;
            return getAddNewLineCount(count, length);
        } else {
            return count;
        }
    }

    private int getSelfNewLineAndTextCount() {
        int i = 0;
        for (TextBean bean : beans) {
            if (bean.isNewLineSelf() || bean.isDeleteLineTemp()) {
                i++;
                i += bean.content.length();
            }
        }
        return i;
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

    private void addNewLine(int inDataPosition, CharSequence text) {
        if (text.length() > LINE_MAX_LENGTH) {
            Object[] objects = doSubStr(inDataPosition, text);
            TextBean textBean = new TextBean();
            String str = objects[1].toString();
            textBean.setContent(str);
            beans.add((Integer) objects[0], textBean);
        } else {
            TextBean textBean = new TextBean();
            textBean.setContent(text);
            beans.add(textBean);
        }
    }

    public void setEditTextView(MapFlashCardEditTextView editTextView) {
        this.editTextView = editTextView;
    }

    /**
     * 数据截断处理
     *
     * @param inDataPosition
     * @param text
     * @return
     */
    private Object[] doSubStr(int inDataPosition, CharSequence text) {
        Object[] objects = new Object[2];
        if (text.length() >= LINE_MAX_LENGTH) {
            CharSequence sequence = text.subSequence(0, LINE_MAX_LENGTH);
            MapFlashInputConnection.TextBean textBean = new MapFlashInputConnection.TextBean();
            textBean.setContent(sequence);
            textBean.setNewLine(true);
            beans.add(inDataPosition, textBean);
            inDataPosition++;
            return doSubStr(inDataPosition, text.subSequence(LINE_MAX_LENGTH, text.length()));
        } else {
            objects[0] = inDataPosition;
            objects[1] = text;
            return objects;
        }
    }

    public static int calculateStrLength(String etstring) {
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

    /**
     * 获取当前游标位置
     *
     * @return
     */
    private MapFlashInputConnection.TextBean getCurrentCursorPosition(String dataStr) {
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

    /**
     * 获取输入spannablestring
     *
     * @return
     */

    Paint paint = new Paint();

    public int getFontHeight(float fontSize) {
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    private int toCalculateFontWidth(int length) {
        return ScreenUtil.sp2px(218 * 100f / length);
    }

    private SpannableString getAllData() {
        StringBuilder data = new StringBuilder();
        int totalHeight = 0;
        for (TextBean value : beans) {
            totalHeight += getFontHeight(toCalculateFontWidth(value.getCalculateLength()));
            if (value.isNewLine) {
//                CharSequence charSequence = value.getContent().subSequence(value.getContent().length() - 1, value.getContent().length());
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


    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            commitText("", -1);
            return true;
        } else if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            commitText("\n", -1);
            finishComposingText();
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            finishComposingText();
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            return true;
        }
        return super.sendKeyEvent(event);
    }


    @Override
    public boolean setComposingRegion(int start, int end) {
        return true;
    }


    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return true;
    }

    @Override
    public boolean finishComposingText() {
        return true;
    }

    public static class TextBean {
        private CharSequence content;
        private boolean isNewLine;
        private boolean isNewLineSelf;
        private int cursorPosition;
        private boolean isDeleteLineTemp;

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
            if (TextUtils.isEmpty(content)) {
                this.content = "";
            } else {
                this.content = content;
            }
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
    }
}
