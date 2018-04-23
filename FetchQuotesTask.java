package com.kogul.stockboard;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class FetchQuotesTask extends
        AsyncTask<FetchQuotesTask.Parameters, Void, FetchQuotesTask.Result> {

    private static final String LOG_TAG = FetchQuotesTask.class.getSimpleName();

    private final ResultCallback resultCallback;

    public interface ResultCallback {
         void onDone(Result result);
    }

    public static class Parameters {
        public List<String> symbols;
    }

    public static class Result {
        public Exception exception;
        public String[][] data;
    }

    public FetchQuotesTask(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    @Override
    protected Result doInBackground(Parameters... params) {
        Result result = new Result();
        try {
            String response;
            URL url = new URL(buildURL(params[0]));
            response = WebClient.get(url);
            BufferedReader reader = new BufferedReader(new StringReader(
                    response));
            List<String[]> data = new ArrayList<String[]>();
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                data.add(parseLine(line));
            }
            result.data = data.toArray(new String[data.size()][]);
        } catch (Exception e) {
            Log.e(LOG_TAG, "comm error", e);
            result.exception = e;
        }
        return result;
    }

    private String[] parseLine(String line) {
        List<String> list = new ArrayList<String>();
        int length = line.length();
        int start = 0;
        boolean inQuote = false;
        for (int i = 0; i < length; ++i) {
            char c = line.charAt(i);
            if (c == '"')
                inQuote = !inQuote;
            else if (c == ',' && !inQuote) {
                list.add(getWord(line, start, i));
                start = i + 1;
            }
        }
        list.add(getWord(line, start, length));
        return list.toArray(new String[list.size()]);
    }

    private String getWord(String line, int start, int end) {
        if (end - start > 1 && line.charAt(start) == '"'
                && line.charAt(end - 1) == '"') {
            ++start;
            --end;
        }
        return line.substring(start, end);
    }

    private String buildURL(Parameters params) {
        StringBuilder sValue = new StringBuilder();
        for (String s : params.symbols) {
            if (sValue.length() > 0) {
                sValue.append('+');
            }
            sValue.append(s);
        }
        StringBuilder fValue = new StringBuilder();
        return String.format("https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols="+MainActivity.DEFAULT_SYMBOLS+"&apikey=Y7E1YNJR5RGHPY16&datatype=csv",
                sValue, fValue);
    }

    @Override
    protected void onPostExecute(Result result) {
        resultCallback.onDone(result);
    }
}