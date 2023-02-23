package ee.vvk.ivotingverification.tasks;

import android.util.Base64;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.model.VoteContainerInfo;
import ee.vvk.ivotingverification.model.VerificationProfile;
import ee.vvk.ivotingverification.util.BDocContainer;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.JsonRpc;
import ee.vvk.ivotingverification.util.TlsConnection;
import ee.vvk.ivotingverification.util.Util;

public class GetVoteTask extends BaseTask<VoteContainerInfo> {

    private static final String TAG = GetVoteTask.class.getSimpleName();

    private final AsyncTaskActivity<VoteContainerInfo> activity;
    private final QRCodeContents qrCodeContents;
    private final VerificationProfile verificationProfile;

    public GetVoteTask(AsyncTaskActivity<VoteContainerInfo> activity, QRCodeContents qrCodeContents, VerificationProfile verificationProfile) {
        this.activity = activity;
        this.qrCodeContents = qrCodeContents;
        this.verificationProfile = verificationProfile;
    }

    @Override
    public void setUiForLoading() {
        activity.onPreExecute();
    }

    @Override
    public VoteContainerInfo call() throws Exception {
        InputStream response;
        Map<String, Object> params = new HashMap<>();
        params.put("voteid", qrCodeContents.getVoteId());
        params.put("sessionid", qrCodeContents.getSessionId());
        ByteBuffer buf = JsonRpc.createRequest(JsonRpc.Method.VERIFY, params);
        TlsConnection tls = new TlsConnection(C.verificationTlsArray);
        response = tls.sendRequest(C.verificationUrlArray, C.verificationSNI, buf);
        return readResponse(response);
    }

    private VoteContainerInfo readResponse(InputStream in) throws Exception {
        JsonRpc.Response res = JsonRpc.unmarshalResponse(JsonRpc.Method.VERIFY, in);
        if (res.error != null) {
            Util.logError(TAG, res.error);
            return null;
        }

        byte[] containerData = Base64.decode((String) res.result.get("Vote"), Base64.DEFAULT);
        byte[] ocspData = Base64.decode((String) res.result.get("ocsp"), Base64.DEFAULT);
        byte[] regData = Base64.decode((String) res.result.get("tspreg"), Base64.DEFAULT);

        BDocContainer container = BDocContainer.getVerifiedContainer(verificationProfile,
                containerData, ocspData, regData);

        String signerCN = container.getSignerCN();

        if (container.getVotes() != null) {
            ArrayList<Vote> voteList = new ArrayList<>();
            for (Map.Entry<String, byte[]> vote : container.getVotes().entrySet()) {
                voteList.add(new Vote(vote.getKey(), vote.getValue()));
            }
            return new VoteContainerInfo(signerCN, voteList);
        }

        return null;

    }

    @Override
    public void setDataAfterLoading(VoteContainerInfo result) {
        activity.onPostExecute(result);
    }
}


