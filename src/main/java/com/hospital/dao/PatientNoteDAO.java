package com.hospital.dao;

import com.hospital.model.PatientNote;
import com.hospital.util.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class PatientNoteDAO {
    private static final String COLLECTION_NAME = "notes";

    private MongoCollection<Document> getCollection() {
        MongoDatabase db = MongoConnection.getDatabase();
        if (db == null)
            return null;
        return db.getCollection(COLLECTION_NAME);
    }

    public void save(PatientNote note) {
        MongoCollection<Document> col = getCollection();
        if (col == null)
            return; // Fail safe
        col.insertOne(note.toDocument());
    }

    public List<PatientNote> findByPatientId(int patientId) {
        List<PatientNote> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection();
        if (col == null)
            return list;

        for (Document doc : col.find(eq("patient_id", patientId))) {
            list.add(PatientNote.fromDocument(doc));
        }
        return list;
    }
}
