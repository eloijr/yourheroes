package com.runze.yourheroes.net;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.runze.yourheroes.utilities.Action;
import com.runze.yourheroes.utilities.Tools;

/**
 * Created by Eloi Jr on 12/01/2015.
 */
public class ConnectTask extends AsyncTask<Void, Void, Boolean> {

    private final static String LOG_TAG = ConnectTask.class.getSimpleName();

    private Context context;
    private Action action;
    private int codMsg;

    private Throwable exceptionError;
    private Dialog dialog;

    public ConnectTask(Context context, Action action, int codMsg) {
        this.context = context;
        this.action = action;
        this.codMsg = codMsg;
        if (codMsg == 0)
            dialog = null;
        else
            dialog = Tools.dialogSpinner(context, context.getString(this.codMsg));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (dialog != null)
            dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            action.execute();
        } catch (Throwable e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            this.exceptionError = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean ok) {
        super.onPostExecute(ok);
        if (dialog != null)
            dialog.dismiss();
        if (ok)
            action.updateView();
        else
            Tools.alertDialog(context, "Erro: " + this.exceptionError.getMessage());

    }

}
