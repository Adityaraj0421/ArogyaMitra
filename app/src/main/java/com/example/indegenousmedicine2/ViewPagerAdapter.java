package com.example.indegenousmedicine2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<ViewPagerItem> viewPagerItems;

    public ViewPagerAdapter(List<ViewPagerItem> viewPagerItems) {
        this.viewPagerItems = viewPagerItems;
    }

    @Override
    public int getCount() {
        return viewPagerItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(R.layout.view_pager_item, container, false);

        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        TextView tag = view.findViewById(R.id.featureTagTextView);

        ViewPagerItem item = viewPagerItems.get(position);

        title.setText(item.getTitle());
        description.setText(item.getDescription());
        // Tag is optional — set a numbered label for each card
        if (tag != null) {
            tag.setText("Feature " + (position + 1));
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
