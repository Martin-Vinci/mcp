package com.micropay.popups;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micropay.adaptor.RolesAdaptor;
import com.micropay.api.Constants;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by micropay on 01/25/2021.
 */

public class RoleSheet extends BottomSheetDialogFragment {

    private RecyclerView roles_list;
    private RolesAdaptor adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.role_sheet, container, false);

        roles_list = rootView.findViewById(R.id.roles_list);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        roles_list.setHasFixedSize(true);
        roles_list.setLayoutManager(linearLayout);
        roles_list.setItemAnimator(new DefaultItemAnimator());

        try {
            JSONArray roles = new JSONArray(getArguments().getString(Constants.KEY));
            adapter = new RolesAdaptor(roles, getActivity());
            roles_list.setAdapter(adapter);
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

}
