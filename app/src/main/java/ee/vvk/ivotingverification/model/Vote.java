package ee.vvk.ivotingverification.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Vote implements Parcelable {

    public static final Parcelable.Creator<Vote> CREATOR =
            new Parcelable.Creator<Vote>() {
                @Override
                public Vote createFromParcel(Parcel source) {
                    return new Vote(source);
                }

                @Override
                public Vote[] newArray(int size) {
                    return new Vote[size];
                }
            };

    public final String questionId;
    public final byte[] vote;

    public Vote(String questionId, byte[] vote) {
        this.questionId = questionId;
        this.vote = vote;
    }

    private Vote(Parcel source) {
        questionId = source.readString();
        vote = new byte[source.readInt()];
        source.readByteArray(vote);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(questionId);
        dest.writeInt(vote.length);
        dest.writeByteArray(vote);
    }
}
