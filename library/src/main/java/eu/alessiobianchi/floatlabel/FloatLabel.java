package eu.alessiobianchi.floatlabel;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;

/**
 * @author Alessio Bianchi
 * Inspired by Ian G. Clifton's FloatLabel
 */
public class FloatLabel extends EditText {

	private TextPaint mLabelPaint;

	private boolean mLabelShowing;

	private int mEditTextHintTextColor;
	private int mLabelHintTextColor;

	private long mAnimationStartTime = Long.MIN_VALUE;
	private long mAnimationDuration = 300;
	private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	public FloatLabel(Context context) {
		super(context);
		init(context, null, 0);
	}

	public FloatLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public FloatLabel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mLabelPaint.setSubpixelText(true);
		setLabelHintTextColor(getCurrentTextColor());

		reloadEditTextHintTextColor();

		final float minHintSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
		mLabelPaint.setTextSize(Math.max(getTextSize() * 0.6f, minHintSize));

		mLabelShowing = !TextUtils.isEmpty(getText());
	}

	public void setLabelTypeface(@NonNull Typeface typeface) {
		mLabelPaint.setTypeface(typeface);
		invalidate();
	}

	public void setLabelHintTextColor(int color) {
		mLabelPaint.setColor(color);
		mLabelHintTextColor = color;
		invalidate();
	}

	public void setAnimationDuration(long duration) {
		this.mAnimationDuration = duration;
	}

	public void setAnimationInterpolator(@NonNull Interpolator interpolator) {
		this.mInterpolator = interpolator;
	}

	/**
	 * If you set an hint text color programmatically, be sure to call this method afterwards.
	 * This is an ugly hack but it's required since setHintTextColor() is final...
	 */
	public void reloadEditTextHintTextColor() {
		mEditTextHintTextColor = getCurrentHintTextColor();
	}

	@Override
	public void setGravity(int gravity) {
		gravity &= ~Gravity.VERTICAL_GRAVITY_MASK; // remove vertical gravity
		gravity |= Gravity.BOTTOM; // force bottom vertical gravity
		super.setGravity(gravity);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int newHeight = (int) Math.ceil(getMeasuredHeight() + mLabelPaint.getFontSpacing());
		setMeasuredDimension(getMeasuredWidth(), newHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float mAnimationFraction = 1f;

		if (mAnimationStartTime != Long.MIN_VALUE) {
			// animation is running: calculate current fraction and invalidate to draw next frame
			long currentAnimationTime = SystemClock.elapsedRealtime() - mAnimationStartTime;
			if (currentAnimationTime <= mAnimationDuration) {
				mAnimationFraction = currentAnimationTime / (float) mAnimationDuration;
				mAnimationFraction = mInterpolator.getInterpolation(mAnimationFraction);
			} else {
				mAnimationStartTime = Long.MIN_VALUE;
			}
			invalidate();
		}

		final float offset = mLabelPaint.getFontSpacing() / 2f;
		final float labelY;
		if (mLabelShowing) {
			labelY = getPaddingTop() - mLabelPaint.ascent() + (1f - mAnimationFraction) * offset;
			mLabelPaint.setColor(interpolateAlpha(mLabelHintTextColor, mAnimationFraction));
		} else {
			setHintTextColor(interpolateAlpha(mEditTextHintTextColor, mAnimationFraction));
			labelY = getPaddingTop() - mLabelPaint.ascent() + (mAnimationFraction) * offset;
			mLabelPaint.setColor(interpolateAlpha(mLabelHintTextColor, 1f - mAnimationFraction));
		}
		canvas.drawText(getHint(), 0, getHint().length(), ViewCompat.getPaddingStart(this), labelY, mLabelPaint);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);

		if (TextUtils.isEmpty(text)) {
			if (mLabelShowing) {
				// if the editText has just been emptied, set its hint text color to transparent, we will animate it to full alpha in onDraw()
				setHintTextColor(Color.TRANSPARENT);
			}
			hideLabel();
		} else if (!mLabelShowing) {
			displayLabel();
		}
	}

	private void displayLabel() {
		mLabelShowing = true;
		mAnimationStartTime = SystemClock.elapsedRealtime();
		invalidate();
	}

	private void hideLabel() {
		mLabelShowing = false;
		mAnimationStartTime = SystemClock.elapsedRealtime();
		invalidate();
	}

	private static int interpolateAlpha(int color, float fraction) {
		int endAlpha = Color.alpha(color);
		int endColor = color & 0xffffff;
		return (int) (fraction * endAlpha) << 24 | endColor;
	}

}
