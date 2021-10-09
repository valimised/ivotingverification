package ee.vvk.ivotingverification.tasks;

public interface AsyncTaskActivity<R> {
  void onPreExecute();
  void onPostExecute(R result);
}
