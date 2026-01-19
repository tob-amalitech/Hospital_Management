package com.hospital.service;

import com.hospital.dao.PatientNoteDAO;
import com.hospital.model.PatientNote;

import java.util.List;

public class NoteService {
    private final PatientNoteDAO dao = new PatientNoteDAO();

    public void addNote(int patientId, String content) {
        PatientNote note = new PatientNote(patientId, content);
        dao.save(note);
    }

    public List<PatientNote> getNotes(int patientId) {
        return dao.findByPatientId(patientId);
    }
}
