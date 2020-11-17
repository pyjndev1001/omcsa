package logo.omcsa_v9.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectStructure {
    public String text;
    public String id;
    public String color;
    public Map<String, List<List<Double>>> draw = new HashMap<>();
}
