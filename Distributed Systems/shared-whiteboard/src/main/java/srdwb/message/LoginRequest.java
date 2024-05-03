package srdwb.message;

@JsonSerializable
public class LoginRequest extends Message {

    @JsonElement
    public String username;
    @JsonElement
    public int updatePort;
    @JsonElement
    public String serverSecret;

    public LoginRequest() {}

    public LoginRequest(String username, String serverSecret) {
        this.username = username;
        this.updatePort = updatePort;
        this.serverSecret = serverSecret;

    }
}
