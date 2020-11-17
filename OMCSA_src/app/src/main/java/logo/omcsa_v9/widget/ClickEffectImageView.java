/*
 * HorizontalListView.java v1.5
 *
 * 
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package logo.omcsa_v9.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ClickEffectImageView extends ImageView {

	boolean m_bOutOf = false;
	
	private Rect rect;    // Variable rect to hold the bounds of the view
	
	public ClickEffectImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
/*		setClickable(true);
		setEnabled(true);
		setFocusable(true);
		setFocusableInTouchMode(true);*/
	}

	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            //overlay is black with transparency of 0x77 (119)
            this.getDrawable().setColorFilter(0x50000000,android.graphics.PorterDuff.Mode.SRC_ATOP);
            this.invalidate();
            rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            m_bOutOf = false;
            //Log.e("test", "Down");
            return true;
            
        case MotionEvent.ACTION_CANCEL:
            //clear the overlay
        	//Log.e("test", "CANCEL");
            this.getDrawable().clearColorFilter();
            this.invalidate();
            return true;
        case MotionEvent.ACTION_MOVE:
            //clear the overlay
        	if (m_bOutOf)  	return false;
        	
        	if(!rect.contains(getLeft() + (int) event.getX(), getTop() + (int) event.getY())){
        		this.getDrawable().clearColorFilter();
        		this.invalidate();
        		m_bOutOf = true;
        		//Log.e("test", "OUTSIDE");
        	} else {
        		//Log.e("test", "INSIDE");
        	}
        	return true;
        case MotionEvent.ACTION_UP:
            //clear the overlay
        	//Log.e("test", "Up");
            this.getDrawable().clearColorFilter();
            this.invalidate();
            if (m_bOutOf == false) 
            	performClick();
            return true;
		}
		
		return false;
	}
}
