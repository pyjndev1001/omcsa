package logo.omcsa_v9.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;
import logo.omcsa_v9.model.CategoryInfo;
import logo.omcsa_v9.model.ProjectInfo;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.Utils;
import logo.omcsa_v9.widget.HorizontalListView;

public class CategoryFragment extends BaseFragment {

    public int[] selCategoryImageIdxs = {R.drawable.head_white, R.drawable.neck_white, R.drawable.spine_white,
            R.drawable.thorax_white, R.drawable.abdomen_white, R.drawable.upper_limb_white, R.drawable.lower_limb_white};

    public int[] categoryImageIdxs = {R.drawable.head, R.drawable.neck,  R.drawable.spine,
            R.drawable.thorax, R.drawable.abdomen, R.drawable.upper_limb, R.drawable.lower_limb};

    String mKeyword = "";
    HorizontalListView mCategoryListView = null;
    ListView mProjectListView = null;

    CategoryAdapter mCategoryAdapter = null;
    ProjectAdapter mProjectAdapter = null;

    int nSelCategoryIdx = 0;
    List<ProjectInfo> projectInfoList = new ArrayList<>();

    Map<String, Integer> imageMap = new HashMap<>();
    Map<String, Integer> selImageMap = new HashMap<>();

    String mSearchedText = "";

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_category, container, false);

        Utils.doApplyAllFontForTextView(mContext, mRootView);
        mContext.doShowSearchButton(true);
        showBackButton(true);
        mCategoryListView = mRootView.findViewById(R.id.category_list);
        mProjectListView = mRootView.findViewById(R.id.project_list);
        mCategoryAdapter = new CategoryAdapter();
        mProjectAdapter = new ProjectAdapter();
        mCategoryListView.setAdapter(mCategoryAdapter);
        mProjectListView.setAdapter(mProjectAdapter);
        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                nSelCategoryIdx = i;
                mKeyword = Utils.categoryInfoList.get(i).name;
                mCategoryListView.setSelection(nSelCategoryIdx);
                mCategoryAdapter.notifyDataSetChanged();
                refreshProjectList();
            }
        });
        mProjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProjectInfo projectInfo = mProjectAdapter.getItem(i);

                if(projectInfo.free.equals("free") || Utils.isValidUser(mContext))
                {
                    for(Bitmap bitmap : Utils.bitmapList)
                    {
                        if(!bitmap.isRecycled())
                        {
                            bitmap.recycle();
                        }
                    }
                    Utils.bitmapList.clear();
                    String strSearchedString = mContext.getSearchText();

                    DetailFragment fragment = new DetailFragment();
                    fragment.setContext(mContext);
                    fragment.setProjectInfo(projectInfo);
                    Bundle args = new Bundle();
                    args.putString("searchKey", strSearchedString);
                    fragment.setArguments(args);

                    mContext.replaceFragment(fragment);
                }
                else
                {
                    for(Bitmap bitmap : Utils.bitmapList)
                    {
                        if(!bitmap.isRecycled())
                        {
                            bitmap.recycle();
                        }
                    }
                    Utils.bitmapList.clear();

                    DetailFragment fragment = new DetailFragment();
                    fragment.setContext(mContext);
                    fragment.setProjectInfo(projectInfo);
                    Bundle args = new Bundle();
                    args.putString("searchKey", "");
                    fragment.setArguments(args);

                    mContext.replaceFragment(fragment);
                }
            }
        });

        if(!TextUtils.isEmpty(mKeyword))
        {
            for(int i = 0; i < Utils.categoryInfoList.size(); i++)
            {
                if(mKeyword.equals(Utils.categoryInfoList.get(i).name))
                {
                    nSelCategoryIdx = i;
                    break;
                }
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mCategoryAdapter.notifyDataSetChanged();
                    mCategoryListView.scrollTo((int)Utils.convertDpToPixel(160 * nSelCategoryIdx, mContext));
                }
            });

        }

        imageMap.put("Head", R.drawable.head);
        imageMap.put("neck & lower face", R.drawable.neck);
        imageMap.put("spine", R.drawable.spine);
        imageMap.put("thorax", R.drawable.thorax);
        imageMap.put("abdomen & pelvis", R.drawable.abdomen);
        imageMap.put("Upper limb", R.drawable.upper_limb);
        imageMap.put("lower limb", R.drawable.lower_limb);

        selImageMap.put("Head", R.drawable.head_white);
        selImageMap.put("neck & lower face", R.drawable.neck_white);
        selImageMap.put("spine", R.drawable.spine_white);
        selImageMap.put("thorax", R.drawable.thorax_white);
        selImageMap.put("abdomen & pelvis", R.drawable.abdomen_white);
        selImageMap.put("Upper limb", R.drawable.upper_limb_white);
        selImageMap.put("lower limb", R.drawable.lower_limb_white);

        loadData();
        return mRootView;
    }

    private void refreshProjectList()
    {
        projectInfoList.clear();
        if(!TextUtils.isEmpty(mContext.getSearchText()))
        {
            for(int i = 0; i < Utils.categoryInfoList.size(); i++)
            {
                CategoryInfo categoryInfo =  Global.categoryMap.get(Utils.categoryInfoList.get(i).name);
                if(categoryInfo == null) continue;

                List<ProjectInfo> projectInfos = categoryInfo.data;
                for(ProjectInfo projectInfo : projectInfos)
                {
                    if(projectInfo.searched_count != 0)
                    {
                        projectInfoList.add(projectInfo);
                    }
                }
            }
            mCategoryListView.setVisibility(View.GONE);
        }
        else
        {
            CategoryInfo categoryInfo = Global.categoryMap.get(Utils.categoryInfoList.get(nSelCategoryIdx).name);
            if(categoryInfo != null)
            {
                List<ProjectInfo> projectInfos = categoryInfo.data;
                for(ProjectInfo projectInfo : projectInfos)
                {
                    projectInfoList.add(projectInfo);
                }
                mCategoryListView.setVisibility(View.VISIBLE);
            }

        }

        Collections.sort(projectInfoList, new Comparator<ProjectInfo>() {
            @Override
            public int compare(ProjectInfo projectInfo, ProjectInfo t1) {
                if(projectInfo.weight == 0 && t1.weight == 0)
                {
                    return projectInfo.name.compareTo(t1.name);
                }
                else if(projectInfo.weight == 0)
                {
                    return 1;
                }
                else if(t1.weight == 0)
                {
                    return -1;
                }
                if(projectInfo.weight - t1.weight == 0)
                {
                    return projectInfo.name.compareTo(t1.name);
                }
                else
                {
                    return projectInfo.weight - t1.weight;
                }
            }
        });
        mProjectAdapter.notifyDataSetChanged();
        if(projectInfoList.size() == 0)
        {
            mRootView.findViewById(R.id.no_result).setVisibility(View.VISIBLE);
            mProjectListView.setVisibility(View.GONE);
        }
        else
        {
            mRootView.findViewById(R.id.no_result).setVisibility(View.GONE);
            mProjectListView.setVisibility(View.VISIBLE);
        }
    }

    public void loadData()
    {
        doSearch();
    }

    public void setKeyword(String keyword){
        this.mKeyword = keyword;
    }

    public void doSearch()
    {
        if(Global.categoryMap.size() == 0) return;
        mContext.showProgressDialog("Searching...");
        new Thread()
        {
            @Override
            public void run() {

                String strSearchText = mContext.getSearchText();
                if(!TextUtils.isEmpty(strSearchText) && mSearchedText.equals(strSearchText))
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideProgressDialog();
                            refreshProjectList();
                        }
                    });
                    return;
                }
                mSearchedText = strSearchText;
                int nCategoryExitID = -1;
                boolean bMainExist = false;
                for(int i = 0; i < Utils.categoryInfoList.size(); i++)
                {
                    CategoryInfo categoryInfo = Global.categoryMap.get(Utils.categoryInfoList.get(i).name);
                    if(categoryInfo == null) continue;

                    List<ProjectInfo> projectInfos = categoryInfo.data;
                    boolean bExist = false;
                    for(ProjectInfo projectInfo : projectInfos)
                    {
                        if(TextUtils.isEmpty(strSearchText))
                        {
                            projectInfo.searched_count = projectInfo.image_count;
                        }
                        else
                        {
                            projectInfo.searched_count = 0;
                            List<String> projectIds = Utils.legendTextProjectMap.get(strSearchText);
                            for(String projectId : projectIds)
                            {
                                if(projectId.equals(projectInfo.id))
                                {
                                    projectInfo.searched_count = projectInfo.image_count;
                                    break;
                                }
                            }
                        }

                        if(projectInfo.searched_count > 0)
                        {
                            bExist = true;
                        }
                    }
                    if(bExist && nCategoryExitID == -1)
                    {
                        nCategoryExitID = i;
                    }
                    if(i == nSelCategoryIdx && bExist)
                    {
                        bMainExist = true;
                    }
                }
                if(bMainExist == false && nCategoryExitID != -1)
                {
                    nSelCategoryIdx = nCategoryExitID;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideProgressDialog();
                            mKeyword = Utils.categoryInfoList.get(nSelCategoryIdx).name;
                            mCategoryListView.setSelection(nSelCategoryIdx);
                            mCategoryAdapter.notifyDataSetChanged();
                            mCategoryListView.scrollTo((int)Utils.convertDpToPixel(160 * nSelCategoryIdx, mContext));
                            refreshProjectList();
                        }
                    });

                }
                else
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideProgressDialog();
                            refreshProjectList();
                        }
                    });
                }



            }
        }.start();
    }

    public class CategoryAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return Utils.categoryInfoList.size();
        }

        @Override
        public CategoryInfo getItem(int i) {
            return Global.categoryMap.get(Utils.categoryInfoList.get(i).name);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null)
            {
                view = mContext.getLayoutInflater().inflate(R.layout.item_category, viewGroup, false);
            }
            Utils.doApplyAllFontForTextView(mContext, view);
            if(i == nSelCategoryIdx)
            {
                view.findViewById(R.id.layoutCategory).setBackground(getResources().getDrawable(R.drawable.border_shadow_3));
                ((ImageView)view.findViewById(R.id.imgCategory)).setImageDrawable(getResources().getDrawable(selImageMap.get(Utils.categoryInfoList.get(i).name)));
                ((TextView)view.findViewById(R.id.txtCategory)).setTextColor(getResources().getColor(R.color.white));
            }
            else
            {
                view.findViewById(R.id.layoutCategory).setBackground(getResources().getDrawable(R.drawable.border_shadow_2));
                ((ImageView)view.findViewById(R.id.imgCategory)).setImageDrawable(getResources().getDrawable(imageMap.get(Utils.categoryInfoList.get(i).name)));
                ((TextView)view.findViewById(R.id.txtCategory)).setTextColor(getResources().getColor(R.color.deep_grey));
            }
            ((TextView)view.findViewById(R.id.txtCategory)).setText(Utils.categoryInfoList.get(i).name);

            return view;
        }
    }

    public class ProjectAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return projectInfoList.size();
        }

        @Override
        public ProjectInfo getItem(int i) {
            return projectInfoList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view == null)
            {
                view = mContext.getLayoutInflater().inflate(R.layout.item_project, viewGroup, false);
            }
            Utils.doApplyAllFontForTextView(mContext, view);
            final ProjectInfo projectInfo = getItem(i);
            if(projectInfo.free.equals("free"))
            {
                view.findViewById(R.id.layoutProject).setBackground(getResources().getDrawable(R.drawable.border_shadow_2));
                ((TextView)view.findViewById(R.id.txtMember)).setText("Free");
                ((TextView)view.findViewById(R.id.txtMember)).setTextColor(getResources().getColor(R.color.green));
                view.findViewById(R.id.layoutItem).setAlpha(1f);
                view.findViewById(R.id.txtInvisible).setVisibility(View.GONE);
            }
            else
            {
                if(Utils.isValidUser(mContext))
                {
                    view.findViewById(R.id.layoutProject).setBackground(getResources().getDrawable(R.drawable.border_shadow_2));
                    view.findViewById(R.id.layoutItem).setAlpha(1f);
                    view.findViewById(R.id.txtInvisible).setVisibility(View.GONE);

                    ((TextView)view.findViewById(R.id.txtMember)).setText("Member");
                    ((TextView)view.findViewById(R.id.txtMember)).setTextColor(getResources().getColor(R.color.green));
                }
                else
                {
                    view.findViewById(R.id.layoutProject).setBackground(getResources().getDrawable(R.drawable.border_shadow_2));
                    view.findViewById(R.id.layoutItem).setAlpha(1f);
                    view.findViewById(R.id.txtInvisible).setVisibility(View.GONE);

                    ((TextView)view.findViewById(R.id.txtMember)).setText("Member");
                    ((TextView)view.findViewById(R.id.txtMember)).setTextColor(getResources().getColor(R.color.red));
                }

            }

            final View finalView = view;
            new Thread()
            {
                @Override
                public void run() {

                    try
                    {
                        Bitmap bmp = Utils.projectBitmap.get(projectInfo.id);

                        if(bmp == null)
                        {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            options.inJustDecodeBounds = false;
                            options.inPurgeable = true;
                            byte[] data = getBitmapData(Global.IMAGE_ROOT_PATH + projectInfo.logo_image_url);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 1, data.length - 1, options);
                            int nWidth = bitmap.getWidth();
                            int nHeight = bitmap.getHeight();
                            float fWidthScale = 100 / (nWidth * 1.0f);
                            float fHeighScale = 100 / (nHeight * 1.0f);
                            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*Math.min(fWidthScale, fHeighScale)), (int)(bitmap.getHeight()*Math.min(fWidthScale, fHeighScale)), true);
                            bitmap.recycle();
                            bitmap = newBitmap;
                            System.gc();
                            bmp = bitmap;
                            Utils.projectBitmap.put(projectInfo.id, bmp);
                        }

                        final ImageView imgProduct = finalView.findViewById(R.id.imgProject);
                        final Bitmap finalBmp = bmp;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                imgProduct.setImageBitmap(finalBmp);
                            }
                        });

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }.start();
            ((TextView)view.findViewById(R.id.txtProjectName)).setText(projectInfo.name);
            ((TextView)view.findViewById(R.id.txtImageCount)).setText(String.format("%d images", projectInfo.image_count));
            /*ImageView imgProduct = view.findViewById(R.id.imgProject);
            Picasso.get().load("https://omcsa.org/bundles/swarminfoimagescript/ressources" + projectInfo.logo_image_url).into(imgProduct);*/
            return view;
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
    }
}
