package com.example.update.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.update.R;


/**
 * 自定义更新提示：Dialog样式一
 */
public class OneStyleDialog extends Dialog {

    private TextView titleTV;
    private TextView contentTV;
    private Button sureBTN;
    private Button cancelBTN;

    private String title;//标题
    private String content;//内容
    private String leftBtnText;//左边按钮text
    private String rightBtnText;//右边按钮text

    private OnYesClickListener onYesClickListener;
    private OnNoClickListener onNoClickListener;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLeftBtnText(String leftBtnText) {
        this.leftBtnText = leftBtnText;
    }


    public void setRightBtnText(String rightBtnText) {
        this.rightBtnText = rightBtnText;
    }


    public void setOnYesClickListener(OnYesClickListener onYesClickListener) {
        this.onYesClickListener = onYesClickListener;
    }

    public void setOnNoClickListener(OnNoClickListener onNoClickListener) {
        this.onNoClickListener = onNoClickListener;
    }

    public OneStyleDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_one_style);

        setCanceledOnTouchOutside(false);//点击空白不能取消dialog

        initView();
        initData();
        initEvent();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        sureBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onYesClickListener!=null){
                    onYesClickListener.yesClick();
                }
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onNoClickListener!=null){
                    onNoClickListener.noClick();
                }
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {
        titleTV.setText(title);
        contentTV.setText(content);
        sureBTN.setText(leftBtnText);
        cancelBTN.setText(rightBtnText);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        titleTV= (TextView) findViewById(R.id.titleTV);
        contentTV= (TextView) findViewById(R.id.contentTV);
        sureBTN= (Button) findViewById(R.id.sureBTN);
        cancelBTN= (Button) findViewById(R.id.cancelBTN);
    }

    /**
     * 设置确定按钮和取消按钮的接口回调
     */
    public interface OnYesClickListener {
        void yesClick();
    }
    public interface OnNoClickListener{
        void noClick();
    }
}
