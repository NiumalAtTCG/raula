package lk.javainstitute.raula.adapters;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.javainstitute.raula.R;
import lk.javainstitute.raula.model.PackageModel;

public class PackageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BASIC = 0;
    private static final int TYPE_PREMIUM = 1;

    private List<PackageModel> basicPackageList;
    private List<PackageModel> premiumPackageList;
    private OnPackageSelectedListener listener;

    public PackageAdapter(List<PackageModel> basicPackageList, List<PackageModel> premiumPackageList, OnPackageSelectedListener listener) {
        this.basicPackageList = basicPackageList;
        this.premiumPackageList = premiumPackageList;
        this.listener = listener;
    }

    public interface OnPackageSelectedListener {
        void onPackageSelected(PackageModel packageModel, boolean isSelected);
    }

    @Override
    public int getItemViewType(int position) {
        return (position < basicPackageList.size()) ? TYPE_BASIC : TYPE_PREMIUM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_BASIC) {
            return new BasicPackageViewHolder(inflater.inflate(R.layout.item_package_basic, parent, false));
        } else {
            return new PremiumPackageViewHolder(inflater.inflate(R.layout.item_package_premium, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BasicPackageViewHolder) {
            ((BasicPackageViewHolder) holder).bind(basicPackageList.get(position));
        } else {
            int adjustedPosition = position - basicPackageList.size();
            ((PremiumPackageViewHolder) holder).bind(premiumPackageList.get(adjustedPosition));
        }
    }

    @Override
    public int getItemCount() {
        return basicPackageList.size() + premiumPackageList.size();
    }

    public class BasicPackageViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, description, price;
        Button bookButton;

        public BasicPackageViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconHaircut);
            title = itemView.findViewById(R.id.textPackage1);
            description = itemView.findViewById(R.id.descPackage1);
            price = itemView.findViewById(R.id.pricePackage1);
            bookButton = itemView.findViewById(R.id.btnBookPackage1);

            bookButton.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

                boolean isSelected = !bookButton.isSelected();
                bookButton.setSelected(isSelected);
                listener.onPackageSelected(basicPackageList.get(getAdapterPosition()), isSelected);
            });
        }

        public void bind(PackageModel packageModel) {
            title.setText(packageModel.getName());
            description.setText(packageModel.getDescription());
            price.setText("Rs:" + packageModel.getPrice());
        }
    }

    public class PremiumPackageViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, description, price;
        Button bookButton;

        public PremiumPackageViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconPremium);
            title = itemView.findViewById(R.id.textPackage2);
            description = itemView.findViewById(R.id.descPackage2);
            price = itemView.findViewById(R.id.pricePackage2);
            bookButton = itemView.findViewById(R.id.btnBookPackage2);

            bookButton.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                boolean isSelected = !bookButton.isSelected();
                bookButton.setSelected(isSelected);
                listener.onPackageSelected(premiumPackageList.get(getAdapterPosition() - basicPackageList.size()), isSelected);
            });
        }

        public void bind(PackageModel packageModel) {
            title.setText(packageModel.getName());
            description.setText(packageModel.getDescription());
            price.setText("$" + packageModel.getPrice());
        }

    }
}
