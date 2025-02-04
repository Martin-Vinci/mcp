package com.micropay.popups;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micropay.api.Constants;
import com.micropay.micropay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by micropay on 01/25/2021.
 */

public class StatementDetail extends BottomSheetDialogFragment {

    private NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stmt_sheet, container, false);

        TextView tran_journal = rootView.findViewById(R.id.tran_journal);
        TextView tran_date = rootView.findViewById(R.id.tran_date);
        TextView tran_amount = rootView.findViewById(R.id.tran_amount);
        TextView ledger_bal = rootView.findViewById(R.id.ledger_bal);
        TextView tran_desc = rootView.findViewById(R.id.tran_desc);

        try {
            JSONObject stmtList = new JSONObject(getArguments().getString(Constants.KEY));
            tran_journal.setText(stmtList.optString("TRAN_JOURNAL_ID"));
            tran_date.setText(stmtList.optString("TRAN_DT"));
            BigDecimal amount = getAbs(stmtList.optString("AMOUNT"));
            tran_amount.setText(stmtList.optString(formatter.format(amount)) +
                    " " + stmtList.optString("CRNCY_CD"));

            if (stmtList.optString("AMOUNT").startsWith("-")) {
                tran_amount.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tran_amount.setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
            }

            ledger_bal.setText(stmtList.optString("LEDGER_BAL"));
            tran_desc.setText(stmtList.optString("TRAN_DESC"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        rootView.findViewById(R.id.app_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }

    private BigDecimal getAbs(String defaultValue) {
        try {
            if (defaultValue != null || "null".equalsIgnoreCase(defaultValue)) {
                return new BigDecimal(defaultValue).abs();
            }
        } catch (NumberFormatException e) {
        }
        return BigDecimal.ZERO;
    }

}
