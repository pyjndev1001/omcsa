package logo.omcsa_v9.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import logo.omcsa_v9.R;
import logo.omcsa_v9.model.OrderHistory;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.Utils;

public class OrderHistoryActivity extends BaseActivity {

    public OrderHistory orderHistory = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        orderHistory = Global.selOrderHistory;
        Utils.doApplyAllFontForTextView(this, findViewById(R.id.layoutMain));
        loadData();

        findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadData()
    {
        ((TextView)findViewById(R.id.txtInvoiceNo)).setText("Invoice no.: " + String.format("INV%04d", orderHistory.id));
        Date date = new Date();
        try
        {
            date = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(orderHistory.paydate);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ((TextView)findViewById(R.id.txtDate)).setText(new SimpleDateFormat("dd MMMM yyy").format(date));
        ((TextView)findViewById(R.id.txtProductName)).setText("Renew member subscription");
        ((TextView)findViewById(R.id.txtTotalEx)).setText(String.valueOf(orderHistory.totalexvat));
        ((TextView)findViewById(R.id.txtTotalInc)).setText(String.valueOf(orderHistory.totalinvat));
        ((TextView)findViewById(R.id.txtOrder)).setText(String.valueOf(orderHistory.plan));
    }
}
