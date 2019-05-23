package user.dao;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import user.vo.ChangeWord;
import user.vo.User;

public class displayDAO {
	private SqlSessionFactory factory = MybatisConfig.getSqlSessionFactory(); // 마이바티스 객체

	// 채팅 내용 저장
	public void insertChat(User user) {
		SqlSession ss = null;

		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			mapper.insertChat(user);
			ss.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
	}

	// 전체 내용 불러오기
	public ArrayList<User> showAll() {
		SqlSession ss = null;
		ArrayList<User> list = null;

		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			list = mapper.showAll();
			ss.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
		return list;
	}

	// 날짜에 해당하는 내용 불러오기
	public ArrayList<User> showByChatDate(String date) {
		SqlSession ss = null;
		ArrayList<User> list = null;
		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			list = mapper.showByChatDate(date);
			ss.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
		return list;
	}

	// 단어에 해당하는 내용 불러오기
	public ArrayList<User> showByText(String word) {
		SqlSession ss = null;
		ArrayList<User> list = null;

		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			list = mapper.showByText(word);
			ss.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
		return list;
	}

	// ip주소에 해당하는 내용 불러오기
	public ArrayList<User> showById(String id) {
		SqlSession ss = null;
		ArrayList<User> list = null;

		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			list = mapper.showByID(id);
			ss.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
		return list;
	}
	
	// 날짜로 삭제
	public int deleteByChatDate(String date) {
		SqlSession ss = null;

		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			int num = mapper.deleteByChatDate(date);
			ss.commit();
			return num;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
			return 0;
	}
	
	public int updateWord(ChangeWord changeWord) {
		SqlSession ss = null;

		try {
			ss = factory.openSession();
			UserMapper mapper = ss.getMapper(UserMapper.class);
			int num = mapper.updateWord(changeWord);
			ss.commit();
			return num;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null)
				ss.close();
		}
			return 0;
		
		
		
	}
	
	
}
