package logo.omcsa_v9.widget;

import android.app.Activity;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import logo.omcsa_v9.R;
import logo.omcsa_v9.utils.Utils;

public class AutoCompleteArrayAdapter extends ArrayAdapter<String>{

    Activity mContext;
    List<String> data;
    private ListFilter listFilter = new ListFilter();
    private List<String> dataListAllItems;
    public AutoCompleteArrayAdapter(Activity context, int resource, List<String> objects) {
        super(context, resource, objects);
        mContext = context;
        data = objects;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {

        return data.get(position);
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null)
        {
            view = mContext.getLayoutInflater().inflate(R.layout.item_auto_complete_drop_down, viewGroup, false);
        }
        Utils.doApplyAllFontForTextView(mContext, view);
        ((TextView)view.findViewById(R.id.txtCandidate)).setText(getItem(i));
        return view;
    }

    public class ListFilter extends Filter {
        private Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (dataListAllItems == null) {
                synchronized (lock) {
                    dataListAllItems = new ArrayList<String>(data);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = dataListAllItems;
                    results.count = dataListAllItems.size();
                }
            } else {
                final String searchStrLowerCase = prefix.toString().toLowerCase();

                ArrayList<String> matchValues = new ArrayList<String>();

                for (String dataItem : dataListAllItems) {
                    if (dataItem.toLowerCase().contains(searchStrLowerCase)) {
                        matchValues.add(dataItem);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                data = (ArrayList<String>)results.values;
            } else {
                data = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
