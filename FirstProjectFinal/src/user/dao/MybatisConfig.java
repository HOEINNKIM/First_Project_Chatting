package user.dao;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisConfig {
	private static SqlSessionFactory sqlSessionFactory;
	
	//static 초기화블럭을 이용하면 클래스가 로딩 될 때 최초 한번 실행하게 된다. 
	//특히나 초기화블럭을 이용하면 logic을 담을 수 있기 때문에 복잡한 초기변수 셋팅이나 위와 같이 에러처리를 위한 구문을 담을 수 있다.
	
	static {
		String resource = "mybatis-config.xml";		//Mybatis 환경설정 파일 읽기. src 경로에 저장.
		

		try {
			Reader reader = Resources.getResourceAsReader(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}
}

