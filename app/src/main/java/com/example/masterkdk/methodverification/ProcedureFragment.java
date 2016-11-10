package com.example.masterkdk.methodverification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.DataStructureUtil.ProcItem;
import com.example.masterkdk.methodverification.Util.ProcedureComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ProcedureFragment extends Fragment {

    private int mColumnCount = 1;

    private int mCurrentPos = 0;
    private int mLastInSno = 0;
    private OnListFragmentInteractionListener mListener;
    private ProcedureRecyclerViewAdapter mRecyclerViewAdapter;
    private List<ProcItem> mItems;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProcedureFragment() {
    }

    @SuppressWarnings("unused")
    public static ProcedureFragment newInstance(int columnCount) {
        ProcedureFragment fragment = new ProcedureFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 手順データをIntentから取得
        Intent intent = getActivity().getIntent();
        String resultSt = intent.getStringExtra("resultStTmp");  // 指示者タブレットの調整箇所

        // 手順データを解析し、tejunを取り出す
        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(resultSt);
        Bundle tmpBundle = dsHelper.getRecievedData();

        View view = inflater.inflate(R.layout.fragment_procedure_list, container, false);

        // アダプターにセット
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new ProcedureRecyclerViewAdapter(dsHelper.ITEMS, mListener));

            // Adapterへの参照
            mRecyclerViewAdapter = (ProcedureRecyclerViewAdapter)recyclerView.getAdapter();
            mItems = dsHelper.ITEMS;
        }
        int last = dsHelper.ITEMS.size()-1;

        // TODO: コメントではない最終行を取得する
        mLastInSno = getLastPorcedure();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public int getCurrentPos(){
        // 現在の手順の位置を取得する
        return mCurrentPos;
    }
    public int getLastInSno(){
        // 最終手順の in_sno を取得する
        return mLastInSno;
    }
    public ProcItem getCurrentItem(){
        // 現在の手順の行を取得する
        return mItems.get(mCurrentPos);
    }
    public void setFirstProcedure(){
        int pos = 0;

        // コメント行を無視して、最初の手順を取得する
        while(mItems.get(pos).tx_sno.equals("C")){
            pos++;
        }
        // コメントスキップ後、最初に"0"の手順を 開始位置とする
        boolean done = false; // 実行済み手順判定
        int execProcPosition = 0; // 実行中の手順ポジション
        while(!mItems.get(pos).cd_status.equals("0")){
            if(mItems.get(pos).cd_status.equals("7")){
                done = true;
            }
            if(mItems.get(pos).cd_status.equals("1")){
                execProcPosition=pos; //　最新の実行中を退避（スキップを考慮）
                done = true;
            }
            pos++;
        }

        if(execProcPosition>0){
            // 実行中手順があった場合、現在位置を事項中の手順に設定
            mCurrentPos = execProcPosition;
            setProcStatus(mCurrentPos, mItems.get(mCurrentPos).cd_status, mItems.get(mCurrentPos).ts_b, mItems.get(mCurrentPos).bo_gs,mItems.get(mCurrentPos).tx_gs);
        }else {
            // 現在位置を設定
            mCurrentPos = pos;
            setProcStatus(mCurrentPos, "1","","False","");
        }

        if(done){ // 実行済み手順あり（手順再開）の場合、一つ前の手順までスクロール
            RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.ProcedureList);
            recyclerView.scrollToPosition(mCurrentPos-1);
        }

    }

    private int getLastPorcedure(){
        int pos = 0;

        // 手順一覧を逆順にして、最終手順を取得する
        List<ProcItem> reverse = new ArrayList<ProcItem>(mItems);
        Collections.reverse(reverse);

        while(reverse.get(pos).tx_sno.equals("C") || reverse.get(pos).tx_sno.equals("")){
            pos++;
        }
        return reverse.get(pos).in_sno;
    }

    public void setProcStatus(int pos, String status, String ts_b, String bo_gs, String tx_gs){

        // ヘッダーに表示するため、対象の指示をActivityへ通知
        mRecyclerViewAdapter.setActivate(pos, status, ts_b, bo_gs, tx_gs);
        Bundle rcBundle = new Bundle();
        rcBundle.putString("tx_sno",mItems.get(pos).tx_sno);
        rcBundle.putString("tx_s_l",mItems.get(pos).tx_s_l);
        rcBundle.putString("tx_action",mItems.get(pos).tx_action);
        rcBundle.putString("tx_b_l",mItems.get(pos).tx_b_l);
        rcBundle.putString("tx_action",mItems.get(pos).tx_action);
        rcBundle.putString("tx_biko",mItems.get(pos).tx_biko);
        rcBundle.putString("cd_status",mItems.get(pos).cd_status);

        ((OnListFragmentInteractionListener)getActivity()).onListFragmentInteraction(rcBundle);

    }
    public void updateProcedure(){
        // 次の手順へ進める
        int nextPos = mCurrentPos + 1;

        // コメント行を無視して、次の手順を取得する
        while(mItems.get(nextPos).tx_sno.equals("C")){
            nextPos++;  // 次がコメントの時は一つ進める
        }

        // 対象の指示を更新
        mItems.get(nextPos).cd_status="1";

        // 次の手順を Activityへ通知
        setProcStatus(nextPos, mItems.get(nextPos).cd_status, "","False","");

        // RecyclerViewを更新
        mCurrentPos=nextPos;
        mRecyclerViewAdapter.notifyDataSetChanged();
    }
    public void addProcedure(){
        // 現場差異で追加の時、表示の更新のみ行う
        mRecyclerViewAdapter.notifyDataSetChanged();
    }
    public void updateLastProcedure(){
        // 最後の手順の場合、手順を進めずに表示のみ更新
        mRecyclerViewAdapter.notifyDataSetChanged();
    }
    public int getCurrentInSno() {
        // システム用の手順書番号in_snoを取得。activityでonListItemClickイベント以外でも取得できる
        ProcItem data = mRecyclerViewAdapter.getItem(mCurrentPos);
        return data.in_sno;
    }

    /**
     リスナー
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(Bundle bundle);
        void onListItemClick(ProcItem item);
    }
}
