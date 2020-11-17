package logo.omcsa_v9.model;

import java.util.HashMap;
import java.util.Map;

public class ProjectImage {
    public String id = "";
    public String url = "";
    public String position = "";
    public String series = "";
    public Map<String, ProjectLegendInfo> legends = new HashMap<>();
}
