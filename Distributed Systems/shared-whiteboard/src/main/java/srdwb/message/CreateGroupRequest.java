package srdwb.message;

@JsonSerializable
public class CreateGroupRequest extends Message {

    @JsonElement
    public String groupName;
    @JsonElement
    public String groupSecret;
    @JsonElement
    public String message;
    @JsonElement
    public Integer port;

    public CreateGroupRequest() {}
    public CreateGroupRequest(String groupName, String groupSecret, String message, int port) {
        this.groupName = groupName;
        this.groupSecret = groupSecret;
        this.message = message == null ? "" : message;
        this.port = port;
    }

}
