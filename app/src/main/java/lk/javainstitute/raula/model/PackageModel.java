package lk.javainstitute.raula.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PackageModel implements Parcelable {
    private String id;  // Add ID field
    private String name;
    private String description;
    private double price;
    private String category;

    public PackageModel(String id, String name, String description, double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    protected PackageModel(Parcel in) {
        id = in.readString();  // Read ID
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        category = in.readString();
    }

    public static final Creator<PackageModel> CREATOR = new Creator<PackageModel>() {
        @Override
        public PackageModel createFromParcel(Parcel in) {
            return new PackageModel(in);
        }

        @Override
        public PackageModel[] newArray(int size) {
            return new PackageModel[size];
        }
    };

    // Getter for ID
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);  // Write ID
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(category);
    }
}
