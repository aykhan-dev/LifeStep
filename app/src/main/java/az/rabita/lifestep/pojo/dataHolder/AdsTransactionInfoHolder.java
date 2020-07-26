package az.rabita.lifestep.pojo.dataHolder;

import android.os.Parcel;
import android.os.Parcelable;

public class AdsTransactionInfoHolder implements Parcelable {

    private String transactionId;
    private String videoUrl;
    private String description;
    private String logoUrl;
    private String title;
    private String subtitle;
    private String openingUrl;

    private int watchTime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.transactionId);
        dest.writeString(this.videoUrl);
        dest.writeString(this.description);
        dest.writeString(this.logoUrl);
        dest.writeString(this.title);
        dest.writeString(this.subtitle);
        dest.writeString(this.openingUrl);
        dest.writeInt(this.watchTime);
    }

    public AdsTransactionInfoHolder(String transactionId, String videoUrl, String description, String logoUrl, String title, String subtitle, String openingUrl, int watchTime) {
        this.transactionId = transactionId;
        this.videoUrl = videoUrl;
        this.description = description;
        this.logoUrl = logoUrl;
        this.title = title;
        this.subtitle = subtitle;
        this.openingUrl = openingUrl;
        this.watchTime = watchTime;
    }

    protected AdsTransactionInfoHolder(Parcel in) {
        this.transactionId = in.readString();
        this.videoUrl = in.readString();
        this.description = in.readString();
        this.logoUrl = in.readString();
        this.title = in.readString();
        this.subtitle = in.readString();
        this.openingUrl = in.readString();
        this.watchTime = in.readInt();
    }

    public static final Creator<AdsTransactionInfoHolder> CREATOR = new Creator<AdsTransactionInfoHolder>() {
        @Override
        public AdsTransactionInfoHolder createFromParcel(Parcel source) {
            return new AdsTransactionInfoHolder(source);
        }

        @Override
        public AdsTransactionInfoHolder[] newArray(int size) {
            return new AdsTransactionInfoHolder[size];
        }
    };

    public String getTransactionId() {
        return transactionId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getOpeningUrl() {
        return openingUrl;
    }

    public int getWatchTime() {
        return watchTime;
    }
}
