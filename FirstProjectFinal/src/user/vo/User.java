package user.vo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Platform;

// DB에 저장하기 위한 클래스 생성
public class User {

	private String id; // IP주소를 바탕으로 자기 id설정
	private String text; // 대화 내용
	private String chatDate; // 날짜를 저장
	

	// mybatis를 실행하기 위한 기본 생성자
	public User() {
	}

	
	
	public User(String id, String text) {
		this.id = id;
		this.text = text;
	}

	// 대화내용을 저장하기 위한 객체 생성해서 DB에 전달
	public User(String id, String text, String chatDate) {
		this.id = id;
		this.text = text;
		this.chatDate = chatDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getChatDate() {
		return chatDate;
	}

	public void setChatDate(String chatDate) {
		this.chatDate = chatDate;
	}


}