package logo.omcsa_v9.model;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ProjectContent {
    public String id = "";
    public Map<String, ProjectImage> images = new HashMap<>();
    public Map<String, ProjectLegend> legends = new HashMap<>();
    public Map<String, ProjectStructure> structures = new HashMap<>();
    public Map<String, ProjectSeries> series = new HashMap<>();

    public static ProjectContent buildFromJsonString(String strJson)
    {
        Gson g = new Gson();
        ProjectContent projectContent = g.fromJson(strJson, ProjectContent.class);
        return projectContent;
    }
}
