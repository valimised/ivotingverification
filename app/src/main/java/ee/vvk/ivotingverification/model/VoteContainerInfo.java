package ee.vvk.ivotingverification.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class VoteContainerInfo implements Parcelable {

    public static final Parcelable.Creator<VoteContainerInfo> CREATOR =
            new Parcelable.Creator<VoteContainerInfo>() {
                @Override
                public VoteContainerInfo createFromParcel(Parcel source) {
                    return new VoteContainerInfo(source);
                }

                @Override
                public VoteContainerInfo[] newArray(int size) {
                    return new VoteContainerInfo[size];
                }
            };
    private final String signerCN;
    private final ArrayList<Vote> voteList;

    public VoteContainerInfo(String signerCN, ArrayList<Vote> voteList) {
        this.signerCN = signerCN;
        this.voteList = voteList;
    }

    private VoteContainerInfo(Parcel source) {
        signerCN = source.readString();
        voteList = source.readArrayList(Vote.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(signerCN);
        dest.writeList(voteList);
    }

    public String getSignerCN() {
        return signerCN;
    }

    public ArrayList<Vote> getVoteList() {
        return voteList;
    }
}
