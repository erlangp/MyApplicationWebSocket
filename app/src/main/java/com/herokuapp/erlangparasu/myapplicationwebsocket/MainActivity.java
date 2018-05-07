package com.herokuapp.erlangparasu.myapplicationwebsocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketListener;

import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Tag." + MainActivity.class.getSimpleName();

    private TextView mTextView;

    private Disposable mDisposable;
    private WebSocket mWebSocket;
    private WebSocketListener mWsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text_view_1);

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");

                try {
                    connectToWsServer();
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "onClick: IOException=" + e.getMessage());
                } catch (WebSocketException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "onClick: WebSocketException=" + e.getMessage());
                }
            }
        });
    }

    private void connectToWsServer() throws IOException, WebSocketException {
        Log.d(TAG, "connectToWsServer: ");

        Single.fromCallable(new Callable<WebSocket>() {
            @Override
            public WebSocket call() throws Exception {
                Log.d(TAG, "call: ");

                WebSocketFactory factory = new WebSocketFactory();
                factory.setConnectionTimeout(5000);
                // Create a WebSocketFactory instance.
                WebSocket ws = factory.createSocket("ws://10.0.2.2:8092");
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
                            mWebSocket.removeListener(mWsListener);
                        } catch (Exception e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }

                            Log.d(TAG, "onSuccess: Exception=" + e.getMessage());
                        }

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
                        mWsListener = getNewWsListener();
                        mWebSocket.addListener(mWsListener);
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, "onError: Throwable=" + e.getMessage());

                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText("\n" + mTextView.getText());
                                mTextView.setText(e.getMessage() + mTextView.getText());
                            }
                        });
                    }
                });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");

        super.onResume();

        try {
            connectToWsServer();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

            Log.d(TAG, "onClick: IOException=" + e.getMessage());
        } catch (WebSocketException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

            Log.d(TAG, "onClick: WebSocketException=" + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");

        super.onPause();

        try {
            mWebSocket.removeListener(mWsListener);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

            Log.d(TAG, "onPause: Exception=" + e.getMessage());
        }

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

    private WebSocketListener getNewWsListener() {
        Log.d(TAG, "getNewWsListener: ");

        return new WebSocketAdapter() {
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
        };
    }
}
