//package test;
//
//import android.text.TextUtils;
//
//import com.testndk.jnistudy.ui.weight.edit.TextConnection;
//
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TestClass {
//    static List<TextConnection.TextBean> beans = new ArrayList<>();
//    public static int LINE_MAX_LENGTH = 11;
//
//    @Test
//    public void main() {
//        TextConnection.TextBean bean = new TextConnection.TextBean();
//        bean.setContent("嗯嗯嗯嗯嗯嗯嗯嗯嗯嗯嗯");
//        bean.setNewLine(true);
//        beans.add(bean);
////        TextConnection.TextBean bean1 = new TextConnection.TextBean();
////        bean1.setContent("嗯嗯嗯嗯嗯嗯嗯嗯嗯嗯嗯");
////        bean1.setNewLine(true);
////        beans.add(bean1);
//        TextConnection.TextBean targetBean = toGetOptionBean(12);
//
//        int cursorPosition = targetBean.getCursorPosition();
//        //前面的字符
//        CharSequence frontText = targetBean.getContent().subSequence(0, cursorPosition);
//        //后面的字符
//        CharSequence backText = targetBean.getContent().subSequence(cursorPosition, targetBean.getContent().length());
//        //获取拼接后的文字
//        CharSequence groupText = frontText + "the" + backText.toString();
//
//        doAutoSub(beans.indexOf(targetBean), groupText);
//        System.out.println(beans.size());
//    }
//
//    @Test
//    public void testa() {
//        System.out.println(0 % 3);
//    }
//
//    private TextConnection.TextBean toGetOptionBean(int selection) {
//        TextConnection.TextBean targetBean = null;
//        for (TextConnection.TextBean bean : beans) {
//            targetBean = bean;
//            if (selection <= bean.getContent().length()) {
//                targetBean.setCursorPosition(selection);
//                break;
//            } else {
//                selection -= bean.getContent().length() + 1;
//            }
//        }
//        return targetBean;
//    }
//
//
//    private void doAutoSub(int position, CharSequence data) {
//        //首先判断该行是否存在
//        if (beans.size() > position) {
//            //存在
//            TextConnection.TextBean bean = beans.get(position);
//            //判断data长度是否超过一行最大长度
//            if (data.length() >= LINE_MAX_LENGTH) {
//                //超过,做截断处理,并且换行
//                bean.setNewLine(true);
//                bean.setContent(data.subSequence(0, LINE_MAX_LENGTH));
//                //塞入本行后剩下的数据
//                CharSequence lastStr = data.subSequence(LINE_MAX_LENGTH, data.length());
//                //判断是否刚好为空
//                if (lastStr.length() <= 0) {
//                    return;
//                }
//                position++;
//                if (beans.size() > position) {
//                    doAutoInsert(position, lastStr);
//                } else {
//                    addNewLine(position, lastStr);
//                }
//            } else {
//                //判断是否刚好为空
//                if (TextUtils.isEmpty(data)) {
//                    return;
//                }
//                //不超过,直接塞入进原疏忽,并且结束循环
//                bean.setContent(data);
//            }
//        } else {
//            addNewLine(position, data);
//        }
//    }
//
//    /**
//     * 自动向选中行的下一行插入数据
//     *
//     * @param position
//     * @param lastStr
//     */
//    private void doAutoInsert(int position, CharSequence lastStr) {
//        //获取本行
//        TextConnection.TextBean bean = beans.get(position);
//        //判断本行是否是手动换行
//        if (bean.isNewLineSelf()) {
//            //如果是手动换行,作为新行添加
//            addNewLine(position, lastStr);
//        } else {
//            //如果不是手动换行,
//            CharSequence groupText = lastStr + bean.getContent().toString();
//            doAutoSub(position, groupText);
//        }
//    }
//
//    private void addNewLine(int inDataPosition, CharSequence text) {
//        if (text.length() >= LINE_MAX_LENGTH) {
//            if (beans.size() > inDataPosition) {
//                doSubStr(inDataPosition, text);
//            } else {
//                TextConnection.TextBean bean = new TextConnection.TextBean();
//                CharSequence subText = text.subSequence(0, 11);
//                CharSequence last = text.subSequence(11, text.length());
//                bean.setContent(subText);
//                bean.setNewLine(true);
//                beans.add(bean);
//                if (last.length() <= 0) {
//                    return;
//                }
//                inDataPosition++;
//                addNewLine(inDataPosition, last);
//            }
//        } else {
//            TextConnection.TextBean textBean = new TextConnection.TextBean();
//            textBean.setContent(text);
//            beans.add(textBean);
//        }
//    }
//
//    /**
//     * 数据截断处理
//     *
//     * @param inDataPosition
//     * @param text
//     * @return
//     */
//    private void doSubStr(int inDataPosition, CharSequence text) {
//        if (text.length() >= LINE_MAX_LENGTH) {
//            CharSequence sequence = text.subSequence(0, LINE_MAX_LENGTH);
//            TextConnection.TextBean textBean = new TextConnection.TextBean();
//            textBean.setContent(sequence);
//            textBean.setNewLine(true);
//            beans.add(inDataPosition, textBean);
//            inDataPosition++;
//            doSubStr(inDataPosition, text.subSequence(LINE_MAX_LENGTH, text.length()));
//        } else {
//            addNewLine(inDataPosition, text);
//        }
//    }
//}
