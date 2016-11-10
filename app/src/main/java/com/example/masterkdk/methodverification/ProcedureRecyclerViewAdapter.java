package com.example.masterkdk.methodverification;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.masterkdk.methodverification.ProcedureFragment.OnListFragmentInteractionListener;
import com.example.masterkdk.methodverification.Util.DataStructureUtil.ProcItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProcItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ProcedureRecyclerViewAdapter extends RecyclerView.Adapter<ProcedureRecyclerViewAdapter.ViewHolder> {

    private final List<ProcItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ProcedureRecyclerViewAdapter(List<ProcItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0){
            return new ProcedureViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_procedure, parent, false), this);
        }else{
            // コメント行のView
            return new CommentViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_procedure_comment, parent, false), this);
        }

    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if(mValues.get(position).tx_sno.equals("C") || mValues.get(position).tx_sno.equals("")){
            viewType = 1;
        }

        return viewType;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                ((ProcedureViewHolder)holder).onBindItemViewHolder(mValues.get(position));
                break;
            case 1:
                ((CommentViewHolder)holder).onBindItemViewHolder(mValues.get(position));
                break;
        }
    }

    // 操作ボタンクリック
    private void onButtonClick(View v, int position){
        // Activity へ通知
        mListener.onListItemClick(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    /* 通常操作の ViewHolder*/
    public class ProcedureViewHolder extends ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mNumberView;
        public final TextView mPlaceView;
        public final TextView mOperationView;
        public final TextView mRemarksView;
        public ProcItem mItem;

        public FrameLayout mWrapPlace;
        public FrameLayout mWrapOperation;

        private ProcedureRecyclerViewAdapter mAdapter;

        public ProcedureViewHolder(View view, ProcedureRecyclerViewAdapter adapter) {
            super(view);
            mView = view;
            mNumberView = (TextView) view.findViewById(R.id.proc_number);
            mPlaceView = (TextView) view.findViewById(R.id.proc_place);
            mOperationView = (TextView) view.findViewById(R.id.proc_operation);
            mRemarksView = (TextView) view.findViewById(R.id.proc_remarks);

            mWrapPlace = (FrameLayout) view.findViewById(R.id.wrap_place);
            mWrapOperation = (FrameLayout) view.findViewById(R.id.wrap_operation);

            mAdapter = adapter;
            mOperationView.setOnClickListener(this);  //  操作ボタンにリスナー設定

        }

        private boolean noTap = true;  // 未タップフラグ

        public void onClick(View view){

            // 対象の操作の時のみ、Activityへ通知。指示ボタンのタップは一度のみ
            int position = getAdapterPosition();
            if(mValues.get(position).cd_status.equals("1") && noTap) {
                noTap = false;
                mAdapter.onButtonClick(mView, getAdapterPosition());
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mOperationView.getText() + "'";
        }

        private int getColorInt(String code){
            //  色指定（16進 から 10進）
            int color = Color.rgb(
                    Integer.valueOf( code.substring( 0, 2 ), 16 ),
                    Integer.valueOf( code.substring( 2, 4 ), 16 ),
                    Integer.valueOf( code.substring( 3, 6 ), 16 ) );
            return color;
        }
        private void setItemColor(ProcItem data){
            Resources res = this.mView.getResources();

            // 初期設定
            int bgColor = res.getColor(R.color.colorBgTransparent);
            int btnColor = res.getColor(R.color.colorInstructButton);    // 操作ボタンの背景色（水色）
            int bgPlaceColor = res.getColor(R.color.colorBoardEquipmentText);  // 盤名の背景色（濃水色）
            int txtColor= res.getColor(R.color.colorTextBlack);             // 操作ボタンの文字色（黒）
            int bgWrapPlace = res.getColor(R.color.colorBgTransparent);             // 盤名のラップフレームの色（透過）
            int bgWrapOperation = res.getColor(R.color.colorBgTransparent);         // 操作ボタンのラップフレームの色（透過）
            int bgNumberColor = res.getColor(R.color.colorBgTransparent);  // 手順番号欄の背景色
            int bgRemarksColor = res.getColor(R.color.colorBgTransparent); // 確認時刻欄の背景色

            if(data.cd_status.equals("1")){   // 実行中の時
                bgColor = res.getColor(R.color.colorYellowButton);
            }

            if(data.cd_status.equals("1")) {  // 指示

                if(data.bo_gs.equals("True")){
                    // 盤情報周り（黄）
                    bgWrapPlace= res.getColor(R.color.colorYellowButton);
                    bgWrapOperation= res.getColor(R.color.colorYellowButton);
                }
            }else if(data.cd_status.equals("7")){  //完了
                // 確認待機時の着色は、緑が残ってしまうので、要素毎に行う

                if(data.bo_gs.equals("True")){
                    bgPlaceColor = res.getColor(R.color.colorGrayButton);
                    txtColor = res.getColor(R.color.colorText);
                    if(data.tx_gs.equals("スキップ")) {
                        btnColor = res.getColor(R.color.colorGrayButton);

                        bgNumberColor = res.getColor(R.color.colorGrayButton);
                        bgRemarksColor = res.getColor(R.color.colorGrayButton);

                    }else{
                        btnColor = getColorInt(data.tx_clr2);
                    }
                }else {
                    btnColor = getColorInt(data.tx_clr2);
                    bgPlaceColor = res.getColor(R.color.colorBoardEquipmentDoneText);
                    txtColor = res.getColor(R.color.colorText);
                }
            }

            this.mView.setBackgroundColor(bgColor);
            this.mPlaceView.setBackgroundColor(bgPlaceColor);
            this.mOperationView.setBackgroundColor(btnColor);
            this.mOperationView.setTextColor(txtColor);
            this.mWrapPlace.setBackgroundColor(bgWrapPlace);
            this.mWrapOperation.setBackgroundColor(bgWrapOperation);
            this.mNumberView.setBackgroundColor(bgNumberColor);
            this.mRemarksView.setBackgroundColor(bgRemarksColor);
        }
        private String getRemarks(String tx_gs, String remark){
            String rc="";

            Pattern preP = Pattern.compile("^(\\d*):(\\d*):(\\d*)$");
            Matcher preM = preP.matcher(remark);
            Pattern reP = Pattern.compile("^(.*) (\\d*):(\\d*):(\\d*)$");
            Matcher reM = reP.matcher(remark);
            if(preM.find()) {
                // 時刻フォーマットの時(初期表示)
                String[] arrTime = remark.split(":");
                rc = arrTime[0] + ":" + arrTime[1];
            } else if(reM.find()) {
                // 時刻フォーマットの時(再表示)
                rc = reM.group(2) + ":" + reM.group(3);
            } else {
                rc = remark;
            }

            if(tx_gs.equals("追加") && ! rc.equals("")){
                rc = tx_gs + "\r\n" + rc;
            } else {
                rc = tx_gs + rc;
            }

            return rc;
        }
        public void onBindItemViewHolder(final ProcItem data) {

            this.mItem = data;
            this.mNumberView.setText(data.tx_sno);
            this.mPlaceView.setText(data.tx_s_l);
            this.mOperationView.setText(data.tx_action);
            this.mRemarksView.setText(getRemarks(data.tx_gs, data.ts_b));

            setItemColor(data);
        }
    }

    /* コメント行のViewHolder */
    public class CommentViewHolder extends ViewHolder {
        public final TextView mComment;

        public CommentViewHolder(View view, ProcedureRecyclerViewAdapter adapter) {
            super(view);
            mComment = (TextView) itemView.findViewById(R.id.proc_comment);
        }

        public void onBindItemViewHolder(ProcItem data) {
            this.mComment.setText(data.tx_com);
        }
    }

    public void setActivate(int position, String status, String ts_b, String bo_gs, String tx_gs){
        // 該当の手順の状態を更新する

        mValues.get(position).cd_status = status;
        mValues.get(position).ts_b = ts_b;
        mValues.get(position).bo_gs = bo_gs;
        mValues.get(position).tx_gs = tx_gs;
    }
    public ProcItem getItem(int position){
        // 該当の手順を取得する
        return mValues.get(position);

    }
    public ProcItem getPairItem(int sno, String swno){
        for(ProcItem item : mValues){
            if(item.in_swno.equals(swno) && item.in_sno != sno){
                return item;
            }
        }
        return mValues.get(0);
    }
}
