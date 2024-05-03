package srdwb.message;

@JsonSerializable
public class JoinGroupRequest extends Message {

    @JsonElement
    public String groupName;
    @JsonElement
    public String groupSecret;
    public String groupUUID;
    public User user;

    public JoinGroupRequest() {}

    public JoinGroupRequest(String groupName, String groupSecret, String groupUUID, User user) {
        this.groupName = groupName;
        this.groupSecret = groupSecret;
        this.groupUUID = groupUUID;
        this.user = user;
    }
}
