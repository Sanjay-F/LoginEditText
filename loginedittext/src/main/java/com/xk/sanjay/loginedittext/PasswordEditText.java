package com.xk.sanjay.loginedittext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;


/**
 * Custom version of EditText that shows and hides password onClick of the visibility icon
 * Created by Sanjay on 2016/1/13 on project lifeWork.
 */
public class PasswordEditText extends EditText implements TextWatcher {

    private boolean isShowingPassword = false;
    private Drawable drawableEnd;
    private Rect bounds;
    private boolean leftToRight = true;
    //用来设置右边的符号是否一直显示，还是有数据才显示
    private boolean isStickMode = true;

    @DrawableRes
    private int visiblityIndicatorShow = R.drawable.ic_visibility_grey_900_24dp;
    @DrawableRes
    private int visiblityIndicatorHide = R.drawable.ic_visibility_off_grey_900_24dp;
    private boolean monospace;

    public PasswordEditText(Context context) {
        super(context);
        init(null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attrsArray =
                    getContext().obtainStyledAttributes(attrs, R.styleable.PasswordEditText);
            visiblityIndicatorShow = attrsArray.getResourceId(R.styleable.PasswordEditText_drawable_show, visiblityIndicatorShow);
            visiblityIndicatorHide = attrsArray.getResourceId(R.styleable.PasswordEditText_drawable_hide, visiblityIndicatorHide);
            isStickMode = attrsArray.getBoolean(R.styleable.PasswordEditText_stick_mode, true);
            monospace = attrsArray.getBoolean(R.styleable.PasswordEditText_monospace, true);
            attrsArray.recycle();
        }
        leftToRight = isLeftToRight();
        isShowingPassword = false;
        setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, true);
        if (!monospace) {
            setTypeface(Typeface.DEFAULT);
        }
        showPasswordVisibilityIndicator(isStickMode);
        addTextChangedListener(this);
    }

    private boolean isLeftToRight() {
        // If we are pre JB assume always LTR
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return true;
        }

        // Other methods, seemingly broken when testing though.
        // return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
        // return !ViewUtils.isLayoutRtl(this);

        Configuration config = getResources().getConfiguration();
        return !(config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom) {

        //keep a reference to the right drawable so later on touch we can check if touch is on the drawable
        if (leftToRight && right != null) {
            drawableEnd = right;
        } else if (!leftToRight && left != null) {
            drawableEnd = left;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && drawableEnd != null) {
            bounds = drawableEnd.getBounds();
            final int x = (int) event.getX();

            //扩大点击范围
            int rightBoudn = this.getRight() - (3 * bounds.width());
            int leftBound = this.getLeft() + (3 * bounds.width());

            //check if the touch is within bounds of drawableEnd icon
            if ((leftToRight && (x >= rightBoudn)) ||
                    (!leftToRight && (x <= leftBound))) {
                togglePasswordVisability();
                //use this to prevent the keyboard from coming up
                event.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        return super.onTouchEvent(event);
    }

    private void showPasswordVisibilityIndicator(boolean show) {
        if (show) {
            Drawable drawable = isShowingPassword ?
                    getResources().getDrawable(visiblityIndicatorHide) :
                    getResources().getDrawable(visiblityIndicatorShow);

            setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[0], null, leftToRight ? drawable : null, null);

        } else {
            // 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片,获取图片的顺序是左上右下（0,1,2,3,）
            setCompoundDrawables(getCompoundDrawables()[0], null, null, null);
        }
    }

    private void togglePasswordVisability() {
        if (isShowingPassword) {
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, true);
        } else {
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, true);
        }
        isShowingPassword = !isShowingPassword;
        showPasswordVisibilityIndicator(true);
    }

    @Override
    protected void finalize() throws Throwable {
        drawableEnd = null;
        bounds = null;
        super.finalize();
    }

    private void setInputType(int inputType, boolean keepState) {
        int selectionStart = -1;
        int selectionEnd = -1;
        if (keepState) {
            selectionStart = getSelectionStart();
            selectionEnd = getSelectionEnd();
        }
        setInputType(inputType);
        if (keepState) {
            setSelection(selectionStart, selectionEnd);
        }
    }


    public
    @DrawableRes
    int getVisiblityIndicatorShow() {
        return visiblityIndicatorShow;
    }

    public void setVisiblityIndicatorShow(@DrawableRes int visiblityIndicatorShow) {
        this.visiblityIndicatorShow = visiblityIndicatorShow;
    }

    public
    @DrawableRes
    int getVisiblityIndicatorHide() {
        return visiblityIndicatorHide;
    }

    public void setVisiblityIndicatorHide(@DrawableRes int visiblityIndicatorHide) {
        this.visiblityIndicatorHide = visiblityIndicatorHide;
    }

    /**
     * @return true if the password is visable | false if hidden
     */
    public boolean isShowingPassword() {
        return isShowingPassword;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (text.length() > 0) {
            showPasswordVisibilityIndicator(true);
        } else {
            showPasswordVisibilityIndicator(isStickMode);
        }


    }

}