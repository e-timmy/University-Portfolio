package srdwb.message;

import srdwb.Shapes.Shape;

import java.util.ArrayList;
import java.util.HashMap;

@JsonSerializable
public class JoinGroupReply extends Message {

    @JsonElement
    public String message;
    @JsonElement
    public String groupName;
    @JsonElement
    public HashMap<String,User> userList;
    @JsonElement
    public GroupEntry group;
    @JsonElement
    public ArrayList<Shape> state;

    @JsonElement
    public Boolean outcome;
    public User user;

    public JoinGroupReply() {}

    public JoinGroupReply(String message, String groupName, HashMap<String,User> userList,
                          ArrayList<Shape> state, GroupEntry groupEntry, Boolean outcome, User user) {
    	this.message = message == null ? "" : message;
        this.groupName = groupName;
        this.userList = userList;
        this.state = state;
        this.outcome = outcome;
        this.group = groupEntry;
        this.user = user;
    }
}
