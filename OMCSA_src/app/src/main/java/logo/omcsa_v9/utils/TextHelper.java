package logo.omcsa_v9.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextHelper {
    public static String GetText(InputStream in) {
        String text = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if(sb.length() == 0)
                {
                    sb.append(line);
                }
                else
                {
                    sb.append("\n" + line);
                }

            }
            text = sb.toString();
        } catch (Exception ex) {

        } finally {
            try {
                reader.close();
                in.close();
            } catch (Exception ex) {
            }
        }
        return text;
    }
}
