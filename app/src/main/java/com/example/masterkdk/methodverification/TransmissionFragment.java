package com.example.masterkdk.methodverification;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.masterkdk.methodverification.loader.SendRequestLoader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransmissionFragment.TransmissionFragmentListener} interface
 * to handle interaction events.
 * Use the {@link TransmissionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransmissionFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>{
    private static final String ARG_HOST = "host";
    private static final String ARG_PORT = "port";

    private String mHost;
    private int mPort;

    private TransmissionFragmentListener mListener;

    public TransmissionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param host server host.
     * @param port server port.
     * @return A new instance of fragment TransmissionFragment.
     */
    public static TransmissionFragment newInstance(String host, int port) {
        TransmissionFragment fragment = new TransmissionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOST, host);
        args.putInt(ARG_PORT, port);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHost = getArguments().getString(ARG_HOST);
            mPort = getArguments().getInt(ARG_PORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        return textView;
    }

    @Override
    public void onAttach(Context context) {
        // 発火しない？
        super.onAttach(context);
        if (context instanceof TransmissionFragmentListener) {
            mListener = (TransmissionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TransmissionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        // 発火しない？
        super.onDetach();
        mListener = null;
    }

    /* Loader */
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        if(args != null) {
            String data = args.getString("Data");
            return  new SendRequestLoader(getActivity(), mHost, mPort, data);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        int id = loader.getId();

        ((TransmissionFragmentListener)getActivity()).onResponseRecieved(data);  // Activity event
        getLoaderManager().destroyLoader(id);  // ローダーを破棄
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    public void send(String data){
        System.out.println("send");
        if(data != null) {
            Bundle args = new Bundle();
            args.putString("Data",data);
            // Loaderを初期化する
            getLoaderManager().initLoader(0, args, this);  // onCreateLoaderが呼ばれる
        }
    }
    /**

     */
    public interface TransmissionFragmentListener {
        void onResponseRecieved(String data);
        void onFinishTransmission(String data);
    }

    /* Loader */
}
