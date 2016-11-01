package com.btofindr.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.btofindr.R;
import com.btofindr.controller.Utility;
import com.btofindr.model.BlockItem;
import com.btofindr.model.UnitItem;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;

import static com.btofindr.R.id.relLayout1;
import static com.btofindr.fragment.CompareUnitsFragment.blockList;
import static com.btofindr.fragment.CompareUnitsFragment.selectedUnitList;

/**
 * Adapter Class for the List view used in the Compare units page
 */
public class CompareAdapter extends BaseAdapter   {
    private Context context;
    private ArrayList<UnitItem> unitItems;
    private ArrayList<BlockItem> blockItems;

    private Gson gson;
    private ImageView icon;
    private String url;
    private Bitmap bitmap;
    View rootView;
    ListView lvUnits;
    TextView tv_nofave;


    /**
     * Constructor of the CompareAdapter Class
     * used to set the local variables to the data received in the parameters
     *
     * @param context the context of the fragment
     * @param unitItems an ArrayList of UnitItem used for displaying in the list view
     * @param blockItems an ArrayList of BlockItem used to pass through when each unit is clicked
     * @param rootView a view of the parent page, CompareUnitsFragment
     */
    public CompareAdapter(Context context, ArrayList<UnitItem> unitItems,
                          ArrayList<BlockItem> blockItems, View rootView){
        this.context = context;
        this.unitItems = unitItems;
        this.blockItems = blockItems;
        this.rootView = rootView;
    }

    /**
     * Method to set the view of the application
     * The values of the unit details are set in this method
     * such values are the images, title, address, unit number, unit type, price
     * Check box click listener is also been declared in this method
     *
     * @param position
     * @param convertView
     * @param parent
     * @return a View object
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.compare_list_item, null);
        }

        RelativeLayout l1 = (RelativeLayout) convertView.findViewById(relLayout1);
        icon = (ImageView) convertView.findViewById(R.id.iv_icon);//project image
        TextView title = (TextView) convertView.findViewById(R.id.tv_title);
        TextView address = (TextView) convertView.findViewById(R.id.tv_address);
        TextView unitNumber = (TextView) convertView.findViewById(R.id.tv_unit_no);
        TextView unitType = (TextView) convertView.findViewById(R.id.tv_unit_types);
        TextView price = (TextView) convertView.findViewById(R.id.tv_price);
        tv_nofave = (TextView)rootView.findViewById(R.id.tv_nofave);

        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkboxUnit);


        url = blockItems.get(position).getIcon();
        new getImage().execute();

        title.setText(blockItems.get(position).getProjectName());
        unitNumber.setText(unitItems.get(position).getUnitNo()+" ");
        address.setText(blockItems.get(position).getBlockNo() + " " + blockItems.get(position).getStreet());
        price.setText("Price: $" + Utility.formatPrice(unitItems.get(position).getPrice()));
        unitType.setText("("+unitItems.get(position).getUnitType().getUnitTypeName()+")"); //room type
        cb.setTag(position);

        cb.setOnClickListener(new View.OnClickListener(){
            /**
             * Method for CheckBox click listener to store the position of the selected units
             * Storing of the UnitItem that was selected and the BlockItem that is paired with the UnitItem
             * Checking if there is only 2 units selected
             * if more than two or less than two units have been selected
             * a toast would be created to let users know the error
             *
             * @param v
             */
            @Override
            public void onClick(View v){

                CheckBox cb = (CheckBox) v;

                int position = (Integer) v.getTag();
                UnitItem unit = unitItems.get((Integer) v.getTag());

                if(selectedUnitList.contains(unit)){
                    selectedUnitList.remove(unit);
                    blockList.remove(blockItems.get(position));
                }
                else{
                    if(selectedUnitList.size()<2) {
                        selectedUnitList.add(unit);
                        BlockItem a = blockItems.get(position);
                        blockItems.get(position).getBlockId();
                        blockList.add(a);
                    }else {
                        Toast.makeText(context, "Please select only two units", Toast.LENGTH_SHORT).show();
                        cb.toggle();
                    }
                }

            }
        });

        lvUnits = (ListView)rootView.findViewById(R.id.lv_units);
        return convertView;
    }

    /**
     * Method to get the size of the UnitItems in the list
     *
     * @return an integer containing the number of items in the list view
     */
    @Override
    public int getCount() {
        return unitItems.size();
    }

    /**
     * Method to get the number of list items by calling the previous getCount() method
     *
     * @return an integer containing the number of items in the list view
     */
    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    /**
     * Method to get the position of the item that is in the list view
     *
     * @param position
     * @return an integer containing the position of the item in the list view
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Method to get the Object that is in the list view
     *
     * @param position the integer indicates which item it is in the ArrayList
     * @return an Object of the specific list view item
     */
    @Override
    public Object getItem(int position) {
        return unitItems.get(position);
    }

    /**
     * Method to get the list view item's id
     *
     * @param position
     * @return a long number containing the position of the item in the list view
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * AsyncTask Class to load the image for each unit/block
     */
    private class getImage extends AsyncTask<String, Void, Bitmap> {
        /**
         * Method to initialize the bitmap variable to null before executing the loading of an image
         */
        @Override
        protected void onPreExecute() {
            bitmap = null;
        }

        /**
         * Method to set the image to the icon of each list view item after image is being loaded
         * @param bitmap
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            icon.setImageBitmap(bitmap);
        }

        /**
         * Method to load the image from the webservice
         *
         * @param params
         * @return a Bitmap variable containing the loaded image
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}
