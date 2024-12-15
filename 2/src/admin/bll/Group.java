package bll;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Group {
    private int groupId;
    private String name;
    private List<String> participants;
    private Date dateCreated;

    
    public Group(int groupId, String name, List<String> participants, Date dateCreated) {
        this.groupId = groupId;
        this.name = name;
        this.participants = participants;
        this.dateCreated = dateCreated;
    }

    public Group(String name, List<String> participants, Date dateCreated) {
        this.name = name;
        this.participants = participants;
        this.dateCreated = dateCreated;
    }

    public Group(String name, List<String> participants) {
        this.name = name;
        this.participants = participants;
    }

    public Group(int group_id, String name) {
        this.groupId = group_id;
        this.name = name;
        this.participants = new ArrayList<>();
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void addParticipant(String participant) {
        participants.add(participant);
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
