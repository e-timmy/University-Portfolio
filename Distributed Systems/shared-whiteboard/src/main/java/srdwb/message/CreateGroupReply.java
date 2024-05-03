package srdwb.message;

@JsonSerializable
public class CreateGroupReply extends Message {

    @JsonElement
    public String message;
    @JsonElement
    public String groupName;
    @JsonElement
    public User user;
    @JsonElement
    public Boolean outcome;
    @JsonElement
    public GroupEntry group;

    public CreateGroupReply() {

    }

    public CreateGroupReply(String message, String groupName, User user, Boolean outcome, GroupEntry groupEntry) {

    	this.message = message == null ? "" : message;
        this.groupName = groupName;
        this.user = user;
        this.outcome = outcome;
        this.group = groupEntry;
    }
}
