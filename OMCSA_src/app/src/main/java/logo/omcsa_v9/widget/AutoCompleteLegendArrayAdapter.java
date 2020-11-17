package logo.omcsa_v9.widget;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import logo.omcsa_v9.R;
import logo.omcsa_v9.model.LegendText;
import logo.omcsa_v9.utils.Utils;

public class AutoCompleteLegendArrayAdapter extends ArrayAdapter<LegendText>{

    Activity mContext;
    List<LegendText> data;
    public AutoCompleteLegendArrayAdapter(Activity context, int resource, List<LegendText> objects) {
        super(context, resource, objects);
        mContext = context;
        data = objects;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null)
        {
            view = mContext.getLayoutInflater().inflate(R.layout.item_auto_complete_drop_down, viewGroup, false);
        }
        Utils.doApplyAllFontForTextView(mContext, view);
        ((TextView)view.findViewById(R.id.txtCandidate)).setText(data.get(i).text);
        return view;
    }
}
