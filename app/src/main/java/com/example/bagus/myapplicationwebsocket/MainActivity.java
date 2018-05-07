package com.example.bagus.myapplicationwebsocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Tag." + MainActivity.class.getSimpleName();

    private TextView mTextView;
    private WebSocket mWebSocket;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text_view_1);

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Single.fromCallable(new Callable<WebSocket>() {
                    @Override
                    public WebSocket call() throws Exception {
                        Log.d(TAG, "call: ");

                        WebSocketFactory factory = new WebSocketFactory();
                        factory.setConnectionTimeout(5000);
                        // Create a WebSocketFactory instance.
                        WebSocket ws = factory.createSocket("ws://10.0.2.2:8091");
                        ws.connect();

                        return ws;
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<WebSocket>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                Log.d(TAG, "onSubscribe: ");

                                mDisposable = d;
                            }

                            @Override
                            public void onSuccess(WebSocket ws) {
                                Log.d(TAG, "onSuccess: ");

                                try {
                                    mWebSocket.disconnect();
                                    mWebSocket = null;
                                } catch (Exception e) {
                                    if (BuildConfig.DEBUG) {
                                        e.printStackTrace();
                                    }

                                    Log.d(TAG, "onSuccess: Exception=" + e.getMessage());
                                }
                                mWebSocket = ws;

                                mWebSocket.addListener(new WebSocketAdapter() {
                                    @Override
                                    public void onTextMessage(WebSocket websocket, final String text) throws Exception {
                                        super.onTextMessage(websocket, text);

                                        Log.d(TAG, "onTextMessage: text=" + text);

                                        mTextView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText("\n" + mTextView.getText());
                                                mTextView.setText(text + mTextView.getText());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onConnectError(WebSocket websocket, final WebSocketException exception) throws Exception {
                                        super.onConnectError(websocket, exception);

                                        Log.d(TAG, "onConnectError: WebSocketException=" + exception.getMessage());

                                        mTextView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText("\n" + mTextView.getText());
                                                mTextView.setText(exception.getMessage() + mTextView.getText());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(WebSocket websocket, final WebSocketException cause) throws Exception {
                                        super.onError(websocket, cause);

                                        if (BuildConfig.DEBUG) {
                                            cause.printStackTrace();
                                        }

                                        Log.d(TAG, "onError: WebSocketException=" + cause.getMessage());

                                        mTextView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText("\n" + mTextView.getText());
                                                mTextView.setText(cause.getMessage() + mTextView.getText());
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: ");

                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                }

                                Log.d(TAG, "onError: Throwable=" + e.getMessage());

                                mTextView.setText("\n" + mTextView.getText());
                                mTextView.setText(e.getMessage() + mTextView.getText());
                            }
                        });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause: ");

        try {
            mDisposable.dispose();
            mDisposable = null;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

            Log.d(TAG, "onPause: Throwable=" + e.getMessage());
        }
    }
}
