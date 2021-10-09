package ee.vvk.ivotingverification.tasks;

import java.util.ArrayList;

import ee.vvk.ivotingverification.model.Candidate;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.util.ElGamalPub;


public class DecryptionTask extends BaseTask<ArrayList<Candidate>> {

    private final AsyncTaskActivity<ArrayList<Candidate>> activity;

    private final ArrayList<Vote> voteList;
    private final ElGamalPub pub;
    private final byte[] rnd;

    public DecryptionTask(AsyncTaskActivity<ArrayList<Candidate>> activity, ArrayList<Vote> voteList, ElGamalPub pub, byte[] rnd) {
        this.activity = activity;
        this.voteList = voteList;
        this.pub = pub;
        this.rnd = rnd;
    }

    @Override
    public void setUiForLoading() {
        activity.onPreExecute();
    }

    @Override
    public ArrayList<Candidate> call() throws Exception {

        ArrayList<Candidate> result = new ArrayList<>();
        for (Vote vote : voteList) {
            String decChoice = pub.getDecryptedChoice(vote.vote, rnd);
            if (decChoice.equals("")) {
                result.add(Candidate.NO_CHOICE);
            } else {
                result.add(new Candidate(decChoice));
            }
        }
        return result;
    }

    @Override
    public void setDataAfterLoading(ArrayList<Candidate> candidates) {
        activity.onPostExecute(candidates);
    }
}

