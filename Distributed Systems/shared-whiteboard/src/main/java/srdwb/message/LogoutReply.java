package srdwb.message;

@JsonSerializable
public class LogoutReply extends Message {

    @JsonElement
    public String message;
    @JsonElement
    public Boolean outcome;

    public LogoutReply() {}

    public LogoutReply(String message, Boolean outcome) {
        this.message = message == null ? "" : message;
        this.outcome = outcome;
    }

}
