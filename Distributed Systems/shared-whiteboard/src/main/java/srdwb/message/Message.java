package srdwb.message;

/**
 * to create subclass: <br />
 * Create an empty Constructor <br />
 * At least one field and a default value for it<br />
 */
@JsonSerializable
public class Message {
	public Message() {}

	@Override
	public String toString()  {
		return MessageFactory.toJsonString(this);
	}

}
