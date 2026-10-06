package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class RoleFile {
    Role role;
    List<Candidate> candidates;
    public RoleFile() {
        role = null;
        candidates = new ArrayList<>();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}
