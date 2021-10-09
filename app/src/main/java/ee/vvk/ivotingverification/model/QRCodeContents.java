package ee.vvk.ivotingverification.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import ee.vvk.ivotingverification.exceptions.InvalidQrCodeException;
import ee.vvk.ivotingverification.util.RegexMatcher;

public class QRCodeContents implements Parcelable {

    private final String sessionId;
    private final byte[] rndSeed;
    private final String voteId;

    public static final Parcelable.Creator<QRCodeContents> CREATOR =
            new Parcelable.Creator<QRCodeContents>() {
                @Override
                public QRCodeContents createFromParcel(Parcel source) {
                    return new QRCodeContents(source);
                }

                @Override
                public QRCodeContents[] newArray(int size) {
                    return new QRCodeContents[size];
                }
            };

    public QRCodeContents(String qrCodeMessage) throws InvalidQrCodeException {
        if (qrCodeMessage != null && RegexMatcher.isCorrectQR(qrCodeMessage)) {
            String[] splitQr = qrCodeMessage.split("\n");
            this.sessionId = splitQr[0];
            this.rndSeed = Base64.decode(splitQr[1], Base64.DEFAULT);
            this.voteId = splitQr[2];
        }
        else {
            throw new InvalidQrCodeException();
        }
    }

    private QRCodeContents(Parcel source) {
        sessionId = source.readString();
        rndSeed = new byte[source.readInt()];
        source.readByteArray(rndSeed);
        voteId = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sessionId);
        dest.writeInt(rndSeed.length);
        dest.writeByteArray(rndSeed);
        dest.writeString(voteId);
    }

    public String getSessionId() {
        return sessionId;
    }

    public byte[] getRndSeed() {
        return rndSeed;
    }

    public String getVoteId() {
        return voteId;
    }

}
