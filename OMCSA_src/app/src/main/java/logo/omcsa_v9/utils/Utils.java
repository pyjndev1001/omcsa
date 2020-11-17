package logo.omcsa_v9.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import logo.omcsa_v9.model.CategoryInfo;
import logo.omcsa_v9.model.ProjectContent;
import logo.omcsa_v9.model.ProjectImage;
import logo.omcsa_v9.model.ProjectLegend;

public class Utils {

    public static List<CategoryInfo> categoryInfoList = new ArrayList<>();
    public static List<String> legendTextMap = new ArrayList<>();
    public static Map<String, List<String>> legendTextProjectMap = new HashMap<>();
    public static List<Bitmap> bitmapList = new ArrayList<>();
    public static Map<String, Bitmap> projectBitmap = new HashMap<>();

    public static boolean getBooleanSetting(Context mContext, String key) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        return pref.getBoolean(key, false);
    }

    public static void setBooleanSetting(Context mContext, String key, boolean val) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    public static int getIntSetting(Context mContext, String key) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        return pref.getInt(key, 0);
    }

    public static void setIntSetting(Context mContext, String key, int val) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public static long getLongSetting(Context mContext, String key) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        return pref.getLong(key, 0);
    }

    public static void setLongSetting(Context mContext, String key, long val) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, val);
        editor.commit();
    }

    public static String getStringSetting(Context mContext, String key) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        return pref.getString(key, "");
    }

    public static void setStringSetting(Context mContext, String key, String val) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, val);
        editor.commit();

    }

    public static void setFloatSetting(Context mContext, String key, float val) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, Float.floatToIntBits(val));
        editor.commit();
    }

    public static Float getFloatSetting(Context mContext, String key) {
        SharedPreferences pref = null;
        try{
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch(Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        float nResult = Float.intBitsToFloat(pref.getInt(key, 0));
        return Float.isNaN(nResult) == true ? 0 : nResult;
    }

    public static void setDoubleSetting(Context mContext, String key, double val) {
        SharedPreferences pref = null;
        try
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch (Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, Double.doubleToRawLongBits(val));
        editor.commit();
    }

    public static Double getDoubleSetting(Context mContext, String key) {
        SharedPreferences pref = null;
        try
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
        }catch (Exception e)
        {
            pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
        }
        double nResult = Double.longBitsToDouble(pref.getLong(key, 0));
        return Double.isNaN(nResult) == true ? 0 : nResult;
    }

    public static int parseIntFromString(String strVal) {
        int nResult = 0;
        if (strVal.length() > 0) {
            String pattern ="-?\\d+";
            if(strVal.matches(pattern))
            {
                try {
                    nResult = Integer.valueOf(strVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return nResult;
    }

    public static long parseLongFromString(String strVal) {
        long nResult = 0;
        String pattern ="-?\\d+";
        if(strVal.matches(pattern))
        {
            try{
                nResult = Long.valueOf(strVal);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return nResult;
    }
    public static float parseFloatFromString(String str) {
        float result = 0;
        try {
            result = Float.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static double parseDoubleFromString(String str) {
        double result = 0;
        try {
            result = Double.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressLint("NewApi")
    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        Display d = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            d.getSize(size);
        } else {
            size.x = d.getWidth();
            size.y = d.getHeight();
        }

        return size.x;
    }

    public static void showKeyboard(Context context, EditText editText, boolean bShow) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (bShow == true) {
            editText.requestFocus();
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            editText.setSelection(editText.getText().toString().length());
        } else {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void downloadFile(final String fileURL, final String fileName, final FileDownload fileDownload) {
        new Thread()
        {
            @Override
            public void run() {
                try {
                    URL url = new URL(fileURL);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();

                    int nProgress = 0;
                    c.setRequestMethod("GET");
                    c.setDoOutput(true);
                    c.connect();

                    int nFileSize = c.getContentLength();
                    FileOutputStream f = new FileOutputStream(new File(fileName));
                    InputStream in = new BufferedInputStream(url.openStream(), 4096);
                    byte[] buffer = new byte[1024 * 1000];
                    int len1;

                    fileDownload.startDownload();
                    int oldProgress = 0;
                    while ((len1 = in.read(buffer)) > 0) {
                        f.write(buffer, 0, len1);
                        nProgress += len1;
                        final int progress = (int)(nProgress * 1.0f / nFileSize * 100f);
                        if(progress - oldProgress > 1)
                        {
                            oldProgress = progress;
                            fileDownload.setProgress(progress);
                        }

                    }
                    try
                    {
                        f.flush();
                        f.close();
                        in.close();
                        c.disconnect();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    fileDownload.endDownload();

                } catch (Exception e) {
                    Log.d("Error....", e.toString());
                    fileDownload.errorDownload(e.toString());
                }
            }
        }.start();
    }

    public static boolean createDirectory(String dirPath) {
        boolean bRes = false;
        File dir = new File(dirPath);
        if (dir.exists() == false) {
            try {
                bRes = dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bRes = dir.canWrite();
        }
        return bRes;
    }

    public static void fileDelete(String filePath) {
        File file = new File(filePath);
        if (file.exists() == true) {
            file.delete();
        }
    }

    public static void deleteDirectory(String path) {
        File dir = new File(path);
        if (dir.exists() == true) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory() == true) {
                        deleteDirectory(files[i].getAbsolutePath());
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        dir.delete();
    }

    public static boolean unzipData(String strZipPath, String strExtractDir) {
        boolean bRes = false;
        try {
            //deleteDirectory(strExtractDir);
            createDirectory(strExtractDir);

            InputStream is = new FileInputStream(strZipPath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            String strFileName;
            byte[] buffer = new byte[1024];
            int count;
            while ((ze = zis.getNextEntry()) != null) {
                strFileName = ze.getName();
                if (ze.isDirectory()) {
                    File fmd = new File(strExtractDir + "/" + strFileName);
                    fmd.mkdirs();
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(strExtractDir + "/" + strFileName);
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.flush();
                fout.close();
                zis.closeEntry();
            }
            zis.close();
            is.close();
            bRes = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bRes;
    }

    public static  String ReadFile( String path){
        String line = null;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
            inputStreamReader.close();

        }
        catch(FileNotFoundException ex) {

        }
        catch(IOException ex) {

        }
        return line;
    }

    public static String getLang(String strLang)
    {
        if(strLang.equals("en"))
        {
            return "english";
        }
        else if(strLang.equals("fr"))
        {
            return "Français";
        }
        else if(strLang.equals("ja"))
        {
            return "JAPANESE";
        }
        else if(strLang.equals("sc"))
        {
            return "Chinese_sc";
        }
        else if(strLang.equals("tc"))
        {
            return "Chinese_tc";
        }
        else if(strLang.equals("es"))
        {
            return "Español";
        }
        else if(strLang.equals("pt"))
        {
            return "Português";
        }
        else
        {
            return strLang;
        }
    }

    //contrast : 0 to 10 brightness : -255 to 255
    public static Bitmap enhanceImage(Bitmap mBitmap, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, -128 * (contrast) + 128 + brightness,
                        0, contrast, 0, 0, -128 * (contrast) + 128 + brightness,
                        0, 0, contrast, 0, -128 * (contrast) + 128 + brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap mEnhancedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap
                .getConfig());
        System.gc();
        Canvas canvas = new Canvas(mEnhancedBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(mBitmap, 0, 0, paint);
        System.gc();
        mBitmap.recycle();
        return mEnhancedBitmap;
    }

    public static boolean isValidUser(Context context)
    {
        if( Utils.getBooleanSetting(context, Global.PREF_IS_LOGIN) )
        {
            long expireTime = Utils.getLongSetting(context, Global.PREF_EXPIRE) * 1000;
            long currentTime = System.currentTimeMillis();
            long remain = expireTime - currentTime;
            if(remain > 0) {
                return true;
            }
            else
            {
                return false;
            }
        }
        else{
            return false;
        }
    }



    public static int getImageCount(String strSearch, ProjectContent projectContent)
    {
        Map<String, ProjectImage> projectImageMap = projectContent.images;
        Map<String, ProjectLegend> legendMap = projectContent.legends;
        int nCount = 0;
        for(String key : projectImageMap.keySet())
        {
            ProjectImage image = projectImageMap.get(key);
            if(TextUtils.isEmpty(strSearch))
                nCount++;
            else
            {
                boolean bOK = false;
                for(String legend_key : image.legends.keySet())
                {
                    ProjectLegend projectLegend = legendMap.get(legend_key);
                    if(projectLegend != null && projectLegend.text != null && legendMap.get(legend_key).text.get("english") != null && legendMap.get(legend_key).text.get("english").equals(strSearch))
                    {
                        bOK = true;
                        break;
                    }
                }
                if(bOK)
                {
                    nCount++;
                }
            }
        }
        return nCount;
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static int getTopIntRound(int n, int m)
    {
        if(n % m == 0)
        {
            return n/m;
        }
        else
        {
            return n/m + 1;
        }
    }

    public static int getBottomIntRound(int n, int m)
    {
        return n/m;
    }

    public static int getImageIndexForThumb(int nThumbIndex, int nImageCount)
    {
        int nImageIndex = 0;
        int nStartValue = getTopIntRound(nThumbIndex * nImageCount, 10);

        int nEndValue = getBottomIntRound((nThumbIndex + 1) * nImageCount, 10);

        return (nEndValue - nStartValue) / 2 + nStartValue;
    }

    public static int getIntValueFromDouble(double value)
    {
        BigDecimal rounded = BigDecimal.valueOf( value ).setScale(1, RoundingMode.HALF_UP);
        rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(0, RoundingMode.HALF_UP);
        return rounded.intValue();
    }

    public static Bitmap verticalFlip(Bitmap bitmap)
    {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        Bitmap bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        System.gc();
        return bOutput;
    }

    public static Bitmap horizontalFlip(Bitmap bitmap)
    {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        Bitmap bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        System.gc();
        return bOutput;
    }

    public static Bitmap verticalHorizontalFlip(Bitmap bitmap, boolean horz, boolean vert)
    {
        Matrix matrix = new Matrix();
        matrix.preScale(horz ? -1.0f : 1.0f, vert ? -1.0f : 1.0f);
        Bitmap bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        System.gc();
        return bOutput;
    }

    public static Typeface getBoldFont(Context context)
    {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-SemiBold.ttf");
        return tf;
    }

    public static Typeface getRegularFont(Context context)
    {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Regular.ttf");
        return tf;
    }

    public static void doApplyAllFontForTextView(Context context, View v){

        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    doApplyAllFontForTextView(context, child);
                }
            } else if (v instanceof TextView) {
                if(((TextView)v).getTypeface().isBold())
                {
                    ((TextView)v).setTypeface(getBoldFont(context));
                }
                else
                {
                    ((TextView)v).setTypeface(getRegularFont(context));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String removeComma(String strText){
        if(strText.contains("\'"))
        {
            //return strText.replace("\'", "\\\'");
            return strText.split("'")[0];
        }
        else {
            return strText;
        }
    }

    public static ProjectContent getProjectContent(String id)
    {
        String strJSON = Utils.ReadFile(Global.ROOT_PATH + "/json/" + id + ".json");
        ProjectContent projectContent = null;
        try
        {
            projectContent = ProjectContent.buildFromJsonString(strJSON);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return projectContent;
    }
}


