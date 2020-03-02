package com.kassette.wifip2p;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "FileServerAsyncTask";
    private Context context;
    private ResetFileServer listener;

    public FileServerAsyncTask(Context context, ResetFileServer listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface ResetFileServer {
        void resetFileServer(String result);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(TAG, "Server: connection done");
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/kassette-" + System.currentTimeMillis()
                    + ".mp3");

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(TAG, "server: copying files " + f.toString());

            InputStream inputstream = client.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        listener.resetFileServer(result);
    }


    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long startTime = System.currentTimeMillis();

        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
            long endTime = System.currentTimeMillis() - startTime;
            Log.v("", "Time taken to transfer all bytes is : " + endTime);

        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }
}

