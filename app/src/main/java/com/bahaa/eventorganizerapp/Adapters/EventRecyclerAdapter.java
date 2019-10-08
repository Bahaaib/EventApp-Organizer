package com.bahaa.eventorganizerapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bahaa.eventorganizerapp.Models.EventModel;
import com.bahaa.eventorganizerapp.R;
import com.bahaa.eventorganizerapp.Root.EventAdapterListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventRecyclerAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<EventModel> adapterModel;
    private DialogInterface.OnClickListener dialogClickListener;
    private EventAdapterListener eventAdapterListener;


    public EventRecyclerAdapter(Context context, ArrayList<EventModel> adapterModel) {
        this.context = context;
        this.adapterModel = adapterModel;
        eventAdapterListener = (EventAdapterListener) context;


    }


    //Here We tell the RecyclerView what to show at each element of it..it'd be a cardView!
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.events_card, parent, false);
        return new EventViewHolder(view);
    }

    //Here We tell the RecyclerView what to show at each CardView..
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((EventViewHolder) holder).BindView(position);

    }

    @Override
    public int getItemCount() {
        return adapterModel.size();
    }

    private void displayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.event_removal)).setPositiveButton(context.getString(R.string.yes_sure), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no_sure), dialogClickListener).show();
    }

    //Here we bind all the children views of each cardView with their corresponding
    // actions to show & interact with them
    public class EventViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.future_events_image)
        public ImageView futureImage;

        @BindView(R.id.future_events_date)
        public TextView futureDate;

        @BindView(R.id.future_events_title)
        public TextView futureTitle;

        @BindView(R.id.future_events_card)
        public CardView eventCard;


        EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        //Here where all the glory being made..!
        void BindView(final int position) {
            Picasso.get()
                    .load(adapterModel.get(position).getImage())
                    .fit()
                    .into(futureImage);

            futureDate.setText(adapterModel.get(position).getStartDate());

            setAdjustedTitle(futureTitle, position, 90);

            eventCard.setOnLongClickListener(v -> {

                dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            EventModel headModel;
                            adapterModel.get(position).setOrganizer(null);
                            adapterModel.get(position).setTitle(null);
                            adapterModel.get(position).setAddress(null);
                            adapterModel.get(position).setCapacity(null);
                            adapterModel.get(position).setDescription(null);
                            adapterModel.get(position).setDistance(null);
                            adapterModel.get(position).setStartDate(null);
                            adapterModel.get(position).setEndDate(null);
                            adapterModel.get(position).setStartTime(null);
                            adapterModel.get(position).setLatitude(null);
                            adapterModel.get(position).setLongitude(null);
                            adapterModel.get(position).setImage(null);
                            adapterModel.get(position).setTicketPrice(null);
                            adapterModel.get(position).setTicketsAvailable(null);
                            adapterModel.get(position).setCapacity(null);

                            headModel = adapterModel.get(position);

                            eventAdapterListener.onDataRemoved(headModel);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                };

                displayDialog();

                return true;
            });
        }

        private void setAdjustedTitle(TextView tv, int cardPos, int maxLength) {
            tv.setText(adapterModel.get(cardPos).getTitle());
            ViewTreeObserver treeObserver = tv.getViewTreeObserver();
            treeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver observer = tv.getViewTreeObserver();
                    observer.removeOnGlobalLayoutListener(this);
                    int titleLen = tv.length();
                    if (titleLen > maxLength) {
                        String text = tv.getText().toString();
                        text = text.replace(text.substring(maxLength, titleLen), "");
                        text = text + "...";
                        tv.setText(text);
                    }
                }
            });
        }


        @OnClick(R.id.future_events_card)
        void openEvent() {
            // Intent intent = new Intent(context, DetailsActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //context.startActivity(intent);
        }

    }
}
