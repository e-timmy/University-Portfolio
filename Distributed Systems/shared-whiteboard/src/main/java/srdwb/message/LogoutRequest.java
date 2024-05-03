package srdwb.message;

@JsonSerializable
public class LogoutRequest extends Message {

    @JsonElement
    public String message;

    public LogoutRequest() {}

    public LogoutRequest(String message) {this.message = message == null ? "" : message;}

}
