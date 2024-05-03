package srdwb.message;

@JsonSerializable
public class CanvasUpdateReply extends Message {

    @JsonElement
    public String message;
    @JsonElement
    public Boolean outcome;

    public CanvasUpdateReply() {};

    public CanvasUpdateReply(String message, Boolean outcome) {
        this.message = message == null ? "" : message;
        this.outcome = outcome;
    }
}
