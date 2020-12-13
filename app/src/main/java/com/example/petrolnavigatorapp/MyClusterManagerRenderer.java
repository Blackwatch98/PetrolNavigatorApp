package com.example.petrolnavigatorapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.petrolnavigatorapp.utils.MyCluster;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<MyCluster> {

    private final IconGenerator iconGenerator;
    private final int markerWidth;
    private final int markerHeight;

    private final LinearLayout linearLayout;
    private TextView theText;
    private final ImageView img;

    public MyClusterManagerRenderer(Context context, GoogleMap mMap, ClusterManager<MyCluster> clusterManager)
    {
        super(context.getApplicationContext(), mMap, clusterManager);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsFrame = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        linearLayout.setLayoutParams(layoutParamsFrame);

        img = new ImageView(context);

        theText = new TextView(context);
        theText.setTextSize(16);
        theText.setTextColor(Color.BLACK);
        theText.setTypeface(Typeface.DEFAULT_BOLD);

        iconGenerator = new IconGenerator(context);

        markerWidth = (int)context.getResources().getDimension(R.dimen.custom_marker_image_width);
        markerHeight = (int)context.getResources().getDimension(R.dimen.custom_marker_image_height);
        img.setLayoutParams(new ViewGroup.LayoutParams(markerWidth,markerHeight));
        theText.setLayoutParams(new ViewGroup.LayoutParams(markerWidth,markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        linearLayout.setPadding(padding,padding,padding,padding);
        linearLayout.addView(img);
        linearLayout.addView(theText);
        iconGenerator.setContentView(linearLayout);
    }

    public MyClusterManagerRenderer(Context context, GoogleMap mMap,ClusterManager<MyCluster> clusterManager, boolean noPrice)
    {
        super(context.getApplicationContext(), mMap, clusterManager);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsFrame = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        linearLayout.setLayoutParams(layoutParamsFrame);

        img = new ImageView(context);

        iconGenerator = new IconGenerator(context);
        markerWidth = (int)context.getResources().getDimension(R.dimen.custom_marker_image_width);
        markerHeight = (int)context.getResources().getDimension(R.dimen.custom_marker_image_height);
        img.setLayoutParams(new ViewGroup.LayoutParams(markerWidth,markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        linearLayout.setPadding(padding,padding,padding,padding);
        linearLayout.addView(img);
        iconGenerator.setContentView(linearLayout);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MyCluster item, @NonNull MarkerOptions markerOptions) {
        //img.setImageResource(item.getImageIcon());
        img.setImageBitmap(item.getImageIcon());
        if(theText != null)
            theText.setText(item.getPrice());
        Bitmap bmp = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp)).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<MyCluster> cluster) {
        return false;
    }
}
