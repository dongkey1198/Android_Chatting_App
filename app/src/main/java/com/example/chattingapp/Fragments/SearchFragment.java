package com.example.chattingapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Adapter.UserAdapter;
import com.example.chattingapp.Models.User;
import com.example.chattingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// 가입된 사용자들을 보여주는 페이지(**검색기능 추가할 예정)
// Firebase Real Time Database 안에 존재하는 모든 User 정보를 부러온다
// FirebaseAuth.getInstance.getCurrentUser()를 사용하여 현재 로그인 상태인 사용자를 제외한
// 나머지 사용자 정보를 모두 ArrayList에 담은후 UserAdapter로 본내다
// 그후 RecyclerView를 통해 모두 UI에 나오게한다.

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    private EditText edit_text_search;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_search, container, false);

        edit_text_search = (EditText)view.findViewById(R.id.edit_text_search);
        edit_text_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView = view.findViewById(R.id.recyler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        mUsers = new ArrayList<>();
        getUsers();

        return view;
    }

    private void searchUsers(String s) {

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("name").startAt(s).endAt(s+"\uf0ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!edit_text_search.getText().toString().equals("")){
                    mUsers.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);

                        assert user != null;
                        assert fuser != null;
                        if(!user.getUserId().equals(fuser.getUid())){
                            mUsers.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(getContext(), mUsers);
                    recyclerView.setAdapter(userAdapter);
                }
                else{
                    getUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUsers() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot data : snapshot.getChildren()){

                    User user = data.getValue(User.class);

                    assert user != null;
                    assert firebaseUser != null;
                    if(!user.getEmail().equals(firebaseUser.getEmail())){
                        mUsers.add(user);
                    }

                }

                userAdapter = new UserAdapter(getContext(), mUsers);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}