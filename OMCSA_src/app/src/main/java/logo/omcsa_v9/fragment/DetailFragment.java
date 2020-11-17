package logo.omcsa_v9.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;
import logo.omcsa_v9.model.LegendPoint;
import logo.omcsa_v9.model.LegendRegion;
import logo.omcsa_v9.model.ProjectContent;
import logo.omcsa_v9.model.ProjectImage;
import logo.omcsa_v9.model.ProjectInfo;
import logo.omcsa_v9.model.ProjectLegend;
import logo.omcsa_v9.model.ProjectLegendInfo;
import logo.omcsa_v9.model.ProjectSeries;
import logo.omcsa_v9.model.ProjectStructure;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.Utils;
import logo.omcsa_v9.widget.AutoCompleteArrayAdapter;
import logo.omcsa_v9.widget.EllipsizingTextView;
import logo.omcsa_v9.widget.LegendView;
import logo.omcsa_v9.zoom.ZoomApi;
import logo.omcsa_v9.zoom.ZoomEngine;
import logo.omcsa_v9.zoom.ZoomImageView;

public class DetailFragment extends BaseFragment implements View.OnClickListener {

    public int[] thumbImageIdx = {R.id.image_1, R.id.image_2, R.id.image_3, R.id.image_4, R.id.image_5,
            R.id.image_6, R.id.image_7, R.id.image_8, R.id.image_9, R.id.image_10};

    public String[] rainbowColors = {"rainbow_1", "rainbow_2", "rainbow_3", "rainbow_4", "rainbow_5",
            "rainbow_6", "rainbow_7", "rainbow_8", "rainbow_9", "rainbow_10", "rainbow_11"};
    public List<String> legendTextArray = null;
    ProjectInfo mProjectInfo;
    ProjectContent mProjectContent;
    Map<String, ProjectImage> mImageMap = null;
    Map<String, ProjectSeries> mSeriesMap = null;
    Map<String, ProjectStructure> mStructureMap = null;
    Map<String, ProjectLegend> mLegendMap = null;
    AutoCompleteArrayAdapter legendSearchAdapter = null;
    private String[] langKeys = {"english", "Français", "JAPANESE", "Chinese_sc", "Chinese_tc", "Español", "Português"};
    private String[] langKeys1 = {"en", "fr", "ja", "sc", "tc", "es", "pt"};

    int mSelImgIdx = -1;
    Map<String, List<ProjectImage>> mProjectImageList = new HashMap<>();
    List<ProjectImage> mSelProjectImageList = new ArrayList<>();
    List<String> mSeries = new ArrayList<>();
    private LinearLayout mSeekBar;
    private int visibleControllerIdx = -1;
    private int brightness = 0;
    private int contrast = 0;
    private boolean vertical_flip = false;
    private boolean horz_flip = false;
    private int mRainbowIdx = 1;
    private int mSeriesIdx = 0;
    private int mStructureIdx = 0;
    private Map<Integer, Boolean> selStructureIDList = new HashMap<>();
    private List<String> mStructures = new ArrayList<>();
    private int mLanguageIdx = 0;
    private float mScaleFactor = 1.0f;
    ZoomEngine zoomEngine;
    private int mImageWidth;
    private int mImageHeight;
    private LegendView legendView = null;
    List<View> mLeftTextViewList = new ArrayList<>();
    List<View> mRightTextViewList = new ArrayList<>();
    WebView detailContentView;
    ProjectLegend selProjectLegend = null;
    private String strSearchText = "";
    private float moveY = 0f;

    private int nRowCount = 0;

    float mCurSelXPos = 0;
    float mCurSelYPos = 0;

    int nDisplayMode = 0; // 0 -- Displayed, 1 -- Quiz, 2 -- Hidden

    private Handler mHandler;

    boolean bShowText = false;

    private View.OnClickListener mLegendClickEvent = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            TextView legendCaption = view.findViewById(R.id.text);
            View lineView = view.findViewById(R.id.line1);
            if(legendCaption.getCurrentTextColor() == getResources().getColor(R.color.transparent))
            {
                Drawable drawable = lineView.getBackground();
                if( drawable instanceof ColorDrawable )
                {
                    legendCaption.setTextColor(((ColorDrawable) drawable).getColor());
                }
            }
            else
            {
                ProjectLegend legend = (ProjectLegend)view.getTag();
                selProjectLegend = legend;
                doRefreshProjectLegendDetailContent();
            }
        }
    };

    private void doRefreshProjectLegendDetailContent()
    {
        if(selProjectLegend != null)
        {
            mRootView.findViewById(R.id.layoutDetail).setVisibility(View.VISIBLE);
            ((TextView)mRootView.findViewById(R.id.legend_name)).setText(selProjectLegend.text.get(langKeys[mLanguageIdx]));
            detailContentView.loadData(selProjectLegend.description.get(langKeys1[mLanguageIdx]), "text/html", "UTF-8");
        }
    }

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLanguageIdx = Utils.getIntSetting(mContext, Global.PREF_LANGAUGE);
    }

    ZoomEngine.Listener listener = new ZoomEngine.Listener() {
        @Override
        public void onUpdate(ZoomEngine engine, Matrix matrix) {
            if(engine.getRealZoom() != 1.0f){
                ((ZoomImageView)mRootView.findViewById(R.id.image)).onUpdate(engine, matrix);
                if(!bShowText)
                {
                    loadLegends(false);
                }
            }
        }

        @Override
        public void onIdle(ZoomEngine engine) {
            if(!bShowText)
            {
                loadLegends(true);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView == null)
        {
            mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mHandler = new Handler(Looper.getMainLooper());

            if(mContext != null)
            {
                Utils.doApplyAllFontForTextView(mContext, mRootView);
                mContext.doShowSearchButton(false);
            }

            mSeekBar = mRootView.findViewById(R.id.layoutSeek);

            mRootView.findViewById(R.id.btnQuiz).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(nDisplayMode != 1)
                    {
                        nDisplayMode = 1;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadLegends(true);
                            }
                        }, 500);

                    }
                }
            });

            mRootView.findViewById(R.id.btnHidden).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(nDisplayMode != 2)
                    {
                        nDisplayMode = 2;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadLegends(true);
                            }
                        }, 500);
                    }
                }
            });

            mRootView.findViewById(R.id.btnDisplayed).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(nDisplayMode != 0)
                    {
                        nDisplayMode = 0;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadLegends(true);
                            }
                        }, 500);
                    }
                }
            });

            mRootView.findViewById(R.id.layoutImageBar).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int x = (int) motionEvent.getRawX() - view.getLeft();
                    if(x >= view.getWidth() - mSeekBar.getWidth())
                    {
                        x = view.getWidth() - mSeekBar.getWidth();
                    }
                    if(x < 0) x = 0;
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                        {
                            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                                    mSeekBar.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            mSeekBar.setLayoutParams(lParams);
                            refreshImage();
                            bShowText = true;
                            loadLegends(true);
                        }
                        break;
                        case MotionEvent.ACTION_UP:
                        {
                            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                                    mSeekBar.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            mSeekBar.setLayoutParams(lParams);
                            refreshImage();
                            bShowText = true;
                            loadLegends(true);
                        }
                        break;
                        case MotionEvent.ACTION_MOVE:
                        {
                            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                                    mSeekBar.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            mSeekBar.setLayoutParams(lParams);
                            refreshImage();
                            bShowText = true;
                            loadLegends(true);
                        }
                        break;
                    }
                    return true;
                }
            });
            mRootView.findViewById(R.id.btnOrigin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bShowText = false;
                    ((ZoomImageView)mRootView.findViewById(R.id.image)).zoomTo(1.0f, true);
                }
            });
            mRootView.findViewById(R.id.layoutController).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideController();
                }
            });
            mRootView.findViewById(R.id.btnBrightness).setOnClickListener(this);
            mRootView.findViewById(R.id.btnContrast).setOnClickListener(this);
            mRootView.findViewById(R.id.btnStructure).setOnClickListener(this);
            mRootView.findViewById(R.id.btnSetting).setOnClickListener(this);

            mRootView.findViewById(R.id.layoutBrightnessRegion).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int y = (int) motionEvent.getRawY();
                    int[] values = new int[2];
                    mRootView.findViewById(R.id.brightnessTrack).getLocationInWindow(values);
                    int baseY = values[1];
                    y -= baseY;
                    ImageView imgController = mRootView.findViewById(R.id.imgBrightness);
                    int nMax = mRootView.findViewById(R.id.brightnessTrack).getHeight();
                    if(y >= nMax)
                    {
                        y = nMax;
                    }
                    else if(y < 0)
                    {
                        y = 0;
                    }

                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.topMargin = y;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            brightness = ((int)((nMax - y) * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshBrightController1Value();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_UP:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.topMargin = y;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            brightness = ((int)((nMax - y) * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshBrightController1Value();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_MOVE:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.topMargin = y;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            brightness = ((int)((nMax - y) * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshBrightController1Value();
                            drawImage();
                        }
                        break;
                    }
                    return true;
                }
            });

            mRootView.findViewById(R.id.layoutContrastRegion).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    int y = (int) motionEvent.getRawY();
                    int[] values = new int[2];
                    mRootView.findViewById(R.id.brightnessTrack).getLocationInWindow(values);
                    int baseY = values[1];
                    y -= baseY;
                    ImageView imgController = mRootView.findViewById(R.id.imgContrast);
                    int nMax = mRootView.findViewById(R.id.brightnessTrack).getHeight();
                    if(y >= nMax)
                    {
                        y = nMax;
                    }
                    else if(y < 0 )
                    {
                        y= 0;
                    }

                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.topMargin = y;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            contrast = ((int)((nMax - y) * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshContrastController1Value();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_UP:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.topMargin = y;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            contrast = ((int)((nMax - y) * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshContrastController1Value();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_MOVE:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.topMargin = y;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            contrast = ((int)((nMax - y) * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshContrastController1Value();
                            drawImage();
                        }
                        break;
                    }
                    return true;
                }
            });

            mRootView.findViewById(R.id.layoutBrightnessRegion1).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int x = (int) motionEvent.getRawX();
                    int[] values = new int[2];
                    mRootView.findViewById(R.id.brightnessTrack1).getLocationInWindow(values);
                    int baseX = values[0];
                    x -= baseX;
                    ImageView imgController = mRootView.findViewById(R.id.imgBrightness1);
                    int nMax = mRootView.findViewById(R.id.brightnessTrack1).getWidth();
                    if(x >= nMax)
                    {
                        x = nMax;
                    }
                    else if(x < 0 )
                    {
                        x = 0;
                    }

                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            brightness = ((int)(x * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshBrightControllerValue();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_UP:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            brightness = ((int)(x * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshBrightControllerValue();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_MOVE:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            brightness = ((int)(x * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshBrightControllerValue();
                            drawImage();
                        }
                        break;
                    }
                    return true;
                }
            });

            mRootView.findViewById(R.id.layoutContrastRegion1).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int x = (int) motionEvent.getRawX();
                    int[] values = new int[2];
                    mRootView.findViewById(R.id.brightnessTrack1).getLocationInWindow(values);
                    int baseX = values[0];
                    x -= baseX;
                    ImageView imgController = mRootView.findViewById(R.id.imgContrast1);
                    int nMax = mRootView.findViewById(R.id.brightnessTrack1).getWidth();
                    if(x >= nMax)
                    {
                        x = nMax;
                    }
                    else if(x < 0 )
                    {
                        x = 0;
                    }

                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            contrast = ((int)(x * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshContrastControllerValue();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_UP:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            contrast = ((int)(x * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshContrastControllerValue();
                            drawImage();
                        }
                        break;
                        case MotionEvent.ACTION_MOVE:
                        {
                            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) imgController.getLayoutParams();
                            lParams.leftMargin = x;
                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;
                            imgController.setLayoutParams(lParams);
                            contrast = ((int)(x * 1.0 / nMax * 200.0f)) - 100;
                            refreshBrightContrast();
                            refreshContrastControllerValue();
                            drawImage();
                        }
                        break;
                    }
                    return true;
                }
            });

            ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String strSearchString = editable.toString();
                    if(TextUtils.isEmpty(strSearchString))
                    {
                        mRootView.findViewById(R.id.btnResetSearch).setVisibility(View.GONE);
                        if(!TextUtils.isEmpty(strSearchText))
                        {
                            doSearch();
                        }
                    }
                    else
                    {
                        mRootView.findViewById(R.id.btnResetSearch).setVisibility(View.VISIBLE);
                    }
                }
            });

            DisplayMetrics displayMetrics = new DisplayMetrics();
            mContext.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setDropDownWidth(width - (int)Utils.convertDpToPixel(130.0f, mContext));
            mRootView.findViewById(R.id.btnResetSearch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setText("");
                }
            });
            zoomEngine = ((ZoomImageView)mRootView.findViewById(R.id.image)).getEngine();

            ((ZoomImageView)mRootView.findViewById(R.id.image)).setOverScrollHorizontal(true);
            ((ZoomImageView)mRootView.findViewById(R.id.image)).setOverScrollVertical(true);

            ((ZoomImageView)mRootView.findViewById(R.id.image)).setOnListener(listener);

            mRootView.findViewById(R.id.image).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    ZoomEngine engine = ((ZoomImageView)mRootView.findViewById(R.id.image)).getEngine();

                    if(motionEvent.getPointerCount() == 1)
                    {
                        if((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
                        {
                            mCurSelXPos = motionEvent.getRawX();
                            int[] screenLocation = new int[2];
                            mRootView.findViewById(R.id.image).getLocationOnScreen(screenLocation);
                            mCurSelYPos = motionEvent.getRawY() - screenLocation[1];
                            bShowText = true;
                            loadLegends(true);
                        }
                    }

                    if(engine.getRealZoom() <= 1.05f && motionEvent.getPointerCount() == 1)
                    {
                        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN: {
                                moveY = motionEvent.getRawY();
                                bShowText = false;
                            }
                            break;
                            case MotionEvent.ACTION_MOVE:{
                                float curY = motionEvent.getRawY();
                                if(curY - moveY > 40)
                                {
                                    mRootView.findViewById(R.id.btnNextImg).performClick();
                                    moveY = curY;
                                }
                                else if(curY - moveY < -40)
                                {
                                    mRootView.findViewById(R.id.btnPrevImg).performClick();
                                    moveY = curY;
                                }
                                bShowText = true;
                            }
                            break;
                            case MotionEvent.ACTION_UP:{
                                moveY = 0;
                                bShowText = false;
                            }
                            break;
                        }
                        return true;
                    }
                    else if(engine.getRealZoom() > 1.05f || motionEvent.getPointerCount() == 2)
                    {
                        bShowText = false;
                        return ((ZoomImageView)view).onTouchEvent(motionEvent);
                    }
                    else
                    {
                        return true;
                    }

                }
            });
            mRootView.findViewById(R.id.btnSettingClose).setOnClickListener(this);
            mRootView.findViewById(R.id.btnSettingClose1).setOnClickListener(this);
            mRootView.findViewById(R.id.btnDetailClose).setOnClickListener(this);
            //mRootView.findViewById(R.id.btnDetailClose1).setOnClickListener(this);
            mRootView.findViewById(R.id.btnHorzSlip).setOnClickListener(this);
            mRootView.findViewById(R.id.btnVertSlip).setOnClickListener(this);
            mRootView.findViewById(R.id.layoutRainbow).setOnClickListener(this);
            mRootView.findViewById(R.id.btnRainbowLeft).setOnClickListener(this);
            mRootView.findViewById(R.id.btnRainbowRight).setOnClickListener(this);
            mRootView.findViewById(R.id.layoutView).setOnClickListener(this);
            mRootView.findViewById(R.id.btnViewLeft).setOnClickListener(this);
            mRootView.findViewById(R.id.btnViewRight).setOnClickListener(this);
            mRootView.findViewById(R.id.btnView).setOnClickListener(this);
            mRootView.findViewById(R.id.layoutStructures).setOnClickListener(this);
            mRootView.findViewById(R.id.imgStructures).setOnClickListener(this);
            mRootView.findViewById(R.id.layoutLanguage).setOnClickListener(this);
            mRootView.findViewById(R.id.imgLanguage).setOnClickListener(this);
            mRootView.findViewById(R.id.layoutLanguage1).setOnClickListener(this);
            mRootView.findViewById(R.id.imgLanguage1).setOnClickListener(this);
            legendView = mRootView.findViewById(R.id.legendView);

            detailContentView = mRootView.findViewById(R.id.detailContent);
            mRootView.findViewById(R.id.btnPrevImg).setOnClickListener(this);
            mRootView.findViewById(R.id.btnNextImg).setOnClickListener(this);

            ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Utils.showKeyboard(mContext, (AutoCompleteTextView)mRootView.findViewById(R.id.editSearch), false);
                    doSearch();
                }
            });
            ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_SEARCH)
                    {
                        String strSearch = ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).getText().toString();
                        if(TextUtils.isEmpty(strSearch))
                        {
                            //doSearch();
                        }
                        else
                        {
                            if(legendTextArray.contains(strSearch))
                            {
                                doSearch();
                            }
                            else
                            {
                                ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setText("");
                                Toast.makeText(mContext, "Please select correct legend text.", Toast.LENGTH_LONG).show();
                            }
                        }
                        Utils.showKeyboard(mContext, (AutoCompleteTextView)mRootView.findViewById(R.id.editSearch), false);
                    }
                    return true;
                }
            });

            String strArray[] = getResources().getStringArray(R.array.lang_text);
            ((TextView) mRootView.findViewById(R.id.txtLanguage)).setText(strArray[mLanguageIdx]);
            ((TextView) mRootView.findViewById(R.id.txtLanguage1)).setText(strArray[mLanguageIdx]);

            loadData();
        }

        showBackButton(true);
        /*Date date = new Date();
        try {
            Date expireDate = new SimpleDateFormat("yyyy-MM-dd").parse("2019-09-30");
            if(date.compareTo(expireDate) > 0)
            {
                //Toast.makeText(this, "Your product expires date. Please contact developer. Thank you.", Toast.LENGTH_LONG).show();
                mContext.finish();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                refreshBrightController1Value();
                refreshBrightControllerValue();
                refreshContrastController1Value();
                refreshContrastControllerValue();
            }
        });
        return mRootView;
    }

    private void refreshBrightControllerValue()
    {
        int nMax = mRootView.findViewById(R.id.brightnessTrack).getHeight();
        int y = (int)(nMax - (brightness + 100) * nMax / 200.0f);
        ImageView imgController = mRootView.findViewById(R.id.imgBrightness);
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams)
                imgController.getLayoutParams();
        lParams.topMargin = y;
        lParams.rightMargin = 0;
        lParams.bottomMargin = 0;
        imgController.setLayoutParams(lParams);
    }

    private void refreshContrastControllerValue()
    {
        int nMax = mRootView.findViewById(R.id.brightnessTrack).getHeight();
        int y = (int)(nMax - (contrast + 100) * nMax / 200.0f);
        ImageView imgController = mRootView.findViewById(R.id.imgContrast);
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams)
                imgController.getLayoutParams();
        lParams.topMargin = y;
        lParams.rightMargin = 0;
        lParams.bottomMargin = 0;
        imgController.setLayoutParams(lParams);
    }

    private void refreshBrightController1Value()
    {
        int nMax = mRootView.findViewById(R.id.brightnessTrack1).getWidth();
        if(nMax == 0)
        {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshBrightController1Value();
                }
            }, 200);
        }
        int x = (int)((brightness + 100)* nMax / 200.0f);
        ImageView imgController = mRootView.findViewById(R.id.imgBrightness1);
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams)
                imgController.getLayoutParams();
        lParams.leftMargin = x;
        lParams.rightMargin = 0;
        lParams.bottomMargin = 0;
        imgController.setLayoutParams(lParams);
    }

    private void refreshContrastController1Value()
    {
        int nMax = mRootView.findViewById(R.id.brightnessTrack1).getWidth();
        if(nMax == 0)
        {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshContrastController1Value();
                }
            }, 200);
        }
        int x = (int)((contrast + 100)* nMax / 200.0f);
        ImageView imgController = mRootView.findViewById(R.id.imgContrast1);
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams)
                imgController.getLayoutParams();
        lParams.leftMargin = x;
        lParams.rightMargin = 0;
        lParams.bottomMargin = 0;
        imgController.setLayoutParams(lParams);
    }

    private void refreshBrightContrast()
    {
        ((TextView)mRootView.findViewById(R.id.txtBrightness)).setText(String.format("%d%%", brightness));
        ((TextView)mRootView.findViewById(R.id.txtContrast)).setText(String.format("%d%%", contrast));
    }

    private void refreshImage()
    {
        if(mSelProjectImageList.size() == 0)
        {
            if(!TextUtils.isEmpty(strSearchText))
            {
                ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setText("");
            }
            else
            {
                doSearch();
            }
            return;
        }
        List<ProjectImage> projectImageList = mSelProjectImageList;
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                mSeekBar.getLayoutParams();
        int x = lParams.leftMargin;
        double position = x * 1.0f / (mRootView.findViewById(R.id.layoutImageBar).getWidth() - mSeekBar.getWidth());
        int nImgIdx = Utils.getIntValueFromDouble(position * projectImageList.size()) - 1;
        if(nImgIdx < 0) nImgIdx = 0;
        if(nImgIdx >= projectImageList.size())
            nImgIdx = projectImageList.size() - 1;

        mSelImgIdx = nImgIdx;
        drawImage();
    }

    private void initLegendView()
    {
        LinearLayout leftPanel = mRootView.findViewById(R.id.layoutLeftTextPanel);
        LinearLayout rightPanel = mRootView.findViewById(R.id.layoutRightTextPanel);
        int nHeight = leftPanel.getHeight();
        int nItemHeight = leftPanel.findViewById(R.id.layoutLeftItem).getHeight();
        nRowCount = nHeight / nItemHeight;

        LayoutInflater inflater = null;
        try
        {
            inflater = getLayoutInflater();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(inflater == null)
        {
            nRowCount = 0;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initLegendView();
                }
            }, 500);
        }
        else
        {
            leftPanel.removeAllViews();
            for(int i = 0; i < nRowCount; i++)
            {
                View view = inflater.inflate(R.layout.item_left_panel, null, false);
                ((EllipsizingTextView)view.findViewById(R.id.text)).setMaxLines(2);
                Utils.doApplyAllFontForTextView(mContext, view);
                mLeftTextViewList.add(view);
                view.setVisibility(View.GONE);
                leftPanel.addView(view);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)view.getLayoutParams();
                params.weight = 1;
                params.gravity = Gravity.CENTER_VERTICAL;
                view.setLayoutParams(params);
                view.setOnClickListener(mLegendClickEvent);
            }
            for(int i = 0; i < nRowCount; i++)
            {
                View view = inflater.inflate(R.layout.item_right_panel, null, false);
                ((EllipsizingTextView)view.findViewById(R.id.text)).setMaxLines(2);
                Utils.doApplyAllFontForTextView(mContext, view);
                mRightTextViewList.add(view);
                view.setVisibility(View.GONE);
                rightPanel.addView(view);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)view.getLayoutParams();
                params.weight = 1;
                params.gravity = Gravity.CENTER_VERTICAL;
                view.setLayoutParams(params);
                view.setOnClickListener(mLegendClickEvent);
            }
        }

    }

    private byte[] getBitmapData(String path)
    {
        String filePath = path.substring(0, path.length() - 4) + ".oms";
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream buf = new BufferedInputStream(fis);
            buf.read(bytes, 0, bytes.length);
            buf.close();
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    private void drawImage()
    {
        if(nRowCount == 0)
        {
            initLegendView();
        }
        List<ProjectImage> projectImageList = mSelProjectImageList;
        ((TextView)mRootView.findViewById(R.id.txtProgressBar)).setText(String.format("%d/%d", mSelImgIdx + 1, projectImageList.size()));
        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            byte[] data = getBitmapData(Global.IMAGE_ROOT_PATH + projectImageList.get(mSelImgIdx).url);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 1, data.length - 1, options);
            System.gc();
            mImageWidth = bitmap.getWidth();
            mImageHeight = bitmap.getHeight();
            bitmap = Utils.enhanceImage(bitmap, 1.0f + 1.0f / 100.0f * contrast, 0f + (128.0f / 100.0f * brightness));
            bitmap = Utils.verticalHorizontalFlip(bitmap, horz_flip, vertical_flip);

            int nPanelWidth = mRootView.findViewById(R.id.image).getWidth();
            int nPanelHeight = mRootView.findViewById(R.id.image).getHeight();
            int nLetterWidth = mRootView.findViewById(R.id.panelLeftLegend).getWidth();
            int nRealPanelWidth = nPanelWidth - nLetterWidth * 2;

            float nWeight = Math.min(nRealPanelWidth * 1.0f / mImageWidth, nPanelHeight * 1.0f / mImageHeight);
            int nLeft = nLetterWidth + ((int)((nRealPanelWidth - mImageWidth * nWeight) / 2.0f));
            int nRight = nLeft + (int)(mImageWidth * nWeight);
            int nTop = (int)((nPanelHeight - mImageHeight * nWeight) / 2.0f);
            int nBottom = nTop + (int)(mImageHeight * nWeight);

            Bitmap mPanelBitmap = Bitmap.createBitmap(nPanelWidth, nPanelHeight, Bitmap.Config.ARGB_8888);
            System.gc();
            Canvas canvas = new Canvas(mPanelBitmap);
            Paint paint = new Paint();
            canvas.drawBitmap(bitmap, new Rect(0, 0, mImageWidth, mImageHeight), new Rect(nLeft, nTop, nRight, nBottom), paint);
            bitmap.recycle();
            System.gc();
            Bitmap oldBmp = (Bitmap)((ZoomImageView)mRootView.findViewById(R.id.image)).getTag();
            if(oldBmp != null && !oldBmp.isRecycled())
            {
                Utils.bitmapList.remove(oldBmp);
                oldBmp.recycle();
            }
            ((ZoomImageView)mRootView.findViewById(R.id.image)).setImageBitmap(mPanelBitmap);
            ((ZoomImageView)mRootView.findViewById(R.id.image)).setTag(mPanelBitmap);

            Utils.bitmapList.add(mPanelBitmap);

            ((ZoomImageView)mRootView.findViewById(R.id.image)).setMinZoom(1.0f, ZoomApi.TYPE_REAL_ZOOM);
            ((ZoomImageView)mRootView.findViewById(R.id.image)).setMaxZoom(10.0f, ZoomApi.TYPE_REAL_ZOOM);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadLegends(boolean bShowText)
    {
        if(mSelImgIdx >= mSelProjectImageList.size()) return;
        if(mSelImgIdx < 0 ) return;
        legendView.resetValues();

        if(nDisplayMode == 2)
        {
            for(int i = 0; i < nRowCount; i++)
            {
                mLeftTextViewList.get(i).setVisibility(View.GONE);
                mRightTextViewList.get(i).setVisibility(View.GONE);
            }

            legendView.invalidate();
            return;
        }

        int nLegendTextPanelWidth = mRootView.findViewById(R.id.panelLeftLegend).getWidth();

        //Draw Point
        ProjectImage projectImage = mSelProjectImageList.get(mSelImgIdx);
        int nPanelWidth = mRootView.findViewById(R.id.legendView).getWidth();
        int nPanelHeight = mRootView.findViewById(R.id.legendView).getHeight();
        int nPanelRealWidth = nPanelWidth - mRootView.findViewById(R.id.panelLeftLegend).getWidth() * 2;
        int nPanelRealHeight = nPanelHeight;
        float ratio = Math.min(nPanelRealWidth * 1.0f / mImageWidth, nPanelRealHeight * 1.0f / mImageHeight);
        int nLeftSpace = (int)(mRootView.findViewById(R.id.panelLeftLegend).getWidth() + (nPanelRealWidth - mImageWidth * ratio) / 2);
        int nTopSpace = (int)((nPanelRealHeight - mImageHeight * ratio) / 2);
        int nViewTop = (int)zoomEngine.getPanY();
        int nViewLeft = (int)zoomEngine.getPanX();
        mScaleFactor = zoomEngine.getRealZoom();
        List<PanelPoint> leftPoints = new ArrayList<>();
        List<PanelPoint> rightPoints = new ArrayList<>();
        Map<String, LegendRegion> structureRegion = new HashMap<>();
        Map<String, LegendPoint> structurePoint = new HashMap<>();
        PanelPoint selPanelPoint = null;
        double distance = 0.0f;
        boolean bLeftSel = false;

        for(String key : projectImage.legends.keySet())
        {
            try
            {
                double percentWidth = projectImage.legends.get(key).percentWidth;
                double percentHeight = projectImage.legends.get(key).percentHeight;
                if(vertical_flip)
                {
                    percentHeight = 1.0f - percentHeight;
                }
                if(horz_flip)
                {
                    percentWidth = 1.0f - percentWidth;
                }
                if(percentWidth < 0 || percentWidth > 1) continue;
                if(percentHeight < 0 || percentHeight > 1) continue;
                int x = (int)(mImageWidth * percentWidth * ratio) + nLeftSpace;
                int y = (int)(mImageHeight * percentHeight * ratio) + nTopSpace;
                x = (int)((x + nViewLeft) * mScaleFactor);
                y = (int)((y + nViewTop) * mScaleFactor );
                String legendID = key;
                String structureID = mLegendMap.get(legendID).structure;

                //Check Structure
                String legendText = mLegendMap.get(legendID).text.get(langKeys[mLanguageIdx]);
                if(TextUtils.isEmpty(strSearchText) || !legendText.equals(strSearchText))
                {
                    if(!selStructureIDList.get(0).booleanValue())
                    {
                        boolean bExist = false;
                        for(int i = 1; i < selStructureIDList.size(); i++)
                        {
                            if(selStructureIDList.get(i).booleanValue() && mStructureMap.get(structureID).text.equals(mStructures.get(i)))
                            {
                                bExist = true;
                                break;
                            }
                        }
                        if(!bExist)
                        {
                            continue;
                        }
                    }
                }

                String color;
                if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                {
                    if(mStructureMap.get(structureID) == null)
                    {
                        color = "white";
                    }
                    else
                    {
                        color = mStructureMap.get(structureID).color;
                    }
                }
                else
                {
                    if(mStructureMap.get(structureID) == null)
                    {
                        color = "other" + "white";
                    }
                    else
                    {
                        color = "other" + mStructureMap.get(structureID).color;
                    }

                }

                if(x > 0 && y > 0 && x < nPanelWidth && y < nPanelHeight)
                {
                    if(x > nLegendTextPanelWidth && x < nPanelWidth - nLegendTextPanelWidth)
                    {
                        PanelPoint ppt = new PanelPoint(x, y, mStructureMap.get(structureID), mLegendMap.get(legendID));
                        double curDistance = ppt.distance(mCurSelXPos, mCurSelYPos);
                        if(selPanelPoint == null || curDistance < distance)
                        {
                            selPanelPoint = ppt;
                            distance = curDistance;
                            bLeftSel = (x <= nPanelWidth / 2);
                        }
                        if(x <= nPanelWidth / 2)
                        {
                            leftPoints.add(ppt);
                        }
                        else
                        {
                            rightPoints.add(ppt);
                        }
                    }
                    structurePoint.put(key, legendView.addPoint(x, y, color));
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        //Draw Structures
        for(String key : mStructureMap.keySet())
        {
            ProjectStructure projectStructure = mStructureMap.get(key);
            List<List<Double>> regionValues = projectStructure.draw.get(projectImage.id);
            if(regionValues == null || regionValues.size() == 0) continue;
            if(!selStructureIDList.get(0).booleanValue())
            {
                boolean bExist = false;
                for(int i = 1; i < selStructureIDList.size(); i++)
                {
                    if(selStructureIDList.get(i).booleanValue() && projectStructure.text.equals(mStructures.get(i)))
                    {
                        bExist = true;
                        break;
                    }
                }
                if(!bExist)
                {
                    continue;
                }
            }

            LegendRegion legendRegion = new LegendRegion();
            legendRegion.color = projectStructure.color;
            for(int i = 0; i < regionValues.size(); i++) {
                List<Double> regionValue = regionValues.get(i);
                List<Double> regionPrevValue, regionAfterValue;
                if (i == 0) {
                    regionPrevValue = regionValues.get(regionValues.size() - 1);
                } else {
                    regionPrevValue = regionValues.get(i - 1);
                }
                if (i == regionValues.size() - 1) {
                    regionAfterValue = regionValues.get(0);
                } else {
                    regionAfterValue = regionValues.get(i + 1);
                }

                if (!isPossibleToAddPoint(regionPrevValue, regionValue, regionAfterValue)) {
                    continue;
                }

                double percentWidth = regionValue.get(0).doubleValue();
                double percentHeight = regionValue.get(1).doubleValue();
                if (vertical_flip) {
                    percentHeight = 1.0f - percentHeight;
                }
                if (horz_flip) {
                    percentWidth = 1.0f - percentWidth;
                }
                float x1, y1;
                x1 = (float) ((mImageWidth * percentWidth * ratio) + nLeftSpace);
                y1 = (float) ((mImageHeight * percentHeight * ratio) + nTopSpace);
                x1 = (x1 + nViewLeft) * mScaleFactor;
                y1 = (y1 + nViewTop) * mScaleFactor;

                legendRegion.pointList.add(new LegendPoint(x1, y1, legendRegion.color));
            }
            if(legendRegion.pointList.size() > 0)
            {
                legendView.addRegion(legendRegion);
                structureRegion.put(key, legendRegion);
            }
        }

        Collections.sort(leftPoints, new Comparator<PanelPoint>() {
            @Override
            public int compare(PanelPoint panelPoint, PanelPoint t1) {
                return panelPoint.y - t1.y;
            }
        });
        Collections.sort(rightPoints, new Comparator<PanelPoint>() {
            @Override
            public int compare(PanelPoint panelPoint, PanelPoint t1) {
                return panelPoint.y - t1.y;
            }
        });


        if(bLeftSel && selPanelPoint != null)
        {
            leftPoints.remove(selPanelPoint);
            leftPoints.add(0,selPanelPoint);
        }

        leftPoints = filterPoints(leftPoints);

        if(!bLeftSel && selPanelPoint != null)
        {
            rightPoints.remove(selPanelPoint);
            rightPoints.add(0,selPanelPoint);
        }
        rightPoints = filterPoints(rightPoints);

        int nCount = Math.min(leftPoints.size(), nRowCount);
        for(int i = 0; i < nCount; i++)
        {
            if(mRainbowIdx == 0)
            {
                //legendView.addPoint(leftPoints.get(i).x, leftPoints.get(i).y, rainbowColors[i % rainbowColors.length]);
                String legendText = mLegendMap.get(leftPoints.get(i).legend.id).text.get(langKeys[mLanguageIdx]);

                LegendPoint legendPoint = structurePoint.get(leftPoints.get(i).legend.id);
                if(legendPoint != null)
                {
                    if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                    {
                        legendPoint.color = rainbowColors[i % rainbowColors.length];
                    }
                    else
                    {
                        legendPoint.color = "other" + rainbowColors[i % rainbowColors.length];
                    }
                }

                LegendRegion region = structureRegion.get(leftPoints.get(i).legend.id);
                if(region != null)
                {
                    if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                    {
                        region.color = rainbowColors[i % rainbowColors.length];
                    }
                    else
                    {
                        region.color = "other" + rainbowColors[i % rainbowColors.length];
                    }
                    for(LegendPoint point : region.pointList)
                    {
                        if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                        {
                            point.color = rainbowColors[i % rainbowColors.length];
                        }
                        else
                        {
                            point.color = "other" + rainbowColors[i % rainbowColors.length];
                        }
                    }
                }
            }
            if(bShowText != false || this.bShowText == true)
            {
                String legendText = mLegendMap.get(leftPoints.get(i).legend.id).text.get(langKeys[mLanguageIdx]);
                float top = (nPanelHeight * 1.0f / nCount) * i;
                float bottom = (nPanelHeight * 1.0f / nCount) * (i + 1);
                float x = nLegendTextPanelWidth;
                float y = top + (bottom - top) / 2.0f;
                String color;
                if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                {
                    if(mRainbowIdx == 0)
                    {
                        color = rainbowColors[i % rainbowColors.length];
                    }
                    else
                    {
                        if(leftPoints.get(i).structure != null)
                        {
                            color = leftPoints.get(i).structure.color;
                        }
                        else
                        {
                            color = "white";
                        }
                    }
                }
                else
                {
                    if(mRainbowIdx == 0)
                    {
                        color = "other" + rainbowColors[i % rainbowColors.length];
                    }
                    else
                    {
                        if(leftPoints.get(i).structure != null)
                        {
                            color = "other" + leftPoints.get(i).structure.color;
                        }
                        else
                        {
                            color = "other" + "white";
                        }
                    }
                }
                legendView.addLine(leftPoints.get(i).x, leftPoints.get(i).y, x, y, color);

                mLeftTextViewList.get(i).setVisibility(View.VISIBLE);
                ((TextView)mLeftTextViewList.get(i).findViewById(R.id.text)).setText(leftPoints.get(i).legend.text.get(langKeys[mLanguageIdx]));
                int nColor = legendView.getColor(color);

                if(nDisplayMode == 0)
                {
                    ((TextView)mLeftTextViewList.get(i).findViewById(R.id.text)).setTextColor(nColor);
                }
                else
                {
                    ((TextView)mLeftTextViewList.get(i).findViewById(R.id.text)).setTextColor(getResources().getColor(R.color.transparent));
                }
                mLeftTextViewList.get(i).findViewById(R.id.line1).setBackgroundColor(nColor);
                mLeftTextViewList.get(i).findViewById(R.id.line2).setBackgroundColor(nColor);
                mLeftTextViewList.get(i).setTag(leftPoints.get(i).legend);
            }
            else
            {
                mLeftTextViewList.get(i).setVisibility(View.GONE);
            }
        }

        for(int i = nCount; i < nRowCount; i++)
        {
            mLeftTextViewList.get(i).setVisibility(View.GONE);
        }

        nCount = Math.min(rightPoints.size(), nRowCount);
        for(int i = 0; i < nCount; i++)
        {
            String legendText = mLegendMap.get(rightPoints.get(i).legend.id).text.get(langKeys[mLanguageIdx]);
            if(mRainbowIdx == 0)
            {
                LegendPoint legendPoint = structurePoint.get(rightPoints.get(i).legend.id);
                String color;
                if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                {
                    color = rainbowColors[i % rainbowColors.length];
                }
                else
                {
                    color = "other" + rainbowColors[i % rainbowColors.length];
                }
                if(legendPoint != null)
                {
                    legendPoint.color = color;
                }
                LegendRegion region = structureRegion.get(rightPoints.get(i).legend.id);
                if(region != null)
                {
                    region.color = color;
                    for(LegendPoint point : region.pointList)
                    {
                        point.color = color;
                    }
                }
            }
            if(bShowText)
            {
                float top = (nPanelHeight * 1.0f / nCount) * i;
                float bottom = (nPanelHeight * 1.0f / nCount) * (i + 1);
                float x = nPanelWidth - nLegendTextPanelWidth;
                float y = top + (bottom - top) / 2;
                String color ;
                if(TextUtils.isEmpty(strSearchText) || strSearchText.equals(legendText))
                {
                    if(mRainbowIdx == 0)
                    {
                        color = rainbowColors[i % rainbowColors.length];
                    }
                    else
                    {
                        if(rightPoints.get(i).structure != null)
                        {
                            color = rightPoints.get(i).structure.color;
                        }
                        else
                        {
                            color = "white";
                        }
                    }
                }
                else
                {
                    if(mRainbowIdx == 0)
                    {
                        color = "other" + rainbowColors[i % rainbowColors.length];
                    }
                    else
                    {
                        if(rightPoints.get(i).structure != null)
                        {
                            color = "other" + rightPoints.get(i).structure.color;
                        }
                        else
                        {
                            color = "other" + "white";
                        }

                    }
                }
                legendView.addLine(rightPoints.get(i).x, rightPoints.get(i).y, x, y, color);

                mRightTextViewList.get(i).setVisibility(View.VISIBLE);
                ((TextView)mRightTextViewList.get(i).findViewById(R.id.text)).setText(rightPoints.get(i).legend.text.get(langKeys[mLanguageIdx]));
                int nColor = legendView.getColor(color);
                if(nDisplayMode == 0)
                {
                    ((TextView)mRightTextViewList.get(i).findViewById(R.id.text)).setTextColor(nColor);
                }
                else
                {
                    ((TextView)mRightTextViewList.get(i).findViewById(R.id.text)).setTextColor(getResources().getColor(R.color.transparent));
                }
                mRightTextViewList.get(i).findViewById(R.id.line1).setBackgroundColor(nColor);
                mRightTextViewList.get(i).findViewById(R.id.line2).setBackgroundColor(nColor);
                mRightTextViewList.get(i).setTag(rightPoints.get(i).legend);
            }
            else
            {
                mRightTextViewList.get(i).setVisibility(View.GONE);
            }
        }

        for(int i = nCount; i < nRowCount; i++)
        {
            mRightTextViewList.get(i).setVisibility(View.GONE);
        }

        legendView.invalidate();
    }

    private boolean isPossibleToAddPoint(List<Double> prev, List<Double> current, List<Double> after)
    {
        double nDistance1 = Math.pow(prev.get(0) - current.get(0), 2.0f) + Math.pow(prev.get(1) - current.get(1), 2.0f);
        double nDistance2 = Math.pow(after.get(0) - current.get(0), 2.0f) + Math.pow(after.get(1) - current.get(1), 2.0f);
        double nDistance3 = Math.pow(after.get(0) - prev.get(0), 2.0f) + Math.pow(after.get(1) - prev.get(1), 2.0f);

        return nDistance1 < nDistance3 && nDistance2 < nDistance3;
    }

    private List<PanelPoint> filterPoints(List<PanelPoint> points)
    {
        if(points.size() < nRowCount) return points;

        if((mProjectInfo.free.equals("free") || Utils.isValidUser(mContext)) &&
                !TextUtils.isEmpty(strSearchText))
        {
            PanelPoint searchPoint = null;
            int nIdx = 0;
            for(int i = 0; i < points.size(); i++)
            {
                if(!TextUtils.isEmpty(strSearchText) && points.get(i).legend.text.get(langKeys[mLanguageIdx]).equals(strSearchText))
                {
                    searchPoint = points.get(i);
                    nIdx = i;
                    break;
                }
            }
            if(nIdx < nRowCount)
            {
                return points.subList(0, nRowCount);
            }
            else
            {
                List<PanelPoint> newPanelList = points.subList(0, nRowCount - 1);
                newPanelList.add(searchPoint);
                return newPanelList;
            }
        }
        else
        {
            return points.subList(0, nRowCount);
        }
    }

    private void loadData()
    {
        mContext.showProgressDialog("Loading...");
        new Thread()
        {
            @Override
            public void run() {
                mProjectContent = Utils.getProjectContent(mProjectInfo.id);
                if(mProjectContent == null)
                {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideProgressDialog();
                            mContext.onBackPressed();
                        }
                    });
                    return;
                }

                mImageMap = mProjectContent.images;

                mSeriesMap = mProjectContent.series;

                mLegendMap = mProjectContent.legends;

                legendTextArray = new ArrayList<>();
                Map<String, Integer> legendCountMap = new HashMap<>();
                for(String key : mLegendMap.keySet()){
                    ProjectLegend legend = mLegendMap.get(key);
                    legendTextArray.add(legend.text.get(langKeys[mLanguageIdx]));
                    if(!TextUtils.isEmpty(legend.structure))
                    {
                        Integer value = legendCountMap.get(legend.structure);
                        if(value == null)
                        {
                            value = 0;
                        }
                        value++;
                        legendCountMap.put(legend.structure, value);
                    }
                    if(!mProjectInfo.free.equals("free") && !Utils.isValidUser(mContext) && !Global.offline)
                    {
                        for(String stringKey : legend.description.keySet())
                        {
                            legend.description.put(stringKey, getResources().getString(R.string.login_to_view_anotomy));
                        }
                        for(String stringKey : legend.text.keySet())
                        {
                            legend.text.put(stringKey, getResources().getString(R.string.login_to_view_anotomy));
                        }
                    }
                }

                Collections.sort(legendTextArray, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });

                mStructureMap = mProjectContent.structures;
                mStructures.add("All");
                for(String structure : mStructureMap.keySet())
                {
                    Integer nLegendCount = legendCountMap.get(structure);
                    if(nLegendCount == null) nLegendCount = 0;
                    if(nLegendCount > 0)
                    {
                        mStructures.add(mStructureMap.get(structure).text);
                    }
                }

                mSeries.clear();
                List<String> seriesList = new ArrayList<>();
                for(String imageID : mImageMap.keySet())
                {
                    ProjectImage projectImage = mImageMap.get(imageID);
                    String series = null;
                    if(!TextUtils.isEmpty(projectImage.series))
                    {
                        ProjectSeries projectSeries = mSeriesMap.get(projectImage.series);
                        if(projectSeries != null)
                        {
                            series = projectSeries.text;
                        }
                    }
                    else
                    {
                        series = "Default";
                    }
                    if(series != null)
                    {
                        List<ProjectImage> imageList = mProjectImageList.get(series);
                        if( imageList == null )
                        {
                            imageList = new ArrayList<>();
                            mProjectImageList.put(series, imageList);
                            seriesList.add(series);
                        }
                        imageList.add(projectImage);
                    }
                }

                for(String series : seriesList)
                {
                    if(mProjectImageList.get(series).size() > 0)
                    {
                        mSeries.add(series);
                    }
                }


                for(String series : mProjectImageList.keySet())
                {
                    final List<ProjectImage> imageList = mProjectImageList.get(series);
                    Collections.sort(imageList, new Comparator<ProjectImage>() {
                        @Override
                        public int compare(ProjectImage projectImage, ProjectImage t1) {
                            int nValue1 = Utils.parseIntFromString(projectImage.position);
                            int nValue2 = Utils.parseIntFromString(t1.position);
                            if(!String.valueOf(nValue1).equals(projectImage.position))
                            {
                                nValue1 = imageList.size() + 1;
                            }
                            if(!String.valueOf(nValue2).equals(t1.position))
                            {
                                nValue2 = imageList.size() + 1;
                            }
                            return nValue1 - nValue2;
                        }
                    });
                }


                for(int i = 0; i < mStructures.size(); i++)
                {
                    selStructureIDList.put(i, true);
                }
                mSelProjectImageList.clear();
                mSelProjectImageList.addAll(mProjectImageList.get(mSeries.get(mSeriesIdx)));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshStructureView();

                        refreshBrightController1Value();
                        refreshContrastController1Value();
                        refreshSearchBar();
                        if (getArguments() != null) {
                            strSearchText = getArguments().getString("searchKey");
                        }
                        ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setText(strSearchText);
                        mContext.hideProgressDialog();
                        doSearch();
                    }
                });
            }
        }.start();

    }

    private List<ProjectImage> getProjectImageList()
    {
        List<ProjectImage> allImageList = mProjectImageList.get(mSeries.get(mSeriesIdx));
        //String structure = mStructures.get(mStructureIdx);
        List<ProjectImage> projectImageList = new ArrayList<>();
        for(ProjectImage projectImage : allImageList)
        {
            if(!TextUtils.isEmpty(strSearchText))
            {
                for(String key : projectImage.legends.keySet())
                {
                    ProjectLegendInfo legendInfo = projectImage.legends.get(key);
                    ProjectLegend legend = mLegendMap.get(legendInfo.id);
                    if(legend.text.get(langKeys[mLanguageIdx]).equals(strSearchText))
                    {
                        projectImageList.add(projectImage);
                        break;
                    }

                    /*if(selStructureIDList.get(0).booleanValue())
                    {
                        if(legend.text.get(langKeys[mLanguageIdx]).equals(strSearchText))
                        {
                            projectImageList.add(projectImage);
                            break;
                        }
                    }
                    else
                    {
                        boolean bExist = false;
                        for(int i = 1; i < selStructureIDList.size(); i++)
                        {
                            if(selStructureIDList.get(i).booleanValue() && mStructureMap.get(legend.structure).text.equals(mStructures.get(i)))
                            {
                                bExist = true;
                                break;
                            }
                        }
                        if(bExist)
                        {
                            if(legend.text.get(langKeys[mLanguageIdx]).equals(strSearchText))
                            {
                                projectImageList.add(projectImage);
                                break;
                            }
                        }
                    }*/
                }
            }
            else
            {
                projectImageList.add(projectImage);
                /*if(selStructureIDList.get(0).booleanValue())
                {
                    projectImageList.add(projectImage);
                }
                else
                {
                    boolean bExist = false;
                    for(String key : projectImage.legends.keySet())
                    {
                        ProjectLegendInfo legendInfo = projectImage.legends.get(key);
                        ProjectLegend legend = mLegendMap.get(legendInfo.id);
                        for(int i = 1; i < selStructureIDList.size(); i++)
                        {
                            if(selStructureIDList.get(i).booleanValue() && mStructureMap.get(legend.structure).text.equals(mStructures.get(i)))
                            {
                                bExist = true;
                                break;
                            }
                        }
                        if(bExist) break;
                    }
                    if(bExist)
                    {
                        projectImageList.add(projectImage);
                    }
                }*/
            }
        }
        return projectImageList;
    }

    public void loadSeries()
    {
        //Load Thumbnail using Picasso
        List<ProjectImage> projectImageList = mSelProjectImageList;
        int nThumbCount = Math.min(thumbImageIdx.length, projectImageList.size());
        for(int i = 0; i < nThumbCount; i++)
        {
            int nImgIdx = Utils.getImageIndexForThumb(i, projectImageList.size());
            try
            {
                //Picasso.get().load(new File(Global.IMAGE_ROOT_PATH + projectImageList.get(nImgIdx).url)).into((ImageView)mRootView.findViewById(thumbImageIdx[i]));
                byte[] data = getBitmapData(Global.IMAGE_ROOT_PATH + projectImageList.get(nImgIdx).url);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPurgeable = true;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 1, data.length - 1, options);
                System.gc();
                Bitmap oldBmp = (Bitmap)((ImageView)mRootView.findViewById(thumbImageIdx[i])).getTag();
                if(oldBmp != null && !oldBmp.isRecycled())
                {
                    Utils.bitmapList.remove(oldBmp);
                    oldBmp.recycle();
                }
                ((ImageView)mRootView.findViewById(thumbImageIdx[i])).setImageBitmap(bitmap);
                ((ImageView)mRootView.findViewById(thumbImageIdx[i])).setTag(bitmap);
                Utils.bitmapList.add(bitmap);
                mRootView.findViewById(thumbImageIdx[i]).setVisibility(View.VISIBLE);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        for(int i = nThumbCount; i < thumbImageIdx.length; i++){
            mRootView.findViewById(thumbImageIdx[i]).setVisibility(View.GONE);
        }

        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mRootView.findViewById(R.id.layoutImageBarContainer).getLayoutParams();
        params.weight = nThumbCount;
        mRootView.findViewById(R.id.layoutImageBarContainer).setLayoutParams(params);

        params = (LinearLayout.LayoutParams)mRootView.findViewById(R.id.imgSpace).getLayoutParams();
        params.weight = thumbImageIdx.length - nThumbCount;
        mRootView.findViewById(R.id.imgSpace).setLayoutParams(params);*/

        ((TextView)mRootView.findViewById(R.id.txtView)).setText(mSeries.get(mSeriesIdx));
        ((TextView)mRootView.findViewById(R.id.txtView1)).setText(mSeries.get(mSeriesIdx));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshImage();
                bShowText = true;
                loadLegends(true);
            }
        }, 500);


    }

    public void setProjectInfo(ProjectInfo projectInfo)
    {
        mProjectInfo = projectInfo;
    }

    public void doSearch()
    {
        strSearchText = ((TextView)mRootView.findViewById(R.id.editSearch)).getText().toString();

        if(TextUtils.isEmpty(strSearchText))
        {
            mRootView.findViewById(R.id.btnResetSearch).setVisibility(View.GONE);
        }
        else
        {
            mRootView.findViewById(R.id.btnResetSearch).setVisibility(View.VISIBLE);
        }

        ((ZoomImageView)mRootView.findViewById(R.id.image)).zoomTo(1.0f, true);
        mContext.showProgressDialog("Searching...");
        new Thread()
        {
            @Override
            public void run() {
                mSelProjectImageList.clear();
                mSelProjectImageList.addAll(getProjectImageList());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.hideProgressDialog();
                        loadSeries();
                    }
                });
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnStructure:
                doClickStructure();
                break;
            case R.id.btnBrightness:
                doClickBrightness();
                break;
            case R.id.btnContrast:
                doClickContrast();
                break;
            case R.id.btnSetting:
                doClickSetting();
                break;
            case R.id.btnSettingClose: case R.id.btnSettingClose1:
                doClickSettingClose();
                break;
            case R.id.btnHorzSlip:
                doHorzSlip();
                break;
            case R.id.btnVertSlip:
                doVertSlip();
                break;
            case R.id.layoutRainbow: case R.id.btnRainbowLeft: case R.id.btnRainbowRight:
                doClickRainbow();
                break;
            case R.id.layoutView: case R.id.btnViewLeft: case R.id.btnViewRight:
                doClickView();
                break;
            case R.id.btnView:
                doClickView();
                break;
            case R.id.layoutStructures: case R.id.imgStructures:
                doClickSettingsStructure();
                break;
            case R.id.layoutLanguage: case R.id.imgLanguage:
                doClickLanguage();
                break;
            case R.id.layoutLanguage1: case R.id.imgLanguage1:
                doClickLanguage();
                break;
            case R.id.btnDetailClose: /*case R.id.btnDetailClose1:*/
                doClickDetailClose();
                break;
            case R.id.btnPrevImg:
                doClickPrevImg();
                break;
            case R.id.btnNextImg:
                doClickNextImg();
                break;
        }
    }

    private void doClickPrevImg()
    {
        mSelImgIdx--;
        List<ProjectImage> projectImageList = mSelProjectImageList;
        if(mSelImgIdx < 0 ) mSelImgIdx = 0;
        int x = (int)((mSelImgIdx * 1.0f * (mRootView.findViewById(R.id.layoutImageBar).getWidth() - mSeekBar.getWidth())) / projectImageList.size());
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                mSeekBar.getLayoutParams();
        lParams.leftMargin = x;
        lParams.rightMargin = 0;
        lParams.bottomMargin = 0;
        mSeekBar.setLayoutParams(lParams);
        drawImage();
        bShowText = true;
        loadLegends(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLegends(true);
            }
        }, 500);
    }

    private void doClickNextImg()
    {
        mSelImgIdx++;
        List<ProjectImage> projectImageList = mSelProjectImageList;
        if(mSelImgIdx >= projectImageList.size()) mSelImgIdx = projectImageList.size() - 1;
        int x = (int)((mSelImgIdx * 1.0f * (mRootView.findViewById(R.id.layoutImageBar).getWidth() - mSeekBar.getWidth())) / projectImageList.size());
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                mSeekBar.getLayoutParams();
        lParams.leftMargin = x;
        lParams.rightMargin = 0;
        lParams.bottomMargin = 0;
        mSeekBar.setLayoutParams(lParams);
        drawImage();
        bShowText = true;
        loadLegends(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLegends(true);
            }
        }, 500);
    }

    private void doHorzSlip()
    {
        horz_flip = !horz_flip;
        drawImage();
        bShowText = true;
        loadLegends(true);
    }

    private void doVertSlip()
    {
        vertical_flip = !vertical_flip;
        drawImage();
        bShowText = true;
        loadLegends(true);
    }

    private void doClickRainbow()
    {
        final String strArray[] = getResources().getStringArray(R.array.on_off);

        final int origIdx = mRainbowIdx;
        final String origText = strArray[mRainbowIdx];

        new AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.select_rainbow))
                .setSingleChoiceItems(strArray, origIdx, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        mRainbowIdx = nSelected;
                        ((TextView) mRootView.findViewById(R.id.txtRainbow)).setText(strArray[nSelected]);
                    }
                })
                .setPositiveButton("Select", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        loadLegends(true);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        mRainbowIdx = origIdx;
                        ((TextView) mRootView.findViewById(R.id.txtRainbow)).setText(origText);
                    }
                })
                .create()
                .show();
    }

    private void doClickView()
    {
        final String strArray[] = new String[mSeries.size()];

        for(int i = 0; i < mSeries.size(); i++)
        {
            strArray[i] = mSeries.get(i);
        }

        final int origIdx = mSeriesIdx;
        final String origText = strArray[mSeriesIdx];

        new AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.select_view))
                .setSingleChoiceItems(strArray, origIdx, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        mSeriesIdx = nSelected;
                        ((TextView) mRootView.findViewById(R.id.txtView)).setText(strArray[nSelected]);
                        ((TextView) mRootView.findViewById(R.id.txtView1)).setText(strArray[nSelected]);
                    }
                })
                .setPositiveButton("Select", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        doSearch();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        mSeriesIdx = origIdx;
                        ((TextView) mRootView.findViewById(R.id.txtView)).setText(origText);
                        ((TextView) mRootView.findViewById(R.id.txtView1)).setText(origText);
                    }
                })
                .create()
                .show();
    }

    private void doClickSettingsStructure()
    {
        final String strArray[] = new String[mStructures.size()];
        final boolean values[] = new boolean[mStructures.size()];
        for(int i = 0; i < mStructures.size(); i++)
        {
            strArray[i] = mStructures.get(i);
            values[i] = selStructureIDList.get(i).booleanValue();
        }

        new AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.select_structure))
                .setMultiChoiceItems(strArray, values, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id, boolean b) {
                        final ListView listView = ((AlertDialog) dialogInterface).getListView();
                        if(id == 0 && b == true)
                        {
                            values[0] = true;
                            for (int i = 1; i < listView.getCount(); i++) {
                                values[i] = true;
                                listView.setItemChecked(i, true);
                            }
                            return;
                        }
                        else if( id == 0 && b == false)
                        {
                            values[0] = false;
                            for (int i = 1; i < listView.getCount(); i++) {
                                values[i] = false;
                                listView.setItemChecked(i, false);
                            }
                            return;
                        }
                        else if(id != 0 && b == false)
                        {
                            values[0] = false;
                            values[id] = b;
                            listView.setItemChecked(0, false);
                            return;
                        }
                        else
                        {
                            values[id] = b;

                            boolean bAllSelected = true;
                            for(int i = 1; i < values.length; i++)
                            {
                                if( values[i] == false )
                                {
                                    bAllSelected = false;
                                    break;
                                }
                            }
                            if(bAllSelected)
                            {
                                values[0] = true;
                                listView.setItemChecked(0, true);
                            }
                            return;
                        }
                    }
                })
                .setPositiveButton("Select", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        //Check Selected Structure
                        /*boolean bValid = false;
                        for(int i = 0; i < values.length; i++)
                        {
                            bValid |= values[i];
                            if(bValid) break;
                        }

                        if(!bValid)
                        {
                            MessageDialog msgDialog = new MessageDialog(mContext, null);
                            msgDialog.setButtonTitle("Okay");
                            msgDialog.setTitleAndMessage("Warning", "You have to select at least one structure");
                            msgDialog.show();
                            return;
                        }*/

                        for(int i = 0; i < values.length; i++)
                        {
                            selStructureIDList.put(i, values[i]);
                        }
                        refreshStructureView();
                        loadLegends(true);
                        /*if(!TextUtils.isEmpty(strSearchText))
                        {
                            ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setText("");
                        }
                        else
                        {
                            doSearch();
                        }*/
                        //refreshSearchBar();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {

                    }
                })
                .create()
                .show();
    }

    private void refreshStructureView()
    {
        if(selStructureIDList.get(0).booleanValue())
        {
            ((TextView) mRootView.findViewById(R.id.txtStructures)).setText("All");
            ((TextView) mRootView.findViewById(R.id.txtStructures1)).setText("All");
        }
        else
        {
            String strText = "";
            int nSelCount = 0;
            for(int i = 0; i < selStructureIDList.size(); i++)
            {
                if(selStructureIDList.get(i).booleanValue())
                {
                    strText = mStructures.get(i);
                    nSelCount++;
                }
            }
            if(nSelCount > 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtStructures)).setText("Multiple");
                ((TextView) mRootView.findViewById(R.id.txtStructures1)).setText("Multiple");
            }
            else if(nSelCount == 0)
            {
                ((TextView) mRootView.findViewById(R.id.txtStructures)).setText("None");
                ((TextView) mRootView.findViewById(R.id.txtStructures1)).setText("None");
            }
            else
            {
                if(strText.length() > 8)
                {
                    strText = strText.substring(0, 5) + "...";
                }
                ((TextView) mRootView.findViewById(R.id.txtStructures)).setText(strText);
                ((TextView) mRootView.findViewById(R.id.txtStructures1)).setText(strText);
            }
        }

    }

    private void doClickLanguage()
    {
        final String strArray[] = getResources().getStringArray(R.array.lang_text);

        final int origIdx = mLanguageIdx;
        final String origText = strArray[mLanguageIdx];

        new AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.select_language))
                .setSingleChoiceItems(strArray, origIdx, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        mLanguageIdx = nSelected;
                        Utils.setIntSetting(mContext, Global.PREF_LANGAUGE, mLanguageIdx);
                        ((TextView) mRootView.findViewById(R.id.txtLanguage)).setText(strArray[nSelected]);
                        ((TextView) mRootView.findViewById(R.id.txtLanguage1)).setText(strArray[nSelected]);
                    }
                })
                .setPositiveButton("Select", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        boolean bRefresh = false;
                        if(TextUtils.isEmpty(strSearchText))
                        {
                            bRefresh = true;
                        }
                        else
                        {
                            bRefresh = false;
                        }
                        refreshSearchBar();
                        if(bRefresh)
                        {
                            doSearch();
                        }
                        //loadLegends(true);
                        //doRefreshProjectLegendDetailContent();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int nSelected)
                    {
                        mLanguageIdx = origIdx;
                        Utils.setIntSetting(mContext, Global.PREF_LANGAUGE, mLanguageIdx);
                        ((TextView) mRootView.findViewById(R.id.txtLanguage)).setText(origText);
                        ((TextView) mRootView.findViewById(R.id.txtLanguage1)).setText(origText);
                    }
                })
                .create()
                .show();
    }

    private void refreshSearchBar()
    {
        legendTextArray.clear();
        Map<String, Integer> legendMap = new HashMap<>();
        List<ProjectImage> allImageList = mProjectImageList.get(mSeries.get(mSeriesIdx));
        //String structure = mStructures.get(mStructureIdx);
        for(ProjectImage projectImage : allImageList)
        {
            for(String key : projectImage.legends.keySet())
            {
                ProjectLegendInfo legendInfo = projectImage.legends.get(key);
                ProjectLegend legend = mLegendMap.get(legendInfo.id);
                legendMap.put(legend.text.get(langKeys[mLanguageIdx]), 0);
            }
            /*if(selStructureIDList.get(0).booleanValue())
            {
                for(String key : projectImage.legends.keySet())
                {
                    ProjectLegendInfo legendInfo = projectImage.legends.get(key);
                    ProjectLegend legend = mLegendMap.get(legendInfo.id);
                    legendMap.put(legend.text.get(langKeys[mLanguageIdx]), 0);
                }
            }
            else
            {
                for(String key : projectImage.legends.keySet())
                {
                    ProjectLegendInfo legendInfo = projectImage.legends.get(key);
                    ProjectLegend legend = mLegendMap.get(legendInfo.id);
                    if(legend == null) continue;
                    if(mStructureMap.get(legend.structure) == null) continue;
                    for(int i = 1; i < selStructureIDList.size(); i++)
                    {
                        if(selStructureIDList.get(i).booleanValue() && mStructureMap.get(legend.structure).text.equals(mStructures.get(i)))
                        {
                            legendMap.put(legend.text.get(langKeys[mLanguageIdx]), 0);
                            break;
                        }
                    }
                }
            }*/
        }
        legendTextArray = new ArrayList<>();
        for(String key : legendMap.keySet())
        {
            legendTextArray.add(key);
        }
        Collections.sort(legendTextArray, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        legendSearchAdapter = new AutoCompleteArrayAdapter(mContext, 0, legendTextArray);
        ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setAdapter(legendSearchAdapter);
        ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setThreshold(1);
        ((AutoCompleteTextView)mRootView.findViewById(R.id.editSearch)).setText("");
    }

    private void doClickStructure()
    {
        hideController();
        doClickSettingsStructure();
    }

    private void doClickBrightness()
    {
        if(visibleControllerIdx == Global.BRIGHTNESS)
        {
            hideController();
        }
        else
        {
            showController(Global.BRIGHTNESS);
        }
    }

    private void doClickContrast()
    {
        if(visibleControllerIdx == Global.CONTRAST)
        {
            hideController();
        }
        else
        {
            showController(Global.CONTRAST);
        }
    }

    private void doClickSettingClose(){
        mRootView.findViewById(R.id.layoutSettings).setVisibility(View.GONE);
    }

    private void doClickDetailClose(){
        selProjectLegend = null;
        mRootView.findViewById(R.id.layoutDetail).setVisibility(View.GONE);
    }

    private void doClickSetting()
    {
        hideController();
        mRootView.findViewById(R.id.layoutSettings).setVisibility(View.VISIBLE);

    }

    private void showController(int nIdx)
    {
        mRootView.findViewById(R.id.layoutController).setVisibility(View.VISIBLE);
        visibleControllerIdx = nIdx;
        if(visibleControllerIdx == Global.BRIGHTNESS)
        {
            mRootView.findViewById(R.id.layoutBrightContrastController).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.layoutBrightnessController).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.layoutContrastController).setVisibility(View.INVISIBLE);
        }
        else if(visibleControllerIdx == Global.CONTRAST)
        {
            mRootView.findViewById(R.id.layoutBrightContrastController).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.layoutBrightnessController).setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.layoutContrastController).setVisibility(View.VISIBLE);
        }
    }

    private void hideController()
    {
        visibleControllerIdx = -1;
        mRootView.findViewById(R.id.layoutController).setVisibility(View.GONE);
    }

    public class PanelPoint
    {
        int x;
        int y;
        ProjectStructure structure;
        ProjectLegend legend;
        public PanelPoint(int x, int y, ProjectStructure structure, ProjectLegend legend)
        {
            this.x = x;
            this.y = y;
            this.structure = structure;
            this.legend = legend;
        }

        public double distance(float x1, float y1)
        {
            return Math.sqrt(Math.pow((x1 - x), 2) + Math.pow((y1 - y), 2));
        }
    }
}
