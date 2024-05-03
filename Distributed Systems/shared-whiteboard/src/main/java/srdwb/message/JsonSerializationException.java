package srdwb.message;

public class JsonSerializationException extends Exception{
	private static final long serialVersionUID = 1L;

	public JsonSerializationException() {
		
	}
	
	public JsonSerializationException(String reason) {
		super(reason);
	}

}
