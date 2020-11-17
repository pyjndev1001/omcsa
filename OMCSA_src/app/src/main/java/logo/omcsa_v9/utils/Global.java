package logo.omcsa_v9.utils;

import android.app.Application;
import android.os.Environment;

import java.util.HashMap;
import java.util.Map;

import logo.omcsa_v9.model.CategoryInfo;
import logo.omcsa_v9.model.OrderHistory;
import logo.omcsa_v9.model.ProjectImage;
import logo.omcsa_v9.model.ProjectLegend;
import logo.omcsa_v9.model.ProjectSeries;
import logo.omcsa_v9.model.ProjectStructure;

public class Global {

    public static String PREF_IS_LOGIN = "IsLogin";

    public static String PREF_USER_ID = "UserID";
    public static String PREF_TITLE = "Title";
    public static String PREF_FIRST_NAME = "FirstName";
    public static String PREF_LAST_NAME = "LastName";
    public static String PREF_PROFESSION = "LastName";
    public static String PREF_COUNTRY = "Country";
    public static String PREF_USERNAME = "UserName";
    public static String PREF_EMAIL = "Email";
    public static String PREF_DATA_VERSION = "DataVersion";
    public static String PREF_EXPIRE = "Expire";
    public static String PREF_LANGAUGE = "Language";

    public static String ROOT_PATH = Environment.getExternalStorageDirectory() + "/Android/data/net.omc.eanatomy/files/v3/.file";
    public static String IMAGE_ROOT_PATH = ROOT_PATH + "/ressources_/";

    public static OrderHistory selOrderHistory = null;

    public static Map<String, CategoryInfo> categoryMap = new HashMap<>();

    public static int BRIGHTNESS = 2;
    public static int CONTRAST = 3;

    public static boolean gLoaded = false;

    public static boolean offline = true;
}
