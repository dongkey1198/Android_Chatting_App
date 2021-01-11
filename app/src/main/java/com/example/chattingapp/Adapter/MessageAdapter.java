package com.example.chattingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingapp.MessageActivity;
import com.example.chattingapp.Models.Chat;
import com.example.chattingapp.Models.User;
import com.example.chattingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> mChats;
    private String imageurl;

    private FirebaseUser fuser;

    public MessageAdapter(Context context, List<Chat> mChats, String imageurl) {
        this.mChats = mChats;
        this.context = context;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mChats.get(position);

        holder.show_message.setText(chat.getMessage());

        if(imageurl.equals("default")){
            holder.profile_image.setImageResource(R.drawable.default_img);
        }
        else{
            Glide.with(context).load(imageurl).into(holder.profile_image);
        }

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public CircleImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = (TextView) itemView.findViewById(R.id.show_message);
            profile_image = (CircleImageView) itemView.findViewById(R.id.profile_image);

        }
    }

    //읽어드린 메세지를 왼쪽에 보여줄지 오른쪽에 보여줄지 결정하는 메서드
    @Override
    public int getItemViewType(int position) {

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if(mChats.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }

    }
}

