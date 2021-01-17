package com.example.chattingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<User> mUsers;

    private String lastMessage;

    public ChatAdapter(Context context, List<User> mUsers){
        this.mUsers = mUsers;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getName());

        if(user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.drawable.default_img);
        }
        else{
            Log.d("mylog", "aaa: " + user.getImageURL());
            Glide.with(context).load(user.getImageURL()).into(holder.profile_image);
        }

        lastMessage(user.getUserId(), holder.last_message);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);

                intent.putExtra("userID", user.getUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username, last_message;
        public CircleImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = (TextView)itemView.findViewById(R.id.user_name);
            last_message = (TextView) itemView.findViewById(R.id.last_message);
            profile_image = (CircleImageView)itemView.findViewById(R.id.profile_image);

        }
    }
    private void lastMessage(String userId, TextView last_message){
        lastMessage = "default";
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(fUser.getUid())){
                        lastMessage = chat.getMessage();
                    }
                }

                if(lastMessage.equals("default")){
                    last_message.append("메세지가 없습니다.");
                }
                else{
                    if (lastMessage.contains("file://") || lastMessage.contains("content://")){
                        last_message.append("이미지");
                    }
                    else{
                        last_message.append(lastMessage);
                    }
                }

                lastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
