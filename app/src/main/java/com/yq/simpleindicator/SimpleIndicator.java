package com.yq.simpleindicator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * 详见洋神博客,此处将三角下表修改为下划线
 * http://blog.csdn.net/lmj623565791/article/details/42160391
 *
 * @author zhy
 */
public class SimpleIndicator extends HorizontalScrollView {
    /**
     * 绘制indicator的画笔
     */
    private Paint mPaint;
    /**
     * path构成一个三角形
     */
    private Path mPath;
    /**
     * 下划线的宽度
     */
    private int mLineWidth;

    /**
     * 初始时，三角形指示器的偏移量
     */
    private int mInitTranslationX;
    /**
     * 手指滑动时的偏移量
     */
    private float mTranslationX;

    /**
     * 默认的Tab数量
     */
    private static final int COUNT_DEFAULT_TAB = 4;
    /**
     * tab数量
     */
    private int mTabVisibleCount = COUNT_DEFAULT_TAB;

    /**
     * tab上的内容
     */
    private List<String> mTabTitles;
    /**
     * 与之绑定的ViewPager
     */
    public ViewPager mViewPager;

    private int indicatorColor = 0xFFC30D23;//默认红色

    private int lineColor = 0x00000000;//中间的线默认透明

    private int textColor = 0xFF333333;//默认字体颜色

    private int selectedTextColor = 0xFFEE8600;//文字选中颜色

    private int textSize = 18;//默认大小

    private int selected_textSize = 18;//默认大小

    private Rect rect;

    private LinearLayout linearLayout;

    private ValueAnimator animator;//滑动动画

    public SimpleIndicator(Context context) {

        this(context, null);
    }

    public SimpleIndicator(Context context, AttributeSet attrs) {

        super(context, attrs);

        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {

        setHorizontalScrollBarEnabled(false);//滚动条不可见

        initAttrs(context, attrs);//初始化属性

        initPaint();

        addLayout(context);

    }

    /**
     * 初始化属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {

        // 获得自定义属性，tab的数量
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);

        mTabVisibleCount = a.getInt(R.styleable.ViewPagerIndicator_item_count, COUNT_DEFAULT_TAB);

        indicatorColor = a.getColor(R.styleable.ViewPagerIndicator_indicator_color, indicatorColor);

        lineColor = a.getColor(R.styleable.ViewPagerIndicator_line_color, lineColor);//书分割线颜色

        textColor = a.getColor(R.styleable.ViewPagerIndicator_text_color, textColor);//字体默认颜色

        selectedTextColor = a.getColor(R.styleable.ViewPagerIndicator_selected_text_color, selectedTextColor);//选中字体颜色

        textSize = a.getInt(R.styleable.ViewPagerIndicator_text_size, textSize);//默认文字大小

        selected_textSize = a.getInt(R.styleable.ViewPagerIndicator_selected_text_size, selected_textSize);//选中字体大小

        if (mTabVisibleCount < 0) mTabVisibleCount = COUNT_DEFAULT_TAB;

        a.recycle();
    }

    /**
     * 画笔初始化
     */
    private void initPaint() {
        // 初始化画笔
        mPaint = new Paint();

        mPaint.setAntiAlias(true);

        mPaint.setColor(indicatorColor);

        mPaint.setStyle(Style.FILL);

        mPaint.setPathEffect(new CornerPathEffect(3));
    }

    /**
     * 添加LinearLayout
     *
     * @param context
     */
    private void addLayout(Context context) {

        linearLayout = new LinearLayout(context);

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        this.addView(linearLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * 初始化下部线的长度
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        mLineWidth = w / mTabVisibleCount;

        mInitTranslationX = 0;
    }

    /**
     * 绘制指示器
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {

        canvas.save();

        // 画笔平移到正确的位置
        canvas.translate(mInitTranslationX + mTranslationX, getHeight() - dip2px(1));

        rect = new Rect(0, 0, mLineWidth, 10);

        canvas.drawRect(rect, mPaint);

        canvas.restore();

        super.dispatchDraw(canvas);

    }

    /**
     * 设置可见的tab的数量
     *
     * @param count
     */
    public void setVisibleTabCount(int count) {

        this.mTabVisibleCount = count;

    }

    /**
     * 设置tab的标题内容 可选，可以自己在布局文件中写死
     *
     * @param datas
     */
    public void setTabItemTitles(List<String> datas) {

        // 如果传入的list有值，则移除布局文件中设置的view
        if (datas != null && datas.size() > 0) {

            linearLayout.removeAllViews();

            this.mTabTitles = datas;

            for (String title : mTabTitles) {
                // 添加view
                linearLayout.addView(generateTextView(title));

                linearLayout.addView(generateLineView());
            }
            // 设置item的click事件
            setItemClickEvent();
        }

        highLightTextView(0);

    }

    /**
     * 对外的ViewPager的回调接口
     *
     * @author zhy
     */
    public interface PageChangeListener {

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

    // 对外的ViewPager的回调接口
    private PageChangeListener onPageChangeListener;

    // 对外的ViewPager的回调接口的设置
    public void setOnPageChangeListener(PageChangeListener pageChangeListener) {

        this.onPageChangeListener = pageChangeListener;

    }

    // 设置关联的ViewPager
    public void setViewPager(ViewPager mViewPager, int pos) {

        this.mViewPager = mViewPager;

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // 设置字体颜色高亮
                resetTextViewColor();

                highLightTextView(position * 2);

                if (null != clickListener) clickListener.onClick(position);//获取当前位置

                // 回调
                if (onPageChangeListener != null) {

                    onPageChangeListener.onPageSelected(position);

                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (getScrollX() == 0 && position > mTabVisibleCount) {//当初始位置超过可见数量时，不同的积分竞猜显示对应的记录界面

                    position = position - 1;

                    positionOffset = 1.0f;

                }

                // 滚动
                scroll(position, positionOffset);

                // 回调
                if (onPageChangeListener != null) {

                    onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 回调
                if (onPageChangeListener != null) {

                    onPageChangeListener.onPageScrollStateChanged(state);

                }

            }
        });

        // 设置当前页
        mViewPager.setCurrentItem(pos);
        // 高亮
//        highLightTextView(pos);
    }

    /**
     * 高亮文本
     *
     * @param position
     */
    protected void highLightTextView(int position) {

        View view = linearLayout.getChildAt(position);

        if (view instanceof TextView) {

            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, selected_textSize);

            ((TextView) view).setTextColor(selectedTextColor);

        }

    }

    /**
     * 重置文本颜色
     */
    private void resetTextViewColor() {

        for (int i = 0; i < linearLayout.getChildCount(); i++) {

            View view = linearLayout.getChildAt(i * 2);

            if (view instanceof TextView) {

                ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

                ((TextView) view).setTextColor(textColor);
            }
        }
    }

    /**
     * 设置点击事件
     */
    public void setItemClickEvent() {

        int cCount = linearLayout.getChildCount() / 2;//只算textview的个数

        for (int i = 0; i < cCount; i++) {

            final int j = i;

            View view = linearLayout.getChildAt(i * 2);

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (null != mViewPager) {

                        if (j == mViewPager.getCurrentItem()) return;

                        mViewPager.setCurrentItem(j, true);

                    } else {

                        resetTextViewColor();

                        highLightTextView(j * 2);

                        scroll(j, 0);

                    }


                    if (null != clickListener) clickListener.onClick(j);

                }
            });

        }
    }

    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {

        this.clickListener = clickListener;

    }

    public interface ClickListener {

        void onClick(int pos);
    }

    /**
     * 根据标题生成我们的TextView
     *
     * @param text
     * @return
     */
    private TextView generateTextView(String text) {

        TextView tv = new TextView(getContext());

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        lp.width = getScreenWidth() / mTabVisibleCount - dip2px(1);

        tv.setGravity(Gravity.CENTER);

        tv.setTextColor(textColor);

        tv.setText(text);

        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        tv.setLayoutParams(lp);

        return tv;
    }

    /**
     * 根据标题生成我们的TextView
     *
     * @return
     */
    private View generateLineView() {

        View line = new View(getContext());

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

        lp.width = dip2px(1);

        line.setLayoutParams(lp);

        line.setBackgroundColor(lineColor);

        return line;
    }


    /**
     * 指示器跟随手指滚动，以及容器滚动
     *
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {

        // 不断改变偏移量，invalidate
        mTranslationX = getWidth() / mTabVisibleCount * (position + offset);

        int tabWidth = getWidth() / mTabVisibleCount;

        // 容器滚动，当移动到倒数最后一个的时候，开始滚动
        if (offset >= 0 && position >= (mTabVisibleCount - 2) && linearLayout.getChildCount() / 2 > mTabVisibleCount && position <= linearLayout.getChildCount() / 2 - 2) {

            if (mTabVisibleCount != 1) {

//                scrollTo((position - (mTabVisibleCount - 2)) * tabWidth + (int) (tabWidth * offset), 0);

                startAnimation(position, offset, tabWidth);

            } else { // 为count为1时 的特殊处理

                scrollTo(position * tabWidth + (int) (tabWidth * offset), 0);

            }

        }

        invalidate();
    }

    /**
     * 滑动导航条
     *
     * @param position
     * @param offset
     * @param tabWidth
     */
    private void startAnimation(int position, float offset, int tabWidth) {

        animator = ValueAnimator.ofInt(getScrollX(), (position - (mTabVisibleCount - 2)) * tabWidth + (int) (tabWidth * offset));

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                scrollTo((int) animation.getAnimatedValue(), 0);

            }
        });

        animator.start();
    }

    /**
     * 获得屏幕的宽度
     *
     * @return
     */
    public int getScreenWidth() {

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics outMetrics = new DisplayMetrics();

        wm.getDefaultDisplay().getMetrics(outMetrics);

        return outMetrics.widthPixels;

    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {

        final float scale = getContext().getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale + 0.5f);

    }

}
