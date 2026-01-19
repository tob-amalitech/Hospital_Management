package com.hospital.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PatientNote {
    private ObjectId id;
    private int patientId;
    private LocalDateTime createdAt;
    private String content;
    private Map<String, Object> metadata = new HashMap<>();

    public PatientNote() {
        this.createdAt = LocalDateTime.now();
    }

    public PatientNote(int patientId, String content) {
        this.patientId = patientId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // Convert from MongoDB Document
    public static PatientNote fromDocument(Document doc) {
        PatientNote note = new PatientNote();
        note.setId(doc.getObjectId("_id"));
        note.setPatientId(doc.getInteger("patient_id"));
        note.setContent(doc.getString("content"));

        Date date = doc.getDate("created_at");
        if (date != null) {
            note.setCreatedAt(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        Document metaDoc = (Document) doc.get("metadata");
        if (metaDoc != null) {
            note.setMetadata(new HashMap<>(metaDoc));
        }
        return note;
    }

    // Convert to MongoDB Document
    public Document toDocument() {
        Document doc = new Document();
        if (id != null)
            doc.append("_id", id);
        doc.append("patient_id", patientId);
        doc.append("content", content);
        doc.append("created_at", Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()));
        doc.append("metadata", metadata);
        return doc;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return createdAt.toString() + ": " + content;
    }
}
