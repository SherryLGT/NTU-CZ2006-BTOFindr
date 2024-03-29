package com.btofindr.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.btofindr.R;
import com.btofindr.activity.MainActivity;
import com.btofindr.adapter.BlockAdapter;
import com.btofindr.controller.Utility;
import com.btofindr.model.Block;
import com.btofindr.model.BlockItem;
import com.btofindr.model.SearchParameter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static com.btofindr.activity.MainActivity.scale;

/**
 * This fragment is for result of searching of BTO (Build To Order)
 * according to user selected search parameters.
 *
 * @author Sherry Lau Geok Teng
 * @version 1.0
 * @since 31/08/2016
 */

public class SearchResultFragment extends Fragment {

    private ProgressDialog dialog;
    private ArrayList<BlockItem> blockItems;
    private LinearLayout llWrapper;
    private Spinner spinSort;
    private ListView lvBlocks;

    private Gson gson;
    private String response;
    private SearchParameter parameter;
    private ArrayList<Block> blockList;
    public static Block selectedBlock;

    /**
     * Default constructor for SearchResultFragment
     */
    public SearchResultFragment() {}

    /**
     * Create a View to display contents on the layout.
     *
     * @param inflater The LayoutInflater object that is used to inflate any view
     * @param container The parent view that fragment UI is attached to
     * @param savedInstanceState Previous state of the fragment
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search_result, container, false);

        llWrapper = (LinearLayout) rootView.findViewById(R.id.ll_wrapper);
        spinSort = (Spinner) rootView.findViewById(R.id.spin_sort);
        lvBlocks = (ListView) rootView.findViewById(R.id.lv_blocks);

        parameter = SearchFragment.parameter;
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);

        // Handles on change event on drop down list for sorting of block
        spinSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        parameter.setOrderBy('P');
                        new loadData().execute();
                        break;
                    case 1:
                        parameter.setOrderBy('T');
                        new loadData().execute();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.title_search_results));
    }

    /**
     * An AsyncTask to load BTO information according to user search parameters.
     */
    private class loadData extends AsyncTask<Void, Integer, Object> {
        @Override
        protected void onPreExecute() {
            dialog.show();
            gson = new Gson();

            blockList = new ArrayList<Block>();
            blockItems = new ArrayList<BlockItem>();
        }

        @Override
        protected void onPostExecute(Object o) {
            if(blockItems.isEmpty()) {
                // If no BTO are found according to user search parameters
                llWrapper.removeAllViews();
                llWrapper.setPadding(0, Utility.getPixels(50, scale), 0, 0);
                TextView tv = new TextView(getContext());
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setTextSize(Utility.getPixels(5, scale));
                tv.setText("There are no available blocks \nbased on your search parameters.");
                llWrapper.addView(tv);
            }
            else {
                // If BTO are found, display accordingly
                lvBlocks.setAdapter(new BlockAdapter(getContext(), blockItems));
                lvBlocks.setOnItemClickListener(new blockItemClickListener());
            }
            dialog.dismiss();
        }

        @Override
        protected Object doInBackground(Void... params) {
            // Retrieve blocks from database according to search parameters
            response = Utility.postRequest("Block/SearchBlocks", gson.toJson(parameter));
            blockList = gson.fromJson(response, new TypeToken<List<Block>>() {
            }.getType());
            for (Block block : blockList) {
                BlockItem item = new BlockItem();
                item.setIcon(block.getProject().getProjectImage());
                item.setProjectName(block.getProject().getProjectName());
                item.setBlockNo(block.getBlockNo());
                item.setStreet(block.getStreet());
                item.setUnitTypes(block.getUnitTypes());
                item.setMinPrice(block.getMinPrice());
                item.setMaxPrice(block.getMaxPrice());
                item.setTravelTime(block.getTravelTime());
                item.setTravelDist(block.getTravelDist());
                blockItems.add(item);
            }

            return blockList;
        }
    }

    /**
     * Handles on click change event on a block item on the list of blocks
     */
    private class blockItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedBlock = blockList.get(position);
            getFragmentManager().beginTransaction().replace(R.id.fl_container, new BlockFragment()).addToBackStack("BlockFragment").commit();
        }
    }
}
