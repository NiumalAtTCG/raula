package lk.javainstitute.raula.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lk.javainstitute.raula.R;

public class SelectedPackagesAdapter extends RecyclerView.Adapter<SelectedPackagesAdapter.SelectedPackageViewHolder> {

    private Cursor cursor;
    private Context context;

    public SelectedPackagesAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        Log.d("SelectedPackagesAdapter", "Adapter initialized with " + (cursor != null ? cursor.getCount() : 0) + " items");
    }

    public Cursor getItem(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            return cursor;
        }
        return null;
    }

    public class SelectedPackageViewHolder extends RecyclerView.ViewHolder {
        TextView PackageName, PackageDescription, PackagePrice;

        public SelectedPackageViewHolder(@NonNull View itemView) {
            super(itemView);
            PackageName = itemView.findViewById(R.id.tv_package_name);
            PackageDescription = itemView.findViewById(R.id.tv_package_description);
            PackagePrice = itemView.findViewById(R.id.tv_package_price);
        }
    }

    @NonNull
    @Override
    public SelectedPackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_package, parent, false);
        return new SelectedPackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedPackageViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            String packageName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String packageDescription = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));

            Log.d("SelectedPackagesAdapter", "Binding Package at position " + position + ": " + packageName);

            holder.PackageName.setText(packageName);
            holder.PackageDescription.setText(packageDescription);
            holder.PackagePrice.setText("Price: Rs:" + price);
        } else {
            Log.d("SelectedPackagesAdapter", "Cursor ERROR: Cannot move to position " + position);
        }
    }



    @Override
    public int getItemCount() {
        Log.d("SelectedPackagesAdapter", "Item count: " + ((cursor != null) ? cursor.getCount() : 0));
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public void updateCursor(Cursor newCursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();  // Close old cursor
        }
        cursor = newCursor;
        notifyDataSetChanged();  // Ensure RecyclerView updates
    }


}
