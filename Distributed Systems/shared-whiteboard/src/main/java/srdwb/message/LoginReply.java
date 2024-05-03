package srdwb.message;

@JsonSerializable
public class LoginReply extends Message {

    @JsonElement
    public String message;

    @JsonElement
    public Boolean outcome;
    @JsonElement
    public User sessionIdentity;

    public LoginReply() {}

    public LoginReply(String message, Boolean outcome, User sessionIdentity) {
    	this.message = message == null ? "" : message;
        this.outcome = outcome;
        this.sessionIdentity = sessionIdentity;
    }

}
