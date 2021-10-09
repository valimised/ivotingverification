package ee.vvk.ivotingverification.tasks;

import java.io.InputStream;

import ee.vvk.ivotingverification.util.HttpRequest;

public class GetConfigTask extends BaseTask<String> {

    private final AsyncTaskActivity<String> activity;
    private final InputStream truststore;
    private final String url;

    public GetConfigTask(AsyncTaskActivity<String> activity, InputStream truststore, String url) {
        this.activity = activity;
        this.truststore = truststore;
        this.url = url;
    }

	@Override
    public void setUiForLoading() {
        activity.onPreExecute();
    }

    @Override
    public String call() throws Exception {
        return new HttpRequest(truststore).get(url);
    }

    @Override
    public void setDataAfterLoading(String result) {
        activity.onPostExecute(result);
    }

}

