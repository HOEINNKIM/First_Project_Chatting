package user.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import user.dao.displayDAO;
import user.vo.ChangeWord;
import user.vo.User;

public class UserClientMain extends Application {
	displayDAO dao = new displayDAO();			// DB와 연결하는 메소드를 담는 DAO객체 생성.
	Socket socket; 								// 유저의 소켓 정보
	String id; 									// 클라이언트 대화명을 저장
	boolean checkId = false;					// 로그인시 필요한 id의 플래그
	int flag; 									// !(1, 2, 3, 4, 5, 6)를 고를 경우의 수를 정하여 mybatis와 연동
	int countChange = 0;						// 모든 출력 메소드의 출력 개수를 저장.
	String chooseWord;							// 단어 변경할 oldMessage 저장해서 ChangeWord 객체로 전달.
	ChangeWord changeWord = new ChangeWord();	// update 구문에 대입할 oldMessage와 newMessage를 저장하는 객체	

	
	//1. start버튼을 클릭했을 때 커넥션 스레드 시작 + receive 메서드로 inputstream 작업을 반복
	void startClient() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress("192.168.168.22", 5001));
					Platform.runLater(() -> { 	
						displayText("[SIRIAI에 연결되었습니다. 어서오세요!]"
								+ "\n<SIRIAI> : !!!!!!사용할 대화명을 입력하세요!!!!!!");
						btnConn.setText("stop");	//연결 버튼의 문자열 변경
						btnSend.setDisable(false);	//send 버튼 활성화
					});
				} catch (Exception e) {
					Platform.runLater(() -> displayText("<SIRIAI> : connect 실패!"));
					if (!socket.isClosed()) { stopClient();}
					return;
				}
				receive();
			}
		};
		thread.start();
	}

	//socket을 닫는 메서드.(에러 발생 혹은 창을 종료할 때)
	public void stopClient() {
		try {
			Platform.runLater(() -> {
				displayText("<SIRIAI> : 연결을 종료합니다. またあいましょう！");
				btnConn.setText("start");
				btnSend.setDisable(true);
			});
			checkId = false;
			if (socket != null && !socket.isClosed()) {socket.close();}
		} catch (IOException e) {}
	}

	//서버로부터 output된 메세지를 받는 메서드
	public void receive() {
		while (true) {
			try {
				byte[] byteArr = new byte[300];
				InputStream inputStream = socket.getInputStream();

				//읽은 내용을 byteArr 배열에 저장하고 읽은 byte 수를 int로 반환.
				// 서버가 비정상적으로 종료했을 경우 IOException 발생
				int readByteCount = inputStream.read(byteArr);
				
				// 서버가 정상적으로 Socket의 close()를 호출했을 경우
				// 소켓이 닫히면서 end of file 즉 -1의 상태로 넘어가므로 
				// exception으로 넘겨 catch 작업을 수행
				if (readByteCount == -1) {throw new IOException();}

				String text = new String(byteArr, 0, readByteCount, "UTF-8");
				Platform.runLater(() -> displayText(text));
				
				User user = new User(text.substring(1, text.indexOf("]")), text.substring(text.indexOf(": ")+2));
				dao.insertChat(user);
			} catch (Exception e) {
				Platform.runLater(() -> displayText("<SIRIAI> : receive 실패!"));
				stopClient();
				break;
			}
		}
	}

	//send 버튼을 클릭했을 때 작업
	//(1) 스레드를 outputstream으로 서버에 메세지 전달
	//(2) !명령어를 통해 DB와 연동하는 작업을 수행
	public void send(String sendText) {

		//0. 아이디를 설정
		if(checkId==false) {
			checkId=true;
			id = sendText;
			Platform.runLater(() -> {
				displayText("<SIRIAI> : 아이디를 [" + id + "] 로 설정하였습니다.");
			});
			return;
		}

		//2. 날짜로 검색
		if (flag == 2) { 
			flag = 0;
			SimpleDateFormat formatCheck = new SimpleDateFormat("yyMMdd", Locale.KOREA);
			formatCheck.setLenient(false);
			try {			
				formatCheck.parse(sendText);
				showbyDateMethod(sendText);
			} catch (Exception E) {
				Platform.runLater(() -> {displayText("<SIRIAI> : 날짜 형식에 맞지 않습니다. 명령어를 다시 입력하세요.");});
			}

		//3. 단어로 검색
		} else if (flag == 3) { 
			flag = 0;
			showbyWordMethod(sendText);

		//5. 날짜로 대화 내용 삭제
		} else if (flag == 5) {
			flag = 0;

			SimpleDateFormat formatCheck = new SimpleDateFormat("yyMMdd", Locale.KOREA);
			formatCheck.setLenient(false);
			try {
				formatCheck.parse(sendText);
				int num = dao.deleteByChatDate(sendText);
				if (num == 0) {
					Platform.runLater(() -> {displayText("<SIRIAI> : 삭제할 내용이 없습니다.");});
				} else {
					Platform.runLater(() -> {displayText("<SIRIAI> : 총 "+ num + "개의 대화를 삭제하였습니다.");});
				}
			} catch (Exception E) {
				Platform.runLater(() -> {displayText("<SIRIAI> : 날짜 형식에 맞지 않습니다. 명령어를 다시 입력하세요.");});
			}

		//6.7. 변경할 단어를 입력 받아 변경
		} else if (flag == 6) {
			chooseWord = sendText;
			Platform.runLater(() -> {
				displayText("<SIRIAI> : 변경할 단어를 입력하세요");
			});
			flag = 7;

		} else if (flag == 7) {
			flag = 0;
			countChange = 0;

			ArrayList<User> showText = dao.showByText("%" + chooseWord + "%");
			for (User text : showText) {
				String changed = text.getText().replace(chooseWord, sendText);
				changeWord.setOldMessage(text.getText());
				changeWord.setNewMessage(changed);
				dao.updateWord(changeWord);
				countChange++;
			}
			Platform.runLater(() -> {
				displayText("<SIRIAI> : 총[ " + countChange + " ]개 변경 되었습니다.");
				countChange = 0;
			});

		} else {

			Thread thread = new Thread() {
				@Override
				public void run() {

					try {
		
						//1. 모든 대화 내용을 출력
						if (sendText.equals("!1")) {
							showAllMethod();
							return;
						
						//(2)와 연결
						} else if (sendText.equals("!2")) { 
							Platform.runLater(() -> displayText("<SIRIAI> : 날짜를 입력하세요(YYMMDD)의 형식으로만 입력해주세요."));
							flag = 2;
							return;
							
						//(3)과 연결
						} else if (sendText.equals("!3")) { 
							Platform.runLater(() -> displayText("<SIRIAI> : 검색할 단어를 입력하세요!"));
							flag = 3;
							return;
							
						//4. 본인의 대화명으로 내용 출력	
						} else if (sendText.equals("!4")) { 
							countChange = 0;
							Platform.runLater(() -> displayText("<SIRIAI> : 당신의 대화명을 바탕으로 대화 내용을 불러옵니다."));
							ArrayList<User> showId = dao.showById(id);
							for (User user : showId) {
								countChange++;
								Platform.runLater(() -> {
									displayText("[" + id + "] [" + user.getChatDate() + "] : " + user.getText());
								});
							}
							Platform.runLater(() -> {
								displayText("총[ " + countChange + " ]개 조회되었습니다.");
								countChange = 0;
							});
							return;
							
						//(5)와 연결
						} else if (sendText.equals("!5")) {
							Platform.runLater(() -> displayText("[날짜를 입력하세요(YYMMDD)의 형식으로만 입력해주세요]"));
							flag = 5;
							return;
							
						//(6)과 연결
						} else if (sendText.equals("!6")) {
							Platform.runLater(() -> displayText("[검색할 단어를 입력하세요]"));
							flag = 6;
							return;
						}
						
						//이외의 경우 outputstream으로 서버에 전달
						String wholeText = "[" + id + "] : " + sendText;
						byte[] byteArr = wholeText.getBytes("UTF-8");
						OutputStream outputStream = socket.getOutputStream();
						outputStream.write(byteArr);
						outputStream.flush();

					} catch (Exception e) {
						Platform.runLater(() -> displayText("[서버 통신 안됨]"));
						stopClient();
					}
				}
			};
			thread.start();
		}
	}

	//(1)과 연결 : 모든 대화 내용을 출력
	public void showAllMethod() {
		countChange = 0;
		ArrayList<User> showAll = dao.showAll();
		for (User user : showAll) {
			Platform.runLater(() -> {
				displayText("[" + user.getId() + "] [" + user.getChatDate() + "] : " + user.getText());
			});
			countChange++;
		}
		Platform.runLater(() -> {
			displayText("총[ " + countChange + " ]개 조회되었습니다.");
			countChange = 0;
		});		
	}
	
	//(2)와 연결 : 날짜로 대화 내용 출력
	public void showbyDateMethod(String date) {
		countChange = 0;
		ArrayList<User> showByChatDate = dao.showByChatDate(date);
		for (User user : showByChatDate) {
			Platform.runLater(() -> {
				displayText("[" + user.getId() + "] [" + user.getChatDate() + "] : " + user.getText());
			});
			countChange++;
		}
		Platform.runLater(() -> {
			displayText("총[ " + countChange + " ]개 조회되었습니다.");
			countChange = 0;
		});
	}
	
	//(3)과 연결 : 단어를 받아서 대화 내용 출력
	private void showbyWordMethod(String Text) {
		countChange = 0;
		ArrayList<User> showText = dao.showByText("%" + Text + "%");
		for (User user : showText) {
			Platform.runLater(() -> {
				displayText("[" + user.getId() + "] [" + user.getChatDate() + "] : " + user.getText());
			});
			countChange++;
		}
		Platform.runLater(() -> {
			displayText("총[ " + countChange + " ]개 조회되었습니다.");
			countChange = 0;
		});
	}


	

	//////////////////////////////////////////////////////
	TextArea txtDisplay;		//텍스트 내용 표시
	TextField txtInput;			//채팅 내용 입력
	Button btnConn, btnSend;	//연결, 전송
	TextArea funcShow;			//기능만 보여주는 표현

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane();
		root.setPrefSize(1000, 400);

		// 텍스트 보이는 창 설정
		txtDisplay = new TextArea();
		txtDisplay.setEditable(false);
		txtDisplay.setFont(Font.font("굴림", FontWeight.EXTRA_BOLD, 15));
		BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
		txtDisplay.getStylesheets().add(getClass().getResource("app2.css").toString());
		root.setCenter(txtDisplay);

		// START 버튼, SEND 버튼, TXTINPUT창 설정
		BorderPane bottom = new BorderPane();
		
		//TXTINPUT 설정
		txtInput = new TextField();
		txtInput.setPrefSize(60, 30);
		BorderPane.setMargin(txtInput, new Insets(0, 1, 1, 1));

		//START/STOP 버튼 설정
		btnConn = new Button("start");
		btnConn.setPrefSize(60, 30);
		btnConn.setOnAction(e -> {
			if (btnConn.getText().equals("start")) {
				startClient();
			} else if (btnConn.getText().equals("stop")) {
				stopClient();
			}
		});

		//SEND 버튼 설정
		btnSend = new Button("send");
		btnSend.setPrefSize(60, 30);
		btnSend.setDisable(true);
		btnSend.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent e) {
				send(txtInput.getText());
			}
		});

		bottom.setCenter(txtInput);
		bottom.setLeft(btnConn);
		bottom.setRight(btnSend);
		root.setBottom(bottom);

		// 왼쪽에 텍스트가 보이는 창 설정
		BorderPane left = new BorderPane();
		funcShow = new TextArea();
		funcShow.setPrefSize(400, 500);
		BorderPane.setMargin(funcShow, new Insets(0, 0, 2, 0));
		funcShow.appendText("시리아이의 기능을 소개합니다.\n" 
		+ "!1 : 현재까지의 대화 내용 보기\n" 
		+ "!2 : 날짜를 검색하여 대화내용 보기(YYMMDD)\n"
		+ "!3 : 검색할 단어로 대화내용 보기\n" 
		+ "!4 : 접속한 대화명으로 쓴 대화만 조회\n" 
		+ "!5 : 특정 날짜에 해당하는 대화내용 삭제(YYMMDD)\n"
		+ "!6 : 특정 단어를 모두 다른 단어로 변경하기\n");
		root.setLeft(funcShow);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("app.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	//textArea에 문자를 출력
	void displayText(String text) {
		txtDisplay.appendText(text + "\n");
	}

	//메인 : javafx를 실행
	public static void main(String[] args) {
		launch(args);
	}
}