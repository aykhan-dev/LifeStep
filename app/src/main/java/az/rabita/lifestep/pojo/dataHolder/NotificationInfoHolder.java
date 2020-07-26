package az.rabita.lifestep.pojo.dataHolder;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationInfoHolder implements Parcelable {

    private int type;

    private String userId;

    public NotificationInfoHolder(int type, String userId) {
        this.type = type;
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.userId);
    }

    protected NotificationInfoHolder(Parcel in) {
        this.type = in.readInt();
        this.userId = in.readString();
    }

    public static final Parcelable.Creator<NotificationInfoHolder> CREATOR = new Parcelable.Creator<NotificationInfoHolder>() {
        @Override
        public NotificationInfoHolder createFromParcel(Parcel source) {
            return new NotificationInfoHolder(source);
        }

        @Override
        public NotificationInfoHolder[] newArray(int size) {
            return new NotificationInfoHolder[size];
        }
    };

}
