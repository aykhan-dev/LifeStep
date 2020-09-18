package az.rabita.lifestep.pojo.dataHolder;

import android.os.Parcel;
import android.os.Parcelable;

public class UserProfileInfoHolder implements Parcelable {

    private String userId;
    private Long balance;

    public UserProfileInfoHolder(String userId, Long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeLong(this.balance);
    }

    protected UserProfileInfoHolder(Parcel in) {
        this.userId = in.readString();
        this.balance = in.readLong();
    }

    public static final Parcelable.Creator<UserProfileInfoHolder> CREATOR = new Parcelable.Creator<UserProfileInfoHolder>() {
        @Override
        public UserProfileInfoHolder createFromParcel(Parcel source) {
            return new UserProfileInfoHolder(source);
        }

        @Override
        public UserProfileInfoHolder[] newArray(int size) {
            return new UserProfileInfoHolder[size];
        }
    };

}
