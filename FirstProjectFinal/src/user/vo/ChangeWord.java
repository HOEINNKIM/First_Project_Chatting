package user.vo;

public class ChangeWord {
	String oldMessage;
	String newMessage;

	
	public ChangeWord() {}

	public ChangeWord(String oldMessage, String newMessage) {
		this.oldMessage = oldMessage;
		this.newMessage = newMessage;
	}

	public String getOldMessage() {
		return oldMessage;
	}

	public void setOldMessage(String oldMessage) {
		this.oldMessage = oldMessage;
	}

	public String getNewMessage() {
		return newMessage;
	}

	public void setNewMessage(String newMessage) {
		this.newMessage = newMessage;
	}
	
	
}
