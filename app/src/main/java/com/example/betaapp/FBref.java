package com.example.betaapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * The type references to the firebase db
 * @author Itey Weintraub <av5350@bs.amalnet.k12.il>
 * @version	1
 * short description:
 *
 *      This are consts for the database (itself, to the root, and to the storage)
 */
public class FBref {
    final static FirebaseAuth auth = FirebaseAuth.getInstance();

    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    public static DatabaseReference refUsers = FBDB.getReference("Users");
    public static DatabaseReference refStudents = FBDB.getReference("Students");

    final static StorageReference storageRef = FirebaseStorage.getInstance().getReference();
}
