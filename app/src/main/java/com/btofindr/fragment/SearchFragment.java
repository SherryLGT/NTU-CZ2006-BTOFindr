package com.btofindr.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.btofindr.R;
import com.btofindr.activity.MainActivity;
import com.btofindr.controller.Utility;
import com.btofindr.model.Profile;
import com.btofindr.model.Project;
import com.btofindr.model.SearchParameter;
import com.btofindr.model.UnitType;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.btofindr.activity.MainActivity.scale;

/**
 * Created by Sherry on 31/08/2016.
 */

public class SearchFragment extends Fragment {

    private ProgressDialog dialog;
    private LinearLayout llTownLeft, llTownRight, llRoomLeft, llRoomRight;
    private Spinner spinEthic;
    private CrystalRangeSeekbar sbPriceRange;
    private TextView tvPriceRange;
    private Button btnSearch;

    private Gson gson;
    private boolean odd;
    private String[] data;
    private ArrayList<Project> projectList;
    private ArrayList<UnitType> unitTypeList;

    private ArrayList<String> townNames, unitTypes;
    private char ethic;
    private int minPrice, maxPrice;
    public static SearchParameter parameter;

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        llTownLeft = (LinearLayout) rootView.findViewById(R.id.ll_town_left);
        llTownRight = (LinearLayout) rootView.findViewById(R.id.ll_town_right);
        llRoomLeft = (LinearLayout) rootView.findViewById(R.id.ll_room_left);
        llRoomRight = (LinearLayout) rootView.findViewById(R.id.ll_room_right);
        spinEthic = (Spinner) rootView.findViewById(R.id.spin_ethic);
        sbPriceRange = (CrystalRangeSeekbar) rootView.findViewById(R.id.sb_price_range);
        tvPriceRange = (TextView) rootView.findViewById(R.id.tv_price_range);
        btnSearch = (Button) rootView.findViewById(R.id.btn_search);

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        new loadData().execute();

        sbPriceRange.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvPriceRange.setText("SGD$" + Utility.formatPrice(minValue.doubleValue()) + " - SGD$" + Utility.formatPrice(maxValue.doubleValue()));
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ethic = Utility.setEthic(spinEthic.getSelectedItem().toString());
                minPrice = sbPriceRange.getSelectedMinValue().intValue();
                maxPrice = sbPriceRange.getSelectedMaxValue().intValue();

                Profile profile = gson.fromJson(Utility.readFromFile("profile", getContext()), Profile.class);
                if (profile == null) {
                    profile = new Profile();
                }

                parameter = new SearchParameter(townNames, ethic, unitTypes, maxPrice, minPrice, 'P', profile.getPostalCode());
                getFragmentManager().beginTransaction().replace(R.id.fl_container, new SearchResultFragment()).addToBackStack("SearchResultFragment").commit();
                ((MainActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.title_search_results));
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.title_search));
    }

    private class loadData extends AsyncTask<Void, Integer, Object> {
        @Override
        protected void onPreExecute() {
            dialog.show();
            odd = true;
            gson = new Gson();

            projectList = new ArrayList<Project>();
            unitTypeList = new ArrayList<UnitType>();
            townNames = new ArrayList<String>();
            unitTypes = new ArrayList<String>();
        }

        @Override
        protected void onPostExecute(Object o) {
            dialog.dismiss();

            for (Project project : projectList) {
                final Button btn = generateButton();
                btn.setText(project.getTownName());
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!btn.isSelected()) {
                            btn.setSelected(true);
                            townNames.add(btn.getText().toString());
                        } else {
                            btn.setSelected(false);
                            townNames.remove(btn.getText().toString());
                        }
                    }
                });
                if (odd) {
                    llTownLeft.addView(btn);
                    odd = false;
                } else {
                    llTownRight.addView(btn);
                    odd = true;
                }
            }

            odd = true;
            for (UnitType unitType : unitTypeList) {
                final Button btn = generateButton();
                btn.setText(unitType.getUnitTypeName());
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!btn.isSelected()) {
                            btn.setSelected(true);
                            unitTypes.add(btn.getText().toString());
                        } else {
                            btn.setSelected(false);
                            unitTypes.remove(btn.getText().toString());
                        }
                    }
                });
                if (odd) {
                    llRoomLeft.addView(btn);
                    odd = false;
                } else {
                    llRoomRight.addView(btn);
                    odd = true;
                }
            }
        }

        @Override
        protected Object doInBackground(Void... params) {
            data = gson.fromJson(Utility.getRequest("Project/GetTownNames"), String[].class);
            for (String townName : data) {
                Project project = new Project();
                project.setTownName(townName);
                projectList.add(project);
            }

            data = gson.fromJson(Utility.getRequest("UnitType/GetUnitTypes"), String[].class);
            for (String type : data) {
                UnitType unitType = new UnitType();
                unitType.setUnitTypeName(type);
                unitTypeList.add(unitType);
            }

            return null;
        }
    }

    private Button generateButton() {
        Button btn = new Button(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utility.getPixels(35, scale));
        params.setMargins(Utility.getPixels(3, scale), Utility.getPixels(5, scale), Utility.getPixels(3, scale), Utility.getPixels(5, scale));
        btn.setPadding(Utility.getPixels(3, scale), Utility.getPixels(3, scale), Utility.getPixels(3, scale), Utility.getPixels(3, scale));
        btn.setLayoutParams(params);
        btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_button_selector));
        btn.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.text_selector_black_white));

        return btn;
    }
}