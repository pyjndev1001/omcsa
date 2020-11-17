package logo.omcsa_v9.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import logo.omcsa_v9.R;
import logo.omcsa_v9.api.ApiClient;
import logo.omcsa_v9.dialog.MessageDialog;
import logo.omcsa_v9.model.CategoryInfo;
import logo.omcsa_v9.model.CategoryInfoResponse;
import logo.omcsa_v9.model.ModifiedDateResponse;
import logo.omcsa_v9.model.ProjectContent;
import logo.omcsa_v9.model.ProjectInfo;
import logo.omcsa_v9.model.ProjectLegend;
import logo.omcsa_v9.utils.FileDownload;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.TextHelper;
import logo.omcsa_v9.utils.Utils;

public class SplashActivity extends BaseActivity {

    public final int MY_PERMISSIONS_REQUEST_STORAGE = 1002;
    public Handler mHandler;
    int mVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_splash);

        ((TextView)findViewById(R.id.txtSplash)).setTypeface(Utils.getBoldFont(this));
        //Check Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
        }
        else
        {
            checkUpdates();
        }

        mHandler = new Handler(getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    checkUpdates();
                }
            }
        }
    }

    private void checkUpdates()
    {
        if(!Global.offline)
        {
            showProgressDialog("Checking Updates...");

            new Thread(){
                @Override
                public void run() {
                    HttpURLConnection urlConnection = null;
                    ModifiedDateResponse modifiedDateResponse = null;
                    String response = "";
                    try {
                        URL url = new URL("https://omcsa.org/imagescript/index.php/Backend/getDataModifiedDate");
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setReadTimeout(600 * 1000);
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        response = TextHelper.GetText(in);

                        if(!TextUtils.isEmpty(response))
                        {
                            Gson gson = new Gson();
                            modifiedDateResponse = gson.fromJson(response, ModifiedDateResponse.class);
                        }
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(urlConnection != null)
                        {
                            urlConnection.disconnect();
                        }
                    }

                    if(modifiedDateResponse != null)
                    {
                        int nDataVersion = Utils.getIntSetting(SplashActivity.this, Global.PREF_DATA_VERSION);
                        if(nDataVersion == (int)modifiedDateResponse.time){
                            //buildProjectData();
                            mVersion = (int)modifiedDateResponse.time;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    doProcessNext();
                                }
                            });

                        }
                        else
                        {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showProgressDialog("Prepare Data File downloading...");
                                }
                            });

                            Utils.deleteDirectory(Global.ROOT_PATH);

                            //Utils.setIntSetting(SplashActivity.this, Global.PREF_DATA_VERSION, (int)modifiedDateResponse.time);
                            mVersion = (int)modifiedDateResponse.time;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadData();
                                }
                            });
                        }
                    }
                    else
                    {
                        final String finalResponse = response;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                if(Utils.getIntSetting(SplashActivity.this, Global.PREF_DATA_VERSION) != 0)
                                {
                                    doProcessNext();
                                }
                                else
                                {
                                    showErrorMessage("Error", "Server Error" + finalResponse, null, null);
                                }
                            }
                        });
                    }

                }
            }.start();
        }
        else
        {
            doProcessNext();
        }
    }

    private void downloadData()
    {
        //download zip file from the server.
        showProgressDialog("Prepare Data File downloading...");
        String strZipFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mVersion + ".omz";
        Utils.downloadFile(ApiClient.API_MAIN_ROOT + "downloadLatestZip", strZipFilePath, new FileDownload() {
            @Override
            public void startDownload() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog("Starting Data File Downloading...");
                    }
                });
            }

            @Override
            public void endDownload() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        unzipData();
                    }
                });
            }

            @Override
            public void setProgress(final int nProgress) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog(String.format("%d%% Downloaded...", nProgress));
                    }
                });
            }

            @Override
            public void errorDownload(final String errorString) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        showErrorMessage("Error", "Downloading Failed : " + errorString, "Retry", new MessageDialog.Callback() {
                            @Override
                            public void onOK() {
                                downloadData();
                            }
                        });

                    }
                });

            }
        });
    }

    private void unzipData()
    {
        showProgressDialog("Extract Data Files...");
        new Thread()
        {
            @Override
            public void run() {
                String strZipFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mVersion + ".omz";
                if(Utils.unzipData(strZipFilePath, Global.ROOT_PATH))
                {
                    Utils.fileDelete(strZipFilePath);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            downloadProjectData();
                        }
                    });
                }
                else
                {
                    Utils.fileDelete(strZipFilePath);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            showErrorMessage("Error", "Data Zip File is not correct.", null, null);
                        }
                    });
                }

            }
        }.start();
    }



    private void downloadProjectData()
    {
        //download zip file from the server.
        showProgressDialog("Preparing Project Detail File Downloading...");
        String strZipFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mVersion + "_project.zip";
        Utils.downloadFile(ApiClient.API_MAIN_ROOT + "get_project_content", strZipFilePath, new FileDownload() {
            @Override
            public void startDownload() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog("Starting Project Detail File Downloading...");
                    }
                });

            }

            @Override
            public void endDownload() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        unzipProjectData();
                    }
                });

            }

            @Override
            public void setProgress(final int nProgress) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog(String.format("%d%% Downloaded...", nProgress));
                    }
                });

            }

            @Override
            public void errorDownload(final String errorString) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        showErrorMessage("Error", "Downloading Failed : " + errorString, "Retry", new MessageDialog.Callback() {
                            @Override
                            public void onOK() {
                                downloadProjectData();
                            }
                        });
                    }
                });

            }
        });
    }

    private void unzipProjectData()
    {
        showProgressDialog("Extract Project Files...");
        new Thread()
        {
            @Override
            public void run() {
                String strZipFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mVersion + "_project.zip";
                if(Utils.unzipData(strZipFilePath, Global.ROOT_PATH))
                {
                    Utils.fileDelete(strZipFilePath);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            Global.gLoaded = false;
                            doProcessNext();
                        }
                    });
                }
                else
                {
                    Utils.fileDelete(strZipFilePath);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            showErrorMessage("Error", "Project Zip File is not correct.", null, null);
                        }
                    });
                }


            }
        }.start();
    }

    private void buildLegendText(ProjectContent projectContent)
    {
        Iterator iterator = projectContent.legends.keySet().iterator();
        while(iterator.hasNext()){
            String key = (String)iterator.next();
            ProjectLegend legend = projectContent.legends.get(key);

            String strLegend = legend.text.get("english");
            Utils.legendTextMap.add(strLegend);

            List<String> projectIds = Utils.legendTextProjectMap.get(strLegend);
            if(projectIds == null)
            {
                projectIds = new ArrayList<>();
                Utils.legendTextProjectMap.put(strLegend, projectIds);
            }
            projectIds.add(projectContent.id);
            /*Iterator legendIterator = legend.text.keySet().iterator();
            while(legendIterator.hasNext())
            {
                String langKey = (String)legendIterator.next();
                String strLegend = legend.text.get(langKey);
                List<String> legendList = Utils.legendTextMap.get(langKey);
                if(legendList == null)
                {
                    legendList = new ArrayList<>();
                    Utils.legendTextMap.put(langKey, legendList);
                }
                legendList.add(strLegend);
            }*/
        }

        /*Iterator langIterator = Utils.legendTextMap.keySet().iterator();
        while(langIterator.hasNext())
        {
            String langKey = (String)langIterator.next();
            List<String> legendList = Utils.legendTextMap.get(langKey);
            Collections.sort(legendList, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.compareTo(t1);
                }
            });
        }*/
        Collections.sort(Utils.legendTextMap, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
    }

    private void doLoadProjectData()
    {
        new Thread()
        {
            @Override
            public void run() {
                for(CategoryInfo categoryInfo : Utils.categoryInfoList){
                    for(ProjectInfo projectInfo : categoryInfo.data)
                    {
                        String strJSON = Utils.ReadFile(Global.ROOT_PATH + "/json/" + projectInfo.id + ".json");
                        ProjectContent projectContent = null;
                        try
                        {
                            projectContent = ProjectContent.buildFromJsonString(strJSON);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if(projectContent != null)
                        {
                            projectInfo.image_count = Utils.getImageCount(null, projectContent);
                            buildLegendText(projectContent);
                        }
                    }
                    Global.categoryMap.put(categoryInfo.name, categoryInfo);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Global.gLoaded = true;
                        doProcessNext();
                    }
                });
            }
        }.start();
    }

    private void doProcessNext()
    {
        if(Global.gLoaded == false)
        {
            Utils.categoryInfoList.clear();
            Utils.legendTextMap.clear();
            Global.categoryMap.clear();

            showProgressDialog("Loading Data...");
            new Thread(){
                @Override
                public void run() {
                    if(Global.offline)
                    {
                        CategoryInfoResponse categoryInfoResponse = null;
                        String response = "";
                        try {
                            File file = new File(Global.ROOT_PATH + "/get_main_categories.txt");
                            if(!file.exists())
                            {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgressDialog();
                                        showErrorMessage("Warning", "cannot find the get_main_categories.txt file", "Okay", null);
                                    }
                                });
                                return;
                            }
                            int nLen = (int)file.length();
                            FileInputStream fis = new FileInputStream(file);
                            byte[] buffer = new byte[nLen];
                            fis.read(buffer);
                            fis.close();
                            response = new String(buffer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Gson gson = new Gson();
                        categoryInfoResponse = gson.fromJson(response, CategoryInfoResponse.class);

                        if(categoryInfoResponse != null && categoryInfoResponse.result == 1)
                        {
                            Utils.categoryInfoList.addAll(categoryInfoResponse.data);
                            doLoadProjectData();
                        }
                        else
                        {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                    showErrorMessage("Warning", "get_main_categories.txt file file damaged", "Okay", null);
                                }
                            });

                        }
                    }
                    else
                    {
                        HttpURLConnection urlConnection = null;
                        CategoryInfoResponse categoryInfoResponse = null;
                        String response = "";
                        try {
                            URL url = new URL("https://omcsa.org/imagescript/index.php/Backend/get_main_categories");
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setReadTimeout(600 * 1000);
                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            response = TextHelper.GetText(in);

                            if(!TextUtils.isEmpty(response))
                            {
                                Gson gson = new Gson();
                                categoryInfoResponse = gson.fromJson(response, CategoryInfoResponse.class);
                            }
                            in.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            if(urlConnection != null)
                            {
                                urlConnection.disconnect();
                            }
                        }

                        if(categoryInfoResponse != null && categoryInfoResponse.result == 1)
                        {
                            Utils.categoryInfoList.addAll(categoryInfoResponse.data);
                            doLoadProjectData();
                        }
                        else
                        {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                    showErrorMessage("Warning", "Network connection Error. Please check your network connection and try again.", "Okay", null);
                                }
                            });

                        }
                    }

                }
            }.start();
        }
        else
        {
            Utils.setIntSetting(SplashActivity.this, Global.PREF_DATA_VERSION, mVersion);

            if(Utils.getBooleanSetting(SplashActivity.this, Global.PREF_IS_LOGIN))
            {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            else
            {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            finish();
        }

    }
}
