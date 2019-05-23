package user.dao;

import java.util.ArrayList;

import user.vo.ChangeWord;
import user.vo.User;

public interface UserMapper {

public int	insertChat(User user);
//0, 1 성공하면 1, 실패하면 0
//update 실행된 개수 0~개수만큼
//delete 실행된 개수 0~개수만큼

public ArrayList<User> showAll();
public ArrayList<User> showByChatDate(String text);
public ArrayList<User> showByText(String word);
public ArrayList<User> showByID(String id);
public int deleteByChatDate(String text);
public int updateWord(ChangeWord changeWord);
}
