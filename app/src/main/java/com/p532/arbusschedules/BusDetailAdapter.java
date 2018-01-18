package com.p532.arbusschedules;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.List;

/**
 * Created by sudhanshu on 12/4/17.
 */

public class BusDetailAdapter extends RecyclerView.Adapter<BusDetailAdapter.BusDetailViewHolder>{

    private Context baseContext;
    private List<BusDetail> busDetailList;

    public class BusDetailViewHolder extends RecyclerView.ViewHolder {

        public TextView busNumber, busTiming;

        public BusDetailViewHolder(View view){
            super(view);
            busNumber = (TextView) view.findViewById(R.id.busnum_title);
            busTiming = (TextView) view.findViewById(R.id.bustiming);
        }
    }

    public BusDetailAdapter(Context baseContext, List<BusDetail> busDetailList){
        this.baseContext = baseContext;
        this.busDetailList = busDetailList;
    }

    @Override
    public BusDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.busdetail_card, parent, false);
        return new BusDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BusDetailViewHolder holder, int position) {
        BusDetail busDetail = busDetailList.get(position);
        holder.busNumber.setText(busDetail.getBusNumber());
        holder.busTiming.setText(busDetail.getBusTiming());
        LocalTime busTime = new LocalTime(busDetail.getBusTiming());
        LocalTime nowTime = LocalTime.now();
        int minutesDiff = Math.abs(Minutes.minutesBetween(busTime, nowTime).getMinutes());
        if(minutesDiff <= 5){
            holder.busTiming.setBackgroundResource(R.color.mins5);
        }else if(minutesDiff <= 10){
            holder.busTiming.setBackgroundResource(R.color.mins10);
        }else{
            holder.busTiming.setBackgroundResource(R.color.mins15);
        }
    }

    @Override
    public int getItemCount() {
        return busDetailList.size();
    }
}
